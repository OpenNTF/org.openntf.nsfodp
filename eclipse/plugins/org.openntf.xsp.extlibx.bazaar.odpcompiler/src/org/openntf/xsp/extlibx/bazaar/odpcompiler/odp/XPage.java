package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

/**
 * This class represents an XPage source file and its accompanying
 * DXL metadata file.
 * 
 * @author Jesse Gallagher
 *
 */
public class XPage extends AbstractSourceDesignElement {
	public static final String EXT_XSP = ".xsp";
	public static final String PACKAGE_XSP = "xsp";
	
	public XPage(Path xspSourceFile) {
		super(xspSourceFile);
	}
	
	public String getJavaClassName() {
		return PACKAGE_XSP + '.' + getJavaClassSimpleName();
	}
	
	public String getJavaClassSimpleName() {
		String pageName = getPageBaseName();
		String capitalized = pageName.substring(0, 1).toUpperCase() + pageName.substring(1);
		
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < capitalized.length(); i++) {
			char c = capitalized.charAt(i);
			if(!(Character.isAlphabetic(c) || Character.isDigit(c))) {
				result.append('_');
				result.append(String.format("%04x", (int)c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}
	
	public String getPageName() {
		return getDataFile().getFileName().toString();
	}
	
	public String getPageBaseName() {
		String fileName = getPageName();
		return fileName.substring(0, fileName.length()-EXT_XSP.length());
	}
}

