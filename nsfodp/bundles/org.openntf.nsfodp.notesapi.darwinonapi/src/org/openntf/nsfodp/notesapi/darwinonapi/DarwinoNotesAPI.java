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
package org.openntf.nsfodp.notesapi.darwinonapi;

import java.util.concurrent.ThreadFactory;

import org.openntf.nsfodp.commons.odp.notesapi.NDXLExporter;
import org.openntf.nsfodp.commons.odp.notesapi.NDXLImporter;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NDominoException;
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;

import com.darwino.domino.napi.DominoAPI;
import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.enums.DBClass;
import com.darwino.domino.napi.enums.DXLIMPORTOPTION;
import com.darwino.domino.napi.enums.DXL_EXPORT_CHARSET;
import com.darwino.domino.napi.wrap.NSFDXLExporter;
import com.darwino.domino.napi.wrap.NSFDXLImporter;
import com.darwino.domino.napi.wrap.NSFDatabase;
import com.darwino.domino.napi.wrap.NSFSession;

public class DarwinoNotesAPI implements NotesAPI {
	private final NSFSession session;
	
	public DarwinoNotesAPI() {
		this.session = new NSFSession(DominoAPI.get());
	}
	
	public DarwinoNotesAPI(String effectiveUserName, boolean internetSession, boolean fullAccess) {
		try {
			this.session = new NSFSession(DominoAPI.get(), effectiveUserName, internetSession, fullAccess);
		} catch (DominoException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void NotesInitExtended(String... argv) {
		try {
			DominoAPI.get().NotesInitExtended(argv);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public NDatabase createDatabase(String filePath) {
		try {
			return new DarwinoNDatabase(this, session.createDatabase("", filePath, DBClass.BY_EXTENSION, true)); //$NON-NLS-1$
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public NDatabase openDatabase(String apiPath) {
		try {
			NSFDatabase db = session.getDatabase(apiPath);
			return db == null ? null : new DarwinoNDatabase(this, db);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public NDatabase openDatabase(String server, String filePath) {
		try {
			NSFDatabase db = session.getDatabase(server, filePath);
			return db == null ? null : new DarwinoNDatabase(this, db);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public NDXLImporter createDXLImporter() {
		try {
			NSFDXLImporter importer = session.createDXLImporter();
			importer.setDesignImportOption(DXLIMPORTOPTION.CREATE);
			importer.setACLImportOption(DXLIMPORTOPTION.REPLACE_ELSE_IGNORE);
			importer.setReplaceDBProperties(true);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			return new DarwinoNDXLImporter(importer);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public NDXLExporter createDXLExporter() {
		try {
			NSFDXLExporter exporter = session.createDXLExporter();
			exporter.setExportCharset(DXL_EXPORT_CHARSET.Utf8);
			exporter.setOutputDoctype(false);
			return new DarwinoNDXLExporter(exporter);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public ThreadFactory createThreadFactory() {
		return new DarwinoNAPIThreadFactory();
	}
	
	@Override
	public void deleteDatabase(String filePath) {
		try {
			DominoAPI.get().NSFDbDelete(filePath);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void close() {
		session.free();
	}
}
