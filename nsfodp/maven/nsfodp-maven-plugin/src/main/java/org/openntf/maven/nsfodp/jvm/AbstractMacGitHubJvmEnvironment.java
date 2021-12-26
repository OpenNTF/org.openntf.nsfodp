package org.openntf.maven.nsfodp.jvm;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang3.SystemUtils;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;
import org.openntf.nsfodp.commons.osgi.EquinoxRunner;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

/**
 * This {@link JvmEnvironment} abstract class contains common behavior for macOS-based environments
 * that download runtimes automatically.
 * 
 * @author Jesse Gallagher
 * @since 3.7.0
 */
public abstract class AbstractMacGitHubJvmEnvironment extends AbstractJvmEnvironment {
	private static final Logger log = Logger.getLogger(AbstractMacGitHubJvmEnvironment.class.getName());

	public static final String JAVA_VERSION = "8"; //$NON-NLS-1$
	
	protected abstract String getReleasesApi();
	
	protected String getJavaVersion() {
		return JAVA_VERSION;
	}
	
	protected abstract String getProviderName();
	
	protected abstract String getShortName();

	@SuppressWarnings("unchecked")
	@Override
	public Path getJavaHome(Path notesProgram) {
		Path userHome = SystemUtils.getUserHome().toPath();
		Path jvmDir = userHome.resolve(".nsfodp").resolve("jvm").resolve(getShortName()); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.isDirectory(jvmDir)) {
			String releasesUrl = format(getReleasesApi(), JAVA_VERSION);
			String providerName = getProviderName();
			List<Map<String, Object>> releases = fetchGitHubReleasesList(providerName, releasesUrl);
			
			// Find any applicable releases, in order, as some releases may contain only certain platforms
			List<Map<String, Object>> validReleases = releases.stream()
				.filter(release -> !(Boolean)release.get("prerelease")) //$NON-NLS-1$
				.filter(release -> !(Boolean)release.get("draft")) //$NON-NLS-1$
				.filter(release -> release.containsKey("assets")) //$NON-NLS-1$
				.collect(Collectors.toList());
			if(validReleases.isEmpty()) {
				throw new IllegalStateException(format("Unable to locate JDK build for {0}, releases URL {1}", providerName, releasesUrl)); //$NON-NLS-1$
			}
			
			String qualifier = format("jdk_{0}_{1}", getOsArch(), getOsName()); //$NON-NLS-1$
			Map<String, Object> download = validReleases.stream()
				.map(release -> (List<Map<String, Object>>)release.get("assets")) //$NON-NLS-1$
				.flatMap(Collection::stream)
				.filter(asset -> !StringUtil.toString(asset.get("name")).contains("-testimage")) //$NON-NLS-1$ //$NON-NLS-2$
				.filter(asset -> !StringUtil.toString(asset.get("name")).contains("-debugimage")) //$NON-NLS-1$ //$NON-NLS-2$
				.filter(asset -> StringUtil.toString(asset.get("name")).contains("-" + qualifier + "_")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.filter(asset -> isValidContentType(asset.get("content_type"))) //$NON-NLS-1$
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(format("Unable to find {0} build for {1}", providerName, qualifier))); //$NON-NLS-1$
			if(log.isLoggable(Level.INFO)) {
				log.info(format("Downloading {0} JDK from {1}", providerName, download.get("browser_download_url")));  //$NON-NLS-1$//$NON-NLS-2$
			}
			
			String contentType = (String)download.get("content_type"); //$NON-NLS-1$
			download((String)download.get("browser_download_url"), contentType, jvmDir); //$NON-NLS-1$
			
			markExecutables(jvmDir);
		}
		return jvmDir.resolve("Contents").resolve("Home"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public Collection<Path> initNotesJars(Path notesProgram) throws IOException {
		Collection<Path> toLink = new LinkedHashSet<>();
		EquinoxRunner.addIBMJars(notesProgram, toLink);

		Collection<Path> result = new LinkedHashSet<>();
		Path destBase = getJavaHome(notesProgram).resolve("jre").resolve("lib").resolve("ext"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Files.createDirectories(destBase);
		
		for(Path jar : toLink) {
			Path destJar = destBase.resolve(jar.getFileName());
			Files.copy(jar, destJar, StandardCopyOption.REPLACE_EXISTING);
			result.add(destJar);
		}
		
		return result;
	}
	
	@Override
	public Map<String, String> getJvmProperties(Path notesProgram) {
		String escapedPath = notesProgram.toString();
		return Collections.singletonMap("java.library.path", escapedPath); //$NON-NLS-1$
	}

	// *******************************************************************************
	// * Utility methods
	// *******************************************************************************
	
	private static boolean isValidContentType(Object contentType) {
		switch(StringUtil.toString(contentType)) {
		case "application/x-compressed-tar": //$NON-NLS-1$
		case "application/gzip": //$NON-NLS-1$
		case "application/zip": //$NON-NLS-1$
			return true;
		default:
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected static List<Map<String, Object>> fetchGitHubReleasesList(String providerName, String releasesUrl) {
		try {
			return download(new URL(releasesUrl), is -> {
				try(Reader r = new InputStreamReader(is)) {
					return (List<Map<String, Object>>)JsonParser.fromJson(JsonJavaFactory.instance, r);
				} catch (JsonException e) {
					throw new RuntimeException(e);
				}
			});
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected static void download(String url, String contentType, Path jvmDir) {
		// TODO consider replacing with NIO filesystem operations, though they don't inherently support .tar.gz
		try {
			download(new URL(url), is -> {
				switch(StringUtil.toString(contentType)) {
				case "application/zip": //$NON-NLS-1$
					try(ZipInputStream zis = new ZipInputStream(is)) {
						extract(zis, jvmDir);
					}
					break;
				case "application/x-compressed-tar": //$NON-NLS-1$
				case "application/gzip": //$NON-NLS-1$
					try(GZIPInputStream gzis = new GZIPInputStream(is)) {
						try(TarArchiveInputStream tis = new TarArchiveInputStream(gzis)) {
							extract(tis, jvmDir);
						}
					}
					break;
				default:
					throw new IllegalStateException(format("Unsupported content type: {0}", contentType));
				}
				return null;
			});
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	protected static void markExecutables(Path jvmDir) {
		Path bin = jvmDir.resolve("bin"); //$NON-NLS-1$
		if(Files.isDirectory(bin)) {
			markExecutablesInBinDir(bin);
		}
		Path jreBin = jvmDir.resolve("jre").resolve("bin"); //$NON-NLS-1$ //$NON-NLS-2$
		if(Files.isDirectory(jreBin)) {
			markExecutablesInBinDir(jreBin);
		}
		Path contentsBin = jvmDir.resolve("Contents").resolve("Home").resolve("bin"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(Files.isDirectory(contentsBin)) {
			markExecutablesInBinDir(contentsBin);
		}
	}
	
	protected static void markExecutablesInBinDir(Path bin) {
		if(bin.getFileSystem().supportedFileAttributeViews().contains("posix")) { //$NON-NLS-1$
			try {
				Files.list(bin)
					.filter(Files::isRegularFile)
					.forEach(p -> {
						try {
							Set<PosixFilePermission> perms = EnumSet.copyOf(Files.getPosixFilePermissions(p));
							perms.add(PosixFilePermission.OWNER_EXECUTE);
							perms.add(PosixFilePermission.GROUP_EXECUTE);
							perms.add(PosixFilePermission.OTHERS_EXECUTE);
							Files.setPosixFilePermissions(p, perms);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
	
	protected static void extract(ZipInputStream zis, Path dest) throws IOException {
		ZipEntry entry = zis.getNextEntry();
		while(entry != null) {
			String name = entry.getName();
			
			if(StringUtil.isNotEmpty(name)) {
				// The first directory is a container
				int slashIndex = name.indexOf('/');
				if(slashIndex > -1) {
					name = name.substring(slashIndex+1);
				}
				
				if(StringUtil.isNotEmpty(name)) {
					if(log.isLoggable(Level.FINER)) {
						log.finer(format("Deploying file {0}", name));
					}
					
					Path path = dest.resolve(name);
					if(entry.isDirectory()) {
						Files.createDirectories(path);
					} else {
						Files.createDirectories(path.getParent());
						Files.copy(zis, path, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
			
			zis.closeEntry();
			entry = zis.getNextEntry();
		}
	}
	
	protected static void extract(TarArchiveInputStream tis, Path dest) throws IOException {
		TarArchiveEntry entry = tis.getNextTarEntry();
		while(entry != null) {
			String name = entry.getName();

			if(StringUtil.isNotEmpty(name)) {
				// The first directory is a container
				int slashIndex = name.indexOf('/');
				if(slashIndex > -1) {
					name = name.substring(slashIndex+1);
				}
				
				if(StringUtil.isNotEmpty(name)) {
					if(log.isLoggable(Level.FINER)) {
						log.finer(format("Deploying file {0}", name));
					}
					
					Path path = dest.resolve(name);
					if(entry.isDirectory()) {
						Files.createDirectories(path);
					} else {
						Files.createDirectories(path.getParent());
						Files.copy(tis, path, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
			
			entry = tis.getNextTarEntry();
		}
	}
	
	@FunctionalInterface
	public static interface IOFunction<T> {
		T apply(InputStream is) throws IOException;
	}
	
	/**
	 * @param <T> the expected return type
	 * @param url the URL to fetch
	 * @param consumer a handler for the download's {@link InputStream}
	 * @return the consumed value
	 * @throws IOException if there is an unexpected problem downloading the file or if the server
	 * 		returns any code other than {@link HttpURLConnection#HTTP_OK}
	 * @since 2.0.0
	 */
	public static <T> T download(URL url, IOFunction<T> consumer) throws IOException {
		// Domino defaults to using old protocols - bump this up for our needs here so the connection succeeds
		String protocols = StringUtil.toString(System.getProperty("https.protocols")); //$NON-NLS-1$
		try {
			System.setProperty("https.protocols", "TLSv1.2"); //$NON-NLS-1$ //$NON-NLS-2$
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			int responseCode = conn.getResponseCode();
			try {
				if(responseCode != HttpURLConnection.HTTP_OK) {
					throw new IOException(format("Unexpected response code {0} from URL {1}", responseCode, url));
				}
				try(InputStream is = conn.getInputStream()) {
					return consumer.apply(is);
				}
			} finally {
				conn.disconnect();
			}
		} finally {
			System.setProperty("https.protocols", protocols); //$NON-NLS-1$
		}
	}
	
	private static String getOsArch() {
		return "x64";
	}
	
	private static String getOsName() {
		return "mac";
	}
}
