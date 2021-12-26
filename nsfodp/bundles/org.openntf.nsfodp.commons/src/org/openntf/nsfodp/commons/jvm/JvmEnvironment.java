package org.openntf.nsfodp.commons.jvm;

import java.nio.file.Path;

/**
 * Represents a running JVM for Equinox tasks.
 * 
 * @author Jesse Gallagher
 * @since 3.7.0
 */
public interface JvmEnvironment {
	/**
	 * Determines the path to the Java home directory.
	 * 
	 * @return a {@link Path} representing the home dir
	 */
	Path getJavaHome();
	
	/**
	 * Determines the path to the Java executable file, e.g.
	 * {@code "C:\Java\bin\java.exe"}.
	 * 
	 * @return a {@link Path} representing the Java binary
	 */
	Path getJavaBin();
}
