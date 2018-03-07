package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

public class JavaScriptLibrary extends AbstractSourceDesignElement {

	public JavaScriptLibrary(Path dataFile) {
		super(dataFile);
	}

	@Override
	public String getFileDataItem() {
		return "$JavaScriptLibrary";
	}
	
	@Override
	public String getFileSizeItem() {
		return null;
	}
}
