package org.openntf.nsfodp.compiler.servlet;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.compiler.servlet.messages"; //$NON-NLS-1$
	public static String ODPCompilerServlet_anonymousDisallowed;
	public static String ODPCompilerServlet_contentMustBeZip;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
