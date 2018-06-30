/**
 * Copyright © 2018 Jesse Gallagher
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
import static com.ibm.designer.domino.napi.NotesConstants.*;
import static org.openntf.nsfodp.commons.NSFODPConstants.JAVA_ITEM_IGNORE_PATTERN;
import static com.ibm.designer.domino.napi.util.NotesUtils.CmemflagTest;
import static com.ibm.designer.domino.napi.util.NotesUtils.CmemflagTestMultiple;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesNoteItem;
import com.ibm.domino.napi.c.NsfNote;

public enum NoteType {
	/** The icon note is also represented in "database.properties" */
	IconNote(Paths.get("Resources", "IconNote"), true), //$NON-NLS-1$ //$NON-NLS-2$
	DBIcon(Paths.get("AppProperties", "$DBIcon"), true), //$NON-NLS-1$ //$NON-NLS-2$
	DBScript(Paths.get("Code", "dbscript.lsdb"), true), //$NON-NLS-1$ //$NON-NLS-2$
	Java(null, Paths.get("Code", "Java"), false, ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN), //$NON-NLS-1$ //$NON-NLS-2$
	JavaScriptLibrary("js", Paths.get("Code", "ScriptLibraries"), false, JAVASCRIPTLIBRARY_CODE, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptLibrary("lss", Paths.get("Code", "ScriptLibraries"), false, SCRIPTLIB_ITEM_NAME, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaLibrary("javalib", Paths.get("Code", "ScriptLibraries"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CustomControl(null, Paths.get("CustomControls"), false, ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN), //$NON-NLS-1$
	XPage(null, Paths.get("XPages"), false, ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN), //$NON-NLS-1$
	Form("form", Paths.get("Forms"), false), //$NON-NLS-1$ //$NON-NLS-2$
	Frameset("frameset", Paths.get("Framesets"), false), //$NON-NLS-1$ //$NON-NLS-2$
	ServerJavaScriptLibrary("jss", Paths.get("Code", "ScriptLibraries"), false, SERVER_JAVASCRIPTLIBRARY_CODE, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Subform("subform", Paths.get("SharedElements", "Subforms"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Page("page", Paths.get("Pages"), false), //$NON-NLS-1$ //$NON-NLS-2$
	AboutDocument(Paths.get("Resources", "AboutDocument"), true), //$NON-NLS-1$ //$NON-NLS-2$
	FileResource(null, Paths.get("Resources", "Files"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	ImageResource(null, Paths.get("Resources", "Images"), false, ITEM_NAME_IMAGE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	StyleSheet("css", Paths.get("Resources", "Stylesheets"), false, ITEM_NAME_STYLE_SHEET_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Theme(null, Paths.get("Resources", "Themes"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	UsingDocument(Paths.get("Resources", "UsingDocument"), true), //$NON-NLS-1$ //$NON-NLS-2$
	SharedField("field", Paths.get("SharedElements", "Fields"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Outline("outline", Paths.get("SharedElements", "Outlines"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	View("view", Paths.get("Views"), false), //$NON-NLS-1$ //$NON-NLS-2$
	JavaAgent("ja", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	ImportedJavaAgent("ija", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptAgent("lsa", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Folder("folder", Paths.get("Folders"), false), //$NON-NLS-1$ //$NON-NLS-2$
	SharedColumn("column", Paths.get("SharedElements", "Columns"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Jar(null, Paths.get("Code", "Jars"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	JavaWebService("jws", Paths.get("Code", "WebServices"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebService("lws", Paths.get("Code", "WebServices"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SharedActions(Paths.get("Code", "actions", "Shared Actions"), true), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SimpleActionAgent("aa", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	FormulaAgent("fa", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Navigator("navigator", Paths.get("SharedElements", "Navigators"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebServiceConsumer("lswsc", Paths.get("Code", "WebServiceConsumer"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaWebServiceConsumer("javalib", Paths.get("Code", "WebServiceConsumer"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	WebContentFile(null, Paths.get("WebContent"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$
	DataConnection("dcr", Paths.get("Data", "DataConnections"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DB2AccessView("db2v", Paths.get("Data", "DB2AccessViews"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Applet("applet", Paths.get("Resources", "Applets"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CompositeApplication("ca", Paths.get("CompositeApplications", "Applications"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CompositeComponent("component", Paths.get("CompositeApplications", "Components"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	WiringProperties(null, Paths.get("CompositeApplications", "WiringProperties"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	/** e.g. plugin.xml, etc. */
	GenericFile(Paths.get("."), false), //$NON-NLS-1$
	/** This is wrapped up into the DB properties */
	ACL(null, null, false),
	DesignCollection(null, null, false),
	Unknown(null, null, false);
	
	
	
	/**
	 * The extension to use for exported files, or <code>null</code> if there is no
	 * consistent extension to add.
	 */
	public final String extension;
	/**
	 * The relative path for exporting the file, either a directory (for individually-named
	 * elements) or an explicit file path (for singleton elements).
	 * 
	 * {@see #singleton}
	 */
	public final Path path;
	/**
	 * Whether or not there is only one note of this kind in the database
	 */
	public final boolean singleton;
	/**
	 * The item containing file data to export, if applicable
	 */
	public final String fileItem;
	/**
	 * A regular expression to match against item names to ignore during export, if applicable
	 */
	public final String itemNameIgnorePattern;
	
	private NoteType(Path path, boolean singleton) {
		this(null, path, singleton);
	}
	private NoteType(String extension, Path path, boolean singleton) {
		this(extension, path, singleton, null, null);
	}
	private NoteType(String extension, Path path, boolean singleton, String fileItem, String itemNameIgnorePattern) {
		this.extension = extension;
		this.path = path;
		this.singleton = singleton;
		this.fileItem = fileItem;
		this.itemNameIgnorePattern = itemNameIgnorePattern;
	}
	
	public static NoteType forNote(NotesNote note) throws NotesAPIException {
		String flags = note.isItemPresent(DESIGN_FLAGS) ? note.getItemValueAsString(DESIGN_FLAGS) : StringUtil.EMPTY_STRING;
		String title = note.isItemPresent(FIELD_TITLE) ? note.getItemAsTextList(FIELD_TITLE).get(0) : StringUtil.EMPTY_STRING;
		String flagsExt = note.isItemPresent(DESIGN_FLAGS_EXTENDED) ? note.getItemValueAsString(DESIGN_FLAGS_EXTENDED) : StringUtil.EMPTY_STRING;
		
		switch(note.getNoteClass() & ~NsfNote.NOTE_CLASS_DEFAULT) {
		case NsfNote.NOTE_CLASS_ACL:
			return ACL;
		case NsfNote.NOTE_CLASS_DESIGN:
			return DesignCollection;
		case NsfNote.NOTE_CLASS_ICON:
			return IconNote;
		case NsfNote.NOTE_CLASS_VIEW:
			if(CmemflagTestMultiple(flags, DFLAGPAT_FOLDER_DESIGN)) {
				return Folder;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_VIEWMAP_DESIGN)) {
				return Navigator;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SHARED_COLS)) {
				return SharedColumn;
			} else {
				return View;
			}
		case NsfNote.NOTE_CLASS_FIELD:
			return SharedField;
		case NsfNote.NOTE_CLASS_HELP:
			return UsingDocument;
		case NsfNote.NOTE_CLASS_INFO:
			return AboutDocument;
		case NsfNote.NOTE_CLASS_FILTER:
			// "filter" is a dumping ground for pre-XPages code elements
			
			if(CmemflagTest(flags, DESIGN_FLAG_DATABASESCRIPT)) {
				return DBScript;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SITEMAP)) {
				return Outline;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SCRIPTLIB_LS)) {
				if(CmemflagTest(flagsExt, DESIGN_FLAGEXT_WEBSERVICELIB)) {
					return LotusScriptWebServiceConsumer;
				} else {
					return LotusScriptLibrary; 
				}
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SCRIPTLIB_JAVA)) {
				if(CmemflagTest(flagsExt, DESIGN_FLAGEXT_WEBSERVICELIB)) {
					return JavaWebServiceConsumer;
				} else {
					return JavaLibrary;
				}
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SCRIPTLIB_JS)) {
				return JavaScriptLibrary;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SCRIPTLIB_SERVER_JS)) {
				return ServerJavaScriptLibrary;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_JAVA_WEBSERVICE)) {
				return JavaWebService;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_LS_WEBSERVICE)) {
				return LotusScriptWebService;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_DATA_CONNECTION_RESOURCE)) {
				return DataConnection;
			}
			
			// Determine from here what kind of agent it is
			int assistType = 0;
			if(note.isItemPresent(ASSIST_TYPE_ITEM)) {
				NotesNoteItem item = note.getItem(ASSIST_TYPE_ITEM);
				try {
					assistType = item.getValueAsInteger();
				} finally {
					item.recycle();
				}
			}
			
			if(CmemflagTest(flags, DESIGN_FLAG_LOTUSSCRIPT_AGENT)) {
				return LotusScriptAgent;
			} else if(CmemflagTest(flags, DESIGN_FLAG_JAVA_AGENT) || CmemflagTest(flags, DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE) || assistType == ASSIST_TYPE_JAVA) {
				// There's not a proper pattern for distinguishing between these two, so look for another marker
				if(note.isItemPresent(ITEM_NAME_JAVA_COMPILER_SOURCE)) {
					return JavaAgent;
				} else {
					return ImportedJavaAgent;
				}
			} else if(assistType == -1) {
				return SimpleActionAgent;
			} else {
				return FormulaAgent;
			}
		case NsfNote.NOTE_CLASS_FORM:
			// Pretty much everything is a form nowadays
			if(flags.isEmpty()) {
				// Definitely an actual form
				return Form;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				if(IMAGE_NEW_DBICON_NAME.equals(title)) {
					return DBIcon;
				}
				return ImageResource;
			} else if(CmemflagTest(flags, DESIGN_FLAG_JARFILE)) {
				return Jar;			
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_COMPDEF)) {
				return WiringProperties;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_COMPAPP)) {
				return CompositeApplication;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_WIDGET)) {
				return CompositeComponent;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_XSPPAGE_NOPROPS)) {
				// TODO figure out XPages properties files
				return XPage;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_XSPCC)) {
				return CustomControl;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_STYLEKIT)) {
				return Theme;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_WEBPAGE)) {
				return Page;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				return ImageResource;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_STYLE_SHEET_RESOURCE)) {
				return StyleSheet;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SUBFORM_DESIGN)) {
				return Subform;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_FRAMESET)) {
				return Frameset;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_DB2ACCESSVIEW)) {
				return DB2AccessView;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_FILE)) {
				if(!CmemflagTest(flags, DESIGN_FLAG_HIDEFROMDESIGNLIST)) {
					return FileResource;
				} else if(CmemflagTest(flagsExt, DESIGN_FLAGEXT_WEBCONTENTFILE)) {
					return WebContentFile;
				} else if(CmemflagTestMultiple(flags, DFLAGPAT_JAVAFILE)) {
					return Java;
				} else {
					return GenericFile;
				}
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SACTIONS_DESIGN)) {
				return SharedActions;
			} else if(CmemflagTest(flags, DESIGN_FLAG_JAVA_RESOURCE)) {
				return Applet;
			} else {
				return Form;
			} 
		}
		
		return Unknown;
	}
}
