/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.maven.nsfodp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.apache.maven.plugin.descriptor.PluginDescriptor;

@Mojo(name="equinox")
public class EquinoxMojo extends AbstractMojo {
	
	@Parameter(defaultValue="${project}", readonly=true)
	private MavenProject project;
	
	@Parameter(defaultValue="${plugin}", readonly=true)
	private PluginDescriptor pluginDescriptor;
	
	@Parameter(defaultValue="${session}", readonly=true)
	private MavenSession mavenSession;
	
	@Parameter(property="notes-platform")
	private URL notesPlatform;
	
	@Parameter(property="notes-program")
	private String notesProgram;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();
		
		Path equinox = getDependencyJar("org.eclipse.equinox.launcher"); //$NON-NLS-1$
		if(log.isDebugEnabled()) {
			log.debug("Using Equinox launcher: " + equinox);
		}
		
		Path javaBin = getJavaBinary();
		try {
			List<Path> classpath = new ArrayList<>(Arrays.asList(
				equinox
			));
			
			Path notesProgram = Paths.get(this.notesProgram);
			if(!Files.exists(notesProgram)) {
				throw new MojoExecutionException("Notes program directory does not exist: " + notesProgram);
			}

			Path osgi = getDependencyJar("org.eclipse.osgi"); //$NON-NLS-1$
			List<Path> platform = new ArrayList<>(Arrays.asList(
				getDependencyJar("org.openntf.nsfodp.commons"), //$NON-NLS-1$
				getDependencyJar("org.openntf.nsfodp.compiler"), //$NON-NLS-1$
				getDependencyJar("org.openntf.nsfodp.compiler.servlet"), //$NON-NLS-1$
				getDependencyJar("org.openntf.nsfodp.deployment"), //$NON-NLS-1$
				getDependencyJar("org.openntf.nsfodp.deployment.servlet"), //$NON-NLS-1$
				getDependencyJar("org.openntf.nsfodp.cli"), //$NON-NLS-1$
				getDependencyJar("org.eclipse.core.runtime"), //$NON-NLS-1$
				getDependencyJar("org.eclipse.equinox.http.jetty"), //$NON-NLS-1$
				getDependencyJar("org.eclipse.equinox.http.registry"), //$NON-NLS-1$
				getDependencyJar("org.eclipse.equinox.http.servlet"), //$NON-NLS-1$
				getDependencyJar("com.ibm.xsp.extlibx.bazaar"), //$NON-NLS-1$
				getDependencyJar("com.ibm.xsp.extlibx.bazaar.interpreter"), //$NON-NLS-1$
				getDependencyJar("jetty-http"), //$NON-NLS-1$
				getDependencyJar("jetty-util"), //$NON-NLS-1$
				getDependencyJar("jetty-io"), //$NON-NLS-1$
				getDependencyJar("jetty-server"), //$NON-NLS-1$
				getDependencyJar("jetty-servlet"), //$NON-NLS-1$
				getDependencyJar("jetty-jmx"), //$NON-NLS-1$
				getDependencyJar("jetty-security"), //$NON-NLS-1$
				getDependencyJar("slf4j-api"), //$NON-NLS-1$
				getDependencyJar("slf4j-simple"), //$NON-NLS-1$
				getDependencyJar("javax.servlet-api"), //$NON-NLS-1$
				createJempowerShim(notesProgram),
				osgi
			));
			
			List<String> skipBundles = Arrays.asList(
				"org.eclipse.core.runtime", //$NON-NLS-1$
				"org.eclipse.osgi", //$NON-NLS-1$
				"org.eclipse.equinox.http.servlet", //$NON-NLS-1$
				"org.eclipse.equinox.http.registry", //$NON-NLS-1$
				"javax.servlet", //$NON-NLS-1$
				"com.ibm.pvc.webcontainer", //$NON-NLS-1$
				"com.ibm.rcp.spellcheck.remote", //$NON-NLS-1$
				"com.ibm.rcp.spellcheck.webapp" //$NON-NLS-1$
			);
			
			Path notesPlatform = Paths.get(this.notesPlatform.toURI());
			if(!Files.exists(notesPlatform)) {
				throw new MojoExecutionException("Notes platform does not exist: " + notesPlatform);
			}
			Path notesPlugins = notesPlatform.resolve("plugins"); //$NON-NLS-1$
			if(!Files.exists(notesPlugins)) {
				throw new MojoExecutionException("Notes plugins directory does not exist: " + notesPlugins);
			}
			Files.list(notesPlugins)
				.filter(p -> p.getFileName().toString().endsWith(".jar")) //$NON-NLS-1$
				.filter(p -> !skipBundles.stream().anyMatch(b -> p.getFileName().toString().startsWith(b+"_"))) //$NON-NLS-1$
				.forEach(platform::add);
			
			Path target = Paths.get(project.getBuild().getDirectory());
			Path framework = target.resolve("nsfodpequinox"); //$NON-NLS-1$
			if(log.isDebugEnabled()) {
				log.debug("Creating OSGi framework: " + framework);
			}
			Files.createDirectories(framework);
			
			Path configuration = framework.resolve("configuration"); //$NON-NLS-1$
			Files.createDirectories(configuration);
			Path configIni = configuration.resolve("config.ini"); //$NON-NLS-1$
			Properties config = new Properties();
			config.put("osgi.bundles.defaultStartLevel", "4"); //$NON-NLS-1$ //$NON-NLS-2$
			config.put("osgi.bundles", //$NON-NLS-1$
					platform.stream()
					.map(path ->"reference:" + path.toUri()) //$NON-NLS-1$
					.collect(Collectors.joining(",")) //$NON-NLS-1$
			);
			config.put("eclipse.application", "org.openntf.nsfodp.cli.CLIApplication"); //$NON-NLS-1$ //$NON-NLS-2$
			config.put("osgi.configuration.cascaded", "false"); //$NON-NLS-1$ //$NON-NLS-2$
			config.put("osgi.install.area", framework.toUri().toString()); //$NON-NLS-1$
			config.put("osgi.framework", osgi.toUri().toString()); //$NON-NLS-1$
			config.put("eclipse.log.level", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
			try(OutputStream os = Files.newOutputStream(configIni)) {
				config.store(os, "NSF ODP OSGi Configuration"); //$NON-NLS-1$
			}
			
			Path plugins = framework.resolve("plugins"); //$NON-NLS-1$
			Files.createDirectories(plugins);
			
			List<String> command = new ArrayList<>();
			command.add(javaBin.toAbsolutePath().toString());
			command.add("-Dosgi.frameworkParentClassloader=boot"); //$NON-NLS-1$
			command.add("-Dorg.openntf.nsfodp.allowAnonymous=true"); //$NON-NLS-1$
			command.add("org.eclipse.core.launcher.Main"); //$NON-NLS-1$
			command.add("-framwork"); //$NON-NLS-1$
			command.add(framework.toAbsolutePath().toString());
			command.add("-configuration"); //$NON-NLS-1$
			command.add(configuration.toAbsolutePath().toString());
			
			if(log.isDebugEnabled()) {
				log.debug("Launching Equinox with command " + command.stream().collect(Collectors.joining(" "))); //$NON-NLS-2$
			}
			
			ProcessBuilder builder = new ProcessBuilder()
					.command(command)
					.redirectOutput(Redirect.INHERIT)
					.redirectInput(Redirect.INHERIT);
			builder.environment().put("Notes_ExecDirectory", notesProgram.toAbsolutePath().toString()); //$NON-NLS-1$
			builder.environment().put("PATH", notesProgram.toAbsolutePath().toString()); //$NON-NLS-1$
			builder.environment().put("LD_LIBRARY_PATH", notesProgram.toAbsolutePath().toString()); //$NON-NLS-1$
			builder.environment().put("DYLD_LIBRARY_PATH", notesProgram.toAbsolutePath().toString()); //$NON-NLS-1$
			builder.environment().put("CLASSPATH", //$NON-NLS-1$
					classpath.stream()
					.map(path -> path.toString())
					.collect(Collectors.joining(":")) //$NON-NLS-1$
			);
			
			Process proc = builder.start();
			proc.waitFor();
		} catch(IOException | URISyntaxException e) {
			throw new MojoExecutionException("Encountered exception while launching application", e);
		} catch (InterruptedException e) {
			
		}
	}

