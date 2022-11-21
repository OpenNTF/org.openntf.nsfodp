/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.maven.nsfodp.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.xml.NSFODPDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@SuppressWarnings("nls")
public class TestDXLUtil {
	@Test
	public void testDeleteItems() throws IOException {
		Document doc;
		try(InputStream is = getClass().getResourceAsStream("/xml/view.xml")) {
			doc = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(doc);
		
		// Make sure we can find the item node
		{
			List<Node> text = NSFODPDomUtil.nodes(doc, "//item[@name='$Formula']");
			assertFalse(text.isEmpty());
		}
		
		DXLUtil.deleteItems(doc, "$Formula");
		
		// Should be gone now
		{
			List<Node> text = NSFODPDomUtil.nodes(doc, "//item[@name='$Formula']");
			assertTrue(text.isEmpty());
		}
	}
	
	@Test
	public void testRootNoteElement() throws IOException {
		Document doc;
		try(InputStream is = getClass().getResourceAsStream("/xml/view.xml")) {
			doc = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(doc);
		
		assertNotNull(DXLUtil.getRootNoteElement(doc));
	}
	
	@Test
	public void testImageResourceData() throws IOException {
		Path tempRes = Files.createTempFile("imageres", ".gif");
		try {
			try(InputStream is = getClass().getResourceAsStream("/Untitled.gif")) {
				Files.copy(is, tempRes, StandardCopyOption.REPLACE_EXISTING);
			}
			
			Document doc;
			try(InputStream is = getClass().getResourceAsStream("/xml/imageres.xml")) {
				doc = NSFODPDomUtil.parseXml(is);
			}
			assertNotNull(doc);
			
			byte[] data = DXLUtil.getImageResourceData(tempRes, doc);
			assertNotNull(data);
			assertFalse(data.length == 0);
		} finally {
			Files.deleteIfExists(tempRes);
		}
	}
	
	@Test
	public void testImageResourceDataNoMime() throws IOException {
		Path tempRes = Files.createTempFile("imageres", ".gif");
		try {
			try(InputStream is = getClass().getResourceAsStream("/Untitled.gif")) {
				Files.copy(is, tempRes, StandardCopyOption.REPLACE_EXISTING);
			}
			
			Document doc;
			try(InputStream is = getClass().getResourceAsStream("/xml/imageres-nomime.xml")) {
				doc = NSFODPDomUtil.parseXml(is);
			}
			assertNotNull(doc);
			
			byte[] data = DXLUtil.getImageResourceData(tempRes, doc);
			assertNotNull(data);
			assertFalse(data.length == 0);
		} finally {
			Files.deleteIfExists(tempRes);
		}
	}
}
