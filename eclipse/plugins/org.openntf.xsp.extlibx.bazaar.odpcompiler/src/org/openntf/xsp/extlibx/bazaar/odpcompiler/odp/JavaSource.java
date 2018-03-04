package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Path;

/**
 * This class represents a Java source file and its accompanying
 * DXL metadata file.
 * 
 * @author Jesse Gallagher
 *
 */
public class JavaSource extends AbstractSplitDesignElement {
	public JavaSource(Path javaSourceFile) {
		super(javaSourceFile);
	}
}
