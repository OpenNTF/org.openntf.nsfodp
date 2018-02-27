/*
 * © Copyright IBM Corp. 2010
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package com.ibm.xsp.extlib.interpreter.interpreter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.binding.ComponentBindingObject;
import com.ibm.xsp.binding.MethodBindingEx;
import com.ibm.xsp.binding.ValueBindingEx;
import com.ibm.xsp.complex.ValueBindingObject;
import com.ibm.xsp.component.UIIncludeComposite;
import com.ibm.xsp.extlib.component.util.DynamicUIUtil;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.DefaultSecurityManager;
import com.ibm.xsp.page.FacesPageDriver;
import com.ibm.xsp.registry.FacesCompositeComponentDefinition;
import com.ibm.xsp.registry.FacesContainerProperty;
import com.ibm.xsp.registry.FacesDefinition;
import com.ibm.xsp.registry.FacesMethodBindingProperty;
import com.ibm.xsp.registry.FacesProperty;
import com.ibm.xsp.registry.FacesSimpleProperty;
import com.ibm.xsp.registry.types.FacesSimpleTypes;
import com.ibm.xsp.util.ValueBindingUtil;

/**
 * XPageFragment control.
 * 
 * @author priand
 */
public abstract class XPagesObject {

	public static interface PropertySetter {
        public String getName();
        public void addToObject(XPagesObject object);
	    public void updateProperty(SecurityManager sm, Object object);
	}
	
    public static abstract class AbstractSetter implements PropertySetter {
        private String name;
        public AbstractSetter(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public void addToObject(XPagesObject object) {
            if(object.setters==null) {
                object.setters = new ArrayList<XPagesObject.PropertySetter>(12);
            }
            object.setters.add(this);
        }
    }
    public static abstract class SetterFactory {
        public abstract String getName();
        public abstract PropertySetter createSetter(Object value);
        public Object parseString(String value) {
            throw new FacesExceptionEx(null,"The property {0} cannot be set from a string value",getName()); 
        }        
        public PropertySetter createSetterFromString(String value) {
            Object v = parseString(value);
            return createSetter(v);
        }
    }
    
    
    
