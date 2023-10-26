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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.openntf.maven.nsfodp.container.NSFODPContainer;

public abstract class AbstractEquinoxMojo extends AbstractMojo {
	@Parameter(defaultValue="${project}", readonly=true, required=false)
	protected MavenProject project;

	@Component
	protected WagonManager wagonManager;
	
	@Parameter(defaultValue="${plugin}", readonly=true)
	protected PluginDescriptor pluginDescriptor;
	
	@Parameter(defaultValue="${session}", readonly=true)
	protected MavenSession mavenSession;

	/**
	 * Location of the generated NSF.
	 */
	@Parameter(defaultValue="${project.build.directory}", property="outputDir", required=true)
	protected File outputDirectory;
	
	/**
	 * The path to a Notes or Domino executable directory to allow for local
	 * compilation.
	 * 
	 * <p>This must be paired with {@code notesPlatform}.</p>
	 */
	@Parameter(property="notes-program", required=false)
	protected File notesProgram;
	/**
	 * The path to a Domino OSGi runtime directory to allow for local
	 * compilation.
	 * 
	 * <p>This must be paired with {@code notesPlatform}.</p>
	 * @see <a href="https://stash.openntf.org/projects/P2T/repos/generate-domino-update-site/browse">
	 * 		https://stash.openntf.org/projects/P2T/repos/generate-domino-update-site/browse
	 * 		</a>
	 */
	@Parameter(property="notes-platform")
	protected URL notesPlatform;
	
	/**
	 * The path to the notes.ini file to use on launch. This is primarily of importance for local
	 * compilation on Linux systems, where the INI file is not reliably located by the runtime.
	 * 
	 * @since 3.0.0
	 */
	@Parameter(property="notes-ini", required=false)
	protected File notesIni;
	
	/**
	 * Sets the project to require server execution even if a local environment is
	 * available.
	 * 
	 * <p>This may be useful for project that use LS2J, which can crash the Equinox JVM during
	 * compilation.</p>
	 */
	@Parameter(property="nsfodp.requireServerExecution", required=false)
	protected boolean requireServerExecution = false;
	
	/**
	 * Sets process arguments to append to the Equinox JVM launcher, such as debug options.
	 * @since 3.5.0
	 */
	@Parameter(property="nsfodp.equinoxJvmArgs", required=false)
	protected String equinoxJvmArgs;
	
	/**
	 * Skips execution of this mojo.
	 * 
	 * @since 3.7.0
	 */
	@Parameter(required=false, defaultValue = "false")
	protected boolean skip;
	
	/**
	 * Sets whether execution should run in a Docker-compatible container.
	 * 
	 * @since 3.11.0
	 */
	@Parameter(property="nsfodp.useContainerExecution", required=false)
	protected boolean container;

	protected boolean isRunLocally() {
		if(this.container) {
			return false;
		}
		return notesProgram != null && notesPlatform != null && !requireServerExecution;
	}
	
	protected Optional<NSFODPContainer> initContainerIfNeeded(List<Path> updateSites, Path packageZip) {
		if(this.container) {
			Log log = getLog();
			
			// Initialize the Docker container
			if(log.isInfoEnabled()) {
				log.info("Initializing NSF ODP container");
			}
			NSFODPContainer container = new NSFODPContainer(updateSites, packageZip, log, outputDirectory.toPath());
			
			container.start();
			if(log.isInfoEnabled()) {
				log.info(MessageFormat.format("Started container: {0}", container.getContainerName()));
			}
			
			// Set the remote URL
			String host = container.getHost();
			int port = container.getMappedPort(80);
			try {
				URI uri = URI.create("http://" + host + ":" + port); //$NON-NLS-1$ //$NON-NLS-2$
				if(log.isDebugEnabled()) {
					log.debug(MessageFormat.format("Setting server URL to container path {0}", uri.toString()));
				}
				setServerUrl(uri.toURL());
			} catch (MalformedURLException e) {
				try {
					container.close();
				} catch(Exception e2) {
					if(log.isWarnEnabled()) {
						log.warn("Unable to terminate container", e);
					}
				}
				throw new RuntimeException(e);
			}
			
			return Optional.of(container);
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * Called by container initialization to override any otherwise-set
	 * remote URL to be the container URL
	 * 
	 * @param serverUrl the new URL to set
	 */
	protected abstract void setServerUrl(URL serverUrl);
}
