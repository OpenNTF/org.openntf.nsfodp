package org.openntf.nsfodp.notesapi.darwinonapi;

import java.util.concurrent.ThreadFactory;

import org.openntf.nsfodp.commons.odp.notesapi.NDXLExporter;
import org.openntf.nsfodp.commons.odp.notesapi.NDXLImporter;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NDominoException;
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;

import com.darwino.domino.napi.DominoAPI;
import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.c.C;
import com.darwino.domino.napi.enums.DBClass;
import com.darwino.domino.napi.enums.DXLIMPORTOPTION;
import com.darwino.domino.napi.enums.DXL_EXPORT_CHARSET;
import com.darwino.domino.napi.wrap.NSFDXLExporter;
import com.darwino.domino.napi.wrap.NSFDXLImporter;
import com.darwino.domino.napi.wrap.NSFDatabase;
import com.darwino.domino.napi.wrap.NSFSession;
import com.ibm.commons.util.StringUtil;

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
			importer.setDesignImportOption(DXLIMPORTOPTION.REPLACE_ELSE_CREATE);
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
	public byte[] toLMBCSString(String value) {
		if(StringUtil.isEmpty(value)) {
			return new byte[0];
		}
		long ptr = C.toLMBCSString(value);
		try {
			long len = C.strlen(ptr, 0);
			if(len > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Cannot convert value of length " + len);
			}
			byte[] result = new byte[(int)len];
			C.readByteArray(result, 0, ptr, 0, result.length);
			return result;
		} finally {
			C.free(ptr);
		}
	}
	
	@Override
	public void close() {
		session.free();
	}
}
