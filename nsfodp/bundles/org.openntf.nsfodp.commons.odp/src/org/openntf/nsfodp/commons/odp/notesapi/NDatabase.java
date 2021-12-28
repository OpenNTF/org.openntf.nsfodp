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

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface NDatabase extends AutoCloseable {
	NotesAPI getAPI();
	
	NNote getNoteByID(int noteId);
	
	String getTitle();
	void setTitle(String title);
	
	String getFilePath();
	
	int getSharedFieldNoteID(String fieldName);
	
	void eachDesignNote(BiConsumer<Integer, NNote> consumer);
	
	/**
	 * Queries the design collection for notes matching the provided flags
	 * pattern and executes the provided {@code consumer} for each entry.
	 * 
	 * @param noteClass the note class mask to search
	 * @param pattern the pattern to query by
	 * @param consumer the {@link Consumer} to evaluate per entry
	 * @since 4.0.0
	 */
	void eachDesignEntry(int noteClass, String pattern, Consumer<NViewEntry> consumer);
	
	/**
	 * Queries the design collection to find a note of the given class, flags pattern,
	 * and title.
	 * 
	 * @param noteClass the note class mask to search
	 * @param pattern the pattern to query by
	 * @param title the $TITLE value of the note
	 * @return the design note, or {@code null} if not found
	 */
	NNote findDesignNote(int noteClass, String pattern, String title);
	
	/**
	 * Queries the design collection to find notes of a given class and matching
	 * a given pattern
	 * 
	 * @param noteClass the note class mask to search
	 * @param pattern the pattern to query by
	 * @return a {@link List} of {@link NViewEntry} instances
	 */
	List<NViewEntry> getDesignEntries(int noteClass, String pattern);
	
	short getCurrentAccessLevel();
	
	/**
	 * Determines the last-modification time of the database.
	 * 
	 * @return the last-modified time, as an {@link Instant}
	 * @since 4.0.0
	 */
	Instant getLastModified();
	
	@Override void close();
}