	private Path getDependencyJar(String artifactId) throws MojoExecutionException {
		List<ComponentDependency> dependencies = pluginDescriptor.getDependencies();
		ComponentDependency dep = dependencies.stream()
				.filter(a -> artifactId.equals(a.getArtifactId()))
				.findFirst()
				.orElseThrow(() -> new MojoExecutionException("Could not find dependency for " + artifactId));
		Artifact art = new DefaultArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), "", dep.getType(), "", new DefaultArtifactHandler()); //$NON-NLS-1$ //$NON-NLS-2$
		art = mavenSession.getLocalRepository().find(art);
		File file = art.getFile();
		Path result;
		if(file.toString().endsWith(".jar")) { //$NON-NLS-1$
			result = file.toPath();
		} else {
			result = Paths.get(file.toString()+".jar"); //$NON-NLS-1$
		}
		if(!Files.exists(result)) {
			throw new MojoExecutionException("Dependency jar does not exist: " + result);
		}
		return result;
	}
	
	private static Path getJavaBinary() throws MojoExecutionException {
		String javaBinName;
		if(SystemUtils.IS_OS_WINDOWS) {
			javaBinName = "java.exe"; //$NON-NLS-1$
		} else {
			javaBinName = "java"; //$NON-NLS-1$
		}
		Path javaBin = SystemUtils.getJavaHome().toPath().resolve("bin").resolve(javaBinName); //$NON-NLS-1$
		if(!Files.exists(javaBin)) {
			throw new MojoExecutionException("Unable to locate Java binary at path " + javaBin);
		}
		return javaBin;
	}
	
	public static Path createJempowerShim(Path notesBin) throws IOException {
		Path njempcl = notesBin.resolve("jvm").resolve("lib").resolve("ext").resolve("njempcl.jar"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		Path tempBundle = Files.createTempFile("njempcl", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
		try(OutputStream os = Files.newOutputStream(tempBundle)) {
			try(JarOutputStream jos = new JarOutputStream(os)) {
				JarEntry entry = new JarEntry("META-INF/MANIFEST.MF"); //$NON-NLS-1$
				jos.putNextEntry(entry);
				try(InputStream is = EquinoxMojo.class.getResourceAsStream("/res/COM.ibm.JEmpower/META-INF/MANIFEST.MF")) { //$NON-NLS-1$
					copyStream(is, jos, 8192);
				}
				JarEntry njempclEntry = new JarEntry("lib/njempcl.jar"); //$NON-NLS-1$
				jos.putNextEntry(njempclEntry);
				try(InputStream is = Files.newInputStream(njempcl)) {
					copyStream(is, jos, 8192);
				}
			}
		}
		return tempBundle;
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
}
