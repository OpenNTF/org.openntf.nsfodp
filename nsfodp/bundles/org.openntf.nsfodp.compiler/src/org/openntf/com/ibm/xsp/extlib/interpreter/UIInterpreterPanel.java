/*
 * � Copyright IBM Corp. 2010
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

package org.openntf.com.ibm.xsp.extlib.interpreter;

import javax.faces.context.FacesContext;

import com.ibm.xsp.application.NavigationRule;
import com.ibm.xsp.component.UIPanelEx;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.resource.Resource;

/**
 * Panel used by the interpreter to wrap a 'ViewRoot' component within a page.
 * <p>
 * </p>
 */
public class UIInterpreterPanel extends UIPanelEx {

    public static final String COMPONENT_TYPE = "com.ibm.xsp.extlib.interpreter.UIInterpreterPanel"; // $NON-NLS-1$

    public UIInterpreterPanel() {
    	setRendererType(RENDERER_TYPE);
    }
    
    public UIViewRootEx getViewRoot() {
    	return (UIViewRootEx)FacesContext.getCurrentInstance().getViewRoot();
    	//return (UIViewRootEx)FacesUtil.getViewRoot(this);
    }
    
    
    
    //
    // FacesPageProvider
    //

    public void addResource(Resource resource) {
    	UIViewRootEx root = getViewRoot();
    	root.addResource(resource);
    }
    
    public void addNavigationRule(NavigationRule rule) {
    	UIViewRootEx root = getViewRoot();
    	root.addNavigationRule(rule);
    }
    
    public boolean isDojoTheme() {
    	UIViewRootEx root = getViewRoot();
    	return root.isDojoTheme();
    }

    public void setDojoTheme(boolean dojoTheme) {
    	UIViewRootEx root = getViewRoot();
    	root.setDojoTheme(dojoTheme);
    }
    
    public boolean isDojoParseOnLoad() {
    	UIViewRootEx root = getViewRoot();
    	return root.isDojoParseOnLoad();
    }

    public void setDojoParseOnLoad(boolean dojoParseOnLoad) {
    	UIViewRootEx root = getViewRoot();
    	root.setDojoParseOnLoad(dojoParseOnLoad);
    }
    public void setDojoForm(boolean dojoForm) {
    	UIViewRootEx root = getViewRoot();
    	root.setDojoForm(dojoForm);
    }
    
    public void setEnableModifiedFlag(boolean enableModifiedFlag) {
    	UIViewRootEx root = getViewRoot();
    	root.setEnableModifiedFlag(enableModifiedFlag);
    }


    public void setPageManifest(String pageManifest) {
    	UIViewRootEx root = getViewRoot();
    	root.setPageManifest(pageManifest);
    }
}
