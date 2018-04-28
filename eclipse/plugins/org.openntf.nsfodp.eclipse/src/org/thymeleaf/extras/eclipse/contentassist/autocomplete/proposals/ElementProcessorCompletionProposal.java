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
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin;
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames;
import org.openntf.nsfodp.eclipse.Activator;

/**
 * A completion proposal for Thymeleaf element processors.
 * 
 * @author Emanuel Rabina
 */
//Modified from original to avoid hard Thymeleaf dependency
@SuppressWarnings({ "restriction", "nls" })
public class ElementProcessorCompletionProposal extends AbstractCompletionProposal {

	private final boolean addendtag;
	private final String fullprocessorname;

	/**
	 * Constructor, creates a completion proposal for a Thymeleaf element
	 * processor.
	 * 
	 * @param elementName     The name of the element being processed
	 * @param charsentered	  How much of the entire proposal has already been
	 * 						  entered by the user.
	 * @param cursorposition
	 */
	public ElementProcessorCompletionProposal(String elementName, int charsentered, int cursorposition) {

		super(elementName.substring(charsentered), cursorposition);
		addendtag = HTMLUIPlugin.getDefault().getPreferenceStore().getBoolean(
				HTMLUIPreferenceNames.TYPING_COMPLETE_ELEMENTS);
		this.fullprocessorname = elementName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) throws BadLocationException {

		String replacement = replacementstring.substring(offset - cursorposition) + ">";
		if (addendtag) {
			replacement += "</" + fullprocessorname + ">";
		}
		document.replace(offset, 0, replacement);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayString() {

		return fullprocessorname;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(Activator.ICON_EMBLEM_SYSTEM);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point getSelection(IDocument document) {

		return new Point(cursorposition + replacementstring.length() + 1, 0);
	}
}