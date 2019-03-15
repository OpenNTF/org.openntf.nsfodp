package org.openntf.nsfodp.exporter.servlet;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.exporter.servlet.messages"; //$NON-NLS-1$
	public static String ODPExporterServlet_anonymousAccessDisallowed;
	public static String ODPExporterServlet_dbPathMissing;
	public static String ODPExporterServlet_insufficientAccess;
	public static String ODPExporterServlet_mismatchedContentType;
	public static String ODPExporterServlet_unableToOpenDb;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
