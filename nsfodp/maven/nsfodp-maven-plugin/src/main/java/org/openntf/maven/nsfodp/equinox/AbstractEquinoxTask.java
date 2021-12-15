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
package org.openntf.maven.nsfodp.equinox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.openntf.maven.nsfodp.Messages;
import org.openntf.nsfodp.commons.osgi.EquinoxRunner;

public abstract class AbstractEquinoxTask {
	private final PluginDescriptor pluginDescriptor;
	private final MavenSession mavenSession;
	private final MavenProject project;
	private final Log log;
	private final Path notesProgram;
	private final URL notesPlatform;
	private final Path notesIni;
	
	private Collection<Path> classpathJars;
	private Map<String, String> equinoxEnvironmentVars;
	private List<Path> updateSites;
	
	private boolean successFlag;
	
	private String jvmArgs;

	public AbstractEquinoxTask(PluginDescriptor pluginDescriptor, MavenSession mavenSession, MavenProject project, Log log, Path notesProgram, URL notesPlatform, Path notesIni) throws IOException {
		this.pluginDescriptor = pluginDescriptor;
		this.mavenSession = mavenSession;
		this.log = log;
		this.notesProgram = notesProgram;
		this.project = project;
		this.notesPlatform = notesPlatform;
		this.notesIni = notesIni;
	}
	
	protected MavenProject getProject() {
		return project;
	}

	protected void setClasspathJars(Collection<Path> classpathJars) {
		this.classpathJars = classpathJars;
	}
	
	protected void setEquinoxEnvironmentVars(Map<String, String> properties) {
		this.equinoxEnvironmentVars = properties;
	}
	
	public void setUpdateSites(List<Path> updateSites) {
		this.updateSites = updateSites;
	}
	
	/**
	 * @since 3.0.0
	 */
	public Path getNotesIni() {
		return notesIni;
	}
	
	/**
	 * @since 3.5.0
	 */
	public void setJvmArgs(String jvmArgs) {
		this.jvmArgs = jvmArgs;
	}
	
	protected void run(String applicationId) {
		successFlag = false;
		try {
			Path equinox = getDependencyJar("org.eclipse.equinox.launcher"); //$NON-NLS-1$
			if(log.isDebugEnabled()) {
				log.debug(Messages.getString("EquinoxMojo.usingEquinoxLauncher", equinox)); //$NON-NLS-1$
			}
			
			EquinoxRunner runner = new EquinoxRunner();
			runner.setJavaBin(getJavaBinary(notesProgram));
			runner.setNotesProgram(notesProgram);
			runner.setJvmArgs(this.jvmArgs);
			
			if(classpathJars != null) {
				classpathJars.forEach(runner::addClasspathJar);
			}
			addNdextJars(runner);
			
			if(!Files.exists(notesProgram)) {
				throw new MojoExecutionException(Messages.getString("EquinoxMojo.notesProgramDirDoesNotExist", notesProgram)); //$NON-NLS-1$
			}
			
			Path target;
			if("standalone-pom".equals(project.getArtifactId())) { //$NON-NLS-1$
				target = Files.createTempDirectory("nsfodp"); //$NON-NLS-1$
			} else {
				target = Paths.get(project.getBuild().getDirectory());
			}
			Path framework = target.resolve("nsfodpequinox"); //$NON-NLS-1$
			if(log.isDebugEnabled()) {
				log.debug(Messages.getString("EquinoxMojo.creatingOsgi", framework)); //$NON-NLS-1$
			}
			runner.setWorkingDirectory(framework);

			Stream.of(
				getDependencyRef("org.openntf.nsfodp.commons", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.notesapi.darwinonapi", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.commons.dxl", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.commons.odp", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.compiler", 2), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.compiler.equinox", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.deployment", 2), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.exporter", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.exporter.equinox", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.transpiler", 2), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.transpiler.equinox", -1), //$NON-NLS-1$
				getDependencyRef("com.ibm.xsp.extlibx.bazaar", -1), //$NON-NLS-1$
				getDependencyRef("com.ibm.xsp.extlibx.bazaar.interpreter", -1), //$NON-NLS-1$
				getDependencyRef("com.darwino.domino.napi", -1) //$NON-NLS-1$
			).forEach(runner::addPlatformEntry);
			
