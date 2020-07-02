package org.openntf.nsfodp.example;

import org.apache.commons.lang3.SystemUtils;

/**
 * This is an example class.
 * 
 * @author Jesse Gallagher
 * @since 1.0
 */
public class ExampleClass {
	private String foo;
	
	// Class referenced in in-NSF Jar
	private com.example.embedded.ExampleClass inner = new com.example.embedded.ExampleClass();
	// Class inside a Maven dependency
	private boolean is8 = SystemUtils.IS_JAVA_1_8;
	// Class inside the plugin from the update site
	private org.openntf.nsfodp.example.Activator pluginActivator = org.openntf.nsfodp.example.Activator.getDefault();
	
	public String getFoo() {
		return foo;
	}
	
	public void setFoo(String foo) {
		this.foo = foo;
	}
}
