package org.openntf.nsfodp.exporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openntf.nsfodp.commons.h.NsfDb;
import org.openntf.nsfodp.commons.h.StdNames;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesCollection;
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
				
				database.search(designCollection, 32766, (NotesFormula)null, (String)null, 0, (NotesDatetime)null);
				
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
		
//		DxlExporter exporter = database.getParent().createDxlExporter();
//		try {
//			exporter.setForceNoteFormat(isBinaryDxl());
//			
//			NoteCollection notes = database.createNoteCollection(false);
//			try {
//				notes.selectAllDesignElements(true);
//				notes.buildCollection();
//				
//				String id = notes.getFirstNoteID();
//				while(StringUtil.isNotEmpty(id)) {
//					Document doc = database.getDocumentByID(id);
//					try {
//						exportNote(doc, exporter, result);
//					} finally {
//						doc.recycle();
//					}
//					
//					id = notes.getNextNoteID(id);
//				}
//			} finally {
//				notes.recycle();
//			}
//		} finally {
//			exporter.recycle();
//		}
		
		return result;
	}

	private void exportNote(NotesNote note, DXLExporter exporter, Path baseDir) throws IOException, NotesAPIException {
		switch(NoteType.forNote(note)) {
		case AboutDocument:
			exportExplicitNote(note, exporter, baseDir.resolve("Resources").resolve("AboutDocument")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case CustomControl:
			// Contents + metadata
			break;
		case DBScript:
			// Special handling
			break;
		case FileResource:
			// Contents + metadata
			break;
		case Form:
			exportNamedNote(note, exporter, baseDir.resolve("Forms"), "form"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case Frameset:
			exportNamedNote(note, exporter, baseDir.resolve("Framesets"), "frameset"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case GenericFile:
			// These float freely with no metadata
			break;
		case Icon:
			// VERY special behavior, since this exists in several places
			break;
		case ImageResource:
			// Contents + metadata
			break;
		case Java:
			// Contents + metadata
			break;
		case JavaLibrary:
			exportNamedNote(note, exporter, baseDir.resolve("Code").resolve("ScriptLibraries"), "javalib"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case JavaScriptLibrary:
			// Contents + metadata
			break;
		case LotusScriptLibrary:
			// Contents + metadata
			break;
		case Outline:
			exportNamedNote(note, exporter, baseDir.resolve("Outlines"), "outline"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case Page:
			exportNamedNote(note, exporter, baseDir.resolve("Pages"), "page"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SharedFields:
			exportNamedNote(note, exporter, baseDir.resolve("SharedElements").resolve("Fields"), "field"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case StyleSheet:
			// Contents + metadata
			break;
		case Theme:
			// Contents + metadata
			break;
		case UsingDocument:
			exportExplicitNote(note, exporter, baseDir.resolve("Resources").resolve("UsingDocument")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case View:
			exportNamedNote(note, exporter, baseDir.resolve("Views"), "view"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case XPage:
			// Contents + metadata
			break;
		case XSPDesign:
			// Special handling: contents only at AppProperties/xspdesign.properties
			break;
		case ServerJavaScriptLibrary:
			// Contents + metadata, I think
			break;
		case ImportedJavaAgent:
			exportNamedNote(note, exporter, baseDir.resolve("Code").resolve("ScriptLibraries"), "ija"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case JavaAgent:
			exportNamedNote(note, exporter, baseDir.resolve("Code").resolve("Agents"), "ja"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case Folder:
			exportNamedNote(note, exporter, baseDir.resolve("Folders"), "folder"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case Jar:
			// Contents + metadata
			break;
		case JavaWebService:
			exportNamedNote(note, exporter, baseDir.resolve("Code").resolve("WebServices"), "jws"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case LotusScriptAgent:
			exportNamedNote(note, exporter, baseDir.resolve("Code").resolve("ScriptLibraries"), "lss"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case LotusScriptWebService:
			exportNamedNote(note, exporter, baseDir.resolve("Code").resolve("WebServices"), "lws"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case SharedActions:
			// Special handling to break this note up
			break;
		case SharedColumn:
			// TODO verify this
			exportNamedNote(note, exporter, baseDir.resolve("SharedElements").resolve("Columns"), "column"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case Subform:
			exportNamedNote(note, exporter, baseDir.resolve("Subforms"), "subform"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SimpleActionAgent:
			exportNamedNote(note, exporter, baseDir.resolve("Code").resolve("Agents"), "aa"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case FormulaAgent:
			exportNamedNote(note, exporter, baseDir.resolve("Code").resolve("Agents"), "fa"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case Unknown:
		default:
			System.out.println("Unknown note, flags=" + note.getItemValueAsString("$Flags") + ", title=" + note.getItemValueAsString("$TITLE"));
			//throw new UnsupportedOperationException("Unhandled note: " + doc.getUniversalID() + ", flags " + doc.getItemValueString("$Flags")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
	}
	
	/**
	 * Exports an individually-named note, based on its $TITLE value.
	 * 
	 * @param note the note to export
	 * @param exporter the exporter to use for the process
	 * @param baseDir the directory to export to
	 * @param extension the file extension to add to the file
	 * @throws IOException 
	 * @throws NotesAPIException 
	 */
	private void exportNamedNote(NotesNote note, DXLExporter exporter, Path baseDir, String extension) throws IOException, NotesAPIException {
		String name = cleanName(note.getItemValueAsString(StdNames.FIELD_TITLE));
		if(StringUtil.isNotEmpty(extension)) {
			name += '.' + extension;
		}
		exportExplicitNote(note, exporter, baseDir.resolve(name));
	}
	
	private String cleanName(String title) {
		int pipe = title.indexOf('|');
		String clean = pipe > -1 ? title.substring(0, pipe) : title;
		
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
	 * @param path the file path to export to
	 * @throws IOException 
	 * @throws NotesAPIException 
	 */
	private void exportExplicitNote(NotesNote note, DXLExporter exporter, Path path) throws IOException, NotesAPIException {
		Files.createDirectories(path.getParent());
		
		try(OutputStream os = Files.newOutputStream(path)) {
			exporter.exportNote(os, note);
		}
	}
}
