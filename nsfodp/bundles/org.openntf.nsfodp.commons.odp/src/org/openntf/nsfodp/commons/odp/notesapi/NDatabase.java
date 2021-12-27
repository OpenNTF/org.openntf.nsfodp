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
	
	short getCurrentAccessLevel();
	
	/**
	 * Determines the last-modification time of the database.
	 * 
	 * @return the last-modified time, as a Unix timestamp
	 * @since 4.0.0
	 */
	long getLastModified();
	
	@Override void close();
}
