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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.odp.CustomControl;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.odp.OnDiskProject;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.odp.XPage;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.odp.XSPCompilationResult;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.update.UpdateSite;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.MultiPathResourceBundleSource;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODPUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.w3c.dom.Document;

import com.ibm.xsp.extlib.library.BazaarActivator;
import com.ibm.xsp.library.FacesClassLoader;
import com.ibm.xsp.registry.CompositeComponentDefinitionImpl;
import com.ibm.xsp.registry.FacesLibrary;
import com.ibm.xsp.registry.FacesLibraryImpl;
import com.ibm.xsp.registry.FacesProject;
import com.ibm.xsp.registry.FacesProjectImpl;
import com.ibm.xsp.registry.FacesSharableRegistry;
import com.ibm.xsp.registry.LibraryFragmentImpl;
import com.ibm.xsp.registry.UpdatableLibrary;
import com.ibm.xsp.registry.config.IconUrlSource;
import com.ibm.xsp.registry.config.ResourceBundleSource;
import com.ibm.xsp.registry.parse.ConfigParser;
import com.ibm.xsp.registry.parse.ConfigParserFactory;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.xsp.extlib.interpreter.DynamicFacesClassLoader;
import com.ibm.xsp.extlib.interpreter.DynamicXPageBean;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.DefaultControlFactory;
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
	private final PrintStream out;
	
	// XSP compiler components
	private final FacesProject facesProject;
	private final DynamicXPageBean dynamicXPageBean = new DynamicXPageBean();
	private final ResourceBundleSource resourceBundleSource;
	private final IconUrlSource iconUrlSource = new IconUrlSource() {
		@Override public URL getIconUrl(String arg0) {
			// TODO ???
			return null;
		}
	};
	
	private static final List<String> DEFAULT_COMPILER_OPTIONS = Arrays.asList(
			"-source", "1.8",
			"-target", "1.8",
			"-g",
			"-parameters",
			"-encoding", "utf-8"
			);
	
	public ODPCompiler(BundleContext bundleContext, OnDiskProject onDiskProject, FacesSharableRegistry facesRegistry, PrintStream out) throws FileNotFoundException, XMLException, IOException {
		this.bundleContext = Objects.requireNonNull(bundleContext);
		this.odp = Objects.requireNonNull(onDiskProject);
		this.facesRegistry = Objects.requireNonNull(facesRegistry);
		this.out = out;
		this.facesProject = new FacesProjectImpl(getClass().getPackage().getName(), facesRegistry);
		this.resourceBundleSource = new MultiPathResourceBundleSource(odp.getResourcePaths());
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
	 * @throws Exception 
	 */
	public void compile() throws Exception {
		Collection<Bundle> bundles = installBundles();
		try {
			initPlugins(bundles);

			// Compile Java classes
			Collection<String> dependencies = ODPUtil.expandRequiredBundles(bundleContext, odp.getRequiredBundles());
			String[] classPath = dependencies.toArray(new String[dependencies.size()]);
			JavaSourceClassLoader classLoader = new JavaSourceClassLoader(getClass().getClassLoader(), compilerOptions, classPath);
			
			Map<String, Class<?>> javaClasses = compileJavaSources(classLoader);
			Map<CustomControl, XSPCompilationResult> customControls = compileCustomControls(classLoader);
			Map<XPage, XSPCompilationResult> xpages = compileXPages(classLoader);
			
		} finally {
			uninstallBundles(bundles);
		}
	}
	
	// *******************************************************************************
	// * Bundle manipulation methods
	// *******************************************************************************
	private Collection<Bundle> installBundles() {
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
		debug("Uninstalling bundles");
		
		bundles.stream().forEach(t -> {
			try {
				t.uninstall();
			} catch (BundleException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	/**
	 * Looks for plugin.xml files inside the provided bundles, initializes their contributions,
	 * and resets the XSP internal library cache.
	 * 
	 * @param bundles the bundles to scan
	 */
	private void initPlugins(Collection<Bundle> bundles) {
		debug("Initializing plugins");
		
		// TODO is this step necessary? They may be automatically contributed, based on the "Duplicate library ID" messages on the console 
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
	
	private Map<String, Class<?>> compileJavaSources(JavaSourceClassLoader classLoader) throws FileNotFoundException, XMLException, IOException, JavaCompilerException {
		debug("Compiling Java source");
		
		Map<String, CharSequence> sources = odp.getJavaSourceFiles().entrySet().stream()
			.map(entry ->
				// Convert to a map of class name -> source
				entry.getValue().stream()
					.collect(Collectors.toMap(
						source -> ODPUtil.toJavaClassName(entry.getKey().relativize(source.getDataFile())),
						source -> ODPUtil.readFile(source.getDataFile())
					))
			)
			.map(Map::entrySet)
			.flatMap(Set::stream)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		debug(MessageFormat.format("- Compiling {0,choice,0#no classes|1# 1 class|1<{0} classes}", sources.size()));
		return classLoader.addClasses(sources);
	}
	
	// *******************************************************************************
	// * XSP compilation methods
	// *******************************************************************************
	
	private Map<CustomControl, XSPCompilationResult> compileCustomControls(JavaSourceClassLoader classLoader) throws Exception {
		debug("Compiling custom controls");
		
		ConfigParser configParser = ConfigParserFactory.getParserInstance();
		FacesClassLoader facesClassLoader = new DynamicFacesClassLoader(dynamicXPageBean, classLoader);
		
		Map<CustomControl, XSPCompilationResult> result = new LinkedHashMap<>();
		
		for(CustomControl cc : odp.getCustomControls()) {
			Document xspConfig = ODPUtil.readXml(cc.getXspConfigFile());
			// TODO support alternate namespaces/prefixes
			String namespace = DefaultControlFactory.XC_NS;
			Path fileName = cc.getXspConfigFile().relativize(odp.getBaseDirectory());
			LibraryFragmentImpl fragment = (LibraryFragmentImpl)configParser.createFacesLibraryFragment(
					facesProject,
					facesClassLoader,
					fileName.toString(),
					xspConfig.getDocumentElement(),
					resourceBundleSource,
					iconUrlSource,
					namespace
			);
			
			UpdatableLibrary library = getLibrary();
			library.addLibraryFragment(fragment);
			
			// Load the definition to refresh its parent ref
			CompositeComponentDefinitionImpl def = (CompositeComponentDefinitionImpl)library.getDefinition(cc.getControlName());
			def.refreshReferences();
			
			// Now actually translate
			XSPCompilationResult compilationResult = compileXSP(cc);
			result.put(cc, compilationResult);
		}
		
		return result;
	}
	
	private Map<XPage, XSPCompilationResult> compileXPages(JavaSourceClassLoader classLoader) throws Exception {
		debug("Compiling XPages");
		Map<XPage, XSPCompilationResult> result = new LinkedHashMap<>();
		
		for(XPage xpage : odp.getXPages()) {
			XSPCompilationResult compilationResult = compileXSP(xpage);
			result.put(xpage, compilationResult);
		}
		
		return result;
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private void debug(Object message, Object... params) {
		if(out != null) {
			out.println(StringUtil.format(StringUtil.toString(message), params));
		}
	}
	
	private UpdatableLibrary getLibrary() {
		UpdatableLibrary library = (UpdatableLibrary)facesRegistry.getLocalLibrary(DefaultControlFactory.XC_NS);
		if(library == null) {
			try {
				library = new FacesLibraryImpl(facesRegistry, DefaultControlFactory.XC_NS);
				Field localLibsField = facesRegistry.getClass().getDeclaredField("_localLibs");
				localLibsField.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<String, UpdatableLibrary> localLibs = (Map<String, UpdatableLibrary>)localLibsField.get(facesRegistry);
				localLibs.put(DefaultControlFactory.XC_NS, library);
			} catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return library;
	}
	
	private XSPCompilationResult compileXSP(XPage xpage) throws Exception {
		String xspSource = ODPUtil.readFile(xpage.getDataFile());
		String javaSource = dynamicXPageBean.translate(xpage.getJavaClassName(), xpage.getPageName(), xspSource, facesRegistry);
		Class<?> compiled = dynamicXPageBean.compile(xpage.getPageName(), javaSource);
		return new XSPCompilationResult(javaSource, compiled);
	}
}