    //////////////////////////////////////////////////////////////
    // Generic setters, using Java reflection
    //////////////////////////////////////////////////////////////
    public static abstract class GenericSetterFactory extends SetterFactory {
        private String name;
        private Method method;
        public GenericSetterFactory(String name, Method method) {
            this.name = name;
            this.method = method;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public PropertySetter createSetter(Object value) {
            return new GenericSetter(name, method, value);
        }
    }
    public static class GenericObjectSetterFactory extends GenericSetterFactory {
        public GenericObjectSetterFactory(String name, Method method) {
            super(name,method);
        }
        @Override
        public Object parseString(String value) {
            return value;
        }
    }
    public static class GenericStringSetterFactory extends GenericSetterFactory {
        public GenericStringSetterFactory(String name, Method method) {
            super(name,method);
        }
        @Override
        public Object parseString(String value) {
            return value;
        }
    }
    public static class GenericIntSetterFactory extends GenericSetterFactory {
        public GenericIntSetterFactory(String name, Method method) {
            super(name,method);
        }
        @Override
        public Object parseString(String value) {
            try {
                return Integer.parseInt(value);
            } catch(NumberFormatException ex) {
                throw new FacesExceptionEx(ex,"Invalid integer {0}",value);
            }
        }
    }
    public static class GenericDoubleSetterFactory extends GenericSetterFactory {
        public GenericDoubleSetterFactory(String name, Method method) {
            super(name,method);
        }
        @Override
        public Object parseString(String value) {
            try {
                return Double.parseDouble(value);
            } catch(NumberFormatException ex) {
                throw new FacesExceptionEx(ex,"Invalid double {0}",value);
            }
        }
    }
    public static class GenericBooleanSetterFactory extends GenericSetterFactory {
        public GenericBooleanSetterFactory(String name, Method method) {
            super(name,method);
        }
        @Override
        public Object parseString(String value) {
            if(value!=null) {
                if(value.equals("true")) {
                    return Boolean.TRUE;
                }
                if(value.equals("false")) {
                    return Boolean.FALSE;
                }
            }
            throw new FacesExceptionEx(null,"Invalid boolean {0}",value);
        }
    }
    public static class GenericSetter extends AbstractSetter {
        private Method method;
        private Object value;
        public GenericSetter(String name, Method method, Object value) {
            super(name);
            this.method = method;
            this.value = value;
        }
        public void updateProperty(SecurityManager sm, Object object) {
            try {
                sm.checkSetProperty(object, getName(), value);
                method.invoke(object, value);
            } catch(Exception e) {
                throw new FacesExceptionEx(null,"Error while setting property {0} on object {1}",getName(),object.getClass());
            }
        }
    }

    
    //////////////////////////////////////////////////////////////
    // Optimized properties
    //////////////////////////////////////////////////////////////
    public static class IdSetterFactory extends SetterFactory {
        public IdSetterFactory() {
        }
        @Override
        public String getName() {
            return "id";
        }
        @Override
        public Object parseString(String value) {
            return value;
        }
        @Override
        public PropertySetter createSetter(Object value) {
            return new IdSetter((String)value);
        }
    }
    public static class IdSetter extends AbstractSetter {
        private String value;
        public IdSetter(String value) {
            super("id");
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public void updateProperty(SecurityManager sm, Object object) {
            ((UIComponent)object).setId(value);
        }
    }    
    public static class RenderedSetterFactory extends SetterFactory {
        public RenderedSetterFactory() {
        }
        @Override
        public String getName() {
            return "rendered";
        }
        @Override
        public Object parseString(String value) {
            if(value!=null) {
                if(value.equals("true")) {
                    return Boolean.TRUE;
                }
                if(value.equals("false")) {
                    return Boolean.FALSE;
                }
            }
            throw new FacesExceptionEx(null,"Invalid boolean {0}",value);
        }
        @Override
        public PropertySetter createSetter(Object value) {
            return new RenderedSetter((Boolean)value);
        }
    }
    public static class RenderedSetter extends AbstractSetter {
        private boolean value;
        public RenderedSetter(boolean value) {
            super("rendered");
            this.value = value;
        }
        public void updateProperty(SecurityManager sm, Object object) {
            ((UIComponent)object).setRendered(value);
        }
    }    

    
    //////////////////////////////////////////////////////////////
    // Complex type
    //////////////////////////////////////////////////////////////
    public static class ComplexTypeSetterFactory extends SetterFactory {
        private String name;
        private Method method;
        public ComplexTypeSetterFactory(String name, Method method) {
            this.name = name;
            this.method = method;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public PropertySetter createSetter(Object value) {
            return new ComplexTypeSetter(name, method, value);
        }
    }
    public static class ComplexTypeSetter extends AbstractSetter {
        private Method method;
        private Object value;
        public ComplexTypeSetter(String name, Method method, Object value) {
            super(name);
            this.method = method;
            this.value = value;
        }
        public void updateProperty(SecurityManager sm, Object object) {
            try {
                Object o = value;
                if(o instanceof ComplexProperty) {
                    o = ((ComplexProperty)o).newObject();
                }
                sm.checkSetProperty(object, getName(), o);
                method.invoke(object, o);
            } catch(Exception e) {
                throw new FacesExceptionEx(null,"Error while setting property {0} on object {1}",getName(),object.getClass());
            }
        }
    }

    
    //////////////////////////////////////////////////////////////
    // Collection
    //////////////////////////////////////////////////////////////
    public static class CollectionSetterFactory extends SetterFactory {
        private String name;
        private Method method;
        public CollectionSetterFactory(String name, Method method) {
            this.name = name;
            this.method = method;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public PropertySetter createSetter(Object value) {
            return new CollectionSetter(name, method, value);
        }
    }
    public static class CollectionSetter extends AbstractSetter {
        private Method method;
        private Object value;
        public CollectionSetter(String name, Method method, Object value) {
            super(name);
            this.method = method;
            this.value = value;
        }
        public void updateProperty(SecurityManager sm, Object object) {
            try {
                Object o = value;
                if(o instanceof ComplexProperty) {
                    o = ((ComplexProperty)o).newObject();
                }
                sm.checkSetProperty(object, getName(), o);
                method.invoke(object, o);
            } catch(Exception e) {
                throw new FacesExceptionEx(null,"Error while setting property {0} on object {1}",getName(),object.getClass());
            }
        }
    }

