/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.nsfodp.notesapi.darwinonapi;

import java.util.function.BiConsumer;

import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NDominoException;
import org.openntf.nsfodp.commons.odp.notesapi.NNote;
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;

import com.darwino.domino.napi.DominoAPI;
import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.c.C;
import com.darwino.domino.napi.proc.NSFSEARCHPROC;
import com.darwino.domino.napi.struct.SEARCH_MATCH;
import com.darwino.domino.napi.wrap.FormulaException;
import com.darwino.domino.napi.wrap.NSFDatabase;
import com.darwino.domino.napi.wrap.NSFNote;

public class DarwinoNDatabase implements NDatabase {
	private final DarwinoNotesAPI notesApi;
	private final NSFDatabase database;
	
	public DarwinoNDatabase(DarwinoNotesAPI notesApi, NSFDatabase database) {
		this.notesApi = notesApi;
		this.database = database;
	}
	
	public NSFDatabase getNSFDatabase() {
		return database;
	}
	
	@Override
	public NotesAPI getAPI() {
		return this.notesApi;
	}
	
	@Override
	public NNote getNoteByID(int noteId) {
		try {
			NSFNote note = database.getNoteByID(noteId);
			return note == null ? null : new DarwinoNNote(note);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public String getTitle() {
		try {
			return database.getTitle();
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void setTitle(String title) {
		try {
			database.setTitle(title);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public String getFilePath() {
		try {
			return database.getFilePath();
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public int getSharedFieldNoteID(String fieldName) {
		try {
			return database.getDesign().findDesignNote(DominoAPI.NOTE_CLASS_FIELD, DominoAPI.DFLAGPAT_FIELD, fieldName, false);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void eachDesignNote(BiConsumer<Integer, NNote> consumer) {
		NSFSEARCHPROC proc = new NSFSEARCHPROC() {
			@Override public short callback(long searchMatchPtr, long summaryBufferPtr) throws DominoException {
				SEARCH_MATCH searchMatch = new SEARCH_MATCH();
				C.memcpy(searchMatch.getDataPtr(), 0, searchMatchPtr, 0, SEARCH_MATCH.sizeOf);

				short noteClass = searchMatch.getNoteClass();
				int noteId = searchMatch.getId().getNoteId();
				byte retFlags = searchMatch.getSERetFlags();
				
				boolean deleted = (noteClass & DominoAPI.NOTE_CLASS_NOTIFYDELETION) != 0;
				boolean isSearchMatch = (retFlags & DominoAPI.SE_FMATCH) != 0;  // The use of "since" means that non-matching notes will be returned; check this flag to make sure
				
				if(isSearchMatch && !deleted) {
					try(NNote note = getNoteByID(noteId)) {
						consumer.accept(noteId, note);
					}
				}
				return DominoAPI.NOERROR;
			}
		};
		try {
			database.search("@All", proc, (short)0, DominoAPI.NOTE_CLASS_ALLNONDATA, null, null); //$NON-NLS-1$
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		} catch (FormulaException e) {
			throw new NDominoException(0, e);
		}
	}
	
	@Override
	public short getCurrentAccessLevel() {
		return database.getCurrentAccessLevel().getValue();
	}

	@Override
	public void close() {
		database.free();
	}

}
