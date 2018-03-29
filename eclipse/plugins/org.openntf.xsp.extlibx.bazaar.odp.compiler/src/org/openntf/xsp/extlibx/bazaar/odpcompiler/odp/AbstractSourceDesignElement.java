package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODPUtil;

/**
 * This class represents a text-based source file and its accompanying
 * DXL metadata file.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class AbstractSourceDesignElement extends AbstractSplitDesignElement {
	public AbstractSourceDesignElement(Path dataFile) {
		super(dataFile);
	}

	public String getSource() {
		return ODPUtil.readFile(this.getDataFile());
	}
}
