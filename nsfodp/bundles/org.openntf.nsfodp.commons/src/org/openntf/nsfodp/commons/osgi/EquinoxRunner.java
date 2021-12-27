/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.nsfodp.commons.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;

public class EquinoxRunner {
	private JvmEnvironment jvm;
	private Path notesProgram;
	private final List<Path> classpath = new ArrayList<>();
	private final List<String> platform = new ArrayList<>();
	private Path workingDirectory;
	private final Map<String, String> environmentVars = new HashMap<>();
	private final Map<String, String> jvmProps = new HashMap<>();
	private String osgiBundle;
	private Path logFile;
	private String jvmArgs;
	
	public JvmEnvironment getJvmEnvironment() {
		return this.jvm;
	}
	public void setJvmEnvironment(JvmEnvironment jvm) {
		this.jvm = jvm;
	}
	
	public Path getNotesProgram() {
		return notesProgram;
	}
	public void setNotesProgram(Path notesProgram) {
		this.notesProgram = notesProgram;
		String shim = createJempowerShim(notesProgram);
		if(shim != null) {
			addPlatformEntry(shim);
		}
	}
	public void setOsgiBundle(String osgiBundle) {
		this.osgiBundle = osgiBundle;
	}
	
	public void addClasspathJar(Path jar) {
		classpath.add(jar);
	}
	
	public void addPlatformEntry(String entry) {
		platform.add(entry);
	}
	
	public void setWorkingDirectory(Path workingDirectory) throws IOException {
		this.workingDirectory = workingDirectory;
		Path configuration = workingDirectory.resolve("configuration"); //$NON-NLS-1$
		Files.createDirectories(configuration);
		this.logFile = configuration.resolve("nsfodp.log"); //$NON-NLS-1$
		Files.deleteIfExists(logFile);
	}
	
	public void addEnvironmentVar(String name, String value) {
		this.environmentVars.put(name, value);
	}
	
	/**
	 * Adds a property value to be specified in the Java launch command. These
	 * arguments are passed as {@code -Dname=value}.
	 * 
	 * @param name the name of the property to set
	 * @param value the value of the property
	 * @since 3.7.0
	 */
	public void addJvmLaunchProperty(String name, String value) {
		this.jvmProps.put(name, value);
	}
	
	public Path getLogFile() {
		return logFile;
	}
	
	/**
	 * @param jvmArgs an argument string to add to the JVM launch
	 * @since 3.5.0
	 */
	public void setJvmArgs(String jvmArgs) {
		this.jvmArgs = jvmArgs;
	}
	
