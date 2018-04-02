/**
 * Copyright © 2018 Jesse Gallagher
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

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents an Eclipse Update site on the filesystem containing a "plugins" directory.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class FilesystemUpdateSite implements UpdateSite {
	private final File baseDir;
	
	public FilesystemUpdateSite(File baseDirectory) {
		this.baseDir = Objects.requireNonNull(baseDirectory);
		if(!this.baseDir.isDirectory()) {
			throw new IllegalArgumentException("baseDir must be a directory");
		}
	}
	
	@Override
	public Collection<URI> getBundleURIs() {
		File plugins = new File(baseDir, "plugins");
		if(!plugins.exists()) {
			throw new IllegalStateException("plugins directory does not exist: " + plugins.getAbsolutePath());
		}
		if(!plugins.isDirectory()) {
			throw new IllegalStateException("plugins directory is not a directory: " + plugins.getAbsolutePath());
		}
		
		return Arrays.stream(plugins.listFiles())
			.map(File::toURI)
			.collect(Collectors.toList());
	}
}
