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
package org.openntf.nsfodp.commons.odp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openntf.nsfodp.commons.odp.util.ODPUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.commons.xml.XResult;

/**
 * Represents an On-Disk Project version of an NSF.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class OnDiskProject {
	public static final List<PathMatcher> DIRECT_DXL_FILES = Stream.of(
		"AppProperties/$DBIcon", //$NON-NLS-1$
		"Code/dbscript.lsdb", //$NON-NLS-1$
		"Code/actions/Shared Actions", //$NON-NLS-1$
		"Code/Agents/*.ja", //$NON-NLS-1$
		"Code/Agents/*.fa", //$NON-NLS-1$
		"Code/Agents/*.ija", //$NON-NLS-1$
		"Code/Agents/*.lsa", //$NON-NLS-1$
		"Code/Agents/*.aa", //$NON-NLS-1$
		"Code/ScriptLibraries/*.javalib", //$NON-NLS-1$
		"Forms/*", //$NON-NLS-1$
		"Framesets/*", //$NON-NLS-1$
		"Pages/*", //$NON-NLS-1$
		"Resources/AboutDocument", //$NON-NLS-1$
		"Resources/UsingDocument", //$NON-NLS-1$
		"SharedElements/Fields/*", //$NON-NLS-1$
		"SharedElements/Outlines/*", //$NON-NLS-1$
		"SharedElements/Subforms/*", //$NON-NLS-1$
		"Views/*" //$NON-NLS-1$
	).map(GlobMatcher::glob).collect(Collectors.toList());
	
	public static final List<PathMatcher> IGNORED_FILES = Stream.of(
		"**/.DS_Store", //$NON-NLS-1$
		"**/Thumbs.db" //$NON-NLS-1$
	).map(GlobMatcher::glob).collect(Collectors.toList());

	private final Path baseDir;
	
	public final List<GlobMatcher> FILE_RESOURCES;
	
	public OnDiskProject(Path baseDirectory) {
		this.baseDir = Objects.requireNonNull(baseDirectory);
		
		this.FILE_RESOURCES = Arrays.asList(
			new GlobMatcher(".classpath", path -> new FileResource(path, "~C4gP", null, p -> ODPUtil.toBasicFilePath(baseDir, p))), //$NON-NLS-1$ //$NON-NLS-2$
			new GlobMatcher(".settings/**", path -> new FileResource(path, "~C4gP", null, p -> ODPUtil.toBasicFilePath(baseDir, p))), //$NON-NLS-1$ //$NON-NLS-2$
			new GlobMatcher("AppProperties/xspdesign.properties", path -> new FileResource(path, "~C4g", null, p -> ODPUtil.toBasicFilePath(baseDir, p), p -> "xspdesign.properties")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			new GlobMatcher("Code/Jars/**", path -> new FileResource(path)), //$NON-NLS-1$
			new GlobMatcher("Code/Java/**", path ->  //$NON-NLS-1$
				path.toString().endsWith(".java") || path.toString().endsWith(AbstractSplitDesignElement.EXT_METADATA) ? null : new FileResource(path, true) //$NON-NLS-1$
			),
			new GlobMatcher("Code/ScriptLibraries/*.js", path -> new JavaScriptLibrary(path)), //$NON-NLS-1$
			new GlobMatcher("Code/ScriptLibraries/*.jss", path -> new ServerJavaScriptLibrary(path)), //$NON-NLS-1$
			new GlobMatcher("CompositeApplications/**", path -> new FileResource(path, "34567C|Q", "1", p -> p.getFileName().toString(), p -> "")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			new GlobMatcher("META-INF/**", path -> new FileResource(path, "~C4gP", null, p -> ODPUtil.toBasicFilePath(baseDir, p))), //$NON-NLS-1$ //$NON-NLS-2$
			new GlobMatcher("plugin.xml", path -> new FileResource(path, "~C4gP", null, p -> ODPUtil.toBasicFilePath(baseDir, p))), //$NON-NLS-1$ //$NON-NLS-2$
			new GlobMatcher("Resources/Files/*", path -> new FileResource(path)), //$NON-NLS-1$
			new GlobMatcher("Resources/Images/*", path -> new ImageResource(path)), //$NON-NLS-1$
			new GlobMatcher("Resources/StyleSheets/*", path -> new FileResource(path)), //$NON-NLS-1$
			new GlobMatcher("Resources/Themes/*", path -> new FileResource(path)), //$NON-NLS-1$
			new GlobMatcher("WebContent/**", path -> new FileResource(path, "~C4g", "w", p -> ODPUtil.toBasicFilePath(baseDir.resolve("WebContent"), p))), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			new GlobMatcher("XPages/*.properties", path -> new FileResource(path, "gC~4K2", null, p -> p.getFileName().toString())), //$NON-NLS-1$ //$NON-NLS-2$
			new GlobMatcher("CustomControls/*.properties", path -> new FileResource(path, "gC~4K2", null, p -> p.getFileName().toString())) //$NON-NLS-1$ //$NON-NLS-2$
		);
	}
	
	public Path getBaseDirectory() {
		return baseDir;
	}
	
	public Path getClasspathFile() {
		Path classpath = baseDir.resolve(".classpath"); //$NON-NLS-1$
		if(Files.exists(classpath) && !Files.isRegularFile(classpath)) {
			throw new IllegalStateException(MessageFormat.format(Messages.OnDiskProject_classpathNotAFile, classpath.toAbsolutePath()));
		}
		return classpath;
	}
	
	/**
	 * Generates a collection of the Jar files inside this ODP to be used during compilation, including
	 * those in Code/Jars as well as WEB-INF/lib.
	 * 
	 * @return a collection of {@link Path}s representing jar files to be used in compilation
	 * @throws IOException 
	 * @throws XMLException 
	 */
	public Collection<Path> getJars() throws IOException, XMLException {
		List<Path> result = new ArrayList<>();
		
		Path jars = baseDir.resolve("Code").resolve("Jars"); //$NON-NLS-1$ //$NON-NLS-2$
		if(Files.exists(jars) && Files.isDirectory(jars)) {
			Files.find(jars, Integer.MAX_VALUE,
					(path, attr) -> attr.isRegularFile() && path.getFileName().toString().endsWith(".jar") //$NON-NLS-1$
				).forEach(result::add);
		}
		Path lib = baseDir.resolve("WebContent").resolve("WEB-INF").resolve("lib"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(Files.exists(lib) && Files.isDirectory(lib)) {
			Files.find(lib, Integer.MAX_VALUE,
					(path, attr) -> attr.isRegularFile() && path.getFileName().toString().endsWith(".jar") //$NON-NLS-1$
				).forEach(result::add);
		}
		result.addAll(this.findManualJars());
		return result;
	}
	
	public Path getDbPropertiesFile() {
		Path properties = baseDir.resolve("AppProperties").resolve("database.properties"); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.exists(properties)) {
			throw new IllegalStateException(MessageFormat.format(Messages.OnDiskProject_dbPropertiesDoesNotExist, properties.toAbsolutePath()));
		}
		if(!Files.isRegularFile(properties)) {
			throw new IllegalStateException(MessageFormat.format(Messages.OnDiskProject_dbPropertiesNotAFile, properties.toAbsolutePath()));
		}
		return properties;
	}
	
	public Path getPluginFile() {
		Path pluginXml = baseDir.resolve("plugin.xml"); //$NON-NLS-1$
		if(!Files.exists(pluginXml)) {
			throw new IllegalStateException(MessageFormat.format(Messages.OnDiskProject_pluginDoesNotExist, pluginXml.toAbsolutePath()));
		}
		if(!Files.isRegularFile(pluginXml)) {
			throw new IllegalStateException(MessageFormat.format(Messages.OnDiskProject_pluginNotAFile, pluginXml.toAbsolutePath()));
		}
		return pluginXml;
	}
	
	/**
	 * Returns the paths in the ODP that are used at runtime for Java resource
	 * resolution.
	 * 
	 * @return a {@link List} of {@link Path}s
	 * @throws IOException if there is a problem reading the filesystem
	 * @throws XMLException if there is a problem parsing the class path configuration
	 * @throws FileNotFoundException if one of the configured class paths doesn't exist
	 */
	public List<Path> getResourcePaths() throws FileNotFoundException, XMLException, IOException {
		List<Path> result = new ArrayList<>(findSourceFolders());
		Path files = getBaseDirectory().resolve("Resources").resolve("Files"); //$NON-NLS-1$ //$NON-NLS-2$
		if(Files.exists(files) && Files.isDirectory(files)) {
			result.add(files);
		}
		return result;
	}
	
	public List<String> getRequiredBundles() throws XMLException {
		// TODO adapt to FP10 MANIFEST.MF style?
		Document pluginXml = ODPUtil.readXml(getPluginFile());
		return Arrays.stream(DOMUtil.evaluateXPath(pluginXml, "/plugin/requires/import").getNodes()) //$NON-NLS-1$
			.map(Element.class::cast)
			.map(el -> el.getAttribute("plugin")) //$NON-NLS-1$
			.collect(Collectors.toList());
	}
	
	/**
	 * Generates a map of source folders to the Java source files they contain.
	 * 
	 * @return a {@link Map} of {@link Path}s to {@link List}s of {@code Path}s
	 * @throws IOException if there is a problem reading the filesystem
	 * @throws XMLException if there is a problem parsing the class path configuration
	 * @throws FileNotFoundException if one of the configured class paths doesn't exist
	 */
	public Map<Path, List<JavaSource>> getJavaSourceFiles() throws FileNotFoundException, XMLException, IOException {
		return findSourceFolders().stream()
				.collect(Collectors.toMap(
					Function.identity(),
					ODPUtil::listJavaFiles
				));
	}
	
	public List<CustomControl> getCustomControls() throws IOException {
		Path dir = baseDir.resolve("CustomControls"); //$NON-NLS-1$
		if(Files.exists(dir) && Files.isDirectory(dir)) {
			return Files.find(dir, 1,
					(path, attr) -> path.toString().endsWith(".xsp") && attr.isRegularFile()) //$NON-NLS-1$
					.map(path -> new CustomControl(path))
					.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
	
	public List<XPage> getXPages() throws IOException {
		Path dir = baseDir.resolve("XPages"); //$NON-NLS-1$
		if(Files.exists(dir) && Files.isDirectory(dir)) {
			return Files.find(dir, 1,
					(path, attr) -> path.toString().endsWith(".xsp") && attr.isRegularFile()) //$NON-NLS-1$
					.map(path -> new XPage(path))
					.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Returns a list of DXL content that does not need any additional processing
	 * or contextual information to import, such as forms and views.
	 * 
	 * @return a {@link Map} of file {@link Path}s to {@link String}s containing DXL
	 */
	public Map<Path, String> getDirectDXLElements() {
		return DIRECT_DXL_FILES.stream()
			.map(glob -> {
				try {
					return Files.find(baseDir, Integer.MAX_VALUE, (path, attr) -> glob.matches(baseDir.relativize(path)));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.flatMap(Function.identity())
			.collect(Collectors.toMap(Function.identity(), ODPUtil::readFile));
	}
	
	public List<AbstractSplitDesignElement> getFileResources() {
		return FILE_RESOURCES.stream()
			.map(matcher -> {
				try {
					return Files.find(baseDir, Integer.MAX_VALUE,
						(path, attr) -> {
							for(PathMatcher ignoreMatcher : IGNORED_FILES) {
								if(ignoreMatcher.matches(path)) {
									return false;
								}
							}
							return attr.isRegularFile() && matcher.getMatcher().matches(baseDir.relativize(path)) && !path.getFileName().toString().endsWith(".metadata"); //$NON-NLS-1$
						}
					).map(matcher::getElement);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.flatMap(Function.identity())
			.filter(Objects::nonNull)
			.map(AbstractSplitDesignElement.class::cast)
			.collect(Collectors.toList());
	}
	
	public List<LotusScriptLibrary> getLotusScriptLibraries() throws IOException {
		PathMatcher glob = GlobMatcher.glob("Code/ScriptLibraries/*.lss"); //$NON-NLS-1$
		return Files.find(baseDir, Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile() && glob.matches(baseDir.relativize(path)))
			.map(path -> new LotusScriptLibrary(path))
			.collect(Collectors.toList());
	}
	
	/**
	 * Determines whether the on-disk project has any XPages elements that need to
	 * be compiled, namely XPages, Custom Controls, and Java classes.
	 * 
	 * @return whether the ODP has any XPages elements
	 * @throws IOException 
	 * @throws XMLException 
	 */
	public boolean hasXPagesElements() throws IOException, XMLException {
		Path xpages = baseDir.resolve("XPages"); //$NON-NLS-1$
		if(Files.exists(xpages) && Files.list(xpages).count() > 0) {
			return true;
		}
		Path ccs = baseDir.resolve("CustomControls"); //$NON-NLS-1$
		if(Files.exists(ccs) && Files.list(ccs).count() > 0) {
			return true;
		}
		boolean hasJava = findSourceFolders().stream()
				.map(t -> {
					try {
						return Files.list(t);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.map(Stream::count)
				.anyMatch(i -> i > 0);
		if(hasJava) {
			return true;
		}
		return false;
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private List<Path> findSourceFolders() throws FileNotFoundException, IOException, XMLException {
		Path classpath = getClasspathFile();
		if(!Files.exists(classpath)) {
			return Collections.emptyList();
		}
		
		Document domDoc;
		try(InputStream is = Files.newInputStream(classpath)) {
			domDoc = DOMUtil.createDocument(is);
		}
		XResult xresult = DOMUtil.evaluateXPath(domDoc, "/classpath/classpathentry[kind=src]"); //$NON-NLS-1$
		List<String> paths = Arrays.stream(xresult.getNodes())
			.map(node -> Element.class.cast(node))
			.map(el -> el.getAttribute("path")) //$NON-NLS-1$
			.filter(path -> !"Local".equals(path)) //$NON-NLS-1$
			.collect(Collectors.toList());
		paths.add("Code/Java"); //$NON-NLS-1$
		return paths.stream()
			.map(path -> getBaseDirectory().resolve(path))
			.filter(Files::exists)
			.filter(Files::isDirectory)
			.collect(Collectors.toList());
	}
	private List<Path> findManualJars() throws IOException, XMLException {
		Path classpath = getClasspathFile();
		if(!Files.exists(classpath)) {
			return Collections.emptyList();
		}
		Document domDoc;
		try(InputStream is = Files.newInputStream(classpath)) {
			domDoc = DOMUtil.createDocument(is);
		}
		XResult xresult = DOMUtil.evaluateXPath(domDoc, "/classpath/classpathentry[kind=lib]"); //$NON-NLS-1$
		return Arrays.stream(xresult.getNodes())
			.map(node -> Element.class.cast(node))
			.map(el -> el.getAttribute("path")) //$NON-NLS-1$
			.map(path -> getBaseDirectory().resolve(path))
			.filter(Files::exists)
			.filter(Files::isRegularFile)
			.collect(Collectors.toList());
	}
}
