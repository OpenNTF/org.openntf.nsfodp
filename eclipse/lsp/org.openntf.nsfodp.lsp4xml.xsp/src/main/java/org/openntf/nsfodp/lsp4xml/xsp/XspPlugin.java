package org.openntf.nsfodp.lsp4xml.xsp;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

/**
 * LSP4XML contributor for XSP language syntax.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class XspPlugin implements IXMLExtension {
	
	public static final String EXT_XSP = ".xsp"; //$NON-NLS-1$
	public static final String EXT_XSP_CONFIG = ".xsp-config"; //$NON-NLS-1$
	
	private final ICompletionParticipant completionParticipant;
	
	public XspPlugin() {
		this.completionParticipant = new XspCompletionParticipant();
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.registerCompletionParticipant(completionParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
	}

	@Override
	public void doSave(ISaveContext context) {
		// NOP
	}
	
	public static boolean match(DOMDocument document) {
		return document.getDocumentURI().endsWith(EXT_XSP);
	}

}
