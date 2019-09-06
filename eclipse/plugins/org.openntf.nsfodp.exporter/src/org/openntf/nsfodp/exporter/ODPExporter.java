/**
 * Copyright © 2018-2019 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.nsfodp.exporter;

import static org.openntf.nsfodp.commons.h.StdNames.*;
import static org.openntf.nsfodp.commons.h.StdNames.FIELD_TITLE;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGS;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_FILE_NAMES;
import static com.ibm.designer.domino.napi.NotesConstants.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import org.openntf.nsfodp.commons.NoteType;
import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.io.SwiperOutputStream;
import org.openntf.nsfodp.commons.odp.OnDiskProject;
import org.openntf.nsfodp.commons.odp.util.NoteTypeUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.Format;
import com.ibm.commons.xml.XMLException;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesCollection;
import com.ibm.designer.domino.napi.NotesCollectionEntry;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.designer.domino.napi.dxl.DXLExporter;
import com.ibm.designer.domino.napi.util.NotesIterator;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.NsfNote;

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
	private static final Collection<NoteType> IGNORE_FILENAMES_TYPES = EnumSet.of(NoteType.FileResource, NoteType.StyleSheet, NoteType.ImageResource, NoteType.Theme);
	
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
	private boolean richTextAsItemData = false;
	private boolean swiperFilter = false;

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
	
	/**
	 * Sets whether the exporter is configured to export rich text items as Base64'd
	 * binary data.
	 * 
	 * @param richTextAsItemData the value to set
	 * @since 2.0.0
	 */
	public void setRichTextAsItemData(boolean richTextAsItemData) {
		this.richTextAsItemData = richTextAsItemData;
	}
	
	/**
	 * Whether the exporter is configured to export rich text items as Base64'd
	 * binary data.
	 * 
	 * @return the current rich text setting
	 * @since 2.0.0
	 */
	public boolean isRichTextAsItemData() {
		return richTextAsItemData;
	}
	
	/**
	 * Sets whether to filter exported DXL using the XSLT files from Swiper.
	 * 
	 * @param swiperFilter the value to set
	 * @throws IOException if there is a problem initializing Swiper
	 */
	public void setSwiperFilter(boolean swiperFilter) throws IOException {
		this.swiperFilter = swiperFilter;
	}
	
	/**
	 * Whether the export is configured to filter exported DXL using the
	 * XSLT files from Swiper.
	 * 
	 * @return the current Swiper filter setting
	 */
	public boolean isSwiperFilter() {
		return swiperFilter;
	}
	
	@SuppressWarnings("unchecked")
	public Path export() throws IOException, NotesAPIException, NException, XMLException {
		Path result = Files.createTempDirectory(getClass().getName());
		
		DXLExporter exporter = new DXLExporter(database);
		try {
			exporter.open();

			Path databaseProperties = result.resolve("AppProperties").resolve("database.properties"); //$NON-NLS-1$ //$NON-NLS-2$
			Files.createDirectories(databaseProperties.getParent());
			try(OutputStream os = new SwiperOutputStream(databaseProperties, isSwiperFilter())) {
				exporter.exportDbProperties(os, database);
			}
			
			exporter.setExporterProperty(DXLExporter.eForceNoteFormat, isBinaryDxl() ? 1 : 0);
			exporter.setExporterProperty(DXLExporter.eDxlRichtextOption, isRichTextAsItemData() ? 1 : 0);
			
			NotesCollection designView = database.designOpenCollection(false, 0);
			try {
				int readMask = READ_MASK_NOTEID | READ_MASK_NOTECLASS;
				NotesIterator iter = designView.readEntries(readMask, 0, Integer.MAX_VALUE);
				iter.forEachRemaining(entryObj -> {
					NotesCollectionEntry entry = (NotesCollectionEntry)entryObj;
					int noteId = 0;
					try {
						noteId = entry.getNoteID();
						NotesNote note = database.openNote(noteId, NsfNote.OPEN_RAW_MIME);
						try {
							exportNote(note, exporter, result);
						} finally {
							note.recycle();
						}
					} catch(Throwable e) {
						System.out.println(StringUtil.format(Messages.ODPExporter_nativeExceptionNoteId, Integer.toString(noteId, 16), e.getMessage()));
					}
				});
			} finally {
				designView.recycle();
			}
			
			// Export the icon note specially
			NotesNote iconNote = database.openNote(NOTE_ID_SPECIAL | NOTE_CLASS_ICON, NsfNote.OPEN_RAW_MIME);
			try {
				exportNote(iconNote, exporter, result);
			} catch(Throwable e) {
				System.out.println(StringUtil.format(Messages.ODPExporter_nativeExceptionIconNote, e.getMessage()));
			} finally {
				iconNote.recycle();
			}
			
			generateManifestMf(result);
			generateEclipseProjectFile(result);
			createClasspathDirectories(result);
		} finally {
			exporter.recycle();
		}
		
		return result;
	}

	private void exportNote(NotesNote note, DXLExporter exporter, Path baseDir) throws IOException, NotesAPIException, NException, XMLException {
		NoteType type = NoteTypeUtil.forNote(note);
		if(type == NoteType.Unknown) {
			String flags = note.isItemPresent(DESIGN_FLAGS) ? note.getItemValueAsString(DESIGN_FLAGS) : StringUtil.EMPTY_STRING;
			String title = note.isItemPresent(FIELD_TITLE) ? note.getItemValueAsString(FIELD_TITLE) : Integer.toString(note.getNoteId(), 16);
			System.out.println(StringUtil.format(Messages.ODPExporter_unknownNote, flags, title, (note.getNoteClass() & ~NsfNote.NOTE_CLASS_DEFAULT)));
			return;
		}
		if(!type.isInOdp()) {
			return;
		}
		
		if(type.isSingleton()) {
			exportExplicitNote(note, exporter, baseDir, type.getPath());
		} else {
			switch(type.getOutputFormat()) {
			case METADATA:
				exportNamedDataAndMetadata(note, exporter, baseDir, type);
				break;
			case RAWFILE:
				exportNamedData(note, exporter, baseDir, type);
				break;
			case DXL:
			default:
				exportNamedNote(note, exporter, baseDir, type);
				break;
			}
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
		Path name = getCleanName(note, type);
		if(StringUtil.isNotEmpty(type.getExtension()) && !name.getFileName().toString().endsWith(type.getExtension())) {
			Path parent = name.getParent();
			if(parent == null) {
				name = Paths.get(name.getFileName().toString() + '.' + type.getExtension());
			} else {
				name = parent.resolve(name.getFileName().toString() + '.' + type.getExtension());
			}
		}
		
		exportExplicitNote(note, exporter, baseDir, type.getPath().resolve(name));
	}
	
	/**
	 * Converted a VFS-style file name to an FS-friendly version.
	 * 
	 * @param note the note to get a title for
	 * @return an FS-friendly version of the title
	 * @throws NotesAPIException 
	 */
	private Path getCleanName(NotesNote note, NoteType type) throws NotesAPIException {
		if(!note.isItemPresent(FIELD_TITLE)) {
			return Paths.get("(Untitled)"); //$NON-NLS-1$
		}
		
		String title;
		String path = note.isItemPresent(ITEM_NAME_FILE_NAMES) ? note.getItemAsTextList(ITEM_NAME_FILE_NAMES).get(0) : null;
		if(StringUtil.isNotEmpty(path) && !IGNORE_FILENAMES_TYPES.contains(type)) {
			// Then it's a "true" VFS path
			return Paths.get(note.getItemAsTextList(ITEM_NAME_FILE_NAMES).get(0).replace('/', File.separatorChar));
		} else {
			title = note.getItemAsTextList(FIELD_TITLE).get(0);
			
			int pipe = title.indexOf('|');
			String clean = pipe > -1 ? title.substring(0, pipe) : title;
			clean = clean.isEmpty() ? "(Untitled)" : clean; //$NON-NLS-1$
			
			// TODO replace with a proper algorithm 
			return Paths.get(clean
				.replace("\\", "_5c") //$NON-NLS-1$ //$NON-NLS-2$
				.replace("/", "_2f") //$NON-NLS-1$ //$NON-NLS-2$
				.replace("*", "_2a") //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
	}
	
	/**
	 * Exports the file data of the provided note, without the metadata file.
	 * 
	 * @param note the note to export
	 * @param exporter the exporter to use for the process
	 * @param baseDir the base directory for export operations
	 * @param type the NoteType enum for the note
	 * @throws NotesAPIException
	 * @throws IOException
	 * @throws NException 
	 * @throws XMLException 
	 */
	private void exportNamedData(NotesNote note, DXLExporter exporter, Path baseDir, NoteType type) throws NotesAPIException, IOException, NException, XMLException {
		Path name = getCleanName(note, type);
		if(StringUtil.isNotEmpty(type.getExtension()) && !name.getFileName().toString().endsWith(type.getExtension())) {
			Path parent = name.getParent();
			if(parent == null) {
				name = Paths.get(name.getFileName().toString() + '.' + type.getExtension());
			} else {
				name = parent.resolve(name.getFileName().toString() + '.' + type.getExtension());
			}
		}
		
		// These are normal files in the NSF, but should not be exported
		if(name.startsWith(Paths.get("WebContent", "WEB-INF", "classes")) || name.startsWith(Paths.get("WEB-INF", "classes"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return;
		} else if(name.getFileName().toString().equals("build.properties")) { //$NON-NLS-1$
			return;
		}
		
		exportFileData(note, exporter, baseDir, type.getPath().resolve(name), type);
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
	 * @throws NException 
	 * @throws XMLException 
	 */
	private void exportNamedDataAndMetadata(NotesNote note, DXLExporter exporter, Path baseDir, NoteType type) throws NotesAPIException, IOException, NException, XMLException {
		exportNamedData(note, exporter, baseDir, type);
		
		Path name = getCleanName(note, type);
		if(StringUtil.isNotEmpty(type.getExtension()) && !name.getFileName().toString().endsWith(type.getExtension())) {
			Path parent = name.getParent();
			if(parent == null) {
				name = Paths.get(name.getFileName().toString() + '.' + type.getExtension() + EXT_METADATA);
			} else {
				name = parent.resolve(name.getFileName().toString() + '.' + type.getExtension() + EXT_METADATA);
			}
		} else {
			Path parent = name.getParent();
			if(parent == null) {
				name = Paths.get(name.getFileName().toString() + EXT_METADATA);
			} else {
				name = parent.resolve(name.getFileName().toString() + EXT_METADATA);
			}
		}
		
		List<String> ignoreItems = new ArrayList<>(Arrays.asList(type.getFileItem(), ITEM_NAME_FILE_SIZE, XSP_CLASS_INDEX, SCRIPTLIB_OBJECT, ITEM_NAME_FILE_DATA, ITEM_NAME_CONFIG_FILE_DATA, ITEM_NAME_CONFIG_FILE_SIZE));
		// Some of these will have pattern-based item ignores
		Pattern pattern = type.getItemNameIgnorePattern();
		if(pattern != null) {
			for(String itemName : note.getItemNames()) {
				if(pattern.matcher(itemName).matches()) {
					ignoreItems.add(itemName);
				}
			}
		}
		
		exporter.setExporterListProperty(DXLExporter.eOmitItemNames, ignoreItems.toArray(new String[ignoreItems.size()]));
		exporter.setExporterProperty(38, 1);
		try {
			exportExplicitNote(note, exporter, baseDir, type.getPath().resolve(name));
		} finally {
			exporter.setExporterListProperty(DXLExporter.eOmitItemNames, StringUtil.EMPTY_STRING_ARRAY);
			exporter.setExporterProperty(38, 0);
		}
	}
	
	/**
	 * Exports the file data of the provided note to the specified path.
	 * 
	 * @param note the note to export
	 * @param exporter the exporter to use for the process
	 * @param baseDir the base directory for export operations
	 * @param path the relative file path to export to within the base dir
	 * @param type the NoteType enum for the note
	 * @throws NotesAPIException
	 * @throws IOException
	 * @throws NException 
	 * @throws XMLException 
	 */
	private void exportFileData(NotesNote note, DXLExporter exporter, Path baseDir, Path path, NoteType type) throws NotesAPIException, IOException, NException, XMLException {
		Path fullPath = baseDir.resolve(path);
		Files.createDirectories(fullPath.getParent());
		
		try(OutputStream os = Files.newOutputStream(fullPath)) {
			// readFileContent works for some but not all file types
			switch(type) {
			case LotusScriptLibrary:
				// This is actually just a set of text items. The NAPI wrapper, however, doesn't
				//   properly handle multiple text items of the same name, so we have to do it
				//   manually
				try(PrintWriter writer = new PrintWriter(os)) {
					// TODO replace with better NAPI implementation. Using the direct NAPI methods for
					//   item info led to UnsatisfiedLinkErrors. The legacy API also falls on its face,
					//   with iterating over the items properly finding the right count, but then returning
					//   the value for only the first each time
					byte[] dxl;
					try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
						if(!isBinaryDxl()) {
							exporter.setExporterProperty(DXLExporter.eForceNoteFormat, 1);
						}
						
						exporter.exportNote(baos, note);
						
						if(!isBinaryDxl()) {
							exporter.setExporterProperty(DXLExporter.eForceNoteFormat, 0);
						}
						
						dxl = baos.toByteArray();
					}
					
					try(InputStream in = new ByteArrayInputStream(dxl)) {
						Document doc = DOMUtil.createDocument(in);
						for(String bit : DXLUtil.getItemValueStrings(doc, type.getFileItem())) {
							writer.write(bit);
						}
					}
				}
				
				break;
			case JavaScriptLibrary:
			case ServerJavaScriptLibrary:
				try {
					NReadScriptContent.invoke(null, note.getHandle(), type.getFileItem(), os);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new NotesAPIException(e, "Exception when reading script content"); //$NON-NLS-1$
				}
				break;
			case CustomControl:
				// Special behavior: also export the config data field
				
				Path configPath = fullPath.getParent().resolve(fullPath.getFileName()+"-config"); //$NON-NLS-1$
				try(OutputStream configOut = Files.newOutputStream(configPath)) {
					try(InputStream configIn = FileAccess.readFileContentAsInputStream(note, ITEM_NAME_CONFIG_FILE_DATA)) {
						StreamUtil.copyStream(configIn, configOut);
					}
				}
				// Fallthrough intentional
			case XPage:
				FileAccess.readFileContent(note, os);
				
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
		
		try(OutputStream os = new SwiperOutputStream(fullPath, isSwiperFilter())) {
			exporter.exportNote(os, note);
		}
	}
	
	/**
	 * Generates a stub MANIFEST.MF file if the exporter did not find one in the NSF.
	 * 
	 * @param baseDir the base directory for export operations
	 * @throws IOException
	 */
	private void generateManifestMf(Path baseDir) throws IOException {
		Path manifest = baseDir.resolve("META-INF").resolve("MANIFEST.MF"); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.isRegularFile(manifest)) {
			Files.createDirectories(manifest.getParent());
			
			// Just create a blank file for now, as Designer does
			Files.createFile(manifest);
		}
	}
	
	/**
	 * Generates a stub .project file if the exporter did not find one in the NSF.
	 * 
	 * @param baseDir the base directory for export operations
	 * @throws IOException
	 * @throws XMLException 
	 */
	private void generateEclipseProjectFile(Path baseDir) throws IOException, XMLException {
		Path manifest = baseDir.resolve(".project"); //$NON-NLS-1$
		if(!Files.isRegularFile(manifest)) {
			try(OutputStream os = Files.newOutputStream(manifest, StandardOpenOption.CREATE)) {
				Document xmlDoc = DOMUtil.createDocument();
				Element projectDescription = DOMUtil.createElement(xmlDoc, "projectDescription"); //$NON-NLS-1$
				{
					Element name = DOMUtil.createElement(xmlDoc, projectDescription, "name"); //$NON-NLS-1$
					String path = database.getDatabasePath().replace('\\', '/');
					name.setTextContent(path.substring(path.lastIndexOf('/')+1).replaceAll("\\W", "_")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				DOMUtil.createElement(xmlDoc, projectDescription, "comment"); //$NON-NLS-1$
				DOMUtil.createElement(xmlDoc, projectDescription, "projects"); //$NON-NLS-1$
				DOMUtil.createElement(xmlDoc, projectDescription, "buildSpec"); //$NON-NLS-1$
				DOMUtil.createElement(xmlDoc, projectDescription, "natures"); //$NON-NLS-1$
				DOMUtil.serialize(os, xmlDoc, Format.defaultFormat);
			}
		}
	}
	
	/**
	 * Creates any directories expected by default and specified by the project classpath that
	 * don't exist in the NSF, to smooth Java compilation downstream.
	 *  
	 * @param baseDir the base directory for export operations
	 * @throws IOException if there is a problem creating directories
	 * @throws XMLException if there is a problem parsing the project configuration
	 * @throws FileNotFoundException if there is a problem creating directories
	 * @since 2.5.0
	 */
	private void createClasspathDirectories(Path baseDir) throws FileNotFoundException, XMLException, IOException {
		OnDiskProject odp = new OnDiskProject(baseDir);
		for(Path path : odp.getResourcePaths()) {
			Files.createDirectories(path);
		}
		
		// Resources/Files may not be in the programmatic list
		Files.createDirectories(baseDir.resolve("Resources").resolve("Files")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
