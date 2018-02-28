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

package com.ibm.xsp.extlib.codemirror.component;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;



/**
 * Component that can be added to an existing text area to use the CodeMirror editor
 * instead of the regular text area.
 * <p>
 * </p>
 */
public class UICodeMirror extends UIComponentBase {
    
    public static final String RENDERER_TYPE = "com.ibm.xsp.extlib.codemirror.CodeMirror"; //$NON-NLS-1$
    
    public static final String COMPONENT_TYPE = "com.ibm.xsp.extlib.codemirror.CodeMirror"; //$NON-NLS-1$
    public static final String COMPONENT_FAMILY = "com.ibm.xsp.extlib.Bazaar"; //$NON-NLS-1$
    
    private String _for;
    private Boolean lineNumbers;
    
    public UICodeMirror() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }
        
    public String getFor() {
        if (_for == null) {
            ValueBinding vb = getValueBinding("for"); //$NON-NLS-1$
            if (vb != null) {
                return (String) vb.getValue(getFacesContext());
            }
        }
        return _for;
    }

    public void setFor(String _for) {
        this._for = _for;
    }

    public boolean isLineNumbers() {
        if (null != this.lineNumbers) {
            return this.lineNumbers;
        }
        ValueBinding _vb = getValueBinding("lineNumbers"); //$NON-NLS-1$
        if (_vb != null) {
            Boolean val = (java.lang.Boolean) _vb.getValue(FacesContext.getCurrentInstance());
            if(val!=null) {
                return val.booleanValue();
            }
        } 
        return false;
    }

    public void setLineNumbers(boolean lineNumbers) {
        this.lineNumbers = lineNumbers;
    }
    
    
    //
    // State management
    //
    @Override
    public void restoreState(FacesContext _context, Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.lineNumbers = (Boolean)_values[1];
    }

    @Override
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[2];
        _values[0] = super.saveState(_context);
        _values[1] = lineNumbers;
        return _values;
    }
}