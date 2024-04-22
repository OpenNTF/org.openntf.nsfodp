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
package org.openntf.nsfodp.commons.odp;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Function;

import org.openntf.nsfodp.commons.NSFODPUtil;

/**
 * Represents a pairing of a design-element-matching glob to a provider
 * that creates a new design element object for each matched path.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
class GlobMatcher {
	private final String unixGlob;
	private final Function<Path, ? extends AbstractSplitDesignElement> elementProvider;

	/**
	 * @param glob a filesystem glob in Unix format, e.g. "Resources/Images/*" or "WebContent/**"
	 * @param elementProvider a function that provides an element object for a given path
	 */
	public GlobMatcher(String glob, Function<Path, ? extends AbstractSplitDesignElement> elementProvider) {
		this.unixGlob = glob;
		this.elementProvider = elementProvider;
	}

	public PathMatcher getMatcher(FileSystem fileSystem) {
		return glob(fileSystem, unixGlob);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractSplitDesignElement> T getElement(Path path) {
		return (T)elementProvider.apply(path);
	}
	
	public static PathMatcher glob(FileSystem fileSystem, String unixGlob) {
		String sep = fileSystem.getSeparator();
		if("\\".equals(sep) && NSFODPUtil.isOsWindows()) { //$NON-NLS-1$
			// On Windows filesystems specifically, globs must use double-escaped slashes
			sep = "\\\\"; //$NON-NLS-1$
		}
		return fileSystem.getPathMatcher("glob:" + unixGlob.replace("/", sep)); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
