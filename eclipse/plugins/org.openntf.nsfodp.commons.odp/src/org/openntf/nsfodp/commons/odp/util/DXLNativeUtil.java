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
package org.openntf.nsfodp.commons.odp.util;

import static org.openntf.nsfodp.commons.dxl.ODSConstants.*;
import static org.openntf.nsfodp.commons.h.Ods.ACTION_TYPE_JAVASCRIPT;
import static org.openntf.nsfodp.commons.h.Ods.HTML_EVENT_LIBRARY;
import static org.openntf.nsfodp.commons.h.Ods.SIG_CDBLOBPART;
import static org.openntf.nsfodp.commons.h.Ods.SIG_CDEVENT;
import static org.openntf.nsfodp.commons.h.Ods.SIZE_CDBLOBPART;
import static org.openntf.nsfodp.commons.h.Ods.SIZE_CDEVENT;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.domino.napi.c.C;
import com.ibm.domino.napi.c.NotesUtil;

import lotus.domino.Database;
import lotus.domino.DxlImporter;
import lotus.domino.NotesException;

/**
 * Utilities for manipulating "raw"-type DXL documents.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum DXLNativeUtil {
	;
	
	/**
	 * Imports a generic file resource, such as an outer class file from a multi-class Java resource.
	 */
	public static void importFileResource(DxlImporter importer, byte[] data, Database database, String name, String flags, String flagsExt) throws XMLException, IOException, NotesException {
		Document dxlDoc = DOMUtil.createDocument();
		Element note = DOMUtil.createElement(dxlDoc, "note"); //$NON-NLS-1$
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
		String dxl = DOMUtil.getXMLString(dxlDoc);
		importer.importDxl(dxl, database);
	}

	public static byte[] getJavaScriptLibraryData(Path file) throws IOException {
	
		// Read in the file data as an LMBCS string first
		long lmbcsPtr;
		try(InputStream is = Files.newInputStream(file)) {
			String fileContent = StreamUtil.readString(is);
			lmbcsPtr = NotesUtil.toLMBCS(fileContent);
		}
		if(lmbcsPtr == 0) {
			return new byte[0];
		}
		
		try {
			int fileLength = C.strlen(lmbcsPtr, 0);
			
			// Spec out the structure
			int segCount = fileLength / BLOBPART_SIZE_CAP;
			if (fileLength % BLOBPART_SIZE_CAP > 0) {
				segCount++;
			}
	
			int paddedLength = fileLength + 1; // Make sure there's at least one \0 at the end
			int totalSize = SIZE_CDEVENT + (SIZE_CDBLOBPART * segCount) + paddedLength + (paddedLength % 2);
			
			
			// Now create a CD record for the file data
			// TODO this could be a little more efficient by writing to a Base64 wrapper and
			//   cutting off and making a new item node at size intervals
			ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN) ;
			// CDEVENT
			{
				buf.putShort(SIG_CDEVENT);                     // Header.Signature
				buf.putShort(SIZE_CDEVENT);                    // Header.Length
				buf.putInt(0);                                 // Flags
				buf.putShort(HTML_EVENT_LIBRARY);              // EventType
				buf.putShort(ACTION_TYPE_JAVASCRIPT);          // ActionType
				buf.putInt(paddedLength + (paddedLength % 2)); // ActionLength
				buf.putShort((short)0);                        // SignatureLength
				buf.put(new byte[14]);                         // Reserved
			}
			for(int i = 0; i < segCount; i++) {
				// Each chunk begins with a CDBLOBPART
	
				// Figure out our data and segment sizes
				int dataOffset = BLOBPART_SIZE_CAP * i;
				short dataSize = (short)Math.min((paddedLength - dataOffset), BLOBPART_SIZE_CAP);
				short segSize = (short)(dataSize + (dataSize % 2));
	
				// CDBLOBPART
				{
					buf.putShort(SIG_CDBLOBPART);                     // Header.Signature
					buf.putShort((short)(segSize + SIZE_CDBLOBPART)); // Header.Length
					buf.putShort(SIG_CDEVENT);                        // OwnerSig
					buf.putShort((short)segSize);                     // Length
					buf.putShort((short)BLOBPART_SIZE_CAP);           // BlobMax
					buf.put(new byte[8]);                             // Reserved
					
					byte[] segData = new byte[dataSize];
					C.readByteArray(segData, 0, lmbcsPtr, dataOffset, dataSize);
					buf.put(segData);
					if(segSize > dataSize) {
						buf.put(new byte[segSize-dataSize]);
					}
				}
			}
			return buf.array();
		} finally {
			C.free(lmbcsPtr);
		}
	}
}
