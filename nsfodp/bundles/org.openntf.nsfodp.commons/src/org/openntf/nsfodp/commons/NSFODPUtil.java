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
package org.openntf.nsfodp.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public enum NSFODPUtil {
	;

	/**
	 * Returns an appropriate temp directory for the system. On Windows, this is
	 * equivalent to <code>System.getProperty("java.io.tmpdir")</code>. On
	 * Linux, however, since this seems to return the data directory in some
	 * cases, it uses <code>/tmp</code>.
	 *
	 * @return an appropriate temp directory for the system
	 */
	public static Path getTempDirectory() {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (osName.startsWith("Linux") || osName.startsWith("LINUX")) { //$NON-NLS-1$ //$NON-NLS-2$
			return Paths.get("/tmp"); //$NON-NLS-1$
		} else {
			return Paths.get(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
		}
	}
	
	public static void deltree(Collection<Path> paths) throws IOException {
		for(Path path : paths) {
			deltree(path);
		}
	}
	
	/**
	 * @since 3.0.0
	 */
	public static void deltree(Path path) throws IOException {
		if(Files.isDirectory(path)) {
			try(Stream<Path> walk = Files.list(path)) {
				walk.forEach(p -> {
					try {
						deltree(p);
					} catch(IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
		}
		try {
			Files.deleteIfExists(path);
		} catch(IOException e) {
			// This is likely a Windows file-locking thing
			e.printStackTrace();
		}
	}
	
	/**
	 * Determines whether the given {@link path} is a directory and contains at least
	 * one entry.
	 * 
	 * @param path the path to check
	 * @return {@true} if the path is a non-empty directory; {@code false} otherwise
	 * @throws UncheckedIOException if there is a problem reading the filesystem
	 * @since 3.5.0
	 */
	public static boolean isNonEmptyDirectory(Path path) {
		if(Files.isDirectory(path)) {
			try(Stream<Path> entryStream = Files.list(path)) {
				return entryStream.findFirst().isPresent();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return false;
	}
	
	/**
	 * Moves the source directory to the destination, taking into account the possibility
	 * that the source and destination reside on different volumes.
	 * 
	 * @param source the source directory to move (e.g. "/foo/bar")
	 * @param dest the destination path of the directory (e.g. "/baz/bar")
	 * @throws IOException if there is a filesystem problem moving the directory
	 * @since 2.1.0
	 */
	public static void moveDirectory(Path source, Path dest) throws IOException {
		Files.walkFileTree(source, new CopyFileVisitor(dest));
		deltree(Collections.singleton(source));
	}
	
	/**
	 * Copies the source directory to the destination.
	 * 
	 * @param source the source directory to copy (e.g. "/foo/bar")
	 * @param dest the destination path of the directory (e.g. "/baz/bar")
	 * @throws IOException if there is a filesystem problem copying the directory
	 * @since 3.0.0
	 */
	public static void copyDirectory(Path source, Path dest) throws IOException {
		Files.walkFileTree(source, new CopyFileVisitor(dest));
	}
	
	private static class CopyFileVisitor extends SimpleFileVisitor<Path> {
		private final Path targetPath;
		private Path sourcePath = null;

		public CopyFileVisitor(Path targetPath) {
			this.targetPath = targetPath;
		}

		@Override
		public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
			if (sourcePath == null) {
				sourcePath = dir;
			} else {
				Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir).toString()));
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			if(file.getFileName().toString().startsWith(".DS_Store")) { //$NON-NLS-1$
				// skip
				return FileVisitResult.CONTINUE;
			}
			
			Path target = targetPath.resolve(sourcePath.relativize(file).toString());
			// Make sure the directory was indeed created - observed oddities on macOS Docker's bridged FS
			if(!Files.exists(target.getParent())) {
				Files.createDirectories(target.getParent());
			}
 			Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
			return FileVisitResult.CONTINUE;
		}
	}
	
	/**
	 * Expands the contents of the provided ZIP file to the destination directory.
	 * 
	 * @param zipFile the ZIP file to expand
	 * @param destDirectory the destination base directory
	 * @throws IOException if there is a problem reading the ZIP or expanding the contents
	 * @since 3.0.0
	 */
	public static void unzip(Path zipFile, Path destDirectory) throws IOException {
		try(InputStream is = Files.newInputStream(zipFile)) {
			try(ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8)) {
				ZipEntry entry = zis.getNextEntry();
				while(entry != null) {
					Path dest = destDirectory.resolve(entry.getName());
					if(entry.isDirectory()) {
						Files.createDirectories(dest);
					} else {
						Files.createDirectories(dest.getParent());
						Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
					}
					
					entry = zis.getNextEntry();
				}
			}
		}
	}
	
	/**
	 * @return {@code true} if the current OS is macOS; {@code false} otherwise
	 * @since 3.0.0
	 */
	public static boolean isOsMac() {
		String osName = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("os.name")); //$NON-NLS-1$
		return osName.toLowerCase().startsWith("mac"); //$NON-NLS-1$
	}

	/**
	 * Expands the provided ZIP file to a temporary directory and returns that directory path.
	 * 
	 * <p>This method may rename ZIP entries with non-ASCII characters to URL-encoded variants if the
	 * underlying filesystem driver throws an exception. This was seen on Linux in particular.</p>
	 * 
	 * @param zipFilePath a {@link Path} to the ZIP file to expand
	 * @return a {@link Path} to the expanded contents of the ZIP file
	 * @throws IOException if there is a problem reading the ZIP file or writing to the temporary directory
	 * @since 3.4.0
	 */
	public static Path expandZip(Path zipFilePath) throws IOException {
		Path result = Files.createTempDirectory(getTempDirectory(), "zipFile"); //$NON-NLS-1$
		
		try(InputStream is = Files.newInputStream(zipFilePath)) {
			try(ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8)) {
				ZipEntry entry;
				while((entry = zis.getNextEntry()) != null) {
					String name = entry.getName();

					Path subFile;
					try {
						subFile = result.resolve(name);
					} catch(InvalidPathException e) {
						// This occurs with non-ASCII characters on Unix sometimes
						String urlName = URLEncoder.encode(name, "UTF-8") //$NON-NLS-1$
							.replace("%2F", "/"); //$NON-NLS-1$ //$NON-NLS-2$
						subFile = result.resolve(urlName);
					}
					
					if(entry.isDirectory()) {
						Files.createDirectories(subFile);
					} else {
						Files.createDirectories(subFile.getParent());
						Files.copy(zis, subFile);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Creates an NIO {@link FileSystem} reference for the contents of the provided ZIP file.
	 * 
	 * @param zipFilePath a {@link Path} to the ZIP file
	 * @return a {@link FileSystem} object representing the contents of the ZIP
	 * @throws IOException if there is a problem creating the path
	 * @since 3.4.0
	 */
	public static FileSystem openZipPath(Path zipFilePath) throws IOException {
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
	}
	
	/**
	 * Determines whether the provided pattern matches the flags value from a note, in the fashion
	 * of the Notes C API.
	 * 
	 * @param flags a design flag value to test
	 * @param pattern a flag pattern to test against (DFLAGPAT_*)
	 * @return whether the flags match the pattern
	 * @since 3.5.0
	 */
	public static boolean matchesFlagsPattern(String flags, String pattern) {
		if(pattern == null || pattern.isEmpty()) {
			return false;
		}
		
		String toTest = flags == null ? "" : flags; //$NON-NLS-1$
		
		// Patterns start with one of four characters:
		// "+" (match any)
		// "-" (match none)
		// "*" (match all)
		// "(" (multi-part test)
		String matchers = null;
		String antiMatchers = null;
		String allMatchers = null;
		char first = pattern.charAt(0);
		switch(first) {
		case '+':
			matchers = pattern.substring(1);
			antiMatchers = ""; //$NON-NLS-1$
			allMatchers = ""; //$NON-NLS-1$
			break;
		case '-':
			matchers = ""; //$NON-NLS-1$
			antiMatchers = pattern.substring(1);
			allMatchers = ""; //$NON-NLS-1$
			break;
		case '*':
			matchers = ""; //$NON-NLS-1$
			antiMatchers = ""; //$NON-NLS-1$
			allMatchers = pattern.substring(1);
		case '(':
			// The order is always +-*
			int plusIndex = pattern.indexOf('+');
			int minusIndex = pattern.indexOf('-');
			int starIndex = pattern.indexOf('*');
			
			matchers = pattern.substring(plusIndex+1, minusIndex == -1 ? pattern.length() : minusIndex);
			antiMatchers = minusIndex == -1 ? "" : pattern.substring(minusIndex+1, starIndex == -1 ? pattern.length() : starIndex); //$NON-NLS-1$
			allMatchers = starIndex == -1 ? "" : pattern.substring(starIndex+1); //$NON-NLS-1$
			break;
		}
		if(matchers == null) { matchers = ""; } //$NON-NLS-1$
		if(antiMatchers == null) { antiMatchers = ""; } //$NON-NLS-1$
		if(allMatchers == null) { allMatchers = ""; } //$NON-NLS-1$
		
		// Test "match against any" and fail if it doesn't
		boolean matchedAny = matchers.isEmpty();
		for(int i = 0; i < matchers.length(); i++) {
			if(toTest.indexOf(matchers.charAt(i)) > -1) {
				matchedAny = true;
				break;
			}
		}
		if(!matchedAny) {
			return false;
		}
		
		// Test "match none" and fail if it does
		for(int i = 0; i < antiMatchers.length(); i++) {
			if(toTest.indexOf(antiMatchers.charAt(i)) > -1) {
				// Exit immediately
				return false;
			}
		}
		
		// Test "match all" and fail if it doesn't
		for(int i = 0; i < allMatchers.length(); i++) {
			if(toTest.indexOf(allMatchers.charAt(i)) == -1) {
				// Exit immediately
				return false;
			}
		}
		
		// If we survived to here, it must match
		return true;
	}
	
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
	 * @since 3.5.0
	 */
	public static InputStream newInputStream(Path path, OpenOption... options) throws IOException {
		Objects.requireNonNull(path, "path cannot be null");
		FileSystem fs = path.getFileSystem();
		if("jar".equals(fs.provider().getScheme())) { //$NON-NLS-1$
			// In practice, Files.copy in ZIP FS copies the file properly, while Files.newInputStream adds nulls
			Path tempFile = Files.createTempFile(NSFODPUtil.class.getSimpleName(), ".bin"); //$NON-NLS-1$
			Files.copy(path, tempFile, StandardCopyOption.REPLACE_EXISTING);
			return new TempFileInputStream(tempFile, options);
		} else {
			// Otherwise, just use the normal method
			return Files.newInputStream(path, options);
		}
	}
}
