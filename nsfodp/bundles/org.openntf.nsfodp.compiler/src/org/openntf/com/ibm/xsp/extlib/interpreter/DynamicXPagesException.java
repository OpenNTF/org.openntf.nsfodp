/*
 * � Copyright IBM Corp. 2013
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

import java.io.PrintWriter;

import com.ibm.commons.util.AbstractException;

/**
 * Exception thrown when the compiler fail to compile classes.
 * 
 * @author priand
 */
public class DynamicXPagesException extends AbstractException {
	
	public static boolean DEBUG = false;

	private static final long serialVersionUID	= 1L;

	private String xpagesSource;
	private String translatedJava;
	
	public DynamicXPagesException(Throwable cause, String xpagesSource, String translatedJava, String message, Object... params) {
		super(cause, message, params);
		this.xpagesSource = xpagesSource;
		this.translatedJava = translatedJava;
	}
	
	public String getXPagesSource() {
		return xpagesSource;
	}
	
	public String getTranslatedJava() {
		return translatedJava;
	}

	public void printExtraInformation(PrintWriter err) {
		if(DEBUG) {
			if(xpagesSource!=null) {
				err.println(xpagesSource);
			}
			if(translatedJava!=null) {
				err.println(translatedJava);
			}
		}
	}
	
}
