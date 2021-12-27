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
package org.openntf.maven.nsfodp.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.openntf.nsfodp.commons.NSFODPDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@SuppressWarnings("nls")
public class TestDomUtil {
	@Test
	public void testParseBasic() throws IOException {
		Document doc;
		try(InputStream is = getClass().getResourceAsStream("/xml/basic.xml")) {
			doc = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(doc);
		Element docElem = doc.getDocumentElement();
		assertEquals("foo", docElem.getNodeName());
		NodeList bars = docElem.getElementsByTagName("bar");
		assertEquals(2, bars.getLength());
	}
	
	@Test
	public void testBasicXPath() throws IOException {
		Document doc;
		try(InputStream is = getClass().getResourceAsStream("/xml/basic.xml")) {
			doc = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(doc);
		
		assertEquals(2, NSFODPDomUtil.streamNodes(doc, "//bar").count());
		assertEquals("hey", NSFODPDomUtil.selectSingleNode(doc, "/foo/bar[@alt]/@alt").getNodeValue());
		assertEquals("there", NSFODPDomUtil.selectSingleNode(doc, "/foo/bar[@alt]/text()").getNodeValue());
	}
	
	@Test
	public void testRoundTrip() throws IOException {
		String xml;
		{
			Document doc;
			try(InputStream is = getClass().getResourceAsStream("/xml/basic.xml")) {
				doc = NSFODPDomUtil.parseXml(is);
			}
			assertNotNull(doc);
			xml = NSFODPDomUtil.getXmlString(doc, null);
		}
		{
			Document doc = NSFODPDomUtil.parseXml(xml);
			
			assertEquals(2, NSFODPDomUtil.streamNodes(doc, "//bar").count());
			assertEquals("hey", NSFODPDomUtil.selectSingleNode(doc, "/foo/bar[@alt]/@alt").getNodeValue());
			assertEquals("there", NSFODPDomUtil.selectSingleNode(doc, "/foo/bar[@alt]/text()").getNodeValue());
		}
	}
	
	@Test
	public void testParseLarger() throws IOException {
		String xml;
		{
			Document doc;
			try(InputStream is = getClass().getResourceAsStream("/xml/dbprops.xml")) {
				doc = NSFODPDomUtil.parseXml(is);
			}
			assertNotNull(doc);
			xml = NSFODPDomUtil.getXmlString(doc, null);
		}
		assertTrue(xml.contains("<text>NSF ODP Tooling Example</text>"));
	}
}
