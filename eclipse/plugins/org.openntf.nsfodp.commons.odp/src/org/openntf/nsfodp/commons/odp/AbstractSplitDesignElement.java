/**
 * Copyright © 2018-2019 Jesse Gallagher
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.openntf.nsfodp.commons.h.Ods;
import org.openntf.nsfodp.commons.odp.util.CompositeDataUtil;
import org.openntf.nsfodp.commons.odp.util.DXLUtil;
import org.openntf.nsfodp.commons.odp.util.ODPUtil;
import org.openntf.nsfodp.commons.odp.util.ODSConstants;
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
	public static final String EXT_METADATA = ".metadata"; //$NON-NLS-1$
	
	private final Path dataFile;
	private final Path dxlFile;
	private byte[] overrideData;
	
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
		return "$FileData"; //$NON-NLS-1$
	}
	public String getFileSizeItem() {
		return "$FileSize"; //$NON-NLS-1$
	}
	
	public Document getDxl() throws XMLException, IOException {
		if(Files.isRegularFile(dxlFile)) {
			return attachFileData(ODPUtil.readXml(dxlFile));
		} else {
			throw new IllegalStateException("Could not locate DXL file for " + dataFile);
		}
	}
	
	public void setOverrideData(byte[] overrideData) {
		this.overrideData = overrideData;
	}
	
	protected Document attachFileData(Document dxlDoc) throws IOException, XMLException {
		byte[] data = getCompositeData();
		String itemName = getFileDataItem();
		String sizeItemName = getFileSizeItem();
		
		DXLUtil.writeItemDataRaw(dxlDoc, itemName, data, ODSConstants.PER_FILE_ITEM_DATA_CAP, Ods.SIZE_CDFILEHEADER);
		if(StringUtil.isNotEmpty(sizeItemName)) {
			DXLUtil.writeItemNumber(dxlDoc, sizeItemName, data.length);
		}
		
		return dxlDoc;
	}
	
	public byte[] getCompositeData() throws IOException, XMLException {
		if(this.overrideData != null) {
			try(InputStream is = new ByteArrayInputStream(this.overrideData)) {
				return CompositeDataUtil.getFileResourceData(is, this.overrideData.length);
			}
		} else {
			Path file = getDataFile();
			if(!Files.isRegularFile(file)) {
				throw new IllegalArgumentException("Cannot read file " + file);
			}
			try(InputStream is = Files.newInputStream(file)) {
				return CompositeDataUtil.getFileResourceData(is, (int)Files.size(file));
			}
		}
	}
}
