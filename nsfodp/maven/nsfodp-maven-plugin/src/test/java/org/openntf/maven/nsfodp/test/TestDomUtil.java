package org.openntf.maven.nsfodp.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.openntf.nsfodp.commons.xml.NSFODPDomUtil;
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
