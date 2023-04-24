package org.openntf.maven.nsfodp.container;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;

public class NSFODPContainer extends GenericContainer<NSFODPContainer> {
	private static class DominoImage extends ImageFromDockerfile {

		public DominoImage(Collection<Path> updateSites, Path packageZip, Collection<Path> cleanup, Log log) {
			super("nsfodp-container:1.0.0", true); //$NON-NLS-1$
			
			// Copy resources to temp files to avoid an exception in a shutdown hook
			try {
				Path tempDir = Files.createTempDirectory(getClass().getName());
				cleanup.add(tempDir);
				
				try(InputStream is = getClass().getResourceAsStream("/container/Dockerfile")) { //$NON-NLS-1$
					Path temp = tempDir.resolve("Dockerfile"); //$NON-NLS-1$
					Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
					withFileFromPath("Dockerfile", temp); //$NON-NLS-1$
				}
				try(InputStream is = getClass().getResourceAsStream("/container/domino-config.json")) { //$NON-NLS-1$
					Path temp = tempDir.resolve("domino-config.json"); //$NON-NLS-1$
					Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
					withFileFromPath("domino-config.json", temp); //$NON-NLS-1$
				}
				try(InputStream is = getClass().getResourceAsStream("/container/container.link")) { //$NON-NLS-1$
					Path temp = tempDir.resolve("container.link"); //$NON-NLS-1$
					Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
					withFileFromPath("container.link", temp); //$NON-NLS-1$
				}
				try(InputStream is = getClass().getResourceAsStream("/container/JavaOptions.txt")) { //$NON-NLS-1$
					Path temp = tempDir.resolve("JavaOptions.txt"); //$NON-NLS-1$
					Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
					withFileFromPath("JavaOptions.txt", temp); //$NON-NLS-1$
				}
				
				if(packageZip != null) {
					withFileFromPath("odp.zip", packageZip); //$NON-NLS-1$
				}
				
				if(updateSites != null) {
					updateSites.stream()
						.map(p -> p.resolve("plugins")) //$NON-NLS-1$
						.filter(p -> Files.isDirectory(p))
						.flatMap(p -> {
							try {
								return Files.list(p);
							} catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						})
						.forEach(plugin -> {
							if(log.isInfoEnabled()) {
								log.info(MessageFormat.format("Adding custom plugin to container: {0}", plugin));
							}
							withFileFromPath("staging/plugins/" + plugin.getFileName().toString(), plugin); //$NON-NLS-1$
						});
				}
				
				// Read the NSF ODP update site
				String version = getMavenVersion();
				Path updateSite = findLocalMavenArtifact("org.openntf.nsfodp", "org.openntf.nsfodp.domino.updatesite", version, "zip"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if(!(Files.isReadable(updateSite) && Files.isRegularFile(updateSite))) {
					throw new IllegalStateException(MessageFormat.format("Unable to read update site: {0}", updateSite));
				}
				try(FileSystem us = NSFODPUtil.openZipPath(updateSite)) {
					Path plugins = us.getPath("plugins"); //$NON-NLS-1$
					Files.list(plugins).forEach(plugin -> {
						// Copy to a temp directory, as Testcontainers assumes it's a local file
						try {
							Path temp = Files.createTempFile(getClass().getName(), ".jar"); //$NON-NLS-1$
							cleanup.add(temp);
							Files.copy(plugin, temp, StandardCopyOption.REPLACE_EXISTING);
	
							if(log.isInfoEnabled()) {
								log.info(MessageFormat.format("Adding NSF ODP plugin to container: {0}", plugin));
							}
							withFileFromPath("staging/plugins/" + plugin.getFileName().toString(), temp); //$NON-NLS-1$
						} catch(IOException e) { 
							throw new UncheckedIOException(e);
						}
					});
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
	
	private static ThreadLocal<Collection<Path>> cleanup = ThreadLocal.withInitial(() -> new ArrayList<>());
	private final Log log;
	private final Path outputDirectory;

	public NSFODPContainer(Collection<Path> updateSites, Path packageZip, Log log, Path outputDirectory) {
		super(new DominoImage(updateSites, packageZip, cleanup.get(), log));
		this.log = log;
		this.outputDirectory = outputDirectory;
		
		addEnv("LANG", "en_US.UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("SetupAutoConfigure", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("SetupAutoConfigureParams", "/local/runner/domino-config.json"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("DOMINO_DOCKER_STDOUT", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
		
		withImagePullPolicy(imageName -> false);
		withExposedPorts(80);
		withStartupTimeout(Duration.ofMinutes(4));
		waitingFor(
			new WaitAllStrategy()
				.withStrategy(new LogMessageWaitStrategy()
					.withRegEx(".*HTTP Server: Started.*") //$NON-NLS-1$
				)
				.withStrategy(new HttpWaitStrategy()
					.forPath("/org.openntf.nsfodp/containerCompiler?mode=ping") //$NON-NLS-1$
				)
			.withStartupTimeout(Duration.ofMinutes(5))
		);
	}
	
	@Override
	public void stop() {
		super.stop();
		
		try {
			NSFODPUtil.deltree(cleanup.get());
		} catch (IOException e) {
			if(log.isWarnEnabled()) {
				log.warn("Encountered exception cleaning temporary files", e);
			}
		}
		cleanup.get().clear();
	}
	
	private static Path findLocalMavenArtifact(String groupId, String artifactId, String version, String type) {
		String mavenRepo = System.getProperty("maven.repo.local"); //$NON-NLS-1$
		if (StringUtil.isEmpty(mavenRepo)) {
			mavenRepo = PathUtil.concat(System.getProperty("user.home"), ".m2", File.separatorChar); //$NON-NLS-1$ //$NON-NLS-2$
			mavenRepo = PathUtil.concat(mavenRepo, "repository", File.separatorChar); //$NON-NLS-1$
		}
		String groupPath = groupId.replace('.', File.separatorChar);
		Path localPath = Paths.get(mavenRepo).resolve(groupPath).resolve(artifactId).resolve(version);
		String fileName = StringUtil.format("{0}-{1}.{2}", artifactId, version, type); //$NON-NLS-1$
		Path localFile = localPath.resolve(fileName);
		
		if(!Files.isRegularFile(localFile)) {
			throw new RuntimeException("Unable to locate Maven artifact: " + localFile);
		}

		return localFile;
	}
	
	public static String getMavenVersion() {
		// Find the current build version
		Properties props = new Properties();
		try (InputStream is = NSFODPContainer.class.getResourceAsStream("/scm.properties")) { //$NON-NLS-1$
			props.load(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		String version = props.getProperty("git.build.version", null); //$NON-NLS-1$
		if(StringUtil.isEmpty(version)) {
			throw new RuntimeException("Unable to determine artifact version from scm.properties");
		}
		return version;
	}
	
	@Override
	protected void containerIsStopping(InspectContainerResponse containerInfo) {
		super.containerIsStopping(containerInfo);
		
		try {
			// If we can see the target dir, copy log files
			if(Files.isDirectory(this.outputDirectory)) {
				this.execInContainer("tar", "-czvf", "/tmp/IBM_TECHNICAL_SUPPORT.tar.gz", "/local/notesdata/IBM_TECHNICAL_SUPPORT");
				this.copyFileFromContainer("/tmp/IBM_TECHNICAL_SUPPORT.tar.gz", this.outputDirectory.resolve("IBM_TECHNICAL_SUPPORT.tar.gz").toString());
					
				this.execInContainer("tar", "-czvf", "/tmp/workspace-logs.tar.gz", "/local/notesdata/domino/workspace/logs");
				this.copyFileFromContainer("/tmp/workspace-logs.tar.gz", this.outputDirectory.resolve("workspace-logs.tar.gz").toString());
			}
		} catch(IOException | UnsupportedOperationException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
