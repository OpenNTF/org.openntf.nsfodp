package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODPUtil;
import org.w3c.dom.Document;

/**
 * The base class for design elements that are broken up into multiple
 * files in the on-disk project.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class AbstractSplitDesignElement {
	public static final String EXT_METADATA = ".metadata";
	
	private final Path dataFile;
	private final Path dxlFile;
	
	public AbstractSplitDesignElement(Path dataFile) {
		this.dataFile = Objects.requireNonNull(dataFile);
		this.dxlFile = dataFile.getParent().resolve(dataFile.getFileName()+EXT_METADATA);
	}
	
	public Path getDataFile() {
		return dataFile;
	}
	
	public Path getDxlFile() {
		return dxlFile;
	}
	
	public Optional<Document> getDxl() {
		if(Files.isRegularFile(dxlFile)) {
			return Optional.ofNullable(ODPUtil.readXml(dxlFile));
		} else {
			return Optional.empty();
		}
	}
}
