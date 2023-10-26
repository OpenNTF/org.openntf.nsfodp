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
package org.openntf.com.ibm.xsp.extlib.javacompiler.impl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.SimpleJavaFileObject;

/**
 * This file object contains the source code for a java class.
 * 
 * @author priand
 */
public class JavaFileObjectJavaSource extends SimpleJavaFileObject {

	private CharSequence sourceCode;			// SOURCE

	public JavaFileObjectJavaSource(String name, CharSequence sourceCode) {
		super(createUri(name), Kind.SOURCE);
		this.sourceCode=sourceCode;
	}

	private static URI createUri(String name) {
		try {
			return new URI(name);
		} catch(URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws UnsupportedOperationException {
		if(sourceCode==null) {
			throw new IllegalStateException();
		}
		return sourceCode;
	}
}
