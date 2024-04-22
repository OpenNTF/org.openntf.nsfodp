package org.openntf.com.ibm.xsp.extlib.bazaar.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * InputStream of a temporary file that automatically delete the
 * file when the stream is closed.
 * 
 * @since 3.5.0
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
