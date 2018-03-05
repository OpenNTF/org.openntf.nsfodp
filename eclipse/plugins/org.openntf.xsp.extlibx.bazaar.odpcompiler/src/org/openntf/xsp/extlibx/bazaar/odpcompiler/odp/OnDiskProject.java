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
package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODPUtil;
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
	private final Path baseDir;
	
	public OnDiskProject(Path baseDirectory) {
		this.baseDir = Objects.requireNonNull(baseDirectory);
	}
	
	public Path getBaseDirectory() {
		return baseDir;
	}
	
	public Path getClasspathFile() {
		Path classpath = baseDir.resolve(".classpath");
		if(!Files.exists(classpath)) {
			throw new IllegalStateException("Classpath file does not exist: " + classpath.toAbsolutePath());
		}
		if(!Files.isRegularFile(classpath)) {
			throw new IllegalStateException("Classpath file is not a file: " + classpath.toAbsolutePath());
		}
		return classpath;
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
		// TODO add importing of *.javalib
		// really, best to change this to filename/extension matching
		List<String> standardDirs = Arrays.asList(
			"AppProperties" + File.separator + "$DBIcon",
			"AppProperties" + File.separator + "database.properties",
			"Forms",
			"Framesets",
			"Pages",
			"SharedElements" + File.separator + "Fields",
			"SharedElements" + File.separator + "Outlines",
			"Views"
		);
		List<String> standardFiles = Arrays.asList(
			"Code" + File.separator + "dbscript.lsdb",
			"Resources" + File.separator + "AboutDocument",
			//"Resources" + File.separator + "IconNote",
			"Resources" + File.separator + "UsingDocument"
		);
		
		Map<Path, String> result = new LinkedHashMap<>();
		result.putAll(standardDirs.stream()
			.map(dir -> baseDir.resolve(dir))
			.filter(Files::isDirectory)
			.map(t -> {
				try {
					return Files.list(t);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.flatMap(Function.identity())
			.collect(Collectors.toMap(Function.identity(), ODPUtil::readFile)));
		result.putAll(standardFiles.stream()
			.map(file -> baseDir.resolve(file))
			.filter(Files::isRegularFile)
			.collect(Collectors.toMap(Function.identity(), ODPUtil::readFile)));
		return result;
	}
	
	// Special: image resources, Themes, stylesheets, files, META-INF/MANIFEST.MF, WebContent, plugin.xml, xspdesign.properties, most code
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private List<Path> findSourceFolders() throws FileNotFoundException, IOException, XMLException {
		Path classpath = getClasspathFile();
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
