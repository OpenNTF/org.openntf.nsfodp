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
package org.openntf.nsfodp.lsp4xml.xsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.openntf.com.eclipsesource.json.Json;
import org.openntf.com.eclipsesource.json.JsonArray;
import org.openntf.com.eclipsesource.json.JsonObject;
import org.openntf.com.eclipsesource.json.JsonValue;
import org.openntf.domino.utils.xml.XMLDocument;
import org.openntf.nsfodp.lsp4xml.xsp.model.ComponentProperty;
import org.openntf.nsfodp.lsp4xml.xsp.model.CustomControl;
import org.openntf.nsfodp.lsp4xml.xsp.model.StockComponent;
import org.xml.sax.SAXException;

public enum ComponentCache {
	;

	private static final Map<String, Collection<CustomControl>> CC_TAGS = Collections.synchronizedMap(new HashMap<>());
	private static Collection<StockComponent> STOCK_COMPONENTS;

	public static synchronized Collection<CustomControl> getCustomControls(String documentUri)
			throws SAXException, IOException, ParserConfigurationException {
		return CC_TAGS.computeIfAbsent(documentUri, key -> {
			Path documentPath = Paths.get(URI.create(documentUri));
			
			// On-Disk Project
			// TODO look at configured path
			Path ccFolder = documentPath.getParent().getParent().resolve("CustomControls"); //$NON-NLS-1$
			if (Files.isDirectory(ccFolder)) {
				try {
					return readCustomControls(ccFolder);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			// Maven Webapp
			documentPath.getParent().getParent().resolve("controls"); //$NON-NLS-1$
			if(Files.isDirectory(ccFolder)) {
				try {
					return readCustomControls(ccFolder);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			return Collections.emptyList();
		});
	}
	
	private static Collection<CustomControl> readCustomControls(Path path) throws IOException {
		return Files.list(path)
			.filter(Files::isReadable)
			.filter(Files::isRegularFile)
			.filter(file -> file.toString().endsWith(".xsp-config")) //$NON-NLS-1$
			.map(file -> {
				XMLDocument doc = new XMLDocument();
				try (InputStream is = Files.newInputStream(file)) {
					doc.loadInputStream(is);
				} catch (IOException | SAXException | ParserConfigurationException e) {
					throw new RuntimeException(e);
				}
				String namespaceUri = doc
						.selectSingleNode("/faces-config/faces-config-extension/namespace-uri").getText(); //$NON-NLS-1$
				String prefix = doc.selectSingleNode("/faces-config/faces-config-extension/default-prefix") //$NON-NLS-1$
						.getText();
				String tagName = doc.selectSingleNode("/faces-config/composite-component/composite-name") //$NON-NLS-1$
						.getText();
				
				List<ComponentProperty> attributes = doc.selectNodes("/faces-config/composite-component/property") //$NON-NLS-1$
						.stream()
						.map(prop -> {
							String name = prop.selectSingleNode("property-name").getText(); //$NON-NLS-1$
							String javaClass = prop.selectSingleNode("property-class").getText(); //$NON-NLS-1$
							return new ComponentProperty(name, javaClass, false, null);
						})
						.collect(Collectors.toList());

				return new CustomControl(namespaceUri, prefix, tagName, attributes);
			})
			.collect(Collectors.toCollection(TreeSet::new));
	}
	
	public static synchronized Collection<StockComponent> getStockComponents() throws IOException {
		if(STOCK_COMPONENTS == null) {
			JsonValue value;
			try(InputStream is = ComponentCache.class.getResourceAsStream("/components/9.0.1fp10.json")) { //$NON-NLS-1$
				Objects.requireNonNull(is);
				try(Reader reader = new InputStreamReader(is)) {
					value = Json.parse(reader);
				}
			}
			if(!value.isObject()) {
				throw new IllegalStateException();
			}
			JsonObject obj = value.asObject();
			JsonValue componentsValue = obj.get("components"); //$NON-NLS-1$
			if(componentsValue == null || !componentsValue.isArray()) {
				throw new IllegalStateException();
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
