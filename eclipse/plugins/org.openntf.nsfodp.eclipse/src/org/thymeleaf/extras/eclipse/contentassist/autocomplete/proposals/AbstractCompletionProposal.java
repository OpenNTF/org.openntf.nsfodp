/*
 * Copyright 2013, The Thymeleaf Project (http://www.thymeleaf.org/)
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

package org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.openntf.nsfodp.eclipse.Activator;

/**
 * Common code for all processor and expression object completion proposals.
 * 
 * @author Emanuel Rabina
 */
// Modified from original to avoid hard Thymeleaf dependency
public abstract class AbstractCompletionProposal implements ICompletionProposal, ICompletionProposalExtension {

	protected final String replacementstring;
	protected final int cursorposition;

	protected final IContextInformation contextinformation;

	/**
	 * Subclass constructor, set completion information.
	 * 
	 * @param replacementstring Value to be entered into the document if this
	 * 							proposal is selected.
	 * @param cursorposition
	 */
	protected AbstractCompletionProposal(String replacementstring, int cursorposition) {
		this.replacementstring = replacementstring;
		this.cursorposition    = cursorposition;
		this.contextinformation     = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(IDocument document) {

		apply(document, '\0', cursorposition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(IDocument document, char trigger, int offset) {

		try {
			applyImpl(document, trigger, offset);
		}
		catch (BadLocationException ex) {
			Activator.logError("Unable to apply proposal", ex);
		}
	}

	/**
	 * Applies the proposal to the document.
	 * 
	 * @param document
	 * @param trigger
	 * @param offset
	 * @throws BadLocationException
	 */
	protected abstract void applyImpl(IDocument document, char trigger, int offset)
		throws BadLocationException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAdditionalProposalInfo() {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContextInformation getContextInformation() {

//		return contextinformation;
		return new ContextInformation("Context string", "Information string");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getContextInformationPosition() {

		return contextinformation == null ? -1 : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public char[] getTriggerCharacters() {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValidFor(IDocument document, int offset) {

		try {
			// Use this proposal if the characters typed since it was suggested still
			// match the string this proposal will insert into the document
			return replacementstring.startsWith(document.get(cursorposition, offset - cursorposition));
		}
		catch (BadLocationException ex) {
			return false;
		}
	}
}