/**
 * Copyright © 2018-2019 Jesse Gallagher
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openntf.maven.nsfodp.equinox.EquinoxCompiler;
import org.openntf.maven.nsfodp.util.ODPMojoUtil;
import org.openntf.maven.nsfodp.util.ResponseUtil;
import org.openntf.nsfodp.commons.NSFODPConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Goal which compiles an on-disk project.
 */
@Mojo(name="compile", defaultPhase=LifecyclePhase.COMPILE)
public class CompileODPMojo extends AbstractEquinoxMojo {
	
	public static final String CLASSIFIER_NSF = "nsf"; //$NON-NLS-1$
	public static final String SERVLET_PATH = "/org.openntf.nsfodp/compiler"; //$NON-NLS-1$
	
	/**
	 * Location of the generated NSF.
	 */
	@Parameter(defaultValue="${project.build.directory}", property="outputDir", required=true)
	private File outputDirectory;
	/**
	 * File name of the generated NSF.
	 */
	@Parameter(defaultValue="${project.build.finalName}.nsf", required=true)
	private String outputFileName;
	/**
	 * Location of the ODP directory.
	 */
	@Parameter(defaultValue="odp", required=true)
	private File odpDirectory;
	/**
	 * The server id in settings.xml to use when authenticating with the compiler server, or
	 * <code>null</code> to authenticate as anonymous.
	 */
	@Parameter(property="nsfodp.compiler.server", required=false)
	private String compilerServer;
	/**
	 * The base URL of the ODP compiler server, e.g. "http://my.server".
	 * 
	 * <p>This property is ignored if {@code notesProgram} is set.</p>
	 */
	@Parameter(property="nsfodp.compiler.serverUrl", required=false)
	private URL compilerServerUrl;
	/**
	 * Whether or not to trust self-signed SSL certificates.
	 */
	@Parameter(property="nsfodp.compiler.serverTrustSelfSignedSsl", required=false)
	private boolean compilerServerTrustSelfSignedSsl;
	
	/**
	 * An update site whose contents to use when building the ODP.
	 * 
	 * @deprecated use {@link #updateSites} instead
	 */
	@Parameter(required=false)
	@Deprecated
	private File updateSite;
	
	/**
	 * Any update sites whose contents to use when building the ODP.
	 * 
	 * <p>Overrides {@link #updateSite} if both are specified.</p>
	 */
	@Parameter(required=false)
	private File[] updateSites;
	
	/**
	 * The compiler level to target, e.g. "1.6", "1.8", "10", etc.
	 * 
	 * <p>If unspecified, this defaults to the server's JRE version.</p>
	 */
	@Parameter(property="nsfodp.compiler.compilerLevel", required=false)
	private String compilerLevel;
	
	/**
	 * Whether or not to append a timestamp to the generated NSF's title. Defaults to
	 * {@value}.
	 */
	@Parameter(required=false)
	private boolean appendTimestampToTitle = false;
	
	/**
	 * A name to set in the database for use as a master template.
	 * 
	 * <p>Note: this is the name
	 * used by this database when it is a template for others, not the name of a template
	 * to inherit from.</p>
	 */
	@Parameter(required=false)
	private String templateName;
	
	/**
	 * Whether to set production options in the xsp.properties file. Currently, this sets:
	 * 
	 * <ul>
	 * 	<li><code>xsp.resources.aggregate=true</code></li>
	 * 	<li><code>xsp.client.resources.uncompressed=false</code></li>
	 * </ul>
	 */
	@Parameter(required=false)
	private boolean setProductionXspOptions = false;
	
	/**
	 * Any additional jars to include on the compilation classpath.
	 * 
	 * @since 2.0.0
	 */
	@Parameter(required=false)
	private File[] classpathJars;
	
	private Log log;

