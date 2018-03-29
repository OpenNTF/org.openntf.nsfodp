package org.openntf.xsp.extlibx.bazaar.odpcompiler.eclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class OnDiskProjectNature implements IProjectNature {
	public static final String ID = "org.openntf.xsp.extlibx.bazaar.odp.nature";
	
	private IProject project;

	@Override
	public void configure() throws CoreException {

	}

	@Override
	public void deconfigure() throws CoreException {

	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
