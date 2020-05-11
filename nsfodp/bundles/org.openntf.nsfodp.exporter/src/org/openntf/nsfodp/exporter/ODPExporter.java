/**
 * Copyright © 2018-2020 Jesse Gallagher
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

import static com.darwino.domino.napi.DominoAPI.ASSIST_TYPE_ITEM;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAGEXT_WEBCONTENTFILE;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAGEXT_WEBSERVICELIB;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAGS;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAGS_EXTENDED;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAG_DATABASESCRIPT;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAG_HIDEFROMDESIGNLIST;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAG_JAVA_AGENT;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAG_JAVA_RESOURCE;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAG_LOTUSSCRIPT_AGENT;
import static com.darwino.domino.napi.DominoAPI.DESIGN_FLAG_PROPFILE;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_COMPAPP;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_COMPDEF;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_DATA_CONNECTION_RESOURCE;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_DB2ACCESSVIEW;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_FILE;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_FOLDER_DESIGN;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_FRAMESET;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_IMAGE_RESOURCE;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_JAVAFILE;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_JAVA_WEBSERVICE;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_LS_WEBSERVICE;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_SACTIONS_DESIGN;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_SCRIPTLIB_JAVA;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_SCRIPTLIB_JS;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_SCRIPTLIB_LS;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_SCRIPTLIB_SERVER_JS;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_SHARED_COLS;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_SITEMAP;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_STYLEKIT;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_STYLE_SHEET_RESOURCE;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_SUBFORM_DESIGN;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_VIEWMAP_DESIGN;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_WEBPAGE;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_WIDGET;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_XSPCC;
import static com.darwino.domino.napi.DominoAPI.DFLAGPAT_XSPPAGE;
import static com.darwino.domino.napi.DominoAPI.FIELD_TITLE;
import static com.darwino.domino.napi.DominoAPI.ITEM_NAME_FILE_DATA;
import static com.darwino.domino.napi.DominoAPI.ITEM_NAME_FILE_NAMES;
import static com.darwino.domino.napi.DominoAPI.ITEM_NAME_FILE_SIZE;
import static com.darwino.domino.napi.DominoAPI.NOTE_CLASS_ACL;
import static com.darwino.domino.napi.DominoAPI.NOTE_CLASS_HELP;
import static com.darwino.domino.napi.DominoAPI.NOTE_CLASS_ICON;
import static com.darwino.domino.napi.DominoAPI.NOTE_CLASS_INFO;
import static com.darwino.domino.napi.DominoAPI.NOTE_ID_SPECIAL;
import static com.darwino.domino.napi.DominoAPI.SCRIPTLIB_OBJECT;
import static com.darwino.domino.napi.DominoAPI.XSP_CLASS_INDEX;
import static org.openntf.nsfodp.commons.h.StdNames.ASSIST_TYPE_JAVA;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JARFILE;
import static org.openntf.nsfodp.commons.h.StdNames.IMAGE_NEW_DBICON_NAME;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_CONFIG_FILE_DATA;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_CONFIG_FILE_SIZE;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_JAVA_COMPILER_SOURCE;
import static com.darwino.domino.napi.util.DominoNativeUtils.matchesFlagsPattern;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import org.openntf.nsfodp.commons.NoteType;
import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.odp.OnDiskProject;
import org.openntf.nsfodp.exporter.io.CommonsSwiperOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.darwino.domino.napi.DominoAPI;
import com.darwino.domino.napi.DominoException;
import com.darwino.domino.napi.c.C;
import com.darwino.domino.napi.enums.DXL_RICHTEXT_OPTION;
import com.darwino.domino.napi.proc.NSFSEARCHPROC;
import com.darwino.domino.napi.struct.SEARCH_MATCH;
import com.darwino.domino.napi.wrap.FormulaException;
import com.darwino.domino.napi.wrap.NSFDXLExporter;
import com.darwino.domino.napi.wrap.NSFDatabase;
import com.darwino.domino.napi.wrap.NSFNote;
import com.darwino.domino.napi.wrap.NSFNoteIDCollection;
import com.darwino.domino.napi.wrap.item.NSFCompositeData;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.ByteStreamCache;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.Format;
import com.ibm.commons.xml.XMLException;

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
	
	private final NSFDatabase database;
	private boolean binaryDxl = false;
	private boolean richTextAsItemData = false;
	private boolean swiperFilter = false;
	private String projectName;

	public ODPExporter(NSFDatabase database) {
		this.database = database;
	}
	
	/**
	 * Sets the name for the project to be used inside the generated ODP.
	 * 
	 * @param projectName the name of the project to set
	 * @since 2.5.0
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	/**
	 * Gets the name for the project to be used inside the generated ODP.
	 * 
	 * @return the name of the project to set
	 * @since 2.5.0
	 */
	public String getProjectName() {
		return projectName;
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
	
	public Path export() throws IOException, XMLException, DominoException, FormulaException {
		Path result = Files.createTempDirectory(getClass().getName());
		
		NSFDXLExporter exporter = database.getParent().createDXLExporter();
		try {
			exporter.setOutputDoctype(false);
			
			Path databaseProperties = result.resolve("AppProperties").resolve("database.properties"); //$NON-NLS-1$ //$NON-NLS-2$
			Files.createDirectories(databaseProperties.getParent());
			NSFNoteIDCollection iconColl = database.getParent().createNoteIDCollection();
			try {
				iconColl.add(NOTE_ID_SPECIAL | NOTE_CLASS_ICON);
				NSFNote acl = database.getNoteByID(NOTE_ID_SPECIAL | NOTE_CLASS_ACL);
				try {
					iconColl.add(acl.getNoteID());
				} finally {
					acl.free();
				}
				
				try(OutputStream os = new CommonsSwiperOutputStream(databaseProperties, isSwiperFilter())) {
					exporter.export(database, iconColl, os);
				}
			} finally {
				iconColl.free();
			}
			
			exporter.setForceNoteFormat(isBinaryDxl());
			exporter.setRichTextOption(isRichTextAsItemData() ? DXL_RICHTEXT_OPTION.ItemData : DXL_RICHTEXT_OPTION.DXL);
			
			NSFSEARCHPROC proc = new NSFSEARCHPROC() {
				@Override public short callback(long searchMatchPtr, long summaryBufferPtr) throws DominoException {
					SEARCH_MATCH searchMatch = new SEARCH_MATCH();
					C.memcpy(searchMatch.getDataPtr(), 0, searchMatchPtr, 0, SEARCH_MATCH.sizeOf);

					short noteClass = searchMatch.getNoteClass();
					int noteId = searchMatch.getId().getNoteId();
					byte retFlags = searchMatch.getSERetFlags();
					
					boolean deleted = (noteClass & DominoAPI.NOTE_CLASS_NOTIFYDELETION) != 0;
					boolean isSearchMatch = (retFlags & DominoAPI.SE_FMATCH) != 0;  // The use of "since" means that non-matching notes will be returned; check this flag to make sure
					
					if(isSearchMatch && !deleted) {
						NoteType type = null;
						try {
							NSFNote note = database.getNoteByID(noteId);
							type = forNote(note);
							try {
								exportNote(note, exporter, result);
							} finally {
								note.free();
							}
						} catch(Throwable e) {
							System.out.println(StringUtil.format(Messages.ODPExporter_nativeExceptionNoteId, Integer.toString(noteId, 16), e.getMessage(), type));
							e.printStackTrace(System.out);
						}
					}
					return DominoAPI.NOERROR;
				}
			};
			database.search("@All", proc, (short)0, DominoAPI.NOTE_CLASS_ALLNONDATA, null, null); //$NON-NLS-1$
			
			// Export several notes specially
			int[] specialIds = new int[] { NOTE_CLASS_ICON, NOTE_CLASS_HELP, NOTE_CLASS_INFO };
			for(int id : specialIds) {
				try {
					NSFNote iconNote = database.getNoteByID(NOTE_ID_SPECIAL | id);
					if(iconNote != null) {
						try {
							if(iconNote.isRefValid()) {
								exportNote(iconNote, exporter, result);
							}
						} catch(Throwable e) {
							e.printStackTrace();
							System.out.println(StringUtil.format(Messages.ODPExporter_nativeExceptionSpecialNote, id, e.getMessage()));
						} finally {
							iconNote.free();
						}
					}
				} catch(DominoException e) {
					switch(e.getStatus()) {
					case 578:
						// "Special database object cannot be located", which is fine
						break;
					default:
						e.printStackTrace();
						System.out.println(StringUtil.format(Messages.ODPExporter_nativeExceptionSpecialNote, id, e.getMessage()));
						break;
					}
				}
			}
			
			generateManifestMf(result);
			generateEclipseProjectFile(result);
			createClasspathDirectories(result);
			createStubFiles(result);
		} finally {
			exporter.free();
		}
		
		return result;
	}

	private void exportNote(NSFNote note, NSFDXLExporter exporter, Path baseDir) throws IOException, XMLException, DominoException {
		NoteType type = forNote(note);
		if(type == NoteType.Unknown) {
			String flags = note.hasItem(DESIGN_FLAGS) ? note.getAsString(DESIGN_FLAGS, ' ') : StringUtil.EMPTY_STRING;
			String title = getTitle(note);
			if(StringUtil.isEmpty(title)) {
				title = Integer.toString(note.getNoteID(), 16);
			}
			System.out.println(StringUtil.format(Messages.ODPExporter_unknownNote, flags, title, (note.getNoteClassValue() & ~DominoAPI.NOTE_CLASS_DEFAULT)));
			return;
		}
		if(!type.isInOdp()) {
			return;
		}
		
		if(type.isSingleton()) {
			switch(type.getOutputFormat()) {
			case RAWFILE:
				exportFileData(note, exporter, baseDir, type.getPath(), type);
				break;
			case METADATA:
			case DXL:
			default:
				exportExplicitNote(note, exporter, baseDir, type.getPath());
				break;
			}
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
	 * @throws DominoException 
	 */
	private void exportNamedNote(NSFNote note, NSFDXLExporter exporter, Path baseDir, NoteType type) throws IOException, DominoException {
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
	 * @throws DominoException 
	 */
	private Path getCleanName(NSFNote note, NoteType type) throws DominoException {
		if(!note.hasItem(FIELD_TITLE)) {
			return Paths.get("(Untitled)"); //$NON-NLS-1$
		}
		
		String title;
		String path = note.hasItem(ITEM_NAME_FILE_NAMES) ? note.get(ITEM_NAME_FILE_NAMES, String[].class)[0] : null;
		if(StringUtil.isNotEmpty(path) && !IGNORE_FILENAMES_TYPES.contains(type)) {
			// Then it's a "true" VFS path
			return Paths.get(note.get(ITEM_NAME_FILE_NAMES, String[].class)[0].replace('/', File.separatorChar));
		} else {
			title = getTitle(note);
			
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
	 * @throws IOException
	 * @throws XMLException 
	 * @throws DominoException 
	 */
	private void exportNamedData(NSFNote note, NSFDXLExporter exporter, Path baseDir, NoteType type) throws IOException, XMLException, DominoException {
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
	 * @throws IOException
	 * @throws XMLException 
	 * @throws DominoException 
	 */
	private void exportNamedDataAndMetadata(NSFNote note, NSFDXLExporter exporter, Path baseDir, NoteType type) throws IOException, XMLException, DominoException {
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
		exporter.setOmitItemNames(ignoreItems);
		exporter.setProperty(38, true);
		try {
			exportExplicitNote(note, exporter, baseDir, type.getPath().resolve(name));
		} finally {
			exporter.setOmitItemNames(Collections.emptySet());
			exporter.setProperty(38, false);
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
	 * @throws IOException
	 * @throws XMLException 
	 * @throws DominoException 
	 */
	private void exportFileData(NSFNote note, NSFDXLExporter exporter, Path baseDir, Path path, NoteType type) throws IOException, XMLException, DominoException {
		Path fullPath = baseDir.resolve(path);
		Files.createDirectories(fullPath.getParent());
		
		try(OutputStream os = Files.newOutputStream(fullPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
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
							exporter.setForceNoteFormat(true);
						}
						
						exporter.export(note, baos);
						
						if(!isBinaryDxl()) {
							exporter.setForceNoteFormat(false);
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
			case ServerJavaScriptLibrary: {
				NSFCompositeData cd = getFileItem(note, type);
				if(cd != null) {
					try {
						cd.writeJavaScriptLibraryData(os);
					} finally {
						cd.free();
					}
				}
				break;
			}
			case CustomControl: {
				// Special behavior: also export the config data field
				
				Path configPath = fullPath.getParent().resolve(fullPath.getFileName()+"-config"); //$NON-NLS-1$
				try(OutputStream configOut = Files.newOutputStream(configPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					NSFCompositeData cd = note.getCompositeData(ITEM_NAME_CONFIG_FILE_DATA);
					if(cd != null) {
						try {
							cd.writeFileResourceData(configOut);
						} finally {
							cd.free();
						}
					}
				}
				// Fallthrough intentional
			}
			case XPage: {
				NSFCompositeData cd = getFileItem(note, type);
				if(cd != null) {
					try {
						cd.writeFileResourceData(os);
					} finally {
						cd.free();
					}
				}
				break;
			}
			case GenericFile: {
				if("plugin.xml".equals(fullPath.getFileName().toString()) && StringUtil.isNotEmpty(this.projectName)) { //$NON-NLS-1$
					// Special handling here to set the plugin ID
					ByteStreamCache bytes = new ByteStreamCache();
					NSFCompositeData cd = getFileItem(note, type);
					if(cd != null) {
						try {
							cd.writeFileResourceData(bytes.getOutputStream());
						} finally {
							cd.free();
						}
						Document pluginDom = DOMUtil.createDocument(bytes.getInputStream());
						Element pluginElement = pluginDom.getDocumentElement();
						pluginElement.setAttribute("id", this.projectName); //$NON-NLS-1$
						
						DOMUtil.serialize(os, pluginDom, Format.defaultFormat);
					}
				} else {
					NSFCompositeData cd = getFileItem(note, type);
					if(cd != null) {
						try {
							cd.writeFileResourceData(os);
						} finally {
							cd.free();
						}
					}
				}
				break;
			}
			case ImageResource: {
				NSFCompositeData cd = getFileItem(note, type);
				if(cd != null) {
					try {
						cd.writeImageResourceData(os);
					} finally {
						cd.free();
					}
				}
				break;
			}
			default: {
				NSFCompositeData cd = getFileItem(note, type);
				if(cd != null) {
					try {
						cd.writeFileResourceData(os);
					} finally {
						cd.free();
					}
				}
				break;
			}
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
	 * @throws DominoException 
	 */
	private void exportExplicitNote(NSFNote note, NSFDXLExporter exporter, Path baseDir, Path path) throws IOException, DominoException {
		Path fullPath = baseDir.resolve(path);
		Files.createDirectories(fullPath.getParent());
		
		try(OutputStream os = new CommonsSwiperOutputStream(fullPath, isSwiperFilter())) {
			exporter.export(note, os);
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
	 * @throws DominoException 
	 */
	private void generateEclipseProjectFile(Path baseDir) throws IOException, XMLException, DominoException {
		Path manifest = baseDir.resolve(".project"); //$NON-NLS-1$
		if(!Files.isRegularFile(manifest)) {
			try(OutputStream os = Files.newOutputStream(manifest, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				Document xmlDoc = DOMUtil.createDocument();
				Element projectDescription = DOMUtil.createElement(xmlDoc, "projectDescription"); //$NON-NLS-1$
				{
					Element name = DOMUtil.createElement(xmlDoc, projectDescription, "name"); //$NON-NLS-1$
					String path = database.getFilePath().replace('\\', '/');
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
		
		// Resources/Files and Code/Java may not be in the programmatic list
		Files.createDirectories(baseDir.resolve("Resources").resolve("Files")); //$NON-NLS-1$ //$NON-NLS-2$
		Files.createDirectories(baseDir.resolve("Code").resolve("Java")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Creates several known files that Designer exports as zero-length files even when they don't
	 * exist in the database
	 * 
	 * @param baseDir the base directory for export operations
	 * @throws IOException if there is a problem creating files
	 * @since 2.5.0
	 */
	private void createStubFiles(Path baseDir) throws IOException {
		Path usingDocument = baseDir.resolve("Resources").resolve("UsingDocument"); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.exists(usingDocument)) {
			Files.createFile(usingDocument);
		}
		Path aboutDocument = baseDir.resolve("Resources").resolve("AboutDocument"); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.exists(aboutDocument)) {
			Files.createFile(aboutDocument);
		}
	}
	
	public static NoteType forNote(NSFNote note) throws DominoException {
		String flags = note.hasItem(DominoAPI.DESIGN_FLAGS) ? note.getAsString(DominoAPI.DESIGN_FLAGS, ' ') : StringUtil.EMPTY_STRING;
		String title = getTitle(note);
		String flagsExt = note.hasItem(DESIGN_FLAGS_EXTENDED) ? note.getAsString(DESIGN_FLAGS_EXTENDED, ' ') : StringUtil.EMPTY_STRING;
		
		if(flags.indexOf('X') > -1) {
			return NoteType.AgentData;
		}
		
		switch(note.getNoteClassValue() & ~DominoAPI.NOTE_CLASS_DEFAULT) {
		case DominoAPI.NOTE_CLASS_ACL:
			return NoteType.ACL;
		case DominoAPI.NOTE_CLASS_DESIGN:
			return NoteType.DesignCollection;
		case DominoAPI.NOTE_CLASS_ICON:
			return NoteType.IconNote;
		case DominoAPI.NOTE_CLASS_VIEW:
			if(matchesFlagsPattern(flags, DFLAGPAT_FOLDER_DESIGN)) {
				return NoteType.Folder;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_VIEWMAP_DESIGN)) {
				return NoteType.Navigator;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SHARED_COLS)) {
				return NoteType.SharedColumn;
			} else {
				return NoteType.View;
			}
		case DominoAPI.NOTE_CLASS_FIELD:
			return NoteType.SharedField;
		case DominoAPI.NOTE_CLASS_HELP:
			return NoteType.UsingDocument;
		case DominoAPI.NOTE_CLASS_INFO:
			return NoteType.AboutDocument;
		case DominoAPI.NOTE_CLASS_FILTER:
			// "filter" is a dumping ground for pre-XPages code elements
			
			if(flags.indexOf(DESIGN_FLAG_DATABASESCRIPT) > -1) {
				return NoteType.DBScript;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SITEMAP)) {
				return NoteType.Outline;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_LS)) {
				if(flagsExt.indexOf(DESIGN_FLAGEXT_WEBSERVICELIB) > -1) {
					return NoteType.LotusScriptWebServiceConsumer;
				} else {
					return NoteType.LotusScriptLibrary; 
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_JAVA)) {
				if(flagsExt.indexOf(DESIGN_FLAGEXT_WEBSERVICELIB) > -1) {
					return NoteType.JavaWebServiceConsumer;
				} else {
					return NoteType.JavaLibrary;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_JS)) {
				return NoteType.JavaScriptLibrary;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_SERVER_JS)) {
				return NoteType.ServerJavaScriptLibrary;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_JAVA_WEBSERVICE)) {
				return NoteType.JavaWebService;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_LS_WEBSERVICE)) {
				return NoteType.LotusScriptWebService;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_DATA_CONNECTION_RESOURCE)) {
				return NoteType.DataConnection;
			}
			
			// Determine from here what kind of agent it is
			int assistType = 0;
			if(note.hasItem(ASSIST_TYPE_ITEM)) {
				assistType = note.get(ASSIST_TYPE_ITEM, int.class);
			}
			
			if(flags.indexOf(DESIGN_FLAG_LOTUSSCRIPT_AGENT) > -1) {
				return NoteType.LotusScriptAgent;
			} else if(flags.indexOf(DESIGN_FLAG_JAVA_AGENT) > -1 || flags.indexOf(DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE) > -1 || assistType == ASSIST_TYPE_JAVA) {
				// There's not a proper pattern for distinguishing between these two, so look for another marker
				if(flags.indexOf(DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE) > -1 || note.hasItem(ITEM_NAME_JAVA_COMPILER_SOURCE)) {
					return NoteType.JavaAgent;
				} else {
					return NoteType.ImportedJavaAgent;
				}
			} else if(assistType == -1) {
				return NoteType.SimpleActionAgent;
			} else {
				return NoteType.FormulaAgent;
			}
		case DominoAPI.NOTE_CLASS_FORM:
			// Pretty much everything is a form nowadays
			if(flags.isEmpty()) {
				// Definitely an actual form
				return NoteType.Form;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				if(IMAGE_NEW_DBICON_NAME.equals(title)) {
					return NoteType.DBIcon;
				}
				return NoteType.ImageResource;
			} else if(flags.indexOf(DESIGN_FLAG_JARFILE) > -1) {
				return NoteType.Jar;			
			} else if(matchesFlagsPattern(flags, DFLAGPAT_COMPDEF)) {
				return NoteType.WiringProperties;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_COMPAPP)) {
				return NoteType.CompositeApplication;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_WIDGET)) {
				return NoteType.CompositeComponent;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_XSPCC)) {
				if(flags.indexOf(DESIGN_FLAG_PROPFILE) > -1) {
					return NoteType.CustomControlProperties;
				} else {
					return NoteType.CustomControl;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_XSPPAGE)) {
				if(flags.indexOf(DESIGN_FLAG_PROPFILE) > -1) {
					return NoteType.XPageProperties;
				} else {
					return NoteType.XPage;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_STYLEKIT)) {
				return NoteType.Theme;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_WEBPAGE)) {
				return NoteType.Page;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				return NoteType.ImageResource;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_STYLE_SHEET_RESOURCE)) {
				return NoteType.StyleSheet;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SUBFORM_DESIGN)) {
				return NoteType.Subform;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_FRAMESET)) {
				return NoteType.Frameset;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_DB2ACCESSVIEW)) {
				return NoteType.DB2AccessView;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_FILE)) {
				// xspdesign.properties needs special handling, but is distinguished only by file name
				String filePath = note.hasItem(DominoAPI.ITEM_NAME_FILE_NAMES) ? note.get(DominoAPI.ITEM_NAME_FILE_NAMES, String[].class)[0] : null;
				
				if(flags.indexOf(DESIGN_FLAG_HIDEFROMDESIGNLIST) == -1) {
					return NoteType.FileResource;
				} else if("xspdesign.properties".equals(filePath)) { //$NON-NLS-1$
					return NoteType.XSPDesignProperties;
				} else if(flagsExt.indexOf(DESIGN_FLAGEXT_WEBCONTENTFILE) > -1) {
					return NoteType.WebContentFile;
				} else if(matchesFlagsPattern(flags, DFLAGPAT_JAVAFILE)) {
					return NoteType.Java;
				} else {
					return NoteType.GenericFile;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SACTIONS_DESIGN)) {
				return NoteType.SharedActions;
			} else if(flags.indexOf(DESIGN_FLAG_JAVA_RESOURCE) > -1) {
				return NoteType.Applet;
			} else {
				return NoteType.Form;
			} 
		}
		
		return NoteType.Unknown;
	}
	
	private NSFCompositeData getFileItem(NSFNote note, NoteType type) throws DominoException {
		NSFCompositeData cd = note.getCompositeData(type.getFileItem());
		if(cd == null) {
			cd = note.getCompositeData(ITEM_NAME_FILE_DATA);
		}
		if(cd != null) {
			return cd;
		}
		
		return null;
	}
	
	private static String getTitle(NSFNote note) throws DominoException {
		if(note.hasItem(DominoAPI.FIELD_TITLE)) {
			String[] titles = note.get(DominoAPI.FIELD_TITLE, String[].class);
			if(titles != null && titles.length > 0) {
				return StringUtil.toString(titles[0]);
			}
		}
		return StringUtil.EMPTY_STRING;
	}
}
