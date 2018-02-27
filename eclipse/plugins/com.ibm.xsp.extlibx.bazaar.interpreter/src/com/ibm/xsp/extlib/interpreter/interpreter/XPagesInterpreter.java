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
import javax.faces.component.UIViewRoot;

import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.DefaultControlFactory;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.DefaultSecurityManager;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.XPagesLoader;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.XPagesParser;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.XPagesCache;


/**
 * XPages interpreter.
 * 
 * @author priand
 */
public class XPagesInterpreter {
	
	private UIViewRoot viewRoot;
	private SecurityManager security;
	private ControlFactory controlFactory;
	private XPagesLoader pageLoader;
	private XPagesCache pageCache;

	public XPagesInterpreter() {
		this(null,null,null,null,null);
	}

	public XPagesInterpreter(UIViewRoot viewRoot) {
		this(viewRoot,null,null,null,null);
	}
	
	public XPagesInterpreter(UIViewRoot viewRoot, ControlFactory controlFactory, SecurityManager security, XPagesLoader pageLoader, XPagesCache pageCache) {
		this.viewRoot = viewRoot;
		this.controlFactory = controlFactory!=null ? controlFactory : new DefaultControlFactory();
		this.security = security!=null ? security : new DefaultSecurityManager();
		this.pageLoader = pageLoader;
		this.pageCache = pageCache;
	}
	
	public SecurityManager getSecurity() {
		return security;
	}

	public void setSecurity(SecurityManager security) {
		this.security = security;
	}

	public ControlFactory getControlFactory() {
		return controlFactory;
	}

	public void setControlFactory(ControlFactory controlFactory) {
		this.controlFactory = controlFactory;
	}

	public XPagesLoader getPageLoader() {
		return pageLoader;
	}

	public void setPageLoader(XPagesLoader pageLoader) {
		this.pageLoader = pageLoader;
	}

	public XPagesCache getPageCache() {
		return pageCache;
	}

	public void setPageCache(XPagesCache pageCache) {
		this.pageCache = pageCache;
	}

	public Control parseUri(String uri) throws FacesException {
		// Look from the cache
		if(pageCache!=null) {
			XPagesCache.CachedPage page = pageCache.getPage(uri);
			if(page!=null) {
				// Return it if not expired
				if(!pageLoader.isCacheExpired(uri, page.getCacheInfo())) {
					return page.getControl();
				}
			}
		}
		
		// Else load the page
		if(pageLoader!=null) {
			XPagesLoader.XPage page = pageLoader.load(uri);
			
			// Create the control hierarchy
			XPagesParser parser = new XPagesParser(getControlFactory());
			Control c = parser.parse(page.getContent());
			
			// And add it back to the cache
			if(pageCache!=null) {
				pageCache.putPage(uri, c, page.getCacheInfo());
			}
			
			return c;
		}
		
		// Finally
		throw new FacesExceptionEx(null,"Uri {0} cannot be reached",uri);
	}
	
	public Control parseContent(String content) throws FacesException {
		XPagesParser parser = new XPagesParser(getControlFactory());
		if(content.indexOf("<xp:view ")<0) {
		    content =   "<xp:view xmlns:xp=\"http://www.ibm.com/xsp/core\" xmlns:xe=\"http://www.ibm.com/xsp/coreex\" xmlns:xc=\"http://www.ibm.com/xsp/custom\">\n"
		              + content + "\n"
		              + "</xp:view>\n";
		}
		Control c = parser.parse(content);
		return c;
	}
}
