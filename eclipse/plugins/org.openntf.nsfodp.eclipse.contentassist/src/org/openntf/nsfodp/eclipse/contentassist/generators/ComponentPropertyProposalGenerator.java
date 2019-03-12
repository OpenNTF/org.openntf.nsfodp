/**
 * Copyright © 2018-2019 Jesse Gallagher
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
package org.openntf.nsfodp.eclipse.contentassist.generators;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.openntf.nsfodp.eclipse.contentassist.ComponentCache;
import org.openntf.nsfodp.eclipse.contentassist.model.AbstractComponent;
import org.openntf.nsfodp.eclipse.contentassist.model.ComponentProperty;
import org.openntf.nsfodp.eclipse.contentassist.proposals.ComponentPropertyCompletionProposal;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AbstractItemProposalGenerator;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

@SuppressWarnings("restriction")
public class ComponentPropertyProposalGenerator extends AbstractItemProposalGenerator<ComponentPropertyCompletionProposal> {

	@SuppressWarnings("unchecked")
	private static List<ComponentPropertyCompletionProposal> computeAttributeProcessorSuggestions(
		IDOMNode node, IStructuredDocument document, int cursorposition) throws BadLocationException, CoreException, SAXException, IOException, ParserConfigurationException {

		String pattern = findProcessorNamePattern(document, cursorposition);
		
		String nodeName = node.getNodeName();
		
		// Find the component matching the current node
		@SuppressWarnings("rawtypes")
		Optional<AbstractComponent> componentOpt = ComponentCache.getCustomControls().stream()
			.filter(c -> c.getPrefixedName().equals(nodeName))
			.map(AbstractComponent.class::cast)
			.findFirst();
		if(!componentOpt.isPresent()) {
			componentOpt = ComponentCache.getStockComponents().stream()
				.filter(c -> c.getPrefixedName().equals(nodeName))
				.map(AbstractComponent.class::cast)
				.findFirst();
		} 
		
		if(componentOpt.isPresent()) {
			NamedNodeMap existingattributes = node.getAttributes();
			
			AbstractComponent<?> component = componentOpt.get();
			return component.getProperties().stream()
				.map(property -> createPropertyProposal(pattern, property, existingattributes, node, cursorposition))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
		} else {
			return Collections.EMPTY_LIST;
		}

	}

	private static Optional<ComponentPropertyCompletionProposal> createPropertyProposal(String pattern,
		ComponentProperty property, NamedNodeMap existingattributes, IDOMNode node,
		int cursorposition) {

		// Double check that the processor type being used this time around
		// matches the pattern
		if (!property.getName().startsWith(pattern)) {
			return null;
		}

		ComponentPropertyCompletionProposal proposal = new ComponentPropertyCompletionProposal(
				property, pattern.length(), cursorposition);

		// Only include the proposal if it isn't already in the element
		if (existingattributes.getNamedItem(proposal.getDisplayString()) == null) {
			return Optional.of(proposal);
		}
		
		return Optional.empty();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ComponentPropertyCompletionProposal> generateProposals(IDOMNode node,
		ITextRegion textregion, IStructuredDocumentRegion documentregion,
		IStructuredDocument document, int cursorposition) throws BadLocationException {

		try {
			return makeAttributeProcessorSuggestions(node, textregion, documentregion, document, cursorposition) ?
				computeAttributeProcessorSuggestions(node, document, cursorposition) :
				Collections.EMPTY_LIST;
		} catch (CoreException | SAXException | IOException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean makeAttributeProcessorSuggestions(IDOMNode node, ITextRegion textregion,
		IStructuredDocumentRegion documentregion, IStructuredDocument document, int cursorposition)
		throws BadLocationException {

		if (node.getNodeType() == IDOMNode.ELEMENT_NODE) {
			if (Character.isWhitespace(document.getChar(cursorposition - 1))) {
				return true;
			}
			if (textregion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				return true;
			}
			ITextRegionList textregionlist = documentregion.getRegions();
			ITextRegion previousregion = textregionlist.get(textregionlist.indexOf(textregion) - 1);
			if (previousregion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				return true;
			}
		}
		return false;
	}
}