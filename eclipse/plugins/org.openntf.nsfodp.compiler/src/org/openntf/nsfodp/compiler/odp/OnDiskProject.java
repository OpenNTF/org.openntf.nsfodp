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
package org.openntf.nsfodp.compiler.odp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openntf.nsfodp.compiler.util.ODPUtil;
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
	public static final List<PathMatcher> DIRECT_DXL_FILES = Arrays.stream(new String[] {
		"AppProperties/$DBIcon",
		"Code/dbscript.lsdb",
		"Code/Agents/*.ja",
		"Code/Agents/*.fa",
		"Code/Agents/*.ija",
		"Code/Agents/*.lsa",
		"Code/Agents/*.aa",
		"Code/ScriptLibraries/*.javalib",
		"Forms/*",
		"Framesets/*",
		"Pages/*",
		"Resources/AboutDocument",
		"Resources/UsingDocument",
		"SharedElements/Fields/*",
		"SharedElements/Outlines/*",
		"Views/*"
	}).map(GlobMatcher::glob).collect(Collectors.toList());
	

	private final Path baseDir;
	
	public final List<GlobMatcher> FILE_RESOURCES;
	
	public OnDiskProject(Path baseDirectory) {
		this.baseDir = Objects.requireNonNull(baseDirectory);
		
		this.FILE_RESOURCES = Arrays.asList(
			new GlobMatcher(".classpath", path -> new FileResource(path, "~C4gP", null, p -> ODPUtil.toBasicFilePath(baseDir, p))),
			new GlobMatcher(".settings/**", path -> new FileResource(path, "~C4gP", null, p -> ODPUtil.toBasicFilePath(baseDir, p))),
			new GlobMatcher("Code/Java/**", path -> 
				path.toString().endsWith(".java") || path.toString().endsWith(AbstractSplitDesignElement.EXT_METADATA) ? null : new FileResource(path, true)
			),
			new GlobMatcher("Code/ScriptLibraries/*.js", path -> new JavaScriptLibrary(path)),
			new GlobMatcher("Code/ScriptLibraries/*.jss", path -> new ServerJavaScriptLibrary(path)),
			new GlobMatcher("META-INF/*", path -> new FileResource(path, "~C4gP", null, p -> ODPUtil.toBasicFilePath(baseDir, p))),
			new GlobMatcher("plugin.xml", path -> new FileResource(path, "~C4gP", null, p -> ODPUtil.toBasicFilePath(baseDir, p))),
			new GlobMatcher("Resources/Files/*", path -> new FileResource(path)),
			new GlobMatcher("Resources/Images/*", path -> new ImageResource(path)),
			new GlobMatcher("Resources/StyleSheets/*", path -> new FileResource(path)),
			new GlobMatcher("Resources/Themes/*", path -> new FileResource(path)),
			new GlobMatcher("WebContent/**", path -> new FileResource(path, "~C4g", "w", p -> ODPUtil.toBasicFilePath(baseDir.resolve("WebContent"), p)))
		);
	}
	
	public Path getBaseDirectory() {
		return baseDir;
	}
	
	public Path getClasspathFile() {
		Path classpath = baseDir.resolve(".classpath");
		if(Files.exists(classpath) && !Files.isRegularFile(classpath)) {
			throw new IllegalStateException("Classpath file is not a file: " + classpath.toAbsolutePath());
		}
		return classpath;
	}
	
	public Path getDbPropertiesFile() {
		Path properties = baseDir.resolve("AppProperties").resolve("database.properties");
		if(!Files.exists(properties)) {
			throw new IllegalStateException("DB properties file does not exist: " + properties.toAbsolutePath());
		}
		if(!Files.isRegularFile(properties)) {
			throw new IllegalStateException("DB properties file is not a file: " + properties.toAbsolutePath());
		}
		return properties;
	}
	
	public Path getPluginFile() {
		Path pluginXml = baseDir.resolve("plugin.xml");
		if(!Files.exists(pluginXml)) {
			throw new IllegalStateException("Plugin file does not exist: " + pluginXml.toAbsolutePath());
		}
		if(!Files.isRegularFile(pluginXml)) {
			throw new IllegalStateException("Plugin file is not a file: " + pluginXml.toAbsolutePath());
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
		Path files = getBaseDirectory().resolve("Resources").resolve("Files");
		if(Files.exists(files) && Files.isDirectory(files)) {
			result.add(files);
		}
		return result;
	}
	
	public List<String> getRequiredBundles() throws XMLException {
		// TODO adapt to FP10 MANIFEST.MF style?
		Document pluginXml = ODPUtil.readXml(getPluginFile());
		return Arrays.stream(DOMUtil.evaluateXPath(pluginXml, "/plugin/requires/import").getNodes())
			.map(Element.class::cast)
			.map(el -> el.getAttribute("plugin"))
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
		Path dir = baseDir.resolve("CustomControls");
		if(Files.exists(dir) && Files.isDirectory(dir)) {
			return Files.find(dir, 1,
					(path, attr) -> path.toString().endsWith(".xsp") && attr.isRegularFile())
					.map(path -> new CustomControl(path))
					.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
	
	public List<XPage> getXPages() throws IOException {
		Path dir = baseDir.resolve("XPages");
		if(Files.exists(dir) && Files.isDirectory(dir)) {
			return Files.find(dir, 1,
					(path, attr) -> path.toString().endsWith(".xsp") && attr.isRegularFile())
					.map(path -> new CustomControl(path))
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
						(path, attr) -> attr.isRegularFile() && matcher.getMatcher().matches(baseDir.relativize(path)) && !path.getFileName().toString().endsWith(".metadata")
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
		PathMatcher glob = GlobMatcher.glob("Code/ScriptLibraries/*.lss");
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
		Path xpages = baseDir.resolve("XPages");
		if(Files.exists(xpages) && Files.list(xpages).count() > 0) {
			return true;
		}
		Path ccs = baseDir.resolve("CustomControls");
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
		XResult xresult = DOMUtil.evaluateXPath(domDoc, "/classpath/classpathentry[kind=src]");
		List<String> paths = Arrays.stream(xresult.getNodes())
			.map(node -> Element.class.cast(node))
			.map(el -> el.getAttribute("path"))
			.filter(path -> !"Local".equals(path))
			.collect(Collectors.toList());
		paths.add("Code/Java");
		return paths.stream()
			.map(path -> getBaseDirectory().resolve(path))
			.filter(Files::exists)
			.filter(Files::isDirectory)
			.collect(Collectors.toList());
	}
}
