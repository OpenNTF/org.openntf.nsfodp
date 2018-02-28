/*
 * � Copyright IBM Corp. 2010
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

package com.ibm.xsp.extlib.interpreter.interpreter.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.faces.FacesException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.extlib.interpreter.interpreter.ComplexProperty;
import com.ibm.xsp.extlib.interpreter.interpreter.Control;
import com.ibm.xsp.extlib.interpreter.interpreter.ControlFactory;
import com.ibm.xsp.extlib.interpreter.interpreter.ControlPassthoughText;
import com.ibm.xsp.extlib.interpreter.interpreter.XPagesObject;
import com.ibm.xsp.page.parse.SerializationUtil;

/**
 * XPageFragment parser.
 * 
 * This class parses an XPageFragment source code and create an internal
 * representation that can then be used to generate the actual
 * JSF tree.
 * 
 * @author priand
 */
public class XPagesParser {

	private ControlFactory factory;
	
	public XPagesParser(ControlFactory factory) {
		this.factory = factory;
	}
	
	public Control parse(String source) throws FacesException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/namespaces",true);
            SAXParser parser = factory.newSAXParser();
            InputSource is = new InputSource(new StringReader(source));
            XmlHandler handler = new XmlHandler();
            parser.parse(is, handler);
            return handler.mainControl;
        } catch (FacesException e) {
        	throw e;
        } catch (Exception e) {
        	throw new FacesExceptionEx(e,"Error while parsing XPages markup");
        }
	}
	
	public Control parse(Reader source) throws FacesException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            InputSource is = new InputSource(source);
            XmlHandler handler = new XmlHandler();
            parser.parse(is, handler);
            return handler.mainControl;
        } catch (FacesException e) {
        	throw e;
        } catch (Exception e) {
        	throw new FacesExceptionEx(e,"Error while parsing XPages markup");
        }
	}
	
	public Control parse(InputStream source) throws FacesException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            InputSource is = new InputSource(source);
            XmlHandler handler = new XmlHandler();
            parser.parse(is, handler);
            return handler.mainControl;
        } catch (FacesException e) {
        	throw e;
        } catch (Exception e) {
        	throw new FacesExceptionEx(e,"Error while parsing XPages markup");
        }
    }

	static class XPagesContext {
		XPagesContext	previous;
		XPagesObject	object;
		String			property;
        boolean         infacets;
		XPagesContext(XPagesContext	previous, XPagesObject	object) {
			this.previous = previous;
			this.object = object;
		}
	}
	
	
    private class XmlHandler extends DefaultHandler {

    	private Control mainControl;
    	private XPagesContext context;
    	private StringBuilder text;
    	
        public XmlHandler() {
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // If there is some pending text, then this is some pass through text
            if(text!=null) {
                String s = text.toString();
                if(!SerializationUtil.isWhitespace(s)) {
                    // If there is a pending property, then this is an error
                    if(context!=null && context.property!=null) {
                        throw new FacesExceptionEx(null,"Property tag this.{0} should be closed before adding a new element",localName);
                    }
                    
                    // If there is a pending property, then the text is its content
                    ControlPassthoughText pt = new ControlPassthoughText(s);
                    ((Control)context.object).addChild(pt);
                }
                text = null;
            }
            
        	// Look if it is a complex property
        	if(localName.startsWith("this.")) {
        		if(localName.equals("this.facets")) {
        			context.infacets = true;
        			return;
        		}
        		if(context==null || context.property!=null) {
        			throw new FacesExceptionEx(null,"Property {0} should be applied to a control or a complex property",localName);
        		}
        		context.property = localName.substring(5);
        		return;
        	}

        	// Else, create a new control
        	XPagesObject xpagesObject = factory.createXPagesObject(uri, localName);
        	        	
        	// Now, set the different properties
        	if(attributes!=null) {
        		int len = attributes.getLength();
        		for(int i=0; i<len; i++) {
        			String _uri = attributes.getURI(i);
        			String _name = attributes.getLocalName(i);
        			String _value = attributes.getValue(i);
        			if(isPropertyAttribute(_uri, _name, _value)) {
        				xpagesObject.addPropertyFromString(_name, _value);
        			}
        		}
        	}
        	
        	// Choose between a control and a complex property
        	if(xpagesObject instanceof Control) {
        		if(context!=null && ((context.object instanceof ComplexProperty) || context.property!=null)) {
        			throw new FacesExceptionEx(null,"Control {0} cannot be added to a property",localName);
        		}
        		Control control = (Control)xpagesObject;
	        	// Add this control to its parent
	        	if(context!=null) {
	        		// Look how the child should be added: as a regular child or as a facet
	        	    if(context.infacets) {
	        	        String facetName = attributes.getValue(DefaultControlFactory.XP_NS, "key");
	        	        if(StringUtil.isNotEmpty(facetName)) {
	        	            ((Control)context.object).addFacet(facetName, control);
	        	        } else {
	                        throw new FacesExceptionEx(null,"Missing facet key in tag {0}",localName);
	        	        }
	        		} else {
	        			((Control)context.object).addChild(control);
	        		}
	        	} else {
	        		if(mainControl==null) {
	        			mainControl = control;
	        		}
	        	}
	        	context = new XPagesContext(context,control); 
        	} else {
        		ComplexProperty complex = (ComplexProperty)xpagesObject;
        		if(context.property==null) {
        			throw new FacesExceptionEx(null,"Complex type {0} must be added to a property",localName);
        		}
        		context.object.addProperty(context.property, complex);
	        	context = new XPagesContext(context,complex); 
        	}
        }
        
        private boolean isPropertyAttribute(String uri, String name, String value) {
        	// XPages std attributes (facets...)
        	if(StringUtil.equals(uri, DefaultControlFactory.XP_NS)) {
        		return false;
        	}
        	
        	// Else, just assume that it is a property
        	return true;
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // "this." property
        	if(localName.startsWith("this.")) {
                if(localName.equals("this.facets")) {
                    context.infacets = false;
                    return;
                }
                if(text!=null) {
                    String s = text.toString();
                    if(!SerializationUtil.isWhitespace(s)) {
                        String name = context.property; 
                        context.object.addPropertyFromString(name, s);
                    }
                    text = null;
                }
        		context.property = null;
        		return;
        	}
        	
            // If there is some pending text, then this is some pass through text
            if(text!=null) {
                String s = text.toString();
                if(!SerializationUtil.isWhitespace(s)) {
                    ControlPassthoughText pt = new ControlPassthoughText(s);
                    ((Control)context.object).addChild(pt);
                    text = null;
                }
            }

        	
        	// Else go back to the previous control 
        	context = context.previous; 
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(text==null) {
                text = sharedBuilder;
                text.setLength(0);
            }
            text.append(ch, start, length);
        }
        private StringBuilder sharedBuilder = new StringBuilder(256);
    }
}
