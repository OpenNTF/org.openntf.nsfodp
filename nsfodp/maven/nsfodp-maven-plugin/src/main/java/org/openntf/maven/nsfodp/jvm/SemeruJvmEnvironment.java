package org.openntf.maven.nsfodp.jvm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;
import org.openntf.nsfodp.commons.osgi.EquinoxRunner;

/**
 * An implementation of {@link JvmEnvironment} that uses an auto-downloaed
 * IBM Semeru OpenJ9 Java runtime when using Notes V12 on macOS.
 * 
 * @author Jesse Gallagher
 * @since 3.7.0
 */
public class SemeruJvmEnvironment extends AbstractJvmEnvironment {
	

	@Override
	public boolean isActive() {
		return SystemUtils.IS_OS_MAC;
	}
	
	@Override
	public Path getJavaHome(Path notesProgram) {
		return MacOSJVMProvider.getJavaHome();
	}
	
	@Override
	public Collection<Path> initNotesJars(Path notesProgram) throws IOException {
		Collection<Path> toLink = new LinkedHashSet<>();
		EquinoxRunner.addIBMJars(notesProgram, toLink);

		Collection<Path> result = new LinkedHashSet<>();
		Path destBase = MacOSJVMProvider.getJavaHome().resolve("jre").resolve("lib").resolve("ext"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Files.createDirectories(destBase);
		
		for(Path jar : toLink) {
			Path destJar = destBase.resolve(jar.getFileName());
			Files.copy(jar, destJar, StandardCopyOption.REPLACE_EXISTING);
			result.add(destJar);
		}
		
		return result;
	}
	
	@Override
	public Map<String, String> getJvmProperties(Path notesProgram) {
		String escapedPath = notesProgram.toString();
		return Collections.singletonMap("java.library.path", escapedPath); //$NON-NLS-1$
	}

}