    //////////////////////////////////////////////////////////////
    // MethodBinding
    //////////////////////////////////////////////////////////////
    public static class MethodBindingSetterFactory extends SetterFactory {
        private String name;
        private Method method;
        public MethodBindingSetterFactory(String name, Method method) {
            this.name = name;
            this.method = method;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public PropertySetter createSetter(Object value) {
            return new MethodBindingSetter(name, method, value);
        }
    }
    public static class MethodBindingSetter extends AbstractSetter {
        private Method method;
        private Object value;
        public MethodBindingSetter(String name, Method method, Object value) {
            super(name);
            this.method = method;
            this.value = value;
        }
        public void updateProperty(SecurityManager sm, Object object) {
            try {
                Application app = FacesContext.getCurrentInstance().getApplication();
                javax.faces.el.MethodBinding mb = createMethodBinding(app,object,value);
                sm.checkMethodBinding(object, getName(), mb);
                sm.checkSetProperty(object, getName(), value);
                method.invoke(object, mb);
            } catch(Exception e) {
                throw new FacesExceptionEx(null,"Error while setting property {0} on object {1}",getName(),object.getClass());
            }
        }
        protected MethodBinding createMethodBinding(Application application, Object parent, Object value) {
            MethodBinding methodBinding;
            if(value instanceof String) {
                methodBinding = application.createMethodBinding((String)value, null);
//            } else if(value instanceof SimpleActionServer) {
//              SimpleActionServer sa = (SimpleActionServer)value;
//              MethodBinding mb = (MethodBinding)sa.createObject(context);
//              if(mb instanceof MethodBindingEx) {
//                  ((MethodBindingEx) mb).setComponent(component);
//              }
//              return mb;
//          }
            } else if(value instanceof ComplexProperty) {
                ComplexProperty p = (ComplexProperty)value;
                Object o = p.newObject();
                if(!(o instanceof MethodBinding)) { 
                    throw new FacesExceptionEx(null,"The object of class {0} is not a MethodBinding", value.getClass());
                }
                methodBinding = (MethodBinding)o;
            } else {
                throw new FacesExceptionEx(null,"Cannot create method binding from an object class {0}", value.getClass());
            }
            if (methodBinding instanceof ComponentBindingObject) {
                if(parent instanceof UIComponent) {
                    ((ComponentBindingObject) methodBinding).setComponent((UIComponent)parent);
                }
            }
            if (methodBinding instanceof MethodBindingEx) {
                if(parent instanceof UIComponent) {
                    ((MethodBindingEx) methodBinding).setComponent((UIComponent)parent);
                }
                //((MethodBindingEx) methodBinding).setParamNames(names);
                //((MethodBindingEx) methodBinding).setSourceReferenceId(sourceId);
            }
            return methodBinding;
        }
    }
    
