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
package org.openntf.nsfodp.eclipse.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.openntf.nsfodp.eclipse.Activator;
import org.openntf.nsfodp.eclipse.contentassist.generators.ComponentPropertyProposalGenerator;
import org.openntf.nsfodp.eclipse.contentassist.generators.CustomControlProposalGenerator;
import org.openntf.nsfodp.eclipse.contentassist.generators.StockComponentsProposalGenerator;
import org.thymeleaf.extras.eclipse.contentassist.AbstractComputer;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AbstractItemProposalGenerator;

@SuppressWarnings("restriction")
public class XspCompletionProposalComputer extends AbstractComputer implements ICompletionProposalComputer {

	private static AbstractItemProposalGenerator<?>[] proposalgenerators = {
		new CustomControlProposalGenerator(),
		new StockComponentsProposalGenerator(),
		new ComponentPropertyProposalGenerator()
	};

	@Override
	public List<?> computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		if(!ContentAssistUtil.isTargetProject() || !ContentAssistUtil.isXsp(context)) {
			return Collections.emptyList();
		}

		ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		try {
			ITextViewer viewer = context.getViewer();
			IStructuredDocument document = (IStructuredDocument) context.getDocument();
			int cursorposition = context.getInvocationOffset();

			IDOMNode node = (IDOMNode) ContentAssistUtils.getNodeAt(viewer, cursorposition);
			IStructuredDocumentRegion documentregion = ContentAssistUtils.getStructuredDocumentRegion(viewer,
					cursorposition);
			ITextRegion textregion = documentregion.getRegionAtCharacterOffset(cursorposition);

			// Create proposals from the generators given to us by the computers
			for (AbstractItemProposalGenerator<?> proposalgenerator : proposalgenerators) {
				proposals.addAll(proposalgenerator.generateProposals(node, textregion, documentregion, document,
						cursorposition));
			}
		} catch (BadLocationException ex) {
			Activator.logError("Unable to retrieve data at the current document position", ex);
		}

		return proposals;
	}

	@Override
	public List<?> computeContextInformation(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		return Collections.emptyList();
	}


	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionEnded() {
		// NOP
	}

	@Override
	public void sessionStarted() {
		// NOP
	}
}
