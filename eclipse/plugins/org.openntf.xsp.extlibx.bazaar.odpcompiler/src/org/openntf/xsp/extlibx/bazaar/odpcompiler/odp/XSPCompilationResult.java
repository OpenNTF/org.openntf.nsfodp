package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

/**
 * Represents the translated Java source and compiled Java code
 * from an XPage or Custom Control compilation.
 * 
 * @author Jesse Gallagher
 *
 */
public class XSPCompilationResult {
	private final String javaSource;
	private final Class<?> compiledClass;

	public XSPCompilationResult(String javaSource, Class<?> compiledClass) {
		this.javaSource = javaSource;
		this.compiledClass = compiledClass;
	}

	public String getJavaSource() {
		return javaSource;
	}

	public Class<?> getCompiledClass() {
		return compiledClass;
	}
	
	
}
