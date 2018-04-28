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
package org.openntf.nsfodp.eclipse.contentassist.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.openntf.nsfodp.eclipse.Activator;
import org.openntf.nsfodp.eclipse.contentassist.XspCompletionProposalComputer;
import org.openntf.nsfodp.eclipse.contentassist.model.CustomControl;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AbstractItemProposalGenerator;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.ElementProcessorCompletionProposal;
import org.xml.sax.SAXException;

@SuppressWarnings("restriction")
public class XspElementProposalGenerator extends AbstractItemProposalGenerator<ElementProcessorCompletionProposal> {

	/**
	 * Collect element processor suggestions.
	 * 
	 * @param node
	 * @param document
	 * @param cursorposition
	 * @return List of element processor suggestions.
	 * @throws BadLocationException
	 */
	@SuppressWarnings("unchecked")
	private static List<ElementProcessorCompletionProposal> computeElementProcessorSuggestions(
		IDOMNode node, IStructuredDocument document, int cursorposition) throws BadLocationException {

		String pattern = findProcessorNamePattern(document, cursorposition);

		try {
			Collection<CustomControl> customControls = XspCompletionProposalComputer.getCustomControls();
			if (!customControls.isEmpty()) {
				List<ElementProcessorCompletionProposal> proposals = new ArrayList<>();
				for (CustomControl cc : customControls) {
					proposals.add(new ElementProcessorCompletionProposal(cc.getPrefix() + ':' + cc.getTagName(),
							pattern.length(), cursorposition));
				}
				return proposals;
			}
		} catch (CoreException | SAXException | IOException | ParserConfigurationException e) {
			Activator.logError("Error while composing CC suggestions", e);
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ElementProcessorCompletionProposal> generateProposals(IDOMNode node,
		ITextRegion textregion, IStructuredDocumentRegion documentregion,
		IStructuredDocument document, int cursorposition) throws BadLocationException {

		return makeElementProcessorSuggestions(node, textregion, documentregion, document, cursorposition) ?
				computeElementProcessorSuggestions(node, document, cursorposition) :
				Collections.emptyList();
	}

	/**
	 * Check if, given everything, element processor suggestions should be made.
	 * 
	 * @param node
	 * @param textregion
	 * @param documentregion
	 * @param document
	 * @param cursorposition
	 * @return <tt>true</tt> if element processor suggestions should be made.
	 * @throws BadLocationException
	 */
	private static boolean makeElementProcessorSuggestions(IDOMNode node, ITextRegion textregion,
		IStructuredDocumentRegion documentregion, IStructuredDocument document, int cursorposition)
		throws BadLocationException {

		switch (node.getNodeType()) {

		// If we're in a text node, then the first non-whitespace character before
		// the cursor in the document should be an opening bracket
		case IDOMNode.TEXT_NODE:
			int position = cursorposition - 1;
			while (position >= 0 && Character.isWhitespace(document.getChar(position))) {
				position--;
			}
			if (document.getChar(position) == '<') {
				return true;
			}
			break;

		// If we're in an element node, then the previous text region should be an
		// opening XML tag
		case IDOMNode.ELEMENT_NODE:
			ITextRegionList textregionlist = documentregion.getRegions();
			int currentregionindex = textregionlist.indexOf(textregion);
			try {
				ITextRegion previousregion = textregionlist.get(currentregionindex - 1);
				if ((previousregion.getType() == DOMRegionContext.XML_TAG_OPEN) &&
					!Character.isWhitespace(document.getChar(cursorposition - 1))) {
					return true;
				}
			}
			catch (ArrayIndexOutOfBoundsException ex) {
			}
			break;
		}

		return false;
	}
}
