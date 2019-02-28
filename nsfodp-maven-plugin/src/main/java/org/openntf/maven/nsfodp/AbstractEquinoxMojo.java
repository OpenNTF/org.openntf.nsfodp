package org.openntf.maven.nsfodp;

import java.io.File;
import java.net.URL;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

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
	 * Sets the project to require server execution even if a local environment is
	 * available.
	 * 
	 * <p>This may be useful for project that use LS2J, which can crash the Equinox JVM during
	 * compilation.</p>
	 */
	@Parameter(property="nsfodp.requireServerExecution", required=false)
	protected boolean requireServerExecution = false;

	protected boolean isRunLocally() {
		return notesProgram != null && notesPlatform != null && !requireServerExecution;
	}
}
