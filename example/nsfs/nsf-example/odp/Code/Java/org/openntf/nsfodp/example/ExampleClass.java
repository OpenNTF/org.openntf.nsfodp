package org.openntf.nsfodp.example;

/**
 * This is an example class.
 * 
 * @author Jesse Gallagher
 * @since 1.0
 */
public class ExampleClass {
	private String foo;
	private com.example.embedded.ExampleClass inner = new com.example.embedded.ExampleClass();
	
	public String getFoo() {
		return foo;
	}
	
	public void setFoo(String foo) {
		this.foo = foo;
	}
}
