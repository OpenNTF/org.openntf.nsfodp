package org.openntf.nsfodp.compiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.odp.util.ODPUtil;
import org.openntf.nsfodp.compiler.update.UpdateSite;
import org.openntf.nsfodp.compiler.util.LibraryWeightComparator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.interpreter.DynamicXPageBean;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.registry.FacesLibraryImpl;
import com.ibm.xsp.registry.FacesProject;
import com.ibm.xsp.registry.FacesProjectImpl;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.UpdatableLibrary;
import com.ibm.xsp.registry.config.IconUrlSource;
import com.ibm.xsp.registry.config.ResourceBundleSource;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.config.XspRegistryProvider;

/**
 * Abstract parent to handle an XPages compilation environment.
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public abstract class AbstractCompilationEnvironment {

	protected final BundleContext bundleContext;
	protected final Set<UpdateSite> updateSites = new LinkedHashSet<>();
	protected final Set<Path> classPathEntries = new LinkedHashSet<>();
	protected final SharableRegistryImpl facesRegistry = new SharableRegistryImpl(getClass().getPackage().getName());
	protected final FacesProject facesProject;
	protected final DynamicXPageBean dynamicXPageBean = new DynamicXPageBean();
	protected final ResourceBundleSource resourceBundleSource;
	protected final IconUrlSource iconUrlSource = icon -> getClass().getResource(icon);
	protected final IProgressMonitor mon;
	
	public AbstractCompilationEnvironment(BundleContext bundleContext, ResourceBundleSource resourceBundleSource, IProgressMonitor mon) {
		this.bundleContext = Objects.requireNonNull(bundleContext);
		this.facesProject = new FacesProjectImpl(getClass().getPackage().getName(), facesRegistry);
		this.resourceBundleSource = resourceBundleSource;
		this.mon = mon;
	}

	public void addUpdateSite(UpdateSite updateSite) {
		this.updateSites.add(updateSite);
	}

	/**
	 * Adds a JAR path to the XPages compilation classpath.
	 * 
	 * @param entry the path to the entry to add
	 * @since 2.5.0
	 */
	public void addClassPathEntry(Path entry) {
		if(entry != null) {
			this.classPathEntries.add(entry);
		}
	}

	protected Collection<Bundle> installBundles() {
		subTask(Messages.ODPCompiler_installingBundles);
		
		Collection<Bundle> result = updateSites.stream()
			.map(UpdateSite::getBundleURIs)
			.flatMap(Collection::stream)
			.map(this::installBundle)
			.collect(Collectors.toList()).stream() // Force waiting until installation is complete
			.filter(Objects::nonNull)
			.map(this::startBundle)
			.collect(Collectors.toList());
		subTask(MessageFormat.format(Messages.ODPCompiler_installedBundles, result.size()));
		return result;
	}

	protected void uninstallBundles(Collection<Bundle> bundles) {
		subTask(Messages.ODPCompiler_uninstallingBundles);
		
		bundles.stream().forEach(t -> {
			try {
				if(t.getState() == Bundle.RESOLVED && StringUtil.isEmpty(t.getHeaders().get("Fragment-Host"))) {
					t.stop();
				}
				t.uninstall();
			} catch (BundleException e) {
				throw new RuntimeException(e);
			}
		});
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
			BundleContext bundleContext = ODPCompilerActivator.instance.getBundle().getBundleContext();
			bundle = bundleContext.installBundle(uri.toString());
		} catch(Exception e) {
			// Ignore
		}
		return bundle;
	}

	private Bundle startBundle(Bundle bundle) {
		try {
			if(bundle.getHeaders().get("Eclipse-SourceBundle") == null && bundle.getHeaders().get("Fragment-Host") == null) { //$NON-NLS-1$ //$NON-NLS-2$
				bundle.start();
			}
		} catch (BundleException e) {
			if(e.toString().contains("Another singleton bundle selected")) { //$NON-NLS-1$
				// Ignore entirely
			} else {
				// Print the stack trace but move on
				e.printStackTrace();
			}
		}
		return bundle;
	}

	protected void subTask(Object message, Object... params) {
		if(mon != null) {
			mon.subTask(StringUtil.format(StringUtil.toString(message), params));
		}
	}

	/**
	 * Initializes the internal Faces registry with the newly-added plugins.
	 */
	protected void initRegistry() {
		subTask(Messages.ODPCompiler_initializingLibraries);

		List<Object> libraries = ExtensionManager.findServices((List<Object>)null, LibraryServiceLoader.class, "com.ibm.xsp.Library"); //$NON-NLS-1$
		libraries.stream()
			.filter(lib -> lib instanceof XspLibrary)
			.map(XspLibrary.class::cast)
			.sorted(LibraryWeightComparator.INSTANCE)
			.map(lib -> new LibraryWrapper(lib.getLibraryId(), lib))
			.map(wrapper -> {
				SimpleRegistryProvider provider = new SimpleRegistryProvider();
				provider.init(wrapper);
				return provider;
			})
			.map(XspRegistryProvider::getRegistry)
			
			.forEach(facesRegistry::addDepend);
		facesRegistry.refreshReferences();
	}

	protected UpdatableLibrary getLibrary(String namespace) {
		UpdatableLibrary library = (UpdatableLibrary)facesRegistry.getLocalLibrary(namespace);
		if(library == null) {
			try {
				library = new FacesLibraryImpl(facesRegistry, namespace);
				// TODO this is probably properly done by creating a FacesProjectImpl
				// - it can then register the library fragments itself
				Field localLibsField = facesRegistry.getClass().getDeclaredField("_localLibs"); //$NON-NLS-1$
				localLibsField.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<String, UpdatableLibrary> localLibs = (Map<String, UpdatableLibrary>)localLibsField.get(facesRegistry);
				localLibs.put(namespace, library);
			} catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return library;
	}
	
	protected Collection<String> buildDependenciesCollection(Collection<Path> cleanup) throws IOException {
		// Build our classpath
		Collection<String> dependencies = new LinkedHashSet<>();;
		
		// Special support for Notes.jar
		Optional<Bundle> bundle = ODPUtil.findBundle(bundleContext, "com.ibm.notes.java.api.win32.linux", true); //$NON-NLS-1$
		if(bundle.isPresent()) {
			File f = FileLocator.getBundleFile(bundle.get());
			if(!f.exists()) {
				throw new IllegalStateException("Could not locate Notes.jar"); //$NON-NLS-1$
			}
			if(f.isFile()) {
				try(JarFile jar = new JarFile(f)) {
					JarEntry notesJar = jar.getJarEntry("Notes.jar"); //$NON-NLS-1$
					Path tempFile = Files.createTempFile(NSFODPUtil.getTempDirectory(), "Notes", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
					cleanup.add(tempFile);
					try(InputStream is = jar.getInputStream(notesJar)) {
						Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
					}
					dependencies.add("jar:" + tempFile.toUri().toString()); //$NON-NLS-1$
				}
			} else {
				Path path = f.toPath().resolve("Notes.jar"); //$NON-NLS-1$
				Path tempFile = Files.createTempFile("Notes", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
				cleanup.add(tempFile);
				Files.copy(path, tempFile, StandardCopyOption.REPLACE_EXISTING);
				dependencies.add("jar:" + tempFile.toUri().toString()); //$NON-NLS-1$
			}
		}
		
		// Add any explicit JARs
		for(Path jar : this.classPathEntries) {
			dependencies.add("jar:" + jar.toUri()); //$NON-NLS-1$
		}
		
		return dependencies;
	}

}
