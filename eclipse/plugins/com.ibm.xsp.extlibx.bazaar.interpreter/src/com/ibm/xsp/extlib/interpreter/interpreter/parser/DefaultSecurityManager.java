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

import javax.faces.FacesException;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.extlib.interpreter.interpreter.SecurityManager;

/**
 * XPage Default Control SecurityManager.
 * 
 * @author priand
 */
public class DefaultSecurityManager implements SecurityManager {
    
    public static DefaultSecurityManager instance = new DefaultSecurityManager();

	public void checkCreateControl(String uri, String name) throws FacesException {
		// Just allowed
	}

	public void checkSetProperty(Object object, String name, Object value) throws FacesException {
		// Just allowed
	}

	public void checkLoadtimeBinding(Object object, String name, ValueBinding valueBinding) throws FacesException {
		// Just allowed
	}

    public void checkRuntimeBinding(Object object, String name, ValueBinding valueBinding) throws FacesException {
        // Just allowed
    }

    public void checkMethodBinding(Object object, String name, MethodBinding valueBinding) throws FacesException {
        // Just allowed
    }
}
