/*
 * © Copyright IBM Corp. 2013
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
package com.ibm.xsp.extlib.javacompiler;

import java.io.PrintWriter;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import com.ibm.commons.util.AbstractException;

/**
 * Exception thrown when the compiler fail to compile classes.
 * 
 * @author priand
 */
public class JavaCompilerException extends AbstractException {

	private static final long serialVersionUID	= 1L;

	private DiagnosticCollector<JavaFileObject> diagnosticCollector;

	public JavaCompilerException(Throwable cause, DiagnosticCollector<JavaFileObject> diagnosticCollector, String message, Object... params) {
		super(cause, message, params);
		this.diagnosticCollector=diagnosticCollector;
	}

	public DiagnosticCollector<JavaFileObject> getDiagnosticCollector() {
		return diagnosticCollector;
	}

	public void printExtraInformation(PrintWriter err) {
		List<Diagnostic<? extends JavaFileObject>> l = diagnosticCollector.getDiagnostics();
		for(Diagnostic<? extends JavaFileObject> d: l) {
			err.println(d.toString());
		}
	}
	
}
