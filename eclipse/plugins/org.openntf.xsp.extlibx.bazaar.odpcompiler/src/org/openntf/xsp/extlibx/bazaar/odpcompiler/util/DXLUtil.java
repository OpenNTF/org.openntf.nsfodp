package org.openntf.xsp.extlibx.bazaar.odpcompiler.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

/**
 * Utilities for manipulating "raw"-type DXL documents.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum DXLUtil {
	;

	private static final int SIZE_WORD = 2;
	private static final int SIZE_DWORD = 4;
	private static final int SIZE_LSIG = SIZE_WORD          // Signature
	                             + SIZE_DWORD;              // Length
	private static final int SIZE_CDFILEHEADER = SIZE_LSIG  // Header
	                             + SIZE_WORD                // FilleExtLen
	                             + SIZE_DWORD               // FileDataSize
	                             + SIZE_DWORD               // SegCount
	                             + SIZE_DWORD               // Flags
	                             + SIZE_DWORD;              // Reserved
	private static final int SIZE_CDFILESEGMENT = SIZE_LSIG // Header
			                     + SIZE_WORD                // DataSize
	                             + SIZE_WORD                // SegSize
	                             + SIZE_DWORD               // Flags
	                             + SIZE_DWORD;              // Reserved
	// It appears that CDFILESEGMENTs cap out at 10240 bytes of data
	private static final int SEGMENT_SIZE_CAP = 10240;
	/** The amount of data to store in each CD record item */
	private static final int PER_ITEM_DATA_CAP = (SIZE_CDFILESEGMENT + SEGMENT_SIZE_CAP) * 2;
	
	public static void writeItemFileData(Document dxlDoc, String itemName, byte[] itemData) throws XMLException, IOException {
		try(InputStream is = new ByteArrayInputStream(itemData)) {
			writeItemFileData(dxlDoc, itemName, is, itemData.length);
		}
	}
	
	public static void writeItemFileData(Document dxlDoc, String itemName, Path file) throws XMLException, IOException {
		if(!Files.isRegularFile(file)) {
			throw new IllegalArgumentException("Cannot read file " + file);
		}
		try(InputStream is = Files.newInputStream(file)) {
			writeItemFileData(dxlDoc, itemName, is, (int)Files.size(file));
		}
	}
	public static void writeItemFileData(Document dxlDoc, String itemName, InputStream is, int fileLength) throws XMLException, IOException {
		deleteItems(dxlDoc, itemName);
		
		// Spec out the structure
		int segCount = fileLength / SEGMENT_SIZE_CAP;
		if (fileLength % SEGMENT_SIZE_CAP > 0) {
			segCount++;
		}
		
		int totalSize = SIZE_CDFILEHEADER + (SIZE_CDFILESEGMENT * segCount) + fileLength + (fileLength % 2);
		
		// Now create a CD record for the file data
		// TODO this could be a little more efficient by writing to a Base64 wrapper and
		//   cutting off and making a new item node at size intervals
		ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN) ;
		// CDFILEHEADER
		{
			buf.putShort((short)97);       // Header.Signature - SIG_CDFILEHEADER
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
			int dataOffset = SEGMENT_SIZE_CAP * i;
			short dataSize = (short)Math.min((fileLength - dataOffset), SEGMENT_SIZE_CAP);
			short segSize = (short)(dataSize + (dataSize % 2));

			// CDFILESEGMENT
			{
				buf.putShort((short)96);                  // Header.Signature - SIG_CDFILESEGMENT
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

		Element note = (Element)DOMUtil.evaluateXPath(dxlDoc, "/note").getSingleNode();
		
		
		byte[] reconData = buf.array();
		int dxlChunks = totalSize / PER_ITEM_DATA_CAP;
		if(totalSize % PER_ITEM_DATA_CAP > 0) {
			dxlChunks++;
		}
		int offset = 0;
		Base64.Encoder base64 = Base64.getEncoder();
		for (int i = 0; i < dxlChunks; i++) {
			int chunkSize = Math.min(reconData.length-offset, PER_ITEM_DATA_CAP + (i==0 ? SIZE_CDFILEHEADER : 0));
			String chunkData = base64.encodeToString(Arrays.copyOfRange(reconData, offset, offset + chunkSize));

			Element itemNode = DOMUtil.createElement(dxlDoc, note, "item");
			itemNode.setAttribute("name", itemName);
			Element fileDataNode = DOMUtil.createElement(dxlDoc, itemNode, "rawitemdata");
			fileDataNode.setAttribute("type", "1");
			// Write out the value with 72-column wrapping
			StringBuilder wrapped = new StringBuilder("\n");
			for(int stringIndex = 0; stringIndex < chunkData.length(); stringIndex += 72) {
				wrapped.append(chunkData.substring(stringIndex, Math.min(stringIndex+72, chunkData.length())));
				wrapped.append('\n');
			}
			fileDataNode.setTextContent(wrapped.toString());

			offset += chunkSize;
		}
	}
	
	public static void writeItemNumber(Document dxlDoc, String itemName, Number... value) throws XMLException {
		deleteItems(dxlDoc, itemName);
		
		if(value != null) {
			Element note = (Element)DOMUtil.evaluateXPath(dxlDoc, "/note").getSingleNode();
			Element item = DOMUtil.createElement(dxlDoc, note, "item");
			item.setAttribute("name", itemName);
			for(Number val : value) {
				Element number = DOMUtil.createElement(dxlDoc, item, "number");
				number.setTextContent(val.toString());
			}
		}
	}
	
	public static Element writeItemString(Document dxlDoc, String itemName, boolean removeExisting, CharSequence... value) throws XMLException {
		if(removeExisting) {
			deleteItems(dxlDoc, itemName);
		}
		
		if(value != null) {
			Element note = (Element)DOMUtil.evaluateXPath(dxlDoc, "/note").getSingleNode();
			Element item = DOMUtil.createElement(dxlDoc, note, "item");
			item.setAttribute("name", itemName);
			if(value.length > 1) {
				item = DOMUtil.createElement(dxlDoc, item, "textlist");
			}
			for(CharSequence val : value) {
				Element text = DOMUtil.createElement(dxlDoc, item, "text");
				text.setTextContent(val.toString());
			}
			return item;
		} else {
			return null;
		}
	}
	
	public static void deleteItems(Document dxlDoc, String itemName) throws XMLException {
		Object[] existingNodes = DOMUtil.evaluateXPath(dxlDoc, "/note/item[@name='" + escapeXPathValue(itemName) + "']").getNodes();
		for(Object existing : existingNodes) {
			Node node = (Node)existing;
			node.getParentNode().removeChild(node);
		}
	}
	
	public static String escapeXPathValue(final String input) {
		return input.replace("'", "\\'");
	}
}
