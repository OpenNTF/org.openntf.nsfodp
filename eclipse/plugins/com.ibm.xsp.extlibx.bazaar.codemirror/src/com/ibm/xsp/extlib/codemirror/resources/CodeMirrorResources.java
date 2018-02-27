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

package com.ibm.xsp.extlib.codemirror.resources;

import com.ibm.xsp.resource.DojoModuleResource;
import com.ibm.xsp.resource.StyleSheetResource;

/**
 * Shared CodeMirror Resources.
 * 
 * @author priand
 *
 */
public class CodeMirrorResources {


    public static final DojoModuleResource dojoCodeMirror = new DojoModuleResource("extlib.codemirror.lib.codemirror"); // $NON-NLS-1$
    public static final DojoModuleResource dojoModeJavaScript = new DojoModuleResource("extlib.codemirror.mode.javascript.javascript"); // $NON-NLS-1$
    public static final DojoModuleResource dojoModeXml = new DojoModuleResource("extlib.codemirror.mode.xml.xml"); // $NON-NLS-1$
    public static final DojoModuleResource dojoModeCss = new DojoModuleResource("extlib.codemirror.mode.css.css"); // $NON-NLS-1$
 
    public static final StyleSheetResource cssCodeMirror = new StyleSheetResource("/.ibmxspres/.extlib/codemirror/lib/codemirror.css");   // $NON-NLS-1$
   
}