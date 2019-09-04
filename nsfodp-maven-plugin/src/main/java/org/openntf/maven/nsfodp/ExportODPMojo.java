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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openntf.maven.nsfodp.equinox.EquinoxExporter;
import org.openntf.maven.nsfodp.util.ODPMojoUtil;
import org.openntf.maven.nsfodp.util.ResponseUtil;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.NSFODPUtil;

/**
 * Goal which generates an "odp" folder (replacing any existing in the directory or project)
 * from an NSF path.
 * 
 * @author Jesse Gallagher
 * @since 1.4.0
 */
@Mojo(name="export-odp", requiresProject=false)
public class ExportODPMojo extends AbstractEquinoxMojo {
	
	public static final String SERVLET_PATH = "/org.openntf.nsfodp/exporter"; //$NON-NLS-1$
	
	/**
	 * Location of the ODP directory.
	 */
	@Parameter(property="nsfodp.exporter.odpDirectory", defaultValue="odp", required=true)
	private File odpDirectory;
	
	/**
	 * The server id in settings.xml to use when authenticating with the exporter server, or
	 * <code>null</code> to authenticate as anonymous.
	 */
	@Parameter(property="nsfodp.exporter.server", required=false)
	private String exporterServer;
	/**
	 * The base URL of the ODP exporter server, e.g. "http://my.server".
	 */
	@Parameter(property="nsfodp.exporter.serverUrl", required=true)
	private URL exporterServerUrl;
	/**
	 * Whether or not to trust self-signed SSL certificates.
	 */
	@Parameter(property="nsfodp.exporter.serverTrustSelfSignedSsl", required=false)
	private boolean exporterServerTrustSelfSignedSsl;
	
	/**
	 * The database path for the remote server to export.
	 */
	@Parameter(property="databasePath", required=false)
	private String databasePath;
	
	@Parameter(property="file", required=false)
	private File file;
	
	/**
	 * Whether or not to run the DXL through a Swiper filter. Defaults to <code>true</code>.
	 */
	@Parameter(property="nsfodp.exporter.swiperFilter", required=false)
	private boolean swiperFilter = true;
	/**
	 * Whether or not to use "binary" DXL format. Defaults to <code>false</code>.
	 */
	@Parameter(property="nsfodp.exporter.binaryDxl", required=false)
	private boolean binaryDxl = false;

	/**
	 * Whether or not to export rich text items as Base64'd binary data. Defaults to <code>true</code>.
	 */
	@Parameter(property="nsfodp.exporter.richTextAsItemData", required=false)
	private boolean richTextAsItemData = true;

	private Log log;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();
		
		Path odpDir;
		if(odpDirectory.isAbsolute()) {
			odpDir = odpDirectory.toPath();
		} else if("standalone-pom".equals(project.getArtifactId())) { //$NON-NLS-1$
			// Then it's executed in just any directory - base on the current dir
			Path base = Paths.get(System.getProperty("user.dir")); //$NON-NLS-1$
			odpDir = base.resolve(odpDirectory.toPath());
		} else {
			Path base = project.getBasedir().toPath();
			odpDir = base.resolve(odpDirectory.toPath());
		}
		
		String databasePath = this.databasePath;
		File databaseFile = this.file;
		if((databasePath == null || databasePath.isEmpty()) && databaseFile == null) {
			if("standalone-pom".equals(project.getArtifactId()) || project.getPackaging().equals("domino-nsf")) { //$NON-NLS-1$ //$NON-NLS-2$
				throw new IllegalArgumentException(Messages.getString("GenerateODPMojo.pathOrFileRequired")); //$NON-NLS-1$
			} else {
				if(log.isInfoEnabled()) {
					log.info(Messages.getString("GenerateODPMojo.skip")); //$NON-NLS-1$
				}
				return;
			}
		}
		if(log.isInfoEnabled()) {
			if(databaseFile == null) {
				log.info(Messages.getString("GenerateODPMojo.generatingForDatabase", databasePath)); //$NON-NLS-1$
			} else {
				log.info(Messages.getString("GenerateODPMojo.generatingForDatabase", databaseFile)); //$NON-NLS-1$
			}
		}
		
		URL exporterServerUrl = Objects.requireNonNull(this.exporterServerUrl);
		if(log.isDebugEnabled()) {
			log.debug(Messages.getString("GenerateODPMojo.usingServerUrl", exporterServerUrl)); //$NON-NLS-1$
		}
		
