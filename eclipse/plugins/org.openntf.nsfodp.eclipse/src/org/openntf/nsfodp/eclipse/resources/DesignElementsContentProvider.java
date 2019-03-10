package org.openntf.nsfodp.eclipse.resources;

import java.util.ArrayList;
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
		            newChildren.add(new CustomControlsNode(project));
		            newChildren.add(new ImagesNode(project));
		            newChildren.add(new StyleSheetsNode(project));
		            newChildren.add(new XPagesNode(project));
		            newChildren.addAll(currentChildren);
		            currentChildren.clear();
		            currentChildren.addAll(newChildren);
				} catch (CoreException ex) {
					log.log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Error displaying XPages content", ex));
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
