/*
 * © Copyright Jesse Gallagher 2018
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.openntf.xsp.extlibx.bazaar.odpcompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.odp.OnDiskProject;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.update.UpdateSite;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODPUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import com.ibm.xsp.extlib.library.BazaarActivator;
import com.ibm.xsp.registry.FacesSharableRegistry;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.commons.xml.XResult;
import com.ibm.xsp.extlib.javacompiler.JavaCompilerException;
import com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;

/**
 * Represents an on-disk project compilation environment.
 * 
 * <p>This class is the primary entry point for ODP compilation.</p>
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class ODPCompiler {
	private final BundleContext bundleContext;
	private final OnDiskProject odp;
	private final FacesSharableRegistry facesRegistry;
	private final Set<UpdateSite> updateSites = new LinkedHashSet<>();
	private List<String> compilerOptions = DEFAULT_COMPILER_OPTIONS;
	
	private static final List<String> DEFAULT_COMPILER_OPTIONS = Arrays.asList("-source", "1.8", "-target", "1.8");
	
	public ODPCompiler(BundleContext bundleContext, OnDiskProject onDiskProject, FacesSharableRegistry facesRegistry) {
		this.bundleContext = Objects.requireNonNull(bundleContext);
		this.odp = Objects.requireNonNull(onDiskProject);
		this.facesRegistry = Objects.requireNonNull(facesRegistry);
	}
	
	public OnDiskProject getOnDiskProject() {
		return odp;
	}
	
	public void addUpdateSite(UpdateSite updateSite) {
		this.updateSites.add(updateSite);
	}
	
	public void setCompilerOptions(Collection<String> compilerOptions) {
		if(compilerOptions == null) {
			this.compilerOptions = DEFAULT_COMPILER_OPTIONS;
		} else {
			this.compilerOptions = new ArrayList<>(compilerOptions);
		}
	}
	
	/**
	 * Runs the compilation process:
	 * 
	 * <ol>
	 * 	<li>Installs all bundles from the provided update sites</li>
	 *	<li>Initializes plugin contributions from installed bundles</li>
	 * 	<li>Compiles all XPage artifacts</li>
	 * 	<li>Constructs the NSF from the on-disk project</li>
	 * 	<li>Uninstalls any installed bundles</li>
	 * </ol>
	 * 
	 * @throws IOException if there is a problem reading files from disk 
	 * @throws XMLException if there is a problem parsing project configuration files
	 * @throws FileNotFoundException if there is a problem reading files from disk
	 * @throws JavaCompilerException if there is a problem compiling Java or XPages code
	 */
	public void compile() throws FileNotFoundException, XMLException, IOException, JavaCompilerException {
		Collection<Bundle> bundles = installBundles();
		try {
			initPlugins(bundles);

			Collection<String> dependencies = ODPUtil.expandRequiredBundles(bundleContext, odp.getRequiredBundles());
			String[] classPath = dependencies.toArray(new String[dependencies.size()]);
			JavaSourceClassLoader classLoader = new JavaSourceClassLoader(getClass().getClassLoader(), compilerOptions, classPath);
			compileJavaSources(classLoader);
			
		} finally {
			uninstallBundles(bundles);
		}
	}
	
	// *******************************************************************************
	// * Bundle manipulation methods
	// *******************************************************************************
	private Collection<Bundle> installBundles() {
		debug("============================");
		debug("Installing bundles");
		
		Collection<Bundle> result = updateSites.stream()
			.map(UpdateSite::getBundleURIs)
			.flatMap(Collection::stream)
			.map(this::installBundle)
			.collect(Collectors.toList()).stream() // Force waiting until installation is complete
			.map(this::startBundle)
			.collect(Collectors.toList());
		debug(MessageFormat.format("- Installed {0,choice,0#no bundles|1# 1 bundle|1<{0} bundles}", result.size()));
		return result;
	}
	
	private void uninstallBundles(Collection<Bundle> bundles) {
		debug("============================");
		debug("Uninstalling bundles");
		
		bundles.stream().forEach(t -> {
			try {
				t.uninstall();
			} catch (BundleException e) {
				throw new RuntimeException(e);
			}
		});
		debug(MessageFormat.format("- Uninstalled {0,choice,0#no bundles|1# 1 bundle|1<{0} bundles}", bundles.size()));
	}
	
	/**
	 * Looks for plugin.xml files inside the provided bundles, initializes their contributions,
	 * and resets the XSP internal library cache.
	 * 
	 * @param bundles the bundles to scan
	 */
	private void initPlugins(Collection<Bundle> bundles) {
		debug("============================");
		debug("Initializing plugins");
		
		AtomicBoolean contributed = new AtomicBoolean(false);
		bundles.stream().forEach(bundle -> {
			URL pluginXml = bundle.getEntry("/plugin.xml");
			if(pluginXml != null) {
				IExtensionRegistry reg = Platform.getExtensionRegistry();
				Object token = ODPUtil.getTemporaryUserToken(reg);
				IContributor contributor = new RegistryContributor(String.valueOf(bundle.getBundleId()), bundle.getSymbolicName(), null, null);
				try {
					try(InputStream is = pluginXml.openStream()) {
						if(!reg.addContribution(is, contributor, false, bundle.getSymbolicName(), null, token)) {
							debug(">> failed to contribute plugin " + bundle.getSymbolicName());
						}
						contributed.set(true);
					}
				} catch(IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		});
		
		if(contributed.get()) {
			// Reset the XSP stack's library cache
			try {
				Class<?> libraryServiceLoaderClass = getClass().getClassLoader().loadClass("com.ibm.xsp.library.LibraryServiceLoader");
				Field idToLib = libraryServiceLoaderClass.getDeclaredField("idToLib");
				idToLib.setAccessible(true);
				idToLib.set(null, null);
				Method method = libraryServiceLoaderClass.getDeclaredMethod("init");
				method.setAccessible(true);
				method.invoke(null);
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Installs the provided bundle.
	 * 
	 * @param uri the platform-accessible URI to the bundle
	 * @return the installed {@link Bundle} object
	 */
	private Bundle installBundle(URI uri) {
		Bundle bundle = null;
		try {
			BundleContext bundleContext = BazaarActivator.instance.getBundle().getBundleContext();
			bundle = bundleContext.installBundle(uri.toString());
		} catch(Exception e) {
			// Ignore
		}
		return bundle;
	}
	private Bundle startBundle(Bundle bundle) {
		try {
			if(bundle.getHeaders().get("Eclipse-SourceBundle") == null && bundle.getHeaders().get("Fragment-Host") == null) {
				bundle.start();
			}
		} catch (BundleException e) {
			e.printStackTrace();
			// Ignore
		}
		return bundle;
	}
	
	// *******************************************************************************
	// * Class compilation methods
	// *******************************************************************************
	
	private void compileJavaSources(JavaSourceClassLoader classLoader) throws FileNotFoundException, XMLException, IOException, JavaCompilerException {
		debug("============================");
		debug("Compiling Java source");
		
		Map<String, CharSequence> sources = findSourceFolders().stream()
			.map(path -> new File(odp.getBaseDirectory(), path))
			.collect(Collectors.toMap(
				File::toPath,
				ODPUtil::listJavaFiles
			))
			.entrySet().stream()
			.map(entry ->
				// Convert to a map of class name -> source
				entry.getValue().stream()
					.collect(Collectors.toMap(
						path -> ODPUtil.toJavaClassName(entry.getKey().relativize(path)),
						ODPUtil::readFile
					))
			)
			.map(Map::entrySet)
			.flatMap(Set::stream)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		debug(MessageFormat.format("Compiling {0,choice,0#no classes|1# 1 class|1<{0} classes}", sources.size()));
		Map<String, Class<?>> classes = classLoader.addClasses(sources);
	}
	
	private List<String> findSourceFolders() throws FileNotFoundException, IOException, XMLException {
		File classpath = odp.getClasspathFile();
		Document domDoc;
		try(InputStream is = new FileInputStream(classpath)) {
			domDoc = DOMUtil.createDocument(is);
		}
		XResult xresult = DOMUtil.evaluateXPath(domDoc, "/classpath/classpathentry[kind=src]");
		List<String> paths = Arrays.stream(xresult.getNodes())
			.map(node -> Element.class.cast(node))
			.map(el -> el.getAttribute("path"))
			.filter(path -> !"Local".equals(path))
			.collect(Collectors.toList());
		paths.add("Code/Java");
		return paths;
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private void debug(Object message) {
		System.out.println(message);
	}
}
