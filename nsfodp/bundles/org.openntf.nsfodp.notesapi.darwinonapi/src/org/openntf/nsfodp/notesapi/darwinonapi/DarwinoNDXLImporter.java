package org.openntf.nsfodp.notesapi.darwinonapi;

import java.io.InputStream;
import java.util.Collection;

import org.openntf.nsfodp.commons.odp.notesapi.NDXLImporter;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NDominoException;

import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.wrap.NSFDXLImporter;
import com.darwino.domino.napi.wrap.NSFDatabase;

public class DarwinoNDXLImporter implements NDXLImporter {
	private final NSFDXLImporter importer;
	
	public DarwinoNDXLImporter(NSFDXLImporter importer) {
		this.importer = importer;
	}
	
	@Override
	public Collection<Integer> importDxl(NDatabase database, InputStream dxl) {
		NSFDatabase db = ((DarwinoNDatabase)database).getNSFDatabase();
		try {
			return importer.importDxl(db, dxl);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public String getResultLogXML() {
		try {
			return importer.getResultLog();
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}

	@Override
	public void close() {
		this.importer.free();
	}

}
