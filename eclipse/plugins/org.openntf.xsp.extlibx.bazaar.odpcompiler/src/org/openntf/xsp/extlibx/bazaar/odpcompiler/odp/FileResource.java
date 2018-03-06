package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

/**
 * Represents a "file resource"-type element in the ODP, which may be a file resource,
 * stylesheet, or other loose file.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class FileResource extends AbstractSplitDesignElement {
	public FileResource(Path dataFile) {
		super(dataFile);
	}
}
