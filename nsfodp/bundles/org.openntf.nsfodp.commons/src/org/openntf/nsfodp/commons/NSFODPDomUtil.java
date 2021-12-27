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
package org.openntf.nsfodp.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
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
 * Contains utility methods for working with the org.w3c.dom classes.
 * 
 * <p>These methods catch checked exceptions and re-throw them as {@link RuntimeException}
 * subclasses.</p>
 * 
 * @author Jesse Gallagher
 * @since 3.5.0
 */
public enum NSFODPDomUtil {
	;
	
	private static TransformerFactory tFactory = TransformerFactory.newInstance();
	
	/**
	 * Creates a new, empty {@link Document}.
	 * 
	 * @return the newly-created {@link Document}
	 */
	public static Document createDocument() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parses the provided XML string into a {@link Document} instance.
	 * 
	 * @param xml the XML string to parse
	 * @return a parsed {@link Document} instance
	 * @throws ParserConfigurationException if there is a problem initializng the
	 *       {@link DocumentBuilder}
	 * @throws SAXException if there is a problem parsing the XML
	 */
	public static Document parseXml(String xml) {
		try (InputStream is = new ByteArrayInputStream(xml.getBytes())) {
			return parseXml(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	/**
	 * Parses the provided XML stream into a {@link Document} instance.
	 * 
	 * @param xml the XML stream to parse
	 * @return a parsed {@link Document} instance
	 * @throws ParserConfigurationException if there is a problem initializng the
	 *       {@link DocumentBuilder}
	 * @throws SAXException if there is a problem parsing the XML
	 */
	public static Document parseXml(InputStream xml) {
		return parseXml(new InputStreamReader(xml, StandardCharsets.UTF_8));
	}
	
	/**
	 * Parses the provided XML reader into a {@link Document} instance.
	 * 
	 * @param xml the XML reader to parse
	 * @return a parsed {@link Document} instance
	 * @throws ParserConfigurationException if there is a problem initializng the
	 *       {@link DocumentBuilder}
	 * @throws SAXException if there is a problem parsing the XML
	 */
	public static Document parseXml(Reader r) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource source = new InputSource(r);
			Document doc = builder.parse(source);
			doc.setXmlStandalone(true);
			return doc;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Selects a single node via XPath.
	 * 
	 * @param node the context node to search from
	 * @param xpathString the XPath expression to evaluate
	 * @return a single node selected by the path
	 */
	public static Node selectSingleNode(Node node, String xpathString) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			return (Node)xpath.evaluate(xpathString, node, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Selects a collection of nodes via XPath.
	 * 
	 * @param node the context node to search from
	 * @param xpathString the XPath expression to evaluate
	 * @return a list of nodes selected by the path
	 */
	public static NodeList selectNodes(Node node, String xpathString) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			return (NodeList)xpath.evaluate(xpathString, node, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Retrieves a stream of selected nodes via XPath.
	 * 
	 * @param node the context node to search from
	 * @param xpathString the XPath expression to evaluate
	 * @return a stream of nodes selected by the path
	 */
	public static Stream<Node> streamNodes(Node node, String xpathString) {
		NodeList nodes = selectNodes(node, xpathString);
		return IntStream.range(0, nodes.getLength())
			.mapToObj(i -> nodes.item(i));
	}
	
	/**
	 * Creates a new child element beneath the provided element.
	 * 
	 * @param parent the parent element to append to
	 * @param nodeName the name of the new element
	 * @return the created element
	 */
	public static Element createElement(Element parent, String nodeName) {
		Element el = parent.getOwnerDocument().createElement(nodeName);
		parent.appendChild(el);
		return el;
	}
	
	/**
	 * Writes the provided node to the provided output stream.
	 * 
	 * <p>If {@code transformer} is {@code null}, this will use default formatting.</p>
	 * 
	 * @param os the destination stream
	 * @param node the node to serialize
	 * @param transformer the transformer to use; may be {@code null}
	 */
	public static void serialize(OutputStream os, Node node, Transformer transformer) {
		serialize(new OutputStreamWriter(os), node, transformer);
	}
	
	/**
	 * Writes the provided node to the provided writer.
	 * 
	 * <p>If {@code transformer} is {@code null}, this will use default formatting.</p>
	 * 
	 * @param os the destination writer
	 * @param node the node to serialize
	 * @param transformer the transformer to use; may be {@code null}
	 */
	public static void serialize(Writer w, Node node, Transformer transformer) {
		if (transformer == null) {
			transformer = createTransformer(null);
		}
		StreamResult result = new StreamResult(w);
		DOMSource source = new DOMSource(node);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Serializes the provided node as an XML string.
	 * 
	 * @param node the node to serialize
	 * @param transformer the transformer to use; may be {@code null}
	 * @return the serialized XML string
	 */
	public static String getXmlString(Node node, Transformer transformer) {
		try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			serialize(os, node, transformer);
			return os.toString("UTF-8"); //$NON-NLS-1$
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public static Transformer createTransformer(final InputStream xsltStream) {
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
