/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.nsfodp.commons.odp.designfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.openntf.nsfodp.commons.odp.designfs.db.DesignAccessor;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class DesignFileChannel extends FileChannel {
	
	private static final Set<? extends OpenOption> WRITE_OPTIONS = EnumSet.of(
		StandardOpenOption.APPEND,
		StandardOpenOption.CREATE,
		StandardOpenOption.CREATE_NEW,
		StandardOpenOption.TRUNCATE_EXISTING,
		StandardOpenOption.WRITE
	);
	
	private final DesignPath path;
	private final Path tempFile;
	private Set<? extends OpenOption> options;
	private final boolean openForWrite;
	
	public DesignFileChannel(DesignPath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) {
		this.path = path;
		this.options = options;
		
		// TODO implement TRUNCATE_EXISTING
		this.tempFile = DesignAccessor.extractAttachment(path);
		
		this.openForWrite = !Collections.disjoint(WRITE_OPTIONS, options);
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return getTempFileChannel().read(dst);
	}

	@Override
	public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
		return getTempFileChannel().read(dsts, offset, length);
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return getTempFileChannel().write(src);
	}

	@Override
	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
		return getTempFileChannel().write(srcs, offset, length);
	}

	@Override
	public long position() throws IOException {
		return getTempFileChannel().position();
	}

	@Override
	public FileChannel position(long newPosition) throws IOException {
		getTempFileChannel().position(newPosition);
		return this;
	}

	@Override
	public long size() throws IOException {
		return getTempFileChannel().size();
	}

	@Override
	public FileChannel truncate(long size) throws IOException {
		getTempFileChannel().truncate(size);
		return this;
	}

	@Override
	public void force(boolean metaData) throws IOException {
		// TODO update backend doc
		getTempFileChannel().force(metaData);
	}

	@Override
	public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
		return getTempFileChannel().transferTo(position, count, target);
	}

	@Override
	public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
		return getTempFileChannel().transferFrom(src, position, count);
	}

	@Override
	public int read(ByteBuffer dst, long position) throws IOException {
		return getTempFileChannel().read(dst, position);
	}

	@Override
	public int write(ByteBuffer src, long position) throws IOException {
		return getTempFileChannel().write(src, position);
	}

	@Override
	public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
		// TODO add wrapper MappedByteBuffer to capture modifications
		return getTempFileChannel().map(mode, position, size);
	}

	@Override
	public FileLock lock(long position, long size, boolean shared) throws IOException {
		// TODO implement doc locking
		return getTempFileChannel().lock(position, size, shared);
	}

	@Override
	public FileLock tryLock(long position, long size, boolean shared) throws IOException {
		// TODO implement doc locking
		return getTempFileChannel().tryLock(position, size, shared);
	}

	@Override
	protected void implCloseChannel() throws IOException {
		// TODO implement DELETE_ON_CLOSE
		getTempFileChannel().close();
		this.tempFileChannel = null;
		
		if(openForWrite) {
			DesignAccessor.storeAttachment(path, this.tempFile);
		}
		
		Files.deleteIfExists(this.tempFile);
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private FileChannel tempFileChannel;
	
	private synchronized FileChannel getTempFileChannel() throws IOException {
		if(this.tempFileChannel == null) {
			// TODO pass through options
			this.tempFileChannel = FileChannel.open(this.tempFile, this.options.toArray(new OpenOption[this.options.size()]));
		}
		return this.tempFileChannel;
	}
}
