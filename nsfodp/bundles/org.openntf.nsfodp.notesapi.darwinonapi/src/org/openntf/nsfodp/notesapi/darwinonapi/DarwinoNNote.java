package org.openntf.nsfodp.notesapi.darwinonapi;

import org.openntf.nsfodp.commons.odp.notesapi.NCompositeData;
import org.openntf.nsfodp.commons.odp.notesapi.NDominoException;
import org.openntf.nsfodp.commons.odp.notesapi.NLotusScriptCompilationException;
import org.openntf.nsfodp.commons.odp.notesapi.NNote;

import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.LotusScriptCompilationException;
import com.darwino.domino.napi.wrap.NSFNote;
import com.darwino.domino.napi.wrap.item.NSFCompositeData;

public class DarwinoNNote implements NNote {
	private final NSFNote note;
	
	public DarwinoNNote(NSFNote note) {
		this.note = note;
	}
	
	public NSFNote getNSFNote() {
		return note;
	}
	
	@Override
	public boolean hasItem(String itemName) {
		try {
			return this.note.hasItem(itemName);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}

	@Override
	public void save() {
		try {
			this.note.save();
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}

	@Override
	public void sign() {
		try {
			this.note.sign();
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void compileLotusScript() throws NLotusScriptCompilationException {
		try {
			this.note.compileLotusScript();
		} catch(LotusScriptCompilationException e) {
			throw new NLotusScriptCompilationException(e.getStatus(), e);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public <T> T get(String itemName, Class<T> valueClass) {
		try {
			return this.note.get(itemName, valueClass);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public String getAsString(String itemName, char delimiter) {
		try {
			return this.note.getAsString(itemName, delimiter);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void set(String itemName, Object value) {
		try {
			this.note.set(itemName, value);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public short getNoteClassValue() {
		return this.note.getNoteClassValue();
	}
	
	@Override
	public int getNoteID() {
		try {
			return this.note.getNoteID();
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public NCompositeData getCompositeData(String itemName) {
		try {
			NSFCompositeData data = this.note.getCompositeData(itemName);
			return data == null ? null : new DarwinoNCompositeData(this.note, data);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public Iterable<String> getItemNames() {
		try {
			return this.note.getItemNames();
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public boolean isRefValid() {
		return this.note.isRefValid();
	}

	@Override
	public void close() {
		this.note.free();
	}

}
