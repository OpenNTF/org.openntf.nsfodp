/**
 * Copyright © 2018-2020 Jesse Gallagher
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
package servlet.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.FacesListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.Format;
import com.ibm.commons.xml.XMLException;
import com.ibm.xsp.actions.ActionGroup;
import com.ibm.xsp.extlib.tree.complex.ComplexContainerTreeNode;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.registry.AbstractContainerProperty;
import com.ibm.xsp.registry.FacesComplexDefinition;
import com.ibm.xsp.registry.FacesComponentDefinition;
import com.ibm.xsp.registry.FacesContainerProperty;
import com.ibm.xsp.registry.FacesDefinition;
import com.ibm.xsp.registry.FacesProperty;
import com.ibm.xsp.registry.FacesSimpleProperty;
import com.ibm.xsp.registry.FacesValidatorDefinition;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.config.XspRegistryProvider;

@SuppressWarnings("nls")
public class AbstractSchemaServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String NS_XP = "http://www.ibm.com/xsp/core";
	
	private static final Map<String, String> extMap = new HashMap<>();
	static {
		extMap.put("http://www.ibm.com/xsp/jsf/core", "f");
		extMap.put(NS_XP, "xp");
		extMap.put("http://www.ibm.com/xsp/coreex", "xe");
	}
	
	private final String namespace;
	private final String[] imports;
	private final Map<String, Document> xspConfigs = new HashMap<>();

	public AbstractSchemaServlet(String namespace, String... imports) {
		super();
		this.namespace = namespace;
		this.imports = imports;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			Document doc = DOMUtil.createDocument(namespace, "xs:schema");
			doc.setXmlStandalone(true);
			Element schema = doc.getDocumentElement();
			schema.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
			schema.setAttribute("xmlns:" + extMap.get(namespace), namespace);
			schema.setAttribute("targetNamespace", namespace);
			schema.setAttribute("elementFormDefault", "qualified");
			
			for(String importUri : this.imports) {
				schema.setAttribute("xmlns:" + extMap.get(importUri), importUri);
				
				Element importEl = DOMUtil.createElement(doc, schema, "xs:import");
				importEl.setAttribute("namespace", importUri);
//				importEl.setAttribute("schemaLocation", URI.create(req.getRequestURL().toString()).resolve(extMap.get(importUri) + ".xsd").toString());
				importEl.setAttribute("schemaLocation", extMap.get(importUri) + ".xsd");
			}
			
			outSimpleTypes(schema);
			
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
			
			@SuppressWarnings("unchecked")
			List<FacesDefinition> defs = (List<FacesDefinition>)(List<?>)facesRegistry.findDefs();
			defs.stream()
				.filter(FacesDefinition::isTag)
				.filter(def -> StringUtil.equals(namespace, def.getNamespaceUri()))
				.forEach(def -> outComponentDefinition(def, schema, facesRegistry, false));
			
			resp.setContentType("text/xml");
			DOMUtil.serialize(resp.getOutputStream(), doc, new Format(2, true, "UTF-8"));
		} catch (Throwable e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
			resp.setContentType("text/plain");
			e.printStackTrace(new PrintWriter(resp.getOutputStream()));
			resp.getOutputStream().flush();
			e.printStackTrace();
		}
	}
	
	private void outSimpleTypes(Element schema) {
		Document doc = schema.getOwnerDocument();
		
		// Common run+load-time types
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrInteger");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			pattern.setAttribute("value", "(\\d+)|([#$]\\{[.\\s\\S]*\\})");
		}
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrBoolean");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			pattern.setAttribute("value", "(true)|(false)|([#$]\\{[.\\s\\S]*\\})");
		}
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrDecimal");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			pattern.setAttribute("value", "(\\d+(\\.\\d+)?)|([#$]\\{[.\\s\\S]*\\})");
		}
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrTime");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			// TODO figure out if this can enforce a time format
			pattern.setAttribute("value", "([.\\s\\S]*)|([#$]\\{[.\\s\\S]*\\})");
		}
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrDate");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			// TODO figure out if this can enforce a date format
			pattern.setAttribute("value", "([.\\s\\S]*)|([#$]\\{[.\\s\\S]*\\})");
		}
		
		// Binding-only types
		{
			// TODO allow text before and/or after this, but require a binding.
			//    May have to be done programmatically
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrBinding");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			pattern.setAttribute("value", "[#$]\\{[.\\s\\S]*\\}");
		}
		// String or load-time binding
		{
			// TODO this may have to be enforced programmatically, so this doesn't work for now
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrLoadBinding");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			// TODO allow text before and/or after this, but require a binding
			pattern.setAttribute("value", "$\\{[.\\s\\S]*\\}");
		}
		
		// For xp: only, output a reusable xp:key attribute
		{
			Element attribute = DOMUtil.createElement(doc, schema, "xs:attribute");
			attribute.setAttribute("name", "key");
			attribute.setAttribute("type", "xs:string");
		}
	}
	
	private void outComponentDefinition(FacesDefinition def, Element schema, SharableRegistryImpl registry, boolean noTag) {
		Document doc = schema.getOwnerDocument();
		
		String name = toElementName(def, noTag);
		// Check if one already exists, as shows up with com.ibm.xsp.extlib.component.dojo.UIDojoWidgetBase
		try {
			if(DOMUtil.nodes(schema, "*[@name=\"" + name + "\"]").length > 0) {
				return;
			}
		} catch (XMLException e) {
			throw new RuntimeException(e);
		}
		
		Element complexType = DOMUtil.createElement(doc, schema, "xs:complexType");
		complexType.setAttribute("name", name);
		// UIComponents can have any children, while bindings and resources are special cases with similar needs
		if(
			UIComponent.class.isAssignableFrom(def.getJavaClass())
			|| MethodBinding.class.equals(def.getJavaClass())
			|| ValueBinding.class.equals(def.getJavaClass())
		) {
			complexType.setAttribute("mixed", "true");
		}
		
		Element all = DOMUtil.createElement(doc, complexType, "xs:choice");
		all.setAttribute("minOccurs", "0");
		all.setAttribute("maxOccurs", "unbounded");
		
		outProperties(def, complexType, registry);

		if(UIComponent.class.isAssignableFrom(def.getJavaClass())) {
			// Declare that it allows any children
			DOMUtil.createElement(doc, all, "xs:any").setAttribute("processContents", "lax");
		}
		
		outAnnotations(def, complexType);
		
		if(!noTag) {
			// Output the concrete definition
			Element element = DOMUtil.createElement(doc, schema, "xs:element");
			element.setAttribute("name", def.getTagName());
			String prefix = extMap.get(def.getNamespaceUri()) + ':';
			element.setAttribute("type", prefix + def.getTagName());
			outAnnotations(def, element);
		}
	}

	
	private void outAnnotations(FacesDefinition def, Element element) {
		Document doc = element.getOwnerDocument();
		Element annotation = doc.createElement("xs:annotation");
		if(element.getFirstChild() != null) {
			element.insertBefore(annotation, element.getFirstChild());
		} else {
			element.appendChild(annotation);
		}
		
		Element component = findDefinitionElement(def);
		{
			Element appinfo = DOMUtil.createElement(doc, annotation, "xs:appinfo");
			if(component != null) {
				appinfo.setTextContent(component.getElementsByTagName("display-name").item(0).getTextContent());
			} else {
				appinfo.setTextContent(def.getTagName());
			}
		}
		{
			Element documentation = DOMUtil.createElement(doc, annotation, "xs:documentation");
			documentation.setAttribute("source", "version");
			String since = def.getSince();
			if(StringUtil.isEmpty(since)) {
				since = "8.5.0";
			}
			documentation.setTextContent(since + "+");
		}
		{
			// This appears to be treated as HTML
			Element documentation = DOMUtil.createElement(doc, annotation, "xs:documentation");
			documentation.setAttribute("source", "description");
			
			StringBuilder description = new StringBuilder();

			if(component != null) {
				p(description, component.getElementsByTagName("description").item(0).getTextContent());
			}
			
			if(def instanceof FacesComponentDefinition) {
				FacesComponentDefinition fcd = (FacesComponentDefinition)def;
				
				Collection<String> facets = fcd.getFacetNames();
				if(facets != null && !facets.isEmpty()) {
					p(description, "Facets:");
					description.append("<ul>");
					facets.stream()
						.map(f -> "<li>" + f + "</li>")
						.forEach(description::append);
					description.append("</ul>");
				}
				dt(description, "Component Type", fcd.getComponentType());
				dt(description, "Component Family", fcd.getComponentFamily());
				dt(description, "Renderer Type", fcd.getRendererType());
			} else if(def instanceof FacesComplexDefinition) {
				
				dt(description, "Complex ID", ((FacesComplexDefinition)def).getComplexId());
				if(def instanceof FacesValidatorDefinition) {
					dt(description, "Validator ID", ((FacesValidatorDefinition)def).getValidatorId());
				}
			}
			if(component != null) {
				try {
					Object[] catNodes = DOMUtil.nodes(component, "*/designer-extension/category/text()");
					if(catNodes.length > 0) {
						dt(description, "Group", ((Node)catNodes[0]).getTextContent());
					}
				} catch(XMLException e) {
					throw new RuntimeException(e);
				}
			}
			dt(description, "Class", def.getJavaClass().getName());
			
			documentation.setTextContent(description.toString());
		}
	}
	
	private static StringBuilder dt(StringBuilder stringBuilder, Object name, Object value) {
		stringBuilder.append("<p><b>");
		stringBuilder.append(name);
		stringBuilder.append(":</b> ");
		stringBuilder.append(value);
		stringBuilder.append("</p>");
		return stringBuilder;
	}
	
	private static StringBuilder p(StringBuilder stringBuilder, Object... value) {
		stringBuilder.append("<p>");
		for(Object val : value) {
			stringBuilder.append(val);
		}
		stringBuilder.append("</p>");
		return stringBuilder;
	}
	
	private Document getXspConfig(String filePath) {
		return xspConfigs.computeIfAbsent(PathUtil.concat("/", filePath, '/'), key -> {
			try(InputStream is = getClass().getResourceAsStream(key)) {
				if(is != null) {
					Document result = DOMUtil.createDocument(is);
					
					// Find a translation file
					if(key.endsWith(".xsp-config")) {
						String propFilePath = key.substring(0, key.length()-".xsp-config".length())+".properties";
						try(InputStream propIs = getClass().getResourceAsStream(propFilePath)) {
							if(propIs != null) {
								Properties props = new Properties();
								props.load(propIs);
								
								Stream.of(DOMUtil.nodes(result, "//*[starts-with(text(),'%')]"))
									.filter(Element.class::isInstance)
									.map(Element.class::cast)
									.forEach(el -> {
										String text = el.getTextContent();
										if(text.endsWith("%")) {
											String prop = text.substring(1, text.length()-1);
											String translated = props.getProperty(prop, text);
											el.setTextContent(translated);
										}
									});
							}
						}
					}
					
					return result;
				} else {
					return null;
				}
			} catch(IOException | XMLException e) {
				e.printStackTrace();
				// Ignore
				return null;
			}
		});
	}
	
	private void outProperties(FacesDefinition def, Element element, SharableRegistryImpl registry) {
		Collection<String> names = def.getPropertyNames();
		for(String propName : names) {
			FacesProperty prop = def.getProperty(propName);
			outProperty(def, prop, element, registry);
		}
		
		// Everything allows id and key (to cover for facets)
		// TODO see if xp:key can be restricted to when it's immediately under a facets tag
		if(!names.contains("id")) {
			Element all = (Element)element.getFirstChild();
			Element thisId = DOMUtil.createElement(element.getOwnerDocument(), all, "xs:element");
			thisId.setAttribute("name", "this.id");
			thisId.setAttribute("type", extMap.get(this.namespace) + ":attrLoadBinding");
			Element attrId = DOMUtil.createElement(element.getOwnerDocument(), element, "xs:attribute");
			attrId.setAttribute("name", "id");
			attrId.setAttribute("type", "xs:ID");
		}
		if(!names.contains("xp:key")) {
			Element attrId = DOMUtil.createElement(element.getOwnerDocument(), element, "xs:attribute");
			attrId.setAttribute("ref", "xp:key");
		}
		
		// Special handling for xp:actionGroup et al to allow for actions without this.actions
		if(isWorkaroundContainerType(def)) {
			Element all = (Element)element.getFirstChild();
			DOMUtil.createElement(element.getOwnerDocument(), all, "xs:any").setAttribute("processContents", "lax");
		}
		
//		if(names.isEmpty()) {
//			// Add an xs:anyAttribute here to make this extensible down the line
//			DOMUtil.createElement(element.getOwnerDocument(), element, "xs:anyAttribute");
//		}
	}
	
	private void outProperty(FacesDefinition def, FacesProperty prop, Element element, SharableRegistryImpl registry) {
		// TODO check run/load binding from FacesSimpleProperty
		Class<?> clazz = prop.getJavaClass();
		if(prop instanceof AbstractContainerProperty) {
			clazz = ((AbstractContainerProperty)prop).getItemProperty().getJavaClass();
		}
		if(isBindingType(prop)) {
			outAttribute(def, prop, element, extMap.get(this.namespace) + ":attrBinding", false);
		} else if(byte.class.equals(clazz) || short.class.equals(clazz) || int.class.equals(clazz) || long.class.equals(clazz)
			|| Byte.class.equals(clazz) || Short.class.equals(clazz) || Integer.class.equals(clazz) || Long.class.equals(clazz)) {
			outAttribute(def, prop, element, extMap.get(namespace) + ":attrInteger", false);
		} else if(boolean.class.equals(clazz) || Boolean.class.equals(clazz)) {
			outAttribute(def, prop, element, extMap.get(namespace) + ":attrBoolean", false);
		} else if(float.class.equals(clazz) || double.class.equals(clazz) || Number.class.isAssignableFrom(clazz)) {
			outAttribute(def, prop, element, extMap.get(namespace) + ":attrDecimal", false);
		} else if(LocalTime.class.isAssignableFrom(clazz)) {
			outAttribute(def, prop, element, extMap.get(namespace) + ":attrTime", false);
		} else if(Date.class.equals(clazz) || TemporalAccessor.class.isAssignableFrom(clazz)) {
			outAttribute(def, prop, element, extMap.get(namespace) + ":attrDate", false);
		} else if("id".equals(prop.getName())) {
			outAttribute(def, prop, element, "xs:ID", false);
		} else if(CharSequence.class.isAssignableFrom(clazz)) {
			outAttribute(def, prop, element, "xs:string", false);
		} else if(Object.class.equals(clazz) || FacesListener.class.isAssignableFrom(clazz)) {
			// FacesListeners lead to an xp: prefix below if left unhandled
			
			outAttribute(def, prop, element, "xs:string", false);
		} else {
			// Figure out if it's complex or not
			Class<?> finalClazz = clazz;
			FacesDefinition propDef = registry.findDefs().stream()
				.filter(def2 -> def2.getJavaClass().equals(finalClazz))
				.findFirst()
				.orElse(null);
			if(propDef != null) {
				outAttribute(def, prop, element, extMap.get(propDef.getNamespaceUri()) + ':' + toElementName(propDef, false), true);
			} else {
				outAttribute(def, prop, element, "xs:string", false);
			}
		}
	}
	
	private void outAttribute(FacesDefinition def, FacesProperty prop, Element element, String type, boolean complex) {
		Document doc = element.getOwnerDocument();
		
		Element all = (Element)element.getFirstChild();
		
		// Create a this.child child
		Element thisElement = DOMUtil.createElement(doc, all, "xs:element");
		thisElement.setAttribute("name", "this." + prop.getName());
		if(complex || isBindingType(prop)) {
			Element complexType = DOMUtil.createElement(doc, thisElement, "xs:complexType");
			if(isBindingType(prop)) {
				// Bindings are a mess
				complexType.setAttribute("mixed", "true");
			}
			Element sequence = DOMUtil.createElement(doc, complexType, "xs:choice");
			if(prop instanceof AbstractContainerProperty) {
				// Then mark its upper bound as unlimited
				sequence.setAttribute("maxOccurs", "unbounded");
			}
			sequence.setAttribute("minOccurs", "0");
			Element any = DOMUtil.createElement(doc, sequence, "xs:any");
			any.setAttribute("maxOccurs", "unbounded");
			any.setAttribute("processContents", "lax");
		} else {
			thisElement.setAttribute("type", type);
		}
		outAnnotations(def, prop, thisElement);
		
		Element attribute = DOMUtil.createElement(doc, element, "xs:attribute");
		attribute.setAttribute("name", prop.getName());
		if(complex) {
			// Attributes have to be simple
			attribute.setAttribute("type", extMap.get(namespace) + ":attrBinding");
		} else {
			attribute.setAttribute("type", type);
		}
		// TODO see if we can match either an attr or a this. child
//		if(prop.isRequired()) {
//			attribute.setAttribute("use", "required");
//		}
		
		outAnnotations(def, prop, attribute);
	}
	
	private void outAnnotations(FacesDefinition def, FacesProperty prop, Element element) {
		Document doc = element.getOwnerDocument();
		Element annotation = doc.createElement("xs:annotation");
		if(element.getFirstChild() != null) {
			element.insertBefore(annotation, element.getFirstChild());
		} else {
			element.appendChild(annotation);
		}
		Element propElement = findDefinitionElement(def, prop);
		{
			Element appinfo = DOMUtil.createElement(doc, annotation, "xs:appinfo");
			if(propElement != null) {
				appinfo.setTextContent(propElement.getElementsByTagName("display-name").item(0).getTextContent());
			} else {
				appinfo.setTextContent(prop.getName());
			}
		}
		{
			Element documentation = DOMUtil.createElement(doc, annotation, "xs:documentation");
			documentation.setAttribute("source", "version");
			String since = prop.getSince();
			if(StringUtil.isEmpty(since)) {
				since = def.getSince();
				if(StringUtil.isEmpty(since)) {
					since = "8.5.0";
				}
			}
			documentation.setTextContent(since + "+");
		}
		{
			// This appears to be treated as HTML
			Element documentation = DOMUtil.createElement(doc, annotation, "xs:documentation");
			documentation.setAttribute("source", "description");
			
			StringBuilder description = new StringBuilder();

			if(propElement != null) {
				p(description, propElement.getElementsByTagName("description").item(0).getTextContent());
				
				description.append("<dl>");
				try {
					Object[] catNodes = DOMUtil.nodes(propElement, "property-extension/designer-extension/category/text()");
					if(catNodes.length > 0) {
						dt(description, "Property Group", ((Node)catNodes[0]).getTextContent());
					}
				} catch(XMLException e) {
					throw new RuntimeException(e);
				}
			} else {
				description.append("<dl>");
			}
			
			String className = prop.getJavaClass().getName();
			if(prop instanceof FacesContainerProperty) {
				FacesContainerProperty fcp = (FacesContainerProperty)prop;
				if(fcp.isCollection()) {
					className += "&lt;" + fcp.getItemProperty().getJavaClass().getName() + "&gt;";
				}
			}
			dt(description, "Class", className);
			
			// Expected to be opened in every if condition
			description.append("</dl>");
			
			documentation.setTextContent(description.toString());
		}
	}
	
	
	private String toElementName(FacesDefinition def, boolean noTag) {
		String name;
		String tagName = def.getTagName();
		if(StringUtil.isNotEmpty(tagName) && !noTag) {
			name = tagName;
		} else {
			name = def.getJavaClass().getName();
		}
		return name;
	}
	
	private boolean isBindingType(FacesProperty prop) {
		if(MethodBinding.class.isAssignableFrom(prop.getJavaClass())
			|| ValueBinding.class.isAssignableFrom(prop.getJavaClass())) {
			return true;
		}
		if(prop instanceof FacesSimpleProperty) {
			FacesSimpleProperty simpleProp = (FacesSimpleProperty)prop;
			return !simpleProp.isAllowNonBinding();
		}
		return false;
	}
	
	/**
	 * Finds the XML element for the given definition in its original .xsp-config file.
	 * 
	 * @param def the {@link FacesDefinition} to search for
	 * @return an {@link Element} describing the definition, or {@code null} if it cannot be found
	 */
	private Element findDefinitionElement(FacesDefinition def) {
		String filePath = def.getFile().getFilePath();
		Document xspConfig = getXspConfig(filePath);
		if(xspConfig != null) {
			try {
				String elementName;
				if(def instanceof FacesComplexDefinition) {
					elementName = "complex-class";
				} else if(def instanceof FacesComponentDefinition) {
					elementName = "component-class";
				} else {
					return null;
				}
				Object[] elements = DOMUtil.nodes(xspConfig, "/faces-config/*/" + elementName + "[normalize-space(text())='" + def.getJavaClass().getName() + "']");
				return elements.length == 0 ? null : (Element)((Element)elements[0]).getParentNode();
			} catch(XMLException e) {
				throw new RuntimeException(e);
			}		
		}
		return null;
	}

	/**
	 * Finds the XML element for the given property in its original .xsp-config file.
	 * 
	 * @param def the containing {@link FacesDefinition} for the property
	 * @param prop the {@link FacesProperty} to search for
	 * @return an {@link Element} describing the definition, or {@code null} if it cannot be found
	 */
	private Element findDefinitionElement(FacesDefinition def, FacesProperty prop) {
		try {
			Element parent = findDefinitionElement(def);
			if(parent != null) {
				// Check for the property directly
				Element propElement = Stream.of(DOMUtil.nodes(parent, "property/property-name[normalize-space(text())='" + prop.getName() + "']"))
					.filter(Element.class::isInstance)
					.map(Element.class::cast)
					.map(el -> el.getParentNode())
					.map(Element.class::cast)
					.findFirst()
					.orElse(null);
				if(propElement != null) {
					// Great!
					return propElement;
				}
				
				// Check its parent
				if(def.getParent() != null) {
					propElement = findDefinitionElement(def.getParent(), prop);
					if(propElement != null) {
						return propElement;
					}
				}
				
				Collection<String> groups = def.getGroupTypeRefs();
				if(groups != null && !groups.isEmpty()) {
					String propFile = prop.getFile().getFilePath();
					Document propDoc = getXspConfig(propFile);
					if(propDoc != null) {
						return findInGroups(prop, propDoc, groups);
					}
				}
			}
			return null;
		} catch(XMLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Element findInGroups(FacesProperty prop, Document propDoc, Collection<String> groups) throws XMLException {
		if(groups == null || groups.isEmpty()) {
			return null;
		}
		
		Element groupElement = groups.stream()
				.map(group -> {
					try {
						return DOMUtil.nodes(propDoc, "/faces-config/group/group-type[normalize-space(text())='" + group + "']");
					} catch (XMLException e) {
						throw new RuntimeException(e);
					}
				})
				.filter(nodes -> nodes.length > 0)
				.map(nodes -> nodes[0])
				.map(Element.class::cast)
				.map(Element::getParentNode)
				.map(Element.class::cast)
				.findFirst()
				.orElse(null);
		if(groupElement == null) {
			// No dice
			return null;
		}
		// Check the group for a property element
		Object[] nodes = DOMUtil.nodes(groupElement, "property/property-name[normalize-space(text())='" + prop.getName() + "']");
		if(nodes.length > 0) {
			// Then we found it
			return (Element)((Element)nodes[0]).getParentNode();
		} else {
			// Otherwise, search the group's group refs
			List<String> groupRefs = Stream.of(DOMUtil.nodes(groupElement, "group-type-ref"))
				.map(Element.class::cast)
				.map(el -> el.getTextContent())
				.collect(Collectors.toList());
			
			return findInGroups(prop, propDoc, groupRefs);
		}
	}
	
	/**
	 * Determines whether the provided type definition should get special support for
	 * direct children even though it's technically not correct.
	 * 
	 * @param def the type definition to check
	 * @return whether or not this should get a special child-element workaround
	 * @see <a href="https://github.com/OpenNTF/org.openntf.nsfodp/issues/146">GitHub issue #146</a>
	 */
	private boolean isWorkaroundContainerType(FacesDefinition def) {
		Class<?> clazz = def.getJavaClass();
		if(ActionGroup.class.equals(clazz)) {
			return true;
		} else if(ComplexContainerTreeNode.class.equals(clazz)) {
			return true;
		}
		return false;
	}
}
