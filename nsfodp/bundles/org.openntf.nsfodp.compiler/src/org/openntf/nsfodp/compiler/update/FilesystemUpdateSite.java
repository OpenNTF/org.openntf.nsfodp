/**
 * Copyright © 2018-2023 Jesse Gallagher
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
package org.openntf.nsfodp.compiler.update;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an Eclipse Update site on the filesystem containing a "plugins" directory.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class FilesystemUpdateSite implements UpdateSite {
	private final Path baseDir;
	
	/**
	 * @since 3.4.0
	 */
	public FilesystemUpdateSite(Path baseDirectory) {
		this.baseDir = Objects.requireNonNull(baseDirectory);
		if(!Files.isDirectory(this.baseDir)) {
			throw new IllegalArgumentException("baseDir must be a directory");
		}
	}
	
	@Override
	public Collection<URI> getBundleURIs() {
		Path plugins = baseDir.resolve("plugins"); //$NON-NLS-1$
		if(!Files.isDirectory(plugins)) {
			throw new IllegalStateException("plugins directory is not a directory: " + plugins);
		}
		
		try(Stream<Path> pluginStream = Files.list(plugins)) {
			return pluginStream
				.map(Path::toUri)
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
