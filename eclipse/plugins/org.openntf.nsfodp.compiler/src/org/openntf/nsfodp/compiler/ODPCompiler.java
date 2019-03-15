/**
 * Copyright © 2018-2019 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.nsfodp.compiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.odp.AbstractSplitDesignElement;
import org.openntf.nsfodp.commons.odp.CustomControl;
import org.openntf.nsfodp.commons.odp.FileResource;
import org.openntf.nsfodp.commons.odp.JavaSource;
import org.openntf.nsfodp.commons.odp.LotusScriptLibrary;
import org.openntf.nsfodp.commons.odp.OnDiskProject;
import org.openntf.nsfodp.commons.odp.XPage;
import org.openntf.nsfodp.commons.odp.XSPCompilationResult;
import org.openntf.nsfodp.commons.odp.util.DXLNativeUtil;
import org.openntf.nsfodp.commons.odp.util.ODPUtil;
import org.openntf.nsfodp.compiler.update.UpdateSite;
import org.openntf.nsfodp.compiler.util.LibraryWeightComparator;
import org.openntf.nsfodp.compiler.util.MultiPathResourceBundleSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.xsp.library.FacesClassLoader;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.registry.CompositeComponentDefinitionImpl;
import com.ibm.xsp.registry.FacesLibraryImpl;
import com.ibm.xsp.registry.FacesProject;
import com.ibm.xsp.registry.FacesProjectImpl;
import com.ibm.xsp.registry.LibraryFragmentImpl;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.UpdatableLibrary;
import com.ibm.xsp.registry.config.IconUrlSource;
import com.ibm.xsp.registry.config.ResourceBundleSource;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.config.XspRegistryProvider;
import com.ibm.xsp.registry.parse.ConfigParser;
import com.ibm.xsp.registry.parse.ConfigParserFactory;

import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.DxlImporter;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;

import com.darwino.domino.napi.DominoAPI;
import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.LotusScriptCompilationException;
import com.darwino.domino.napi.enums.DBClass;
import com.darwino.domino.napi.wrap.NSFDatabase;
import com.darwino.domino.napi.wrap.NSFNote;
import com.darwino.domino.napi.wrap.NSFSession;
import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;
import com.ibm.xsp.extlib.interpreter.DynamicFacesClassLoader;
import com.ibm.xsp.extlib.interpreter.DynamicXPageBean;
import com.ibm.xsp.extlib.javacompiler.JavaCompilerException;
import com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;

/**
 * Represents an on-disk project compilation environment.
 * 
 * <p>This class is the primary entry point for ODP compilation.</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class ODPCompiler {
	private final BundleContext bundleContext;
	private final OnDiskProject odp;
	private final Set<UpdateSite> updateSites = new LinkedHashSet<>();
	private List<String> compilerOptions = DEFAULT_COMPILER_OPTIONS;
	private final IProgressMonitor mon;
	private String compilerLevel = DEFAULT_COMPILER_LEVEL;
	
	// XSP compiler components
	private final SharableRegistryImpl facesRegistry = new SharableRegistryImpl(getClass().getPackage().getName());
	private final FacesProject facesProject;
	private final DynamicXPageBean dynamicXPageBean = new DynamicXPageBean();
	private final ResourceBundleSource resourceBundleSource;
	private final IconUrlSource iconUrlSource = new IconUrlSource() {
		@Override public URL getIconUrl(String arg0) {
			// TODO ???
			return null;
		}
	};
	private boolean appendTimestampToTitle = false;
	private String templateName;
	private String templateVersion;
	private boolean setProductionXspOptions = false;
	
	private static final List<String> DEFAULT_COMPILER_OPTIONS = Arrays.asList(
			"-g", //$NON-NLS-1$
			"-parameters", //$NON-NLS-1$
			"-encoding", "utf-8" //$NON-NLS-1$ //$NON-NLS-2$
		);
	public static final String DEFAULT_COMPILER_LEVEL = "1.8"; //$NON-NLS-1$
	
	private static final ThreadLocal<DateFormat> TIMESTAMP = new ThreadLocal<DateFormat>() {
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd h:mm a zzz"); //$NON-NLS-1$
		}
	};
	
	/**
	 * Notes.ini property to set to "1" to output debug information about imported DXL
	 * files.
	 */
	public static final String INI_DEBUGDXL = "NSFODP_DebugDXL"; //$NON-NLS-1$
	private static boolean DEBUG_DXL = false;
	
	public ODPCompiler(BundleContext bundleContext, OnDiskProject onDiskProject, IProgressMonitor mon) throws FileNotFoundException, XMLException, IOException {
		this.bundleContext = Objects.requireNonNull(bundleContext);
		this.odp = Objects.requireNonNull(onDiskProject);
		this.mon = mon;
		this.facesProject = new FacesProjectImpl(getClass().getPackage().getName(), facesRegistry);
		this.resourceBundleSource = new MultiPathResourceBundleSource(odp.getResourcePaths());
		
		try {
			int debugDxl = Os.OSGetEnvironmentInt(INI_DEBUGDXL);
			DEBUG_DXL = debugDxl > 0;
		} catch(NException e) {
			e.printStackTrace();
		}
	}
	
	public OnDiskProject getOnDiskProject() {
		return odp;
	}
	
	public void addUpdateSite(UpdateSite updateSite) {
		this.updateSites.add(updateSite);
	}
	
	/**
	 * Sets the options to pass to the compiler, in the same format as used by
	 * {@code javac}.
	 * 
	 * <p>Note: to set the JRE compilation level, use {@link #setCompilerLevel(String)}.</p>
	 * 
	 * @param compilerOptions the compiler options to set, or {@code} null to reset to
	 *        the default
	 * @since 1.0.0
	 */
	public void setCompilerOptions(Collection<String> compilerOptions) {
		if(compilerOptions == null) {
			this.compilerOptions = DEFAULT_COMPILER_OPTIONS;
		} else {
			this.compilerOptions = new ArrayList<>(compilerOptions);
		}
	}
	
	/**
	 * Sets the compiler source and binary JRE level.
	 * 
	 * @param compilerLevel the compiler level to target, e.g. "1.6", "1.8", "10", etc., 
	 *        or {@code null} to reset to the default
	 * @since 1.1.0
	 */
	public void setCompilerLevel(String compilerLevel) {
		if(StringUtil.isEmpty(compilerLevel)) {
			this.compilerLevel = DEFAULT_COMPILER_LEVEL;
		} else {
			this.compilerLevel = compilerLevel;
		}
	}
	
	/**
	 * 
	 * @return the current targetted compiler level
	 * @since 1.1.0
	 */
	public String getCompilerLevel() {
		return compilerLevel;
	}
	
	/**
	 * Set whether or not to append a timestamp to the generated NSF's title.
	 * 
	 * @param appendTimestampToTitle whether or not to append a timestamp to the generated NSF's title
	 */
	public void setAppendTimestampToTitle(boolean appendTimestampToTitle) {
		this.appendTimestampToTitle = appendTimestampToTitle;
	}
	
	/**
	 * @return whether the compiler is configured to append a timestamp to the NSF's title
	 */
	public boolean isAppendTimestampToTitle() {
		return appendTimestampToTitle;
	}
	
	/**
	 * Sets a name for this database to act as a master template.
	 * 
	 * @param templateName the name to set, or <code>null</code> to un-set it
	 */
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	/**
	 * @return the name this database will use as a master template
	 */
	public String getTemplateName() {
		return templateName;
	}
	
	/**
	 * Sets a version for this database to use when {@link #setTemplateName(String)} is
	 * configured.
	 * 
	 * @param templateVersion the version to use, or <code>null</code> to un-set it
	 */
	public void setTemplateVersion(String templateVersion) {
		this.templateVersion = templateVersion;
	}
	
	/**
	 * @return the version this database will use when a master template
	 */
	public String getTemplateVersion() {
		return templateVersion;
	}
	
	/**
	 * Sets whether to set production options in the xsp.properties file. Currently, this sets:
	 * 
	 * <ul>
	 * 	<li><code>xsp.resources.aggregate=true</code></li>
	 * 	<li><code>xsp.client.resources.uncompressed=false</code></li>
	 * </ul>
	 * 
	 * @param setProductionXspOptions whether to set production XSP options
	 */
	public void setSetProductionXspOptions(boolean setProductionXspOptions) {
		this.setProductionXspOptions = setProductionXspOptions;
	}
	
	/**
	 * @return whether the compiler is set to specify production XSP options
	 */
	public boolean isSetProductionXspOptions() {
		return setProductionXspOptions;
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
	 * @return a {@link Path} representing the created database
	 * @throws Exception if there is a problem compiling any component
	 * @since 1.0.0
	 */
	public synchronized Path compile() throws Exception {
		return compile(getClass().getClassLoader());
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
	 * @param cl the base {@link ClassLoader} to use during compilation
	 * @return a {@link Path} representing the created database
	 * @throws Exception if there is a problem compiling any component
	 * @since 1.0.0
	 */
	public synchronized Path compile(ClassLoader cl) throws Exception {
		Collection<Bundle> bundles = installBundles();
		JavaSourceClassLoader classLoader = null;
		Set<Path> cleanup = new HashSet<>();
		try {
			boolean hasXPages = odp.hasXPagesElements();
			if(hasXPages) {
				initRegistry();
	
				// Build our classpath
				Collection<String> dependencies = ODPUtil.expandRequiredBundles(bundleContext, odp.getRequiredBundles());
				
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
				
				// Add any Jars from the ODP
				for(Path jar : odp.getJars()) {
					dependencies.add("jar:" + jar.toUri()); //$NON-NLS-1$
				}
				
				String[] classPath = dependencies.toArray(new String[dependencies.size()]);
				List<String> options = Stream.concat(
						compilerOptions.stream(),
						Stream.of("-source", compilerLevel, "-target", compilerLevel) //$NON-NLS-1$ //$NON-NLS-2$
					).collect(Collectors.toList());
				classLoader = new JavaSourceClassLoader(cl, options, classPath);

				// Compile Java classes
				compileJavaSources(classLoader);
				compileCustomControls(classLoader);
				compileXPages(classLoader);
			}
			
			lotus.domino.Session lotusSession = NotesFactory.createSession();
			try {
				Path file = createDatabase(lotusSession);
				Database database = lotusSession.getDatabase("", file.toAbsolutePath().toString()); //$NON-NLS-1$
				DxlImporter importer = lotusSession.createDxlImporter();
				importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_CREATE);
				importer.setAclImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_IGNORE);
				importer.setReplaceDbProperties(true);
				importer.setReplicaRequiredForReplaceOrUpdate(false);
				
				importDbProperties(importer, database);
				importBasicElements(importer, database);
				importFileResources(importer, database);
				importLotusScriptLibraries(importer, database);
				
				if(hasXPages) {
					Set<String> compiledClassNames = new HashSet<>(classLoader.getCompiledClassNames());
					importCustomControls(importer, database, classLoader, compiledClassNames);
					importXPages(importer, database, classLoader, compiledClassNames);
					importJavaElements(importer, database, classLoader, compiledClassNames);
				}

				// Append a timestamp if requested
				if(this.isAppendTimestampToTitle()) {
					database.setTitle(database.getTitle() + " - " + TIMESTAMP.get().format(new Date())); //$NON-NLS-1$
				}
				
				// Set the template info if requested
				String templateName = this.getTemplateName();
				if(StringUtil.isNotEmpty(templateName)) {
					NoteCollection notes = database.createNoteCollection(false);
					notes.selectAllDesignElements(true);
					notes.setSelectionFormula("$TITLE='$TemplateBuild'"); //$NON-NLS-1$
					notes.buildCollection();
					String noteId = notes.getFirstNoteID();

					lotus.domino.Document doc;
					if(StringUtil.isNotEmpty(noteId)) {
						doc = database.getDocumentByID(noteId);
					} else {
						// Import an empty one
						try(InputStream is = ODPCompiler.class.getResourceAsStream("/dxl/TemplateBuild.xml")) { //$NON-NLS-1$
							String dxl = StreamUtil.readString(is);
							List<String> ids = importDxl(importer, dxl, database, "$TemplateBuild blank field"); //$NON-NLS-1$
							doc = database.getDocumentByID(ids.get(0));
						}
					}
					String version = this.getTemplateVersion();
					if(StringUtil.isNotEmpty(version)) {
						doc.replaceItemValue("$TemplateBuild", version); //$NON-NLS-1$
					}
					doc.replaceItemValue("$TemplateBuildName", templateName); //$NON-NLS-1$
					DateTime dt = database.getParent().createDateTime(Calendar.getInstance());
					try {
						doc.replaceItemValue("$TemplateBuildDate", dt); //$NON-NLS-1$
					} finally {
						dt.recycle();
					}
					doc.save();
				}
				
				return file;
			} finally {
				lotusSession.recycle();
			}
		} catch(JavaCompilerException e) {
			StringWriter o = new StringWriter();
			PrintWriter errOut = new PrintWriter(o);
			e.printExtraInformation(errOut);
			throw new RuntimeException(MessageFormat.format(Messages.ODPCompiler_javaCompilationFailed, o), e);
		} finally {
			uninstallBundles(bundles);
			
			for(Path path : cleanup) {
				if(Files.isDirectory(path)) {
					Files.walk(path)
					    .sorted(Comparator.reverseOrder())
					    .map(Path::toFile)
					    .forEach(File::delete);
				}
				Files.deleteIfExists(path);
			}
			
			if(classLoader != null) {
				classLoader.close();
			}
		}
	}
	
	// *******************************************************************************
	// * Bundle manipulation methods
	// *******************************************************************************
	private Collection<Bundle> installBundles() {
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
	
	private void uninstallBundles(Collection<Bundle> bundles) {
		subTask(Messages.ODPCompiler_uninstallingBundles);
		
		bundles.stream().forEach(t -> {
			try {
				t.uninstall();
			} catch (BundleException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	/**
	 * Initializes the internal Faces registry with the newly-added plugins.
	 */
	private void initRegistry() {
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
	
	// *******************************************************************************
	// * Class compilation methods
	// *******************************************************************************
	
	private Map<String, Class<?>> compileJavaSources(JavaSourceClassLoader classLoader) throws FileNotFoundException, XMLException, IOException, JavaCompilerException {
		subTask(Messages.ODPCompiler_compilingJava);
		
		Map<Path, List<JavaSource>> javaSourceFiles = odp.getJavaSourceFiles();
		if(javaSourceFiles.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, CharSequence> sources = javaSourceFiles.entrySet().stream()
			.map(entry ->
				// Convert to a map of class name -> source
				entry.getValue().stream()
					.collect(Collectors.toMap(
						source -> ODPUtil.toJavaClassName(entry.getKey().relativize(source.getDataFile())),
						JavaSource::getSource
					))
			)
			.map(Map::entrySet)
			.flatMap(Set::stream)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		subTask(MessageFormat.format(Messages.ODPCompiler_compilingJavaClasses, sources.size()));
		return classLoader.addClasses(sources);
	}
	
	// *******************************************************************************
	// * XSP compilation methods
	// *******************************************************************************
	
	private Map<CustomControl, XSPCompilationResult> compileCustomControls(JavaSourceClassLoader classLoader) throws Exception {
		subTask(Messages.ODPCompiler_compilingCustomControls);
		
		ConfigParser configParser = ConfigParserFactory.getParserInstance();
		FacesClassLoader facesClassLoader = new DynamicFacesClassLoader(dynamicXPageBean, classLoader);
		
		Map<CustomControl, XSPCompilationResult> result = new LinkedHashMap<>();
		
		List<CustomControl> ccs = odp.getCustomControls();
		for(CustomControl cc : ccs) {
			Document xspConfig = cc.getXspConfig().get();
			
			String namespace = StringUtil.trim(DOMUtil.evaluateXPath(xspConfig, "/faces-config/faces-config-extension/namespace-uri/text()").getStringValue()); //$NON-NLS-1$
			Path fileName = odp.getBaseDirectory().relativize(cc.getXspConfigFile());
			LibraryFragmentImpl fragment = (LibraryFragmentImpl)configParser.createFacesLibraryFragment(
					facesProject,
					facesClassLoader,
					fileName.toString(),
					xspConfig.getDocumentElement(),
					resourceBundleSource,
					iconUrlSource,
					namespace
			);
			
			UpdatableLibrary library = getLibrary(namespace);
			library.addLibraryFragment(fragment);
			
			// Load the definition to refresh its parent ref
			CompositeComponentDefinitionImpl def = (CompositeComponentDefinitionImpl)library.getDefinition(cc.getControlName());
			def.refreshReferences();
		}
		
		// Now that they're all defined, try to compile them in a queue
		for(CustomControl cc : ccs) {
			XSPCompilationResult compilationResult = compileXSP(cc, classLoader);
			result.put(cc, compilationResult);
		}
		
		return result;
	}
	
	private Map<XPage, XSPCompilationResult> compileXPages(JavaSourceClassLoader classLoader) throws Exception {
		subTask(Messages.ODPCompiler_compilingXPages);
		Map<XPage, XSPCompilationResult> result = new LinkedHashMap<>();
		
		for(XPage xpage : odp.getXPages()) {
			XSPCompilationResult compilationResult = compileXSP(xpage, classLoader);
			result.put(xpage, compilationResult);
		}
		
		return result;
	}
	
	// *******************************************************************************
	// * NSF manipulation methods
	// *******************************************************************************
	
	/**
	 * Creates a new, non-replica copy of the stub blank database for population
	 * in the local temp directory.
	 * 
	 * @return a {@link Path} representing the new NSF file
	 * @throws Exception 
	 * @throws DominoException if there is an API-level problem creating the copy
	 */
	private Path createDatabase(lotus.domino.Session lotusSession) throws IOException, NotesException, DominoException {
		subTask(Messages.ODPCompiler_creatingNSF);
		Path temp = Files.createTempFile(NSFODPUtil.getTempDirectory(), "odpcompilertemp", ".nsf"); //$NON-NLS-1$ //$NON-NLS-2$
		temp.toFile().deleteOnExit();
		String filePath = temp.toAbsolutePath().toString();
		
		NSFSession session = NSFSession.fromLotus(DominoAPI.get(), lotusSession, false, true);
		try {
			session.createDatabase("", filePath, DBClass.NOTEFILE, true); //$NON-NLS-1$
		} finally {
			session.free();
		}
		
		return temp;
	}
	
	private void importDbProperties(DxlImporter importer, Database database) throws Exception {
		// DB properties gets special handling
		subTask(Messages.ODPCompiler_importingDbProperties);
		Path properties = odp.getDbPropertiesFile();
		Document dxlDoc = ODPUtil.readXml(properties);
		
		// Strip out any FT search settings, since these cause an exception on import
		Element fulltextsettings = (Element)DOMUtil.evaluateXPath(dxlDoc, "/*[name()='database']/*[name()='fulltextsettings']").getSingleNode(); //$NON-NLS-1$
		if(fulltextsettings != null) {
			fulltextsettings.getParentNode().removeChild(fulltextsettings);
		}
		
		String dxl = DOMUtil.getXMLString(dxlDoc);
		importDxl(importer, dxl, database, "database.properties"); //$NON-NLS-1$
	}
	
	private void importBasicElements(DxlImporter importer, Database database) throws Exception {
		subTask(Messages.ODPCompiler_importingDesignElements);
		for(Map.Entry<Path, String> entry : odp.getDirectDXLElements().entrySet()) {
			if(StringUtil.isNotEmpty(entry.getValue())) {
				try {
					importDxl(importer, entry.getValue(), database, MessageFormat.format(Messages.ODPCompiler_basicElementLabel, odp.getBaseDirectory().relativize(entry.getKey())));
				} catch(NotesException ne) {
					throw new NotesException(ne.id, "Exception while importing element " + odp.getBaseDirectory().relativize(entry.getKey()), ne); //$NON-NLS-1$
				}
			}
		}
	}
	
	private void importFileResources(DxlImporter importer, Database database) throws Exception {
		subTask(Messages.ODPCompiler_importingFileResources);
		
		Map<AbstractSplitDesignElement, Document> elements = odp.getFileResources().stream()
			.filter(res -> {
				Path filePath = odp.getBaseDirectory().relativize(res.getDataFile());
				String normalizedPath = filePath.toString().replace('\\', '/');
				
				switch(normalizedPath) {
				case "META-INF/MANIFEST.MF": { //$NON-NLS-1$
					// Special handling of MANIFEST.MF, which can cause trouble in FP10 when blank
					try {
						if(Files.size(res.getDataFile()) == 0) {
							return false;
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} break;
				case "WebContent/WEB-INF/xsp.properties": { //$NON-NLS-1$
					// Special handling of xsp.properties to set production options
					if(this.isSetProductionXspOptions()) {
						try(InputStream is = Files.newInputStream(res.getDataFile())) {
							Properties props = new Properties();
							props.load(is);
							props.put("xsp.resources.aggregate", "true"); //$NON-NLS-1$ //$NON-NLS-2$
							props.put("xsp.client.resources.uncompressed", "false"); //$NON-NLS-1$ //$NON-NLS-2$
							try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
								props.store(baos, null);
								baos.flush();
								res.setOverrideData(baos.toByteArray());
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
				}
				
				
				return true;
			})
			.collect(Collectors.toMap(
				Function.identity(),
				res -> {
					try {
						return res.getDxl();
					} catch (XMLException | IOException e) {
						throw new RuntimeException(e);
					}
				}
			));
		
		for(Map.Entry<AbstractSplitDesignElement, Document> entry : elements.entrySet()) {
			AbstractSplitDesignElement res = entry.getKey();
			Document dxlDoc = entry.getValue();
			Path filePath = odp.getBaseDirectory().relativize(res.getDataFile());
			importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, res.getClass().getSimpleName() + " " + filePath); //$NON-NLS-1$
			
			if(res instanceof FileResource) {
				FileResource fileRes = (FileResource)res;
				if(fileRes.isCopyToClasses()) {
					// Also create a copy beneath WEB-INF/classes
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try(InputStream is = Files.newInputStream(fileRes.getDataFile())) {
						StreamUtil.copyStream(is, baos);
					}
					// Use expanded syntax due to the presence of the xmlns
					String title = DOMUtil.evaluateXPath(dxlDoc, "/*[name()='note']/*[name()='item'][@name='$TITLE']/*[name()='text']/text()").getStringValue(); //$NON-NLS-1$
					if(StringUtil.isEmpty(title)) {
						throw new IllegalStateException(MessageFormat.format(Messages.ODPCompiler_couldNotIdentifyTitle, filePath));
					}
					DXLNativeUtil.importFileResource(importer, baos.toByteArray(), database, "WEB-INF/classes/" + title, "~C4g", "w"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
	}
	
	private void importCustomControls(DxlImporter importer, Database database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames) throws Exception {
		subTask(Messages.ODPCompiler_importingCustomControls);
		
		List<CustomControl> ccs = odp.getCustomControls();
		for(CustomControl cc : ccs) {
			Document dxlDoc = importXSP(importer, database, classLoader, compiledClassNames, cc);
			
			String xspConfig = cc.getXspConfigSource();
			byte[] xspConfigData = xspConfig.getBytes();
			DXLUtil.writeItemFileData(dxlDoc, "$ConfigData", xspConfigData); //$NON-NLS-1$
			DXLUtil.writeItemNumber(dxlDoc, "$ConfigSize", xspConfigData.length); //$NON-NLS-1$
			
			importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, MessageFormat.format(Messages.ODPCompiler_customControlLabel, cc.getPageName()));
		}
	}
	
	private void importXPages(DxlImporter importer, Database database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames) throws Exception {
		subTask(Messages.ODPCompiler_importingXPages);
		
		List<XPage> xpages = odp.getXPages();
		for(XPage xpage : xpages) {
			Document dxlDoc = importXSP(importer, database, classLoader, compiledClassNames, xpage);
			importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, MessageFormat.format(Messages.ODPCompiler_XPageLabel, xpage.getPageName()));
		}
	}
	
	private Document importXSP(DxlImporter importer, Database database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames, XPage xpage) throws XMLException, IOException {
		String className = xpage.getJavaClassName();
		byte[] byteCode = classLoader.getClassByteCode(className);
		String innerClassName = xpage.getJavaClassName() + '$' + xpage.getJavaClassSimpleName() + "Page"; //$NON-NLS-1$
		byte[] innerByteCode = classLoader.getClassByteCode(innerClassName);

		String xspSource = xpage.getSource();
		byte[] xspSourceData = xspSource.getBytes();
		
		Document dxlDoc = xpage.getDxl();
		
		DXLUtil.writeItemFileData(dxlDoc, "$ClassData0", byteCode); //$NON-NLS-1$
		DXLUtil.writeItemNumber(dxlDoc, "$ClassSize0", byteCode.length); //$NON-NLS-1$
		DXLUtil.writeItemFileData(dxlDoc, "$ClassData1", innerByteCode); //$NON-NLS-1$
		DXLUtil.writeItemNumber(dxlDoc, "$ClassSize1", innerByteCode.length); //$NON-NLS-1$
		DXLUtil.writeItemFileData(dxlDoc, "$FileData", xspSourceData); //$NON-NLS-1$
		DXLUtil.writeItemNumber(dxlDoc, "$FileSize", xspSourceData.length); //$NON-NLS-1$
		
		String[] classIndex = new String[] { "WEB-INF/classes/" + ODPUtil.toJavaPath(className), "WEB-INF/classes/" + ODPUtil.toJavaPath(innerClassName) }; //$NON-NLS-1$ //$NON-NLS-2$
		DXLUtil.writeItemString(dxlDoc, "$ClassIndexItem", true, classIndex); //$NON-NLS-1$
		
		// Drain them from the later queue
		compiledClassNames.remove(className);
		compiledClassNames.remove(innerClassName);
		
		return dxlDoc;
	}
	
	private void importJavaElements(DxlImporter importer, Database database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames) throws Exception {
		subTask(Messages.ODPCompiler_importingJava);
		
		Map<Path, List<JavaSource>> javaSourceFiles = odp.getJavaSourceFiles();
		for(Map.Entry<Path, List<JavaSource>> entry : javaSourceFiles.entrySet()) {
			for(JavaSource source : entry.getValue()) {
				Path filePath = entry.getKey().relativize(source.getDataFile());
				String className = ODPUtil.toJavaClassName(filePath);
				compiledClassNames.remove(className);
				byte[] byteCode = classLoader.getClassByteCode(className);
				
				Document dxlDoc = source.getDxl();
				
				DXLUtil.writeItemFileData(dxlDoc, "$ClassData0", byteCode); //$NON-NLS-1$
				DXLUtil.writeItemNumber(dxlDoc, "$ClassSize0", byteCode.length); //$NON-NLS-1$
				
				List<String> classIndexItem = new ArrayList<>();
				classIndexItem.add("WEB-INF/classes/" + ODPUtil.toJavaPath(className)); //$NON-NLS-1$
				
				// Also look for any inner classes that were compiled
				List<String> innerClasses = classLoader.getCompiledClassNames().stream()
						.filter(cname -> cname.matches("^" + Pattern.quote(className) + "[\\.\\$].+$")) //$NON-NLS-1$ //$NON-NLS-2$
						.collect(Collectors.toList());
				for(int i = 0; i < innerClasses.size(); i++) {
					String innerClassName = innerClasses.get(i);
					compiledClassNames.remove(innerClassName);
					byte[] innerByteCode = classLoader.getClassByteCode(innerClassName);
					DXLUtil.writeItemFileData(dxlDoc, "$ClassData" + (i+1), innerByteCode); //$NON-NLS-1$
					DXLUtil.writeItemNumber(dxlDoc, "$ClassSize" + (i+1), innerByteCode.length); //$NON-NLS-1$
					classIndexItem.add("WEB-INF/classes/" + ODPUtil.toJavaPath(innerClassName)); //$NON-NLS-1$
				}
				DXLUtil.writeItemString(dxlDoc, "$ClassIndexItem", true, classIndexItem.toArray(new CharSequence[classIndexItem.size()])); //$NON-NLS-1$
				
				importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, MessageFormat.format(Messages.ODPCompiler_javaClassLabel, className));
			}
		}
		
		// Create standalone class files for remaining classes
		for(String leftoverClassName : compiledClassNames) {
			String fileName = "WEB-INF/classes/" + ODPUtil.toJavaPath(leftoverClassName); //$NON-NLS-1$
			byte[] leftoverByteCode = classLoader.getClassByteCode(leftoverClassName);
			DXLNativeUtil.importFileResource(importer, leftoverByteCode, database, fileName, "~C4g", "w"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void importLotusScriptLibraries(DxlImporter importer, Database database) throws Exception {
		subTask(Messages.ODPCompiler_importingLotusScript);
		
		List<String> noteIds = new ArrayList<>();
		for(LotusScriptLibrary lib : odp.getLotusScriptLibraries()) {
			Document dxlDoc = lib.getDxl();
			String script = lib.getSource();
			int chunkSize = 60 * 1024;
			for(int startIndex = 0; startIndex < script.length(); startIndex += chunkSize) {
				int endIndex = Math.min(startIndex+chunkSize, script.length());
				String scriptChunk = script.substring(startIndex, endIndex);
				Element el = DXLUtil.writeItemString(dxlDoc, "$ScriptLib", false, scriptChunk); //$NON-NLS-1$
				el.setAttribute("sign", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				el.setAttribute("summary", "false"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			noteIds.addAll(importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, MessageFormat.format(Messages.ODPCompiler_lotusScriptLabel, odp.getBaseDirectory().relativize(lib.getDataFile()))));
		}
		
		if(!noteIds.isEmpty()) {
			try {
				Class.forName("lotus.domino.websvc.client.Stub"); //$NON-NLS-1$
			} catch(ClassNotFoundException e) {
				subTask(Messages.ODPCompiler_webServiceNotFound1);
				subTask(Messages.ODPCompiler_webServiceNotFound2);
				return;
			}
			
			subTask(Messages.ODPCompiler_compilingLotusScript);
			// In lieu of a dependency graph, just keep bashing at the list until it's done
			Queue<String> remaining = new ArrayDeque<>(noteIds);
			Map<String, String> titles = new HashMap<>();
			NSFSession nsfSession = NSFSession.fromLotus(DominoAPI.get(), database.getParent(), false, true);
			try {
				NSFDatabase nsfDatabase = new NSFDatabase(nsfSession, XSPNative.getDBHandle(database), database.getServer(), false);
				for(int i = 0; i < noteIds.size(); i++) {
					Queue<String> nextPass = new ArrayDeque<>();
					
					String noteId;
					while((noteId = remaining.poll()) != null) {
						NSFNote note = nsfDatabase.getNoteByID(noteId);
						String title = null;
						try {
							title = note.get("$TITLE", String.class); //$NON-NLS-1$
							titles.put(noteId, title);
							note.compileLotusScript();
							note.sign();
							note.save();
						} catch(LotusScriptCompilationException err) {
							nextPass.add(noteId);
							titles.put(noteId, title + " - " + err); //$NON-NLS-1$
						} catch(DominoException err) {
							if(err.getStatus() == 12051) { // Same as above, but not encapsulated
								titles.put(noteId, title + " - " + err); //$NON-NLS-1$
								nextPass.add(noteId);
							} else {
								throw err;
							}
						} finally {
							note.free();
						}
					}
					
					remaining = nextPass;
					if(nextPass.isEmpty()) {
						break;
					}
				}
			} finally {
				nsfSession.free();
			}
			if(!remaining.isEmpty()) {
				String notes = remaining.stream()
					.map(noteId -> "Note ID " + noteId + ": " + titles.get(noteId)) //$NON-NLS-1$ //$NON-NLS-2$
					.collect(Collectors.joining("\n")); //$NON-NLS-1$
				throw new RuntimeException(MessageFormat.format(Messages.ODPCompiler_unableToCompileLotusScript, notes));
			}
		}
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private void subTask(Object message, Object... params) {
		if(mon != null) {
			mon.subTask(StringUtil.format(StringUtil.toString(message), params));
		}
	}
	
	private UpdatableLibrary getLibrary(String namespace) {
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
	
	private XSPCompilationResult compileXSP(XPage xpage, JavaSourceClassLoader classLoader) throws Exception {
		try {
			String xspSource = xpage.getSource();
			String javaSource = dynamicXPageBean.translate(xpage.getJavaClassName(), xpage.getPageName(), xspSource, facesRegistry);
			Class<?> compiled = classLoader.addClass(xpage.getJavaClassName(), javaSource);
			return new XSPCompilationResult(javaSource, compiled);
		} catch(Throwable e) {
			throw new RuntimeException(MessageFormat.format(Messages.ODPCompiler_errorConvertingXSP, odp.getBaseDirectory().relativize(xpage.getDataFile())), e);
		}
	}
	
	/**
	 * @param importer the importer to use during the process
	 * @param dxl an XML string to import
	 * @param database the database to import to
	 * @param name a human-readable name of the element, for logging
	 * @return a {@link List} of imported note IDs
	 */
	private List<String> importDxl(DxlImporter importer, String dxl, Database database, String name) throws Exception {
		try {
			if(DEBUG_DXL) {
				String tempFileName = NSFODPUtil.getTempDirectory() + File.separator + name.replace('/', '-').replace('\\', '-') + ".xml"; //$NON-NLS-1$
				try(OutputStream os = Files.newOutputStream(Paths.get(tempFileName))) {
					os.write(dxl.getBytes());
				}
			}
			
			importer.importDxl(dxl, database);
			
			List<String> importedIds = new ArrayList<>();
			String noteId = importer.getFirstImportedNoteID();
			while(StringUtil.isNotEmpty(noteId)) {
				importedIds.add(noteId);
				noteId = importer.getNextImportedNoteID(noteId);
			}
			return importedIds;
		} catch(NotesException ne) {
			if(ne.text.contains("DXL importer operation failed")) { //$NON-NLS-1$
				throw new RuntimeException(MessageFormat.format(Messages.ODPCompiler_dxlImportFailed, name, importer.getLog()), ne);
			}
			throw ne;
		}
	}
}
