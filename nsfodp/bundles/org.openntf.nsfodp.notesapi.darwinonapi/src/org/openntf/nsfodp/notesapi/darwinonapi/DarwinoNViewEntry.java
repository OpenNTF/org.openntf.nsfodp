package org.openntf.nsfodp.notesapi.darwinonapi;

import org.openntf.nsfodp.commons.odp.notesapi.NViewEntry;

import com.darwino.domino.napi.wrap.NSFViewEntry;

public class DarwinoNViewEntry implements NViewEntry {
	private final NSFViewEntry entry;
	
	public DarwinoNViewEntry(NSFViewEntry entry) {
		this.entry = entry;
	}
	
	@Override
	public Object[] getColumnValues() {
		return entry.getColumnValues();
	}
	
	@Override
	public int getNoteID() {
		return entry.getNoteId();
	}
	
	@Override
	public short getNoteClass() {
		return entry.getNoteClass();
	}

	@Override
	public void close() throws Exception {
		// NOP
	}

}
