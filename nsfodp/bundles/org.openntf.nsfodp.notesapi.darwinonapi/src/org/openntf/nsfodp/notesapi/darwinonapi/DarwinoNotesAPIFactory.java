package org.openntf.nsfodp.notesapi.darwinonapi;

import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPIFactory;

public class DarwinoNotesAPIFactory implements NotesAPIFactory {

	@Override
	public NotesAPI createAPI() {
		return new DarwinoNotesAPI();
	}

	@Override
	public NotesAPI createAPI(String effectiveUserName, boolean internetSession, boolean fullAccess) {
		return new DarwinoNotesAPI(effectiveUserName, internetSession, fullAccess);
	}

}
