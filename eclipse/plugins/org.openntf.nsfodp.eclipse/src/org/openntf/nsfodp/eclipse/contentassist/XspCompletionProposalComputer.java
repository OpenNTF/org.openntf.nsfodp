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
package org.openntf.nsfodp.compiler.eclipse.contentassist;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.util.DOMUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.html.ui.internal.contentassist.HTMLTagsCompletionProposalComputer;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.AbstractXMLCompletionProposalComputer;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentModelGenerator;
import org.openntf.domino.utils.xml.XMLDocument;
import org.openntf.nsfodp.compiler.eclipse.Activator;
import org.openntf.nsfodp.compiler.eclipse.contentassist.model.CustomControl;
import org.openntf.nsfodp.compiler.eclipse.nature.OnDiskProjectNature;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@SuppressWarnings("restriction")
public class XspCompletionProposalComputer extends HTMLTagsCompletionProposalComputer {

	public static final String XP_NS = "http://www.ibm.com/xsp/core"; //$NON-NLS-1$
	public static final String XC_NS = "http://www.ibm.com/xsp/custom"; //$NON-NLS-1$
	public static final String XS_NS = "http://www.ibm.com/xsp/extlib"; //$NON-NLS-1$
	public static final String XL_NS = "http://www.ibm.com/xsp/labs"; //$NON-NLS-1$
	public static final String BZ_NS = "http://www.ibm.com/xsp/bazaar"; //$NON-NLS-1$

	private ILog log = Platform.getLog(Activator.getDefault().getBundle());
	
	private static final Map<String, Collection<CustomControl>> CC_TAGS = Collections.synchronizedMap(new HashMap<>());


	@Override
	protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition, CompletionProposalInvocationContext context) {
		if(!isTargetProject() || !isXsp(context)) { return; }
		
		IProject project = getActiveProject();

		try {
			Collection<CustomControl> ccs = getCustomControls(project);
			List<String> ccTags = ccs.stream().map(cc -> cc.getPrefix() + ":" + cc.getTagName()).collect(Collectors.toList());
			
			IStructuredModel model = null;
			if(context.getDocument() instanceof IStructuredDocument) {
				model = StructuredModelManager.getModelManager().getModelForRead((IStructuredDocument)context.getDocument());
			}
			if(model != null) {
				IDOMDocument doc = ((IDOMModel) model).getDocument();
				
				for(String tag : ccTags) {
					String proposalText = "<" + tag + "></" + tag + ">";
					CompletionProposal proposal = new CompletionProposal(proposalText, context.getInvocationOffset(), 0, tag.length());
					contentAssistRequest.addProposal(proposal);
				}
			}
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition, CompletionProposalInvocationContext context) {
		if(!isTargetProject() || !isXsp(context)) { return; }
		
	}
	
	@Override
	protected void addTagCloseProposals(ContentAssistRequest contentAssistRequest, CompletionProposalInvocationContext context) {
		if(!isTargetProject() || !isXsp(context)) { return; }
		
	}

	@Override
	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest, CompletionProposalInvocationContext context) {
		// TODO look up control attributes
		super.addAttributeNameProposals(contentAssistRequest, context);
	}

	@Override
	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest, CompletionProposalInvocationContext context) {
		// TODO maybe add EL proposals one day
		super.addAttributeValueProposals(contentAssistRequest, context);
	}

	@Override
	protected void addEmptyDocumentProposals(ContentAssistRequest contentAssistRequest, CompletionProposalInvocationContext context) {
		// No need here
	}

	@Override
	protected void addEntityProposals(ContentAssistRequest contentAssistRequest, ITextRegion completionRegion, IDOMNode treeNode,
			CompletionProposalInvocationContext context) {
		// XSP doesn't actually support HTML entities
	}

	@Override
	protected void addEntityProposals(@SuppressWarnings("rawtypes") Vector proposals, Properties map, String key, int nodeOffset,
			IStructuredDocumentRegion sdRegion, ITextRegion completionRegion, CompletionProposalInvocationContext context) {
		// XSP doesn't actually support HTML entities
	}

	@Override
	protected void addPCDATAProposal(String nodeName, ContentAssistRequest contentAssistRequest, CompletionProposalInvocationContext context) {
		// Could potentially look for EL/SSJS blocks
		super.addPCDATAProposal(nodeName, contentAssistRequest, context);
	}

	@Override
	protected void addStartDocumentProposals(ContentAssistRequest contentAssistRequest, CompletionProposalInvocationContext context) {
		// I guess this could add <xp:view/>, but that's not very important
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
		if(project == null) {
			return false;
		}
		// Make sure we're working with an ODP project
		try {
			if(!project.hasNature(OnDiskProjectNature.ID)) {
				return false;
			}
		} catch(CoreException e) {
			throw new RuntimeException(e);
		}
		
		IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mavenProject = projectManager.getProject(project);
		if(mavenProject == null) {
			return false;
		}
		
		return true;
	}
	
	private boolean isXsp(CompletionProposalInvocationContext context) {
		Node selectedNode = (Node) ContentAssistUtils.getNodeAt(context.getViewer(), context.getInvocationOffset());
		Element root = getRootElement(selectedNode);
		if(!XP_NS.equals(root.getNamespaceURI())) {
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
		while(parent.getParentNode() != null && parent.getParentNode().getNodeType() == 1) {
			parent = parent.getParentNode();
		}
		return (Element)parent;
	}
	
	private void debug(String message) {
		log.log(new Status(IStatus.WARNING, message, message));
	}
	
	private static synchronized Collection<CustomControl> getCustomControls(IProject project) throws CoreException, SAXException, IOException, ParserConfigurationException {
		String id = project.getFullPath().toString();
		if(!CC_TAGS.containsKey(id)) {
			Set<CustomControl> result = new TreeSet<>();
			IFolder ccFolder = project.getFolder("odp/CustomControls"); // TODO look at configured path
			if(ccFolder.exists()) {
				for(IResource member : ccFolder.members()) {
					if(member instanceof IFile) {
						if(member.getName().endsWith(".xsp-config")) {
							// Then read in the XML
							XMLDocument doc = new XMLDocument();
							try(InputStream is = ((IFile) member).getContents()) {
								doc.loadInputStream(is);
							}
							String namespaceUri = doc.selectSingleNode("/faces-config/faces-config-extension/namespace-uri").getText();
							String prefix = doc.selectSingleNode("/faces-config/faces-config-extension/default-prefix").getText();
							String tagName = doc.selectSingleNode("/faces-config/composite-component/composite-name").getText();
							
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
