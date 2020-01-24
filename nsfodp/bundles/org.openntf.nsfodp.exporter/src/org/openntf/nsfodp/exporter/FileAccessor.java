package org.openntf.nsfodp.exporter;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.wrap.NSFNote;
import com.ibm.commons.util.io.ByteStreamCache;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.designer.domino.napi.design.FileAccess;

public enum FileAccessor {
	;
	
	// Get handles on some FileAccess methods, since the public ones use the wrong item name
	private static Method NReadScriptContent;
	static {
		try {
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
				NReadScriptContent = FileAccess.class.getDeclaredMethod("NReadScriptContent", int.class, String.class, OutputStream.class); //$NON-NLS-1$
				NReadScriptContent.setAccessible(true);
				return null;
			});
		} catch (PrivilegedActionException e) {
			e.printStackTrace();
		}
	}
	
	public static InputStream readFileContentAsInputStream(NSFNote note) {
		ByteStreamCache bytes = new ByteStreamCache();
		readFileContent(note, bytes.getOutputStream());
		return bytes.getInputStream();
	}
	
	public static InputStream readFileContentAsInputStream(NSFNote note, String itemName) {
		try {
			NotesNote notesNote = getNote(note);
			try {
				return FileAccess.readFileContentAsInputStream(notesNote, itemName);
			} finally {
				notesNote.recycle();
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void readFileContent(NSFNote note, OutputStream os) {
		try {
			NotesNote notesNote = getNote(note);
			try {
				FileAccess.readFileContent(notesNote, os);
			} finally {
				notesNote.recycle();
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void readScriptContent(NSFNote note, String itemName, OutputStream os) throws DominoException {
		try {
			NotesNote notesNote = getNote(note);
			try {
				NReadScriptContent.invoke(null, notesNote.getHandle(), itemName, os);
			} finally {
				notesNote.recycle();
			}
		} catch (Exception e) {
			throw new DominoException(e, "Exception when reading script content"); //$NON-NLS-1$
		}
	}
	
	private static NotesNote getNote(NSFNote note) throws NotesAPIException, DominoException {
		NotesSession notesSession = new NotesSession();
		NotesDatabase notesDatabase = notesSession.getDatabase((int)note.getParent().getHandle());
		return notesDatabase.openNote(note.getNoteID(), 0);
	}
}
