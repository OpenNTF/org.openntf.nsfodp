package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.DXLUtil;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODPUtil;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODSConstants;
import org.w3c.dom.Document;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.XMLException;

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
	
	public String getFileDataItem() {
		return "$FileData";
	}
	public String getFileSizeItem() {
		return "$FileSize";
	}
	
	public Document getDxl() throws XMLException, IOException {
		if(Files.isRegularFile(dxlFile)) {
			return attachFileData(ODPUtil.readXml(dxlFile));
		} else {
			throw new IllegalStateException("Could not locate DXL file for " + dataFile);
		}
	}
	
	protected Document attachFileData(Document dxlDoc) throws IOException, XMLException {
		byte[] data = getCompositeData();
		String itemName = getFileDataItem();
		String sizeItemName = getFileSizeItem();
		
		DXLUtil.writeItemDataRaw(dxlDoc, itemName, data, ODSConstants.PER_FILE_ITEM_DATA_CAP, ODSConstants.SIZE_CDFILEHEADER);
		if(StringUtil.isNotEmpty(sizeItemName)) {
			DXLUtil.writeItemNumber(dxlDoc, sizeItemName, data.length);
		}
		
		return dxlDoc;
	}
	
	public byte[] getCompositeData() throws IOException {
		Path file = getDataFile();
		if(!Files.isRegularFile(file)) {
			throw new IllegalArgumentException("Cannot read file " + file);
		}
		try(InputStream is = Files.newInputStream(file)) {
			return DXLUtil.getFileResourceData(is, (int)Files.size(file));
		}
	}
}
