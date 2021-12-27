package org.openntf.nsfodp.commons.odp.designfs.attribute;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import org.openntf.nsfodp.commons.odp.designfs.DesignPath;
import org.openntf.nsfodp.commons.odp.designfs.db.DesignAccessor;

public class DesignFileAttributeView implements BasicFileAttributeView {
	private final DesignPath path;
	
	public DesignFileAttributeView(DesignPath path, LinkOption... options) {
		this.path = path;
	}

	@Override
	public String name() {
		return "basic"; //$NON-NLS-1$
	}

	@Override
	public BasicFileAttributes readAttributes() throws IOException {
		return DesignAccessor.readAttributes(path);
	}

	@Override
	public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
		// NOP
	}

}
