package org.openntf.nsfodp.eclipse.ui.resources.wizards;

import org.openntf.nsfodp.eclipse.ui.resources.DesignElementType;

public class NewViewWizard extends AbstractDesignElementWizard {
	@Override
	public DesignElementType getType() {
		return DesignElementType.Views;
	}
}