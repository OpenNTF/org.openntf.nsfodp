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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.SimpleJavaFileObject;

/**
 * This file object contains the class byte code for a compiled java class.
 * 
 * @author priand
 */
public class JavaFileObjectJavaCompiled extends SimpleJavaFileObject {

	private ByteArrayOutputStream byteStream;	// CLASS

	public JavaFileObjectJavaCompiled(String name, Kind kind) {
		super(createUri(name), kind);
	}

	private static URI createUri(String name) {
		try {
			return new URI(name);
		} catch(URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public byte[] getByteCode() {
		if(byteStream==null) {
			throw new IllegalStateException();
		}
		return byteStream.toByteArray();
	}

	@Override
	public InputStream openInputStream() {
		return new ByteArrayInputStream(getByteCode());
	}

	@Override
	public OutputStream openOutputStream() {
		if(byteStream!=null) {
			throw new IllegalStateException();
		}
		this.byteStream=new ByteArrayOutputStream();
		return byteStream;
	}
}
