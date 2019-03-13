package org.openntf.nsfodp.eclipse.contentassist.generators;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.eclipse.contentassist.generators.messages"; //$NON-NLS-1$
	public static String AbstractCompletionProposal_contextString;
	public static String AbstractCompletionProposal_informationString;
	public static String AbstractCompletionProposal_unableToApply;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
