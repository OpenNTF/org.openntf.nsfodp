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
package org.openntf.nsfodp.commons.odp.util;

import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAGEXT_WEBCONTENTFILE;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAGEXT_WEBSERVICELIB;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAGS;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAGS_EXTENDED;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAG_DATABASESCRIPT;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAG_HIDEFROMDESIGNLIST;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAG_JAVA_AGENT;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAG_JAVA_RESOURCE;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAG_LOTUSSCRIPT_AGENT;
import static com.ibm.designer.domino.napi.NotesConstants.DESIGN_FLAG_PROPFILE;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_COMPAPP;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_COMPDEF;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_DATA_CONNECTION_RESOURCE;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_DB2ACCESSVIEW;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_FILE;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_FOLDER_DESIGN;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_FRAMESET;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_IMAGE_RESOURCE;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_JAVAFILE;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_JAVA_WEBSERVICE;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_LS_WEBSERVICE;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_SACTIONS_DESIGN;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_SCRIPTLIB_JAVA;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_SCRIPTLIB_JS;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_SCRIPTLIB_LS;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_SCRIPTLIB_SERVER_JS;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_SHARED_COLS;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_SITEMAP;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_STYLEKIT;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_STYLE_SHEET_RESOURCE;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_SUBFORM_DESIGN;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_VIEWMAP_DESIGN;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_WEBPAGE;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_WIDGET;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_XSPCC;
import static com.ibm.designer.domino.napi.NotesConstants.DFLAGPAT_XSPPAGE;
import static com.ibm.designer.domino.napi.NotesConstants.FIELD_TITLE;
import static com.ibm.designer.domino.napi.NotesConstants.ITEM_NAME_FILE_NAMES;
import static com.ibm.designer.domino.napi.util.NotesUtils.CmemflagTest;
import static com.ibm.designer.domino.napi.util.NotesUtils.CmemflagTestMultiple;
import static org.openntf.nsfodp.commons.h.StdNames.ASSIST_TYPE_ITEM;
import static org.openntf.nsfodp.commons.h.StdNames.ASSIST_TYPE_JAVA;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JARFILE;
import static org.openntf.nsfodp.commons.h.StdNames.IMAGE_NEW_DBICON_NAME;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_JAVA_COMPILER_SOURCE;

import org.openntf.nsfodp.commons.NoteType;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesNoteItem;
import com.ibm.domino.napi.c.NsfNote;

