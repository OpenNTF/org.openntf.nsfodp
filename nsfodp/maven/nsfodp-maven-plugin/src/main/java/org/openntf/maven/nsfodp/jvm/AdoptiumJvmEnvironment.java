package org.openntf.maven.nsfodp.jvm;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;

/**
 * An implementation of {@link JvmEnvironment} that uses an auto-downloaed
 * IBM Semeru OpenJ9 Java runtime when using Notes V12 on macOS.
 * 
 * @author Jesse Gallagher
 * @since 3.7.0
 */
public class AdoptiumJvmEnvironment extends AbstractMacGitHubJvmEnvironment {
	public static final String API_RELEASES = "https://api.github.com/repos/adoptium/temurin{0}-binaries/releases?per_page=100"; //$NON-NLS-1$
	public static final String PROVIDER_NAME = "Adoptium Temurin"; //$NON-NLS-1$
	public static final String SHORT_NAME = "termurin"; //$NON-NLS-1$
	
	@Override
	protected String getProviderName() {
		return PROVIDER_NAME;
	}
	
	@Override
	protected String getReleasesApi() {
		return API_RELEASES;
	}
	
	@Override
	protected String getShortName() {
		return SHORT_NAME;
	}

	@Override
	public boolean isActive(Path notesProgram) {
		// For now, assume that the presence of a _CodeSignature directory two levels up means Notes < 12
		if(SystemUtils.IS_OS_MAC) {
			return !Files.isDirectory(notesProgram.getParent().getParent().resolve("_CodeSignature")); //$NON-NLS-1$
		} else {
			return false;
		}
	}

}
