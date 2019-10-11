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
package org.openntf.nsfodp.commons.dxl;

import static org.openntf.nsfodp.commons.dxl.ODSConstants.FILE_SEGMENT_SIZE_CAP;
import static org.openntf.nsfodp.commons.dxl.ODSConstants.IMAGE_SEGMENT_SIZE_CAP;
import static org.openntf.nsfodp.commons.dxl.ODSConstants.PER_FILE_ITEM_DATA_CAP;
import static org.openntf.nsfodp.commons.h.Ods.CDGRAPHIC_VERSION3;
import static org.openntf.nsfodp.commons.h.Ods.SIG_CDFILEHEADER;
import static org.openntf.nsfodp.commons.h.Ods.SIG_CDFILESEGMENT;
import static org.openntf.nsfodp.commons.h.Ods.SIG_CDGRAPHIC;
import static org.openntf.nsfodp.commons.h.Ods.SIG_CDIMAGEHEADER;
import static org.openntf.nsfodp.commons.h.Ods.SIG_CDIMAGESEGMENT;
import static org.openntf.nsfodp.commons.h.Ods.SIZE_CDFILEHEADER;
import static org.openntf.nsfodp.commons.h.Ods.SIZE_CDFILESEGMENT;
import static org.openntf.nsfodp.commons.h.Ods.SIZE_CDGRAPHIC;
import static org.openntf.nsfodp.commons.h.Ods.SIZE_CDIMAGEHEADER;
import static org.openntf.nsfodp.commons.h.Ods.SIZE_CDIMAGESEGMENT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.openntf.nsfodp.commons.h.Ods;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

public enum DXLUtil {
	;
	
	public static final ThreadLocal<DateFormat> DXL_DATETIME_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd'T'HHmmss,00X")); //$NON-NLS-1$

