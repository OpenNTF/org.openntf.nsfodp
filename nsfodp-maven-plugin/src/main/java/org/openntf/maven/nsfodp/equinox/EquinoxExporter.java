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

	public EquinoxExporter(PluginDescriptor pluginDescriptor, MavenSession mavenSession, MavenProject project, Log log, Path notesProgram, URL notesPlatform) throws IOException {
		super(pluginDescriptor, mavenSession, project, log, notesProgram, notesPlatform);
	}

	public void exportOdp(Path odpDir, String databasePath, boolean binaryDxl, boolean swiperFilter) {
		Map<String, String> props = new HashMap<>();
		props.put(NSFODPConstants.PROP_OUTPUTFILE, odpDir.toAbsolutePath().toString());
		props.put(NSFODPConstants.PROP_EXPORTER_DATABASE_PATH, databasePath);
		props.put(NSFODPConstants.PROP_EXPORTER_BINARY_DXL, Boolean.toString(binaryDxl));
		props.put(NSFODPConstants.PROP_EXPORTER_SWIPER_FILTER, Boolean.toString(swiperFilter));
		setSystemProperties(props);
		
		run("org.openntf.nsfodp.exporter.equinox.ExporterApplication");
	}
}
