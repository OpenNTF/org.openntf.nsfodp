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
package com.ibm.xsp.extlib.javacompiler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.ibm.xsp.extlib.javacompiler.impl.JavaFileObjectJavaCompiled;
import com.ibm.xsp.extlib.javacompiler.impl.JavaFileObjectJavaSource;
import com.ibm.xsp.extlib.javacompiler.impl.SourceFileManager;

/**
 * Class loader that add classes from source files.
 * 
 * The added source files are compiled on the fly. The dependency classpath is passed
 * as a parameter.
 */
public class JavaSourceClassLoader extends ClassLoader {
	
	public static final String JAVA_EXTENSION=JavaFileObject.Kind.SOURCE.extension;
	public static final String CLASS_EXTENSION=JavaFileObject.Kind.CLASS.extension;

	private Map<String, JavaFileObjectJavaCompiled> classes;
	private JavaCompiler javaCompiler;
	private List<String> options;
	private DiagnosticCollector<JavaFileObject> diagnostics;
	private SourceFileManager javaFileManager;
	private PrintStream out;
	
	private final ClassLoader classPathLoader;

	public JavaSourceClassLoader(ClassLoader parentClassLoader, List<String> compilerOptions, String[] classPath) {
		this(parentClassLoader, compilerOptions, classPath, true);
	}
	public JavaSourceClassLoader(ClassLoader parentClassLoader, List<String> compilerOptions, String[] classPath, boolean resolve) {
		super(parentClassLoader);
		this.classes=new HashMap<String, JavaFileObjectJavaCompiled>();
		this.options=compilerOptions;
		//this.javaCompiler=new EclipseCompiler();
		this.javaCompiler=ToolProvider.getSystemJavaCompiler();
		this.diagnostics=new DiagnosticCollector<JavaFileObject>();

		StandardJavaFileManager standardJavaFileManager=javaCompiler.getStandardFileManager(diagnostics, null, null);
		javaFileManager=new SourceFileManager(standardJavaFileManager, JavaSourceClassLoader.this, classPath, resolve);
		
		URL[] urls = Arrays.stream(javaFileManager.getResolvedClassPath())
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
				.collect(Collectors.toList()).toArray(new URL[0]);
		this.classPathLoader = new URLClassLoader(urls);
	}

	public void addCompiledFile(String qualifiedClassName, JavaFileObjectJavaCompiled classFile) {
		classes.put(qualifiedClassName, classFile);
	}

	public boolean isCompiledFile(String qualifiedClassName) {
		return classes.containsKey(qualifiedClassName);
	}
	
	public SourceFileManager getJavaFileManager() {
		return javaFileManager;
	}
	
	public void setOutputStream(PrintStream out) {
		this.out = out;
	}

	@Override
	protected Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
		// Look if the class had already been compiled
		JavaFileObject file=classes.get(qualifiedClassName);
		if(file!=null) {
			byte[] bytes=((JavaFileObjectJavaCompiled) file).getByteCode();
			return defineClass(qualifiedClassName, bytes, 0, bytes.length);
		}
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6434149
		try {
			Class<?> c=Class.forName(qualifiedClassName);
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
		Boolean result = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				CompilationTask task=javaCompiler.getTask(null, javaFileManager, diagnostics, options, null, sources);
				return task.call();
			}
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
/*
    // TESTS..
	public static void main(String[] args) {
		try {
			String javaSource="package mypackage;\n"+"import com.ibm.commons.util.StringUtil;\n"+"import com.ibm.commons.Platform;\n"
					+"public class MyClass implements Runnable {\n"+"  public void run() {\n"+"    Class<?> c = StringUtil.class;\n"
					+"    System.out.print(\"Dynamically compiled class!\"+c.toString());\n"+"  }\n"+"}\n";
			List<String> options=null; // Arrays.asList(new String[] {"-target",
										// "1.6" })
			JavaSourceClassLoader cl=new JavaSourceClassLoader(JavaSourceClassLoader.class.getClassLoader(), options, null);
			Class<?> c=cl.addClass("mypackage.MyClass", javaSource, null);
			Runnable r=(Runnable) c.newInstance();
			if(r!=null) {
				r.run();
			} else {
				System.out.println("Compilation error");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
*/	
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
