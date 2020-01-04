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
package org.openntf.nsfodp.commons.h;

/**
 * Useful constants and derivatives from stdnames.h that aren't found in the NAPI.
 * 
 * @since 1.4.0
 */
public interface StdNames {
	public static final String SCRIPTLIB_ITEM_NAME = "$ScriptLib"; //$NON-NLS-1$
	public static final String SCRIPTLIB_OBJECT = "$ScriptLib_O"; //$NON-NLS-1$
	public static final String JAVASCRIPTLIBRARY_CODE = "$JavaScriptLibrary"; //$NON-NLS-1$
	public static final String SERVER_JAVASCRIPTLIBRARY_CODE = "$ServerJavaScriptLibrary"; //$NON-NLS-1$
	/**	Type of assistant - related to action type */
	public static final String ASSIST_TYPE_ITEM = "$AssistType"; //$NON-NLS-1$
	public static final String IMAGE_NEW_DBICON_NAME = "$DBIcon"; //$NON-NLS-1$
	public static final String XSP_CLASS_INDEX = "$ClassIndexItem"; //$NON-NLS-1$
	
	public static final char DESIGN_FLAG_JARFILE = ','; // Not actually documented
	
	public static final int ASSIST_TYPE_JAVA = 0xFF93; // Not actually documented, and possibly not true
	public static final String ITEM_NAME_JAVA_COMPILER_SOURCE = "$JavaCompilerSource"; // Not actually documented //$NON-NLS-1$
	
	public static final String FIELD_TITLE = "$TITLE"; //$NON-NLS-1$
	public static final String DESIGN_FLAGS = "$Flags"; //$NON-NLS-1$
	public static final String FILTER_COMMENT_ITEM = "$Comment"; //$NON-NLS-1$
	public static final String DESIGN_FLAGS_EXTENDED = "$FlagsExt"; //$NON-NLS-1$
	public static final String ITEM_NAME_FILE_NAMES = "$FileNames"; //$NON-NLS-1$
	public static final String ITEM_NAME_FILE_MIMETYPE = "$MimeType"; //$NON-NLS-1$
	public static final String ITEM_NAME_FILE_MIMECHARSET = "$MimeCharSet"; //$NON-NLS-1$
	public static final String ITEM_NAME_FILE_MODINFO = "$FileModDT"; //$NON-NLS-1$
	
	public static final String ITEM_NAME_IMAGE_NAMES = "$ImageNames"; //$NON-NLS-1$
	public static final String ITEM_NAME_IMAGES_WIDE = "$ImagesWide"; //$NON-NLS-1$
	public static final String ITEM_NAME_IMAGES_HIGH = "$ImagesHigh"; //$NON-NLS-1$
	public static final String ITEM_NAME_IMAGES_COLORIZE = "$ImagesColorize"; //$NON-NLS-1$
}
