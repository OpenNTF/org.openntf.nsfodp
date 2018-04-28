package org.openntf.nsfodp.eclipse.contentassist.model;

public class ComponentProperty {
	private final String name;
	private final String javaClassName;
	private final boolean required;
	private final String since;
	
	/**
	 * @param name
	 * @param javaClassName
	 */
	public ComponentProperty(String name, String javaClassName, boolean required, String since) {
		super();
		this.name = name;
		this.javaClassName = javaClassName;
		this.required = required;
		this.since = since;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the javaClassName
	 */
	public String getJavaClassName() {
		return javaClassName;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public String getSince() {
		return since;
	}
}
