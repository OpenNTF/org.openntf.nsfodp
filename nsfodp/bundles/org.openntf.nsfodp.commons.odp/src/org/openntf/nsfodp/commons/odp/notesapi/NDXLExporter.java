package org.openntf.nsfodp.commons.odp.notesapi;

import java.io.OutputStream;
import java.util.Collection;

public interface NDXLExporter extends AutoCloseable {
	void setForceNoteFormat(boolean forceNoteFormat);
	boolean isForceNoteFormat();
	void setRichTextAsItemData(boolean richTextAsItemData);
	void setOmitItemNames(Collection<String> itemNames);
	void setProperty(int property, boolean value);
	
	void export(NDatabase database, Collection<Integer> noteIds, OutputStream os);
	void export(NNote note, OutputStream os);
	
	@Override void close();
}
