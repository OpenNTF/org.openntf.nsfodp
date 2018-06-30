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
}
