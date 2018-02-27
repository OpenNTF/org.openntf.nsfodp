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
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;


/**
 * XPage Control SecurityManager.
 * 
 * This class controls what is allowed by the interpreter.
 * The security applies when the Control tree is created and 
 * prevents its construction if it violates a security rule.
 * 
 * @author priand
 */
public interface SecurityManager {

	/**
	 * Check if a control can be created.
	 * 
	 * @param uri the control URI
	 * @param name the control name
	 * @throws FacesException if a security rule is violated
	 */
	public void checkCreateControl(String uri, String name) throws FacesException;

	/**
	 * Check if a property can be assigned.
	 * 
	 * <ul>
	 * For now, a value can be:
	 *   <li>a string</li>
	 *   <li>a number (Integer, Long...)</li>
	 *   <li>a boolean</li>
	 *   <li>a complex property</li>
	 *   <li>a value binding</li>
	 * </ul>
	 * 
	 * @param object the object to assign the property to
	 * @param name the property name
	 * @param value the property value
	 * @throws FacesException if a security rule is violated
	 */
	public void checkSetProperty(Object object, String name, Object value) throws FacesException;

	/**
	 * Check if a load time binding is allowed.
	 * 
	 * This can be used, for example, to prevent JavaScript from being used.
	 * For more granularity about setting a value binding to a particular property,
	 * one should use checkSetProperty()instead.
	 *  
	 * @throws FacesException if a security rule is violated
	 */
	public void checkLoadtimeBinding(Object object, String name, ValueBinding vb) throws FacesException;

    /**
     * Check if a value binding is allowed.
     * 
     * This can be used, for example, to prevent JavaScript from being used.
     * For more granularity about setting a value binding to a particular property,
     * one should use checkSetProperty()instead.
     *  
     * @throws FacesException if a security rule is violated
     */
    public void checkRuntimeBinding(Object object, String name, ValueBinding vb) throws FacesException;

    /**
     * Check if a method binding is allowed.
     * 
     * This can be used, for example, to prevent JavaScript from being used.
     * For more granularity about setting a method binding to a particular property,
     * one should use checkSetProperty()instead.
     *  
     * @throws FacesException if a security rule is violated
     */
    public void checkMethodBinding(Object object, String name, MethodBinding vb) throws FacesException;
}