	public void execute() throws MojoExecutionException {
		log = getLog();
		
		if(notesProgram == null && compilerServerUrl == null) {
			throw new IllegalArgumentException(Messages.getString("CompileODPMojo.programAndUrlEmpty")); //$NON-NLS-1$
		}
		if(compilerServerUrl == null && requireServerExecution) {
			throw new IllegalArgumentException(Messages.getString("CompileODPMojo.requireServerNoServer")); //$NON-NLS-1$
		}
		
		Path outputDirectory = Objects.requireNonNull(this.outputDirectory, "outputDirectory cannot be null").toPath(); //$NON-NLS-1$
		
		Path odpDirectory = Objects.requireNonNull(this.odpDirectory, "odpDirectory cannot be null").toPath(); //$NON-NLS-1$
		if(!Files.exists(odpDirectory)) {
			throw new IllegalArgumentException(Messages.getString("CompileODPMojo.odpDirDoesNotExist", odpDirectory.toAbsolutePath())); //$NON-NLS-1$
		}
		if(!Files.isDirectory(odpDirectory)) {
			throw new IllegalArgumentException(Messages.getString("CompileODPMojo.odpDirNotADir", odpDirectory.toAbsolutePath())); //$NON-NLS-1$
		}
		
		List<Path> updateSites = collectUpdateSites();
		
		String outputFileName = Objects.requireNonNull(this.outputFileName, "outputFileName cannot be null"); //$NON-NLS-1$
		if(outputFileName.isEmpty()) {
			throw new IllegalArgumentException(Messages.getString("CompileODPMojo.outputFileNameEmpty")); //$NON-NLS-1$
		}

		Path outputFile = outputDirectory.resolve(outputFileName);
		boolean needsCompile = true;
		if(Files.exists(outputFile)) {
			// Check to see if we need compilation
			try {
				FileTime mod = Files.getLastModifiedTime(outputFile);
				needsCompile = Files.find(odpDirectory, Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile() && attr.lastModifiedTime().compareTo(mod) > 0).count() > 0;
			} catch(IOException e) {
				throw new MojoExecutionException(Messages.getString("CompileODPMojo.exceptionCheckingFiles"), e); //$NON-NLS-1$
			}
		}
		
		if(needsCompile) {
			if(log.isInfoEnabled()) {
				log.info(Messages.getString("CompileODPMojo.compilingOdp")); //$NON-NLS-1$
			}
			try {
				if(!Files.exists(outputDirectory)) {
					Files.createDirectories(outputDirectory);
				}
				
				if(isRunLocally()) {
					compileOdpLocal(odpDirectory, updateSites, outputFile);
				} else {
					Path odpZip = zipDirectory(odpDirectory);
					List<Path> updateSiteZips = null;
					if(updateSites != null && !updateSites.isEmpty()) {
						updateSiteZips = updateSites.stream()
							.map(this::zipDirectory)
							.collect(Collectors.toList());
					}
					
					Path packageZip = createPackage(odpZip, updateSiteZips);
					Path result = compileOdpOnServer(packageZip);
					Files.move(result, outputFile, StandardCopyOption.REPLACE_EXISTING);
				}
				
				if(log.isInfoEnabled()) {
					log.info(Messages.getString("CompileODPMojo.generatedNsf", outputFile)); //$NON-NLS-1$
				}
			} catch(MojoExecutionException e) {
				throw e;
			} catch(Throwable t) {
				throw new MojoExecutionException(Messages.getString("CompileODPMojo.exceptionCompiling"), t); //$NON-NLS-1$
			}
		} else {
			if(log.isInfoEnabled()) {
				log.info(Messages.getString("CompileODPMojo.skippingCompilation")); //$NON-NLS-1$
			}
		}
		
		// Set the project artifact
		Artifact artifact = project.getArtifact();
		artifact.setFile(outputFile.toFile());
	}
	
	// *******************************************************************************
	// * Local compilation
	// *******************************************************************************
	
	private void compileOdpLocal(Path odpDirectory, List<Path> updateSites, Path outputFile) throws IOException {
		EquinoxCompiler compiler = new EquinoxCompiler(pluginDescriptor, mavenSession, project, getLog(), notesProgram.toPath(), notesPlatform);
		List<Path> classpathJars;
		if(this.classpathJars == null) {
			classpathJars = Collections.emptyList();
		} else {
			classpathJars = Arrays.stream(this.classpathJars).map(File::toPath).collect(Collectors.toList());
		}
		compiler.compileOdp(odpDirectory, updateSites, classpathJars, outputFile, compilerLevel, appendTimestampToTitle, templateName, setProductionXspOptions);
	}
	
	// *******************************************************************************
	// * Server-based compilation
	// *******************************************************************************
	
