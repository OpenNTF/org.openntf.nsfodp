package org.openntf.nsfodp.exporter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.exporter.messages"; //$NON-NLS-1$
	public static String ODPExporter_nativeExceptionIconNote;
	public static String ODPExporter_nativeExceptionNoteId;
	public static String ODPExporter_unknownNote;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
