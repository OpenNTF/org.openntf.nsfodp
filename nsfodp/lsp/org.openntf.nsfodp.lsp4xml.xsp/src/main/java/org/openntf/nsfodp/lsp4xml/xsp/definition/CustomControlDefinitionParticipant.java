/**
 * Copyright © 2018-2023 Jesse Gallagher
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
package org.openntf.nsfodp.lsp4xml.xsp.definition;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.IDefinitionParticipant;
import org.eclipse.lemminx.services.extensions.IDefinitionRequest;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.openntf.nsfodp.lsp4xml.xsp.XspPlugin;
import org.openntf.nsfodp.lsp4xml.xsp.completion.XspCompletionParticipant;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * LSP4XML definition participant to look up custom control files based on used tag.
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public class CustomControlDefinitionParticipant implements IDefinitionParticipant {
	private static final Logger log = XspPlugin.log;

	@Override
	public void findDefinition(IDefinitionRequest request, List<LocationLink> locations, CancelChecker cancelChecker) {
		if(log.isLoggable(Level.INFO)) {
			log.info("findDefinition for " + request + ", locations=" + locations + ", cancelChecker=" + cancelChecker);
		}
		
		// Check to see if the current element has the right namespace
		DOMNode node = request.getNode();
		if(node != null && node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (DOMElement)node;
			if(XspCompletionParticipant.NS_XC.equals(element.getNamespaceURI())) {
				String ccName = node.getLocalName();
				if(log.isLoggable(Level.INFO)) {
					log.info("Received definition request for custom control " + ccName);
				}
				
				Path filePath;
				try {
					String docUri = request.getXMLDocument().getDocumentURI();
					filePath = Paths.get(new URI(docUri));
				} catch(URISyntaxException e) {
					if(log.isLoggable(Level.INFO)) {
						log.info("Invalid URI - leaving early");
					}
					// The URI can't be used, so return quietly
					return;
				}
				if(!Files.isRegularFile(filePath)) {
					if(log.isLoggable(Level.INFO)) {
						log.info("File isn't found: " + filePath);
					}
					// We were given a bogus or unusable path, so end early
					return;
				}
				Range start = new Range(new Position(0, 0), new Position(0, 0));
				
				// Check for ODP layout
				Path odpControl = filePath.getParent().getParent().resolve("CustomControls").resolve(ccName + ".xsp");
				if(Files.isRegularFile(odpControl)) {
					locations.add(new LocationLink(odpControl.toUri().toString(), start, start));
				}
				
				// Check for webapp layout
				// TODO account for potential changes in layout if possible
				Path webappControl = filePath.getParent().getParent().resolve("controls").resolve(ccName + ".xsp");
				if(Files.isRegularFile(webappControl)) {
					locations.add(new LocationLink(webappControl.toUri().toString(), start, start));
				}
			}
		}
	}

}
