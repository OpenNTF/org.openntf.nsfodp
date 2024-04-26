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
package org.openntf.com.ibm.xsp.extlib.javacompiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;

import org.openntf.com.ibm.xsp.extlib.javacompiler.impl.JavaFileObjectJavaCompiled;
import org.openntf.com.ibm.xsp.extlib.javacompiler.impl.JavaFileObjectJavaSource;
import org.openntf.com.ibm.xsp.extlib.javacompiler.impl.SingletonClassLoader;
import org.openntf.com.ibm.xsp.extlib.javacompiler.impl.SourceFileManager;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * Class loader that add classes from source files.
 * 
 * The added source files are compiled on the fly. The dependency classpath is passed
 * as a parameter.
 */
public class JavaSourceClassLoader extends ClassLoader implements AutoCloseable {
	
	public static final String JAVA_EXTENSION=JavaFileObject.Kind.SOURCE.extension;
	public static final String CLASS_EXTENSION=JavaFileObject.Kind.CLASS.extension;

	private final Map<String, JavaFileObjectJavaCompiled> classes = new ConcurrentHashMap<>();
	private final Map<String, Class<?>> definedClasses = new ConcurrentHashMap<>();
	private JavaCompiler javaCompiler;
	private List<String> options;
	private DiagnosticCollector<JavaFileObject> diagnostics;
	private SourceFileManager javaFileManager;
	private PrintStream out;
	
	private final URLClassLoader classPathLoader;
	private final Map<String, SingletonClassLoader> classNameClassLoaders = new ConcurrentHashMap<>();
	private boolean useSingletonClassLoaders = false;

	public JavaSourceClassLoader(ClassLoader parentClassLoader, List<String> compilerOptions, String[] classPath) {
		this(parentClassLoader, compilerOptions, classPath, true);
	}
	public JavaSourceClassLoader(ClassLoader parentClassLoader, List<String> compilerOptions, String[] classPath, boolean resolve) {
		super(parentClassLoader);
		this.options=compilerOptions;
		//this.javaCompiler=new EclipseCompiler();
		this.javaCompiler = Objects.requireNonNull(ToolProvider.getSystemJavaCompiler(), "Unable to create Java compiler");
		this.diagnostics=new DiagnosticCollector<JavaFileObject>();

		javaFileManager = createSourceFileManager(javaCompiler, diagnostics, classPath, resolve);
		
		URL[] urls = javaFileManager.getResolvedClassPath().stream()
			.map(url -> {
				try {
					String fullUrl;
					if(!url.contains("!/")) {
						fullUrl = url + "!/";
					} else {
						fullUrl = url;
					}
					return new URL(fullUrl);
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			})
			.toArray(URL[]::new);
		this.classPathLoader = new URLClassLoader(urls, parentClassLoader);
	}
	
	protected SourceFileManager createSourceFileManager(JavaCompiler javaCompiler, DiagnosticCollector<JavaFileObject> diagnostics, String[] classPath, boolean resolve) {
		StandardJavaFileManager standardJavaFileManager=javaCompiler.getStandardFileManager(diagnostics, null, null);
		return new SourceFileManager(standardJavaFileManager, JavaSourceClassLoader.this, classPath, resolve);
	}

	public void addCompiledFile(String qualifiedClassName, JavaFileObjectJavaCompiled classFile) {
		classes.put(qualifiedClassName, classFile);
	}

	public boolean isCompiledFile(String qualifiedClassName) {
		return classes.containsKey(qualifiedClassName);
	}
	
	/**
	 * Removes the provided class from the cache of compiled classes, if present.
	 * 
	 * @param qualifiedClassName the class name to remove
	 * @since 2.0.4
	 */
	public void purgeClass(String qualifiedClassName) {
		classes.remove(qualifiedClassName);
		getJavaFileManager().purgeSourceFile(StandardLocation.SOURCE_PATH, qualifiedClassName);
		classNameClassLoaders.remove(qualifiedClassName);
	}
	
	public SourceFileManager getJavaFileManager() {
		return javaFileManager;
	}
	
	public void setOutputStream(PrintStream out) {
		this.out = out;
	}
	
	public byte[] getClassByteCode(String qualifiedClassName) {
		if(isCompiledFile(qualifiedClassName)) {
			return classes.get(qualifiedClassName).getByteCode();
		} else {
			return null;
		}
	}

	@Override
	protected Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
		// Look if the class had already been compiled
		JavaFileObject file=classes.get(qualifiedClassName);
		if(file!=null) {
			return definedClasses.computeIfAbsent(qualifiedClassName, className -> {
				byte[] bytes=((JavaFileObjectJavaCompiled) file).getByteCode();
				if(useSingletonClassLoaders) {
					String cname = qualifiedClassName;
					int dollarIndex = cname.indexOf('$');
					if(dollarIndex > -1) {
						cname = cname.substring(0, dollarIndex);
					}
					SingletonClassLoader delegate = classNameClassLoaders.computeIfAbsent(cname, name -> new SingletonClassLoader(this));
					return delegate.defineClass(qualifiedClassName, bytes);
				} else {
					return defineClass(qualifiedClassName, bytes, 0, bytes.length);
				}
			});
		}
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6434149
		try {
			Class<?> c=Class.forName(qualifiedClassName, true, getParent());
			return c;
		} catch (ClassNotFoundException nf) {
		}
		
		// Look through the effective class path
		try {
			Class<?> c = Class.forName(qualifiedClassName, true, this.classPathLoader);
			return c;
		} catch(ClassNotFoundException nf) {
		}
		
		return super.findClass(qualifiedClassName);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		if(name.endsWith(CLASS_EXTENSION)) {
			String qualifiedClassName=name.substring(0, name.length()-CLASS_EXTENSION.length()).replace('/', '.');
			JavaFileObjectJavaCompiled file=classes.get(qualifiedClassName);
			if(file!=null) {
				return new ByteArrayInputStream(file.getByteCode());
			}
		}
		return super.getResourceAsStream(name);
	}

