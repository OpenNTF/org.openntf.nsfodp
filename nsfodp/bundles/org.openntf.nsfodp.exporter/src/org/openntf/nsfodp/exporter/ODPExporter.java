/**
 * Copyright © 2018-2021 Jesse Gallagher
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

import static org.openntf.nsfodp.commons.h.StdNames.ASSIST_TYPE_ITEM;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGEXT_WEBCONTENTFILE;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGEXT_WEBSERVICELIB;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGS;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGS_EXTENDED;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_DATABASESCRIPT;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_HIDEFROMDESIGNLIST;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JAVA_AGENT;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JAVA_RESOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_LOTUSSCRIPT_AGENT;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_PROPFILE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_COMPAPP;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_COMPDEF;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_DATA_CONNECTION_RESOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_DB2ACCESSVIEW;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_FILE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_FOLDER_DESIGN;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_FRAMESET;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_IMAGE_RESOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_JAVAFILE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_JAVA_WEBSERVICE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_LS_WEBSERVICE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SACTIONS_DESIGN;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SCRIPTLIB_JAVA;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SCRIPTLIB_JS;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SCRIPTLIB_LS;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SCRIPTLIB_SERVER_JS;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SHARED_COLS;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SITEMAP;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_STYLEKIT;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_STYLE_SHEET_RESOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SUBFORM_DESIGN;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_VIEWMAP_DESIGN;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_WEBPAGE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_WIDGET;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_XSPCC;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_XSPPAGE;
import static org.openntf.nsfodp.commons.h.StdNames.FIELD_TITLE;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_FILE_DATA;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_FILE_NAMES;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_FILE_SIZE;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_ACL;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_HELP;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_ICON;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_INFO;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_ID_SPECIAL;
import static org.openntf.nsfodp.commons.h.StdNames.SCRIPTLIB_OBJECT;
import static org.openntf.nsfodp.commons.h.StdNames.XSP_CLASS_INDEX;
import static org.openntf.nsfodp.commons.h.StdNames.ASSIST_TYPE_JAVA;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JARFILE;
import static org.openntf.nsfodp.commons.h.StdNames.IMAGE_NEW_DBICON_NAME;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_CONFIG_FILE_DATA;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_CONFIG_FILE_SIZE;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_JAVA_COMPILER_SOURCE;
import static org.openntf.nsfodp.commons.NSFODPUtil.matchesFlagsPattern;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.NoteType;
import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.h.NsfNote;
import org.openntf.nsfodp.commons.h.StdNames;
import org.openntf.nsfodp.commons.odp.OnDiskProject;
import org.openntf.nsfodp.commons.odp.notesapi.NCompositeData;
import org.openntf.nsfodp.commons.odp.notesapi.NDXLExporter;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NDominoException;
import org.openntf.nsfodp.commons.odp.notesapi.NNote;
import org.openntf.nsfodp.exporter.io.CommonsSwiperOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
	public enum ODPType {
		DIRECTORY, ZIP
	}
	
	public static final String EXT_METADATA = ".metadata"; //$NON-NLS-1$
	private static final Collection<NoteType> IGNORE_FILENAMES_TYPES = EnumSet.of(NoteType.FileResource, NoteType.StyleSheet, NoteType.ImageResource, NoteType.Theme);
	
	private final NDatabase database;
	private boolean binaryDxl = false;
	private boolean richTextAsItemData = false;
	private boolean swiperFilter = false;
	private String projectName;
	private ODPType odpType = ODPType.DIRECTORY;

	public ODPExporter(NDatabase database) {
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
	 */
	public void setSwiperFilter(boolean swiperFilter) {
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
	
	/**
	 * Sets the type of ODP to create on export.
	 * 
	 * @param odpType an {@link ODPType} value, or {@code null} to set to the default
	 */
	public void setOdpType(ODPType odpType) {
		this.odpType = odpType == null ? ODPType.DIRECTORY : odpType;
	}
	
	/**
	 * Gets the current type of ODP to be generated.
	 * 
	 * @return the configured {@link ODPType} value
	 */
	public ODPType getOdpType() {
		return odpType;
	}
	
	/**
	 * Exports the NSF to an on-disk project using the configured settings.
	 * 
	 * @return a {@link Path} to the on-disk project root, either a directory or a ZIP file
	 * @throws IOException if there is a problem reading or writing filesystem data
	 * @throws XMLException if there is a problem parsing DXL or other configuration information in the ODP
	 */
	public Path export() throws IOException, XMLException {
		Path target;
		Path returnPath;
		ODPType odpType = this.odpType == null ? ODPType.DIRECTORY : this.odpType;
		switch(odpType) {
		case ZIP:
			returnPath = Files.createTempFile(NSFODPUtil.getTempDirectory(), "org.openntf.nsfodp.exporter", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			target = NSFODPUtil.openZipPath(returnPath).getPath("/"); //$NON-NLS-1$
			break;
		case DIRECTORY:
		default:
			target = returnPath = Files.createTempDirectory(getClass().getName());
			break;
		}
		
		try(NDXLExporter exporter = database.getAPI().createDXLExporter()) {
			// Output database.properties in encapsulated format
			Path databaseProperties = target.resolve("AppProperties").resolve("database.properties"); //$NON-NLS-1$ //$NON-NLS-2$
			Files.createDirectories(databaseProperties.getParent());
			Set<Integer> iconColl = new HashSet<>();
			iconColl.add(NOTE_ID_SPECIAL | NOTE_CLASS_ICON);
			try(NNote acl = database.getNoteByID(NOTE_ID_SPECIAL | NOTE_CLASS_ACL)) {
				iconColl.add(acl.getNoteID());
			}
			
			try(OutputStream os = new CommonsSwiperOutputStream(databaseProperties, isSwiperFilter())) {
				exporter.export(database, iconColl, os);
			}
			
			// Output the rest according to the settings
			exporter.setForceNoteFormat(isBinaryDxl());
			exporter.setRichTextAsItemData(isRichTextAsItemData());
			
			database.eachDesignNote((noteId, note) -> {
				NoteType type = null;
				try {
					type = forNote(note);
					exportNote(note, exporter, target);
				} catch(Throwable e) {
					System.out.println(StringUtil.format(Messages.ODPExporter_nativeExceptionNoteId, Integer.toString(noteId, 16), e.getMessage(), type));
					e.printStackTrace(System.out);
				}
			});
			
			// Export several notes specially
			int[] specialIds = new int[] { NOTE_CLASS_ICON, NOTE_CLASS_HELP, NOTE_CLASS_INFO };
			for(int id : specialIds) {
				try {
					try(NNote iconNote = database.getNoteByID(NOTE_ID_SPECIAL | id)) {
						if(iconNote != null && iconNote.isRefValid()) {
							exportNote(iconNote, exporter, target);
						}
					}
				} catch(NDominoException e) {
					switch(e.getStatus()) {
					case 578:
						// "Special database object cannot be located", which is fine
						break;
					default:
						e.printStackTrace();
						System.out.println(StringUtil.format(Messages.ODPExporter_nativeExceptionSpecialNote, id, e.getMessage()));
						break;
					}
				} catch(Throwable e) {
					e.printStackTrace();
					System.out.println(StringUtil.format(Messages.ODPExporter_nativeExceptionSpecialNote, id, e.getMessage()));
				}
			}
			
			generateManifestMf(target);
			generateEclipseProjectFile(target);
			createClasspathDirectories(target);
			createStubFiles(target);
		}
		
		if(odpType == ODPType.ZIP) {
			target.getFileSystem().close();
		}
		
		return returnPath;
	}

	private void exportNote(NNote note, NDXLExporter exporter, Path baseDir) throws IOException, XMLException {
		NoteType type = forNote(note);
		if(type == NoteType.Unknown) {
			String flags = note.hasItem(DESIGN_FLAGS) ? note.getAsString(DESIGN_FLAGS, ' ') : StringUtil.EMPTY_STRING;
			String title = getTitle(note);
			if(StringUtil.isEmpty(title)) {
				title = Integer.toString(note.getNoteID(), 16);
			}
			System.out.println(StringUtil.format(Messages.ODPExporter_unknownNote, flags, title, (note.getNoteClassValue() & ~NsfNote.NOTE_CLASS_DEFAULT)));
			return;
		}
		if(!type.isInOdp()) {
			return;
		}
		
		if(type.isSingleton()) {
			switch(type.getOutputFormat()) {
			case RAWFILE:
				exportFileData(note, exporter, baseDir, type.getPath(baseDir.getFileSystem()), type);
				break;
			case METADATA:
			case DXL:
			default:
				exportExplicitNote(note, exporter, baseDir, type.getPath(baseDir.getFileSystem()));
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
	 */
	private void exportNamedNote(NNote note, NDXLExporter exporter, Path baseDir, NoteType type) throws IOException {
		Path name = getCleanName(baseDir.getFileSystem(), note, type);
		if(StringUtil.isNotEmpty(type.getExtension()) && !name.getFileName().toString().endsWith(type.getExtension())) {
			Path parent = name.getParent();
			if(parent == null) {
				name = baseDir.getFileSystem().getPath(name.getFileName().toString() + '.' + type.getExtension());
			} else {
				name = parent.resolve(name.getFileName().toString() + '.' + type.getExtension());
			}
		}
		
		exportExplicitNote(note, exporter, baseDir, type.getPath(baseDir.getFileSystem()).resolve(name));
	}
	
	/**
	 * Converted a VFS-style file name to an FS-friendly version.
	 * 
	 * @param contextFileSystem the contextual filesystem for generating names
	 * @param note the note to get a title for
	 * @param type the {@link NoteType} value corresponding to the note
	 * @return an FS-friendly version of the title
	 * @throws DominoException 
	 */
	private Path getCleanName(FileSystem contextFileSystem, NNote note, NoteType type) {
		if(!note.hasItem(FIELD_TITLE)) {
			return contextFileSystem.getPath("(Untitled)"); //$NON-NLS-1$
		}
		
		String title;
		String path = note.hasItem(ITEM_NAME_FILE_NAMES) ? note.get(ITEM_NAME_FILE_NAMES, String[].class)[0] : null;
		if(StringUtil.isNotEmpty(path) && !IGNORE_FILENAMES_TYPES.contains(type)) {
			// Then it's a "true" VFS path
			return contextFileSystem.getPath(note.get(ITEM_NAME_FILE_NAMES, String[].class)[0].replace("/", contextFileSystem.getSeparator())); //$NON-NLS-1$
		} else {
			title = getTitle(note);
			
			int pipe = title.indexOf('|');
			String clean = pipe > -1 ? title.substring(0, pipe) : title;
			clean = clean.isEmpty() ? "(Untitled)" : clean; //$NON-NLS-1$
			
			// TODO replace with a proper algorithm 
			return contextFileSystem.getPath(clean
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
	 */
	private void exportNamedData(NNote note, NDXLExporter exporter, Path baseDir, NoteType type) throws IOException, XMLException {
		Path name = getCleanName(baseDir.getFileSystem(), note, type);
		if(StringUtil.isNotEmpty(type.getExtension()) && !name.getFileName().toString().endsWith(type.getExtension())) {
			Path parent = name.getParent();
			if(parent == null) {
				name = baseDir.getFileSystem().getPath(name.getFileName().toString() + '.' + type.getExtension());
			} else {
				name = parent.resolve(name.getFileName().toString() + '.' + type.getExtension());
			}
		}
		
		// These are normal files in the NSF, but should not be exported
		if(name.startsWith(baseDir.getFileSystem().getPath("WebContent", "WEB-INF", "classes")) || name.startsWith(baseDir.getFileSystem().getPath("WEB-INF", "classes"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return;
		} else if(name.getFileName().toString().equals("build.properties")) { //$NON-NLS-1$
			return;
		}
		
		exportFileData(note, exporter, baseDir, type.getPath(baseDir.getFileSystem()).resolve(name), type);
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
	 */
	private void exportNamedDataAndMetadata(NNote note, NDXLExporter exporter, Path baseDir, NoteType type) throws IOException, XMLException {
		exportNamedData(note, exporter, baseDir, type);
		
		Path name = getCleanName(baseDir.getFileSystem(), note, type);
		if(StringUtil.isNotEmpty(type.getExtension()) && !name.getFileName().toString().endsWith(type.getExtension())) {
			Path parent = name.getParent();
			if(parent == null) {
				name = baseDir.getFileSystem().getPath(name.getFileName().toString() + '.' + type.getExtension() + EXT_METADATA);
			} else {
				name = parent.resolve(name.getFileName().toString() + '.' + type.getExtension() + EXT_METADATA);
			}
		} else {
			Path parent = name.getParent();
			if(parent == null) {
				name = baseDir.getFileSystem().getPath(name.getFileName().toString() + EXT_METADATA);
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
		boolean rawFormat = exporter.isForceNoteFormat();
		exporter.setForceNoteFormat(true);
		try {
			exportExplicitNote(note, exporter, baseDir, type.getPath(baseDir.getFileSystem()).resolve(name));
		} finally {
			exporter.setOmitItemNames(Collections.emptySet());
			exporter.setProperty(38, false);
			exporter.setForceNoteFormat(rawFormat);
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
	 */
	private void exportFileData(NNote note, NDXLExporter exporter, Path baseDir, Path path, NoteType type) throws IOException, XMLException {
		Path fullPath = baseDir.resolve(path.toString());
		Files.createDirectories(fullPath.getParent());
		
		try(OutputStream os = Files.newOutputStream(fullPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			// readFileContent works for some but not all file types
			switch(type) {
			case LotusScriptLibrary:
				// This is actually just a set of text items. The NAPI wrapper, however, doesn't
				//   properly handle multiple text items of the same name, so we have to do it
				//   manually
				try(Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
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
				try(NCompositeData cd = getFileItem(note, type)) {
					if(cd != null) {
						cd.writeJavaScriptLibraryData(os);
					}
				}
				break;
			}
			case CustomControl: {
				// Special behavior: also export the config data field
				
				Path configPath = fullPath.getParent().resolve(fullPath.getFileName()+"-config"); //$NON-NLS-1$
				try(OutputStream configOut = Files.newOutputStream(configPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					try(NCompositeData cd = note.getCompositeData(ITEM_NAME_CONFIG_FILE_DATA)) {
						if(cd != null) {
							cd.writeFileResourceData(configOut);
						}
					}
				}
				// Fallthrough intentional
			}
			case XPage: {
				try(NCompositeData cd = getFileItem(note, type)) {
					if(cd != null) {
						cd.writeFileResourceData(os);
					}
				}
				break;
			}
			case GenericFile: {
				if("plugin.xml".equals(fullPath.getFileName().toString()) && StringUtil.isNotEmpty(this.projectName)) { //$NON-NLS-1$
					// Special handling here to set the plugin ID
					ByteStreamCache bytes = new ByteStreamCache();
					try(NCompositeData cd = getFileItem(note, type)) {
						if(cd != null) {
							cd.writeFileResourceData(bytes.getOutputStream());
							
							Document pluginDom = DOMUtil.createDocument(bytes.getInputStream());
							Element pluginElement = pluginDom.getDocumentElement();
							pluginElement.setAttribute("id", this.projectName); //$NON-NLS-1$
							
							DOMUtil.serialize(os, pluginDom, Format.defaultFormat);
						}
					}
				} else {
					try(NCompositeData cd = getFileItem(note, type)) {
						if(cd != null) {
							cd.writeFileResourceData(os);
						}
					}
				}
				break;
			}
			case ImageResource: {
				try(NCompositeData cd = getFileItem(note, type)) {
					if(cd != null) {
						cd.writeImageResourceData(os);
					}
				}
				break;
			}
			default: {
				try(NCompositeData cd = getFileItem(note, type)) {
					if(cd != null) {
						cd.writeFileResourceData(os);
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
	 */
	private void exportExplicitNote(NNote note, NDXLExporter exporter, Path baseDir, Path path) throws IOException {
		Path fullPath = baseDir.resolve(path.toString());
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
	 */
	private void generateEclipseProjectFile(Path baseDir) throws IOException, XMLException {
		Path manifest = baseDir.resolve(".project"); //$NON-NLS-1$
		if(!Files.isRegularFile(manifest)) {
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
			try(OutputStream os = Files.newOutputStream(manifest, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
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
	
	public static NoteType forNote(NNote note) {
		String flags = note.hasItem(StdNames.DESIGN_FLAGS) ? note.getAsString(StdNames.DESIGN_FLAGS, ' ') : StringUtil.EMPTY_STRING;
		String title = getTitle(note);
		String flagsExt = note.hasItem(DESIGN_FLAGS_EXTENDED) ? note.getAsString(DESIGN_FLAGS_EXTENDED, ' ') : StringUtil.EMPTY_STRING;
		
		if(flags.indexOf('X') > -1) {
			return NoteType.AgentData;
		}
		
		
		switch(note.getNoteClassValue() & NsfNote.NOTE_CLASS_NONPRIV) {
		case NOTE_CLASS_ACL:
			return NoteType.ACL;
		case NsfNote.NOTE_CLASS_DESIGN:
			return NoteType.DesignCollection;
		case NOTE_CLASS_ICON:
			return NoteType.IconNote;
		case NsfNote.NOTE_CLASS_VIEW:
			if(matchesFlagsPattern(flags, DFLAGPAT_FOLDER_DESIGN)) {
				return NoteType.Folder;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_VIEWMAP_DESIGN)) {
				return NoteType.Navigator;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SHARED_COLS)) {
				return NoteType.SharedColumn;
			} else {
				return NoteType.View;
			}
		case NsfNote.NOTE_CLASS_FIELD:
			return NoteType.SharedField;
		case NsfNote.NOTE_CLASS_HELP:
			return NoteType.UsingDocument;
		case NsfNote.NOTE_CLASS_INFO:
			return NoteType.AboutDocument;
		case NsfNote.NOTE_CLASS_FILTER:
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
		case NsfNote.NOTE_CLASS_FORM:
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
				String filePath = note.hasItem(StdNames.ITEM_NAME_FILE_NAMES) ? note.get(StdNames.ITEM_NAME_FILE_NAMES, String[].class)[0] : null;
				
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
		case NsfNote.NOTE_CLASS_REPLFORMULA:
			return NoteType.ReplicationFormula;
		}
		
		return NoteType.Unknown;
	}
	
	private NCompositeData getFileItem(NNote note, NoteType type) {
		NCompositeData cd = note.getCompositeData(type.getFileItem());
		if(cd == null) {
			cd = note.getCompositeData(ITEM_NAME_FILE_DATA);
		}
		if(cd != null) {
			return cd;
		}
		
		return null;
	}
	
	private static String getTitle(NNote note) {
		if(note.hasItem(StdNames.FIELD_TITLE)) {
			String[] titles = note.get(StdNames.FIELD_TITLE, String[].class);
			if(titles != null && titles.length > 0) {
				return StringUtil.toString(titles[0]);
			}
		}
		return StringUtil.EMPTY_STRING;
	}
}
