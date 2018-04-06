package org.openntf.nsfodp.compiler.eclipse.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.openntf.nsfodp.compiler.eclipse.Activator;
import org.openntf.nsfodp.compiler.eclipse.nature.OnDiskProjectNature;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// h/t https://github.com/nonblocking/eclipse-xml-completionproposal-demo/blob/master/src/at/nonblocking/xml/completionproposal/demo/FruitsCompletionProposalComputer.java
@SuppressWarnings("restriction")
public class XspCompletionProposalComputer implements ICompletionProposalComputer {
	
	public static final String NS_XSP = "http://www.ibm.com/xsp/core";

	private ILog log = Platform.getLog(Activator.getDefault().getBundle());
	
	@Override
	public List<ICompletionProposal> computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		IProject project = getActiveProject();
		try {
			// Make sure we're working with an ODP project
			if(!project.hasNature(OnDiskProjectNature.ID)) {
				return Collections.emptyList();
			}

			IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
			IMavenProjectFacade mavenProject = projectManager.getProject(project);
			if(mavenProject == null) {
				throw new IllegalStateException("ODP project is not a Maven project");
			}
			
			debug("Inside an XML file!");
			Node selectedNode = (Node) ContentAssistUtils.getNodeAt(context.getViewer(), context.getInvocationOffset());
			Element root = getRootElement(selectedNode);
			if(!NS_XSP.equals(root.getNamespaceURI())) {
				return Collections.emptyList();
			} else {
				debug("it's an XSP file!");
				
				
			}

			return null;
		} catch (Throwable t) {
			log.log(new Status(IStatus.ERROR, "Exception while computing proposals", t.toString(), t));
			return Collections.emptyList();
		}
	}
	
	@Override
	public List<ICompletionProposal> computeContextInformation(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		return Collections.emptyList();
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionEnded() {
	}

	@Override
	public void sessionStarted() {
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
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
		log.log(new Status(IStatus.INFO, message, message));
	}
}
