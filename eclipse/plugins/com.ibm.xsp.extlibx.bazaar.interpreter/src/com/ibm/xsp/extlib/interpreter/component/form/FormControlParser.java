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

package com.ibm.xsp.extlib.interpreter.component.form;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import javax.faces.FacesException;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.xsp.FacesExceptionEx;


/**
 * Form Control Serializer.
 * <p>
 * This serializer serializes/deserializes the content of a form into JSON.
 * </p>
 * @author priand
 */
public class FormControlParser {
	
	public static FormControlParser get() {
		return new FormControlParser();
	}

	protected FormControlParser() {
	}

	
	// ==============================================================
	// JSON
	// ==============================================================
	
	public CForm parseJson(Reader reader) throws JsonException {
		CForm form = (CForm)JsonParser.fromJson(new CustomFactory(), reader);
		return form;
	}
	public CForm parseJson(String s) throws JsonException {
		CForm form = (CForm)JsonParser.fromJson(new CustomFactory(), s);
		return form;
	}

	public String generateJson(CForm form) throws JsonException, IOException {
		return JsonGenerator.toJson(new CustomFactory(), form);
	}
	
	protected static class CustomFactory extends JsonJavaFactory {
		protected CustomFactory() {
		}
		@Override
        public Object createObject(Object parent, String propertyName) {
        	if(parent==null) {
        		return new CForm();
        	} else if(parent instanceof CForm) {
        		if(propertyName.equals("fields")) {
                	return new CFormRow();
        		}
        	}
        	throw newFacesException("Error while creating object {0} on parent {1}",propertyName,parent!=null?parent.getClass().getName():"<null>");
        }
		
		@Override
        public Object createArray(Object parent, String propertyName, List<Object> values) throws JsonException {
        	if(parent instanceof CForm) {
        		CForm form = (CForm)parent;
        		if(propertyName.equals("fields")) {
        			for(int i=0; i<values.size(); i++) {
        				form.addRow((CFormRow)values.get(i));
        			}
                	return form.getRows();
        		}
        	} else if(parent instanceof CFormRow) {
        		
        	}
        	throw newFacesException("Error while creating array {0} on parent {1}",propertyName,parent!=null?parent.getClass().getName():"<null>");
        }
		@Override
        public void setProperty(Object parent, String propertyName, Object value) throws JsonException {
        	if(parent instanceof CForm) {
        		CForm form = (CForm)parent;
        		if(propertyName.equals("title")) {
        			form.setTitle(asString(value));
        		} else if(propertyName.equals("description")) {
        			form.setDescription(asString(value));
        		} else if(propertyName.equals("fields")) {
        			// already set by create array
        			return;
        		}
        	} else if(parent instanceof CFormRow) {
        		CFormRow field = (CFormRow)parent;
        		if(propertyName.equals("field")) {
        			field.setField(asString(value));
        		} else if(propertyName.equals("label")) {
        			field.setLabel(asString(value));
        		} else if(propertyName.equals("type")) {
        			field.setType(asString(value));
        		} else if(propertyName.equals("multiple")) {
        			field.setMultiple(asBoolean(value));
        		} else if(propertyName.equals("required")) {
        			field.setRequired(asBoolean(value));
        		} else if(propertyName.equals("view")) {
        			field.setView(asBoolean(value));
        		} else if(propertyName.equals("sorted")) {
        			field.setSorted(asBoolean(value));
        		}
        	}
        }
        
        protected String asString(Object value) {
        	return (String)value;
        }
        protected boolean asBoolean(Object value) {
        	if(value instanceof Boolean) {
        		return (Boolean)value;
        	}
        	if(value instanceof String) {
        		if(value.equals("true")) {
        			return true;
        		}
        		if(value.equals("false")) {
        			return false;
        		}
        	}
        	throw newFacesException("Invalid boolean parameter {0}", value); 
        }
        
        protected FacesException newFacesException(String msg, Object... params) {
        	return new FacesExceptionEx(null,msg,params);
        }
	}
}
