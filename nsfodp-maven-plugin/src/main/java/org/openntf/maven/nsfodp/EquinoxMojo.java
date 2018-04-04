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
		
		Path equinox = getDependencyJar("org.eclipse.equinox.launcher");
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

			Path osgi = getDependencyJar("org.eclipse.osgi");
			List<Path> platform = new ArrayList<>(Arrays.asList(
				getDependencyJar("org.openntf.nsfodp.commons"),
				getDependencyJar("org.openntf.nsfodp.compiler"),
				getDependencyJar("org.openntf.nsfodp.compiler.servlet"),
				getDependencyJar("org.openntf.nsfodp.deployment"),
				getDependencyJar("org.openntf.nsfodp.deployment.servlet"),
				getDependencyJar("org.openntf.nsfodp.cli"),
				getDependencyJar("org.eclipse.core.runtime"),
				getDependencyJar("org.eclipse.equinox.http.jetty"),
				getDependencyJar("org.eclipse.equinox.http.registry"),
				getDependencyJar("org.eclipse.equinox.http.servlet"),
				getDependencyJar("com.ibm.xsp.extlibx.bazaar"),
				getDependencyJar("com.ibm.xsp.extlibx.bazaar.interpreter"),
				getDependencyJar("jetty-http"),
				getDependencyJar("jetty-util"),
				getDependencyJar("jetty-io"),
				getDependencyJar("jetty-server"),
				getDependencyJar("jetty-servlet"),
				getDependencyJar("jetty-jmx"),
				getDependencyJar("jetty-security"),
				getDependencyJar("slf4j-api"),
				getDependencyJar("slf4j-simple"),
				getDependencyJar("javax.servlet-api"),
				createJempowerShim(notesProgram),
				osgi
			));
			
			List<String> skipBundles = Arrays.asList(
				"org.eclipse.core.runtime",
				"org.eclipse.osgi",
				"org.eclipse.equinox.http.servlet",
				"org.eclipse.equinox.http.registry",
				"javax.servlet",
				"com.ibm.pvc.webcontainer",
				"com.ibm.rcp.spellcheck.remote",
				"com.ibm.rcp.spellcheck.webapp"
			);
			
			Path notesPlatform = Paths.get(this.notesPlatform.toURI());
			if(!Files.exists(notesPlatform)) {
				throw new MojoExecutionException("Notes platform does not exist: " + notesPlatform);
			}
			Path notesPlugins = notesPlatform.resolve("plugins");
			if(!Files.exists(notesPlugins)) {
				throw new MojoExecutionException("Notes plugins directory does not exist: " + notesPlugins);
			}
			Files.list(notesPlugins)
				.filter(p -> p.getFileName().toString().endsWith(".jar"))
				.filter(p -> !skipBundles.stream().anyMatch(b -> p.getFileName().toString().startsWith(b+"_")))
				.forEach(platform::add);
			
			Path target = Paths.get(project.getBuild().getDirectory());
			Path framework = target.resolve("nsfodpequinox");
			if(log.isDebugEnabled()) {
				log.debug("Creating OSGi framework: " + framework);
			}
			Files.createDirectories(framework);
			
			Path configuration = framework.resolve("configuration");
			Files.createDirectories(configuration);
			Path configIni = configuration.resolve("config.ini");
			Properties config = new Properties();
			config.put("osgi.bundles.defaultStartLevel", "4");
			config.put("osgi.bundles",
					platform.stream()
					.map(path ->"reference:" + path.toUri())
					.collect(Collectors.joining(","))
			);
			config.put("eclipse.application", "org.openntf.nsfodp.cli.CLIApplication");
			config.put("osgi.configuration.cascaded", "false");
			config.put("osgi.install.area", framework.toUri().toString());
			config.put("osgi.framework", osgi.toUri().toString());
			config.put("eclipse.log.level", "ERROR");
			try(OutputStream os = Files.newOutputStream(configIni)) {
				config.store(os, "NSF ODP OSGi Configuration");
			}
			
			Path plugins = framework.resolve("plugins");
			Files.createDirectories(plugins);
			
			List<String> command = new ArrayList<>();
			command.add(javaBin.toAbsolutePath().toString());
			command.add("-Dosgi.frameworkParentClassloader=boot");
			command.add("-Dorg.openntf.nsfodp.allowAnonymous=true");
			command.add("org.eclipse.core.launcher.Main");
			command.add("-framwork");
			command.add(framework.toAbsolutePath().toString());
			command.add("-configuration");
			command.add(configuration.toAbsolutePath().toString());
			
			if(log.isDebugEnabled()) {
				log.debug("Launching Equinox with command " + command.stream().collect(Collectors.joining(" ")));
			}
			
			ProcessBuilder builder = new ProcessBuilder()
					.command(command)
					.redirectOutput(Redirect.INHERIT)
					.redirectInput(Redirect.INHERIT);
			builder.environment().put("Notes_ExecDirectory", notesProgram.toAbsolutePath().toString());
			builder.environment().put("PATH", notesProgram.toAbsolutePath().toString());
			builder.environment().put("LD_LIBRARY_PATH", notesProgram.toAbsolutePath().toString());
			builder.environment().put("DYLD_LIBRARY_PATH", notesProgram.toAbsolutePath().toString());
			builder.environment().put("CLASSPATH",
					classpath.stream()
					.map(path -> path.toString())
					.collect(Collectors.joining(":"))
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
		Artifact art = new DefaultArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), "", dep.getType(), "", new DefaultArtifactHandler());
		art = mavenSession.getLocalRepository().find(art);
		File file = art.getFile();
		Path result;
		if(file.toString().endsWith(".jar")) {
			result = file.toPath();
		} else {
			result = Paths.get(file.toString()+".jar");
		}
		if(!Files.exists(result)) {
			throw new MojoExecutionException("Dependency jar does not exist: " + result);
		}
		return result;
	}
	
	private static Path getJavaBinary() throws MojoExecutionException {
		String javaBinName;
		if(SystemUtils.IS_OS_WINDOWS) {
			javaBinName = "java.exe";
		} else {
			javaBinName = "java";
		}
		Path javaBin = SystemUtils.getJavaHome().toPath().resolve("bin").resolve(javaBinName);
		if(!Files.exists(javaBin)) {
			throw new MojoExecutionException("Unable to locate Java binary at path " + javaBin);
		}
		return javaBin;
	}
	
	public static Path createJempowerShim(Path notesBin) throws IOException {
		Path njempcl = notesBin.resolve("jvm").resolve("lib").resolve("ext").resolve("njempcl.jar");
		Path tempBundle = Files.createTempFile("njempcl", ".jar");
		try(OutputStream os = Files.newOutputStream(tempBundle)) {
			try(JarOutputStream jos = new JarOutputStream(os)) {
				JarEntry entry = new JarEntry("META-INF/MANIFEST.MF");
				jos.putNextEntry(entry);
				try(InputStream is = EquinoxMojo.class.getResourceAsStream("/res/COM.ibm.JEmpower/META-INF/MANIFEST.MF")) {
					copyStream(is, jos, 8192);
				}
				JarEntry njempclEntry = new JarEntry("lib/njempcl.jar");
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
