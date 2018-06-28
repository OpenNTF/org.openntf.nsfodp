package org.openntf.nsfodp.exporter;

import static org.openntf.nsfodp.commons.h.StdNames.*;

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
	Java(Paths.get("Code", "Java"), false), //$NON-NLS-1$ //$NON-NLS-2$
	JavaScriptLibrary("js", Paths.get("Code", "ScriptLibraries"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptLibrary("lss", Paths.get("Code", "ScriptLibraries"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaLibrary("javalib", Paths.get("Code", "ScriptLibraries"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CustomControl(Paths.get("CustomControls"), false), //$NON-NLS-1$
	XPage(Paths.get("XPages"), false), //$NON-NLS-1$
	Form("form", Paths.get("Forms"), false), //$NON-NLS-1$ //$NON-NLS-2$
	Frameset("frameset", Paths.get("Framesets"), false), //$NON-NLS-1$ //$NON-NLS-2$
	ServerJavaScriptLibrary(Paths.get("Code", "ScriptLibraries"), false), //$NON-NLS-1$ //$NON-NLS-2$
	Subform("subform", Paths.get("SharedElements", "Subforms"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Page("page", Paths.get("Pages"), false), //$NON-NLS-1$ //$NON-NLS-2$
	AboutDocument(Paths.get("Resources", "AboutDocument"), true), //$NON-NLS-1$ //$NON-NLS-2$
	FileResource(Paths.get("Resources", "Files"), false), //$NON-NLS-1$ //$NON-NLS-2$
	ImageResource(Paths.get("Resources", "Images"), false), //$NON-NLS-1$ //$NON-NLS-2$
	StyleSheet("css", Paths.get("Resources", "Stylesheets"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Theme(Paths.get("Resources", "Themes"), false), //$NON-NLS-1$ //$NON-NLS-2$
	UsingDocument(Paths.get("Resources", "UsingDocument"), true), //$NON-NLS-1$ //$NON-NLS-2$
	SharedField("field", Paths.get("SharedElements", "Fields"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Outline("outline", Paths.get("SharedElements", "Outlines"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	View("view", Paths.get("Views"), false), //$NON-NLS-1$ //$NON-NLS-2$
	JavaAgent("ja", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	ImportedJavaAgent("ija", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptAgent("lsa", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Folder("folder", Paths.get("Folders"), false), //$NON-NLS-1$ //$NON-NLS-2$
	SharedColumn("column", Paths.get("SharedElements", "Columns"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Jar(Paths.get("Code", "Jars"), false), //$NON-NLS-1$ //$NON-NLS-2$
	JavaWebService("jws", Paths.get("Code", "WebServices"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebService("lws", Paths.get("Code", "WebServices"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SharedActions(Paths.get("Code", "actions", "Shared Actions"), true), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SimpleActionAgent("aa", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	FormulaAgent("fa", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Navigator("navigator", Paths.get("SharedElements", "Navigators"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebServiceConsumer("lswsc", Paths.get("Code", "WebServiceConsumer"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaWebServiceConsumer("javalib", Paths.get("Code", "WebServiceConsumer"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	WebContentFile(Paths.get("WebContent"), false), //$NON-NLS-1$
	DataConnection("dcr", Paths.get("Data", "DataConnections"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DB2AccessView("db2v", Paths.get("Data", "DB2AccessViews"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	
	private NoteType(Path path, boolean singleton) {
		this(null, path, singleton);
	}
	private NoteType(String extension, Path path, boolean singleton) {
		this.extension = extension;
		this.path = path;
		this.singleton = singleton;
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
			if(matchesFlagsPattern(flags, DFLAGPAT_FOLDER_DESIGN)) {
				return Folder;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_VIEWMAP_DESIGN)) {
				return Navigator;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SHARED_COLS)) {
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
			
			if(flags.indexOf(DESIGN_FLAG_DATABASESCRIPT) > -1) {
				return DBScript;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SITEMAP)) {
				return Outline;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_LS)) {
				if(flagsExt.indexOf(DESIGN_FLAGEXT_WEBSERVICELIB) > -1) {
					return LotusScriptWebServiceConsumer;
				} else {
					return LotusScriptLibrary; 
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_JAVA)) {
				if(flagsExt.indexOf(DESIGN_FLAGEXT_WEBSERVICELIB) > -1) {
					return JavaWebServiceConsumer;
				} else {
					return JavaLibrary;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_JS)) {
				return JavaScriptLibrary;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_SERVER_JS)) {
				return ServerJavaScriptLibrary;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_JAVA_WEBSERVICE)) {
				return JavaWebService;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_LS_WEBSERVICE)) {
				return LotusScriptWebService;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_DATA_CONNECTION_RESOURCE)) {
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
			
			if(flags.indexOf(DESIGN_FLAG_LOTUSSCRIPT_AGENT) > -1) {
				return LotusScriptAgent;
			} else if(flags.indexOf(DESIGN_FLAG_JAVA_AGENT) > -1) {
				return ImportedJavaAgent;
			} else if(flags.indexOf(DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE) > -1) {
				return JavaAgent;
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
			} else if(matchesFlagsPattern(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				if(IMAGE_NEW_DBICON_NAME.equals(title)) {
					return DBIcon;
				}
				return ImageResource;
			} else if(flags.indexOf(DESIGN_FLAG_JARFILE) > -1) {
				return Jar;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_XSPPAGE_NOPROPS)) {
				// TODO figure out XPages properties files
				return XPage;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_XSPCC)) {
				return CustomControl;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_STYLEKIT)) {
				return Theme;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_WEBPAGE)) {
				return Page;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				return ImageResource;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_STYLE_SHEET_RESOURCE)) {
				return StyleSheet;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SUBFORM_DESIGN)) {
				return Subform;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_FRAMESET)) {
				return Frameset;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_DB2ACCESSVIEW)) {
				return DB2AccessView;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_FILE)) {
				if(flagsExt.indexOf(DESIGN_FLAGEXT_WEBCONTENTFILE) > -1) {
					return WebContentFile;
				} else {
					return GenericFile;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SACTIONS_DESIGN)) {
				return SharedActions;
			} else {
				return Form;
			} 
		}
		
		return Unknown;
	}
	
	/**
	 * @param flags a design flag value to test
	 * @param pattern a flag pattern to test against (DFLAGPAT_*)
	 * @return whether the flags match the pattern
	 */
	private static boolean matchesFlagsPattern(String flags, String pattern) {
		if(StringUtil.isEmpty(pattern)) {
			return false;
		}
		
		String toTest = flags == null ? StringUtil.EMPTY_STRING : flags;
		
		// Patterns start with one of four characters:
		// "+" (match any)
		// "-" (match none)
		// "*" (match all)
		// "(" (multi-part test)
		String matchers = null;
		String antiMatchers = null;
		String allMatchers = null;
		char first = pattern.charAt(0);
		switch(first) {
		case '+':
			matchers = pattern.substring(1);
			antiMatchers = StringUtil.EMPTY_STRING;
			allMatchers = StringUtil.EMPTY_STRING;
			break;
		case '-':
			matchers = StringUtil.EMPTY_STRING;
			antiMatchers = pattern.substring(1);
			allMatchers = StringUtil.EMPTY_STRING;
			break;
		case '*':
			matchers = StringUtil.EMPTY_STRING;
			antiMatchers = StringUtil.EMPTY_STRING;
			allMatchers = pattern.substring(1);
		case '(':
			// The order is always +-*
			int plusIndex = pattern.indexOf('+');
			int minusIndex = pattern.indexOf('-');
			int starIndex = pattern.indexOf('*');
			
			matchers = pattern.substring(plusIndex+1, minusIndex == -1 ? pattern.length() : minusIndex);
			antiMatchers = minusIndex == -1 ? StringUtil.EMPTY_STRING : pattern.substring(minusIndex+1, starIndex == -1 ? pattern.length() : starIndex);
			allMatchers = starIndex == -1 ? StringUtil.EMPTY_STRING : pattern.substring(starIndex+1);
			break;
		}
		if(matchers == null) { matchers = StringUtil.EMPTY_STRING; }
		if(antiMatchers == null) { antiMatchers = StringUtil.EMPTY_STRING; }
		if(allMatchers == null) { allMatchers = StringUtil.EMPTY_STRING; }
		
		// Test "match against any" and fail if it doesn't
		boolean matchedAny = matchers.isEmpty();
		for(int i = 0; i < matchers.length(); i++) {
			if(toTest.indexOf(matchers.charAt(i)) > -1) {
				matchedAny = true;
				break;
			}
		}
		if(!matchedAny) {
			return false;
		}
		
		// Test "match none" and fail if it does
		for(int i = 0; i < antiMatchers.length(); i++) {
			if(toTest.indexOf(antiMatchers.charAt(i)) > -1) {
				// Exit immediately
				return false;
			}
		}
		
		// Test "match all" and fail if it doesn't
		for(int i = 0; i < allMatchers.length(); i++) {
			if(toTest.indexOf(allMatchers.charAt(i)) == -1) {
				// Exit immediately
				return false;
			}
		}
		
		// If we survived to here, it must match
		return true;
	}
}
