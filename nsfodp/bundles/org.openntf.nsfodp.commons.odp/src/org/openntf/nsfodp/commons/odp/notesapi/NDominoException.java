package org.openntf.nsfodp.commons.odp.notesapi;

public class NDominoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private final int status;

	public NDominoException(int status, Exception cause) {
		super(cause);
		
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
}
