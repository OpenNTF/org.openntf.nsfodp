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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

import org.openntf.com.ibm.xsp.extlib.bazaar.BazaarUtil;

import com.ibm.commons.util.io.StreamUtil;

/**
 * JavaFileObject loaded from a class.
 * 
 * @author priand
 */
public class JavaFileObjectClass implements JavaFileObject, Closeable {

	private Path path;
	private String binaryName;
	private String name;
	private Collection<InputStream> openedStreams = new ArrayList<>();

	public JavaFileObjectClass(Path path, String binaryName) {
		this.path = path;
		this.binaryName=binaryName;
		this.name = path.toString();
	}

	public String binaryName() {
		return binaryName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Kind getKind() {
		return Kind.CLASS;
	}

	@Override
	public URI toUri() {
		return path.toUri();
	}

	@Override
	public InputStream openInputStream() throws IOException {
		InputStream is = BazaarUtil.newInputStream(path);
		this.openedStreams.add(is);
		return is;
	}
	
	@Override
	public void close() {
		this.openedStreams.forEach(StreamUtil::close);
		this.openedStreams.clear();
	}
	
	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		String baseName=simpleName+kind.extension;
		return kind.equals(getKind())&&(baseName.equals(getName())||getName().endsWith("/"+baseName));
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	
	//
	// Unsupported methods
	//

	@Override
	public OutputStream openOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Writer openWriter() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public NestingKind getNestingKind() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Modifier getAccessLevel() {
		throw new UnsupportedOperationException();
	}
}
