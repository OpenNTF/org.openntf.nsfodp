/**
 * Copyright © 2018-2022 Jesse Gallagher
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.openntf.maven.nsfodp.equinox.EquinoxTranspiler;

/**
 * Transpiles XSP source to Java.
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
@Mojo(name="transpile-xsp", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution=ResolutionScope.COMPILE)
public class TranspileXspMojo extends AbstractCompilerMojo {
	
	@Parameter(defaultValue="${project.build.directory}", readonly=true)
	private File outputDirectory;
	
	/**
	 * The root directory to search for XSP files. Defaults to searching for
	 * {@code "XPages"} in the ODP directory or {@code src/main/webapp/WEB-INF/xpages}.
	 */
	@Parameter(required=false)
	private File xspSourceRoot;
	
	/**
	 * The root directory to search for XSP files. Defaults to searching for
	 * {@code "CustomControls"} in the ODP directory or {@code src/main/webapp/WEB-INF/controls}.
	 */
	@Parameter(required=false)
	private File ccSourceRoot;

	private Log log;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();

		Path xspSourceRoot = this.xspSourceRoot == null ? null : this.xspSourceRoot.toPath();
		if(xspSourceRoot == null) {
			xspSourceRoot = odpDirectory.toPath().resolve("XPages"); //$NON-NLS-1$
			if(!Files.exists(xspSourceRoot)) {
				xspSourceRoot = project.getBasedir().toPath()
					.resolve("src") //$NON-NLS-1$
					.resolve("main") //$NON-NLS-1$
					.resolve("webapp") //$NON-NLS-1$
					.resolve("WEB-INF") //$NON-NLS-1$
					.resolve("xpages"); //$NON-NLS-1$
			}
			if(!Files.exists(xspSourceRoot)) {
				xspSourceRoot = null;
			}
		}
		if(log.isInfoEnabled()) {
			log.info("Using XSP source root: " + xspSourceRoot);
		}
		
		Path ccSourceRoot = this.ccSourceRoot == null ? null : this.ccSourceRoot.toPath();
		if(ccSourceRoot == null) {
			ccSourceRoot = odpDirectory.toPath().resolve("CustomControls"); //$NON-NLS-1$
			if(!Files.exists(ccSourceRoot)) {
				ccSourceRoot = project.getBasedir().toPath()
					.resolve("src") //$NON-NLS-1$
					.resolve("main") //$NON-NLS-1$
					.resolve("webapp") //$NON-NLS-1$
					.resolve("WEB-INF") //$NON-NLS-1$
					.resolve("controls"); //$NON-NLS-1$
			}
			if(!Files.exists(ccSourceRoot)) {
				ccSourceRoot = null;
			}
		}
		if(log.isInfoEnabled()) {
			log.info("Using Custom Control source root: " + ccSourceRoot);
		}

		List<Path> updateSites = collectUpdateSites();
		
		try {
			Path output = outputDirectory.toPath().resolve("generated-sources").resolve("java"); //$NON-NLS-1$ //$NON-NLS-2$
			Files.createDirectories(output);
			
			if(log.isDebugEnabled()) {
				log.debug("Using output directory: " + output);
			}
			
			if(isRunLocally()) {
				transpileXspLocal(xspSourceRoot, ccSourceRoot, updateSites, output);
			} else {
				throw new NotImplementedException("Server-based transpilation not yet implemented");
			}
			
		} catch (IOException e) {
			throw new MojoExecutionException("Exception while transpiling XSP source", e);
		}
	}

	// *******************************************************************************
	// * Local transpilation
	// *******************************************************************************
	
	private void transpileXspLocal(Path xspSourceRoot, Path ccSourceRoot, List<Path> updateSites, Path outputDirectory) throws IOException {
		Path notesIni = this.notesIni == null ? null : this.notesIni.toPath();
		EquinoxTranspiler transpiler = new EquinoxTranspiler(pluginDescriptor, mavenSession, project, getLog(), notesProgram.toPath(), notesPlatform, notesIni);
		transpiler.setJvmArgs(this.equinoxJvmArgs);
		List<Path> jars = new ArrayList<>();
		if(this.classpathJars != null) {
			Arrays.stream(this.classpathJars).map(File::toPath).forEach(jars::add);
		}
		
		this.project.getArtifacts().stream()
			.map(Artifact::getFile)
			.map(File::toPath)
			.forEach(jars::add);
		transpiler.transpileXsp(xspSourceRoot, ccSourceRoot, updateSites, jars, outputDirectory);
	}
	
	// *******************************************************************************
	// * Server-based transpilation
	// *******************************************************************************
	
	@Override
	protected void setServerUrl(URL serverUrl) {
		// NOP for now
	}
}
