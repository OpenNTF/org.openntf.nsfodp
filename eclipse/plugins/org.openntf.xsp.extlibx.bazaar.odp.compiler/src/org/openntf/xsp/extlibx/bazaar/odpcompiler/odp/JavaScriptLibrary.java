package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.io.IOException;
import java.nio.file.Path;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.CompositeDataUtil;

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
	
	@Override
	public byte[] getCompositeData() throws IOException {
		return CompositeDataUtil.getJavaScriptLibraryData(getDataFile());
	}
}