		try {
			if(isRunLocally()) {
				exportODPLocal(odpDir);
			} else {
				Path zip = exportODPRemote();
				try {
					Path eclipseProject = odpDir.resolve(".project"); //$NON-NLS-1$
					if(Files.exists(eclipseProject)) {
						Path tempPath = Files.createTempFile("nsfodp", ".project"); //$NON-NLS-1$ //$NON-NLS-2$
						Files.delete(tempPath);
						Files.move(eclipseProject, tempPath);
						eclipseProject = tempPath;
					} else {
						eclipseProject = null;
					}
					
					if(Files.exists(odpDir)) {
						NSFODPUtil.deltree(Collections.singleton(odpDir));
					}
					Files.createDirectories(odpDir);
					
					// Extract the ZIP to the destination
					try(ZipFile zipFile = new ZipFile(zip.toFile(), StandardCharsets.UTF_8)) {
						for(ZipEntry entry : Collections.list(zipFile.entries())) {
							if(log.isDebugEnabled()) {
								log.debug(Messages.getString("GenerateODPMojo.exportingZipEntry", entry.getName())); //$NON-NLS-1$
							}
							
							String name = entry.getName();
							if(name != null && !name.isEmpty()) {
								Path entryPath = Paths.get(name);
								Path fullPath = odpDir.resolve(entryPath);
								if(entry.isDirectory()) {
									Files.createDirectories(fullPath);
								} else {
									Files.createDirectories(fullPath.getParent());
									try(InputStream is = zipFile.getInputStream(entry)) {
										Files.copy(is, fullPath);
									}
								}
							}
						}
					}
					if(eclipseProject != null) {
						Files.move(eclipseProject, odpDir.resolve(".project"), StandardCopyOption.REPLACE_EXISTING); //$NON-NLS-1$
					}
				} finally {
					Files.deleteIfExists(zip);
				}
			}
		} catch(Throwable t) {
			throw new MojoExecutionException(Messages.getString("GenerateODPMojo.exceptionGenerating"), t); //$NON-NLS-1$
		}
	}
	
	// *******************************************************************************
	// * Local execution
	// *******************************************************************************
	
	private void exportODPLocal(Path odpDir) throws IOException {
		EquinoxExporter exporter = new EquinoxExporter(pluginDescriptor, mavenSession, project, getLog(), notesProgram.toPath(), notesPlatform);
		if(file == null) {
			exporter.exportOdp(odpDir, databasePath, binaryDxl, swiperFilter, richTextAsItemData);
		} else {
			exporter.exportOdp(odpDir, file.getAbsolutePath(), binaryDxl, swiperFilter, richTextAsItemData);
		}
	}
	
	// *******************************************************************************
	// * Remote execution
	// *******************************************************************************
	
	private Path exportODPRemote() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException, MojoExecutionException, ClientProtocolException, IOException {
		HttpClientBuilder httpBuilder = HttpClients.custom();
		if(this.exporterServerTrustSelfSignedSsl) {
			SSLContextBuilder sslBuilder = new SSLContextBuilder();
			sslBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslBuilder.build(), null, null, NoopHostnameVerifier.INSTANCE);
			httpBuilder.setSSLSocketFactory(sslsf);
		}
		
		try(CloseableHttpClient client = httpBuilder.build()) {
			URI servlet = exporterServerUrl.toURI().resolve(SERVLET_PATH);
			if(log.isInfoEnabled()) {
				log.info(Messages.getString("GenerateODPMojo.generatingWithServer", servlet)); //$NON-NLS-1$
			}
			
			File databaseFile = this.file;
			HttpUriRequest req;
			if(databaseFile != null) {
				req = new HttpPost(servlet);
				FileEntity fileEntity = new FileEntity(databaseFile);
				fileEntity.setContentType("application/octet-stream"); //$NON-NLS-1$
				((HttpPost)req).setEntity(fileEntity);
			} else {
				req = new HttpGet(servlet);
				req.addHeader(NSFODPConstants.HEADER_DATABASE_PATH, this.databasePath);
			}
			
			ODPMojoUtil.addAuthenticationInfo(this.wagonManager, this.exporterServer, req, this.log);
			
			req.addHeader(NSFODPConstants.HEADER_BINARY_DXL, String.valueOf(this.binaryDxl));
			req.addHeader(NSFODPConstants.HEADER_SWIPER_FILTER, String.valueOf(this.swiperFilter));
			req.addHeader(NSFODPConstants.HEADER_RICH_TEXT_AS_ITEM_DATA, String.valueOf(this.richTextAsItemData));
			
			HttpResponse res = client.execute(req);
			HttpEntity responseEntity = ResponseUtil.checkResponse(log, res);
 			
			try(InputStream is = responseEntity.getContent()) {
				ResponseUtil.monitorResponse(log, is);
				
				// Now that we're here, the rest will be the compiler output
				Path result = Files.createTempFile("odpexporter-output", ".nsf"); //$NON-NLS-1$ //$NON-NLS-2$
				Files.copy(is, result, StandardCopyOption.REPLACE_EXISTING);
				return result;
			}
		}
	}

}
