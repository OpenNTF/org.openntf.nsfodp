package org.openntf.nsfodp.commons.odp.notesapi;

import java.io.InputStream;
import java.util.Collection;

public interface NDXLImporter extends AutoCloseable {
	
	Collection<Integer> importDxl(NDatabase database, InputStream dxl);
	
	String getResultLogXML();
	
	@Override void close();
}
