package org.openntf.maven.nsfodp.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
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
}
