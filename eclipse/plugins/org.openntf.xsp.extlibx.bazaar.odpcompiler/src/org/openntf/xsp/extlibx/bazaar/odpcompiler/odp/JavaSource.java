package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

/**
 * This class represents a Java source file and its accompanying
 * DXL metadata file.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class JavaSource extends AbstractSourceDesignElement {
	public JavaSource(Path javaSourceFile) {
		super(javaSourceFile);
	}
}
