package org.openntf.nsfodp.commons.odp.notesapi;

import java.io.IOException;
import java.io.OutputStream;

public interface NCompositeData extends AutoCloseable {
	void writeJavaScriptLibraryData(OutputStream os) throws IOException;
	void writeFileResourceData(OutputStream os) throws IOException;
	void writeImageResourceData(OutputStream os) throws IOException;
	
	@Override void close();
}
