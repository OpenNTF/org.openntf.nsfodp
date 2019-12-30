package servlet.schema;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.Format;
import com.ibm.commons.xml.XMLException;
import com.ibm.xsp.component.UIFormEx;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.component.xp.XspPager;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.registry.FacesComponentDefinition;
import com.ibm.xsp.registry.FacesDefinition;
import com.ibm.xsp.registry.FacesProperty;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.config.XspRegistryProvider;

public class AbstractSchemaServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Map<String, String> extMap = new HashMap<>();
	static {
		extMap.put("http://www.ibm.com/xsp/jsf/core", "f");
		extMap.put("http://www.ibm.com/xsp/core", "xp");
		extMap.put("http://www.ibm.com/xsp/coreex", "xe");
	}
	// Classes that show up as parents but are squelched in the library definitions
	private static final List<Class<?>> fauxComponents = Arrays.asList(
		UIFormEx.class,
		XspPager.class
	);
	// Classes that show up as parents but have no library definition at all
	private static final List<Class<?>> noComponents = Arrays.asList(
		UIViewRootEx.class
	);
	
	private final String namespace;
	private final String[] imports;

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
			schema.setAttribute("xmlns", namespace);
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
			List<FacesDefinition> defs = (List<FacesDefinition>)(List<?>)facesRegistry.findComponentDefs();
			defs.stream()
				.filter(def -> StringUtil.equals(namespace, def.getNamespaceUri()))
				.forEach(def -> outComponentDefinition(def, schema, defs, false));
			
			@SuppressWarnings("unchecked")
			List<FacesDefinition> complexDefs = (List<FacesDefinition>)(List<?>)facesRegistry.findComplexDefs();
			complexDefs.stream()
				.filter(def -> StringUtil.equals(namespace, def.getNamespaceUri()))
				.forEach(def -> outComponentDefinition(def, schema, defs, false));
			
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
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrInteger");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			pattern.setAttribute("value", "^((\\d+)|(#\\{.*\\}|\\$\\{.*\\}))$");
		}
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrBoolean");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			pattern.setAttribute("value", "^((true)|(false)|#\\{.*\\}|\\$\\{.*\\})$");
		}
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrDecimal");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			pattern.setAttribute("value", "^((\\d+(\\.\\d+)?)|#\\{.*\\}|\\$\\{.*\\})$");
		}
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrTime");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			// TODO figure out if this can enforce a time format
			pattern.setAttribute("value", "^((.*)|#\\{.*\\}|\\$\\{.*\\})$");
		}
		{
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrDate");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			// TODO figure out if this can enforce a date format
			pattern.setAttribute("value", "^((.*)|#\\{.*\\}|\\$\\{.*\\})$");
		}
	}
	
	private void outComponentDefinition(FacesDefinition def, Element schema, List<FacesDefinition> defs, boolean noTag) {
		Document doc = schema.getOwnerDocument();
		
		String name;
		String tagName = def.getTagName();
		if(StringUtil.isNotEmpty(tagName) && !noTag) {
			name = tagName;
		} else {
			name = def.getJavaClass().getName();
		}
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
		
		
		// Find if it's the child of a complex type
		FacesDefinition maybeParent = def.getParent();
		Element attrRoot;
		if(maybeParent != null) {
			if(noComponents.contains(maybeParent.getJavaClass())) {
				maybeParent = maybeParent.getParent();
			}
			if(fauxComponents.contains(maybeParent.getJavaClass())) {
				outComponentDefinition((FacesComponentDefinition)maybeParent, schema, defs, true);
			}
			
			Element complexContent = DOMUtil.createElement(doc, complexType, "xs:complexContent");
			Element extension = DOMUtil.createElement(doc, complexContent, "xs:extension");
			String base;
			if(maybeParent.isTag() && StringUtil.isNotEmpty(maybeParent.getTagName()) && !fauxComponents.contains(maybeParent.getJavaClass())) {
				base = extMap.get(maybeParent.getNamespaceUri()) + ":" + maybeParent.getTagName();
			} else {
				base = extMap.get(maybeParent.getNamespaceUri()) + ":" + maybeParent.getJavaClass().getName();
			}
			extension.setAttribute("base", base);
			attrRoot = extension;
		} else {
			attrRoot = complexType;
		}
		
		outProperties(def, attrRoot, defs);

		if(def.isTag() && !noTag) {
			// Output the concrete definition
			Element element = DOMUtil.createElement(doc, schema, "xs:element");
			element.setAttribute("name", def.getTagName());
			String prefix = StringUtil.equals(def.getNamespaceUri(), namespace) ? "" : (extMap.get(def.getNamespaceUri()) + ':');
			element.setAttribute("type", prefix + def.getTagName());
		} else {
			// Output an abstract representation
			complexType.setAttribute("abstract", "true");
		}
	}
	
	private void outProperties(FacesDefinition def, Element element, List<FacesDefinition> defs) {
		
		for(String propName : def.getDefinedPropertyNames()) {
			FacesProperty prop = def.getProperty(propName);
			
			// Make sure it's not defined in a compatible parent
			FacesDefinition maybeParent = def.getParent();
			boolean foundMatch = false;
			while(maybeParent != null) {
				if(maybeParent.isDefinedProperty(propName)) {
					foundMatch = true;
					break;
				}
				maybeParent = maybeParent.getParent();
			}
			if(foundMatch) {
				continue;
			}
			
			Class<?> clazz = prop.getJavaClass();
			if(byte.class.equals(clazz) || short.class.equals(clazz) || int.class.equals(clazz) || long.class.equals(clazz)
				|| Byte.class.equals(clazz) || Short.class.equals(clazz) || Integer.class.equals(clazz) || Long.class.equals(clazz)) {
				outAttribute(prop, element, extMap.get(namespace) + ":attrInteger");
			} else if(boolean.class.equals(clazz) || Boolean.class.equals(clazz)) {
				outAttribute(prop, element, extMap.get(namespace) + ":attrBoolean");
			} else if(float.class.equals(clazz) || double.class.equals(clazz) || Number.class.isAssignableFrom(clazz)) {
				outAttribute(prop, element, extMap.get(namespace) + ":attrDecimal");
			} else if(LocalTime.class.isAssignableFrom(clazz)) {
				outAttribute(prop, element, extMap.get(namespace) + ":attrTime");
			} else if(Date.class.equals(clazz) || TemporalAccessor.class.isAssignableFrom(clazz)) {
				outAttribute(prop, element, extMap.get(namespace) + ":attrDate");
			} else if(CharSequence.class.equals(clazz)) {
				outAttribute(prop, element, "xs:string");
			} else {
				// Figure out if it's complex or not
				FacesDefinition propDef = defs.stream().filter(def2 -> clazz.equals(def2.getJavaClass())).findAny().orElse(null);
				if(propDef != null) {
					outAttribute(prop, element, "xs:string");
				} else {
					outAttribute(prop, element, "xs:string");
				}
			}
		}
	}
	
	private void outAttribute(FacesProperty prop, Element element, String type) {
		Document doc = element.getOwnerDocument();
		Element attribute = DOMUtil.createElement(doc, element, "xs:attribute");
		attribute.setAttribute("name", prop.getName());
		attribute.setAttribute("type", type);
		if(prop.isRequired()) {
			attribute.setAttribute("use", "required");
		}
	}
}