	private Path createPackage(Path odpZip, List<Path> updateSiteZips) throws IOException {
		if(log.isDebugEnabled()) {
			log.debug(Messages.getString("CompileODPMojo.creatingPackage") + odpZip + ", updateSiteZips=" + updateSiteZips); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		Path packageZip = Files.createTempFile("odpcompiler-package", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
		packageZip.toFile().deleteOnExit();
		try(OutputStream fos = Files.newOutputStream(packageZip)) {
			try(ZipOutputStream zos = new ZipOutputStream(fos)) {
				zos.setLevel(Deflater.BEST_COMPRESSION);
				ZipEntry entry = new ZipEntry("odp.zip"); //$NON-NLS-1$
				zos.putNextEntry(entry);
				Files.copy(odpZip, zos);
				
				if(updateSiteZips != null && !updateSiteZips.isEmpty()) {
					for(int i = 0; i < updateSiteZips.size(); i++) {
						Path updateSiteZip = updateSiteZips.get(i);
						entry = new ZipEntry("site" + i + ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
						zos.putNextEntry(entry);
						Files.copy(updateSiteZip, zos);
					}
				}
			}
		}
		return packageZip;
	}
	
	private Path compileOdpOnServer(Path packageZip) throws IOException, URISyntaxException, MojoExecutionException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		URL compilerServerUrl = Objects.requireNonNull(this.compilerServerUrl);
		if(log.isDebugEnabled()) {
			log.debug(Messages.getString("CompileODPMojo.usingServerUrl", compilerServerUrl)); //$NON-NLS-1$
		}
		
		HttpClientBuilder httpBuilder = HttpClients.custom();
		if(this.compilerServerTrustSelfSignedSsl) {
			SSLContextBuilder sslBuilder = new SSLContextBuilder();
			sslBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslBuilder.build(), null, null, NoopHostnameVerifier.INSTANCE);
			httpBuilder.setSSLSocketFactory(sslsf);
		}
		
		try(CloseableHttpClient client = httpBuilder.build()) {
			URI servlet = compilerServerUrl.toURI().resolve(SERVLET_PATH);
			if(log.isInfoEnabled()) {
				log.info(Messages.getString("CompileODPMojo.compilingWithServer", servlet)); //$NON-NLS-1$
			}
			HttpPost post = new HttpPost(servlet);
			post.addHeader("Content-Type", "application/zip"); //$NON-NLS-1$ //$NON-NLS-2$
			
			ODPMojoUtil.addAuthenticationInfo(this.wagonManager, this.compilerServer, post, this.log);
			
			if(this.compilerLevel != null && !this.compilerLevel.isEmpty()) {
				post.addHeader(NSFODPConstants.HEADER_COMPILER_LEVEL, this.compilerLevel);
			}
			post.addHeader(NSFODPConstants.HEADER_APPEND_TIMESTAMP, String.valueOf(this.appendTimestampToTitle));
			if(this.templateName != null && !this.templateName.isEmpty()) {
				post.addHeader(NSFODPConstants.HEADER_TEMPLATE_NAME, this.templateName);
				post.addHeader(NSFODPConstants.HEADER_TEMPLATE_VERSION, ODPMojoUtil.calculateVersion(project));
			}
			post.addHeader(NSFODPConstants.HEADER_SET_PRODUCTION_XSP, String.valueOf(this.setProductionXspOptions));
			
			FileEntity fileEntity = new FileEntity(packageZip.toFile());
			post.setEntity(fileEntity);
			
			HttpResponse res = client.execute(post);
			HttpEntity responseEntity = ResponseUtil.checkResponse(log, res);
 			
			try(InputStream is = responseEntity.getContent()) {
				ResponseUtil.monitorResponse(log, is);
				
				// Now that we're here, the rest will be the compiler output
				Path result = Files.createTempFile("odpcompiler-output", ".nsf"); //$NON-NLS-1$ //$NON-NLS-2$
				try(InputStream gzis = new GZIPInputStream(is)) {
					Files.copy(gzis, result, StandardCopyOption.REPLACE_EXISTING);
				}
				return result;
			}
		}
	}
	
	private Path zipDirectory(Path path) {
		if(log.isDebugEnabled()) {
			log.debug(Messages.getString("CompileODPMojo.zippingPath", path.toString())); //$NON-NLS-1$
		}
		
		try {
			Path result = Files.createTempFile("odpcompiler-dir", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			result.toFile().deleteOnExit();
			
			try(OutputStream fos = Files.newOutputStream(result)) {
				try(ZipOutputStream zos = new ZipOutputStream(fos)) {
					zos.setLevel(Deflater.BEST_COMPRESSION);
					Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if(attrs.isRegularFile()) {
								Path relativePath = path.relativize(file);
								String unixPath = StreamSupport.stream(relativePath.spliterator(), false).map(String::valueOf).collect(Collectors.joining("/")); //$NON-NLS-1$
								ZipEntry entry = new ZipEntry(unixPath);
								zos.putNextEntry(entry);
								Files.copy(file, zos);
							}
							return FileVisitResult.CONTINUE;
						}
					});
				}
			}
			
			return result;
		} catch(IOException e) {
			throw new RuntimeException(Messages.getString("CompileODPMojo.exceptionCompressingDir", path), e); //$NON-NLS-1$
		}
	}
	
	// *******************************************************************************
	// * Misc. internal utilities
	// *******************************************************************************
	private List<Path> collectUpdateSites() {
		List<Path> result = new ArrayList<>();
		
		// The new setting should override the old setting, so check that first
		if(this.updateSites != null && this.updateSites.length != 0) {
			result = Stream.of(this.updateSites)
					.filter(Objects::nonNull)
					.map(File::toPath)
					.collect(Collectors.toList());
		} else if(this.updateSite != null) {
			result = Collections.singletonList(this.updateSite.toPath());
		}
		
		for(Path updateSite : result) {
			if(updateSite != null) {
				if(!Files.exists(updateSite)) {
					throw new IllegalArgumentException(Messages.getString("CompileODPMojo.usDirDoesNotExist", updateSite.toAbsolutePath())); //$NON-NLS-1$
				}
				if(!Files.isDirectory(updateSite)) {
					throw new IllegalArgumentException(Messages.getString("CompileODPMojo.usDirNotADir", updateSite.toAbsolutePath())); //$NON-NLS-1$
				}
			}
		}
		
		
		return result;
	}
}
