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

import static org.openntf.nsfodp.eclipse.ui.resources.DesignElementType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.openntf.nsfodp.eclipse.Activator;
import org.openntf.nsfodp.eclipse.nature.OnDiskProjectNature;

@SuppressWarnings({"unchecked", "rawtypes"})
public class DesignElementsContentProvider extends BaseWorkbenchContentProvider implements IPipelinedTreeContentProvider {

	private static final ILog log = Activator.log;

	@Override
	public void init(ICommonContentExtensionSite config) {
	}

	@Override
	public void restoreState(IMemento memento) {
	}

	@Override
	public void saveState(IMemento memento) {
	}

	@Override
	public void getPipelinedChildren(Object parent, Set currentChildren) {
		if (parent instanceof IProject) {
			IProject project = (IProject) parent;
			if (project.isAccessible()) {
				try {
					if (!project.hasNature(OnDiskProjectNature.ID)) {
						return;
					}
					
					List newChildren = new ArrayList<>(currentChildren.size()+1);
					newChildren.addAll(Arrays.asList(
						new DesignElementNode(project, Forms),
						new DesignElementNode(project, Views),
						new DesignElementNode(project, Folders),
						new DesignElementNode(project, XPages),
						new DesignElementNode(project, CustomControls),
						new DesignElementNode(project, Framesets),
						new DesignElementNode(project, Pages),
						new DesignElementNode(project, SharedElements).children(
							new DesignElementNode(project, Subforms),
							new DesignElementNode(project, Fields),
							new DesignElementNode(project, Columns),
							new DesignElementNode(project, Outlines),
							new DesignElementNode(project, Navigators)
						),
						new DesignElementNode(project, Code).children(
							new DesignElementNode(project, Agents),
							new DesignElementNode(project, SharedActions),
							new DesignElementNode(project, ScriptLibraries),
							new DesignElementNode(project, DatabaseScript),
							new DesignElementNode(project, WebServiceProviders),
							new DesignElementNode(project, WebServiceConsumers)
						),
						new DesignElementNode(project, Data).children(
							new DesignElementNode(project, DataConnections),
							new DesignElementNode(project, DB2AccessViews)
						),
						new DesignElementNode(project, Resources).children(
							new DesignElementNode(project, Images),
							new DesignElementNode(project, Files),
							new DesignElementNode(project, Applets),
							new DesignElementNode(project, StyleSheets),
							new DesignElementNode(project, Themes),
							new DesignElementNode(project, AboutDocument),
							new DesignElementNode(project, UsingDocument),
							new DesignElementNode(project, Icon)
						),
						new DesignElementNode(project, CompositeApplications).children(
							new DesignElementNode(project, WiringProperties),
							new DesignElementNode(project, Applications),
							new DesignElementNode(project, Components)
						),
						new DesignElementNode(project, ApplicationConfiguration).children(
							new DesignElementNode(project, ApplicationProperties),
							new DesignElementNode(project, XspProperties),
							new DesignElementNode(project, FacesConfig)
						)
					));
		            newChildren.addAll(currentChildren);
		            currentChildren.clear();
		            currentChildren.addAll(newChildren);
				} catch (CoreException ex) {
					log.log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Error displaying ODP content", ex));
				}
			}
		}
	}

	@Override
	public void getPipelinedElements(Object element, Set currentElements) {

	}

	@Override
	public Object getPipelinedParent(Object element, Object suggestedParent) {
		return suggestedParent;
	}

	@Override
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification addModification) {
		return addModification;
	}

	@Override
	public boolean interceptRefresh(PipelinedViewerUpdate refreshSynchronization) {
		return false;
	}

	@Override
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification removeModification) {
		return removeModification;
	}

	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate updateSynchronization) {
		return false;
	}

}
