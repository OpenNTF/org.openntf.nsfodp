package org.openntf.nsfodp.exporter.equinox;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.exporter.equinox.messages"; //$NON-NLS-1$
	public static String ExporterApplication_dbPathCannotBeEmpty;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
