/*
 * � Copyright IBM Corp. 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package com.ibm.xsp.extlib.javacompiler.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.internal.adaptor.URLConverterImpl;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.Bundle;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;

/**
 * A JavaFileManager for Java source and classes consumed by the compiler.
 * 
 * @author priand
 */
public class SourceFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	// Use an OSGi service instead!
	private static final URLConverter urlConverter=new URLConverterImpl();

	private JavaSourceClassLoader classLoader;
	private Map<URI, JavaFileObjectJavaSource> fileObjects=new HashMap<URI, JavaFileObjectJavaSource>();
	
	private String[] resolvedClassPath;

	public SourceFileManager(JavaFileManager fileManager, JavaSourceClassLoader classLoader, String[] classPath, boolean resolve) {
		super(fileManager);
		this.classLoader=classLoader;
		if(resolve) {
			resolvedClassPath = resolveClasspath(classPath);
		} else {
			resolvedClassPath = classPath;
		}
	}
	
	public static String[] resolveClasspath(final String[] classPath) {
		return AccessController.doPrivileged(new PrivilegedAction<String[]>() {
			public String[] run() {
				try {
					if(classPath!=null) {
						ArrayList<String> resolved = new ArrayList<String>();
						for(int i=0; i<classPath.length; i++) {
							String cp = classPath[i];
							// Known protocols
							if(cp.startsWith("file:") || cp.startsWith("jar:")) {
								resolved.add(cp);
							} else {
								// Resolve a simple bundle to its URL
								resolveBundle(resolved,cp);
							}
						}
						if(resolved.size()>0) {
							return resolved.toArray(new String[resolved.size()]);
						}
					}
				} catch(IOException ex) {
					ex.printStackTrace();
				}
				return StringUtil.EMPTY_STRING_ARRAY;
			}
		});
	}
	private static void resolveBundle(ArrayList<String> resolved, String bundleName) throws IOException {
		Bundle b = org.eclipse.core.runtime.Platform.getBundle(bundleName);
		resolveBundle(resolved, b, true);
	}
	private static void resolveBundle(ArrayList<String> resolved, Bundle b, boolean includeFragments) throws IOException {
		if(b!=null) {
			File f = FileLocator.getBundleFile(b);
			
			// If it is a directory, then look inside
			if(f.isDirectory()) {
				// In dev mode, we have a bin/ directory that is not reflected in the Bundle-classpath
				// So we hard code it here
				File fbin = new File(f,"bin");
				if(fbin.exists() && fbin.isDirectory()) {
			        String path = fbin.toURI().toString();
			        resolved.add(path);
				}
				File fmaven = new File(f,"target/classes");
				if(fmaven.exists() && fmaven.isDirectory()) {
			        String path = fmaven.toURI().toString();
			        resolved.add(path);
				}
				Collection<String> classPath = getBundleClassPath(b,false);
				for(String cp: classPath) {
					if(StringUtil.isEmpty(cp)) {
						continue;
					}
				    File cpPath = new File(f,cp);
				    if(cpPath.exists()) {
				    	//resolveFile(resolved, path);
				        String path = cpPath.toURI().toString();
				        if(path.endsWith(".jar")) {
				            path = "jar:"+path;
				        }
				        resolved.add(path);
				    }
				}
				// Add the fragments separately as this needs a full File path
				if(includeFragments) {
			    	Bundle[] fragments = Platform.getFragments(b);
					if(fragments!=null) {
						for(int i=0; i<fragments.length; i++) {
							resolveBundle(resolved, fragments[i],false);
						}
					}
				}
			}
			
			// If it is a file, treat it as a jar file
			if(f.isFile()) {
				Collection<String> classPath = getBundleClassPath(b,includeFragments);
				// Make sure that this jar file is added, as this is not in Bundle-classpath when it is empty
				classPath.add(".");
				for(String cp: classPath) {
					if(StringUtil.isEmpty(cp)) {
						continue;
					}
					if(cp.equals(".")) {
			            String path = "jar:"+f.toURI().toString();
			            resolved.add(path);
			            continue;
					}
					URL u = b.getResource(cp);
					if(u!=null) {
						try {
							// We have to go through an intermediate File object ar url.toURI() has an issue
							// with path encoding. For example, spaces are not replaced by %20
							URL asFileUrl = urlConverter.toFileURL(u);
							String asUriString = (new File(asFileUrl.getFile()).toURI()).toString();
				            String url = "jar:"+asUriString;
				            resolved.add(url);
						} catch(Exception e) {e.printStackTrace();}
					}
				}
			}
		}
	}
    
    private static Collection<String> getBundleClassPath(Bundle b, boolean includeFragments) {
    	// Create a set to make sure that the same path is not added twice
    	// That breaks the order of the class loader, but this should not be a problem.
    	Set<String> classPath = new HashSet<String>();
    	gatherBundleClassPath(classPath, b);
    	if(includeFragments) {
	    	Bundle[] fragments = Platform.getFragments(b);
	    	if(fragments!=null) {
	    		for(int i=0; i<fragments.length; i++) {
	    			gatherBundleClassPath(classPath, fragments[i]);
	    		}
	    	}
    	}
    	return classPath;
    }
    private static void gatherBundleClassPath(Set<String> classPath, Bundle b) {
		Dictionary<String, String> header = b.getHeaders(); // "Bundle-ClassPath"
    	for(Enumeration<String> e=header.keys(); e.hasMoreElements(); ) {
    		String key = e.nextElement();
    		if(key.equals("Bundle-ClassPath")) {
    			String[] values = StringUtil.splitString(header.get(key),',',true);
    			for(int i=0; i<values.length; i++) {
    				String v = values[i];
    				if(StringUtil.isNotEmpty(v)) {
    					classPath.add(v);
    				}
    			}
    		}
    	}
    }

