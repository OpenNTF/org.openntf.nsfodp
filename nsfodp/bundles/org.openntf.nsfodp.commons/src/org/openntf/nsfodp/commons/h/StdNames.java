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
package org.openntf.nsfodp.commons.h;

/**
 * Useful constants and derivatives from stdnames.h.
 * 
 * @since 1.4.0
 */
public interface StdNames {
	String SCRIPTLIB_ITEM_NAME = "$ScriptLib"; //$NON-NLS-1$
	String SCRIPTLIB_OBJECT = "$ScriptLib_O"; //$NON-NLS-1$
	String JAVASCRIPTLIBRARY_CODE = "$JavaScriptLibrary"; //$NON-NLS-1$
	String SERVER_JAVASCRIPTLIBRARY_CODE = "$ServerJavaScriptLibrary"; //$NON-NLS-1$
	/**	Type of assistant - related to action type */
	String ASSIST_TYPE_ITEM = "$AssistType"; //$NON-NLS-1$
	String IMAGE_NEW_DBICON_NAME = "$DBIcon"; //$NON-NLS-1$
	String XSP_CLASS_INDEX = "$ClassIndexItem"; //$NON-NLS-1$
	
	char DESIGN_FLAG_JARFILE = ','; // Not actually documented
	
	// These types are not actually documented, and possibly not true
	int ASSIST_TYPE_JAVA = 0xFF93;
	int ASSIST_TYPE_FORMULA = 0xFF92;
	
	String ITEM_NAME_JAVA_COMPILER_SOURCE = "$JavaCompilerSource"; // Not actually documented //$NON-NLS-1$
	
	String FIELD_TITLE = "$TITLE"; //$NON-NLS-1$
	String DESIGN_FLAGS = "$Flags"; //$NON-NLS-1$
	String FILTER_COMMENT_ITEM = "$Comment"; //$NON-NLS-1$
	String DESIGN_FLAGS_EXTENDED = "$FlagsExt"; //$NON-NLS-1$
	String ITEM_NAME_FILE_NAMES = "$FileNames"; //$NON-NLS-1$
	String ITEM_NAME_FILE_MIMETYPE = "$MimeType"; //$NON-NLS-1$
	String ITEM_NAME_FILE_MIMECHARSET = "$MimeCharSet"; //$NON-NLS-1$
	String ITEM_NAME_FILE_MODINFO = "$FileModDT"; //$NON-NLS-1$
	String ITEM_NAME_FILE_DATA = "$FileData"; //$NON-NLS-1$
	String ITEM_NAME_FILE_SIZE = "$FileSize"; //$NON-NLS-1$
	
	String ITEM_NAME_IMAGE_DATA = "$ImageData"; //$NON-NLS-1$
	String ITEM_NAME_STYLE_SHEET_DATA = "$StyleSheetData"; //$NON-NLS-1$
	
	String ITEM_NAME_IMAGE_NAMES = "$ImageNames"; //$NON-NLS-1$
	String ITEM_NAME_IMAGES_WIDE = "$ImagesWide"; //$NON-NLS-1$
	String ITEM_NAME_IMAGES_HIGH = "$ImagesHigh"; //$NON-NLS-1$
	String ITEM_NAME_IMAGES_COLORIZE = "$ImagesColorize"; //$NON-NLS-1$
	
	char DESIGN_FLAGEXT_WEBCONTENTFILE = 'w';
	char DESIGN_FLAGEXT_WEBSERVICELIB = 'W';
	char DESIGN_FLAG_DATABASESCRIPT = 't';
	char DESIGN_FLAG_HIDEFROMDESIGNLIST = '~';
	char DESIGN_FLAG_JAVA_AGENT = 'J';
	char DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE = 'j';
	char DESIGN_FLAG_JAVA_RESOURCE = '@';
	char DESIGN_FLAG_LOTUSSCRIPT_AGENT = 'L';
	char DESIGN_FLAG_PROPFILE = '2';
	
	String DFLAGPAT_COMPDEF = "+:"; //$NON-NLS-1$
	String DFLAGPAT_COMPAPP = "+|"; //$NON-NLS-1$
	String DFLAGPAT_DATA_CONNECTION_RESOURCE = "+k"; //$NON-NLS-1$
	String DFLAGPAT_DB2ACCESSVIEW = "+z"; //$NON-NLS-1$
	String DFLAGPAT_FILE = "+g-K[];`,"; //$NON-NLS-1$
	String DFLAGPAT_FOLDER_DESIGN = "(+-04*F"; //$NON-NLS-1$
	String DFLAGPAT_FRAMESET = "(+-*#"; //$NON-NLS-1$
	String DFLAGPAT_IMAGE_RESOURCE = "+i"; //$NON-NLS-1$
	String DFLAGPAT_JAVAFILE = "(+-*g["; //$NON-NLS-1$
	String DFLAGPAT_JAVA_WEBSERVICE = "(+Jj-*{"; //$NON-NLS-1$
	String DFLAGPAT_LS_WEBSERVICE = "*{L"; //$NON-NLS-1$
	String DFLAGPAT_SACTIONS_DESIGN = "+y"; //$NON-NLS-1$
	String DFLAGPAT_SCRIPTLIB_JAVA = "*sj"; //$NON-NLS-1$
	String DFLAGPAT_SCRIPTLIB_JS = "+h"; //$NON-NLS-1$
	String DFLAGPAT_SCRIPTLIB_SERVER_JS = "+."; //$NON-NLS-1$
	String DFLAGPAT_SCRIPTLIB_LS = "(+s-jh.*"; //$NON-NLS-1$
	String DFLAGPAT_SHARED_COLS = "(+-*^"; //$NON-NLS-1$
	String DFLAGPAT_SITEMAP = "+m"; //$NON-NLS-1$
	String DFLAGPAT_STYLEKIT = "(+-*g`"; //$NON-NLS-1$
	String DFLAGPAT_STYLE_SHEET_RESOURCE = "+="; //$NON-NLS-1$
	String DFLAGPAT_SUBFORM_DESIGN = "(+U-40*"; //$NON-NLS-1$
	String DFLAGPAT_VIEWMAP_DESIGN = "(+-04*G"; //$NON-NLS-1$
	String DFLAGPAT_WEBPAGE = "(+-*W"; //$NON-NLS-1$
	String DFLAGPAT_WIDGET = "(+-*g_"; //$NON-NLS-1$
	String DFLAGPAT_XSPCC = "*g;"; //$NON-NLS-1$
	String DFLAGPAT_XSPPAGE = "*gK"; //$NON-NLS-1$
	
	// Not in official API, but in NAPI NotesConstants
	String ITEM_NAME_CONFIG_FILE_DATA = "$ConfigData"; //$NON-NLS-1$
	String ITEM_NAME_CONFIG_FILE_SIZE = "$ConfigSize"; //$NON-NLS-1$
}
