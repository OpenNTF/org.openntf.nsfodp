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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openntf.com.eclipsesource.json.Json;
import org.openntf.com.eclipsesource.json.JsonArray;
import org.openntf.com.eclipsesource.json.JsonObject;
import org.openntf.com.eclipsesource.json.JsonValue;
import org.openntf.nsfodp.lsp4xml.xsp.model.ComponentProperty;
import org.openntf.nsfodp.lsp4xml.xsp.model.StockComponent;

public enum ComponentCache {
	;

	private static Collection<StockComponent> STOCK_COMPONENTS;
	
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