    //////////////////////////////////////////////////////////////
    // Pseudo 'loaded' property
    //////////////////////////////////////////////////////////////
    public static class LoadedSetterFactory extends GenericBooleanSetterFactory {
        public LoadedSetterFactory(String name) {
            super(name,null);
        }
        @Override
        public PropertySetter createSetter(Object value) {
            return new LoadedSetter((Boolean)value);
        }
    }
    public static class LoadedSetter extends AbstractSetter {
        private boolean value;
        LoadedSetter(boolean value) {
            super("loaded");
            this.value = value;
        }
        @Override
        public void addToObject(XPagesObject object) {
            object.loadedSetter = this;
        }
        public boolean isLoaded() {
            return value;
        }
        public void updateProperty(SecurityManager sm, Object object) {
            // This is a pseudo property...
        }
    }

	
    //////////////////////////////////////////////////////////////
    // Runtime binding
    //////////////////////////////////////////////////////////////
    public static class RuntimeBinding extends AbstractSetter {
        private FacesProperty property;
        private String expression;
        public RuntimeBinding(String name, FacesProperty property, String expression) {
            super(name);
            this.property = property;
            this.expression = expression;
        }
        public FacesProperty getProperty() {
            return property;
        }
        public String getExpression() {
            return expression;
        }
        public void updateProperty(SecurityManager sm, Object object) {
            Application app = FacesContext.getCurrentInstance().getApplication();
            javax.faces.el.ValueBinding vb = app.createValueBinding(expression);
            if(vb instanceof ValueBindingEx) {
                ValueBindingEx vbx = (ValueBindingEx)vb;
                if(object instanceof UIComponent) {
                    vbx.setComponent((UIComponent)object);
                }
                vbx.setExpectedType(property.getJavaClass());
            }
            sm.checkRuntimeBinding(object, getName(), vb);
            if(object instanceof UIComponent) {
                ((UIComponent)object).setValueBinding(property.getName(), vb);
            } else if(object instanceof ValueBindingObject) {
                ((ValueBindingObject)object).setValueBinding(property.getName(), vb);
            } else {
                throw new FacesExceptionEx(null,"Cannot assign value binding to property {0} of object class {1}", property.getName(), object.getClass());
            }
        }
    }

    
    //////////////////////////////////////////////////////////////
    // Loadtime binding
    //////////////////////////////////////////////////////////////
    public static class LoadtimeBinding extends AbstractSetter {
        private XPagesObject _this;
        private String name;
        private String expression;
        private Class<?> javaClass;
        public LoadtimeBinding(XPagesObject _this, String name, String expression, Class<?> javaClass) {
            super(name);
            this._this = _this;
            this.name = name;
            this.expression = expression;
            this.javaClass = javaClass;
        }
        public String getExpression() {
            return expression;
        }
        public void updateProperty(SecurityManager sm, Object object) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            javax.faces.el.ValueBinding vb = createValueBinding(ctx,object);
            sm.checkLoadtimeBinding(object, name, vb);
            Object value = vb.getValue(ctx);
            PropertySetter setter = _this.createSetter(name, value);
            setter.updateProperty(sm,object);
        }
        public Object evaluateProperty(Object object) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            javax.faces.el.ValueBinding vb = createValueBinding(ctx,object);
            Object value = vb.getValue(ctx);
            return value;
        }
        protected javax.faces.el.ValueBinding createValueBinding(FacesContext ctx, Object object) {
            Application app = ctx.getApplication();
            javax.faces.el.ValueBinding vb = app.createValueBinding(expression);
            if(vb instanceof ValueBindingEx) {
                ValueBindingEx vbx = (ValueBindingEx)vb;
                if(object instanceof UIComponent) {
                    vbx.setComponent((UIComponent)object);
                }
                vbx.setExpectedType(javaClass);
            }
            return vb;
        }
    }
    
	
	private FacesDefinition definition;
	//private Map<String,PropertySetter> properties;
	private PropertySetter loadedSetter;
    private List<PropertySetter> setters;

	public XPagesObject(FacesDefinition definition) {
	    this.definition = definition;
	}
	
	public FacesDefinition getFacesDefinition() {
	    return definition;
	}
	
	public List<PropertySetter> getSetters() {
	    return setters;
	}
    
