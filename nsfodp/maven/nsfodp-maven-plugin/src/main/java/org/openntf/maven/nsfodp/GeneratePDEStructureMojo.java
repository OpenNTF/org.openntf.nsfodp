/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.maven.nsfodp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.openntf.nsfodp.commons.xml.NSFODPDomUtil;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Creates PDE structure files (MANIFEST.MF, build.properties) at the top level of
 * the current project based on the On-Disk Project data.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
@Mojo(name="generate-pde-structure", requiresDependencyResolution=ResolutionScope.COMPILE)
public class GeneratePDEStructureMojo extends AbstractMojo {
	@Parameter(defaultValue="${project}", readonly=true, required=false)
	protected MavenProject project;
	
	/**
	 * Location of the ODP directory.
	 */
	@Parameter(defaultValue="odp", required=true)
	private File odpDirectory;
	
	/**
	 * Any additional jars to include on the compilation classpath.
	 * 
	 * @since 2.0.0
	 */
	@Parameter(required=false)
	private File[] classpathJars;
	
	@Component
	private BuildContext buildContext;
	
	Log log;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();
		
		if(!project.getPackaging().equals("domino-nsf")) { //$NON-NLS-1$
			if(log.isInfoEnabled()) {
				log.info(Messages.getString("GeneratePDEStructureMojo.skip")); //$NON-NLS-1$
			}
			return;
		}
		
