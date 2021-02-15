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
package org.openntf.nsfodp.commons.odp.util;

import static org.openntf.nsfodp.commons.dxl.ODSConstants.BLOBPART_SIZE_CAP;
import static org.openntf.nsfodp.commons.h.Ods.ACTION_TYPE_JAVASCRIPT;
import static org.openntf.nsfodp.commons.h.Ods.HTML_EVENT_LIBRARY;
import static org.openntf.nsfodp.commons.h.Ods.SIG_CDBLOBPART;
import static org.openntf.nsfodp.commons.h.Ods.SIG_CDEVENT;
import static org.openntf.nsfodp.commons.h.Ods.SIZE_CDBLOBPART;
import static org.openntf.nsfodp.commons.h.Ods.SIZE_CDEVENT;

import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ibm.commons.util.io.StreamUtil;
import com.ibm.domino.napi.c.C;
import com.ibm.domino.napi.c.NotesUtil;

/**
 * Utilities for manipulating "raw"-type DXL documents.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum DXLNativeUtil {
	;

	public static byte[] getJavaScriptLibraryData(Path file) throws IOException {
	
		// Read in the file data as an LMBCS string first
		long lmbcsPtr;
		String fileContent;
		try(Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			fileContent = StreamUtil.readString(r);
		}
		lmbcsPtr = NotesUtil.toLMBCS(fileContent);
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
