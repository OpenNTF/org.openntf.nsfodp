package org.openntf.nsfodp.eclipse.resources;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.openntf.nsfodp.eclipse.Activator;

public abstract class AbstractDesignElementResource implements IWorkbenchAdapter {
	private static final ILog log = Activator.log;

	private final IProject project;

	public AbstractDesignElementResource(IProject project) {
		this.project = project;
	}

	@Override
	public Object[] getChildren(Object o) {
		// TODO look up odp dir from config
		//MavenProject mp = MavenPlugin.getMavenProjectRegistry().getProject(project).getMavenProject();
		
		IFolder odpDir = project.getFolder("odp");
		IFolder resourceDir = odpDir.getFolder(getDesignElementPath());
		try {
			return Stream.of(resourceDir.members())
				.filter(IFile.class::isInstance)
				.map(IFile.class::cast)
				.filter(getFilter())
				.toArray(Object[]::new);
		} catch (CoreException e) {
			log.log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Exception while enumerating XPages", e));
			return new Object[0];
		}
	}

	@Override
	public abstract ImageDescriptor getImageDescriptor(Object o);

	@Override
	public String getLabel(Object o) {
		return getLabel();
	}

	@Override
	public Object getParent(Object o) {
		return project;
	}
	

	public abstract String getLabel();
	public abstract int getIndex();
	public abstract String getDesignElementPath();
	public abstract Predicate<IFile> getFilter();
}
