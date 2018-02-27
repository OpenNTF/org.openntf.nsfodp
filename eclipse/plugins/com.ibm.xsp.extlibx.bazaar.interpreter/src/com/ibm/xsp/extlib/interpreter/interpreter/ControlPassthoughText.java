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

import com.ibm.xsp.component.UIPassThroughText;

/**
 * XPages passthrough text.
 * 
 * @author priand
 */
public class ControlPassthoughText extends Control {
	
	private String text;

	public ControlPassthoughText(String text) {
        super(null);
        this.text = text;
	}
	
	public String getText() {
	    return text;
	}

	@Override
    public Object newObject() throws FacesException {
        UIPassThroughText pt = new UIPassThroughText();
        pt.setText(text);
        //initProperties(pt);
        return pt;
    }
}
