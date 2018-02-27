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

package com.ibm.xsp.extlib.interpreter.interpreter.parser;

import javax.faces.component.UIComponent;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.extlib.interpreter.interpreter.ComplexProperty;
import com.ibm.xsp.extlib.interpreter.interpreter.Control;
import com.ibm.xsp.extlib.interpreter.interpreter.ControlFactory;
import com.ibm.xsp.extlib.interpreter.interpreter.ControlPassthoughTag;
import com.ibm.xsp.extlib.interpreter.interpreter.XPagesObject;
import com.ibm.xsp.registry.FacesComplexDefinition;
import com.ibm.xsp.registry.FacesComponentDefinition;
import com.ibm.xsp.registry.FacesDefinition;
import com.ibm.xsp.registry.FacesSharableRegistry;

/**
 * Default XPage Control Factory.
 * 
 * @author priand
 */
public class DefaultControlFactory implements ControlFactory {

	public static final String XP_NS="http://www.ibm.com/xsp/core";
	public static final String XC_NS="http://www.ibm.com/xsp/custom";
	public static final String XS_NS="http://www.ibm.com/xsp/extlib";
	public static final String XL_NS="http://www.ibm.com/xsp/labs";
	public static final String BZ_NS="http://www.ibm.com/xsp/bazaar";
	
	private FacesSharableRegistry registry;
	
    public DefaultControlFactory() {
        this(FacesContextEx.getCurrentInstance());
    }
    public DefaultControlFactory(FacesContextEx ctx) {
        this(ctx.getApplicationEx().getRegistry());
    }
	public DefaultControlFactory(FacesSharableRegistry registry) {
	    this.registry = registry;
	}
	
	public FacesSharableRegistry getRegistry() {
	    return registry;
	}
	
	public XPagesObject createXPagesObject(String nsUri, String tagName) {
		// Replace the view root component by a panel if the markup is a fragment
		// within an existing page
		if(tagName.equals("view") && StringUtil.equals(nsUri, DefaultControlFactory.XP_NS)) {
			//tagName = "panel";
			nsUri = BZ_NS;
			tagName = "com.ibm.xsp.extlib.interpreter.UIInterpreterPanel";
		}

	    // Check for passthrough tags
	    if(StringUtil.isEmpty(nsUri)) {
	        return new ControlPassthoughTag(tagName);
	    }
	    FacesDefinition def = registry.findDef(nsUri, tagName);
	    if(def==null) {
	        throw new FacesExceptionEx(null,"Unknown control/complex property, namespace {0}, tag {1}", nsUri, tagName);
	    }
	    if(def instanceof FacesComponentDefinition) {
	        return new Control((FacesComponentDefinition)def);
	    }
        if(def instanceof FacesComplexDefinition) {
            return new ComplexProperty((FacesComplexDefinition)def);
        }
        throw new FacesExceptionEx(null, "Invalid definition type {0} for tag {1}:{2}", def.getClass(), nsUri, tagName);
	}
}
