package org.openntf.nsfodp.commons.odp.notesapi;

/**
 * Represents an entry from a view or folder index.
 * 
 * @author Jesse Gallagher
 * @since 4.0.0
 */
public interface NViewEntry extends AutoCloseable {
	/**
	 * Retrieves an array of column values, converted to a Java-standard
	 * format when possible.
	 * 
	 * @return an array of column values
	 */
	Object[] getColumnValues();
	
	/**
	 * Retrieves the note ID of the view entry.
	 * 
	 * @return the note ID as an {@code int}
	 */
	int getNoteID();
	
	/**
	 * Retrieves the note class of the view entry.
	 * 
	 * @return the note class as an {@code short}
	 */
	short getNoteClass();
}
