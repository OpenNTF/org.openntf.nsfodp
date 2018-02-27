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

import com.ibm.commons.util.SystemCache;
import com.ibm.xsp.extlib.interpreter.interpreter.Control;

/**
 * XPage Cache.
 * 
 * Default XPage cache implementation.
 * 
 * @author priand
 */
public class DefaultXPagesCache implements XPagesCache {
	
	public static class Page implements CachedPage {
		private Control control;
		private Object cacheInfo;
		public Page(Control control, Object cacheInfo) {
			this.control = control;
			this.cacheInfo = cacheInfo;
		}
		public Control getControl() {
			return control;
		}
		public Object getCacheInfo() {
			return cacheInfo;
		}
	}
	
	private SystemCache cache;
	
	public DefaultXPagesCache(String title, int cacheSize) {
		this.cache = new SystemCache(title,cacheSize);
	}
	
	public CachedPage getPage(String uri) throws FacesException {
		return (CachedPage)cache.get(uri);
	}

	public void putPage(String uri, Control control, Object cacheInfo) throws FacesException {
		Page page = new Page(control,cacheInfo);
		cache.put(uri, page);
	}

}
