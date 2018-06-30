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
package org.openntf.nsfodp.commons.odp.util;

import static org.openntf.nsfodp.commons.odp.util.ODSConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.openntf.nsfodp.commons.h.Ods;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

import lotus.domino.Database;
import lotus.domino.DxlImporter;
import lotus.domino.NotesException;

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
		writeItemDataRaw(dxlDoc, itemName, data, PER_FILE_ITEM_DATA_CAP, Ods.SIZE_CDFILEHEADER);
	}
	
	public static void writeItemDataRaw(Document dxlDoc, String itemName, byte[] data, int itemCap, int headerSize) throws XMLException {
		deleteItems(dxlDoc, itemName);
		
		Element note = (Element)DOMUtil.evaluateXPath(dxlDoc, "/note").getSingleNode(); //$NON-NLS-1$
		
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
	
	public static void writeItemNumber(Document dxlDoc, String itemName, Number... value) throws XMLException {
		deleteItems(dxlDoc, itemName);
		
		if(value != null) {
			Element note = (Element)DOMUtil.evaluateXPath(dxlDoc, "/note").getSingleNode(); //$NON-NLS-1$
			Element item = DOMUtil.createElement(dxlDoc, note, "item"); //$NON-NLS-1$
			item.setAttribute("name", itemName); //$NON-NLS-1$
			for(Number val : value) {
				Element number = DOMUtil.createElement(dxlDoc, item, "number"); //$NON-NLS-1$
				number.setTextContent(val.toString());
			}
		}
	}
	
	public static Element writeItemString(Document dxlDoc, String itemName, boolean removeExisting, CharSequence... value) throws XMLException {
		if(removeExisting) {
			deleteItems(dxlDoc, itemName);
		}
		
		if(value != null) {
			Element note = (Element)DOMUtil.evaluateXPath(dxlDoc, "/note").getSingleNode(); //$NON-NLS-1$
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
	
	public static List<String> getItemValueStrings(Document dxlDoc, String itemName) throws XMLException {
		List<String> result = new ArrayList<>();
		
		Object[] nodes = DOMUtil.evaluateXPath(dxlDoc, "/*[name()='note']/*[name()='item'][@name='" + escapeXPathValue(itemName) + "']/*[name()='text']").getNodes(); //$NON-NLS-1$ //$NON-NLS-2$
		for(Object nodeObj : nodes) {
			Node node = (Node)nodeObj;
			result.add(node.getTextContent());
		}
		
		return result;
	}
	
	public static void deleteItems(Document dxlDoc, String itemName) throws XMLException {
		Object[] existingNodes = DOMUtil.evaluateXPath(dxlDoc, "/note/item[@name='" + escapeXPathValue(itemName) + "']").getNodes(); //$NON-NLS-1$ //$NON-NLS-2$
		for(Object existing : existingNodes) {
			Node node = (Node)existing;
			node.getParentNode().removeChild(node);
		}
	}
	
	public static String escapeXPathValue(final String input) {
		return input.replace("'", "\\'"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Imports a generic file resource, such as an outer class file from a multi-class Java resource.
	 */
	public static void importFileResource(DxlImporter importer, byte[] data, Database database, String name, String flags, String flagsExt) throws XMLException, IOException, NotesException {
		Document dxlDoc = DOMUtil.createDocument();
		Element note = DOMUtil.createElement(dxlDoc, "note"); //$NON-NLS-1$
		note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
		note.setAttribute("xmlns", "http://www.lotus.com/dxl"); //$NON-NLS-1$ //$NON-NLS-2$
		writeItemString(dxlDoc, "$Flags", false, flags); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(flagsExt)) {
			writeItemString(dxlDoc, "$FlagsExt", false, flagsExt);	 //$NON-NLS-1$
		}
		writeItemString(dxlDoc, "$TITLE", false, name); //$NON-NLS-1$
		writeItemNumber(dxlDoc, "$FileSize", data.length); //$NON-NLS-1$
		writeItemFileData(dxlDoc, "$FileData", data); //$NON-NLS-1$
		writeItemString(dxlDoc, "$FileNames", false, name); //$NON-NLS-1$
		String dxl = DOMUtil.getXMLString(dxlDoc);
		importer.importDxl(dxl, database);
	}
}