			runner.addClasspathJar(equinox);
			
			Path notesPlatform = Paths.get(this.notesPlatform.toURI());
			if(!Files.exists(notesPlatform)) {
				throw new MojoExecutionException(Messages.getString("EquinoxMojo.notesPlatformDoesNotExist", notesPlatform)); //$NON-NLS-1$
			}
			Path notesPlugins = notesPlatform.resolve("plugins"); //$NON-NLS-1$
			if(!Files.exists(notesPlugins)) {
				throw new MojoExecutionException(Messages.getString("EquinoxMojo.notesPluginsDirDoesNotExist", notesPlugins)); //$NON-NLS-1$
			}
			String[] osgiBundle = new String[1];
			try(Stream<Path> pluginsStream = Files.list(notesPlugins)) {
				pluginsStream.filter(p -> p.getFileName().toString().endsWith(".jar")) //$NON-NLS-1$
				.filter(p -> {
					if(p.getFileName().toString().startsWith("org.eclipse.osgi_")) { //$NON-NLS-1$
						osgiBundle[0] = p.toUri().toString();
						return false;
					}
					return true;
				})
				.map(p -> getPathRef(p, -1))
				.forEach(runner::addPlatformEntry);
			}
			if(osgiBundle[0] == null) {
				throw new IllegalStateException("Unable to locate org.eclipse.osgi bundle");
			}
			runner.setOsgiBundle(osgiBundle[0]);
			
			if(this.updateSites != null) {
				for(Path updateSite : this.updateSites) {
					Path sitePlugins = updateSite.resolve("plugins"); //$NON-NLS-1$
					if(Files.isDirectory(sitePlugins)) {
						try(Stream<Path> pluginsStream = Files.list(sitePlugins)) {
							pluginsStream.filter(p -> p.getFileName().toString().endsWith(".jar")) //$NON-NLS-1$
								.map(p -> getPathRef(p, -1))
								.forEach(runner::addPlatformEntry);
						}
					}
				}
			}
			
			if(equinoxEnvironmentVars != null) {
				equinoxEnvironmentVars.forEach(runner::addEnvironmentVar);
			}
			
