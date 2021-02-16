package org.openntf.nsfodp.commons.odp.notesapi;

public class NLotusScriptCompilationException extends NDominoException {
	private static final long serialVersionUID = 1L;

	public NLotusScriptCompilationException(int status, Exception cause) {
		super(status, cause);
	}
	
	@Override
	public String toString() {
		return getCause().toString();
	}
}
