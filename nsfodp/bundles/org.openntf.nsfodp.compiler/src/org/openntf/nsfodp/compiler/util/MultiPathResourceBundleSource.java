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
package org.openntf.nsfodp.compiler.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.ibm.xsp.library.ClasspathResourceBundleSource;
import com.ibm.xsp.registry.config.ResourceBundleSource;

public class MultiPathResourceBundleSource implements ResourceBundleSource {
	private final Collection<ResourceBundleSource> sources;
	
	public MultiPathResourceBundleSource(Collection<Path> paths) {
		this.sources = Objects.requireNonNull(paths).stream()
			.map(Path::toUri)
			.map(uri -> {
				try {
					return new URLClassLoader(new URL[] { uri.toURL() });
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			})
			.map(ClasspathResourceBundleSource::new)
			.collect(Collectors.toList());
	}

	@Override
	public ResourceBundle getBundle(String bundleName) {
		return sources.stream()
			.map(source -> source.getBundle(bundleName))
			.filter(Objects::nonNull)
			.findFirst().orElse(null);
	}
}
