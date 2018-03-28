package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.CompositeDataUtil;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.DXLUtil;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODPUtil;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODSConstants;
import org.w3c.dom.Document;

import com.ibm.commons.xml.XMLException;

/**
 * Represents an image resource in the ODP.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class ImageResource extends FileResource {

	public ImageResource(Path dataFile) {
		super(dataFile);
	}
	
	@Override
	public String getFileDataItem() {
		return "$ImageData";
	}
	
	@Override
	public String getFileSizeItem() {
		return null;
	}

	protected Document attachFileData(Document dxlDoc) throws IOException, XMLException {
		byte[] data = getCompositeData();
		String itemName = getFileDataItem();
		
		DXLUtil.writeItemDataRaw(dxlDoc, itemName, data, ODSConstants.PER_IMAGE_ITEM_DATA_CAP, ODSConstants.SIZE_CDIMAGEHEADER + ODSConstants.SIZE_CDGRAPHIC);
		
		return dxlDoc;
	}
	
	public byte[] getCompositeData() throws IOException, XMLException {
		Path file = getDataFile();
		if(!Files.isRegularFile(file)) {
			throw new IllegalArgumentException("Cannot read file " + file);
		}
		Document dxlDoc = ODPUtil.readXml(getDxlFile());
		try(InputStream is = Files.newInputStream(file)) {
			return CompositeDataUtil.getImageResourceData(file, dxlDoc);
		}
	}
}