	public Class<?> addClass(String qualifiedClassName, CharSequence javaSource) throws JavaCompilerException {
		return addClass(qualifiedClassName, javaSource, null);
	}

	public synchronized Class<?> addClass(String qualifiedClassName, CharSequence javaSource, DiagnosticCollector<JavaFileObject> diagnosticsCollector)
			throws JavaCompilerException {
		Map<String, CharSequence> classes=Collections.singletonMap(qualifiedClassName, javaSource);
		Map<String, Class<?>> compiled=addClasses(classes, diagnosticsCollector);
		Class<?> newClass=compiled.get(qualifiedClassName);
		return newClass;
	}

	public Map<String, Class<?>> addClasses(Map<String, CharSequence> classes) throws JavaCompilerException {
		return addClasses(classes, null);
	}

	public synchronized Map<String, Class<?>> addClasses(final Map<String, CharSequence> classes, DiagnosticCollector<JavaFileObject> diagnosticsCollector)
			throws JavaCompilerException {
		if(diagnosticsCollector!=null) {
			diagnostics=diagnosticsCollector;
		} else {
			diagnostics=new DiagnosticCollector<JavaFileObject>();
		}
		final List<JavaFileObject> sources=new ArrayList<JavaFileObject>();
		for(Entry<String, CharSequence> entry : classes.entrySet()) {
			String qualifiedClassName=entry.getKey();
			CharSequence javaSource=entry.getValue();
			if(javaSource!=null&&javaSource.length()>0) {
				int dotPos=qualifiedClassName.lastIndexOf('.');
				String packageName=dotPos<0 ? "" : qualifiedClassName.substring(0, dotPos);
				String className=dotPos<0 ? qualifiedClassName : qualifiedClassName.substring(dotPos+1);
				String javaName=className+JAVA_EXTENSION;
				JavaFileObjectJavaSource source=new JavaFileObjectJavaSource(javaName, javaSource);
				sources.add(source);
				// Store the source file in the FileManager via package/class name.
				// For source files, we add a .java extension
				javaFileManager.addSourceFile(StandardLocation.SOURCE_PATH, packageName, javaName, source);
			}
		}
		Boolean result = AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
			CompilationTask task=javaCompiler.getTask(null, javaFileManager, diagnostics, options, null, sources);
			return task.call();
		});
		if(result==null||!result.booleanValue()) {
			List<Diagnostic<? extends JavaFileObject>> l=diagnostics.getDiagnostics();
			for(Diagnostic<? extends JavaFileObject> d : l) {
				println(d.toString());
			}
			throw new JavaCompilerException(null, diagnostics, "Compilation failed.");
		}
		try {
			Map<String, Class<?>> compiled=new HashMap<String, Class<?>>();
			for(String qualifiedClassName : classes.keySet()) {
				Class<?> newClass=JavaSourceClassLoader.this.loadClass(qualifiedClassName);
				compiled.put(qualifiedClassName, newClass);
			}
			return compiled;
		} catch (Throwable e) {
			throw new JavaCompilerException(e, diagnostics, "Error while loading the compiled classes");
		}
	}
	
	public Collection<String> getCompiledClassNames() {
		return Collections.unmodifiableCollection(classes.keySet());
	}
	
	/**
	 * Sets whether to use per-class classloaders. This isolates each class and any inner classes into an individual
	 * classloader, which allows for dynamic discarding of compiled classes. Set this to {@code false} (the default)
	 * to keep all classes within the same classloader.
	 * 
	 * @param useSingletonClassLoaders whether to use per-class classloaders
	 * @since 2.0.5
	 */
	public void setUseSingletonClassLoaders(boolean useSingletonClassLoaders) {
		this.useSingletonClassLoaders = useSingletonClassLoaders;
	}
	
	@Override
	public void close() {
		try {
			javaFileManager.close();
		} catch(Exception e) {
		}
		try {
			classPathLoader.close();
		} catch (IOException e) {
		}
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private void println(Object message) {
		if(this.out != null) {
			out.print(message);
		} else {
			System.out.println(message);
		}
	}
}
