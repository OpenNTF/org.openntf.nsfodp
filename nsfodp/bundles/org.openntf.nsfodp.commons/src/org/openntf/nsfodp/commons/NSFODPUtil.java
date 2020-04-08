/**
 * Copyright © 2018-2020 Jesse Gallagher
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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;

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
			Files.list(path)
			    .forEach(t -> {
					try {
						NSFODPUtil.deltree(t);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		}
		try {
			Files.deleteIfExists(path);
		} catch(IOException e) {
			// This is likely a Windows file-locking thing. In this case,
			//   punt and hand it off to File#deleteOnExit
			path.toFile().deleteOnExit();
		}
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
				Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
			return FileVisitResult.CONTINUE;
		}
	}
}
