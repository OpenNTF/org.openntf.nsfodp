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

package com.ibm.xsp.extlib.interpreter;

import java.util.List;
import java.util.Locale;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.FacesComponent;
import com.ibm.xsp.component.UIPassThroughTag;
import com.ibm.xsp.component.xp.XspOutputText;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.extlib.builder.ControlBuilder;
import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.extlib.builder.DynamicComponentFactory;
import com.ibm.xsp.extlib.component.dynamiccontent.AbstractDynamicContent;
import com.ibm.xsp.page.FacesComponentBuilder;
import com.ibm.xsp.page.FacesPage;
import com.ibm.xsp.page.FacesPageDispatcher;
import com.ibm.xsp.page.FacesPageDriver;
import com.ibm.xsp.page.FacesPageException;
import com.ibm.xsp.util.FacesUtil;



/**
 * Dynamic panel that selects a facet to display.
 * <p>
 * </p>
 */
public class UIControlFactory extends AbstractDynamicContent implements NamingContainer, FacesComponent, DynamicComponentFactory {

    public static final String COMPONENT_FAMILY = "com.ibm.xsp.ControlFactory"; // $NON-NLS-1$
	
    public static final String COMPONENT_TYPE = "com.ibm.xsp.extlib.UIControlFactory"; // $NON-NLS-1$
    public static final String RENDERER_TYPE = "com.ibm.xsp.extlib.UIControlFactory"; //$NON-NLS-1$

    private IControlBuilder controlBuilder;
    private Boolean autoCreate;
   
    /**
     * 
     */
    public UIControlFactory() {
    	setRendererType(RENDERER_TYPE);
    }
    
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

	@Override
    public boolean isAutoCreate() {
        if (null != this.autoCreate) {
            return this.autoCreate;
        }
        ValueBinding _vb = getValueBinding("autoCreate"); //$NON-NLS-1$
        if (_vb != null) {
            Boolean val = (java.lang.Boolean) _vb.getValue(FacesContext.getCurrentInstance());
            if(val!=null) {
                return val;
            }
        } 
        return false;
    }
	
	public void setAutoCreate(boolean autoCreate) {
		this.autoCreate = autoCreate;
	}
	
	public IControlBuilder getControlBuilder() {
		if (null != this.controlBuilder) {
			return this.controlBuilder;
		}
		ValueBinding _vb = getValueBinding("controlBuilder"); //$NON-NLS-1$
		if (_vb != null) {
			return (IControlBuilder) _vb.getValue(getFacesContext());
		}
		return null;
	}

	public void setControlBuilder(IControlBuilder controlBuilder) {
		this.controlBuilder = controlBuilder;
	}

	@Override
	public void restoreState(FacesContext _context, Object _state) {
		Object _values[] = (Object[]) _state;
		super.restoreState(_context, _values[0]);
        this.autoCreate = (Boolean)_values[1];
        this.controlBuilder = (IControlBuilder) FacesUtil.objectFromSerializable(_context, this, _values[2]);
	}

	@Override
	public Object saveState(FacesContext _context) {
		Object _values[] = new Object[3];
		_values[0] = super.saveState(_context);
        _values[1] = autoCreate;
        _values[2] = FacesUtil.objectToSerializable(_context, controlBuilder);
		return _values;
	}
    
    public void createContent() {
        FacesContextEx ctx = FacesContextEx.getCurrentInstance();
        createContent(ctx);
    }
    
    public void deleteContent() {
        FacesContextEx ctx = FacesContextEx.getCurrentInstance();
        deleteContent(ctx);
    }
    
    
	
    // ====================================================================
    //  Dynamic component factory
    // ====================================================================
    
    public FacesPageDriver getPageDriver() {
        return new PageDriver();
    }
    
    public String getSourceComponentRef() {
        //This is not needed for this component as it contains the page definition itself        
        return null;
    }
    
    protected class PageDriver implements FacesPageDriver {
        protected PageDriver() {
        }
        public FacesPageDispatcher loadPage(FacesContext context, String pageName) throws FacesPageException {
            return new PageDispatcher(pageName);
        }
    }

    protected class PageDispatcher implements FacesPageDispatcher {
        protected PageDispatcher(String pageName) {
        }
        public FacesPage loadPage(FacesContext context, String renderKitId, Locale locale) {
            return new Page();
        }
    }

    protected class Page implements FacesPage {
        protected Page() {
        }
        public UIViewRoot createViewRoot(FacesContext context) throws FacesPageException, UnsupportedOperationException {
            throw new FacesPageException("The dynamic factory control cannot create a whole page");
        }
        public void addComponent(FacesContext context, FacesComponentBuilder builder, UIComponent parent, String initialTag) throws FacesPageException, UnsupportedOperationException {
            IControl ic = UIControlFactory.this.createIControl();
            if(ic!=null) {
                if(StringUtil.isNotEmpty(initialTag)) {
                    ic = findControl(ic, initialTag);
                    if(ic==null) {
                        throw new FacesPageException(StringUtil.format("The control with id {0} doesn't exist",initialTag));
                    }
                }
                UIComponent c = ControlBuilder.buildControl(context, ic, true);
                parent.getChildren().add(c);
            }
        }
        protected IControl findControl(IControl control, String id) {
            String cid = control.getId();
            if(StringUtil.equals(cid, id)) {
                return control;
            }
            List<IControl> children = control.getChildren();
            if(children!=null) {
                int count = children.size();
                for(int i=0; i<count; i++) {
                    IControl ic = findControl(children.get(i),id);
                    if(ic!=null) {
                        return ic;
                    }
                }
            }
            return null;
        }
    }

    
	// ====================================================================
	//	Dynamic Form Management
	// ====================================================================

    public IControl createIControl() {
        IControlBuilder builder = getControlBuilder();
        if(builder!=null) {
            try {
                IControl control = builder.generateXPagesControl();
                if(control!=null) {
                    return control;
                }
            } catch(Throwable t) {
                UIComponent err = loadErrorComponent(t);
                if(err!=null) {
                    return new ControlBuilder.ControlImpl<UIComponent>(err);
                }
            }
        }
        return null;
    }
    
	@Override
    public void createChildren(FacesContextEx context) {
	    IControl ic = createIControl();
	    if(ic!=null) {
	        ControlBuilder.buildControl(context, this, this, ic, true);
	    }
    }

	public UIComponent loadErrorComponent(Throwable t) {
        UIPassThroughTag tag = new UIPassThroughTag();
        tag.setTag("pre");
        XspOutputText lbl = new XspOutputText();
        StringBuilder b = new StringBuilder();
        for( Throwable ex=t; ex!=null; ex=ex.getCause() ) {
            b.append("\n");
            b.append(ex.getMessage());
        }
        
        lbl.setValue("Error while parsing Content"+b);
        tag.getChildren().add(lbl);
        return tag;
    }
	
	
	//
	// ++++ TODO ++++
	// See what is needed here.
	//
	
	protected void updateForm(FacesContext context) {
		// Clear the existing children
		clearForm(context);
		
		// And create the new content
		createForm(context);
	}

	protected void clearForm(FacesContext context) {
		if(getChildCount()>0) {
			getChildren().clear();
		}
	}

	protected void createForm(FacesContext context) {
	}	
}
