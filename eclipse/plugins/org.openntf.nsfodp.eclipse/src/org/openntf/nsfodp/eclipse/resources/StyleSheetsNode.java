package org.openntf.nsfodp.eclipse.resources;

import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openntf.nsfodp.eclipse.Activator;

public class StyleSheetsNode extends AbstractDesignElementResource {
	
	public StyleSheetsNode(IProject project) {
		super(project);
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object o) {
		return Activator.getDefault().getImageRegistry().getDescriptor(Activator.ICON_FONT);
	}
	
	@Override
	public String getLabel() {
		return "Style Sheets";
	}
	@Override
	public int getIndex() {
		return 3;
	}

	@Override
	public String getDesignElementPath() {
		return "Resources/StyleSheets";
	}

	@Override
	public Predicate<IFile> getFilter() {
		return f -> f.getFileExtension().equals("css");
	}

}
