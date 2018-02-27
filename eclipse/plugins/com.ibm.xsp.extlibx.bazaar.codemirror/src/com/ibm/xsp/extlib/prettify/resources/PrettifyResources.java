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

package com.ibm.xsp.extlib.prettify.resources;

import com.ibm.xsp.resource.DojoModuleResource;
import com.ibm.xsp.resource.StyleSheetResource;

/**
 * Shared Prettify Resources.
 * 
 * @author priand
 *
 */
public class PrettifyResources {


    public static final DojoModuleResource dojoPrettify = new DojoModuleResource("extlib.prettify.prettify"); // $NON-NLS-1$
 
    public static final StyleSheetResource cssPrettify = new StyleSheetResource("/.ibmxspres/.extlib/prettify/prettify.css");   // $NON-NLS-1$
   
}