/**
 * Utilities for working with {@link NoteType} values.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum NoteTypeUtil {
	;

	public static NoteType forNote(NotesNote note) throws NotesAPIException {
		String flags = note.isItemPresent(DESIGN_FLAGS) ? note.getItemValueAsString(DESIGN_FLAGS) : StringUtil.EMPTY_STRING;
		String title = note.isItemPresent(FIELD_TITLE) ? note.getItemAsTextList(FIELD_TITLE).get(0) : StringUtil.EMPTY_STRING;
		String flagsExt = note.isItemPresent(DESIGN_FLAGS_EXTENDED) ? note.getItemValueAsString(DESIGN_FLAGS_EXTENDED) : StringUtil.EMPTY_STRING;
		
		switch(note.getNoteClass() & ~NsfNote.NOTE_CLASS_DEFAULT) {
		case NsfNote.NOTE_CLASS_ACL:
			return NoteType.ACL;
		case NsfNote.NOTE_CLASS_DESIGN:
			return NoteType.DesignCollection;
		case NsfNote.NOTE_CLASS_ICON:
			return NoteType.IconNote;
		case NsfNote.NOTE_CLASS_VIEW:
			if(CmemflagTestMultiple(flags, DFLAGPAT_FOLDER_DESIGN)) {
				return NoteType.Folder;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_VIEWMAP_DESIGN)) {
				return NoteType.Navigator;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SHARED_COLS)) {
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
			
			if(CmemflagTest(flags, DESIGN_FLAG_DATABASESCRIPT)) {
				return NoteType.DBScript;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SITEMAP)) {
				return NoteType.Outline;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SCRIPTLIB_LS)) {
				if(CmemflagTest(flagsExt, DESIGN_FLAGEXT_WEBSERVICELIB)) {
					return NoteType.LotusScriptWebServiceConsumer;
				} else {
					return NoteType.LotusScriptLibrary; 
				}
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SCRIPTLIB_JAVA)) {
				if(CmemflagTest(flagsExt, DESIGN_FLAGEXT_WEBSERVICELIB)) {
					return NoteType.JavaWebServiceConsumer;
				} else {
					return NoteType.JavaLibrary;
				}
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SCRIPTLIB_JS)) {
				return NoteType.JavaScriptLibrary;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SCRIPTLIB_SERVER_JS)) {
				return NoteType.ServerJavaScriptLibrary;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_JAVA_WEBSERVICE)) {
				return NoteType.JavaWebService;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_LS_WEBSERVICE)) {
				return NoteType.LotusScriptWebService;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_DATA_CONNECTION_RESOURCE)) {
				return NoteType.DataConnection;
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
				return NoteType.LotusScriptAgent;
			} else if(CmemflagTest(flags, DESIGN_FLAG_JAVA_AGENT) || CmemflagTest(flags, DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE) || assistType == ASSIST_TYPE_JAVA) {
				// There's not a proper pattern for distinguishing between these two, so look for another marker
				if(CmemflagTest(flags, DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE) || note.isItemPresent(ITEM_NAME_JAVA_COMPILER_SOURCE)) {
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
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				if(IMAGE_NEW_DBICON_NAME.equals(title)) {
					return NoteType.DBIcon;
				}
				return NoteType.ImageResource;
			} else if(CmemflagTest(flags, DESIGN_FLAG_JARFILE)) {
				return NoteType.Jar;			
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_COMPDEF)) {
				return NoteType.WiringProperties;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_COMPAPP)) {
				return NoteType.CompositeApplication;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_WIDGET)) {
				return NoteType.CompositeComponent;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_XSPCC)) {
				if(CmemflagTest(flags, DESIGN_FLAG_PROPFILE)) {
					return NoteType.CustomControlProperties;
				} else {
					return NoteType.CustomControl;
				}
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_XSPPAGE)) {
				if(CmemflagTest(flags, DESIGN_FLAG_PROPFILE)) {
					return NoteType.XPageProperties;
				} else {
					return NoteType.XPage;
				}
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_STYLEKIT)) {
				return NoteType.Theme;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_WEBPAGE)) {
				return NoteType.Page;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				return NoteType.ImageResource;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_STYLE_SHEET_RESOURCE)) {
				return NoteType.StyleSheet;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SUBFORM_DESIGN)) {
				return NoteType.Subform;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_FRAMESET)) {
				return NoteType.Frameset;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_DB2ACCESSVIEW)) {
				return NoteType.DB2AccessView;
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_FILE)) {
				// xspdesign.properties needs special handling, but is distinguished only by file name
				String filePath = note.isItemPresent(ITEM_NAME_FILE_NAMES) ? note.getItemAsTextList(ITEM_NAME_FILE_NAMES).get(0) : null;
				
				if(!CmemflagTest(flags, DESIGN_FLAG_HIDEFROMDESIGNLIST)) {
					return NoteType.FileResource;
				} else if("xspdesign.properties".equals(filePath)) { //$NON-NLS-1$
					return NoteType.XSPDesignProperties;
				} else if(CmemflagTest(flagsExt, DESIGN_FLAGEXT_WEBCONTENTFILE)) {
					return NoteType.WebContentFile;
				} else if(CmemflagTestMultiple(flags, DFLAGPAT_JAVAFILE)) {
					return NoteType.Java;
				} else {
					return NoteType.GenericFile;
				}
			} else if(CmemflagTestMultiple(flags, DFLAGPAT_SACTIONS_DESIGN)) {
				return NoteType.SharedActions;
			} else if(CmemflagTest(flags, DESIGN_FLAG_JAVA_RESOURCE)) {
				return NoteType.Applet;
			} else {
				return NoteType.Form;
			} 
		}
		
		return NoteType.Unknown;
	}
}
