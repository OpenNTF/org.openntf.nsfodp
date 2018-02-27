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

/**
 * XPage Uri loader.
 * 
 * This class gives access to the content of an XPage based on
 * a URI. It also gives basic functionality for maintaining a
 * cache.
 * 
 * @author priand
 */
public interface XPagesLoader {

	public static interface XPage {
		public String getContent();
		public Object getCacheInfo();
	}
	
	/**
	 * Load the XPage markup.
	 * 
	 * @param uri the uri to load from
	 * @return an XPage object
	 * @throws FacesException if the page doesn't exist
	 */
	public XPage load(String uri) throws FacesException;
	
	/**
	 * Check if a cache as expired.
	 */
	public boolean isCacheExpired(String uri, Object cacheInfo) throws FacesException;
}
