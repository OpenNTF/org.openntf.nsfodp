package org.openntf.maven.nsfodp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.openntf.maven.nsfodp.util.ODPMojoUtil;
import org.openntf.maven.nsfodp.util.ResponseUtil;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.NSFODPUtil;

/**
 * Goal which generates an "odp" folder (replacing any existing in the directory or project)
 * from an NSF path using a remote server.
 * 
 * @author Jesse Gallagher
 * @since 1.4.0
 */
@Mojo(name="generateODP", requiresProject=false)
public class GenerateODPMojo extends AbstractMojo {
	
	public static final String SERVLET_PATH = "/org.openntf.nsfodp/exporter"; //$NON-NLS-1$

	@Parameter(defaultValue="${project}", readonly=true, required=false)
	private MavenProject project;

	@Component
	private WagonManager wagonManager;
	
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
	@Parameter(property="nsfodp.exporter.databasePath", required=true)
	private String databasePath;
	
	/**
	 * Whether or not to run the DXL through a Swiper filter. Defaults to <code>true</code>.
	 */
	@Parameter(property="nsfodp.exporter.swiperFilter", required=false)
	private boolean swiperFilter = true;
	/**
	 * Whether or not to use "binary" DXL format. Defaults to <code>true</code>.
	 */
	@Parameter(property="nsfodp.exporter.binaryDxl", required=false)
	private boolean binaryDxl = true;

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
		
		String databasePath = Objects.requireNonNull(this.databasePath);
		if(log.isInfoEnabled()) {
			log.info(Messages.getString("GenerateODPMojo.generatingForDatabase", databasePath)); //$NON-NLS-1$
		}
		
		URL exporterServerUrl = Objects.requireNonNull(this.exporterServerUrl);
		if(log.isDebugEnabled()) {
			log.debug(Messages.getString("GenerateODPMojo.usingServerUrl", exporterServerUrl)); //$NON-NLS-1$
		}
		
		try {
			Path zip = exportODP();
			try {
				if(Files.exists(odpDir)) {
					NSFODPUtil.deltree(Collections.singleton(odpDir));
				}
				Files.createDirectories(odpDir);
				
				// Extract the ZIP to the destination
				try(ZipFile zipFile = new ZipFile(zip.toFile())) {
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
			} finally {
				Files.deleteIfExists(zip);
			}
		} catch(Throwable t) {
			throw new MojoExecutionException(Messages.getString("GenerateODPMojo.exceptionGenerating"), t); //$NON-NLS-1$
		}

	}
	
	private Path exportODP() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException, MojoExecutionException, ClientProtocolException, IOException {
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
			HttpGet get = new HttpGet(servlet);
			
			ODPMojoUtil.addAuthenticationInfo(this.wagonManager, this.exporterServer, get, this.log);
			
			get.addHeader(NSFODPConstants.HEADER_DATABASE_PATH, this.databasePath);
			get.addHeader(NSFODPConstants.HEADER_BINARY_DXL, String.valueOf(this.binaryDxl));
			get.addHeader(NSFODPConstants.HEADER_SWIPER_FILTER, String.valueOf(this.swiperFilter));
			
			HttpResponse res = client.execute(get);
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
