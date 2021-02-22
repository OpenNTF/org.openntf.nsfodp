package org.openntf.nsfodp.commons.odp.notesapi;

/**
 * This interface represents an object capable of producing {@link NotesAPI} objects.
 * 
 * <p>Clients should find an implementation {@link ServiceLoader}.</p>
 * 
 * @author Jesse Gallagher
 * @since 3.5.0
 */
public interface NotesAPIFactory {
	NotesAPI createAPI();
	NotesAPI createAPI(String effectiveUserName, boolean internetSession, boolean fullAccess);
}
