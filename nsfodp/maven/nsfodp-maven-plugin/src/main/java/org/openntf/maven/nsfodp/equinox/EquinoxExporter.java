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
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.openntf.nsfodp.commons.NSFODPConstants;

/**
 * Represents an Equinox environment to export the provided project.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class EquinoxExporter extends AbstractEquinoxTask {

	public EquinoxExporter(PluginDescriptor pluginDescriptor, MavenSession mavenSession, MavenProject project, Log log, Path notesProgram, URL notesPlatform, Path notesIni) throws IOException {
		super(pluginDescriptor, mavenSession, project, log, notesProgram, notesPlatform, notesIni);
	}

	public void exportOdp(Path odpDir, String databasePath, boolean binaryDxl, boolean swiperFilter, boolean richTextAsItemData) {
		Map<String, String> props = new HashMap<>();
		props.put(NSFODPConstants.PROP_OUTPUTFILE, odpDir.toAbsolutePath().toString());
		props.put(NSFODPConstants.PROP_EXPORTER_DATABASE_PATH, databasePath);
		props.put(NSFODPConstants.PROP_EXPORTER_BINARY_DXL, Boolean.toString(binaryDxl));
		props.put(NSFODPConstants.PROP_EXPORTER_SWIPER_FILTER, Boolean.toString(swiperFilter));
		props.put(NSFODPConstants.PROP_RICH_TEXT_AS_ITEM_DATA, Boolean.toString(richTextAsItemData));
		props.put(NSFODPConstants.PROP_PROJECT_NAME, getProject().getGroupId() + '.' + getProject().getArtifactId());
		Path notesIni = getNotesIni();
		if(notesIni != null) {
			props.put(NSFODPConstants.PROP_NOTESINI, notesIni.toString());
		}
		setSystemProperties(props);
		
		run("org.openntf.nsfodp.exporter.equinox.ExporterApplication"); //$NON-NLS-1$
	}
}
