package org.openntf.nsfodp.commons.jvm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * Represents a running JVM for Equinox tasks. This ia a service type
 * that uses {@link ServiceLoader} for locating implementations.
 * 
 * @author Jesse Gallagher
 * @since 3.7.0
 */
public interface JvmEnvironment {
	static JvmEnvironment get() {
		return StreamSupport.stream(ServiceLoader.load(JvmEnvironment.class).spliterator(), false)
			.filter(JvmEnvironment::isActive)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Unable to locate JvmEnvironment service"));
	}
	
	/**
	 * Determines whether the provider is active for the current environment.
	 * 
	 * @return {@code true} if the JVM applies to this environment;
	 *         {@code false} otherwise
	 */
	boolean isActive();
	
	/**
	 * Determines the path to the Java home directory.
	 * 
	 * @param notesProgram the directory for a Notes or Domino installation
	 * @return a {@link Path} representing the home dir
	 */
	Path getJavaHome(Path notesProgram);
	
	/**
	 * Determines the path to the Java executable file, e.g.
	 * {@code "C:\Java\bin\java.exe"}.
	 * 
	 * @param notesProgram the directory for a Notes or Domino installation
	 * @return a {@link Path} representing the Java binary
	 */
	Path getJavaBin(Path notesProgram);
	
	/**
	 * Populates the JVM with JARs from the provided Notes/Domino program directory,
	 * if applicable.
	 *  
	 * @param notesProgram the directory for a Notes or Domino installation
	 * @throws IOException if there is a problem copying JARs
	 * @return a {@link Collection} of the JARs added, or an empty one if none were
	 *         added
	 */
	default Collection<Path> initNotesJars(Path notesProgram) throws IOException {
		return Collections.emptySet();
	}
	
	/**
	 * Retrieves a map of properties that should be set when executing the JVM.
	 * 
	 * @param notesProgram the directory for a Notes or Domino installation
	 * @return a {@link Map} of properties and values to set
	 */
	default Map<String, String> getJvmProperties(Path notesProgram) {
		return Collections.emptyMap();
	}
}
