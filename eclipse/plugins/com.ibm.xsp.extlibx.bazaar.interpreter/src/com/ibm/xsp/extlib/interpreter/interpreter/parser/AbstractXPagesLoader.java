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

import com.ibm.xsp.FacesExceptionEx;

/**
 * Abstract XPage Uri loader.
 * 
 * @author priand
 */
public abstract class AbstractXPagesLoader implements XPagesLoader {

	public static class XPageContent implements XPage {
		private String content;
		private Object cacheInfo;
		public XPageContent(String content, Object cacheInfo) {
			this.content = content;
			this.cacheInfo = cacheInfo;
		}
		public String getContent() {
			return content;
		}
		public Object getCacheInfo() {
			return cacheInfo;
		}
	}
	
	public AbstractXPagesLoader() {
	}
	
	public XPage load(String uri) throws FacesException {
		throw new FacesExceptionEx(null,"Unknown XPage Uri {0}");
	}
	
	public boolean isCacheExpired(String uri, Object cacheInfo) throws FacesException {
		return false;
	}
}
