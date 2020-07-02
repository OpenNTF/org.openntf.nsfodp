package org.openntf.maven.nsfodp;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public abstract class AbstractCompilerMojo extends AbstractEquinoxMojo {

	/**
	 * Any update sites whose contents to use when compiling XPages elements.
	 * 
	 * <p>Overrides {@link #updateSite} if both are specified.</p>
	 */
	@Parameter(required = false)
	protected File[] updateSites;
	/**
	 * Any additional JARs to include on the compilation classpath.
	 * 
	 * @since 2.0.0
	 */
	@Parameter(required = false)
	protected File[] classpathJars;
	/**
	 * Location of the ODP directory.
	 */
	@Parameter(defaultValue = "odp", required = true)
	protected File odpDirectory;
	
	protected List<Path> collectUpdateSites() {
		List<Path> result = new ArrayList<>();
		
		if(this.updateSites != null && this.updateSites.length != 0) {
			result = Stream.of(this.updateSites)
					.filter(Objects::nonNull)
					.map(File::toPath)
					.collect(Collectors.toList());
		}
		
		for(Path updateSite : result) {
			if(updateSite != null) {
				if(!Files.exists(updateSite)) {
					throw new IllegalArgumentException(Messages.getString("CompileODPMojo.usDirDoesNotExist", updateSite.toAbsolutePath())); //$NON-NLS-1$
				}
				if(!Files.isDirectory(updateSite)) {
					throw new IllegalArgumentException(Messages.getString("CompileODPMojo.usDirNotADir", updateSite.toAbsolutePath())); //$NON-NLS-1$
				}
			}
		}
		
		
		return result;
	}

}
