package org.openntf.nsfodp.exporter;

import static org.openntf.nsfodp.commons.h.StdNames.*;

import org.openntf.nsfodp.commons.h.StdNames;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesNoteItem;

public enum NoteType {
	/** Covers $DBIcon, database.properties, and IconNote */
	Icon,
	XSPDesign, DBScript, Java, JavaScriptLibrary, LotusScriptLibrary, JavaLibrary,
	CustomControl, XPage, Form, Frameset, ServerJavaScriptLibrary, Subform,
	/** e.g. WebContent contents, plugin.xml, etc. */
	GenericFile,
	Page, AboutDocument, FileResource, ImageResource, StyleSheet, Theme, UsingDocument,
	SharedFields, Outline, View, JavaAgent, ImportedJavaAgent, LotusScriptAgent, Folder,
	SharedColumn, Jar, JavaWebService, LotusScriptWebService, SharedActions, SimpleActionAgent,
	FormulaAgent,
	Unknown;
	
	public static NoteType forNote(NotesNote note) throws NotesAPIException {
		// TODO see if there's a better way to identify the DB icon in the legacy API (or move to NAPI/JNA)
		// Alternatively, handle these special ones via their constant note ID variants
		if(note.isItemPresent("IconBitmap")) { //$NON-NLS-1$
			return Icon;
		}
		
		String title = note.getItemValueAsString(FIELD_TITLE);
		String flags = note.getItemValueAsString(DESIGN_FLAGS);
		
		if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_LS)) {
			return LotusScriptLibrary;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_JAVA)) {
			return JavaLibrary;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_JS)) {
			return JavaScriptLibrary;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_SERVER_JS)) {
			return ServerJavaScriptLibrary;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_DATABASESCRIPT)) {
			return DBScript;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_SUBFORM_DESIGN)) {
			return Subform;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_VIEW_DESIGN)) {
			return View;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_FOLDER_DESIGN)) {
			return Folder;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_SHARED_COLS)) {
			return SharedColumn;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_FRAMESET)) {
			return Frameset;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_IMAGE_RESOURCE)) {
			return ImageResource;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_XSPPAGE_NOPROPS)) {
			// TODO figure out XPages properties files
			return XPage;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_XSPCC)) {
			return CustomControl;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_DATA_CONNECTION_RESOURCE)) {
			// TODO Figure out where this goes
		} else if(matchesFlagsPattern(flags, DFLAGPAT_DB2ACCESSVIEW)) {
			// TODO Figure out where this goes
		} else if(matchesFlagsPattern(flags, DFLAGPAT_STYLE_SHEET_RESOURCE)) {
			return StyleSheet;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_STYLEKIT)) {
			return Theme;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_WEBPAGE)) {
			return Page;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_WIDGET)) {
			// TODO Figure out where this goes
		} else if(matchesFlagsPattern(flags, DFLAGPAT_SERVLET)) {
			// ???
		} else if(matchesFlagsPattern(flags, DFLAGPAT_HTMLFILES)) {
			// ???
		} else if(matchesFlagsPattern(flags, DFLAGPAT_FILE)) {
			// ???
		} else if(matchesFlagsPattern(flags, DFLAGPAT_SACTIONS_DESIGN)) {
			return SharedActions;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_JAVA_WEBSERVICE)) {
			return JavaWebService;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_LS_WEBSERVICE)) {
			return LotusScriptWebService;
		} else if(matchesFlagsPattern(flags, DFLAGPAT_AGENTSLIST)) {
			// Determine from here what kind of agent it is
			
			int assistType = 0;
			if(note.isItemPresent(StdNames.ASSIST_TYPE_ITEM)) {
				NotesNoteItem item = note.getItem(StdNames.ASSIST_TYPE_ITEM);
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
		} else if(matchesFlagsPattern(flags, DFLAGPAT_FORM)) {
			return Form;
		}
		
		if(flags.indexOf(DESIGN_FLAG_DATABASESCRIPT) > -1) {
			return DBScript;
		} else if(flags.indexOf(DESIGN_FLAG_FILE) > -1) {
			// Could be a file resource or an XPage-special type of file
			if(flags.indexOf(DESIGN_FLAG_HIDEFROMDESIGNLIST) > -1) {
				// XSP-style element
				
				// There are some further special cases here
				if("WEB-INF/xsp.properties".equals(title)) { //$NON-NLS-1$
					return XSPDesign;
				}
				
				return GenericFile;
			} else {
				return FileResource;
			}
		} else if(flags.indexOf(DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE) > -1) {
			return JavaAgent;
		} else if(flags.indexOf(DESIGN_FLAG_JAVA_AGENT) > -1) {
			return ImportedJavaAgent;
		} else if(flags.indexOf(DESIGN_FLAG_LOTUSSCRIPT_AGENT) > -1) {
			return LotusScriptAgent;
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
