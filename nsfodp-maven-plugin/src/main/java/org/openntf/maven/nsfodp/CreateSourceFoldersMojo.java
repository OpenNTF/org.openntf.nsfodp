package org.openntf.maven.nsfodp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
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
 * Creates source folders on the filesystem that are referenced by the ODP
 * configuration files if they don't already exist.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
@Mojo(name="create-source-folders")
public class CreateSourceFoldersMojo extends AbstractMojo {
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
			generateSourceFolders();
		} catch(IOException | XMLException e) {
			throw new MojoExecutionException("Exception while generating build.properties", e);
		}
	}

	private void generateSourceFolders() throws IOException, XMLException {
		Path classpath = odpDirectory.toPath().resolve(".classpath");
		if(!Files.isReadable(classpath) || !Files.isRegularFile(classpath)) {
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
		
		sourceFolders.stream()
			.map(path -> odpDirectory.toPath().resolve(path.replace('/', File.separatorChar)))
			.filter(path -> !Files.exists(path))
			.forEach(path -> {
				if(log.isInfoEnabled()) {
					log.info(Messages.getString("CreateSourceFoldersMojo.generatingFolder", project.getBasedir().toPath().relativize(path)));
				}
				try {
					Files.createDirectories(path);
					buildContext.refresh(path.toFile());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
	}
}