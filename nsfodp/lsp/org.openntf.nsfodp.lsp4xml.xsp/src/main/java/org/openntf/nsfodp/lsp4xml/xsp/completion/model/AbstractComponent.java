/**
 * Copyright © 2018-2023 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.nsfodp.lsp4xml.xsp.completion.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractComponent<T extends AbstractComponent<T>> implements Comparable<T> {
	private final String namespaceUri;
	private final String prefix;
	private final String tagName;
	private final Collection<ComponentProperty> properties;
	
	public AbstractComponent(String namespaceUri, String prefix, String tagName, Collection<ComponentProperty> properties) {
		this.namespaceUri = namespaceUri;
		this.prefix = prefix;
		this.tagName = tagName;
		if(properties == null) {
			this.properties = Collections.emptyList();
		} else {
			this.properties = Collections.unmodifiableList(new ArrayList<>(properties));
		}
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
	
	public Collection<ComponentProperty> getProperties() {
		return properties;
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
