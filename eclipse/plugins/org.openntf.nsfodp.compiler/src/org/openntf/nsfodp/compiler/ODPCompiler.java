/**
 * Copyright © 2018 Jesse Gallagher
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
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.compiler.odp.AbstractSplitDesignElement;
import org.openntf.nsfodp.compiler.odp.CustomControl;
import org.openntf.nsfodp.compiler.odp.FileResource;
import org.openntf.nsfodp.compiler.odp.JavaSource;
import org.openntf.nsfodp.compiler.odp.LotusScriptLibrary;
import org.openntf.nsfodp.compiler.odp.OnDiskProject;
import org.openntf.nsfodp.compiler.odp.XPage;
import org.openntf.nsfodp.compiler.odp.XSPCompilationResult;
import org.openntf.nsfodp.compiler.update.UpdateSite;
import org.openntf.nsfodp.compiler.util.DXLUtil;
import org.openntf.nsfodp.compiler.util.MultiPathResourceBundleSource;
import org.openntf.nsfodp.compiler.util.ODPUtil;
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
import com.mindoo.domino.jna.NotesDatabase;
import com.mindoo.domino.jna.NotesNote;
import com.mindoo.domino.jna.NotesDatabase.Encryption;
import com.mindoo.domino.jna.constants.CreateDatabase;
import com.mindoo.domino.jna.constants.DBClass;
import com.mindoo.domino.jna.errors.LotusScriptCompilationError;
import com.mindoo.domino.jna.errors.NotesError;
import com.mindoo.domino.jna.gc.NotesGC;
import com.mindoo.domino.jna.utils.LegacyAPIUtils;

import lotus.domino.Database;
import lotus.domino.DxlImporter;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.Os;
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
 * @since 2.0.0
 */
public class ODPCompiler {
	private final BundleContext bundleContext;
	private final OnDiskProject odp;
	private final Set<UpdateSite> updateSites = new LinkedHashSet<>();
	private List<String> compilerOptions = DEFAULT_COMPILER_OPTIONS;
	private final IProgressMonitor mon;
	
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
	
	private static final List<String> DEFAULT_COMPILER_OPTIONS = Arrays.asList(
			"-source", "1.8", //$NON-NLS-1$ //$NON-NLS-2$
			"-target", "1.8", //$NON-NLS-1$ //$NON-NLS-2$
			"-g", //$NON-NLS-1$
			"-parameters", //$NON-NLS-1$
			"-encoding", "utf-8" //$NON-NLS-1$ //$NON-NLS-2$
			);
	
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
	 * @return a {@link Path} representing the created database
	 * @throws Exception if there is a problem compiling any component
	 */
	public synchronized Path compile() throws Exception {
		return compile(getClass().getClassLoader());
	}
	
	public synchronized Path compile(ClassLoader cl) throws Exception {
		Collection<Bundle> bundles = installBundles();
		try {
			JavaSourceClassLoader classLoader = null;
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
							try(InputStream is = jar.getInputStream(notesJar)) {
								Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
							}
							dependencies.add("jar:" + tempFile.toUri().toString()); //$NON-NLS-1$
						}
					} else {
						Path path = f.toPath().resolve("Notes.jar"); //$NON-NLS-1$
						Path tempFile = Files.createTempFile("Notes", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
						Files.copy(path, tempFile, StandardCopyOption.REPLACE_EXISTING);
						dependencies.add("jar:" + tempFile.toUri().toString()); //$NON-NLS-1$
					}
				}
				
				// Add any Jars from the ODP
				for(Path jar : odp.getJars()) {
					dependencies.add("jar:" + jar.toUri()); //$NON-NLS-1$
				}
				
				String[] classPath = dependencies.toArray(new String[dependencies.size()]);
				classLoader = new JavaSourceClassLoader(cl, compilerOptions, classPath);

