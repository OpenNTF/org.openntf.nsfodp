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
package org.openntf.nsfdesign.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.sshd.common.file.util.BaseFileSystem;

public class NSFFileSystem extends BaseFileSystem<NSFPath> {
	
	private final String userName;
	private final String nsfPath;
	private final List<FileStore> fileStores;
	
	public NSFFileSystem(NSFFileSystemProvider provider, String userName, String nsfPath) {
		super(provider);
		
		this.userName = userName;
		this.nsfPath = nsfPath;
		this.fileStores = Arrays.asList(new NSFFileStore(this));
	}

	@Override
	protected NSFPath create(String root, List<String> names) {
		List<String> bits = names == null || names.isEmpty() ? Arrays.asList("") : names; //$NON-NLS-1$
		return new NSFPath(this, root, bits);
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		return new HashSet<>(Arrays.asList("basic", "posix", "owner", "user")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		return null;
	}
	
	@Override
	public Iterable<FileStore> getFileStores() {
		return fileStores;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(userName, nsfPath);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof NSFFileSystem)) {
			return false;
		}
		NSFFileSystem o = (NSFFileSystem)obj;
		return userName.equals(o.userName) && nsfPath.equals(o.nsfPath);
	}
	
	// *******************************************************************************
	// * Domino-specific methods
	// *******************************************************************************
	
	public String getUserName() {
		return userName;
	}
	
	public String getNsfPath() {
		return nsfPath;
	}
}
