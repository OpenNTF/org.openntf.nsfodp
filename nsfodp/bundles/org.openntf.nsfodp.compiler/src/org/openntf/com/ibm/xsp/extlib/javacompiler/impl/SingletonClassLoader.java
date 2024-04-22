package org.openntf.com.ibm.xsp.extlib.javacompiler.impl;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * This {@link ClassLoader} implementation is intended to house an individual page class
 * so that it can be discarded as needed.
 * 
 * @author Jesse Gallagher
 * @since 2.0.4
 */
public class SingletonClassLoader extends URLClassLoader {
	public SingletonClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
	}

	public Class<?> defineClass(String qualifiedClassName, byte[] classData) {
		return defineClass(qualifiedClassName, classData, 0, classData.length);
	}
}
