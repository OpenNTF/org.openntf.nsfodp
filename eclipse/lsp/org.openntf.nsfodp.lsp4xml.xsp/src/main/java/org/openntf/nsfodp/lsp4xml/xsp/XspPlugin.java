/**
 * Copyright © 2018-2020 Jesse Gallagher
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
package org.openntf.nsfodp.lsp4xml.xsp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;
import org.openntf.nsfodp.lsp4xml.xsp.completion.XspCompletionParticipant;
import org.openntf.nsfodp.lsp4xml.xsp.schema.ExtLibSchemaResolver;
import org.openntf.nsfodp.lsp4xml.xsp.schema.XspCoreSchemaResolver;

/**
 * LSP4XML contributor for XSP language syntax.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class XspPlugin implements IXMLExtension {
	
	public static final Logger log = Logger.getLogger(XspPlugin.class.getPackage().getName());
	static {
		log.setLevel(Level.FINEST);
	}
	
	public static final String EXT_XSP = ".xsp"; //$NON-NLS-1$
	public static final String EXT_XSP_CONFIG = ".xsp-config"; //$NON-NLS-1$
	
	private final ICompletionParticipant completionParticipant;
	private final URIResolverExtension[] resolvers;
	
	public XspPlugin() {
		log.info(getClass().getName() + " initialize");
		
		this.completionParticipant = new XspCompletionParticipant();
		this.resolvers = new URIResolverExtension[] {
			new XspCoreSchemaResolver(),
			new ExtLibSchemaResolver()
		};
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.registerCompletionParticipant(completionParticipant);
		for(URIResolverExtension resolver : this.resolvers) {
			registry.getResolverExtensionManager().registerResolver(resolver);
		}
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
		for(URIResolverExtension resolver : this.resolvers) {
			registry.getResolverExtensionManager().unregisterResolver(resolver);
		}
	}

	@Override
	public void doSave(ISaveContext context) {
		// NOP
	}
}
