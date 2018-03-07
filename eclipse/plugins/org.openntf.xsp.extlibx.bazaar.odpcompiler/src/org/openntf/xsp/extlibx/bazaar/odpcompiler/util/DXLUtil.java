package org.openntf.xsp.extlibx.bazaar.odpcompiler.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

import javax.activation.MimetypesFileTypeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

import static org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODSConstants.*;

/**
 * Utilities for manipulating "raw"-type DXL documents.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum DXLUtil {
	;
	
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
		byte[] data = getFileResourceData(is, fileLength);
		writeItemDataRaw(dxlDoc, itemName, data, PER_FILE_ITEM_DATA_CAP, SIZE_CDFILEHEADER);
	}
	
	public static void writeItemDataRaw(Document dxlDoc, String itemName, byte[] data, int itemCap, int headerSize) throws XMLException {
		deleteItems(dxlDoc, itemName);
		
		Element note = (Element)DOMUtil.evaluateXPath(dxlDoc, "/note").getSingleNode();
		
		int dxlChunks = data.length / itemCap;
		if(data.length % itemCap > 0) {
			dxlChunks++;
		}
		int offset = 0;
		Base64.Encoder base64 = Base64.getEncoder();
		for (int i = 0; i < dxlChunks; i++) {
			int chunkSize = Math.min(data.length-offset, itemCap + (i==0 ? headerSize : 0));
			String chunkData = base64.encodeToString(Arrays.copyOfRange(data, offset, offset + chunkSize));

			Element itemNode = DOMUtil.createElement(dxlDoc, note, "item");
			itemNode.setAttribute("name", itemName);
			Element fileDataNode = DOMUtil.createElement(dxlDoc, itemNode, "rawitemdata");
			fileDataNode.setAttribute("type", "1");
//			fileDataNode.setTextContent(chunkData);
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
	
	public static byte[] getImageResourceData(Path file) throws IOException {
		int fileLength = (int)Files.size(file);
		// Load image info
		File imageFile = file.toFile();
		int height = 0; // true value not actually stored
		int width = 0; // true value not actually stored
		String mimeType = new MimetypesFileTypeMap().getContentType(imageFile);
		if(mimeType == null) {
			throw new RuntimeException("Cannot determine MIME type for " + file);
		}
		short imageType = 0;
		switch(mimeType) {
		case "image/gif":
			imageType = 1; // CDIMAGETYPE_GIF
			break;
		case "image/jpeg":
		case "image/png": // for some reason
			imageType = 2; // CDIMAGETYPE_JPEG
			break;
		case "image/bmp":
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
