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

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.jscript.json.JsonJavaScriptFactory;
import com.ibm.jscript.types.FBSValue;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.complex.ValueBindingObjectImpl;
import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.extlib.interpreter.IControlBuilder;
import com.ibm.xsp.util.JavaScriptUtil;
import com.ibm.xsp.util.StateHolderUtil;


/**
 * Form Control Factory.
 * <p>
 * This factory is used to create simple form with fields.
 * </p>
 * @author priand
 */
public class FormControlBuilder extends ValueBindingObjectImpl implements IControlBuilder {

	private String nextPage;
	private String queryString;
	private String dataSource;
	private Object formContent;
	
	public FormControlBuilder() {
	}
	
	public String getNextPage() {
		if (null != this.nextPage) {
			return this.nextPage;
		}
		ValueBinding _vb = getValueBinding("nextPage"); //$NON-NLS-1$
		if (_vb != null) {
			return (java.lang.String) _vb.getValue(FacesContext.getCurrentInstance());
		} else {
			return null;
		}
	}

	public void setNextPage(String nextPage) {
		this.nextPage = nextPage;
	}
	
	public String getQueryString() {
		if (null != this.queryString) {
			return this.queryString;
		}
		ValueBinding _vb = getValueBinding("queryString"); //$NON-NLS-1$
		if (_vb != null) {
			return (java.lang.String) _vb.getValue(FacesContext.getCurrentInstance());
		} else {
			return null;
		}
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
	
	public String getDataSource() {
		if (null != this.dataSource) {
			return this.dataSource;
		}
		ValueBinding _vb = getValueBinding("dataSource"); //$NON-NLS-1$
		if (_vb != null) {
			return (java.lang.String) _vb.getValue(FacesContext.getCurrentInstance());
		} else {
			return null;
		}
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	
	public Object getFormContent() {
		if (null != this.formContent) {
			return this.formContent;
		}
		ValueBinding _vb = getValueBinding("formContent"); //$NON-NLS-1$
		if (_vb != null) {
			return _vb.getValue(FacesContext.getCurrentInstance());
		} else {
			return null;
		}
	}

	public void setFormContent(Object formContent) {
		this.formContent = formContent;
	}
	
	@Override
	public void restoreState(FacesContext _context, Object _state) {
		Object _values[] = (Object[]) _state;
		super.restoreState(_context, _values[0]);
        this.nextPage = (java.lang.String) _values[1];
        this.queryString = (java.lang.String) _values[2];
        this.dataSource = (java.lang.String) _values[3];
        this.formContent = StateHolderUtil.restoreObjectState(_context, getComponent(),_values[4]);
	}

	@Override
	public Object saveState(FacesContext _context) {
		Object _values[] = new Object[5];
		_values[0] = super.saveState(_context);
		_values[1] = nextPage;
		_values[2] = queryString;
		_values[3] = dataSource;
		_values[4] = StateHolderUtil.saveObjectState(_context, formContent);
		return _values;
	}

	
	// ========================================================================
	// Form Construction options
	// ========================================================================
	
	
	// ========================================================================
	// Construction of the UI
	// ========================================================================
	
	public CForm findForm() {
		Object json = getFormContent();
		if(json!=null) {
			String jsonString = null;
			if(json instanceof String) {
				jsonString = (String)json;
			} else if(json instanceof FBSValue) {
				try {
					jsonString = JsonGenerator.toJson(new JsonJavaScriptFactory(JavaScriptUtil.getJSContext()), json);
				} catch(Exception ex) {
					throw new FacesExceptionEx(ex,"jsonMarkup is not a valid Json object");
				}
			} else {
				try {
					jsonString = JsonGenerator.toJson(JsonJavaFactory.instanceEx, json);
				} catch(Exception ex) {
					throw new FacesExceptionEx(ex,"jsonMarkup is not a valid Json object");
				}
			}
			if(StringUtil.isNotEmpty(jsonString)) {
				try {
					CForm form = FormControlParser.get().parseJson(jsonString);
					return form;
				} catch(JsonException ex) {
					throw new FacesExceptionEx(ex);
				}
			}
		}
		return null;
	}
	
	public IControl generateXPagesControl() {
		CForm form = findForm();
		if(form!=null) {
			FormControlGenerator gen = new FormControlGenerator(this);
			return gen.generateXPagesControl(form);
		}
		return null;
	}
}
