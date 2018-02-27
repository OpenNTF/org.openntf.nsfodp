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

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.complex.ValueBindingObjectImpl;
import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.extlib.interpreter.IControlBuilder;


/**
 * Markup Control Factory.
 * <p>
 * This factory is used to create XPages code from XML markup.
 * </p>
 * @author priand
 */
public class MarkupControlBuilder extends ValueBindingObjectImpl implements IControlBuilder {

	private String xmlMarkup;
    private String uri;
	
	public MarkupControlBuilder() {
	}
	
	public String getXmlMarkup() {
		if (null != this.xmlMarkup) {
			return this.xmlMarkup;
		}
		ValueBinding _vb = getValueBinding("xmlMarkup"); //$NON-NLS-1$
		if (_vb != null) {
			return (java.lang.String) _vb.getValue(FacesContext.getCurrentInstance());
		} else {
			return null;
		}
	}

	public void setXmlMarkup(String xmlMarkup) {
		this.xmlMarkup = xmlMarkup;
	}
    
    public String getUri() {
        if (null != this.uri) {
            return this.uri;
        }
        ValueBinding _vb = getValueBinding("uri"); //$NON-NLS-1$
        if (_vb != null) {
            return (java.lang.String) _vb.getValue(FacesContext.getCurrentInstance());
        } else {
            return null;
        }
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
	
	@Override
	public void restoreState(FacesContext _context, Object _state) {
		Object _values[] = (Object[]) _state;
		super.restoreState(_context, _values[0]);
        this.xmlMarkup = (java.lang.String) _values[1];
        this.uri = (java.lang.String) _values[2];
	}

	@Override
	public Object saveState(FacesContext _context) {
		Object _values[] = new Object[3];
		_values[0] = super.saveState(_context);
		_values[1] = xmlMarkup;
        _values[2] = uri;
		return _values;
	}

	
	// ========================================================================
	// Construction of the UI
	// ========================================================================

    public IControl generateXPagesControl() {
        // Look for a URI
        String uri = getUri();
        if(StringUtil.isNotEmpty(uri)) {
            XPagesInterpreter it = loadInterpreter(FacesContext.getCurrentInstance());
            return it.parseUri(uri);
        }
        
        // Then look for a string content
        String content = getXmlMarkup();
        if(StringUtil.isNotEmpty(content)) {
            XPagesInterpreter it = loadInterpreter(FacesContext.getCurrentInstance());
            return it.parseContent(content);
        }
        
        // Ok, nothing to return
        return null;
    }
	
    public XPagesInterpreter loadInterpreter(FacesContext context) {
        return new XPagesInterpreter();
    }
}
