package org.openntf.maven.nsfodp.jvm;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;

/**
 * An implementation of {@link JvmEnvironment} that uses the Notes/Domino
 * JVM.
 * 
 * @author Jesse Gallagher
 * @since 3.7.0
 */
public class NotesJvmEnvironment extends AbstractJvmEnvironment {

	@Override
	public boolean isActive(Path notesProgram) {
		return !SystemUtils.IS_OS_MAC;
	}

	@Override
	public Path getJavaHome(Path notesProgram) {
		Path jvmHome = notesProgram.resolve("jvm"); //$NON-NLS-1$
		if(!Files.isDirectory(jvmHome)) {
			throw new RuntimeException("Could not find JVM at " + jvmHome);
		}
		return jvmHome;
	}

}
