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

package org.openntf.nsfodp.eclipse.contentassist.proposals;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin;
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames;
import org.openntf.nsfodp.eclipse.Activator;
import org.openntf.nsfodp.eclipse.contentassist.model.CustomControl;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.AbstractCompletionProposal;

/**
 * Completion proposal provider for project Custom Controls
 */
@SuppressWarnings({ "restriction", "nls" })
public class CustomControlCompletionProposal extends AbstractCompletionProposal {

	private final boolean addendtag;
	private final CustomControl customControl;

	/**
	 * Constructor, creates a completion proposal for an in-project custom control.
	 * 
	 * @param customControl   The custom control in question
	 * @param charsentered	  How much of the entire proposal has already been
	 * 						  entered by the user.
	 * @param cursorposition  The user's current cursor position
	 */
	public CustomControlCompletionProposal(CustomControl customControl, int charsentered, int cursorposition) {
		super(customControl.getPrefixedName().substring(charsentered), cursorposition);
		addendtag = HTMLUIPlugin.getDefault().getPreferenceStore().getBoolean(HTMLUIPreferenceNames.TYPING_COMPLETE_ELEMENTS);
		this.customControl = customControl;
	}
	
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) throws BadLocationException {
		String replacement = replacementstring.substring(offset - cursorposition) + ">";
		if (addendtag) {
			replacement += "</" + customControl.getPrefixedName() + ">";
		}
		document.replace(offset, 0, replacement);
	}

	@Override
	public String getDisplayString() {
		return customControl.getPrefixedName();
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(Activator.ICON_EMBLEM_SYSTEM);
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(cursorposition + replacementstring.length() + 1, 0);
	}
}