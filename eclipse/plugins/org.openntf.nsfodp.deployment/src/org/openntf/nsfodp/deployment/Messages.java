package org.openntf.nsfodp.deployment;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.deployment.messages"; //$NON-NLS-1$
	public static String DeployNSFTask_dbExists;
	public static String DeployNSFTask_destPathNull;
	public static String DeployNSFTask_exceptionDeploying;
	public static String DeployNSFTask_nsfFileNull;
	public static String ReplaceDesignTaskLocal_label;
	public static String ReplaceDesignTaskLocal_targetDbNameNull;
	public static String ReplaceDesignTaskLocal_templatePathNull;
	public static String ReplaceDesignTaskTest_errorReplacingDesign;
	public static String ReplaceDesignTaskTest_label;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
