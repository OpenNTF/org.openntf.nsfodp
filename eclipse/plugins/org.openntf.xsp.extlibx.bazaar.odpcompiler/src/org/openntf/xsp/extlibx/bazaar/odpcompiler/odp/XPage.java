package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

/**
 * This class represents an XPage source file and its accompanying
 * DXL metadata file.
 * 
 * @author Jesse Gallagher
 *
 */
public class XPage extends AbstractSplitDesignElement {
	public static final String EXT_XSP = ".xsp";
	public static final String PACKAGE_XSP = "xsp";
	
	public XPage(Path xspSourceFile) {
		super(xspSourceFile);
	}
	
	public String getJavaClassName() {
		String pageName = getPageBaseName();
		String capitalized = pageName.substring(0, 1).toUpperCase() + pageName.substring(1);
		return PACKAGE_XSP + '.' + capitalized;
	}
	
	public String getPageName() {
		return getDataFile().getFileName().toString();
	}
	
	public String getPageBaseName() {
		String fileName = getPageName();
		return fileName.substring(0, fileName.length()-EXT_XSP.length());
	}
}

