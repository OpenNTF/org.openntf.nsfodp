/**
 * Copyright © 2018-2023 Jesse Gallagher
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
import java.nio.file.Path;

import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.dxl.ODSConstants;
import org.openntf.nsfodp.commons.h.Ods;
import org.openntf.nsfodp.commons.odp.util.DXLNativeUtil;
import org.w3c.dom.Document;

public class JavaScriptLibrary extends AbstractSourceDesignElement {

	public JavaScriptLibrary(Path dataFile) {
		super(dataFile);
	}

	@Override
	public String getFileDataItem() {
		return "$JavaScriptLibrary"; //$NON-NLS-1$
	}
	
	@Override
	public String getFileSizeItem() {
		return null;
	}
	
	@Override
	public byte[] getCompositeData() throws IOException {
		return DXLNativeUtil.getJavaScriptLibraryData(getDataFile());
	}
	
	@Override
	protected Document attachFileData(Document dxlDoc) throws IOException {
		byte[] data = getCompositeData();
		String itemName = getFileDataItem();
		
		DXLUtil.writeItemDataRaw(dxlDoc, itemName, data, ODSConstants.PER_BLOB_ITEM_DATA_CAP, Ods.SIZE_CDEVENT);
		
		return dxlDoc;
	}
}