		try {
			generateBuildProperties();
		} catch(IOException e) {
			throw new MojoExecutionException("Exception while generating build.properties", e);
		}
		try {
			generateManifestMf();
		} catch(IOException e) {
			throw new MojoExecutionException("Exception while generating META-INF/MANIFEST.MF", e);
		}
	}

	private void generateBuildProperties() throws IOException {
		Path classpath = odpDirectory.toPath().resolve(".classpath"); //$NON-NLS-1$
		if(!Files.isReadable(classpath) || !Files.isRegularFile(classpath)) {
			if(log.isWarnEnabled()) {
				log.warn(Messages.getString("GeneratePDEStructureMojo.noClasspath")); //$NON-NLS-1$
			}
			return;
		}
		
		Document classpathXml;
		try(InputStream is = Files.newInputStream(classpath)) {
			classpathXml = NSFODPDomUtil.createDocument((InputStream) is);
		}
		Collection<String> sourceFolders = NSFODPDomUtil.streamNodes(classpathXml, "/classpath/classpathentry[@kind='src']") //$NON-NLS-1$
			.map(Element.class::cast)
			.map(el -> el.getAttribute("path")) //$NON-NLS-1$
			.filter(path -> !"Local".equals(path)) //$NON-NLS-1$
			.collect(Collectors.toCollection(LinkedHashSet::new));
		sourceFolders.add("Code/Java"); //$NON-NLS-1$
		sourceFolders.add("Resources/Files"); //$NON-NLS-1$
		
		Properties props = new Properties();
		props.put("bin.includes", "META-INF/,."); //$NON-NLS-1$ //$NON-NLS-2$
		props.put("output..", "target/classes"); //$NON-NLS-1$ //$NON-NLS-2$
		props.put("source..", sourceFolders.stream() //$NON-NLS-1$
				.map(path -> "odp/" + path) //$NON-NLS-1$
				.collect(Collectors.joining(","))); //$NON-NLS-1$
		
		// Look for JARs specified in the Maven project
		Set<Path> extraPaths = new LinkedHashSet<>();
		if(this.classpathJars != null) {
			// Though the Eclipse docs say that you should use relative paths, the IDE only actually
			//   picks up on absolute paths
			Stream.of(this.classpathJars)
				.map(File::toPath)
				.forEach(extraPaths::add);
		}
		this.project.getArtifacts().stream()
			.map(Artifact::getFile)
			.map(File::toPath)
			.forEach(extraPaths::add);
		if(!extraPaths.isEmpty()) {
			props.put("extra..", extraPaths.stream() //$NON-NLS-1$
				.map(Path::toAbsolutePath)
				.map(Path::toString)
				.collect(Collectors.joining(","))); //$NON-NLS-1$
		}
		
		Path buildProperties = project.getBasedir().toPath().resolve("build.properties"); //$NON-NLS-1$
		try(OutputStream os = buildContext.newFileOutputStream(buildProperties.toFile())) {
			props.store(os, "Generated by " + getClass().getName()); //$NON-NLS-1$
		}
	}
	
	private void generateManifestMf() throws IOException {
		Path metaInf = project.getBasedir().toPath().resolve("META-INF"); //$NON-NLS-1$
		Files.createDirectories(metaInf);
		Path manifestMf = metaInf.resolve("MANIFEST.MF"); //$NON-NLS-1$
		
		String symbolicName = project.getGroupId() + '.' + project.getArtifactId();
		
		Manifest manifest = new Manifest();
		Attributes attrs = manifest.getMainAttributes();
		attrs.putValue("Manifest-Version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
		attrs.putValue("Bundle-ManifestVersion", "2"); //$NON-NLS-1$ //$NON-NLS-2$
		attrs.putValue("Bundle-Name", project.getName()); //$NON-NLS-1$
		attrs.putValue("Bundle-SymbolicName", symbolicName); //$NON-NLS-1$
		attrs.putValue("Automatic-Module-Name", symbolicName); //$NON-NLS-1$
		attrs.putValue("Bundle-RequiredExecutionEnvironment", "JavaSE-1.8"); //$NON-NLS-1$ //$NON-NLS-2$
		attrs.putValue("Bundle-Version", project.getVersion().replace("-SNAPSHOT", ".qualifier")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		// Look for plugin dependencies
		Path pluginXmlFile = odpDirectory.toPath().resolve("plugin.xml"); //$NON-NLS-1$
		if(Files.isReadable(pluginXmlFile) && Files.isRegularFile(pluginXmlFile)) {
			Document pluginXml;
			try(InputStream is = Files.newInputStream(pluginXmlFile)) {
				pluginXml = NSFODPDomUtil.createDocument((InputStream) is);
			}
			
			List<Node> nodes = NSFODPDomUtil.nodes(pluginXml, "/plugin/requires/import"); //$NON-NLS-1$
			if(!nodes.isEmpty()) {
				attrs.putValue("Require-Bundle", nodes.stream() //$NON-NLS-1$
					.map(Element.class::cast)
					.map(e -> e.getAttribute("plugin") + (isOptionalImport(e) ? ";resolution:=optional" : "")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					.collect(Collectors.joining(",")) //$NON-NLS-1$
				);
			}
		}
		
		Collection<String> jarPaths = new LinkedHashSet<>();
		
		// Look for jars in Code/Jars and WebContent/WEB-INF/lib
		Path jars = odpDirectory.toPath().resolve("Code").resolve("Jars"); //$NON-NLS-1$ //$NON-NLS-2$
		if(Files.isDirectory(jars)) {
			Files.walk(jars)
				.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".jar")) //$NON-NLS-1$
				.forEach(path -> jarPaths.add("odp/Code/Jars/" + jars.relativize(path).toString().replace("/", jars.getFileSystem().getSeparator()))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		Path lib = odpDirectory.toPath().resolve("WebContent").resolve("WEB-INF").resolve("lib"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(Files.isDirectory(lib)) {
			Files.walk(lib)
				.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".jar")) //$NON-NLS-1$
				.forEach(path -> jarPaths.add("odp/WebContent/WEB-INF/lib/" + lib.relativize(path).toString().replace("/", jars.getFileSystem().getSeparator()))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if(!jarPaths.isEmpty()) {
			attrs.putValue("Bundle-Classpath", String.join(",", jarPaths)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		attrs.putValue("Created-By", getClass().getName()); //$NON-NLS-1$
		
		try(OutputStream os = buildContext.newFileOutputStream(manifestMf.toFile())) {
			manifest.write(os);
		}
	}
	
	private boolean isOptionalImport(Element e) {
		if("true".equals(e.getAttribute("optional"))) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} else if("org.eclipse.ui".equals(e.getAttribute("plugin"))) { //$NON-NLS-1$ //$NON-NLS-2$
			// org.eclipse.ui is marked as required, but this is problematic for server-generated update sites
			return true;
		}
		return false;
	}
}
