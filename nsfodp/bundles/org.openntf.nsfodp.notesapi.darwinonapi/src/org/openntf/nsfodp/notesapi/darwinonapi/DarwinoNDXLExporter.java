package org.openntf.nsfodp.notesapi.darwinonapi;

import java.io.OutputStream;
import java.util.Collection;

import org.openntf.nsfodp.commons.odp.notesapi.NDXLExporter;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NDominoException;
import org.openntf.nsfodp.commons.odp.notesapi.NNote;

import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.enums.DXL_RICHTEXT_OPTION;
import com.darwino.domino.napi.wrap.NSFDXLExporter;
import com.darwino.domino.napi.wrap.NSFDatabase;
import com.darwino.domino.napi.wrap.NSFNote;
import com.darwino.domino.napi.wrap.NSFNoteIDCollection;

public class DarwinoNDXLExporter implements NDXLExporter {
	private final NSFDXLExporter exporter;
	
	public DarwinoNDXLExporter(NSFDXLExporter exporter) {
		this.exporter = exporter;
	}
	
	@Override
	public void setForceNoteFormat(boolean forceNoteFormat) {
		try {
			this.exporter.setForceNoteFormat(forceNoteFormat);
		} catch(DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public boolean isForceNoteFormat() {
		try {
			return this.exporter.isForceNoteFormat();
		} catch(DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void setRichTextAsItemData(boolean richTextAsItemData) {
		try {
			this.exporter.setRichTextOption(richTextAsItemData ? DXL_RICHTEXT_OPTION.ItemData : DXL_RICHTEXT_OPTION.DXL);
		} catch(DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void setOmitItemNames(Collection<String> itemNames) {
		try {
			this.exporter.setOmitItemNames(itemNames);
		} catch(DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void setProperty(int property, boolean value) {
		try {
			this.exporter.setProperty(property, value);
		} catch(DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void export(NDatabase database, Collection<Integer> noteIds, OutputStream os) {
		try {
			NSFDatabase db = ((DarwinoNDatabase)database).getNSFDatabase();
			NSFNoteIDCollection coll = db.getParent().createNoteIDCollection();
			try {
				coll.addAll(noteIds);
				this.exporter.export(db, coll, os);
			} finally {
				coll.free();
			}
		} catch(DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void export(NNote note, OutputStream os) {
		try {
			NSFNote n = ((DarwinoNNote)note).getNSFNote();
			this.exporter.export(n, os);
		} catch(DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}

	@Override
	public void close() {
		this.exporter.free();
	}

}
