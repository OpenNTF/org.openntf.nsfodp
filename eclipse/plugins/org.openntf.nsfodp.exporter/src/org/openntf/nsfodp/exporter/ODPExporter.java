package org.openntf.nsfodp.exporter;

import static org.openntf.nsfodp.commons.h.StdNames.*;
import static com.ibm.designer.domino.napi.NotesConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesDatetime;
import com.ibm.designer.domino.napi.NotesFormula;
import com.ibm.designer.domino.napi.NotesIDTable;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.design.FileAccess;
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
	public static final String EXT_METADATA = ".metadata"; //$NON-NLS-1$
	
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

			Path databaseProperties = result.resolve("AppProperties").resolve("database.properties"); //$NON-NLS-1$ //$NON-NLS-2$
			Files.createDirectories(databaseProperties.getParent());
			try(OutputStream os = Files.newOutputStream(databaseProperties)) {
				exporter.exportDbProperties(os, database);
			}
			
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
		case IconNote:
		case DBScript:
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
		case Applet:
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
		case WiringProperties:
		case CompositeApplication:
		case CompositeComponent:
			exportNamedDataAndMetadata(note, exporter, baseDir, type);
			break;
		case WebContentFile:
		case GenericFile:
			exportNamedData(note, baseDir, type);
			break;
		case DesignCollection:
		case ACL:
			// Nothing to do here
			break;
		case Unknown:
		default:
			String flags = note.isItemPresent(DESIGN_FLAGS) ? note.getItemValueAsString(DESIGN_FLAGS) : StringUtil.EMPTY_STRING;
			String title = note.isItemPresent(FIELD_TITLE) ? note.getItemValueAsString(FIELD_TITLE) : String.valueOf(note.getNoteId());
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
		String name = cleanName(note.getItemAsTextList(FIELD_TITLE).get(0));
		if(StringUtil.isNotEmpty(type.extension) && !name.endsWith(type.extension)) {
			name += '.' + type.extension;
		}
		
		exportExplicitNote(note, exporter, baseDir, type.path.resolve(name));
	}
	
	/**
	 * Converted a VFS-style file name to an FS-friendly version.
	 * 
	 * @param title the $TITLE value
	 * @return an FS-friendly version of the title
	 */
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
	 * Exports the file data of the provided note, without the metadata file.
	 * 
	 * @param note the note to export
	 * @param baseDir the base directory for export operations
	 * @param type the NoteType enum for the note
	 * @throws NotesAPIException
	 * @throws IOException
	 */
	private void exportNamedData(NotesNote note, Path baseDir, NoteType type) throws NotesAPIException, IOException {
		String name = cleanName(note.getItemAsTextList(FIELD_TITLE).get(0));
		if(StringUtil.isNotEmpty(type.extension) && !name.endsWith(type.extension)) {
			name += '.' + type.extension;
		}
		
		// These are normal files in the NSF, but should not be exported
		if(name.startsWith("WebContent/WEB-INF/classes") || name.startsWith("WEB-INF/classes")) { //$NON-NLS-1$ //$NON-NLS-2$
			return;
		} else if(name.equals("build.properties")) { //$NON-NLS-1$
			return;
		}
		
		exportFileData(note, baseDir, type.path.resolve(name), type);
	}
	
	/**
	 * Exports the file data of the provided note, plus a neighboring ".metadata" file.
	 * 
	 * @param note the note to export
	 * @param exporter the exporter to use for the process
	 * @param baseDir the base directory for export operations
	 * @param type the NoteType enum for the note
	 * @throws NotesAPIException
	 * @throws IOException
	 */
	private void exportNamedDataAndMetadata(NotesNote note, DXLExporter exporter, Path baseDir, NoteType type) throws NotesAPIException, IOException {
		exportNamedData(note, baseDir, type);
		
		String name = cleanName(note.getItemAsTextList(FIELD_TITLE).get(0));
		if(StringUtil.isNotEmpty(type.extension) && !name.endsWith(type.extension)) {
			name += '.' + type.extension;
		}
		
		List<String> ignoreItems = new ArrayList<>(Arrays.asList(type.fileItem, ITEM_NAME_FILE_SIZE, XSP_CLASS_INDEX));
		// Some of these will have pattern-based item ignores
		if(StringUtil.isNotEmpty(type.itemNameIgnorePattern)) {
			for(String itemName : note.getItemNames()) {
				if(Pattern.matches(type.itemNameIgnorePattern, itemName)) {
					ignoreItems.add(itemName);
				}
			}
		}
		
		exporter.setExporterListProperty(DXLExporter.eOmitItemNames, ignoreItems.toArray(new String[ignoreItems.size()]));
		exporter.setExporterProperty(38, 1);
		try {
			exportExplicitNote(note, exporter, baseDir, type.path.resolve(name + EXT_METADATA));
		} finally {
			exporter.setExporterListProperty(DXLExporter.eOmitItemNames, StringUtil.EMPTY_STRING_ARRAY);
			exporter.setExporterProperty(38, 0);
		}
	}
	
	/**
	 * Exports the file data of the provided note to the specified path.
	 * 
	 * @param note the note to export
	 * @param baseDir the base directory for export operations
	 * @param path the relative file path to export to within the base dir
	 * @param type the NoteType enum for the note
	 * @throws NotesAPIException
	 * @throws IOException
	 */
	private void exportFileData(NotesNote note, Path baseDir, Path path, NoteType type) throws NotesAPIException, IOException {
		Path fullPath = baseDir.resolve(path);
		Files.createDirectories(fullPath.getParent());
		
		try(OutputStream os = Files.newOutputStream(fullPath)) {
			// readFileContent works for some but not all file types
			switch(type) {
			case LotusScriptLibrary:
			case JavaScriptLibrary:
			case ServerJavaScriptLibrary:
				try {
					NReadScriptContent.invoke(null, note.getHandle(), type.fileItem, os);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
					throw new NotesAPIException(e, "Exception when reading script content"); //$NON-NLS-1$
				}
				break;
			default:
				FileAccess.readFileContent(note, os);
				break;
			}
		}
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
