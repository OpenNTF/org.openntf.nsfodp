/**
 * Copyright © 2019-2020 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.nsfdesign.fs.attribute;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NoneFileAttributeView implements PosixFileAttributeView, BasicFileAttributeView, FileOwnerAttributeView, UserDefinedFileAttributeView {
	
	private final Path path;
	
	public NoneFileAttributeView(Path path) {
		this.path = path;
		
	}

	@Override
	public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public UserPrincipal getOwner() throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public void setOwner(UserPrincipal owner) throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public String name() {
		return "posix"; //$NON-NLS-1$
	}

	@Override
	public PosixFileAttributes readAttributes() throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public void setGroup(GroupPrincipal group) throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public List<String> list() throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public int size(String name) throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public int read(String name, ByteBuffer dst) throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public int write(String name, ByteBuffer src) throws IOException {
		throw new NoSuchFileException(path.toString());
	}

	@Override
	public void delete(String name) throws IOException {
		throw new NoSuchFileException(path.toString());
	}

}
