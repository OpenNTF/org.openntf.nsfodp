package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODPUtil;
import org.w3c.dom.Document;

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
		String fileName = xspSourceFile.getFileName().toString();
		String baseName = fileName.substring(0, fileName.length()-EXT_XSP.length());
		this.xspConfigFile = xspSourceFile.getParent().resolve(baseName+EXT_XSPCONFIG);
	}
	
	public Path getXspConfigFile() {
		return xspConfigFile;
	}
	
	public String getXspConfigSource() {
		return ODPUtil.readFile(xspConfigFile);
	}
	
	public Optional<Document> getXspConfig() {
		if(Files.isRegularFile(xspConfigFile)) {
			return Optional.ofNullable(ODPUtil.readXml(xspConfigFile));
		} else {
			return Optional.empty();
		}
	}
	
	public String getControlName() {
		return this.getPageBaseName();
	}
}
