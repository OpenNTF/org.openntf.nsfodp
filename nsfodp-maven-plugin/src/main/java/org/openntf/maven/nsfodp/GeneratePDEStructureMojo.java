package org.openntf.maven.nsfodp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

/**
 * Creates PDE structure files (MANIFEST.MF, build.properties) at the top level of
 * the current project based on the On-Disk Project data.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
@Mojo(name="generate-pde-structure")
public class GeneratePDEStructureMojo extends AbstractMojo {
	@Parameter(defaultValue="${project}", readonly=true, required=false)
	protected MavenProject project;
	
	/**
	 * Location of the ODP directory.
	 */
	@Parameter(defaultValue="odp", required=true)
	private File odpDirectory;
	
	@Component
	private BuildContext buildContext;
	
	Log log;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();
		
		if(!project.getPackaging().equals("domino-nsf")) {
			if(log.isInfoEnabled()) {
				log.info(Messages.getString("GeneratePDEStructureMojo.skip")); //$NON-NLS-1$
			}
			return;
		}
		
		try {
			generateBuildProperties();
		} catch(IOException | XMLException e) {
			throw new MojoExecutionException("Exception while generating build.properties", e);
		}
		try {
			generateManifestMf();
		} catch(IOException | XMLException e) {
			throw new MojoExecutionException("Exception while generating META-INF/MANIFEST.MF", e);
		}
	}

	private void generateBuildProperties() throws IOException, XMLException {
		Path classpath = odpDirectory.toPath().resolve(".classpath");
		if(!Files.isReadable(classpath) || !Files.isRegularFile(classpath)) {
			if(log.isWarnEnabled()) {
				log.warn(Messages.getString("GeneratePDEStructureMojo.noClasspath"));
			}
			return;
		}
		
		Document classpathXml;
		try(InputStream is = Files.newInputStream(classpath)) {
			classpathXml = DOMUtil.createDocument(is);
		}
		Collection<String> sourceFolders = Stream.of(DOMUtil.nodes(classpathXml, "/classpath/classpathentry[kind=src]"))
			.map(Element.class::cast)
			.map(el -> el.getAttribute("path"))
			.filter(path -> !"Local".equals(path))
			.collect(Collectors.toCollection(LinkedHashSet::new));
		sourceFolders.add("Code/Java");
		sourceFolders.add("Resources/Files");
		
		Path buildProperties = project.getBasedir().toPath().resolve("build.properties");
		try(OutputStream os = Files.newOutputStream(buildProperties, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			os.write(("bin.includes = META-INF/,\\\n" + 
					"               .\n" +
					"output.. = target/classes\n").getBytes());
			os.write("source.. = ".getBytes());
			os.write(
				sourceFolders.stream()
					.map(path -> "odp/" + path)
					.collect(Collectors.joining(",\\\n "))
					.getBytes()
			);
		}
		buildContext.refresh(buildProperties.toFile());
	}
	
	private void generateManifestMf() throws IOException, XMLException {
		Path metaInf = project.getBasedir().toPath().resolve("META-INF");
		Files.createDirectories(metaInf);
		Path manifestMf = metaInf.resolve("MANIFEST.MF");
		
		try(OutputStream os = Files.newOutputStream(manifestMf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			os.write(("Manifest-Version: 1.0\n" + 
					"Bundle-ManifestVersion: 2\n" + 
					"Bundle-Name: " + project.getName() + "\n" + 
					"Bundle-SymbolicName: " + project.getName() + "\n" + 
					"Automatic-Module-Name: " + project.getName() + "\n" +
					"Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n" +
					"Bundle-Version: " + project.getVersion().replace("-SNAPSHOT", ".qualifier") + "\n").getBytes()); // TODO translate version
			
			// Look for dependencies in the plugin.xml
			Path pluginXmlFile = odpDirectory.toPath().resolve("plugin.xml");
			if(Files.isReadable(pluginXmlFile) && Files.isRegularFile(pluginXmlFile)) {
				Document pluginXml;
				try(InputStream is = Files.newInputStream(pluginXmlFile)) {
					pluginXml = DOMUtil.createDocument(is);
				}
				
				Object[] nodes = DOMUtil.nodes(pluginXml, "/plugin/requires/import");
				if(nodes.length > 0) {
					os.write("Require-Bundle: ".getBytes());
					os.write(
						Stream.of(nodes)
							.map(Element.class::cast)
							.map(e -> e.getAttribute("plugin"))
							.collect(Collectors.joining(",\n "))
							.getBytes()
					);
					os.write('\n');
				}
			}
			
			// Look for jars in Code/Jars and WebContent/WEB-INF/lib
			List<String> jarPaths = new ArrayList<String>();
			
			Path jars = odpDirectory.toPath().resolve("Code").resolve("Jars");
			if(Files.isDirectory(jars)) {
				Files.walk(jars)
					.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".jar"))
					.forEach(path -> jarPaths.add("odp/Code/Jars/" + jars.relativize(path).toString().replace('/', File.separatorChar)));
			}
			
			Path lib = odpDirectory.toPath().resolve("WebContent").resolve("WEB-INF").resolve("lib");
			if(Files.isDirectory(lib)) {
				Files.walk(lib)
				.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".jar"))
				.forEach(path -> jarPaths.add("odp/WebContent/WEB-INF/lib/" + jars.relativize(path).toString().replace('/', File.separatorChar)));
			}
			if(!jarPaths.isEmpty()) {
				os.write("Bundle-Classpath: ".getBytes());
				os.write(
						jarPaths.stream()
						.collect(Collectors.joining(",\n "))
						.getBytes()
				);
				os.write('\n');
			}
		}
		buildContext.refresh(manifestMf.toFile());
	}
}
