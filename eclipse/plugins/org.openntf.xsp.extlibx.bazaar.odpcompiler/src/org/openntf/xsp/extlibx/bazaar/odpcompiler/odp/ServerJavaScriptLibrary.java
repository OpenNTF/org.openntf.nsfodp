package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

public class ServerJavaScriptLibrary extends JavaScriptLibrary {

	public ServerJavaScriptLibrary(Path dataFile) {
		super(dataFile);
	}

	@Override
	public String getFileDataItem() {
		return "$ServerJavaScriptLibrary";
	}
}
