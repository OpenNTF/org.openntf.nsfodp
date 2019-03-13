package org.openntf.nsfodp.eclipse.contentassist;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.eclipse.contentassist.messages"; //$NON-NLS-1$
	public static String ComponentCache_errorLoadStockComponents;
	public static String ComponentCache_errorStockComponentsInvalid;
	public static String ComponentCache_errorStockComponentsNotObject;
	public static String CustomControlProposalGenerator_errorComposingSuggestions;
	public static String StockComponentsProposalGenerator_errorComposingSuggestions;
	public static String XspCompletionProposalComputer_errorDocumentPosition;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