    public PropertySetter getSetter(String name) {
        if(setters!=null) {
            int count = setters.size();
            for(int i=0; i<count; i++) {
                if(StringUtil.equals(setters.get(i).getName(),name)) {
                    return setters.get(i);
                }
            }
        }
        return null;
    }
	
	public boolean isLoaded() {
	    // loaded=true, unless something is specified
	    if(loadedSetter!=null) {
	        if(loadedSetter instanceof LoadtimeBinding) {
	            Object v = ((LoadtimeBinding)loadedSetter).evaluateProperty(null);
	            if(v instanceof Boolean) {
	                return ((Boolean)v).booleanValue();
	            }
	            throw new FacesExceptionEx(null,"Invalid value {0} for property loaded",v);
	        }
	        return ((LoadedSetter)loadedSetter).isLoaded();
	    }
	    return true;
	}
	
    public Object newObject() throws FacesException {
        try {
            Object o = definition.getJavaClass().newInstance();
            initProperties(o);
            if(o instanceof UIIncludeComposite) {
                UIIncludeComposite cc = (UIIncludeComposite)o;
                String pageName = ((FacesCompositeComponentDefinition)definition).getCompositeFile();
                FacesPageDriver pageDriver = DynamicUIUtil.findDefaultPageDriver(FacesContext.getCurrentInstance());
                cc.setPageName(pageName);
                cc.setPageDriver(pageDriver);
            }
            return o;
        } catch(Exception ex) {
            throw new FacesExceptionEx(ex,"Error while instanciating object {0}:{1} (class {2})",definition.getNamespaceUri(),definition.getTagName(),definition.getJavaClass().getName());
        }
    }
	

    public void initProperties(Object object) throws FacesException {
        if(setters!=null) {
            // Use the right security manager...
            // Get it from FacesContextEx??
            SecurityManager sm = DefaultSecurityManager.instance;
            int np = setters.size();
            for(int i=0; i<np; i++) {
                setters.get(i).updateProperty(sm,object);
            }
        }
    }
	
    public void addPropertyFromString(String name, String value) {
        PropertySetter setter = createSetterFromString(name, value);
        setter.addToObject(this);
    }
    public PropertySetter createSetterFromString(String name, String value) {
        // Get the property from the registry
        FacesProperty prop = definition.getProperty(name);
        if(prop==null) {
            throw new FacesExceptionEx(null,"Unknown property {0} in object id {1}", name, getFacesDefinition().getId());
        }
        
        // Check if this is a loadtime binding expressions
        if(ValueBindingUtil.isLoadtimeExpression(value)) {
            return new LoadtimeBinding(this, name, value, prop.getJavaClass());
        }

        // Check if it is a value binding
		if(ValueBindingUtil.isRuntimeExpression(value)) {
		    boolean supportsValueBinding = true; // How to get that from the registry?
			if(!supportsValueBinding) {
				throw new FacesExceptionEx(null,"Property {0} does not support value binding", name);
			}
			RuntimeBinding vb = new RuntimeBinding(name,prop,value);
			return vb;
		}
		
		PropertySetter setter = createPropertySetterFromString(name, value);
		return setter;
	}

    public PropertySetter createSetter(String name, Object value) {
        if(value.getClass()==String.class) {
            return createSetterFromString(name, (String)value);
        }
        PropertySetter setter = createPropertySetter(name, value);
        return setter;
    }
    
	public void addProperty(String name, ComplexProperty value) {
        PropertySetter setter = createPropertySetter(name, value);
        setter.addToObject(this);
	}
	
	public void validate() throws FacesException {
	}

	
	private static final String SETTERFACTORY = "extlib.setter";
	
