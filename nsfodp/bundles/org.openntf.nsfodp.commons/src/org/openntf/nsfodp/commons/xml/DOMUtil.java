package org.openntf.nsfodp.commons.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Contains methods for working with XML and DOM classes.
 * 
 * @author Jesse Gallagher
 * @since 3.7.1
 */
public enum DOMUtil {
	;
	
	private static XPath xpath;
	
	public static List<Node> nodes(Node node, String xpathString) {
		try {
			NodeList nodes = (NodeList) getXPath().compile(xpathString).evaluate(node, XPathConstants.NODESET);
			List<Node> result = new ArrayList<>(nodes.getLength());
			for (int i = 0; i < nodes.getLength(); i++) {
				result.add(nodes.item(i));
			}

			return result;
		} catch (XPathExpressionException xee) {
			throw new RuntimeException(xee);
		}
	}
	
	public static Optional<Node> node(Node node, String xpathString) {
		List<Node> nodes = nodes(node, xpathString);
		return nodes.isEmpty() ? Optional.empty() : Optional.of(nodes.get(0));
	}
	
	public static Element createElement(Element parent, String name) {
		Element element = parent.getOwnerDocument().createElement(name);
		parent.appendChild(element);
		return element;
	}
	
	public static Element createElement(Document parent, String name) {
		Element element = parent.createElement(name);
		parent.appendChild(element);
		return element;
	}
	
	public static Document createDocument() {
		return getBuilder().newDocument();
	}
	
	public static Document createDocument(Reader r) {
		InputSource source = new InputSource(r);
		try {
			return getBuilder().parse(source);
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Document createDocument(InputStream is) {
		InputSource source = new InputSource(is);
		try {
			return getBuilder().parse(source);
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void serialize(OutputStream os, Node node) {
		try {
			Transformer transformer = createTransformer(null);
			StreamResult result = new StreamResult(os);
			DOMSource source = new DOMSource(node);
			transformer.transform(source, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void serialize(Writer w, Node node) {
		try {
			Transformer transformer = createTransformer(null);
			StreamResult result = new StreamResult(w);
			DOMSource source = new DOMSource(node);
			transformer.transform(source, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getXMLString(Node node) {
		try(StringWriter w = new StringWriter()) {
			serialize(w, node);
			w.flush();
			return w.toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
    /**
     * Inserts the node newChild after the existing child node refChild.
	 * Inserts the node newChild after the existing child node refChild. If refChild is null, 
	 * insert newChild at the end of the list of children.
	 * If newChild is a DocumentFragment object, all of its children are inserted, 
	 * in the same order, after refChild. If the newChild is already in the tree, 
	 * it is first removed.
	 * @return The node being inserted     
     */
    public static Node insertAfter(Node parent, Node newChild, Node refChild) {
    	if(refChild!=null) {
    		Node next = refChild.getNextSibling();
    		return parent.insertBefore(newChild, next);
    	}
		return parent.insertBefore(newChild, refChild);
    }
	
	// *******************************************************************************
	// * Internal implementation utilities
	// *******************************************************************************
	
	private static XPath getXPath() {
		if (xpath == null) {
			xpath = XPathFactory.newInstance().newXPath();
		}
		return xpath;
	}
	
	private static DocumentBuilder getBuilder() {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setValidating(false);
		try {
			return fac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Transformer createTransformer(final InputStream xsltStream) {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			if (xsltStream == null) {
				transformer = tFactory.newTransformer();
			} else {
				Source filter = new StreamSource(xsltStream);
				transformer = tFactory.newTransformer(filter);
			}
			// We don't want the XML declaration in front
			//transformer.setOutputProperty("omit-xml-declaration", "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}

		return transformer;
	}
}
