package servlet.schema;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.FacesListener;
import javax.faces.event.ValueChangeListener;
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
import com.ibm.xsp.component.UIDataPanelBase;
import com.ibm.xsp.component.UIFormEx;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.component.xp.XspPager;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.registry.AbstractContainerProperty;
import com.ibm.xsp.registry.FacesComponentDefinition;
import com.ibm.xsp.registry.FacesDefinition;
import com.ibm.xsp.registry.FacesProperty;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.config.XspRegistryProvider;
import com.ibm.xsp.resource.Resource;

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
				.forEach(def -> outComponentDefinition(def, schema, facesRegistry, false));
			
			@SuppressWarnings("unchecked")
			List<FacesDefinition> complexDefs = (List<FacesDefinition>)(List<?>)facesRegistry.findComplexDefs();
			complexDefs.stream()
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
			Element simpleType = DOMUtil.createElement(doc, schema, "xs:simpleType");
			simpleType.setAttribute("name", "attrBinding");
			Element restriction = DOMUtil.createElement(doc, simpleType, "xs:restriction");
			restriction.setAttribute("base", "xs:string");
			Element pattern = DOMUtil.createElement(doc, restriction, "xs:pattern");
			// TODO figure out if this can enforce a date format
			pattern.setAttribute("value", "[#$]\\{[.\\s\\S]*\\}");
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
		
		
		// Find if it's the child of a complex type
		FacesDefinition maybeParent = def.getParent();
		Element attrRoot;
		if(maybeParent != null) {
			if(noComponents.contains(maybeParent.getJavaClass())) {
				maybeParent = maybeParent.getParent();
			}
			if(fauxComponents.contains(maybeParent.getJavaClass())) {
				outComponentDefinition((FacesComponentDefinition)maybeParent, schema, registry, true);
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
			
			if(def.getDefinedPropertyNames().isEmpty()) {
				// Add an xs:anyAttribute here to make this extensible down the line
				DOMUtil.createElement(complexType.getOwnerDocument(), complexType, "xs:anyAttribute");
			}
		}
		
		outProperties(def, attrRoot, registry);

		if(UIComponent.class.isAssignableFrom(def.getJavaClass())) {
			// Declare that it allows any children
			// TODO see if this can restrict to text + UIComponents. This is tough because HTML tags are allowed
			Element all = (Element)attrRoot.getFirstChild();
			if(all == null || !all.getTagName().equals("xs:choice")) {
				all = DOMUtil.createElement(doc, attrRoot, "xs:choice");
				all.setAttribute("minOccurs", "0");
				all.setAttribute("maxOccurs", "unbounded");
			}
			DOMUtil.createElement(doc, all, "xs:any");
		}
		
		if(def.isTag() && !noTag) {
			// Output the concrete definition
			Element element = DOMUtil.createElement(doc, schema, "xs:element");
			element.setAttribute("name", def.getTagName());
			String prefix = StringUtil.equals(def.getNamespaceUri(), namespace) ? "" : (extMap.get(def.getNamespaceUri()) + ':');
			element.setAttribute("type", prefix + def.getTagName());

//			Element annotation = DOMUtil.createElement(doc, element, "xs:annotation");
			
		} else {
			if(!(
				MethodBinding.class.isAssignableFrom(def.getJavaClass())
				|| ValueBinding.class.isAssignableFrom(def.getJavaClass())
			)) {
				// Output an abstract representation
//				complexType.setAttribute("abstract", "true");
			}
		}
	}
	
	private void outProperties(FacesDefinition def, Element element, SharableRegistryImpl registry) {
		Collection<String> names = def.getPropertyNames();
		for(String propName : names) {
			FacesProperty prop = def.getProperty(propName);
			
			// Make sure it's not defined in a compatible parent
			FacesDefinition maybeParent = def.getParent();
			boolean foundMatch = false;
			while(maybeParent != null && StringUtil.equals(def.getNamespaceUri(), maybeParent.getNamespaceUri())) {
				// <xp:view/> is defined multiple times, so handle it specially
				if(!UIViewRootEx.class.equals(maybeParent.getJavaClass()) && maybeParent.isDefinedProperty(propName)) {
					foundMatch = true;
					break;
				}
				maybeParent = maybeParent.getParent();
			}
			if(foundMatch) {
				continue;
			}

			outProperty(def, prop, element, registry);
		}
	}
	
	private void outProperty(FacesDefinition def, FacesProperty prop, Element element, SharableRegistryImpl registry) {
		// TODO check run/load binding from FacesSimpleProperty
		Class<?> clazz = prop.getJavaClass();
		if(prop instanceof AbstractContainerProperty) {
			clazz = ((AbstractContainerProperty)prop).getItemProperty().getJavaClass();
		}
		if(byte.class.equals(clazz) || short.class.equals(clazz) || int.class.equals(clazz) || long.class.equals(clazz)
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
		if(all == null || !all.getTagName().equals("xs:choice")) {
			all = DOMUtil.createElement(doc, element, "xs:choice");
			all.setAttribute("minOccurs", "0");
			all.setAttribute("maxOccurs", "unbounded");
		}
		
		// Create a this.child child
		Element thisElement = DOMUtil.createElement(doc, all, "xs:element");
		thisElement.setAttribute("name", "this." + prop.getName());
		if(complex && !isBindingType(prop)) {
			// TODO see if there's a way to require elements of a given subclass
			/*
			 * <xs:complexType>
          		<xs:sequence>
          			<xs:any></xs:any>
          		</xs:sequence>
          	</xs:complexType>
			 */
			Element complexType = DOMUtil.createElement(doc, thisElement, "xs:complexType");
			Element sequence = DOMUtil.createElement(doc, complexType, "xs:sequence");
			if(prop instanceof AbstractContainerProperty) {
				// Then mark its upper bound as unlimited
				sequence.setAttribute("maxOccurs", "unbounded");
			}
			DOMUtil.createElement(doc, sequence, "xs:any").setAttribute("maxOccurs", "unbounded");
		} else {
			thisElement.setAttribute("type", type);
		}
		
		// Only output the attribute if no parent also names it
		FacesDefinition maybeParent = def.getParent();
		while(maybeParent != null) {
			if(maybeParent.isDefinedProperty(prop.getName())) {
				// Then exit the whole thing
				return;
			}
			
			maybeParent = maybeParent.getParent();
		}
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
		return
			MethodBinding.class.isAssignableFrom(prop.getJavaClass())
			|| ValueBinding.class.isAssignableFrom(prop.getJavaClass());
	}
}
