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

import com.ibm.xsp.extlib.interpreter.interpreter.Control;

/**
 * XPage Cache.
 * 
 * This class maintains a cache for the already loaded pages
 * 
 * @author priand
 */
public interface XPagesCache {

	public static interface CachedPage {
		public Control getControl();
		public Object getCacheInfo();
	}
	
	/**
	 * Check if a control can be created.
	 * 
	 * @param uri the control URI
	 * @param name the control name
	 * @throws FacesException if a security rule is violated
	 */
	public CachedPage getPage(String uri) throws FacesException;

	/**
	 * Check if a property can be assigned.
	 * @param object the object to assign the property to
	 * @param name the property name
	 * @param value the property value
	 * @throws FacesException if a security rule is violated
	 */
	public void putPage(String uri, Control control, Object cacheInfo) throws FacesException;
}
