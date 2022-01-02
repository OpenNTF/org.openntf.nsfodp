/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.nsfodp.compiler.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.odp.notesapi.NDXLImporter;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.xml.NSFODPDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.util.StringUtil;

/**
 * Utilities for manipulating "raw"-type DXL documents.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum CompilerUtil {
	;
	
	/**
	 * Imports a generic file resource, such as an outer class file from a multi-class Java resource.
	 */
	public static void importFileResource(NDXLImporter importer, byte[] data, NDatabase database, String name, String flags, String flagsExt) throws IOException {
		Document dxlDoc = NSFODPDomUtil.createDocument();
		Element note = NSFODPDomUtil.createElement(dxlDoc, "note"); //$NON-NLS-1$
		note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
		note.setAttribute("xmlns", "http://www.lotus.com/dxl"); //$NON-NLS-1$ //$NON-NLS-2$
		DXLUtil.writeItemString(dxlDoc, "$Flags", false, flags); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(flagsExt)) {
			DXLUtil.writeItemString(dxlDoc, "$FlagsExt", false, flagsExt);	 //$NON-NLS-1$
		}
		DXLUtil.writeItemString(dxlDoc, "$TITLE", false, name); //$NON-NLS-1$
		DXLUtil.writeItemNumber(dxlDoc, "$FileSize", data.length); //$NON-NLS-1$
		DXLUtil.writeItemFileData(dxlDoc, "$FileData", data); //$NON-NLS-1$
		DXLUtil.writeItemString(dxlDoc, "$FileNames", false, name); //$NON-NLS-1$
		String dxl = NSFODPDomUtil.getXmlString(dxlDoc, null);
		try(InputStream is = new ByteArrayInputStream(dxl.getBytes(StandardCharsets.UTF_8))) {
			importer.importDxl(database, is);
		}
	}
}
