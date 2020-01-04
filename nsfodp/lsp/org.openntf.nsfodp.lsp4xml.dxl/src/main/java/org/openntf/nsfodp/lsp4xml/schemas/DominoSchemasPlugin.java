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
package org.openntf.nsfodp.lsp4xml.schemas;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

/**
 * LSP4XML extension to provide access to the Domino DXL schemas.
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
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
