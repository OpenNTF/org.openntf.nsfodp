/**
 * Copyright © 2018-2019 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.nsfodp.eclipse.ui.resources;

import java.text.MessageFormat;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.openntf.nsfodp.eclipse.Activator;
import org.openntf.nsfodp.eclipse.ui.Messages;

public class DesignElementNode extends WorkbenchContentProvider implements IWorkbenchAdapter, IAdaptable {
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
			DesignElementType type = getType();
			IFolder resourceDir = getFolder();
			try {
				if(resourceDir.exists()) {
					return Stream.of(resourceDir.members())
						.filter(IFile.class::isInstance)
						.map(IFile.class::cast)
						.filter(type.getFilter())
						.toArray(Object[]::new);
				} else {
					return new Object[0];
				}
			} catch (CoreException e) {
				log.log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), MessageFormat.format(Messages.DesignElementNode_ExceptionWhileEnumerating, getType().getLabel()), e));
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
	
	protected IFolder getFolder() {
		// TODO look up odp dir from config
		//MavenProject mp = MavenPlugin.getMavenProjectRegistry().getProject(project).getMavenProject();
		IFolder odpDir = project.getFolder("odp"); //$NON-NLS-1$
		return odpDir.getFolder(getType().getDesignElementPath());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		if(!getType().isContainer()) {
			if(IFolder.class.equals(clazz) || IContainer.class.equals(clazz) || IResource.class.equals(clazz)) {
				IFolder folder = getFolder();
				if(!folder.exists()) {
					try {
						folder.create(false, false, null);
					} catch (CoreException e) {
						Activator.logError("Error auto-creating folder " + folder, e); //$NON-NLS-1$
					}
				}
				return (T)folder;
			}
		}
		return null;
	}
}
