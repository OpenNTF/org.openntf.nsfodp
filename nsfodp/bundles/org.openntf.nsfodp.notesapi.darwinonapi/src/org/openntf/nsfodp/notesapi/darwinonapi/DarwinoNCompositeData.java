package org.openntf.nsfodp.notesapi.darwinonapi;

import java.io.IOException;
import java.io.OutputStream;

import org.openntf.nsfodp.commons.odp.notesapi.NCompositeData;
import org.openntf.nsfodp.commons.odp.notesapi.NDominoException;

import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.wrap.item.NSFCompositeData;

public class DarwinoNCompositeData implements NCompositeData {
	private final NSFCompositeData data;
	
	public DarwinoNCompositeData(NSFCompositeData data) {
		this.data = data;
	}
	
	@Override
	public void writeFileResourceData(OutputStream os) throws IOException {
		try {
			this.data.writeFileResourceData(os);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void writeImageResourceData(OutputStream os) throws IOException {
		try {
			this.data.writeImageResourceData(os);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}
	
	@Override
	public void writeJavaScriptLibraryData(OutputStream os) throws IOException {
		try {
			this.data.writeJavaScriptLibraryData(os);
		} catch (DominoException e) {
			throw new NDominoException(e.getStatus(), e);
		}
	}

	@Override
	public void close() {
		this.data.free();
	}

}
