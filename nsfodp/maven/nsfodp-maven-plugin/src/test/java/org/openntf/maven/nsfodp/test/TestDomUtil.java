/*
 * Copyright © 2018-2025 Jesse Gallagher
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openntf.nsfodp.commons.xml.NSFODPDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
	
	@Test
	public void testStreamNodes() throws IOException {
		Document doc;
		try(InputStream is = getClass().getResourceAsStream("/xml/classpath.xml")) {
			doc = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(doc);
		
		String path = NSFODPDomUtil.streamNodes(doc, "/classpath/classpathentry[@kind='lib']") //$NON-NLS-1$
			.map(node -> Element.class.cast(node))
			.map(el -> el.getAttribute("path")) //$NON-NLS-1$
			.findFirst()
			.get();
		assertEquals("WebContent/WEB-INF/lib/CstClientTest.jar", path);
	}
	
	@Test
	public void testSelectText() throws IOException {
		Document doc;
		try(InputStream is = getClass().getResourceAsStream("/xml/classpath.xml")) {
			doc = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(doc);
		
		Node node = NSFODPDomUtil.streamNodes(doc, "/classpath/classpathentry[@kind='lib']/@path")
			.findFirst()
			.get();
		assertEquals("WebContent/WEB-INF/lib/CstClientTest.jar", node.getTextContent());
	}
	
	@Test
	public void testPluginXml() throws IOException {
		Document doc;
		try(InputStream is = getClass().getResourceAsStream("/xml/plugin.xml")) {
			doc = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(doc);
		
		List<String> bundles = NSFODPDomUtil.streamNodes(doc, "/plugin/requires/import") //$NON-NLS-1$
			.map(Element.class::cast)
			.map(el -> el.getAttribute("plugin")) //$NON-NLS-1$
			.collect(Collectors.toList());
		assertTrue(bundles.contains("com.ibm.xsp.extsn"));
	}
	
	@Test
	public void testPluginXml2() throws IOException {
		Document doc;
		try(InputStream is = getClass().getResourceAsStream("/xml/plugin.xml")) {
			doc = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(doc);
		
		List<String> bundles = NSFODPDomUtil.nodes(doc, "/plugin/requires/import").stream() //$NON-NLS-1$
			.map(Element.class::cast)
			.map(el -> el.getAttribute("plugin")) //$NON-NLS-1$
			.collect(Collectors.toList());
		assertTrue(bundles.contains("com.ibm.xsp.extsn"));
	}
	
	@Test
	public void testFindAndDelete() throws IOException {
		Document props;
		try(InputStream is = getClass().getResourceAsStream("/xml/dbprops.xml")) {
			props = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(props);
		
		assertFalse(NSFODPDomUtil.nodes(props, "/database/acl").isEmpty());
		
		for(Node nodeObj : NSFODPDomUtil.nodes(props.getDocumentElement(), "/database/acl")) { //$NON-NLS-1$
			nodeObj.getParentNode().removeChild(nodeObj);
		}

		assertTrue(NSFODPDomUtil.nodes(props, "/database/acl").isEmpty());
	}
	
	@Test
	public void testFindFtSettings() throws IOException {
		Document props;
		try(InputStream is = getClass().getResourceAsStream("/xml/dbprops2.xml")) {
			props = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(props);
		
		Element fulltextsettings = (Element)NSFODPDomUtil.node(props, "/*[name()='database']/*[name()='fulltextsettings']").orElse(null); //$NON-NLS-1$
		assertNotNull(fulltextsettings);
	}
	
	@Test
	public void testNamespaceUri() throws IOException {
		Document xspConfig;
		try(InputStream is = getClass().getResourceAsStream("/xml/xsp-config.xml")) {
			xspConfig = NSFODPDomUtil.parseXml(is);
		}
		assertNotNull(xspConfig);
		
		String uri = NSFODPDomUtil.node(xspConfig, "/faces-config/faces-config-extension/namespace-uri/text()").get().getTextContent();
		assertEquals("http://www.ibm.com/xsp/core", uri);
	}
}
