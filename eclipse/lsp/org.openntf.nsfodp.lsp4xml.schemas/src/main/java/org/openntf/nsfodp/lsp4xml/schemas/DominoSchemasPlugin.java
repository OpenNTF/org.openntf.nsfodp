package org.openntf.nsfodp.lsp4xml.schemas;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

/**
 * LSP4XML extension to provide access to the Domino DXL schemas.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class DominoSchemasPlugin implements IXMLExtension {
	
	private final DominoSchemasResolver resolver = new DominoSchemasResolver();

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.getResolverExtensionManager().registerResolver(resolver);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.getResolverExtensionManager().unregisterResolver(resolver);
	}

	@Override
	public void doSave(ISaveContext context) {
		
	}

}
