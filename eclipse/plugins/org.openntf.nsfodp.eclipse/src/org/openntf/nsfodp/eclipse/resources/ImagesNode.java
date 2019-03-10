package org.openntf.nsfodp.eclipse.resources;

import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openntf.nsfodp.eclipse.Activator;

public class ImagesNode extends AbstractDesignElementResource {

	public ImagesNode(IProject project) {
		super(project);
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object o) {
		return Activator.getDefault().getImageRegistry().getDescriptor(Activator.ICON_IMAGE);
	}
	
	@Override
	public String getLabel() {
		return "Images";
	}

	@Override
	public int getIndex() {
		return 4;
	}

	@Override
	public String getDesignElementPath() {
		return "Resources/Images";
	}

	@Override
	public Predicate<IFile> getFilter() {
		return f -> !f.getFileExtension().equals("metadata");
	}

}