	/**
	 * Builds the command string used to launch the Equinox process, as used by
	 * {@link #start(String)}.
	 * 
	 * <p>As a side effect, this creates the Equinox launch configuration file
	 * and working directory.</p>
	 * 
	 * @param applicationId the Equinox application ID to launch
	 * @return a {@link List} of the exec command and arguments
	 * @throws IOException if there is a problem building the command
	 * @since 3.7.0
	 */
	public List<String> getCommand(String applicationId) throws IOException {
		Objects.requireNonNull(jvm, "jvmEnvironment must be set");
		Objects.requireNonNull(notesProgram, "notesProgram must be set");
		Objects.requireNonNull(workingDirectory, "workingDirectory must be set");
		Objects.requireNonNull(osgiBundle, "core OSGi bundle must be set");
		
		if(Files.exists(workingDirectory)) {
			// Always start clean, as existing data can cause trouble
			NSFODPUtil.deltree(workingDirectory);
		}
		Files.createDirectories(workingDirectory);
		
		Path plugins = workingDirectory.resolve("plugins"); //$NON-NLS-1$
		Files.createDirectories(plugins);
		
		List<String> platform = new ArrayList<>(this.platform);
		platform.add(createClasspathExtensionBundle(this.classpath, plugins));
		
		Path workspace = workingDirectory.resolve("workspace"); //$NON-NLS-1$
		Files.createDirectories(workspace);
		
		Path configuration = workingDirectory.resolve("configuration"); //$NON-NLS-1$
		Files.createDirectories(configuration);
		Path configIni = configuration.resolve("config.ini"); //$NON-NLS-1$
		Properties config = new Properties();
		config.put("osgi.bundles.defaultStartLevel", "4"); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("osgi.bundles", String.join(",", platform)); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("eclipse.application", applicationId); //$NON-NLS-1$
		config.put("osgi.configuration.cascaded", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("osgi.install.area", workingDirectory.toUri().toString()); //$NON-NLS-1$
		config.put("osgi.instance.area", workspace.toAbsolutePath().toString()); //$NON-NLS-1$
		config.put("osgi.framework", osgiBundle); //$NON-NLS-1$
		config.put("osgi.parentClassloader", "ext"); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("osgi.classloader.define.packages", "noattributes"); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("org.osgi.framework.bootdelegation", "lotus.*"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Logger configuration
		config.put("eclipse.log.level", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("osgi.logfile", logFile.toString()); //$NON-NLS-1$
		
		try(OutputStream os = Files.newOutputStream(configIni, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			config.store(os, "NSF ODP OSGi Configuration"); //$NON-NLS-1$
		}
		
		List<String> command = new ArrayList<>();
		command.add(getJvmEnvironment().getJavaBin(notesProgram).toString());
		if(this.jvmArgs != null) {
			// TODO account for spaces
			Stream.of(this.jvmArgs.split("\\s+")) //$NON-NLS-1$
				.filter(s -> s != null && !s.isEmpty())
				.forEach(command::add);
		}
		this.jvmProps.forEach((name, value) -> {
			// TODO better escaping
			command.add(MessageFormat.format("-D{0}={1}", name, value)); //$NON-NLS-1$
		});
		command.add("-Dosgi.frameworkParentClassloader=boot"); //$NON-NLS-1$
		command.add("org.eclipse.core.launcher.Main"); //$NON-NLS-1$
		command.add("-framwork"); //$NON-NLS-1$
		command.add(workingDirectory.toAbsolutePath().toString());
		command.add("-configuration"); //$NON-NLS-1$
		command.add(configuration.toAbsolutePath().toString());
		command.add("-consoleLog"); //$NON-NLS-1$
		
		if (NSFODPUtil.isOsMac()) {
			// Copy all *.lss files from the ../Resources directory in V12
			Path resources = notesProgram.getParent().resolve("Resources"); //$NON-NLS-1$
			if (Files.isDirectory(resources)) {
				Files.list(resources).filter(p -> p.getFileName().toString().toLowerCase().endsWith(".lss")) //$NON-NLS-1$
						.forEach(lss -> {
							Path dest = workingDirectory.resolve(lss.getFileName());
							try {
								Files.copy(lss, dest, StandardCopyOption.REPLACE_EXISTING);
							} catch (IOException e) {
								e.printStackTrace();
								throw new UncheckedIOException(e);
							}
						});
			}
		}
		
		return command;
	}
	
	/**
	 * Retrieves the environment variables to be send to the exec process,
	 * as used by {@link #start(String)}.
	 * 
	 * @return a {@link Map} of environment variables
	 * @since 3.7.0
	 */
	public Map<String, String> getExecEnvironmentVariables() {
		Map<String, String> env = new HashMap<>();
		env.put("Notes_ExecDirectory", notesProgram.toAbsolutePath().toString()); //$NON-NLS-1$
		StringBuilder path = new StringBuilder();
		path.append(notesProgram.toAbsolutePath().toString());
		Path resC = notesProgram.resolve("res").resolve("C"); //$NON-NLS-1$ //$NON-NLS-2$
		if(Files.isDirectory(resC)) {
			path.append(File.pathSeparatorChar);
			path.append(resC.toString());
		}
		env.put("PATH", path.toString()); //$NON-NLS-1$
		env.put("LD_LIBRARY_PATH", notesProgram.toAbsolutePath().toString()); //$NON-NLS-1$
		env.put("DYLD_LIBRARY_PATH", notesProgram.toAbsolutePath().toString()); //$NON-NLS-1$
		env.put("JAVA_HOME", getJvmEnvironment().getJavaHome(notesProgram).toString()); //$NON-NLS-1$
		env.put("CLASSPATH", //$NON-NLS-1$
			classpath.stream()
				.map(Path::toString)
				.collect(Collectors.joining(File.pathSeparator))
		);
		env.putAll(environmentVars);
		return env;
	}
	
	public Process start(String applicationId) throws IOException {
		Objects.requireNonNull(notesProgram, "notesProgram must be set");
		Objects.requireNonNull(workingDirectory, "workingDirectory must be set");
		Objects.requireNonNull(osgiBundle, "core OSGi bundle must be set");
		
		List<String> command = getCommand(applicationId);
		
		ProcessBuilder builder = new ProcessBuilder()
				.command(command)
				.directory(workingDirectory.toFile())
				.redirectOutput(Redirect.PIPE)
				.redirectError(Redirect.PIPE)
				.redirectInput(Redirect.INHERIT);
		Map<String, String> env = builder.environment();
		env.putAll(getExecEnvironmentVariables());
		
		return builder.start();
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	/**
     * @since 3.0.0
     */
    private static Path getMacNotesAppDir(Path notesProgram) {
    	if(notesProgram == null) {
    		return null;
    	}
    	Path notesApp = notesProgram.getParent();
    	if(notesApp.getParent() == null) {
    		return null;
    	}
    	return notesApp.getParent();
    }
    
    public static void addIBMJars(Path notesProgram, Collection<Path> classpath) {
    	Path lib = notesProgram.resolve("jvm").resolve("lib"); //$NON-NLS-1$ //$NON-NLS-2$
    	if(!Files.isDirectory(lib) && NSFODPUtil.isOsMac()) {
    		// Shared Java libs moved in V12
    		lib = notesProgram.getParent().resolve("Resources").resolve("jvm").resolve("lib"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	}
    	
    	// Add ibmpkcs.jar if available, though it's gone in V11
    	Path ibmPkcs = lib.resolve("ibmpkcs.jar"); //$NON-NLS-1$
    	if(!Files.isReadable(ibmPkcs) && NSFODPUtil.isOsMac()) {
    		// Different path on macOS
    		Path notesApp = getMacNotesAppDir(notesProgram);
    		if(notesApp != null) {
    			ibmPkcs = notesApp.resolve("jre").resolve("Contents").resolve("Home").resolve("lib").resolve("endorsed").resolve("ibmpkcs.jar"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    		}
    	}
    	if(Files.isReadable(ibmPkcs)) {
    		classpath.add(ibmPkcs);
    	}
    	
    	Path notesJar = lib.resolve("ext").resolve("Notes.jar"); //$NON-NLS-1$ //$NON-NLS-2$
    	if(!Files.isReadable(notesJar)) {
    		throw new IllegalStateException("Unable to locate Notes.jar at expected path " + notesJar);
    	}
    	classpath.add(notesJar);
    	
    	Path websvc = lib.resolve("ext").resolve("websvc.jar"); //$NON-NLS-1$ //$NON-NLS-2$
    	if(!Files.isReadable(websvc)) {
    		throw new IllegalStateException("Unable to locate websvc.jar at expected path " + websvc);
    	}
    	classpath.add(websvc);
    	
    	// Look for ndext and add all those to match the Domino classpath
    	Path ndext = notesProgram.resolve("ndext"); //$NON-NLS-1$
    	if(Files.isDirectory(ndext)) {
    		try(Stream<Path> filesStream = Files.list(ndext)) {
				filesStream
					.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar")) //$NON-NLS-1$
					.forEach(classpath::add);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    	}
    }
    
    public static String createJempowerShim(Path notesBin) {
    	try {
			Path njempcl = notesBin.resolve("jvm").resolve("lib").resolve("ext").resolve("njempcl.jar"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			if(!Files.isRegularFile(njempcl) && "MacOS".equals(notesBin.getFileName().toString())) { //$NON-NLS-1$
	    		// Shared Java libs moved in V12
				njempcl = notesBin.getParent().resolve("Resources").resolve("jvm").resolve("lib").resolve("ext").resolve("njempcl.jar"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	    	}
			if(Files.isRegularFile(njempcl)) {
				Path tempBundle = Files.createTempFile("njempcl", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
				try(OutputStream os = Files.newOutputStream(tempBundle)) {
					try(JarOutputStream jos = new JarOutputStream(os)) {
						JarEntry entry = new JarEntry("META-INF/MANIFEST.MF"); //$NON-NLS-1$
						jos.putNextEntry(entry);
						try(InputStream is = EquinoxRunner.class.getResourceAsStream("/res/COM.ibm.JEmpower/META-INF/MANIFEST.MF")) { //$NON-NLS-1$
							copyStream(is, jos, 8192);
						}
						JarEntry njempclEntry = new JarEntry("lib/njempcl.jar"); //$NON-NLS-1$
						jos.putNextEntry(njempclEntry);
						Files.copy(njempcl, jos);
					}
				}
				return "reference:" + tempBundle.toAbsolutePath().toUri(); //$NON-NLS-1$
			} else {
				// That's find - may be on a server
				return null;
			}
    	} catch(IOException e) {
    		throw new RuntimeException(e);
    	}
	}
	
    private static long copyStream(InputStream is, OutputStream os, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		long totalBytes = 0;
		int readBytes;
		while( (readBytes = is.read(buffer))>0 ) {
			os.write(buffer, 0, readBytes);
			totalBytes += readBytes;
		}
		return totalBytes;
    }
    
    public static String createClasspathExtensionBundle(Collection<Path> classpathJars, Path plugins) throws IOException {
		Path tempBundle = Files.createTempFile(plugins, "org.openntf.nsfodp.frameworkextension", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
		try(OutputStream os = Files.newOutputStream(tempBundle, StandardOpenOption.TRUNCATE_EXISTING)) {
			try(JarOutputStream jos = new JarOutputStream(os)) {
				JarEntry entry = new JarEntry("META-INF/MANIFEST.MF"); //$NON-NLS-1$
				jos.putNextEntry(entry);
				
				Manifest manifest = new Manifest();
				Attributes attrs = manifest.getMainAttributes();
				attrs.putValue("Manifest-Version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
				attrs.putValue("Bundle-ManifestVersion", "2"); //$NON-NLS-1$ //$NON-NLS-2$
				attrs.putValue("Bundle-SymbolicName", "org.openntf.nsfodp.classpathprovider"); //$NON-NLS-1$ //$NON-NLS-2$
				attrs.putValue("Bundle-Version", "1.0.0." + System.currentTimeMillis()); //$NON-NLS-1$ //$NON-NLS-2$
				attrs.putValue("Bundle-Name", "NSF ODP Tooling Extended Classpath Provider"); //$NON-NLS-1$ //$NON-NLS-2$
				
				if(classpathJars != null) {
					String exportPackage = classpathJars.stream()
						.map(EquinoxRunner::getPackages)
						.flatMap(Collection::stream)
						.filter(p -> !p.startsWith("lotus.")) //$NON-NLS-1$
						.collect(Collectors.joining(",")); //$NON-NLS-1$
					attrs.putValue("Export-Package", exportPackage); //$NON-NLS-1$
					
					attrs.putValue("Bundle-ClassPath", classpathJars.stream() //$NON-NLS-1$
						.filter(j -> !j.getFileName().toString().equals("Notes.jar")) //$NON-NLS-1$
						.filter(j -> !j.getFileName().toString().equals("websvc.jar")) //$NON-NLS-1$
						.map(j -> "external:" + j.toAbsolutePath()) //$NON-NLS-1$
						.collect(Collectors.joining(",")) //$NON-NLS-1$
					);
				}
				
				manifest.write(jos);
			}
		}
		return "reference:" + tempBundle.toAbsolutePath().toUri(); //$NON-NLS-1$
	}
    
    private static Collection<String> getPackages(Path jar) {
    	try {
    		Collection<String> packages = new HashSet<String>();
			try(InputStream is = NSFODPUtil.newInputStream(jar)) {
				try(JarInputStream jis = new JarInputStream(is)) {
					JarEntry jarEntry = jis.getNextJarEntry();
					while(jarEntry != null) {
						String name = jarEntry.getName();
						if(name.endsWith(".class") && !name.startsWith("java/") && name.indexOf('/') > 0) { //$NON-NLS-1$ //$NON-NLS-2$
							String packagePath = name.substring(0, name.lastIndexOf('/'));
							packages.add(packagePath.replace('/', '.'));
						}
						
						jarEntry = jis.getNextJarEntry();
					}
				}
			}
			return packages;
    	} catch(IOException e) {
    		throw new RuntimeException(e);
    	}
    }
}
