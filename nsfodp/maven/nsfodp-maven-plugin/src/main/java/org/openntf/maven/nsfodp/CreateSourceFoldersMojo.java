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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.openntf.nsfodp.commons.xml.NSFODPDomUtil;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
		
		if(!project.getPackaging().equals("domino-nsf")) { //$NON-NLS-1$
			if(log.isInfoEnabled()) {
				log.info(Messages.getString("GeneratePDEStructureMojo.skip")); //$NON-NLS-1$
			}
			return;
		}
		
		try {
			generateSourceFolders();
		} catch(IOException e) {
			throw new MojoExecutionException("Exception while generating build.properties", e);
		}

		try {
			generateResourcesFiles();
		} catch(IOException e) {
			throw new MojoExecutionException("Exception while generating Resources/Files", e);
		}
	}

	private void generateSourceFolders() throws IOException {
		Path classpath = odpDirectory.toPath().resolve(".classpath"); //$NON-NLS-1$
		if(!Files.isReadable(classpath) || !Files.isRegularFile(classpath)) {
			return;
		}
		
		Document classpathXml;
		try(InputStream is = Files.newInputStream(classpath)) {
			classpathXml = NSFODPDomUtil.createDocument((InputStream) is);
		}
		Collection<String> sourceFolders = NSFODPDomUtil.streamNodes(classpathXml, "/classpath/classpathentry[kind=src]") //$NON-NLS-1$
			.map(Element.class::cast)
			.map(el -> el.getAttribute("path")) //$NON-NLS-1$
			.filter(path -> !"Local".equals(path)) //$NON-NLS-1$
			.collect(Collectors.toCollection(LinkedHashSet::new));
		sourceFolders.add("Code/Java"); //$NON-NLS-1$
		
		sourceFolders.stream()
			.map(path -> odpDirectory.toPath().resolve(path.replace("/", odpDirectory.toPath().getFileSystem().getSeparator()))) //$NON-NLS-1$
			.filter(path -> !Files.exists(path))
			.forEach(path -> {
				if(log.isInfoEnabled()) {
					log.info(Messages.getString("CreateSourceFoldersMojo.generatingFolder", project.getBasedir().toPath().relativize(path))); //$NON-NLS-1$
				}
				try {
					Files.createDirectories(path);
					buildContext.refresh(path.toFile());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
	}
	
	private void generateResourcesFiles() throws IOException {
		Path files = odpDirectory.toPath().resolve("Resources").resolve("Files"); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.exists(files)) {
			Files.createDirectories(files);
			buildContext.refresh(files.toFile());
		}
	}
}