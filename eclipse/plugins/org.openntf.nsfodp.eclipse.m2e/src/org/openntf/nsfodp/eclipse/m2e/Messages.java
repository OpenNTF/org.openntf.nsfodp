package org.openntf.nsfodp.eclipse.m2e;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.eclipse.m2e.messages"; //$NON-NLS-1$
	public static String CompileODPJob_compileOdp;
	public static String CompileODPJob_errorExecutingMaven;
	public static String CompileODPJob_executingPOM;
	public static String DeployNSFJob_errorExecutingMaven;
	public static String DeployNSFJob_executingPom;
	public static String DeployNSFJob_label;
	public static String ODPPDEUtil_errorBuildProperties;
	public static String ODPPDEUtil_updatingClasspath;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
