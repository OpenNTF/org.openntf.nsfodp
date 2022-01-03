/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.nsfodp.commons.odp.util;

import static org.openntf.nsfodp.commons.NSFODPUtil.matchesFlagsPattern;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_ACL;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_ICON;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_NONPRIV;
import static org.openntf.nsfodp.commons.h.StdNames.ASSIST_TYPE_ITEM;
import static org.openntf.nsfodp.commons.h.StdNames.ASSIST_TYPE_JAVA;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGEXT_WEBCONTENTFILE;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGEXT_WEBSERVICELIB;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGS_EXTENDED;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_DATABASESCRIPT;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_HIDEFROMDESIGNLIST;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JARFILE;
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
import static org.openntf.nsfodp.commons.h.StdNames.IMAGE_NEW_DBICON_NAME;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_JAVA_COMPILER_SOURCE;

import org.openntf.nsfodp.commons.NoteType;
import org.openntf.nsfodp.commons.h.NsfNote;
import org.openntf.nsfodp.commons.h.StdNames;
import org.openntf.nsfodp.commons.odp.notesapi.NNote;

import com.ibm.commons.util.StringUtil;

/**
 * Utilities for working with {@link NoteType} values.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum NoteTypeUtil {
	;
	
	public static NoteType forNote(NNote note) {
		String flags = note.hasItem(StdNames.DESIGN_FLAGS) ? note.getAsString(StdNames.DESIGN_FLAGS, ' ') : StringUtil.EMPTY_STRING;
		String title = getTitle(note);
		String flagsExt = note.hasItem(DESIGN_FLAGS_EXTENDED) ? note.getAsString(DESIGN_FLAGS_EXTENDED, ' ') : StringUtil.EMPTY_STRING;
		
		if(flags.indexOf('X') > -1) {
			return NoteType.AgentData;
		}
		
		
		switch(note.getNoteClassValue() & NOTE_CLASS_NONPRIV) {
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
	
	public static String getTitle(NNote note) {
		if(note.hasItem(StdNames.FIELD_TITLE)) {
			String[] titles = note.get(StdNames.FIELD_TITLE, String[].class);
			if(titles != null && titles.length > 0) {
				return StringUtil.toString(titles[0]);
			}
		}
		return StringUtil.EMPTY_STRING;
	}
}