	public static String escapeXPathValue(final String input) {
		return input.replace("'", "\\'"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void deleteItems(Document dxlDoc, String itemName) throws XMLException {
		// Force the side effect of checking for the note root
		getRootNoteElement(dxlDoc);
		
		Object[] existingNodes = DOMUtil.evaluateXPath(dxlDoc, "/note/item[@name='" + escapeXPathValue(itemName) + "']").getNodes(); //$NON-NLS-1$ //$NON-NLS-2$
		for(Object existing : existingNodes) {
			Node node = (Node)existing;
			node.getParentNode().removeChild(node);
		}
	}

	public static List<String> getItemValueStrings(Document dxlDoc, String itemName) throws XMLException {
		List<String> result = new ArrayList<>();

		// Force the side effect of checking for the note root
		getRootNoteElement(dxlDoc);
		
		Object[] nodes = DOMUtil.evaluateXPath(dxlDoc, "/*[name()='note']/*[name()='item'][@name='" + escapeXPathValue(itemName) + "']/*[name()='text']").getNodes(); //$NON-NLS-1$ //$NON-NLS-2$
		for(Object nodeObj : nodes) {
			Node node = (Node)nodeObj;
			result.add(node.getTextContent());
		}
		
		return result;
	}

	public static Element writeItemString(Document dxlDoc, String itemName, boolean removeExisting, CharSequence... value) throws XMLException {
		if(removeExisting) {
			deleteItems(dxlDoc, itemName);
		}
		
		if(value != null) {
			Element note = getRootNoteElement(dxlDoc);
			Element item = DOMUtil.createElement(dxlDoc, note, "item"); //$NON-NLS-1$
			item.setAttribute("name", itemName); //$NON-NLS-1$
			if(value.length > 1) {
				item = DOMUtil.createElement(dxlDoc, item, "textlist"); //$NON-NLS-1$
			}
			for(CharSequence val : value) {
				Element text = DOMUtil.createElement(dxlDoc, item, "text"); //$NON-NLS-1$
				text.setTextContent(val.toString());
			}
			return item;
		} else {
			return null;
		}
	}

	public static void writeItemNumber(Document dxlDoc, String itemName, Number... value) throws XMLException {
		deleteItems(dxlDoc, itemName);
		
		if(value != null) {
			Element note = getRootNoteElement(dxlDoc);
			Element item = DOMUtil.createElement(dxlDoc, note, "item"); //$NON-NLS-1$
			item.setAttribute("name", itemName); //$NON-NLS-1$
			for(Number val : value) {
				Element number = DOMUtil.createElement(dxlDoc, item, "number"); //$NON-NLS-1$
				number.setTextContent(val.toString());
			}
		}
	}

	public static void writeItemDataRaw(Document dxlDoc, String itemName, byte[] data, int itemCap, int headerSize) throws XMLException {
		deleteItems(dxlDoc, itemName);

		Element note = getRootNoteElement(dxlDoc);
		
		int dxlChunks = data.length / itemCap;
		if(data.length % itemCap > 0) {
			dxlChunks++;
		}
		int offset = 0;
		Base64.Encoder base64 = Base64.getEncoder();
		for (int i = 0; i < dxlChunks; i++) {
			int chunkSize = Math.min(data.length-offset, itemCap + (i==0 ? headerSize : 0));
			String chunkData = base64.encodeToString(Arrays.copyOfRange(data, offset, offset + chunkSize));
	
			Element itemNode = DOMUtil.createElement(dxlDoc, note, "item"); //$NON-NLS-1$
			itemNode.setAttribute("name", itemName); //$NON-NLS-1$
			Element fileDataNode = DOMUtil.createElement(dxlDoc, itemNode, "rawitemdata"); //$NON-NLS-1$
			fileDataNode.setAttribute("type", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			// Write out the value with 72-column wrapping
			StringBuilder wrapped = new StringBuilder("\n"); //$NON-NLS-1$
			for(int stringIndex = 0; stringIndex < chunkData.length(); stringIndex += 72) {
				wrapped.append(chunkData.substring(stringIndex, Math.min(stringIndex+72, chunkData.length())));
				wrapped.append('\n');
			}
			fileDataNode.setTextContent(wrapped.toString());
	
			offset += chunkSize;
		}
	}

	public static void writeItemFileData(Document dxlDoc, String itemName, InputStream is, int fileLength) throws XMLException, IOException {
		byte[] data = getFileResourceData(is, fileLength);
		writeItemDataRaw(dxlDoc, itemName, data, PER_FILE_ITEM_DATA_CAP, Ods.SIZE_CDFILEHEADER);
	}

	public static byte[] getFileResourceData(InputStream is, int fileLength) throws IOException {
		// Spec out the structure
		int segCount = fileLength / FILE_SEGMENT_SIZE_CAP;
		if (fileLength % FILE_SEGMENT_SIZE_CAP > 0) {
			segCount++;
		}
		
		int totalSize = SIZE_CDFILEHEADER + (SIZE_CDFILESEGMENT * segCount) + fileLength + (fileLength % 2);
		
		// Now create a CD record for the file data
		// TODO this could be a little more efficient by writing to a Base64 wrapper and
		//   cutting off and making a new item node at size intervals
		ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN) ;
		// CDFILEHEADER
		{
			buf.putShort(SIG_CDFILEHEADER);// Header.Signature
			buf.putInt(SIZE_CDFILEHEADER); // Header.Length
			buf.putShort((short)0);        // FileExtLen
			buf.putInt(fileLength);        // FileDataSize
			buf.putInt(segCount);          // SegCount
			buf.putInt(0);                 // Flags
			buf.putInt(0);                 // Reserved
		}
		for(int i = 0; i < segCount; i++) {
			// Each chunk begins with a CDFILESEGMENT
	
			// Figure out our data and segment sizes
			int dataOffset = FILE_SEGMENT_SIZE_CAP * i;
			short dataSize = (short)Math.min((fileLength - dataOffset), FILE_SEGMENT_SIZE_CAP);
			short segSize = (short)(dataSize + (dataSize % 2));
	
			// CDFILESEGMENT
			{
				buf.putShort(SIG_CDFILESEGMENT);          // Header.Signature
				buf.putInt(segSize + SIZE_CDFILESEGMENT); // Header.Length
				buf.putShort((short)dataSize);            // DataSize
				buf.putShort((short)segSize);             // SegSize
				buf.putInt(0);                            // Flags
				buf.putInt(0);                            // Reserved
				
				byte[] segData = new byte[dataSize];
				is.read(segData);
				buf.put(segData);
				if(segSize > dataSize) {
					buf.put((byte)0);
				}
			}
		}
		return buf.array();
	}

	public static byte[] getImageResourceData(Path file, Document dxlDoc) throws IOException, XMLException {
		int fileLength = (int)Files.size(file);
		// Load image info
		File imageFile = file.toFile();
		int height = 0; // true value not actually stored
		int width = 0; // true value not actually stored
		String mimeType;
		// First, check the DXL file
		mimeType = DOMUtil.evaluateXPath(dxlDoc, "/*[name()='note']/*[name()='item'][@name='$MimeType']/*[name()='text']/text()").getStringValue(); //$NON-NLS-1$
		// Failing that, go by the ImageNames item
		if(StringUtil.isEmpty(mimeType)) {
			String imageNames = StringUtil.toString(DOMUtil.evaluateXPath(dxlDoc, "/*[name()='note']/*[name()='item'][@name='$ImageNames']/*[name()='text']/text()").getStringValue()).toLowerCase(); //$NON-NLS-1$
			if(imageNames.endsWith(".gif")) { //$NON-NLS-1$
				mimeType = "image/gif"; //$NON-NLS-1$
			} else if(imageNames.endsWith(".bmp")) { //$NON-NLS-1$
				mimeType = "image/bmp"; //$NON-NLS-1$
			} else if(imageNames.endsWith(".jpg") || imageNames.endsWith(".jpeg")) { //$NON-NLS-1$ //$NON-NLS-2$
				mimeType = "image/jpeg"; //$NON-NLS-1$
			} else if(imageNames.endsWith(".png")) { //$NON-NLS-1$
				mimeType = "image/png"; //$NON-NLS-1$
			}
		}
		// Finally, try to guess it
		if(StringUtil.isEmpty(mimeType)) {
			mimeType = new MimetypesFileTypeMap().getContentType(imageFile);
		}
		if(mimeType == null) {
			throw new RuntimeException(MessageFormat.format(Messages.getString("DXLUtil.noMimeType"), file)); //$NON-NLS-1$
		}
		short imageType = 0;
		switch(mimeType) {
		case "image/gif": //$NON-NLS-1$
			imageType = 1; // CDIMAGETYPE_GIF
			break;
		case "image/jpeg": //$NON-NLS-1$
		case "image/png": // for some reason //$NON-NLS-1$
			imageType = 2; // CDIMAGETYPE_JPEG
			break;
		case "image/bmp": //$NON-NLS-1$
			imageType = 3; // CDIMAGETYPE_BMP
			break;
		default:
			// Everything else is 0
			break;
		}
		
		// Spec out the structure
		int segCount = fileLength / IMAGE_SEGMENT_SIZE_CAP;
		if (fileLength % IMAGE_SEGMENT_SIZE_CAP > 0) {
			segCount++;
		}
		
		int totalSize = SIZE_CDGRAPHIC + SIZE_CDIMAGEHEADER + (SIZE_CDIMAGESEGMENT * segCount) + fileLength + (fileLength % 2);
		
		// Now create a CD record for the file data
		// TODO this could be a little more efficient by writing to a Base64 wrapper and
		//   cutting off and making a new item node at size intervals
		ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN) ;
		// CDGRAPHIC
		{
			buf.putShort(SIG_CDGRAPHIC); // Header.Signature
			buf.putInt(SIZE_CDGRAPHIC);  // Header.Length
			buf.putShort((short)0);      // DestSize.width
			buf.putShort((short)0);      // DestSize.height
			buf.putShort((short)0);      // CropSize.height
			buf.putShort((short)0);      // CropSize.width
			buf.putShort((short)0);      // CropOffset.left
			buf.putShort((short)0);      // CropOffset.top
			buf.putShort((short)0);      // CropOffset.right
			buf.putShort((short)0);      // CropOffset.bottom
			buf.putShort((short)0);      // fResize
			buf.put(CDGRAPHIC_VERSION3); // Version
			buf.put((byte)0);            // bFlags;
			buf.putShort((short)0);      // wReserved
		}
		// CDIMAGEHEADER
		{
			buf.putShort(SIG_CDIMAGEHEADER);// Header.Signature
			buf.putInt(SIZE_CDIMAGEHEADER); // Header.Length
			buf.putShort(imageType);        // ImageType
			buf.putShort((short)width);     // Width
			buf.putShort((short)height);    // Height
			buf.putInt(fileLength);         // ImageDataSize
			buf.putInt(segCount);           // SegCount
			buf.putInt(0);                  // Flags
			buf.putInt(0);                  // Reserved
		}
		try(InputStream is = Files.newInputStream(file)) {
			for(int i = 0; i < segCount; i++) {
				// Each chunk begins with a CDIMAGESEGMENT
	
				// Figure out our data and segment sizes
				int dataOffset = IMAGE_SEGMENT_SIZE_CAP * i;
				short dataSize = (short)Math.min((fileLength - dataOffset), IMAGE_SEGMENT_SIZE_CAP);
				short segSize = (short)(dataSize + (dataSize % 2));
	
				// CDIMAGESEGMENT
				{
					buf.putShort(SIG_CDIMAGESEGMENT);          // Header.Signature - SIG_CDIMAGESEGMENT
					buf.putInt(segSize + SIZE_CDIMAGESEGMENT); // Header.Length
					buf.putShort((short)dataSize);             // DataSize
					buf.putShort((short)segSize);              // SegSize
					
					byte[] segData = new byte[dataSize];
					is.read(segData);
					buf.put(segData);
					if(segSize > dataSize) {
						buf.put((byte)0);
					}
				}
			}
		}
		return buf.array();
	}

	public static void writeItemFileData(Document dxlDoc, String itemName, Path file) throws XMLException, IOException {
		if(!Files.isRegularFile(file)) {
			throw new IllegalArgumentException(MessageFormat.format(Messages.getString("DXLUtil.cannotReadFile"), file)); //$NON-NLS-1$
		}
		try(InputStream is = Files.newInputStream(file)) {
			writeItemFileData(dxlDoc, itemName, is, (int)Files.size(file));
		}
	}

	public static void writeItemFileData(Document dxlDoc, String itemName, byte[] itemData) throws XMLException, IOException {
		try(InputStream is = new ByteArrayInputStream(itemData)) {
			writeItemFileData(dxlDoc, itemName, is, itemData.length);
		}
	}
	
	public static Element writeItemDateTime(Document dxlDoc, String itemName, boolean removeExisting, Instant value) throws XMLException {
		if(removeExisting) {
			deleteItems(dxlDoc, itemName);
		}
		
		if(value != null) {
			Element note = getRootNoteElement(dxlDoc);
			Element item = DOMUtil.createElement(dxlDoc, note, "item"); //$NON-NLS-1$
			item.setAttribute("name", itemName); //$NON-NLS-1$
			item = DOMUtil.createElement(dxlDoc, item, "datetime"); //$NON-NLS-1$
			item.setTextContent(DXL_DATETIME_FORMAT.get().format(Date.from(value)));
			return item;
		} else {
			return null;
		}
	}
	
	/**
	 * @param dxlDoc the DXL document to search
	 * @return the root {@code note} element
	 * @throws XMLException if there is a problem evaluating the XPath
	 * @throws IllegalStateException if the root {@code note} element is not present
	 * @since 2.5.0
	 */
	private static Element getRootNoteElement(Document dxlDoc) throws XMLException {
		Element note = (Element)DOMUtil.evaluateXPath(dxlDoc, "/note").getSingleNode(); //$NON-NLS-1$
		if(note == null) {
			throw new IllegalStateException("Root element <note> not found. This is most likely because the ODP is not using binary DXL, and this is currently unsupported");
		}
		return note;
	}
}
