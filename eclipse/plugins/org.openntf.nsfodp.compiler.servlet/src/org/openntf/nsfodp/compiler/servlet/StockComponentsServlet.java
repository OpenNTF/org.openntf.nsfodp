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
package org.openntf.nsfodp.compiler.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.nsfodp.commons.LineDelimitedJsonProgressMonitor;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.io.json.JsonArray;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonObject;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.registry.FacesComponentDefinition;
import com.ibm.xsp.registry.FacesProperty;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.config.XspRegistryProvider;

public class StockComponentsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private JsonObject componentInfo;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setBufferSize(0);
		resp.setHeader("Content-Type", "text/json"); //$NON-NLS-1$ //$NON-NLS-2$
		
		ServletOutputStream os = resp.getOutputStream();

		try {
			if(componentInfo == null) {
				
				SharableRegistryImpl facesRegistry = new SharableRegistryImpl(getClass().getPackage().getName());
				List<Object> libraries = ExtensionManager.findServices((List<Object>)null, LibraryServiceLoader.class, "com.ibm.xsp.Library"); //$NON-NLS-1$
				libraries.stream()
					.filter(lib -> lib instanceof XspLibrary)
					.map(XspLibrary.class::cast)
					.map(lib -> new LibraryWrapper(lib.getLibraryId(), lib))
					.map(wrapper -> {
						SimpleRegistryProvider provider = new SimpleRegistryProvider();
						provider.init(wrapper);
						return provider;
					})
					.map(XspRegistryProvider::getRegistry)
					.forEach(facesRegistry::addDepend);
				facesRegistry.refreshReferences();
				
				componentInfo = new JsonJavaObject();
				JsonArray defs = new JsonJavaArray();
				facesRegistry.findComponentDefs().stream()
					.filter(FacesComponentDefinition::isTag)
					.map(def -> {
						try {
							JsonObject defObj = new JsonJavaObject();
							defObj.putJsonProperty("namespaceUri", def.getNamespaceUri()); //$NON-NLS-1$
							defObj.putJsonProperty("tagName", def.getTagName()); //$NON-NLS-1$
							JsonArray facetNames = new JsonJavaArray();
							for(String facetName : def.getFacetNames()) {
								facetNames.add(facetName);
							}
							defObj.putJsonProperty("facetNames", facetNames); //$NON-NLS-1$
							defObj.putJsonProperty("since", def.getSince()); //$NON-NLS-1$
							defObj.putJsonProperty("defaultPrefix", def.getFirstDefaultPrefix()); //$NON-NLS-1$
							defObj.putJsonProperty("componentFamily", def.getComponentFamily()); //$NON-NLS-1$
							defObj.putJsonProperty("componentType", def.getComponentType()); //$NON-NLS-1$
							defObj.putJsonProperty("id", def.getId()); //$NON-NLS-1$
							
							FacesProperty defaultProp = def.getDefaultFacesProperty();
							if(defaultProp != null) {
								JsonObject defaultPropObj = new JsonJavaObject();
								defaultPropObj.putJsonProperty("name", defaultProp.getName()); //$NON-NLS-1$
								defaultPropObj.putJsonProperty("since", defaultProp.getSince()); //$NON-NLS-1$
								defaultPropObj.putJsonProperty("class", defaultProp.getJavaClass().getName()); //$NON-NLS-1$
								defaultPropObj.putJsonProperty("required", defaultProp.isRequired()); //$NON-NLS-1$
								defaultPropObj.putJsonProperty("attribute", defaultProp.isAttribute()); //$NON-NLS-1$
							}
							
							JsonArray properties = new JsonJavaArray();
							for(String propName : def.getPropertyNames()) {
								FacesProperty prop = def.getProperty(propName);
								JsonObject propObj = new JsonJavaObject();
								propObj.putJsonProperty("name", propName); //$NON-NLS-1$
								propObj.putJsonProperty("class", prop.getJavaClass().getName()); //$NON-NLS-1$
								propObj.putJsonProperty("since", prop.getSince()); //$NON-NLS-1$
								propObj.putJsonProperty("required", prop.isRequired()); //$NON-NLS-1$
								propObj.putJsonProperty("attribute", prop.isAttribute()); //$NON-NLS-1$
								properties.add(propObj);
							}
							defObj.putJsonProperty("properties", properties); //$NON-NLS-1$
							
							
							return defObj;
						} catch (JsonException e) {
							throw new RuntimeException(e);
						}
					})
					.forEach(defObj -> {
						try {
							defs.add(defObj);
						} catch (JsonException e) {
							throw new RuntimeException(e);
						}
					});
				
				componentInfo.putJsonProperty("components", defs); //$NON-NLS-1$
			}
			os.print(componentInfo.toString());
		} catch(Throwable e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(baos);
			e.printStackTrace(out);
			out.flush();
			os.println(LineDelimitedJsonProgressMonitor.message(
				"type", "error", //$NON-NLS-1$ //$NON-NLS-2$
				"stackTrace", baos.toString() //$NON-NLS-1$
				)
			);
		}
	}
}
