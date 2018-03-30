/**
 * Copyright © 2018 Jesse Gallagher
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

import static org.openntf.nsfodp.compiler.util.ODSConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		byte[] data = CompositeDataUtil.getFileResourceData(is, fileLength);
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
