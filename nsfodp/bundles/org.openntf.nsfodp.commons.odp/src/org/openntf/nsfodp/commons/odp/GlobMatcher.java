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
package org.openntf.nsfodp.commons.odp;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Function;

/**
 * Represents a pairing of a design-element-matching glob to a provider
 * that creates a new design element object for each matched path.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
class GlobMatcher {
	/** Platform-specific PathMatcher separator, escaped in the case of Windows */
	public static final String MATCH_SEP = File.separatorChar == '\\' ? "\\\\" : File.separator; //$NON-NLS-1$
	
	private final PathMatcher matcher;
	private final Function<Path, ? extends AbstractSplitDesignElement> elementProvider;

	/**
	 * @param glob a filesystem glob in Unix format, e.g. "Resources/Images/*" or "WebContent/**"
	 * @param elementProvider a function that provides an element object for a given path
	 */
	public GlobMatcher(String glob, Function<Path, ? extends AbstractSplitDesignElement> elementProvider) {
		this.matcher = glob(glob);
		this.elementProvider = elementProvider;
	}

	public PathMatcher getMatcher() {
		return matcher;
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractSplitDesignElement> T getElement(Path path) {
		return (T)elementProvider.apply(path);
	}
	
	public static final PathMatcher glob(String unixGlob) {
		return FileSystems.getDefault().getPathMatcher("glob:" + unixGlob.replace("/", MATCH_SEP)); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
