package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

public class ServerJavaScriptLibrary extends AbstractSourceDesignElement {

	public ServerJavaScriptLibrary(Path dataFile) {
		super(dataFile);
	}

	@Override
	public String getFileDataItem() {
		return "$ServerJavaScriptLibrary";
	}
	
	@Override
	public String getFileSizeItem() {
		return null;
	}
}
