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

import javax.faces.FacesException;

import com.ibm.xsp.component.UIPassThroughTag;

/**
 * XPages passthrough tag.
 * 
 * @author priand
 */
public class ControlPassthoughTag extends Control {
	
	private String tag;

	public ControlPassthoughTag(String tag) {
        super(null);
        this.tag = tag;
	}
	
	public String getTag() {
	    return tag;
	}

	@Override
    public Object newObject() throws FacesException {
        UIPassThroughTag pt = new UIPassThroughTag();
        pt.setTag(tag);
        //initProperties(pt);
        return pt;
    }
}
