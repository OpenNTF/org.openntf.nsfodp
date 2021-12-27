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

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NSFFileAttributes implements BasicFileAttributes, PosixFileAttributes {
	public enum Type {
		File, Folder
	}
	
	private UserPrincipal owner;

	private GroupPrincipal group;
	private Type type;
	private FileTime lastModified;
	private FileTime lastAccessed;
	private FileTime created;
	private long size;
	private Set<PosixFilePermission> permissions;

	public NSFFileAttributes(UserPrincipal owner, GroupPrincipal group, Type type, FileTime lastModified,
			FileTime lastAccessed, FileTime created, long size, Set<PosixFilePermission> permissions) {
		super();
		this.owner = owner;
		this.group = group;
		this.type = type;
		this.lastModified = lastModified;
		this.lastAccessed = lastAccessed;
		this.created = created;
		this.size = size;
		this.permissions = permissions;
	}

	@Override
	public UserPrincipal owner() {
		return owner;
	}

	@Override
	public GroupPrincipal group() {
		return group;
	}

	@Override
	public Set<PosixFilePermission> permissions() {
		return permissions;
	}

	@Override
	public FileTime lastModifiedTime() {
		return lastModified;
	}

	@Override
	public FileTime lastAccessTime() {
		return lastAccessed;
	}

	@Override
	public FileTime creationTime() {
		return created;
	}

	@Override
	public boolean isRegularFile() {
		return type == Type.File;
	}

	@Override
	public boolean isDirectory() {
		return type == Type.Folder;
	}

	@Override
	public boolean isSymbolicLink() {
		return false;
	}

	@Override
	public boolean isOther() {
		return false;
	}

	@Override
	public long size() {
		return type == Type.File ? size : 0;
	}

	@Override
	public Object fileKey() {
		return null;
	}
}
