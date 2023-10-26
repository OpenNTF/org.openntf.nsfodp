/**
 * Copyright © 2018-2023 Jesse Gallagher
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
package org.openntf.nsfodp.commons;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * InputStream of a temporary file that automatically delete the
 * file when the stream is closed.
 * 
 * @since 3.7.0
 */
public class TempFileInputStream extends InputStream {
	private Path tmpFile;
    private InputStream inputStream;
    
    public TempFileInputStream(Path tmpFile, OpenOption... options) throws IOException {
    	this.tmpFile = tmpFile;
        this.inputStream = Files.newInputStream(tmpFile, options);
    }

    @Override
    public int available() throws IOException {
    	return this.inputStream.available();
    }
    
    @Override
    public synchronized void mark(int readlimit) {
    	this.inputStream.mark(readlimit);
    }
    
    @Override
    public boolean markSupported() {
    	return this.inputStream.markSupported();
    }
    
    @Override
    public int read(byte[] b) throws IOException {
    	return this.inputStream.read(b);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
    	return this.inputStream.read(b, off, len);
    }
    
    @Override
    public synchronized void reset() throws IOException {
    	this.inputStream.reset();
    }
    
    @Override
    public long skip(long n) throws IOException {
    	return this.inputStream.skip(n);
    }
    
    @Override
    public void close() throws IOException {
    	this.inputStream.close();
    	Files.deleteIfExists(this.tmpFile);
    }
    
    @Override
    protected void finalize() throws Throwable {
    	this.inputStream.close();
    	Files.deleteIfExists(this.tmpFile);
    }
    
    @Override
    public int read() throws IOException {
    	return this.inputStream.read();
    }
}
