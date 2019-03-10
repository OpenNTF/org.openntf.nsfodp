package org.openntf.nsfodp.eclipse.resources;

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

public class DesignElementNode implements IWorkbenchAdapter {
	private static final ILog log = Activator.log;

	private final IProject project;
	private final DesignElementType type;
	private DesignElementNode[] children;

	public DesignElementNode(IProject project, DesignElementType type) {
		this.project = project;
		this.type = type;
	}

	@Override
	public Object[] getChildren(Object o) {
		if(this.children == null) {
			// TODO look up odp dir from config
			//MavenProject mp = MavenPlugin.getMavenProjectRegistry().getProject(project).getMavenProject();
			
			DesignElementType type = getType();
			IFolder odpDir = project.getFolder("odp");
			IFolder resourceDir = odpDir.getFolder(type.getDesignElementPath());
			try {
				return Stream.of(resourceDir.members())
					.filter(IFile.class::isInstance)
					.map(IFile.class::cast)
					.filter(type.getFilter())
					.toArray(Object[]::new);
			} catch (CoreException e) {
				log.log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Exception while enumerating " + getType().getLabel(), e));
				return new Object[0];
			}
		} else {
			return children;
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object o) {
		return Activator.getIconDescriptor(getType().getIcon());
	}

	@Override
	public String getLabel(Object o) {
		return getType().getLabel();
	}

	@Override
	public Object getParent(Object o) {
		return project;
	}

	public DesignElementType getType() {
		return type;
	}
	
	public DesignElementNode children(DesignElementNode... children) {
		this.children = children;
		return this;
	}
}