// TEST	
// Scan the existing jar files in the classpath...	
//	private void loadJarClasspath(ArrayList<String> classPath) throws IOException {
//		// Load all the jar from the class path
//		Enumeration<URL> e=classLoader.getResources("META-INF/MANIFEST.MF");
//		while(e.hasMoreElements()) {
//			URL url=e.nextElement();
//			String s = url.toExternalForm();
//			String sUrl=s.substring(0,s.length()-"META-INF/MANIFEST.MF".length());
//			if(sUrl.startsWith("file:") || sUrl.startsWith("jar:")) {
//				classPath.add(sUrl);
//			}
//		}
//	}
	
	@Override
	public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		try {
			URI uri = new URI(location.getName()+'/'+packageName+'/'+relativeName);
			JavaFileObjectJavaSource o=fileObjects.get(uri);
			if(o!=null) {
				return o;
			}
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
		return super.getFileForInput(location, packageName, relativeName);
	}

	public void addSourceFile(StandardLocation location, String packageName, String relativeName, JavaFileObjectJavaSource file) {
		try {
			URI uri = new URI(location.getName()+'/'+packageName+'/'+relativeName);
			fileObjects.put(uri, file);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind, FileObject outputFile) throws IOException {
		JavaFileObjectJavaCompiled file=new JavaFileObjectJavaCompiled(qualifiedName, kind);
		classLoader.addCompiledFile(qualifiedName, file);
		return file;
	}

	@Override
	public ClassLoader getClassLoader(JavaFileManager.Location location) {
		return classLoader;
	}

	@Override
	public String inferBinaryName(Location loc, JavaFileObject file) {
		if(file instanceof JavaFileObjectClass) {
			return ((JavaFileObjectClass) file).binaryName();
		}
		if(file instanceof JavaFileObjectJavaSource) {
			return file.getName();
		}
		return super.inferBinaryName(loc, file);
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		ArrayList<JavaFileObject> javaFiles=new ArrayList<JavaFileObject>();
		
		// We add all the file registered into the manager for source access
		if(location==StandardLocation.SOURCE_PATH) {
			if(kinds.contains(JavaFileObject.Kind.SOURCE)) {
				for(JavaFileObject file : fileObjects.values()) {
					if(file.getKind()==Kind.SOURCE && file.getName().startsWith(packageName)) {
						javaFiles.add(file);
					}
				}
			}
		}
		
		// We handle class files for compilation purposes from the classpath, identified by the content class loader
		if(location==StandardLocation.CLASS_PATH) {
			if(kinds.contains(JavaFileObject.Kind.CLASS)) {
				// java.* must come from the SystemClassLoader, so no reason to look
				// from our ClassLoader
				if(!packageName.startsWith("java.")) {
					listPackage(javaFiles,packageName);
				}
			}
		}
		// Particular case for the servlet classes
		if(location==StandardLocation.PLATFORM_CLASS_PATH) {
			if(kinds.contains(JavaFileObject.Kind.CLASS)) {
				// java.* must come from the SystemClassLoader, so no reason to look
				// from our ClassLoader
				if(packageName.startsWith("javax.servlet")) {
					listPackage(javaFiles,packageName);
					return javaFiles;
				}
			}
		}

		// And then what comes from the default implementation
		Iterable<JavaFileObject> result=super.list(location, packageName, kinds, recurse);
		if(javaFiles.isEmpty()) {
			return result;
		} else {
			for(JavaFileObject file : result) {
				javaFiles.add(file);
			}
			return javaFiles;
		}
	}

	private void listPackage(List<JavaFileObject> list, String packageName) throws IOException {
		String packagePath=StringUtil.replace(packageName, '.', '/');
		if(resolvedClassPath!=null) {
			for(int i=0; i<resolvedClassPath.length; i++) {
				String path = resolvedClassPath[i];
				if(path.startsWith("jar:")) {
					URL url = new URL(path+"!/"+packagePath);
					listPackageFromJarFile(list, packageName, url);
				} else {
					URL url = new URL(path+"/"+packagePath);
					File directory = new File(url.getFile());
					listPackageFromDirectory(list, packageName, directory);
				}
			}
		}
	}

	private void listPackageFromDirectory(List<JavaFileObject> list, String packageName, File directory) throws IOException {
		File[] files=directory.listFiles();
		if(files!=null) {
			for(int i=0; i<files.length; i++) {
				File file = files[i];
				if(file.isFile()) {
					String cName=file.getName();
					if(cName.endsWith(JavaSourceClassLoader.CLASS_EXTENSION)) {
						String binaryName=packageName+"."+removeClassExtension(cName);
						list.add(new JavaFileObjectClass(file.toURI(), binaryName));
					}
				}
			}
		}
	}

	private void listPackageFromJarFile(List<JavaFileObject> list, String packageName, URL url) throws IOException {
		try {
			String sUrl=url.toExternalForm();
			String jarPrefix=sUrl.substring(0,sUrl.lastIndexOf("!/")+2);

			JarURLConnection jarConn=(JarURLConnection) url.openConnection();
			String rootEntryName=jarConn.getEntryName();
			int rootEnd=rootEntryName.length()+1;

			for( Enumeration<JarEntry> e=jarConn.getJarFile().entries(); e.hasMoreElements(); ) {
				JarEntry entry=e.nextElement();
				String name=entry.getName();
				if(name.startsWith(rootEntryName) && name.indexOf('/',rootEnd)<0 && name.endsWith(JavaSourceClassLoader.CLASS_EXTENSION)) {
					//URI uri=(new URL(jarPrefix+name)).toURI();
					URI uri = new URI(jarPrefix+name);
					String binaryName=removeClassExtension(StringUtil.replace(name,'/', '.'));
					list.add(new JavaFileObjectClass(uri, binaryName));
				}
			}
		} catch (URISyntaxException e) {
			throw new IOException(StringUtil.format("Not able to open uri {0} as a jar file", url), e);
		} catch (FileNotFoundException e) {
			// The jar doesn't exists with this path, ignore....
		}
	}
	
	private static String removeClassExtension(String s) {
		return s.substring(0, s.length()-JavaSourceClassLoader.CLASS_EXTENSION.length());
	}
}
