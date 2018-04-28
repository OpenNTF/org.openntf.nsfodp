package org.openntf.nsfodp.eclipse.contentassist.proposals;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.openntf.nsfodp.eclipse.contentassist.model.ComponentProperty;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.proposals.AbstractCompletionProposal;

public class ComponentPropertyCompletionProposal extends AbstractCompletionProposal {

	private final ComponentProperty componentAttribute;

	/**
	 * Constructor, creates a completion proposal for a component attribute.
	 * 
	 * @param componentAttribute  The component attribute being processed
	 * @param charsentered   How much of the entire proposal has already been
	 *                       entered by the user.
	 * @param cursorposition
	 */
	public ComponentPropertyCompletionProposal(ComponentProperty componentAttribute, int charsentered, int cursorposition) {
		super(componentAttribute.getName().substring(charsentered), cursorposition);
		this.componentAttribute = componentAttribute;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyImpl(IDocument document, char trigger, int offset) throws BadLocationException {

		document.replace(offset, 0, replacementstring.substring(offset - cursorposition) + "=\"\""); //$NON-NLS-1$
	}

	@Override
	public String getDisplayString() {
		return componentAttribute.getName();
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(cursorposition + replacementstring.length() + 2, 0);
	}
}