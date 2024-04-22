package org.openntf.com.ibm.xsp.extlib.bazaar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import org.openntf.com.ibm.xsp.extlib.bazaar.util.TempFileInputStream;

/**
 * Utility methods shared by compiler classes.
 * 
 * @author Jesse Gallagher
 * @since 2.0.7
 */
public enum BazaarUtil {
	;
	
	/**
	 * Opens an {@link InputStream} for the provided path.
	 * 
	 * <p>This method differs from {@link Files#newInputStream} in that it has special handling
	 * for ZIP filesystems to work around bugs in the Java 8 implementation. Specifically, when
	 * {@code path} is in a ZIP filesystem, this method first extracts the file to a temporary file.
	 * This file is deleted when the input stream is closed.</p>
	 *
     * @param path the path to the file to open
     * @param options options specifying how the file is opened
     * @return a new input stream
	 * @throws IOException if a lower-level I/O exception occurs
	 * @since 2.0.7
	 */
	public static InputStream newInputStream(Path path, OpenOption... options) throws IOException {
		Objects.requireNonNull(path, "path cannot be null");
		FileSystem fs = path.getFileSystem();
		if("jar".equals(fs.provider().getScheme())) { //$NON-NLS-1$
			// In practice, Files.copy in ZIP FS copies the file properly, while Files.newInputStream adds nulls
			Path tempFile = Files.createTempFile(BazaarUtil.class.getSimpleName(), ".bin"); //$NON-NLS-1$
			Files.copy(path, tempFile, StandardCopyOption.REPLACE_EXISTING);
			return new TempFileInputStream(tempFile, options);
		} else {
			// Otherwise, just use the normal method
			return Files.newInputStream(path, options);
		}
	}
	
	/**
	 * Creates an NIO {@link FileSystem} reference for the contents of the provided ZIP file.
	 * 
	 * @param zipFilePath a {@link Path} to the ZIP file
	 * @return a {@link FileSystem} object representing the contents of the ZIP
	 * @throws UncheckedIOException if there is a problem creating the path
	 * @since 2.0.7
	 */
	public static FileSystem openZipPath(Path zipFilePath) {
		try {
			// Create the ZIP file if it doesn't exist already
			if(!Files.exists(zipFilePath) || Files.size(zipFilePath) == 0) {
				try(OutputStream fos = Files.newOutputStream(zipFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					try(ZipOutputStream zos = new ZipOutputStream(fos, StandardCharsets.UTF_8)) {
						zos.setLevel(Deflater.BEST_COMPRESSION);
					}
				}
			}
			
			URI uri = URI.create("jar:" + zipFilePath.toUri()); //$NON-NLS-1$
			Map<String, String> env = new HashMap<>();
			env.put("create", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			env.put("encoding", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			return FileSystems.newFileSystem(uri, env);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