	protected PropertySetter createPropertySetterFromString(String name, String value) {
        SetterFactory factory = findSetterFactory(name);
        return factory.createSetterFromString(value);
	}
    protected PropertySetter createPropertySetter(String name, Object value) {
        SetterFactory factory = findSetterFactory(name);
        return factory.createSetter(value);
    }
    protected SetterFactory findSetterFactory(String name) {
        FacesProperty prop = definition.getProperty(name);
        if(prop==null) {
            throw new FacesExceptionEx(null,"Unknown property {0} in object id {1}", name, getFacesDefinition().getId());
        }       
        SetterFactory factory = (SetterFactory)prop.getExtension(SETTERFACTORY);
        if(factory==null) {
            factory = createSetterFactory(prop);
            definition.setExtension(SETTERFACTORY, factory);
        }
        return factory;
    }
	protected SetterFactory createSetterFactory(FacesProperty prop) {
	    String name = prop.getName();
	            
        // Simple property setters
        if(prop instanceof FacesSimpleProperty) {
            FacesSimpleProperty p = (FacesSimpleProperty)prop;
            
    	    // Specific setters
    	    switch(name.charAt(0)) {
                case 'i': {
                    if(name.equals("id")) {
                        return new IdSetterFactory();
                    }
                } break;
    	        case 'l': {
    	            if(name.equals("loaded")) {
    	                return new LoadedSetterFactory(prop.getName());
    	            }
    	        } break;
                case 'r': {
                    if(name.equals("rendered")) {
                        return new RenderedSetterFactory();
                    }
                } break;
    	    }

    	    Class<?> c = prop.getJavaClass();
    	    
    	    // Complex property
    	    if(p.getType()==FacesSimpleTypes.TYPE_OBJECT) {
                return new ComplexTypeSetterFactory(prop.getName(),findSetter(prop, c));
    	    }
    	    
    	    // Generic setter
            if(c==Object.class) {
                return new GenericObjectSetterFactory(prop.getName(),findSetter(prop, c));
            }
            if(c==String.class) {
                return new GenericStringSetterFactory(prop.getName(),findSetter(prop, c));
            }
            if(c==Integer.TYPE) {
                return new GenericIntSetterFactory(prop.getName(),findSetter(prop, c));
            }
            if(c==Double.TYPE) {
                return new GenericDoubleSetterFactory(prop.getName(),findSetter(prop, c));
            }
            if(c==Boolean.TYPE) {
                return new GenericBooleanSetterFactory(prop.getName(),findSetter(prop, c));
            }
            throw new FacesExceptionEx(null,"Unsupported simple property type {0} for {1}",c.getName(),prop.getName());
        }
	    
	    // Collection setters
	    // TODO: fix the lookup for props
	    if(prop instanceof FacesContainerProperty) {
	        FacesContainerProperty p = (FacesContainerProperty)prop;
	        if(p.isCollection()) {
	            String methodName = p.getCollectionAddMethod();
                Class<?> objClass = getFacesDefinition().getJavaClass();
                FacesProperty sp = p.getItemProperty();
                Class<?> paramClass = sp.getJavaClass();
                try {
	                Method method = objClass.getMethod(methodName, paramClass);
	                return new CollectionSetterFactory(name, method);
	            } catch(Exception ex) {
	                throw new FacesExceptionEx(ex,"Cannot find the method {0}({1}) on class {2} to access the property {3}",methodName,paramClass,objClass,p.getName());
	            }
	        }
	    }
        
        // Methodbinding setters
        if(prop instanceof FacesMethodBindingProperty) {
            FacesMethodBindingProperty p = (FacesMethodBindingProperty)prop;
            return new MethodBindingSetterFactory(prop.getName(),findSetter(prop, MethodBinding.class));
        }
	        
        throw new FacesExceptionEx(null,"Cannot set value for property {0}",prop.getName());
	}
    protected Method findSetter(FacesProperty prop, Class<?> c) {
        String pName = prop.getName();
        try {
            String methodName = "set"+Character.toUpperCase(pName.charAt(0))+pName.substring(1);
            return definition.getJavaClass().getMethod(methodName,c);
        } catch(NoSuchMethodException ex) {}
        throw new FacesExceptionEx(null,"Unknown setter for property {0} of class {1}",prop.getName(),c.getName());
    }
}
