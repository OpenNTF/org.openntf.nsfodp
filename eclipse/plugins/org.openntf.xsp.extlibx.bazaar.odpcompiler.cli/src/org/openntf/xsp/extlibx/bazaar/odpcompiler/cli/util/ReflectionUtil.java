package org.openntf.xsp.extlibx.bazaar.odpcompiler.cli.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.cli.CLIApp;

/**
 * Utility methods for dealing with context objects reflectively
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum ReflectionUtil {
	;
	
	private static ClassLoader classLoader;
	
	public static Path createJempowerShim(Path notesBin) throws IOException {
		Path njempcl = notesBin.resolve("jvm").resolve("lib").resolve("ext").resolve("njempcl.jar");
		Path tempBundle = Files.createTempFile("njempcl", ".jar");
		try(OutputStream os = Files.newOutputStream(tempBundle)) {
			try(JarOutputStream jos = new JarOutputStream(os)) {
				JarEntry entry = new JarEntry("META-INF/MANIFEST.MF");
				jos.putNextEntry(entry);
				try(InputStream is = ReflectionUtil.class.getResourceAsStream("/res/COM.ibm.JEmpower/META-INF/MANIFEST.MF")) {
					copyStream(is, jos, 8192);
				}
				JarEntry njempclEntry = new JarEntry("lib/njempcl.jar");
				jos.putNextEntry(njempclEntry);
				try(InputStream is = Files.newInputStream(njempcl)) {
					copyStream(is, jos, 8192);
				}
			}
		}
		return tempBundle;
	}
	
	public static Path createXspBootstrapShim(Path notesBin) throws IOException {
		Path njempcl = notesBin.resolve("jvm").resolve("lib").resolve("ext").resolve("xsp.http.bootstrap.jar");
		Path tempBundle = Files.createTempFile("xsp.http.bootstrap", ".jar");
		try(OutputStream os = Files.newOutputStream(tempBundle)) {
			try(JarOutputStream jos = new JarOutputStream(os)) {
				JarEntry entry = new JarEntry("META-INF/MANIFEST.MF");
				jos.putNextEntry(entry);
				try(InputStream is = ReflectionUtil.class.getResourceAsStream("/res/com.ibm.domino.xsp.http.bootstrap/META-INF/MANIFEST.MF")) {
					copyStream(is, jos, 8192);
				}
				JarEntry njempclEntry = new JarEntry("lib/xsp.http.bootstrap.jar");
				jos.putNextEntry(njempclEntry);
				try(InputStream is = Files.newInputStream(njempcl)) {
					copyStream(is, jos, 8192);
				}
			}
		}
		return tempBundle;
	}
	
	/**
	 * Initializes the Equinox framework from the provided class loader.
	 * 
	 * @param cl a class loader able to find the EclipseStarter class
	 * @return the BundleContext object
	 * @throws IOException 
	 */
	public static Object init(ClassLoader cl) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		ReflectionUtil.classLoader = cl;
		
		Properties javaProfile = new Properties();
		try(InputStream is = cl.getResourceAsStream("JavaSE-1.8.profile")) {
			javaProfile.load(is);
		}
		String packages = javaProfile.getProperty("org.osgi.framework.system.packages");
		javaProfile.setProperty("org.osgi.framework.system.packages", packages + ",lotus.domino.*,lotus.notes.*");
		Path tempProfile = Files.createTempFile("javaProfile", ".profile");
		try(OutputStream os = Files.newOutputStream(tempProfile)) {
			javaProfile.store(os, "Temp java profile");
		}
		
		Class<?> eclipseStarter = classLoader.loadClass("org.eclipse.core.runtime.adaptor.EclipseStarter");
		
		Map<String, String> initialProperties = new HashMap<String, String>();
		initialProperties.put("osgi.compatibility.bootdelegation", "true");
		initialProperties.put("osgi.java.profile", tempProfile.toUri().toString());
		eclipseStarter.getMethod("setInitialProperties", Map.class).invoke(null, initialProperties);
		
		return eclipseStarter.getMethod("startup", String[].class, Runnable.class).invoke(null, new String[0], null);
	}
	
	/**
	 * Terminates the Equinox framework.
	 */
	public static void term() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> eclipseStarter = classLoader.loadClass("org.eclipse.core.runtime.adaptor.EclipseStarter");
		eclipseStarter.getMethod("shutdown").invoke(null);
	}
	
	/**
	 * Installs the provided bundle into the context.
	 * 
	 * @param bundleContext the BundleContext object
	 * @param url the URL to the bundle to install
	 * @return the installed Bundle object
	 */
	public static Object installBundle(Object bundleContext, String url) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		try {
			return bundleContext.getClass().getMethod("installBundle", String.class).invoke(bundleContext, url);
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter out = new PrintWriter(sw);
			e.printStackTrace(out);
			out.close();
			sw.close();
			if(sw.toString().contains("A bundle is already installed with the name")) {
				// Ignore and find the original
				Object[] bundles = (Object[])bundleContext.getClass().getMethod("getBundles").invoke(bundleContext);
				for(Object obj : bundles) {
					String name = (String)obj.getClass().getMethod("getSymbolicName").invoke(obj);
					Object version = obj.getClass().getMethod("getVersion").invoke(obj);
					if(url.contains(name + "_" + version)) {
						return obj;
					}
				}
				return null;
			} else {
				throw new RuntimeException(e);
			}
 		}
	}
	
	public static void startBundle(Object bundle) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		@SuppressWarnings("unchecked")
		Dictionary<String, String> headers = (Dictionary<String, String>)bundle.getClass().getMethod("getHeaders").invoke(bundle);
		if(headers.get("Eclipse-SourceBundle") == null && headers.get("Fragment-Host") == null) {
			try {
				bundle.getClass().getMethod("start").invoke(bundle);
			} catch(Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter out = new PrintWriter(sw);
				e.printStackTrace(out);
				out.close();
				sw.close();
				if(sw.toString().contains("Another singleton bundle selected")) {
					// Ignore
				} else {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static Object loadBundle(Object bundleContext, Path path) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		Object bundle = installBundle(bundleContext, path.toUri().toString());
		startBundle(bundle);
		return bundle;
	}
	
	public static void loadEmbeddedBundles(Object bundleContext, String... resPaths) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<Object> bundles = new ArrayList<>();
		for(String resPath : resPaths) {
			Path tempFile = Files.createTempFile(resPath.replace('/', '-'), ".jar");
			Files.delete(tempFile);
			try(InputStream is = ReflectionUtil.class.getResourceAsStream(resPath)) {
				Files.copy(is, tempFile);
			}
			bundles.add(installBundle(bundleContext, tempFile.toUri().toString()));
		}
		
		for(Object bundle : bundles) {
			startBundle(bundle);
		}
	}
	
	/**
	 * Installs and starts all bundles in the provided path.
	 * 
	 * @param bundleContext the BundleContext object
	 * @param path a path containing OSGi bundles to load
	 */
	public static void loadBundles(Object bundleContext, Path path) throws IOException {
		Files.find(path, 1, (file, attr) -> file.getFileName().toString().endsWith(".jar"))
			.map(bundle -> {
				try {
					return installBundle(bundleContext, bundle.toUri().toString());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException | IOException e) {
					throw new RuntimeException(e);
				}
			})
			.collect(Collectors.toList()).stream()
			.forEach(bundle -> {
				try {
					startBundle(bundle);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException | IOException e) {
					throw new RuntimeException(e);
				}
			});
	}
	
	public static void loadCurrentBundle(Object bundleContext) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		String thisJar = ReflectionUtil.class.getProtectionDomain().getCodeSource().getLocation().toString();
		Object bundle = installBundle(bundleContext, thisJar);
		startBundle(bundle);
	}
	
    private static long copyStream(InputStream is, OutputStream os, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		long totalBytes = 0;
		int readBytes;
		while( (readBytes = is.read(buffer))>0 ) {
			os.write(buffer, 0, readBytes);
			totalBytes += readBytes;
		}
		return totalBytes;
    }
    
	public static Map<String, String> getLaunchParams(ClassLoader cl) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException, ClassNotFoundException {
		Class<?> optionsClass = cl.loadClass("org.apache.commons.cli.Options");
		Object options = optionsClass.newInstance();
		Method addRequiredOption = options.getClass().getMethod("addRequiredOption", String.class, String.class, boolean.class, String.class);
		addRequiredOption.invoke(options, "notesBin", null, true, "Notes binary directory");
		addRequiredOption.invoke(options, "pluginsDir", null, true, "OSGi plugins directory");
		addRequiredOption.invoke(options, "odp", null, true, "On-disk project path");
		addRequiredOption.invoke(options, "updateSite", null, true, "App-specific update site");
		
		Object defaultParser = cl.loadClass("org.apache.commons.cli.DefaultParser").newInstance();
		Method parse = defaultParser.getClass().getMethod("parse", optionsClass, String[].class);
		
		Object cmd = parse.invoke(defaultParser, options, CLIApp.argv);

		Map<String, String> result = new HashMap<>();
		
		Method getOptionValue = cmd.getClass().getMethod("getOptionValue", String.class);
		result.put("notesBin", (String)getOptionValue.invoke(cmd, "notesBin"));
		result.put("pluginsDir", (String)getOptionValue.invoke(cmd, "pluginsDir"));
		result.put("odp", (String)getOptionValue.invoke(cmd, "odp"));
		result.put("updateSite", (String)getOptionValue.invoke(cmd, "updateSite"));
		return result;
	}
}
