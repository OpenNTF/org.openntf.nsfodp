package org.openntf.nsfodp.commons.odp.notesapi;

import java.util.function.BiConsumer;

public interface NDatabase extends AutoCloseable {
	NotesAPI getAPI();
	
	NNote getNoteByID(int noteId);
	
	String getTitle();
	void setTitle(String title);
	
	String getFilePath();
	
	int getSharedFieldNoteID(String fieldName);
	
	void eachDesignNote(BiConsumer<Integer, NNote> consumer);
	
	@Override void close();
}
