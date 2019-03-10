package org.openntf.nsfodp.eclipse.resources;

import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openntf.nsfodp.eclipse.Activator;

public class XPagesNode extends AbstractDesignElementResource {
	public XPagesNode(IProject project) {
		super(project);
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(Object o) {
		return Activator.getDefault().getImageRegistry().getDescriptor(Activator.ICON_HTML);
	}
	
	@Override
	public String getLabel() {
		return "XPages";
	}
	
	@Override
	public int getIndex() {
		return 1;
	}

	@Override
	public String getDesignElementPath() {
		return "XPages";
	}

	@Override
	public Predicate<IFile> getFilter() {
		return f -> f.getFileExtension().equals("xsp");
	}

}
