package org.openntf.nsfodp.eclipse.contentassist.model;

public abstract class AbstractComponent<T extends AbstractComponent<T>> implements Comparable<T> {
	private final String namespaceUri;
	private final String prefix;
	private final String tagName;
	
	public AbstractComponent(String namespaceUri, String prefix, String tagName) {
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
	
	public String getPrefixedName() {
		return getPrefix() + ':' + getTagName();
	}
	
	@Override
	public int compareTo(T o) {
		String name = prefix + tagName;
		String oName = o.getPrefix() + o.getTagName();
		return name.compareTo(oName);
	}
}
