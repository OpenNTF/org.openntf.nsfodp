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
import org.openntf.nsfodp.commons.NSFODPConstants;

/**
 * Represents an Equinox environment to transpile XSP source in the provided project.
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public class EquinoxTranspiler extends AbstractEquinoxTask {

	public EquinoxTranspiler(PluginDescriptor pluginDescriptor, MavenSession mavenSession, MavenProject project,
			Log log, Path notesProgram, URL notesPlatform, Path notesIni) throws IOException {
		super(pluginDescriptor, mavenSession, project, log, notesProgram, notesPlatform, notesIni);
	}
	
	public void transpileXsp(
			Path xspSourceRoot,
			Path ccSourceRoot,
			List<Path> updateSites,
			Collection<Path> classpathJars,
			Path outputDirectory
		) {
		setClasspathJars(classpathJars);
		
		Map<String, String> props = new HashMap<>();
		props.put(NSFODPConstants.PROP_XSP_SOURCE_ROOT, xspSourceRoot.toAbsolutePath().toString());
		props.put(NSFODPConstants.PROP_CC_SOURCE_ROOT, ccSourceRoot.toAbsolutePath().toString());
		setUpdateSites(updateSites);
		props.put(NSFODPConstants.PROP_OUTPUTFILE, outputDirectory.toAbsolutePath().toString());
		
		Path notesIni = getNotesIni();
		if(notesIni != null) {
			props.put(NSFODPConstants.PROP_NOTESINI, notesIni.toString());
		}
		
		setSystemProperties(props);
		
		run("org.openntf.nsfodp.transpiler.equinox.TranspilerApplication"); //$NON-NLS-1$
	}

}
