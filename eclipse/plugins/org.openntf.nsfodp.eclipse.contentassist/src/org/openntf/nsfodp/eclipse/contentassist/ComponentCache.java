/**
 * Copyright © 2018-2019 Jesse Gallagher
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
package org.openntf.nsfodp.eclipse.contentassist;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.openntf.com.eclipsesource.json.Json;
import org.openntf.com.eclipsesource.json.JsonArray;
import org.openntf.com.eclipsesource.json.JsonObject;
import org.openntf.com.eclipsesource.json.JsonValue;
import org.openntf.domino.utils.xml.XMLDocument;
import org.openntf.nsfodp.eclipse.contentassist.model.ComponentProperty;
import org.openntf.nsfodp.eclipse.contentassist.model.CustomControl;
import org.openntf.nsfodp.eclipse.contentassist.model.StockComponent;
import org.xml.sax.SAXException;

public enum ComponentCache {
	;

	private static final Map<String, Collection<CustomControl>> CC_TAGS = Collections.synchronizedMap(new HashMap<>());
	private static Collection<StockComponent> STOCK_COMPONENTS;

	public static synchronized Collection<CustomControl> getCustomControls(IProject project)
			throws CoreException, SAXException, IOException, ParserConfigurationException {
		String id = project.getFullPath().toString();
		if (!CC_TAGS.containsKey(id)) {
			Set<CustomControl> result = new TreeSet<>();
			IFolder ccFolder = project.getFolder("odp/CustomControls"); // TODO look at configured path //$NON-NLS-1$
			if (ccFolder.exists()) {
				for (IResource member : ccFolder.members()) {
					if (member instanceof IFile) {
						if (member.getName().endsWith(".xsp-config")) { //$NON-NLS-1$
							// Then read in the XML
							XMLDocument doc = new XMLDocument();
							try (InputStream is = ((IFile) member).getContents()) {
								doc.loadInputStream(is);
							}
							String namespaceUri = doc
									.selectSingleNode("/faces-config/faces-config-extension/namespace-uri").getText(); //$NON-NLS-1$
							String prefix = doc.selectSingleNode("/faces-config/faces-config-extension/default-prefix") //$NON-NLS-1$
									.getText();
							String tagName = doc.selectSingleNode("/faces-config/composite-component/composite-name") //$NON-NLS-1$
									.getText();
							
							List<ComponentProperty> attributes = doc.selectNodes("/faces-config/composite-component/property")
									.stream()
									.map(prop -> {
										String name = prop.selectSingleNode("property-name").getText();
										String javaClass = prop.selectSingleNode("property-class").getText();
										return new ComponentProperty(name, javaClass, false, null);
									})
									.collect(Collectors.toList());
	
							result.add(new CustomControl(namespaceUri, prefix, tagName, attributes));
						}
					}
				}
			}
			CC_TAGS.put(id, result);
		}
		return CC_TAGS.get(id);
	}

	public static Collection<CustomControl> getCustomControls() throws CoreException, SAXException, IOException, ParserConfigurationException {
		return getCustomControls(ContentAssistUtil.getActiveProject());
	}
	
	public static synchronized Collection<StockComponent> getStockComponents() throws IOException {
		if(STOCK_COMPONENTS == null) {
			JsonValue value;
			try(InputStream is = ComponentCache.class.getResourceAsStream("/components/9.0.1fp10.json")) { //$NON-NLS-1$
				Objects.requireNonNull(is, "Could not load stock components JSON");
				try(Reader reader = new InputStreamReader(is)) {
					value = Json.parse(reader);
				}
			}
			if(!value.isObject()) {
				throw new IllegalStateException("Stock components JSON is not an object");
			}
			JsonObject obj = value.asObject();
			JsonValue componentsValue = obj.get("components"); //$NON-NLS-1$
			if(componentsValue == null || !componentsValue.isArray()) {
				throw new IllegalStateException("Stock components JSON does not contain a components array");
			}
			JsonArray components = componentsValue.asArray();
			STOCK_COMPONENTS = components.values().stream()
				.map(JsonValue::asObject)
				.map(ComponentCache::toStockComponent)
				.collect(Collectors.toList());
		}
		return STOCK_COMPONENTS;
	}
	
	private static StockComponent toStockComponent(JsonObject o) {
		String namespaceUri = o.getString("namespaceUri", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String defaultPrefix = o.getString("defaultPrefix", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String tagName = o.getString("tagName", ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		List<ComponentProperty> props = null;
		JsonValue propsValue = o.get("properties"); //$NON-NLS-1$
		if(propsValue != null && propsValue.isArray()) {
			props = propsValue.asArray().values().stream()
				.map(JsonValue::asObject)
				.map(ComponentCache::toComponentProperty)
				.collect(Collectors.toList());
		}
		
		return new StockComponent(namespaceUri, defaultPrefix, tagName, props);
	}
	
	private static ComponentProperty toComponentProperty(JsonObject o) {
		String name = o.getString("name", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String javaClass = o.getString("class", ""); //$NON-NLS-1$ //$NON-NLS-2$
		boolean required = o.getBoolean("required", false); //$NON-NLS-1$
		JsonValue sinceVal = o.get("since"); //$NON-NLS-1$
		String since = sinceVal.isString() ? sinceVal.asString() : null;
		return new ComponentProperty(name, javaClass, required, since);
	}
}
