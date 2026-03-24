/*
 * Copyright (c) 2018-2025 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.nsfodp.commons.odp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.dxl.ODSConstants;
import org.openntf.nsfodp.commons.h.Ods;
import org.openntf.nsfodp.commons.odp.util.ODPUtil;
import org.openntf.nsfodp.commons.xml.NSFODPDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.commons.util.StringUtil;

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
		return "$ImageData"; //$NON-NLS-1$
	}
	
	@Override
	public String getFileSizeItem() {
		return null;
	}
	
	@Override
	protected Document attachFileData(Document dxlDoc) throws IOException {
		byte[] data = getCompositeData();
		
		List<Node> existingNodes = NSFODPDomUtil.nodes(dxlDoc, "/note/item[@name='" + DXLUtil.escapeXPathValue("$FileSize") + "']"); //$NON-NLS-1$ //$NON-NLS-2$
		if(existingNodes.isEmpty()) {
			long fileSize = getFileSize();
			Element note = DXLUtil.getRootNoteElement(dxlDoc);
			Element item = NSFODPDomUtil.createElement(note, "item"); //$NON-NLS-1$
			item.setAttribute("name", "$FileSize"); //$NON-NLS-1$
			item.setAttribute("sign", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			Element number = NSFODPDomUtil.createElement(item, "number"); //$NON-NLS-1$
			number.setTextContent(StringUtil.toString(fileSize));
		}
		
		List<Node> existingNodes2 = NSFODPDomUtil.nodes(dxlDoc, "/note/item[@name='" + DXLUtil.escapeXPathValue("$DesignerVersion") + "']"); //$NON-NLS-1$ //$NON-NLS-2$
		if(existingNodes2.isEmpty()) {
			Path file = getDataFile();
			if(file.getFileName().toString().toLowerCase().endsWith(".png")) {
				Element note = DXLUtil.getRootNoteElement(dxlDoc);
				Element item = NSFODPDomUtil.createElement(note, "item"); //$NON-NLS-1$
				item.setAttribute("name", "$DesignerVersion"); //$NON-NLS-1$
				Element text = NSFODPDomUtil.createElement(item, "text"); //$NON-NLS-1$
				text.setTextContent("8.5.3"); //$NON-NLS-1$
			}
		}
		
		HashMap<String, String> additionalItemAttributes = new HashMap<>(1);
		additionalItemAttributes.put("sign", "true"); //$NON-NLS-1$ //$NON-NLS-2$

		String itemName = getFileDataItem();
		DXLUtil.writeItemDataRaw(dxlDoc, itemName, data, ODSConstants.PER_IMAGE_ITEM_DATA_CAP, Ods.SIZE_CDIMAGEHEADER + Ods.SIZE_CDGRAPHIC, additionalItemAttributes);
		
		return dxlDoc;
	}

	@Override
	public byte[] getCompositeData() throws IOException {
		Path file = getDataFile();
		if(!Files.isRegularFile(file)) {
			throw new IllegalArgumentException(MessageFormat.format(Messages.AbstractSplitDesignElement_cannotReadFile, file));
		}
		Document dxlDoc = ODPUtil.readXml(getDxlFile());
		try(InputStream is = NSFODPUtil.newInputStream(file)) {
			return DXLUtil.getImageResourceData(file, dxlDoc);
		}
	}
	
	public long getFileSize() throws IOException {
		Path file = getDataFile();
		return Files.size(file);
	}
}
