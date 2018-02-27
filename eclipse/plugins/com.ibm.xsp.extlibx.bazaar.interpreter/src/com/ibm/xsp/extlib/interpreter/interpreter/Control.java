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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;

import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.registry.FacesComponentDefinition;

/**
 * XPages control.
 * 
 * @author priand
 */
public class Control extends XPagesObject implements IControl {
	
	private Control parent;
	private List<Control> children;
	private Map<String,Control> facets;

	public Control(FacesComponentDefinition definition) {
        super(definition);
	}
	
	//
	// IControl implementation
	//

    public String getId() {
        // Should we optimize this by managing a specific ID property?
        PropertySetter setter = getSetter("id");
        if(setter!=null) {
            return ((IdSetter)setter).getValue();
        }
        return null;
    }
	
    public UIComponent getComponent() {
        if(isLoaded()) {
            return (UIComponent)newObject();
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<IControl> getChildren() {
        return (List)children;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String,IControl> getFacets() {
        return (Map)facets;
    }

    
    //
    // XPages Object
    //
    
	public Control getParent() {
		return parent;
	}
	
	public int getChildCount() {
		return children!=null ? children.size() : 0;
	}
	
	public void addChild(Control child) {
		if(children==null) {
			children = new ArrayList<Control>();
		}
		children.add(child);
		child.parent = this;
	}
	
	
	public int getFacetCount() {
		return facets!=null ? facets.size() : 0;
	}

	public void addFacet(String facetName, Control child) {
		if(facets==null) {
			facets = new HashMap<String,Control>();
		}
		facets.put(facetName,child);
		child.parent = this;
	}

}
