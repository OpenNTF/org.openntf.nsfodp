package org.openntf.nsfodp.transpiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.odp.util.ODPUtil;
import org.openntf.nsfodp.compiler.AbstractCompilationEnvironment;
import org.openntf.nsfodp.compiler.util.MultiPathResourceBundleSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.xsp.extlib.interpreter.DynamicFacesClassLoader;
import com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;
import com.ibm.xsp.library.FacesClassLoader;
import com.ibm.xsp.page.compiled.PageToClassNameUtil;
import com.ibm.xsp.registry.CompositeComponentDefinitionImpl;
import com.ibm.xsp.registry.LibraryFragmentImpl;
import com.ibm.xsp.registry.UpdatableLibrary;
import com.ibm.xsp.registry.parse.ConfigParser;
import com.ibm.xsp.registry.parse.ConfigParserFactory;

/**
 * Handles transpiling XPage and Custom Control sources into Java source.
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public class XspTranspiler extends AbstractCompilationEnvironment {
	
	private final Path xspSourceRoot;
	private final Path ccSourceRoot;

	public XspTranspiler(BundleContext bundleContext, Path xspSourceRoot, Path ccSourceRoot, IProgressMonitor mon) {
		super(bundleContext, new MultiPathResourceBundleSource(Arrays.asList(xspSourceRoot, ccSourceRoot)), mon);
		this.xspSourceRoot = xspSourceRoot;
		this.ccSourceRoot = ccSourceRoot;
	}
	
	/**
	 * Transpiles the XSP code in {@code xspSourceRoot} and {@code ccSourceRoot} to Java.
	 * 
	 * @return a {@link Path} to the generated source root
	 * @throws IOException 
	 */
	public Path transpile() throws IOException {
		
		subTask("Transpiling XSP Source");

		Collection<Bundle> bundles = installBundles();
		try {
			initRegistry();
			
			Path outputDirectory = Files.createTempDirectory(getClass().getName());
	
			if(ccSourceRoot != null && Files.isDirectory(ccSourceRoot)) {
				defineCustomControls(ccSourceRoot);
				
				subTask("Transpiling Custom Controls");
				try(Stream<Path> xspFiles = Files.find(ccSourceRoot, Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile() && path.toString().toLowerCase().endsWith(".xsp"), FileVisitOption.FOLLOW_LINKS)) { //$NON-NLS-1$
					xspFiles.forEach(p -> transpileXsp(ccSourceRoot, p, outputDirectory));
				}
			}
			if(xspSourceRoot != null && Files.isDirectory(xspSourceRoot)) {
				subTask("Transpiling XPages");
				try(Stream<Path> xspFiles = Files.find(xspSourceRoot, Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile() && path.toString().toLowerCase().endsWith(".xsp"), FileVisitOption.FOLLOW_LINKS)) { //$NON-NLS-1$
					xspFiles.forEach(p -> transpileXsp(xspSourceRoot, p, outputDirectory));
				}
			}
			
			return outputDirectory;
		} finally {
			uninstallBundles(bundles);
		}
	}
	
	private void defineCustomControls(Path ccSourceRoot) throws IOException {
		if(ccSourceRoot != null) {
			subTask("Initializing Custom Control definitions");
			
			// Generate a classpath, which the CC library needs to find classes for property types
			Set<Path> cleanup = new HashSet<>();
			try {
				Collection<String> dependencies = buildDependenciesCollection(cleanup);
				
				String[] classPath = dependencies.toArray(new String[dependencies.size()]);
				
				ConfigParser configParser = ConfigParserFactory.getParserInstance();
				try(JavaSourceClassLoader classLoader = new JavaSourceClassLoader(Thread.currentThread().getContextClassLoader(), Collections.emptyList(), classPath)) {
					FacesClassLoader facesClassLoader = new DynamicFacesClassLoader(dynamicXPageBean, classLoader);
					
					try(Stream<Path> ccConfigs = Files.find(ccSourceRoot, Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile() && path.toString().toLowerCase().endsWith(".xsp-config"), FileVisitOption.FOLLOW_LINKS)) { //$NON-NLS-1$
						ccConfigs.forEach(ccConfig -> {
							try {
								Document xspConfig = ODPUtil.readXml(ccConfig);
								
								String namespace = StringUtil.trim(DOMUtil.evaluateXPath(xspConfig, "/faces-config/faces-config-extension/namespace-uri/text()").getStringValue()); //$NON-NLS-1$
								Path fileName = ccSourceRoot.relativize(ccConfig);
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
								String controlName = StringUtil.trim(DOMUtil.evaluateXPath(xspConfig, "/faces-config/composite-component/composite-name/text()").getStringValue()); //$NON-NLS-1$
								CompositeComponentDefinitionImpl def = (CompositeComponentDefinitionImpl)library.getDefinition(controlName);
								def.refreshReferences();
							} catch (XMLException e) {
								throw new RuntimeException(e);
							}
						});
					}
				}
			} finally {
				NSFODPUtil.deltree(cleanup);
			}
		}
	}
	
	private void transpileXsp(Path rootDir, Path xspFile, Path outputDirectory) {
		try {
			String xspSource;
			try(InputStream is = Files.newInputStream(xspFile)) {
				xspSource = StreamUtil.readString(is, "UTF-8"); //$NON-NLS-1$
			}
			
			Path relativeFile = rootDir.relativize(xspFile);
			String className = PageToClassNameUtil.getClassNameForPage(relativeFile.toString());
		
			String javaSource = dynamicXPageBean.translate(className, relativeFile.toString(), xspSource, facesRegistry);
			
			String outputFileName = className.replace('.', File.separatorChar) + ".java"; //$NON-NLS-1$
			Path outputFile = outputDirectory.resolve(outputFileName);
			Files.createDirectories(outputFile.getParent());
			try(Writer w = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				w.write(javaSource);
			}
		} catch(Exception e) {
			throw new RuntimeException("Exception processing page " + rootDir.relativize(xspFile), e);
		}
	}
}
