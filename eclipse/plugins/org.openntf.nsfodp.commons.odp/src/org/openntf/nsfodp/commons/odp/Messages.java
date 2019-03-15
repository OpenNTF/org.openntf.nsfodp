package org.openntf.nsfodp.commons.odp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.commons.odp.messages"; //$NON-NLS-1$
	public static String AbstractSplitDesignElement_cannotLocateDxl;
	public static String AbstractSplitDesignElement_cannotReadFile;
	public static String FileResource_noNameProvider;
	public static String ODPUtil_bundleInInstalledState;
	public static String ODPUtil_cannotInferClassName;
	public static String OnDiskProject_classpathNotAFile;
	public static String OnDiskProject_dbPropertiesDoesNotExist;
	public static String OnDiskProject_dbPropertiesNotAFile;
	public static String OnDiskProject_pluginDoesNotExist;
	public static String OnDiskProject_pluginNotAFile;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
