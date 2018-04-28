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
package org.openntf.nsfodp.cli;

import static org.openntf.nsfodp.cli.util.ReflectionUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CLIApp {
	
	public static String[] argv;
	public static Map<String, String> param;
	
	public static void main(String[] argv) {
		CLIApp.argv = argv;
		
		try {
			System.out.println("hey hey hey!");
			
			ClassLoader cl1 = buildBootstrapClassLoader("/lib/commons-cli-1.4.jar");
			param = getLaunchParams(cl1);
			
			// Ferry some params through System.setProperty because the activator is in another classloader
			System.setProperty(CLIApp.class.getPackage().getName() + "-odp", param.get("odp"));
			System.setProperty(CLIApp.class.getPackage().getName() + "-updateSite", param.get("updateSite"));
			
			ClassLoader cl = buildOSGiClassLoader(param.get("notesBin"), "/lib-nonmaven/org.eclipse.osgi_3.10.102.v20160118-1700.jar");
			Thread.currentThread().setContextClassLoader(cl);
			
			Object bundleContext = init(cl);
			String installPath = null;
			try {
				loadBundle(bundleContext, Paths.get(param.get("pluginsDir"), "com.ibm.notes.java.api_9.0.1.20180131-1500.jar"));
				loadBundle(bundleContext, Paths.get(param.get("pluginsDir"), "com.ibm.notes.java.api.win32.linux_9.0.1.20180131-1500.jar"));
				
				Object container = bundleContext.getClass().getMethod("getContainer").invoke(bundleContext);
				Object storage = container.getClass().getMethod("getStorage").invoke(container);
				Field installPathField = storage.getClass().getDeclaredField("installPath");
				installPathField.setAccessible(true);
				installPath = (String)installPathField.get(storage);
				
				Path njempcl = createJempowerShim(Paths.get(param.get("notesBin")));
				loadBundle(bundleContext, njempcl);
				Path bootstrap = createXspBootstrapShim(Paths.get(param.get("notesBin")));
				loadBundle(bundleContext, bootstrap);
				
				loadBundles(bundleContext, Paths.get(param.get("pluginsDir")));
				
				loadEmbeddedBundles(bundleContext, "/lib/com.ibm.xsp.extlibx.bazaar.jar", "/lib/com.ibm.xsp.extlibx.bazaar.interpreter.jar", "/lib/org.openntf.xsp.extlibx.bazaar.odpcompiler.jar");
				
				loadCurrentBundle(bundleContext);
			} finally {
				term();
				if(installPath != null && !installPath.isEmpty()) {
					Path directory = Paths.get(installPath);
					Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
					   @Override
					   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						   if(Files.exists(file)) {
							   Files.delete(file);
						   }
					       return FileVisitResult.CONTINUE;
					   }

					   @Override
					   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						   if(Files.exists(dir) && Files.list(dir).collect(Collectors.toList()).isEmpty()) {
							   Files.delete(dir);
						   }
					       return FileVisitResult.CONTINUE;
					   }
					});
				}
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	private static ClassLoader buildBootstrapClassLoader(String... jars) throws IOException {
		List<URL> urls = new ArrayList<URL>();
		for(String jarPath : jars) {
			URL osgi = CLIApp.class.getResource(jarPath);
			Path tempFile = Files.createTempFile(jarPath.replace('/', '-'), ".jar");
			Files.delete(tempFile);
			try(InputStream is = osgi.openConnection().getInputStream()) {
				Files.copy(is, tempFile);
			}
			urls.add(tempFile.toUri().toURL());
		}
		
		URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
		
		return cl;
	}
	
	private static ClassLoader buildOSGiClassLoader(String notesBin, String... jars) throws IOException {
		List<URL> urls = new ArrayList<URL>();
		for(String jarPath : jars) {
			URL osgi = CLIApp.class.getResource(jarPath);
			Path tempFile = Files.createTempFile(jarPath.replace('/', '-'), ".jar");
			Files.delete(tempFile);
			try(InputStream is = osgi.openConnection().getInputStream()) {
				Files.copy(is, tempFile);
			}
			urls.add(tempFile.toUri().toURL());
		}
		
		urls.add(Paths.get(notesBin, "jvm", "lib", "ext", "njempcl.jar").toUri().toURL());
		urls.add(Paths.get(notesBin, "jvm", "lib", "ext", "Notes.jar").toUri().toURL());
		
		URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
		
		return cl;
	}
}
