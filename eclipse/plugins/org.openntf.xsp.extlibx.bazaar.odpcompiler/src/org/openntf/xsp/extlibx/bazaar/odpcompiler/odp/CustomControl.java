package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

/**
 * This class represents the files that make up a custom control: the XSP
 * source, the xsp-config file, and the DXL metadata.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class CustomControl extends XPage {
	public static final String EXT_XSPCONFIG = ".xsp-config";
	
	private final Path xspConfigFile;
	
	public CustomControl(Path xspSourceFile) {
		super(xspSourceFile);
		this.xspConfigFile = xspSourceFile.getParent().resolve(xspSourceFile.getFileName()+EXT_XSPCONFIG);
	}
	
	public Path getXspConfigFile() {
		return xspConfigFile;
	}
	
	public String getControlName() {
		String fileName = this.getDataFile().getFileName().toString();
		return fileName.substring(0, fileName.length()-EXT_XSP.length());
	}
}