				// Compile Java classes
				compileJavaSources(classLoader);
				compileCustomControls(classLoader);
				compileXPages(classLoader);
			}
			
			lotus.domino.Session lotusSession = NotesFactory.createSession();
			try {
				Path file = createDatabase(lotusSession);
				Database database = lotusSession.getDatabase("", file.toAbsolutePath().toString());
				DxlImporter importer = lotusSession.createDxlImporter();
				importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_CREATE);
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
				
				return file;
			} finally {
				lotusSession.recycle();
			}
		} catch(JavaCompilerException e) {
			StringWriter o = new StringWriter();
			PrintWriter errOut = new PrintWriter(o);
			e.printExtraInformation(errOut);
			throw new RuntimeException("Java compilation failed:\n" + o, e); //$NON-NLS-1$
		} finally {
			uninstallBundles(bundles);
		}
	}
	
	// *******************************************************************************
	// * Bundle manipulation methods
	// *******************************************************************************
	private Collection<Bundle> installBundles() {
		subTask("Installing bundles");
		
		Collection<Bundle> result = updateSites.stream()
			.map(UpdateSite::getBundleURIs)
			.flatMap(Collection::stream)
			.map(this::installBundle)
			.collect(Collectors.toList()).stream() // Force waiting until installation is complete
			.filter(Objects::nonNull)
			.map(this::startBundle)
			.collect(Collectors.toList());
		subTask(MessageFormat.format("- Installed {0,choice,0#no bundles|1# 1 bundle|1<{0} bundles}", result.size()));
		return result;
	}
	
	private void uninstallBundles(Collection<Bundle> bundles) {
		subTask("Uninstalling bundles");
		
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
		subTask("Initializing libraries");

		List<Object> libraries = ExtensionManager.findServices((List<Object>)null, LibraryServiceLoader.class, "com.ibm.xsp.Library");
		libraries.stream()
			.filter(lib -> lib instanceof XspLibrary)
			.map(XspLibrary.class::cast)
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
			if(bundle.getHeaders().get("Eclipse-SourceBundle") == null && bundle.getHeaders().get("Fragment-Host") == null) {
				bundle.start();
			}
		} catch (BundleException e) {
			if(e.toString().contains("Another singleton bundle selected")) {
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
		subTask("Compiling Java source");
		
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
		
		subTask(MessageFormat.format("- Compiling {0,choice,0#no classes|1# 1 class|1<{0} classes}", sources.size()));
		return classLoader.addClasses(sources);
	}
	
	// *******************************************************************************
	// * XSP compilation methods
	// *******************************************************************************
	
	private Map<CustomControl, XSPCompilationResult> compileCustomControls(JavaSourceClassLoader classLoader) throws Exception {
		subTask("Compiling custom controls");
		
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
		subTask("Compiling XPages");
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
	 * @throws IOException if there is a problem creating the file
	 * @throws NotesException if there is an API-level problem creating the copy
	 */
	private Path createDatabase(lotus.domino.Session lotusSession) throws IOException, NotesException {
		subTask("Creating destination NSF");
		Path temp = Files.createTempFile(NSFODPUtil.getTempDirectory(), "odpcompilertemp", ".nsf"); //$NON-NLS-1$ //$NON-NLS-2$
		temp.toFile().deleteOnExit();
		String filePath = temp.toAbsolutePath().toString();
		
		NotesDatabase.createDatabase("", filePath, DBClass.NOTEFILE, true, EnumSet.of(CreateDatabase.LARGE_UNKTABLE), Encryption.None, 0); //$NON-NLS-1$
		
		return temp;
	}
	
	private void importDbProperties(DxlImporter importer, Database database) throws Exception {
		// DB properties gets special handling
		subTask("Importing DB properties");
		Path properties = odp.getDbPropertiesFile();
		Document dxlDoc = ODPUtil.readXml(properties);
		Element fulltextsettings = (Element)DOMUtil.evaluateXPath(dxlDoc, "/*[name()='database']/*[name()='fulltextsettings']").getSingleNode(); //$NON-NLS-1$
		if(fulltextsettings != null) {
			fulltextsettings.getParentNode().removeChild(fulltextsettings);
		}
		String dxl = DOMUtil.getXMLString(dxlDoc);
		importDxl(importer, dxl, database, "database.properties"); //$NON-NLS-1$
	}
	
	private void importBasicElements(DxlImporter importer, Database database) throws Exception {
		subTask("Importing basic design elements");
		for(Map.Entry<Path, String> entry : odp.getDirectDXLElements().entrySet()) {
			if(StringUtil.isNotEmpty(entry.getValue())) {
				try {
					importDxl(importer, entry.getValue(), database, "Basic element " + odp.getBaseDirectory().relativize(entry.getKey()));
				} catch(NotesException ne) {
					throw new NotesException(ne.id, "Exception while importing element " + odp.getBaseDirectory().relativize(entry.getKey()), ne); //$NON-NLS-1$
				}
			}
		}
	}
	
	private void importFileResources(DxlImporter importer, Database database) throws Exception {
		subTask("Importing file resources");
		
		// Generate DXL in parallel
		Map<AbstractSplitDesignElement, Document> elements = odp.getFileResources().stream()
//			.parallel()
			.filter(res -> {
				Path filePath = odp.getBaseDirectory().relativize(res.getDataFile());
				
				// Special handling of MANIFEST.MF, which can cause trouble in FP10 when blank
				if("META-INF/MANIFEST.MF".equals(filePath.toString().replace('\\', '/'))) { //$NON-NLS-1$
					try {
						if(Files.size(res.getDataFile()) == 0) {
							return false;
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
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
			importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, res.getClass().getSimpleName() + " " + filePath);
			
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
						throw new IllegalStateException("Could not identify original title for file resource " + filePath);
					}
					ODPUtil.importFileResource(importer, baos.toByteArray(), database, "WEB-INF/classes/" + title, "~C4g", "w"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
	}
	
	private void importCustomControls(DxlImporter importer, Database database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames) throws Exception {
		subTask("Importing custom controls");
		
		List<CustomControl> ccs = odp.getCustomControls();
		for(CustomControl cc : ccs) {
			Document dxlDoc = importXSP(importer, database, classLoader, compiledClassNames, cc);
			
			String xspConfig = cc.getXspConfigSource();
			byte[] xspConfigData = xspConfig.getBytes();
			DXLUtil.writeItemFileData(dxlDoc, "$ConfigData", xspConfigData); //$NON-NLS-1$
			DXLUtil.writeItemNumber(dxlDoc, "$ConfigSize", xspConfigData.length); //$NON-NLS-1$
			
			importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, "Custom Control " + cc.getPageName());
		}
	}
	
	private void importXPages(DxlImporter importer, Database database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames) throws Exception {
		subTask("Importing XPages");
		
		List<XPage> xpages = odp.getXPages();
		for(XPage xpage : xpages) {
			Document dxlDoc = importXSP(importer, database, classLoader, compiledClassNames, xpage);
			importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, "XPage " + xpage.getPageName());
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
		subTask("Importing Java design elements");
		
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
				
				importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, "Java class " + className);
			}
		}
		
		// Create standalone class files for remaining classes
		for(String leftoverClassName : compiledClassNames) {
			String fileName = "WEB-INF/classes/" + ODPUtil.toJavaPath(leftoverClassName); //$NON-NLS-1$
			byte[] leftoverByteCode = classLoader.getClassByteCode(leftoverClassName);
			ODPUtil.importFileResource(importer, leftoverByteCode, database, fileName, "~C4g", "w"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void importLotusScriptLibraries(DxlImporter importer, Database database) throws Exception {
		subTask("Importing LotusScript libraries");
		
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
			noteIds.addAll(importDxl(importer, DOMUtil.getXMLString(dxlDoc), database, "LotusScript library " + odp.getBaseDirectory().relativize(lib.getDataFile())));
		}

		subTask("- Compiling LotusScript");
		// In lieu of a dependency graph, just keep bashing at the list until it's done
		Queue<String> remaining = new ArrayDeque<>(noteIds);
		Map<String, String> titles = new HashMap<>();
		for(int i = 0; i < noteIds.size(); i++) {
			Queue<String> nextPass = new ArrayDeque<>();
			
			String noteId;
			while((noteId = remaining.poll()) != null) {
				lotus.domino.Document doc = database.getDocumentByID(noteId);
				String title = doc.getItemValueString("$TITLE"); //$NON-NLS-1$
				titles.put(noteId, title);
				try {
					NotesGC.runWithAutoGC(() -> {
						NotesNote notesNote = LegacyAPIUtils.toNotesNote(doc);
						notesNote.compileLotusScript();
						notesNote.sign();
						notesNote.update();
						return null;
					});
				} catch(LotusScriptCompilationError err) {
					nextPass.add(noteId);
					titles.put(noteId, title + " - " + err); //$NON-NLS-1$
				} catch(NotesError err) {
					if(err.getId() == 12051) { // Same as above, but not encapsulated
						titles.put(noteId, title + " - " + err); //$NON-NLS-1$
						nextPass.add(noteId);
					} else {
						throw err;
					}
				}
			}
			
			remaining = nextPass;
			if(nextPass.isEmpty()) {
				break;
			}
		}
		if(!remaining.isEmpty()) {
			String notes = remaining.stream()
				.map(noteId -> "Note ID " + noteId + ": " + titles.get(noteId)) //$NON-NLS-1$ //$NON-NLS-2$
				.collect(Collectors.joining("\n")); //$NON-NLS-1$
			throw new RuntimeException("Unable to compile LotusScript in notes:\n\n" + notes); //$NON-NLS-1$
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
			throw new RuntimeException("Exception while converting XSP element " + odp.getBaseDirectory().relativize(xpage.getDataFile()), e);
		}
	}
	
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
				throw new RuntimeException("DXL import failed for element '" + name + "':\n" + importer.getLog(), ne);
			}
			throw ne;
		}
	}
}
