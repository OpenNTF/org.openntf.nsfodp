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
package org.openntf.nsfodp.commons.odp.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.tools.JavaFileObject;

import org.openntf.nsfodp.commons.odp.JavaSource;
import org.openntf.nsfodp.commons.odp.Messages;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

public enum ODPUtil {
	;
	
	public static String readFile(Path path) {
		try {
			return String.join("\n", Files.readAllLines(path, StandardCharsets.UTF_8)); //$NON-NLS-1$
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Document readXml(Path file) {
		// Let the XML parser handle reading, since XML has charset hints in the prolog
		try(InputStream is = Files.newInputStream(file)) {
			return DOMUtil.createDocument(is);
		} catch(IOException | XMLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toJavaClassName(Path path) {
		String name = path.toString();
		if(name.endsWith(JavaFileObject.Kind.SOURCE.extension)) {
			return name.substring(0, name.length()-JavaFileObject.Kind.SOURCE.extension.length())
				.replace(path.getFileSystem().getSeparator(), "."); //$NON-NLS-1$
		} else if(name.endsWith(JavaFileObject.Kind.CLASS.extension)) {
			return name.substring(0, name.length()-JavaFileObject.Kind.CLASS.extension.length())
				.replace(path.getFileSystem().getSeparator(), "."); //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException(MessageFormat.format(Messages.ODPUtil_cannotInferClassName, path));
		}
	}
	
	public static String toJavaPath(String className) {
		return className.replace('.', '/') + JavaFileObject.Kind.CLASS.extension;
	}
	
	public static List<JavaSource> listJavaFiles(Path baseDir) {
		try {
			return Files.find(baseDir, Integer.MAX_VALUE,
					(path, attr) -> path.toString().endsWith(JavaFileObject.Kind.SOURCE.extension) && attr.isRegularFile())
					.map(path -> new JavaSource(path))
					.collect(Collectors.toList());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gathers a full dependency hierarchy for the bundles described in {@code bundleIds}.
	 * 
	 * @param bundleContext the bundle context to use for resolution
	 * @param bundleIds the base bundle IDs
	 * @return a list of bundle IDs for the provided bundles and all re-exported dependencies
	 */
	public static Collection<String> expandRequiredBundles(BundleContext bundleContext, List<String> bundleIds) {
		Objects.requireNonNull(bundleContext);
		return Objects.requireNonNull(bundleIds).stream()
			.map(id -> ODPUtil.findBundle(bundleContext, id, false))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(bundle -> {
				if(bundle.getState() == Bundle.INSTALLED) {
					throw new IllegalStateException(MessageFormat.format(Messages.ODPUtil_bundleInInstalledState, bundle.getSymbolicName()));
				}
				
				List<Bundle> deps = new ArrayList<>(resolveRequiredBundles(bundleContext, bundle));
				deps.add(bundle);
				return deps;
			})
			.flatMap(Collection::stream)
			.filter(Objects::nonNull)
			.map(Bundle::getSymbolicName)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	private static List<Bundle> resolveRequiredBundles(BundleContext bundleContext, Bundle bundle) {
		String requiredBundles = bundle.getHeaders().get("Require-Bundle"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(requiredBundles)) {
			String[] requires = StringUtil.splitString(requiredBundles, ',', true);
			return Arrays.stream(requires)
				.filter(req -> req.contains("visibility:=reexport")) //$NON-NLS-1$
				.map(req -> req.substring(0, req.indexOf(';')))
				.map(id -> ODPUtil.findBundle(bundleContext, id, false))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(dependency -> {
					if(dependency.getState() == Bundle.INSTALLED) {
						throw new IllegalStateException(MessageFormat.format(Messages.ODPUtil_bundleInInstalledState, dependency.getSymbolicName()));
					}
					
					List<Bundle> deps = new ArrayList<>(resolveRequiredBundles(bundleContext, dependency));
					deps.add(dependency);
					return deps;
				})
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
	
	public static Optional<Bundle> findBundle(BundleContext bundleContext, String bundleId, boolean resolveAny) {
		for(Bundle bundle : bundleContext.getBundles()) {
			if(StringUtil.equals(bundleId, bundle.getSymbolicName()) && (resolveAny || bundle.getState() != Bundle.INSTALLED)) {
				return Optional.of(bundle);
			}
		}
		return Optional.empty();
	}

	public static String toBasicFilePath(Path baseDir, Path file) {
		return baseDir.relativize(file).toString().replace(File.separatorChar, '/');
	}
	
	/**
	 * Finds extension objects for the provided service class.
	 *
	 * @param <T> the expected type of extension objects
	 * @param serviceClass the class representing the desired extension point
	 * @return a {@link Stream} of available implementation objects
	 * @since 3.5.0
	 */
	public static <T> Stream<T> findServices(final Class<T> serviceClass) {
		return AccessController.doPrivileged((PrivilegedAction<Stream<T>>)() ->
			StreamSupport.stream(ServiceLoader.load(serviceClass).spliterator(), false)
		);
	}
}