			Collection<Path> jars = initJreJars(notesProgram);
			try {
				Path logFile = runner.getLogFile();
				
				Process proc = runner.start(applicationId);
				watchOutput(proc.getInputStream(), proc);
				watchOutput(proc.getErrorStream(), proc);
				proc.waitFor();
				int exitValue = proc.exitValue();
				switch(exitValue) {
				case 0: // Success
				case 137: // teminated by watchOutput
					break;
				case 1: // also likely terminated - check successFlag
					if(successFlag) {
						break;
					} else {
						throw new RuntimeException(Messages.getString("EquinoxMojo.processExitedWithNonZero", exitValue)); //$NON-NLS-1$
					}
				case 13: // Equinox launch failure - look for log file
					if(Files.isReadable(logFile)) {
						Files.lines(logFile).forEach(log::error);
					}
					// Passthrough intentional
				default:
					throw new RuntimeException(Messages.getString("EquinoxMojo.processExitedWithNonZero", exitValue)); //$NON-NLS-1$
				}
			} finally {
				teardownJreJars(jars);
			}
		} catch (InterruptedException e) {
			// No problem here
		} catch(Throwable e) {
			throw new RuntimeException(Messages.getString("EquinoxMojo.exceptionLaunching"), e); //$NON-NLS-1$
		}
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private Path getDependencyJar(String artifactId) throws MojoExecutionException {
		List<ComponentDependency> dependencies = pluginDescriptor.getDependencies();
		ComponentDependency dep = dependencies.stream()
				.filter(a -> artifactId.equals(a.getArtifactId()))
				.findFirst()
				.orElseThrow(() -> new MojoExecutionException(Messages.getString("EquinoxMojo.couldNotFindDependency", artifactId))); //$NON-NLS-1$
		Artifact art = new DefaultArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), "", dep.getType(), "", new DefaultArtifactHandler()); //$NON-NLS-1$ //$NON-NLS-2$
		art = mavenSession.getLocalRepository().find(art);
		File file = art.getFile();
		Path result;
		if(file.toString().endsWith(".jar")) { //$NON-NLS-1$
			result = file.toPath();
			if(!Files.exists(result)) {
				throw new MojoExecutionException(Messages.getString("EquinoxMojo.dependencyJarDoesNotExist", result)); //$NON-NLS-1$
			}
		} else {
			result = Paths.get(file.toString()+".jar"); //$NON-NLS-1$
		}
		if(!Files.exists(result)) {
			throw new MojoExecutionException(Messages.getString("EquinoxMojo.dependencyJarDoesNotExist", result)); //$NON-NLS-1$
		}
		return result;
	}
	
	private String getDependencyRef(String artifactId, int startLevel) throws MojoExecutionException {
		Path path = getDependencyJar(artifactId);
		return getPathRef(path, startLevel);
	}
	
	private String getPathRef(Path path, int startLevel) {
		if(startLevel < 1) {
			return "reference:" + path.toUri(); //$NON-NLS-1$
		} else {
			return "reference:" + path.toUri() + "@" + startLevel + ":start"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	private Path getJavaBinary(Path notesProgram) throws MojoExecutionException {
		// Look to see if we can find a Notes JVM
		Path jvmBin = notesProgram.resolve("jvm").resolve("bin"); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.isDirectory(jvmBin) && SystemUtils.IS_OS_MAC) {
			// macOS 10.0.1+ embedded JVM
			Path notesApp = getMacNotesAppDir(notesProgram);
			if(notesApp != null) {
				jvmBin = notesApp.resolve("jre").resolve("Contents").resolve("Home").resolve("bin"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
		if(!Files.isDirectory(jvmBin)) {
			throw new RuntimeException("Could not find JVM at " + jvmBin);
		}
			
		
		String javaBinName;
		if(SystemUtils.IS_OS_WINDOWS) {
			javaBinName = "java.exe"; //$NON-NLS-1$
		} else {
			javaBinName = "java"; //$NON-NLS-1$
		}
		Path javaBin = jvmBin.resolve(javaBinName);
		if(!Files.exists(javaBin)) {
			throw new MojoExecutionException(Messages.getString("EquinoxMojo.unableToLocateJava", javaBin)); //$NON-NLS-1$
		}
		return javaBin;
	}
    
    /**
     * Adds JARs that are provided in the "ndext" directory of Domino but aren't IBM/HCL-specific.
     * 
     * @param classpath the classpath collection to add to
     * @throws MojoExecutionException if there is an exception locating the JARs
     * @since 3.0.0
     */
	private void addNdextJars(EquinoxRunner runner) throws MojoExecutionException {
		runner.addClasspathJar(getDependencyJar("guava")); //$NON-NLS-1$
	}
    
	private Collection<Path> initJreJars(Path notesProgram) throws MojoExecutionException, IOException {
    	// On macOS, we'll need to create some symlinks in our active JRE due to the way the ext folder works
    	if(SystemUtils.IS_OS_MAC) {
    		if(log.isDebugEnabled()) {
    			log.debug("Linking environment Jars in macOS Notes JRE");
    		}
    		
    		Path tools = SystemUtils.getJavaHome().toPath().resolve("lib").resolve("tools.jar"); //$NON-NLS-1$ //$NON-NLS-2$
    		if(!Files.exists(tools)) {
    			// Java Home might be a JRE dir - try a level up
    			tools = SystemUtils.getJavaHome().toPath().getParent().resolve("lib").resolve("tools.jar"); //$NON-NLS-1$ //$NON-NLS-2$
    		}
    		if(!Files.exists(tools)) {
    			if(log.isWarnEnabled()) {
    				log.warn("Unable to locate tools.jar in running JVM - if there are downstream problems, this may be the cause (expected " + tools + ")");
    			}
    		}

    		Collection<Path> toLink = new LinkedHashSet<>();
    		EquinoxRunner.addIBMJars(notesProgram, toLink);

    		Collection<Path> result = new LinkedHashSet<>();
    		Path notesApp = getMacNotesAppDir(notesProgram);
    		if(notesApp != null) {
	    		Path destBase = notesApp.resolve("jre").resolve("Contents").resolve("Home").resolve("lib").resolve("ext"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	    		if(!Files.isDirectory(destBase)) {
	    			throw new IllegalStateException("Unable to locate embedded Notes JRE ext directory at " + destBase);
	    		}
	    		if(!Files.isWritable(destBase)) {
	    			throw new IllegalStateException("Unable to write to embedded Notes JRE ext directory at " + destBase);
	    		}
	    		
	    		for(Path jar : toLink) {
	    			Path destJar = destBase.resolve(jar.getFileName());
	    			if(!Files.exists(destJar)) {
	    				Files.copy(jar, destJar);
	    				result.add(destJar);
	    			}
	    		}
	    		
	    		if(Files.exists(tools)) {
		    		Path toolsDest = destBase.getParent().resolve("tools.jar"); //$NON-NLS-1$
		    		if(!Files.exists(toolsDest)) {
		    			Files.copy(tools, toolsDest);
		    			result.add(toolsDest);
		    		}
	    		}
    		}
    		
    		return result;
    	} else {
    		return Collections.emptyList();
    	}
    }
    private void teardownJreJars(Collection<Path> result) throws MojoExecutionException, IOException {
    	if(SystemUtils.IS_OS_MAC) {
    		if(log.isDebugEnabled()) {
    			log.debug("Unlinking environment Jars in macOS Notes JRE");
    		}
    		
    		for(Path jar : result) {
    			Files.deleteIfExists(jar);
    		}
    	}
    }
    
    private static final char[] STOP_SEQUENCE = { '#', 'e', 'n', 'd' };
    
    /**
     * Monitors the given {@link InputStream} and performs two actions:
     * 
     * <ul>
     *   <li>Redirects all output to {@link System#out}</li>
     *   <li>Looks for the character sequence "#end" and ends execution if found</li>
     * </ul>
     * 
     * @param is the {@link InputStream} to monitor
     * @param proc the {@link Process} to kill when "#end" is found
     * @since 3.0.0
     */
    // TODO figure out why this is needed.
    //   The trouble is that the Equinox process sometimes will remain running indefinitely,
    //   even when execution of the IApplication completes successfully.
    private void watchOutput(InputStream is, Process proc) {
    	Executors.newSingleThreadExecutor().submit(() -> {
    		char[] lastFour = new char[4];
    		StringBuilder buffer = new StringBuilder();
    		
    		try {
	    		try(Reader r = new InputStreamReader(is, Charset.forName("UTF-8"))) { //$NON-NLS-1$
	    			int ch;
	    			while((ch = r.read()) != -1) {
	    				if(ch == '\n' || ch == '\r') {
	    					// Flush the buffer
	    					if(buffer.length() > 0) {
		    					if(log.isInfoEnabled()) {
		    						log.info(buffer.toString());
		    					}
		    					buffer.setLength(0);
	    					}
	    				} else {
	    					// Otherwise, enqeue
	    					buffer.append((char)ch);
	    				}
	    				
	    				addChar(lastFour, (char)ch);
	    				if(Arrays.equals(lastFour, STOP_SEQUENCE)) {
	    					proc.destroyForcibly();
	    					successFlag = true;
	    					return;
	    				}
	    			}
	    		}
    		} catch(Exception e) {
    			e.printStackTrace();
    		} finally {
    	    	if(buffer.length() > 0) {
    	    		if(log.isInfoEnabled()) {
    	    			log.info(buffer.toString());
    	    		}
    	    	}
    		}
    	});
    }
    
    /**
     * Shifts all characters in the provided away one slot down and assigns
     * the value of {@code ch} to the last slot.
     * 
     * @param lastFour the array to modify
     * @param ch the character to assign to the end
     * @since 3.0.0
     */
    private static void addChar(char[] lastFour, char ch) {
    	for(int i = 0; i < lastFour.length-1; i++) {
    		lastFour[i] = lastFour[i+1];
    	}
    	lastFour[lastFour.length-1] = ch;
    }
    
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
}
