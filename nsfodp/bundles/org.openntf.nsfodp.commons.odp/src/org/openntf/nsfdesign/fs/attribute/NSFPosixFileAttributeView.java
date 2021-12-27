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
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

import org.openntf.nsfdesign.fs.NSFPath;
import org.openntf.nsfdesign.fs.db.NSFAccessor;

/**
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NSFPosixFileAttributeView implements PosixFileAttributeView, BasicFileAttributeView, FileOwnerAttributeView {
	
	private final NSFPath path;
	
	public NSFPosixFileAttributeView(NSFPath path, LinkOption... options) {
		this.path = path;
    }

	@Override
	public String name() {
		return "posix"; //$NON-NLS-1$
	}

	@Override
	public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
		// lastAccessTime is intentionally ignored, as it cannot be set in the NSF
		NSFAccessor.setTimes(this.path, lastModifiedTime, createTime);
	}

	@Override
	public UserPrincipal getOwner() throws IOException {
		return this.readAttributes().owner();
	}

	@Override
	public void setOwner(UserPrincipal owner) throws IOException {
		NSFAccessor.setOwner(this.path, owner);
	}

	@Override
	public synchronized PosixFileAttributes readAttributes() throws IOException {
		return NSFAccessor.readAttributes(this.path);
	}

	@Override
	public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
		NSFAccessor.setPermissions(this.path, perms);
	}

	@Override
	public void setGroup(GroupPrincipal group) throws IOException {
		NSFAccessor.setGroup(this.path, group);
	}
	
}
