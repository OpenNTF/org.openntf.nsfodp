package org.openntf.nsfodp.commons;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.commons.messages"; //$NON-NLS-1$
	public static String PrintStreamProgressMonitor_canceled;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
