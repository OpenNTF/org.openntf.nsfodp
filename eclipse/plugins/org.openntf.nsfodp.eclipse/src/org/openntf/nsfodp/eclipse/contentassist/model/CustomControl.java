/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.nsfodp.eclipse.contentassist.model;

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
