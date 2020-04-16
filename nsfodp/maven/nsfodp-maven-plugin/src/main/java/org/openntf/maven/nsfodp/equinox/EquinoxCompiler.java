/**
 * Copyright © 2018-2019 Jesse Gallagher
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
package org.openntf.maven.nsfodp.equinox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.openntf.maven.nsfodp.util.ODPMojoUtil;
import org.openntf.nsfodp.commons.NSFODPConstants;

import com.ibm.commons.util.StringUtil;

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

	public void compileOdp(
			Path odpDirectory,
			List<Path> updateSites,
			Collection<Path> classpathJars,
			Path outputFile,
			String compilerLevel,
			boolean appendTimestampToTitle,
			String templateName,
			boolean setProductionXspOptions,
			String odsRelease
		) {
		setClasspathJars(classpathJars);
		
		Map<String, String> props = new HashMap<>();
		props.put(NSFODPConstants.PROP_ODPDIRECTORY, odpDirectory.toAbsolutePath().toString());
		setUpdateSites(updateSites);
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
		props.put(NSFODPConstants.PROP_ODSRELEASE, StringUtil.toString(odsRelease));
		
		setSystemProperties(props);
		
		run("org.openntf.nsfodp.compiler.equinox.CompilerApplication"); //$NON-NLS-1$
	}

	
}
