package org.openntf.maven.nsfodp.equinox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.openntf.maven.nsfodp.util.ODPMojoUtil;
import org.openntf.nsfodp.commons.NSFODPConstants;

/**
 * Represents an Equinox environment to compile the provided project.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class EquinoxCompiler extends AbstractEquinoxTask {
	public EquinoxCompiler(PluginDescriptor pluginDescriptor, MavenSession mavenSession, MavenProject project, Log log, Path notesProgram, URL notesPlatform) throws IOException {
		super(pluginDescriptor, mavenSession, project, log, notesProgram, notesPlatform);
	}

	public void compileOdp(Path odpDirectory, Path updateSite, Collection<Path> classpathJars, Path outputFile, String compilerLevel, boolean appendTimestampToTitle, String templateName, boolean setProductionXspOptions) {
		setClasspathJars(classpathJars);
		
		Map<String, String> props = new HashMap<>();
		props.put(NSFODPConstants.PROP_ODPDIRECTORY, odpDirectory.toAbsolutePath().toString());
		if(updateSite != null) {
			props.put(NSFODPConstants.PROP_UPDATESITE, updateSite.toAbsolutePath().toString());
		}
		props.put(NSFODPConstants.PROP_OUTPUTFILE, outputFile.toAbsolutePath().toString());
		if(compilerLevel != null) {
			props.put(NSFODPConstants.PROP_COMPILERLEVEL, compilerLevel);
		}
		props.put(NSFODPConstants.PROP_APPENDTIMESTAMPTOTITLE, Boolean.toString(appendTimestampToTitle));
		if(templateName != null) {
			props.put(NSFODPConstants.PROP_TEMPLATENAME, templateName);
			props.put(NSFODPConstants.PROP_TEMPLATEVERSION, ODPMojoUtil.calculateVersion(getProject()));
		}
		props.put(NSFODPConstants.PROP_SETPRODUCTIONXSPOPTIONS, Boolean.toString(setProductionXspOptions));
		setSystemProperties(props);
		
		run("org.openntf.nsfodp.compiler.equinox.CompilerApplication");
	}

	
}
