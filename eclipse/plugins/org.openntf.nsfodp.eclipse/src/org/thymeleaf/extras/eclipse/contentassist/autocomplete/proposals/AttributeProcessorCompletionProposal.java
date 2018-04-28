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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * A completion proposal for Thymeleaf attribute processors.
 * 
 * @author Emanuel Rabina
 */
//Modified from original to avoid hard Thymeleaf dependency
@SuppressWarnings("nls")
public class AttributeProcessorCompletionProposal extends AbstractCompletionProposal {

	private final String displaystring;

	/**
	 * Constructor, creates a completion proposal for a Thymeleaf attribute
	 * processor.
	 * 
	 * @param attributeName  Name of the attribute being processed
	 * @param charsentered   How much of the entire proposal has already been
	 *                       entered by the user.
	 * @param cursorposition
	 */
	public AttributeProcessorCompletionProposal(String attributeName,
		int charsentered, int cursorposition) {

		super(attributeName.substring(charsentered), cursorposition);
		this.displaystring = attributeName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) throws BadLocationException {

		document.replace(offset, 0, replacementstring.substring(offset - cursorposition) + "=\"\"");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayString() {

		return displaystring;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point getSelection(IDocument document) {

		return new Point(cursorposition + replacementstring.length() + 2, 0);
	}
}