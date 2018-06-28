package org.openntf.nsfodp.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openntf.nsfodp.commons.h.StdNames;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesDatetime;
import com.ibm.designer.domino.napi.NotesFormula;
import com.ibm.designer.domino.napi.NotesIDTable;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.dxl.DXLExporter;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.IdTable;
import com.ibm.domino.napi.c.NsfNote;
import com.ibm.domino.napi.c.callback.IDENUMERATEPROC;

/**
 * Represents an on-disk project export environment.
 * 
 * <p>This class is the primary entry point for ODP exporting.</p>
 * 
 * @author Jesse Gallagher
 * @since 1.4.0
 */
public class ODPExporter {
	private final NotesDatabase database;
	private boolean binaryDxl = false;

	public ODPExporter(NotesDatabase database) {
		this.database = database;
	}
	
	/**
	 * Sets whether to use "binary" DXL for exporting operations.
	 * 
	 * @param binaryDxl the value to set
	 */
	public void setBinaryDxl(boolean binaryDxl) {
		this.binaryDxl = binaryDxl;
	}
	
	/**
	 * Whether the exporter is configured to use "binary" DXL.
	 * 
	 * @return the current binary DXL setting
	 */
	public boolean isBinaryDxl() {
		return binaryDxl;
	}
	
	public Path export() throws IOException, NotesAPIException, NException {
		Path result = Files.createTempDirectory(getClass().getName());
		
		DXLExporter exporter = new DXLExporter(database);
		try {
			exporter.open();
			
			exporter.setExporterProperty(DXLExporter.eForceNoteFormat, isBinaryDxl() ? 1 : 0);
			
			NotesIDTable designCollection = new NotesIDTable(database);
			try {
				designCollection.create();
				
				database.search(designCollection, NsfNote.NOTE_CLASS_ALLNONDATA, (NotesFormula)null, (String)null, 0, (NotesDatetime)null);
				
				IdTable.IDEnumerate(designCollection.getHandle(), new IDENUMERATEPROC() {
					@Override
					public short callback(int noteId) {
						try {
							NotesNote note = database.openNote(noteId, NsfNote.OPEN_RAW_MIME);
							exportNote(note, exporter, result);
						} catch (NotesAPIException | IOException e) {
							throw new RuntimeException(e);
						}
						
						return 0;
					}
				});
			} finally {
				designCollection.recycle();
			}
		} finally {
			exporter.recycle();
		}
		
		return result;
	}

	private void exportNote(NotesNote note, DXLExporter exporter, Path baseDir) throws IOException, NotesAPIException {
		NoteType type = NoteType.forNote(note);
		switch(type) {
		case AboutDocument:
		case UsingDocument:
		case SharedActions:
		case DBIcon:
			exportExplicitNote(note, exporter, baseDir, type.path);
			break;
		case Form:
		case Frameset:
		case JavaLibrary:
		case Outline:
		case Page:
		case SharedField:
		case View:
		case ImportedJavaAgent:
		case JavaAgent:
		case JavaWebService:
		case Folder:
		case LotusScriptAgent:
		case LotusScriptWebService:
		case JavaWebServiceConsumer:
		case LotusScriptWebServiceConsumer:
		case SharedColumn:
		case Subform:
		case SimpleActionAgent:
		case FormulaAgent:
		case Navigator:
		case DB2AccessView:
		case DataConnection:
			exportNamedNote(note, exporter, baseDir, type);
			break;
		case CustomControl:
		case FileResource:
		case ImageResource:
		case JavaScriptLibrary:
		case Java:
		case LotusScriptLibrary:
		case StyleSheet:
		case Theme:
		case XPage:
		case ServerJavaScriptLibrary:
		case Jar:
			// Contents + metadata
			break;
		case WebContentFile:
		case GenericFile:
			// These float freely with no metadata
		case DBScript:
			// Special handling
			break;
		case IconNote:
			// VERY special behavior, since this exists in several places
			break;
		case DesignCollection:
		case ACL:
			// Nothing to do here
			break;
		case Unknown:
		default:
			String flags = note.isItemPresent(StdNames.DESIGN_FLAGS) ? note.getItemValueAsString(StdNames.DESIGN_FLAGS) : StringUtil.EMPTY_STRING;
			String title = note.isItemPresent(StdNames.FIELD_TITLE) ? note.getItemValueAsString(StdNames.FIELD_TITLE) : String.valueOf(note.getNoteId());
			System.out.println("Unknown note, flags=" + flags + ", title=" + title + ", class=" + (note.getNoteClass() & ~NsfNote.NOTE_CLASS_DEFAULT));
			//throw new UnsupportedOperationException("Unhandled note: " + doc.getUniversalID() + ", flags " + doc.getItemValueString("$Flags")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
	}
	
	/**
	 * Exports an individually-named note, based on its $TITLE value.
	 * 
	 * @param note the note to export
	 * @param exporter the exporter to use for the process
	 * @param baseDir the base directory for export operations
	 * @param type the NoteType enum for the note
	 * @throws IOException 
	 * @throws NotesAPIException 
	 */
	private void exportNamedNote(NotesNote note, DXLExporter exporter, Path baseDir, NoteType type) throws IOException, NotesAPIException {
		String name = cleanName(note.getItemValueAsString(StdNames.FIELD_TITLE));
		if(StringUtil.isNotEmpty(type.extension) && !name.endsWith(type.extension)) {
			name += '.' + type.extension;
		}
		exportExplicitNote(note, exporter, baseDir, type.path.resolve(name));
	}
	
	private String cleanName(String title) {
		int pipe = title.indexOf('|');
		String clean = pipe > -1 ? title.substring(0, pipe) : title;
		clean = clean.isEmpty() ? "(Untitled)" : clean; //$NON-NLS-1$
		
		// TODO replace with a proper algorithm 
		return clean
			.replace("\\", "_5c") //$NON-NLS-1$ //$NON-NLS-2$
			.replace("*", "_2a"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Exports a note to the provided path.
	 * 
	 * @param note the note to export
	 * @param exporter the exporter to use for the process
	 * @param baseDir the base directory for export operations
	 * @param path the relative file path to export to within the base dir
	 * @throws IOException 
	 * @throws NotesAPIException 
	 */
	private void exportExplicitNote(NotesNote note, DXLExporter exporter, Path baseDir, Path path) throws IOException, NotesAPIException {
		Path fullPath = baseDir.resolve(path);
		Files.createDirectories(fullPath.getParent());
		
		try(OutputStream os = Files.newOutputStream(fullPath)) {
			exporter.exportNote(os, note);
		}
	}
}
