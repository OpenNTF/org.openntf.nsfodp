package org.openntf.nsfodp.compiler;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.compiler.messages"; //$NON-NLS-1$
	public static String ODPCompiler_basicElementLabel;
	public static String ODPCompiler_compilingCustomControls;
	public static String ODPCompiler_compilingJava;
	public static String ODPCompiler_compilingJavaClasses;
	public static String ODPCompiler_compilingLotusScript;
	public static String ODPCompiler_compilingXPages;
	public static String ODPCompiler_couldNotIdentifyTitle;
	public static String ODPCompiler_creatingNSF;
	public static String ODPCompiler_customControlLabel;
	public static String ODPCompiler_dxlImportFailed;
	public static String ODPCompiler_errorConvertingXSP;
	public static String ODPCompiler_importingCustomControls;
	public static String ODPCompiler_importingDbProperties;
	public static String ODPCompiler_importingDesignElements;
	public static String ODPCompiler_importingFileResources;
	public static String ODPCompiler_importingJava;
	public static String ODPCompiler_importingLotusScript;
	public static String ODPCompiler_importingXPages;
	public static String ODPCompiler_initializingLibraries;
	public static String ODPCompiler_installedBundles;
	public static String ODPCompiler_installingBundles;
	public static String ODPCompiler_javaClassLabel;
	public static String ODPCompiler_javaCompilationFailed;
	public static String ODPCompiler_lotusScriptLabel;
	public static String ODPCompiler_unableToCompileLotusScript;
	public static String ODPCompiler_uninstallingBundles;
	public static String ODPCompiler_webServiceNotFound1;
	public static String ODPCompiler_webServiceNotFound2;
	public static String ODPCompiler_XPageLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
