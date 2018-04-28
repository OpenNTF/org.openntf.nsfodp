/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.nsfodp.eclipse.contentassist;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.eclipse.nature.OnDiskProjectNature;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public enum ContentAssistUtil {
	;

	static IProject getActiveProject() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = window.getActivePage();
	
		IEditorPart activeEditor = activePage.getActiveEditor();
	
		IProject project = null;
		if (activeEditor != null) {
			IEditorInput input = activeEditor.getEditorInput();
	
			project = input.getAdapter(IProject.class);
			if (project == null) {
				IResource resource = input.getAdapter(IResource.class);
				if (resource != null) {
					project = resource.getProject();
				}
			}
		}
		return project;
	}

	static Element getRootElement(Node node) {
		Node parent = node;
		while (parent.getParentNode() != null && parent.getParentNode().getNodeType() == 1) {
			parent = parent.getParentNode();
		}
		return (Element) parent;
	}

	static boolean isXsp(CompletionProposalInvocationContext context) {
		Node selectedNode = (Node) ContentAssistUtils.getNodeAt(context.getViewer(), context.getInvocationOffset());
		Element root = getRootElement(selectedNode);
		if (!NSFODPConstants.XP_NS.equals(root.getNamespaceURI())) {
			return false;
		}
		return true;
	}

	static boolean isTargetProject() {
		IProject project = getActiveProject();
		if (project == null) {
			return false;
		}
		// Make sure we're working with an ODP project
		try {
			if (!project.hasNature(OnDiskProjectNature.ID)) {
				return false;
			}
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	
		IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mavenProject = projectManager.getProject(project);
		if (mavenProject == null) {
			return false;
		}
	
		return true;
	}
}
