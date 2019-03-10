package org.openntf.nsfodp.eclipse.resources;

import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openntf.nsfodp.eclipse.Activator;

public class CustomControlsNode extends AbstractDesignElementResource {
	
	public CustomControlsNode(IProject project) {
		super(project);
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object o) {
		return Activator.getDefault().getImageRegistry().getDescriptor(Activator.ICON_SCRIPT);
	}
	
	@Override
	public String getLabel() {
		return "Custom Controls";
	}
	
	@Override
	public int getIndex() {
		return 2;
	}

	@Override
	public String getDesignElementPath() {
		return "CustomControls";
	}

	@Override
	public Predicate<IFile> getFilter() {
		return f -> f.getFileExtension().equals("xsp");
	}

}
