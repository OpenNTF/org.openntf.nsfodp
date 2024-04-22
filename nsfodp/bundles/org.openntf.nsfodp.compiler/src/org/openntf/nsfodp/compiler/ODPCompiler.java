/**
 * Copyright © 2018-2023 Jesse Gallagher
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

import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SACTIONS_DESIGN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.Os;
import com.ibm.xsp.library.FacesClassLoader;
import com.ibm.xsp.registry.CompositeComponentDefinitionImpl;
import com.ibm.xsp.registry.FacesSharableRegistry;
import com.ibm.xsp.registry.LibraryFragmentImpl;
import com.ibm.xsp.registry.UpdatableLibrary;
import com.ibm.xsp.registry.parse.ConfigParser;
import com.ibm.xsp.registry.parse.ConfigParserFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.com.ibm.xsp.extlib.interpreter.DynamicFacesClassLoader;
import org.openntf.com.ibm.xsp.extlib.javacompiler.JavaCompilerException;
import org.openntf.com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.h.NsfNote;
import org.openntf.nsfodp.commons.h.StdNames;
import org.openntf.nsfodp.commons.odp.AbstractSplitDesignElement;
import org.openntf.nsfodp.commons.odp.CustomControl;
import org.openntf.nsfodp.commons.odp.FileResource;
import org.openntf.nsfodp.commons.odp.JavaSource;
import org.openntf.nsfodp.commons.odp.LotusScriptLibrary;
import org.openntf.nsfodp.commons.odp.OnDiskProject;
import org.openntf.nsfodp.commons.odp.XPage;
import org.openntf.nsfodp.commons.odp.XSPCompilationResult;
import org.openntf.nsfodp.commons.odp.notesapi.NDXLImporter;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NDominoException;
import org.openntf.nsfodp.commons.odp.notesapi.NLotusScriptCompilationException;
import org.openntf.nsfodp.commons.odp.notesapi.NNote;
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;
import org.openntf.nsfodp.commons.odp.util.ODPUtil;
import org.openntf.nsfodp.commons.xml.NSFODPDomUtil;
import org.openntf.nsfodp.compiler.dxl.DxlImporterLog;
import org.openntf.nsfodp.compiler.dxl.DxlImporterLog.DXLFatalError;
import org.openntf.nsfodp.compiler.util.CompilerUtil;
import org.openntf.nsfodp.compiler.util.MultiPathResourceBundleSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents an on-disk project compilation environment.
 * 
 * <p>This class is the primary entry point for ODP compilation.</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class ODPCompiler extends AbstractCompilationEnvironment {
	private final OnDiskProject odp;
	private List<String> compilerOptions = DEFAULT_COMPILER_OPTIONS;
	private String compilerLevel = DEFAULT_COMPILER_LEVEL;
	
	private boolean appendTimestampToTitle = false;
	private String templateName;
	private String templateVersion;
	private boolean setProductionXspOptions = false;
	private String odsRelease;
	/**
	 * @since 3.8.0
	 */
	private boolean compileBasicElementLotusScript = false;
	
	private static final List<String> DEFAULT_COMPILER_OPTIONS = Arrays.asList(
			"-g", //$NON-NLS-1$
			"-parameters", //$NON-NLS-1$
			"-encoding", "utf-8" //$NON-NLS-1$ //$NON-NLS-2$
		);
	public static final String DEFAULT_COMPILER_LEVEL = "1.8"; //$NON-NLS-1$
	
	private static final ThreadLocal<DateFormat> TIMESTAMP = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd h:mm a zzz")); //$NON-NLS-1$
	
	/**
	 * Notes.ini property to set to "1" to output debug information about imported DXL
	 * files.
	 */
	public static final String INI_DEBUGDXL = "NSFODP_DebugDXL"; //$NON-NLS-1$
	private static boolean DEBUG_DXL = false;
	
	public ODPCompiler(BundleContext bundleContext, OnDiskProject onDiskProject, IProgressMonitor mon) throws FileNotFoundException, IOException {
		super(bundleContext, new MultiPathResourceBundleSource(Objects.requireNonNull(onDiskProject).getResourcePaths()), mon);
		this.odp = onDiskProject;
		
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
	 * Sets the Notes/Domino ODS release level to target.
	 * 
	 * <p>This value is used in the file extension - e.g. {@code "8"} for ".ns8" - when creating
	 * the temporary compilation NSF.</p>
	 * 
	 * @param odsRelease the ODS release string to use in the file extension
	 * @since 3.0.0
	 */
	public void setOdsRelease(String odsRelease) {
		this.odsRelease = odsRelease;
	}
	
	/**
	 * The Notes/Domino ODS release level to target.
	 * 
	 * <p>This value is used in the file extension - e.g. {@code "8"} for ".ns8" - when creating
	 * the temporary compilation NSF.</p>
	 * 
	 * @return the targeted ODS release, or {@code null} if no release has been set
	 * @since 3.0.0
	 */
	public String getOdsRelease() {
		return odsRelease;
	}
	
	/**
	 * Sets whether LotusScript in "basic" design elements (Forms, Views, etc.) should be compiled.
	 * This is {@code false} by default.
	 * 
	 * <p>This step has proven optional in practice, and so it can be useful to disable this
	 * behavior to reduce compilation times or to work around oddities in older LotusScript that
	 * functions but fails during compilation.</p>
	 * 
	 * @param compileBasicElementLotusScript whether LotusScript in basic design elements should be
	 *                                       explicitly compiled
	 * @since 3.8.0
	 */
	public void setCompileBasicElementLotusScript(boolean compileBasicElementLotusScript) {
		this.compileBasicElementLotusScript = compileBasicElementLotusScript;
	}
	
	/**
	 * Determines whether LotusScript in "basic" design elements (Forms, Views, etc.) should be compiled.
	 * This is {@code false} by default.
	 * 
	 * @return whether LotusScript in basic design elements should be explicitly compiled
	 * @since 3.8.0
	 */
	public boolean isCompileBasicElementLotusScript() {
		return compileBasicElementLotusScript;
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
	
				Collection<String> dependencies = buildDependenciesCollection(cleanup);
				dependencies.addAll(ODPUtil.expandRequiredBundles(bundleContext, odp.getRequiredBundles()));
				
				// Add any Jars from the ODP
				for(Path jar : odp.getJars()) {
					// If the path is inside a JAR, extract it
					if("jar".equals(jar.toUri().getScheme())) { //$NON-NLS-1$
						Path tempJar = Files.createTempFile(NSFODPUtil.getTempDirectory(), jar.getFileName().toString(), ".jar"); //$NON-NLS-1$
						cleanup.add(tempJar);
						Files.copy(jar, tempJar, StandardCopyOption.REPLACE_EXISTING);
						dependencies.add("jar:" + tempJar.toUri()); //$NON-NLS-1$
					} else {
						dependencies.add("jar:" + jar.toUri()); //$NON-NLS-1$	
					}
				}
				
				String[] classPath = dependencies.toArray(new String[dependencies.size()]);
				List<String> options = Stream.concat(
						compilerOptions.stream(),
						Stream.of("-source", compilerLevel, "-target", compilerLevel) //$NON-NLS-1$ //$NON-NLS-2$
					).collect(Collectors.toList());
				classLoader = new JavaSourceClassLoader(cl, options, classPath);
				// Bar loading of different-version SSJS classes from ndext
				classLoader.getJavaFileManager().setNonDelegatingPackages(Arrays.asList("com.ibm.jscript")); //$NON-NLS-1$

				// Compile Java classes
				compileJavaSources(classLoader);
				compileCustomControls(classLoader);
				compileXPages(classLoader);
			}
			
			try(NotesAPI session = NotesAPI.get()) {
				Path file = createDatabase(session);
				try(NDatabase database = session.openDatabase("", file.toAbsolutePath().toString())) { //$NON-NLS-1$
					try(NDXLImporter importer = session.createDXLImporter()) {
						
						importDbProperties(importer, database);
						importEarlyBasicElements(importer, database);
						importLotusScriptLibraries(importer, database);
						importBasicElements(importer, database);
						importFileResources(importer, database);
						importDbScript(importer, database);
						
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
							int noteId = database.getSharedFieldNoteID("$TemplateBuild"); //$NON-NLS-1$
							NNote doc;
							if(noteId != 0) {
								doc = database.getNoteByID(noteId);
							} else {
								// Import an empty one
								try(InputStream is = ODPCompiler.class.getResourceAsStream("/dxl/TemplateBuild.xml")) { //$NON-NLS-1$
									String dxl = StreamUtil.readString(is, "UTF-8"); //$NON-NLS-1$
									List<Integer> ids = importDxl(importer, dxl, database, "$TemplateBuild blank field"); //$NON-NLS-1$
									doc = database.getNoteByID(ids.get(0));
								}
							}
							String version = this.getTemplateVersion();
							if(StringUtil.isNotEmpty(version)) {
								doc.set("$TemplateBuild", version); //$NON-NLS-1$
							}
							doc.set("$TemplateBuildName", templateName); //$NON-NLS-1$
							doc.set("$TemplateBuildDate", new Date()); //$NON-NLS-1$
							doc.save();
						}
					}
				}
				
				return file;
			}
		} catch(JavaCompilerException e) {
			StringWriter o = new StringWriter();
			PrintWriter errOut = new PrintWriter(o);
			e.printExtraInformation(errOut);
			throw new RuntimeException(MessageFormat.format(Messages.ODPCompiler_javaCompilationFailed, o), e);
		} finally {
			uninstallBundles(bundles);
			
			if(classLoader != null) {
				classLoader.close();
			}

			NSFODPUtil.deltree(cleanup);
		}
	}
	
	
	// *******************************************************************************
	// * Class compilation methods
	// *******************************************************************************
	
	private Map<String, Class<?>> compileJavaSources(JavaSourceClassLoader classLoader) throws FileNotFoundException, IOException, JavaCompilerException {
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
		
		int size = sources.size();
		subTask(MessageFormat.format(Messages.ODPCompiler_compilingJavaClasses, sources.size()));
		if(size > 0) {
			return classLoader.addClasses(sources);
		} else {
			return Collections.emptyMap();
		}
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
			
			String namespace = StringUtil.trim(NSFODPDomUtil.node(xspConfig, "/faces-config/faces-config-extension/namespace-uri/text()").get().getTextContent()); //$NON-NLS-1$
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
	private Path createDatabase(NotesAPI session) throws IOException {
		subTask(Messages.ODPCompiler_creatingNSF);
		
		String ext;
		String odsRelease = getOdsRelease();
		if(StringUtil.isEmpty(odsRelease)) {
			ext = ".nsf"; //$NON-NLS-1$
		} else {
			ext = ".ns" + odsRelease; //$NON-NLS-1$
		}
		
		Path temp = Files.createTempFile(NSFODPUtil.getTempDirectory(), "odpcompilertemp", ext); //$NON-NLS-1$
		Files.deleteIfExists(temp);
		String filePath = temp.toAbsolutePath().toString();
		
		session.createDatabase(filePath);
		
		return temp;
	}
	
	private void importDbProperties(NDXLImporter importer, NDatabase database) throws Exception {
		// DB properties gets special handling
		subTask(Messages.ODPCompiler_importingDbProperties);
		Path properties = odp.getDbPropertiesFile();
		Document dxlDoc = ODPUtil.readXml(properties);
		
		// Strip out any FT search settings, since these cause an exception on import
		Element fulltextsettings = (Element)NSFODPDomUtil.node(dxlDoc, "/*[name()='database']/*[name()='fulltextsettings']").orElse(null); //$NON-NLS-1$
		if(fulltextsettings != null) {
			fulltextsettings.getParentNode().removeChild(fulltextsettings);
		}
		
		String dxl = NSFODPDomUtil.getXmlString(dxlDoc, null);
		importDxl(importer, dxl, database, "database.properties"); //$NON-NLS-1$
	}
	
	private void importEarlyBasicElements(NDXLImporter importer, NDatabase database) throws Exception {
		subTask(Messages.ODPCompiler_importingEarlyDesignElements);
		List<Integer> noteIds = new ArrayList<>();
		try(Stream<Path> dxlElements = odp.getDirectEarlyDXLElements()) {
			dxlElements
				.filter(p -> {
					try {
						return Files.size(p) > 0;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.forEach(p -> {
					try {
						try(InputStream is = NSFODPUtil.newInputStream(p)) {
							noteIds.addAll(importDxl(importer, is, database, MessageFormat.format(Messages.ODPCompiler_basicElementLabel, odp.getBaseDirectory().relativize(p))));
						}
					} catch(Exception e) {
						throw new RuntimeException("Exception while importing element " + odp.getBaseDirectory().relativize(p), e);
					}
				});
		}
	}
	
	private void importBasicElements(NDXLImporter importer, NDatabase database) throws Exception {
		subTask(Messages.ODPCompiler_importingDesignElements);
		List<Integer> noteIds = new ArrayList<>();
		try(Stream<Path> dxlElements = odp.getDirectDXLElements()) {
			dxlElements
				.filter(p -> {
					try {
						return Files.size(p) > 0;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.forEach(p -> {
					try {
						try(InputStream is = NSFODPUtil.newInputStream(p)) {
							noteIds.addAll(importDxl(importer, is, database, MessageFormat.format(Messages.ODPCompiler_basicElementLabel, odp.getBaseDirectory().relativize(p))));
						}
					} catch(Exception e) {
						throw new RuntimeException("Exception while importing element " + odp.getBaseDirectory().relativize(p), e);
					}
				});
		}
		if(isCompileBasicElementLotusScript()) {
			compileLotusScript(database, noteIds, false);
		}
	}
	
	private void importFileResources(NDXLImporter importer, NDatabase database) throws Exception {
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
						try(InputStream is = NSFODPUtil.newInputStream(res.getDataFile())) {
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
					} catch ( IOException e) {
						throw new RuntimeException(e);
					}
				}
			));
		
		for(Map.Entry<AbstractSplitDesignElement, Document> entry : elements.entrySet()) {
			AbstractSplitDesignElement res = entry.getKey();
			Document dxlDoc = entry.getValue();
			Path filePath = odp.getBaseDirectory().relativize(res.getDataFile());
			importDxl(importer, NSFODPDomUtil.getXmlString(dxlDoc, null), database, res.getClass().getSimpleName() + " " + filePath); //$NON-NLS-1$
			
			if(res instanceof FileResource) {
				FileResource fileRes = (FileResource)res;
				if(fileRes.isCopyToClasses()) {
					// Also create a copy beneath WEB-INF/classes
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					Files.copy(fileRes.getDataFile(), baos);
					// Use expanded syntax due to the presence of the xmlns
					String title = NSFODPDomUtil.node(dxlDoc, "/*[name()='note']/*[name()='item'][@name='$TITLE']/*[name()='text']/text()").get().getTextContent(); //$NON-NLS-1$
					if(StringUtil.isEmpty(title)) {
						throw new IllegalStateException(MessageFormat.format(Messages.ODPCompiler_couldNotIdentifyTitle, filePath));
					}
					CompilerUtil.importFileResource(importer, baos.toByteArray(), database, "WEB-INF/classes/" + title, "~C4g", "w"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
	}
	
	private void importCustomControls(NDXLImporter importer, NDatabase database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames) throws Exception {
		subTask(Messages.ODPCompiler_importingCustomControls);
		
		List<CustomControl> ccs = odp.getCustomControls();
		for(CustomControl cc : ccs) {
			Document dxlDoc = importXSP(importer, database, classLoader, compiledClassNames, cc);
			
			String xspConfig = cc.getXspConfigSource();
			byte[] xspConfigData = xspConfig.getBytes(StandardCharsets.UTF_8);
			DXLUtil.writeItemFileData(dxlDoc, "$ConfigData", xspConfigData); //$NON-NLS-1$
			DXLUtil.writeItemNumber(dxlDoc, "$ConfigSize", xspConfigData.length); //$NON-NLS-1$
			
			importDxl(importer, NSFODPDomUtil.getXmlString(dxlDoc, null), database, MessageFormat.format(Messages.ODPCompiler_customControlLabel, cc.getPageName()));
		}
	}
	
	private void importXPages(NDXLImporter importer, NDatabase database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames) throws Exception {
		subTask(Messages.ODPCompiler_importingXPages);
		
		List<XPage> xpages = odp.getXPages();
		for(XPage xpage : xpages) {
			Document dxlDoc = importXSP(importer, database, classLoader, compiledClassNames, xpage);
			importDxl(importer, NSFODPDomUtil.getXmlString(dxlDoc, null), database, MessageFormat.format(Messages.ODPCompiler_XPageLabel, xpage.getPageName()));
		}
	}
	
	private Document importXSP(NDXLImporter importer, NDatabase database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames, XPage xpage) throws IOException {
		String className = xpage.getJavaClassName();
		byte[] byteCode = classLoader.getClassByteCode(className);
		String innerClassName = xpage.getJavaClassName() + '$' + xpage.getJavaClassSimpleName() + "Page"; //$NON-NLS-1$
		byte[] innerByteCode = classLoader.getClassByteCode(innerClassName);
		
		Document dxlDoc = xpage.getDxl();
		
		DXLUtil.writeItemFileData(dxlDoc, "$ClassData0", byteCode); //$NON-NLS-1$
		DXLUtil.writeItemNumber(dxlDoc, "$ClassSize0", byteCode.length); //$NON-NLS-1$
		DXLUtil.writeItemFileData(dxlDoc, "$ClassData1", innerByteCode); //$NON-NLS-1$
		DXLUtil.writeItemNumber(dxlDoc, "$ClassSize1", innerByteCode.length); //$NON-NLS-1$
		
		String[] classIndex = new String[] { "WEB-INF/classes/" + ODPUtil.toJavaPath(className), "WEB-INF/classes/" + ODPUtil.toJavaPath(innerClassName) }; //$NON-NLS-1$ //$NON-NLS-2$
		DXLUtil.writeItemString(dxlDoc, "$ClassIndexItem", true, classIndex); //$NON-NLS-1$
		
		// Drain them from the later queue
		compiledClassNames.remove(className);
		compiledClassNames.remove(innerClassName);
		
		return dxlDoc;
	}
	
	private void importJavaElements(NDXLImporter importer, NDatabase database, JavaSourceClassLoader classLoader, Set<String> compiledClassNames) throws Exception {
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
				
				importDxl(importer, NSFODPDomUtil.getXmlString(dxlDoc, null), database, MessageFormat.format(Messages.ODPCompiler_javaClassLabel, className));
			}
		}
		
		// Create standalone class files for remaining classes
		for(String leftoverClassName : compiledClassNames) {
			String fileName = "WEB-INF/classes/" + ODPUtil.toJavaPath(leftoverClassName); //$NON-NLS-1$
			byte[] leftoverByteCode = classLoader.getClassByteCode(leftoverClassName);
			CompilerUtil.importFileResource(importer, leftoverByteCode, database, fileName, "~C4g", "w"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void importLotusScriptLibraries(NDXLImporter importer, NDatabase database) throws Exception {
		subTask(Messages.ODPCompiler_importingLotusScript);
		
		List<Integer> noteIds = new ArrayList<>();
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
			noteIds.addAll(importDxl(importer, NSFODPDomUtil.getXmlString(dxlDoc, null), database, MessageFormat.format(Messages.ODPCompiler_lotusScriptLabel, odp.getBaseDirectory().relativize(lib.getDataFile()))));
		}
		
		compileLotusScript(database, noteIds, true);
	}
	
	/**
	 * Specially imports the database script, including compiling LotusScript.
	 * 
	 * @param importer the DXL importer to use
	 * @param database the database target
	 * @throws Exception 
	 * @since 2.5.0
	 */
	private void importDbScript(NDXLImporter importer, NDatabase database) throws Exception {
		Path dbScript = odp.getDbScriptFile();
		if(dbScript != null) {
			try {
				List<Integer> noteIds;
				try(InputStream is = NSFODPUtil.newInputStream(dbScript)) {
					noteIds = importDxl(importer, is, database, MessageFormat.format(Messages.ODPCompiler_basicElementLabel, odp.getBaseDirectory().relativize(dbScript)));
				}
				compileLotusScript(database, noteIds, true);
			} catch(Exception ne) {
				throw new Exception("Exception while importing element " + odp.getBaseDirectory().relativize(dbScript), ne); //$NON-NLS-1$
			}
		}
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private XSPCompilationResult compileXSP(XPage xpage, JavaSourceClassLoader classLoader) throws Exception {
		try {
			String javaSource;
			try(InputStream xspSource = xpage.getSourceAsStream()) {
				javaSource = dynamicXPageBean.translate(xpage.getJavaClassName(), xpage.getPageName(), xspSource, (FacesSharableRegistry)facesProject.getRegistry());
			}
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
	private List<Integer> importDxl(NDXLImporter importer, String dxl, NDatabase database, String name) throws Exception {
		if(DEBUG_DXL) {
			Path dxlFile = Files.createTempFile(NSFODPUtil.getTempDirectory(), name.replace('/', '-').replace('\\', '-'), ".xml"); //$NON-NLS-1$
			try(OutputStream os = Files.newOutputStream(dxlFile)) {
				os.write(dxl.getBytes(StandardCharsets.UTF_8));
			}
		}
		try(InputStream baos = new ByteArrayInputStream(dxl.getBytes(StandardCharsets.UTF_8))) {
			return importDxl(importer, baos, database, name);
		}
	}
	/**
	 * @param importer the importer to use during the process
	 * @param dxl an XML {@link InputStream} to import
	 * @param database the database to import to
	 * @param name a human-readable name of the element, for logging
	 * @return a {@link List} of imported note IDs
	 * @since 3.4.0
	 */
	private List<Integer> importDxl(NDXLImporter importer, InputStream dxl, NDatabase database, String name) throws Exception {
		try {
			Collection<Integer> imported = new HashSet<>();
			imported.addAll(importer.importDxl(database, dxl));
			String logXml = importer.getResultLogXML();
			if(StringUtil.isNotEmpty(logXml)) {
				DxlImporterLog log = DxlImporterLog.forXml(logXml);
				if(log.getErrors() != null && !log.getErrors().isEmpty()) {
					String msg = log.getErrors().stream()
						.map(e -> StringUtil.format("{2} (line={0}, column={1})", e.getLine(), e.getColumn(), e.getText()))
						.collect(Collectors.joining(", ")); //$NON-NLS-1$
					throw new Exception(MessageFormat.format("Exception importing {0}: {1}", name, msg));
				} else if(log.getFatalErrors() != null && !log.getFatalErrors().isEmpty()) {
					String msg = log.getFatalErrors().stream()
						.map(DXLFatalError::getText)
						.collect(Collectors.joining(", ")); //$NON-NLS-1$
					throw new Exception(MessageFormat.format("Exception importing {0}: {1}", name, msg));
				}
			}

			List<Integer> importedIds = new ArrayList<>();
			for(Integer noteId : imported) {
				importedIds.add(noteId);
				
				try(NNote note = database.getNoteByID(noteId)) {
					note.sign();
					note.save();
				}
			}
			
			return importedIds;
		} catch(Exception ne) {
			if(ne.getMessage().contains("DXL importer operation failed")) { //$NON-NLS-1$
				throw new RuntimeException(MessageFormat.format(Messages.ODPCompiler_dxlImportFailed, name, importer.getResultLogXML()), ne);
			}
			throw ne;
		}
	}
	
	private void compileLotusScript(NDatabase database, List<Integer> noteIds, boolean retry) {
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
			Queue<Integer> remaining = new ArrayDeque<>(noteIds);
			Map<Integer, String> titles = new HashMap<>();
			for(int i = 0; i < noteIds.size(); i++) {
				Queue<Integer> nextPass = new ArrayDeque<>();
				
				Integer noteId;
				while((noteId = remaining.poll()) != null) {
					String title = null;
					try(NNote note = database.getNoteByID(noteId)) {
						// Check to see if this is the Shared Actions note, which we should skip to avoid trouble
						if((note.getNoteClassValue() & NsfNote.NOTE_CLASS_NONPRIV) == NsfNote.NOTE_CLASS_FORM) {
							String flags = note.getAsString(StdNames.DESIGN_FLAGS, ' ');
							if(NSFODPUtil.matchesFlagsPattern(flags, DFLAGPAT_SACTIONS_DESIGN)) {
								continue;
							}
						}
						
						title = note.get("$TITLE", String.class); //$NON-NLS-1$
						titles.put(noteId, title);
						note.compileLotusScript();
						note.sign();
						note.save();
					} catch(NLotusScriptCompilationException err) {
						nextPass.add(noteId);
						titles.put(noteId, title + " - " + err); //$NON-NLS-1$
					} catch(NDominoException err) {
						if(err.getStatus() == 12051) { // Same as above, but not encapsulated
							titles.put(noteId, title + " - " + err); //$NON-NLS-1$
							nextPass.add(noteId);
						} else if(err.getStatus() == 0x222) {
							// Note item not found - occurs for non-LS elements
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
			if(!retry && !remaining.isEmpty()) {
				String notes = remaining.stream()
					.map(noteId -> "Note ID " + noteId + ": " + titles.get(noteId)) //$NON-NLS-1$ //$NON-NLS-2$
					.collect(Collectors.joining("\n")); //$NON-NLS-1$
				throw new RuntimeException(MessageFormat.format(Messages.ODPCompiler_unableToCompileLotusScript, notes));
			}
		}
	}
}
