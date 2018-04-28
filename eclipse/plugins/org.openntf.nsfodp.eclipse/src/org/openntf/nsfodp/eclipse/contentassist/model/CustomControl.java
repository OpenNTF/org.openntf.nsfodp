package org.openntf.nsfodp.compiler.eclipse.contentassist.model;

public class CustomControl implements Comparable<CustomControl> {
	private final String namespaceUri;
	private final String prefix;
	private final String tagName;
	
	public CustomControl(String namespaceUri, String prefix, String tagName) {
		this.namespaceUri = namespaceUri;
		this.prefix = prefix;
		this.tagName = tagName;
	}
	
	public String getNamespaceUri() {
		return namespaceUri;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getTagName() {
		return tagName;
	}

	@Override
	public int compareTo(CustomControl o) {
		String name = prefix + tagName;
		String oName = o.prefix + o.tagName;
		return name.compareTo(oName);
	}
}
