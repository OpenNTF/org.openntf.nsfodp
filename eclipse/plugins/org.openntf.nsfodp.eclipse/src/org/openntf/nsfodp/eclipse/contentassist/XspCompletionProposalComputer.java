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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.openntf.domino.utils.xml.XMLDocument;
import org.openntf.nsfodp.eclipse.Activator;
import org.openntf.nsfodp.eclipse.contentassist.generators.CustomControlProposalGenerator;
import org.openntf.nsfodp.eclipse.contentassist.model.CustomControl;
import org.openntf.nsfodp.eclipse.nature.OnDiskProjectNature;
import org.thymeleaf.extras.eclipse.contentassist.AbstractComputer;
import org.thymeleaf.extras.eclipse.contentassist.autocomplete.generators.AbstractItemProposalGenerator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@SuppressWarnings("restriction")
public class XspCompletionProposalComputer extends AbstractComputer implements ICompletionProposalComputer {

	public static final String XP_NS = "http://www.ibm.com/xsp/core"; //$NON-NLS-1$
	public static final String XC_NS = "http://www.ibm.com/xsp/custom"; //$NON-NLS-1$
	public static final String XS_NS = "http://www.ibm.com/xsp/extlib"; //$NON-NLS-1$
	public static final String XL_NS = "http://www.ibm.com/xsp/labs"; //$NON-NLS-1$
	public static final String BZ_NS = "http://www.ibm.com/xsp/bazaar"; //$NON-NLS-1$

	private static final Map<String, Collection<CustomControl>> CC_TAGS = Collections.synchronizedMap(new HashMap<>());

	private static AbstractItemProposalGenerator<?>[] proposalgenerators = {
		new CustomControlProposalGenerator()
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<?> computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		if(!isTargetProject() || !isXsp(context)) {
			return Collections.emptyList();
		}

		ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		try {
			ITextViewer viewer = context.getViewer();
			IStructuredDocument document = (IStructuredDocument) context.getDocument();
			int cursorposition = context.getInvocationOffset();

			IDOMNode node = (IDOMNode) ContentAssistUtils.getNodeAt(viewer, cursorposition);
			IStructuredDocumentRegion documentregion = ContentAssistUtils.getStructuredDocumentRegion(viewer,
					cursorposition);
			ITextRegion textregion = documentregion.getRegionAtCharacterOffset(cursorposition);

			// Create proposals from the generators given to us by the computers
			for (AbstractItemProposalGenerator<?> proposalgenerator : proposalgenerators) {
				proposals.addAll(proposalgenerator.generateProposals(node, textregion, documentregion, document,
						cursorposition));
			}
		} catch (BadLocationException ex) {
			Activator.logError("Unable to retrieve data at the current document position", ex);
		}

		return proposals;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<?> computeContextInformation(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionEnded() {
		// NOP
	}

	@Override
	public void sessionStarted() {
		// NOP
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************

	private static boolean isTargetProject() {
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

	private boolean isXsp(CompletionProposalInvocationContext context) {
		Node selectedNode = (Node) ContentAssistUtils.getNodeAt(context.getViewer(), context.getInvocationOffset());
		Element root = getRootElement(selectedNode);
		if (!XP_NS.equals(root.getNamespaceURI())) {
			return false;
		}
		return true;
	}

	private static IProject getActiveProject() {
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

	private Element getRootElement(Node node) {
		Node parent = node;
		while (parent.getParentNode() != null && parent.getParentNode().getNodeType() == 1) {
			parent = parent.getParentNode();
		}
		return (Element) parent;
	}
	
	public static Collection<CustomControl> getCustomControls() throws CoreException, SAXException, IOException, ParserConfigurationException {
		return getCustomControls(getActiveProject());
	}

	public static synchronized Collection<CustomControl> getCustomControls(IProject project)
			throws CoreException, SAXException, IOException, ParserConfigurationException {
		String id = project.getFullPath().toString();
		if (!CC_TAGS.containsKey(id)) {
			Set<CustomControl> result = new TreeSet<>();
			IFolder ccFolder = project.getFolder("odp/CustomControls"); // TODO look at configured path //$NON-NLS-1$
			if (ccFolder.exists()) {
				for (IResource member : ccFolder.members()) {
					if (member instanceof IFile) {
						if (member.getName().endsWith(".xsp-config")) { //$NON-NLS-1$
							// Then read in the XML
							XMLDocument doc = new XMLDocument();
							try (InputStream is = ((IFile) member).getContents()) {
								doc.loadInputStream(is);
							}
							String namespaceUri = doc
									.selectSingleNode("/faces-config/faces-config-extension/namespace-uri").getText(); //$NON-NLS-1$
							String prefix = doc.selectSingleNode("/faces-config/faces-config-extension/default-prefix") //$NON-NLS-1$
									.getText();
							String tagName = doc.selectSingleNode("/faces-config/composite-component/composite-name") //$NON-NLS-1$
									.getText();

							result.add(new CustomControl(namespaceUri, prefix, tagName));
						}
					}
				}
			}
			CC_TAGS.put(id, result);
		}
		return CC_TAGS.get(id);
	}
}
