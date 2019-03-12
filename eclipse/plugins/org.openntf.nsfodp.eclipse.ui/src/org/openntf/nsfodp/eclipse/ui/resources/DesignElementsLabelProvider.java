package org.openntf.nsfodp.eclipse.ui.resources;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

public class DesignElementsLabelProvider extends WorkbenchLabelProvider implements ICommonLabelProvider {

	@Override
	public void restoreState(IMemento memento) {

	}

	@Override
	public void saveState(IMemento memento) {
	}

	@Override
	public String getDescription(Object element) {
		if (element instanceof DesignElementNode) {
			return ((DesignElementNode) element).getType().getLabel();
		}
		return null;
	}

	@Override
	public void init(ICommonContentExtensionSite config) {

	}

}
