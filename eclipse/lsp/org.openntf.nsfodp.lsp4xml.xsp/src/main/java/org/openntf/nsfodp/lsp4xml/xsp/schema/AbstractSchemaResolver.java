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
package org.openntf.nsfodp.lsp4xml.xsp.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;
import org.openntf.nsfodp.lsp4xml.xsp.XspPlugin;

public abstract class AbstractSchemaResolver implements URIResolverExtension {
	private static final Logger log = XspPlugin.log;
	private final String namespace;
	private final String schemaName;
	private URI tempSchemas;
	
	public AbstractSchemaResolver(String namespace, String schemaName) {
		if(log.isLoggable(Level.INFO)) {
			log.info(getClass().getName() + " initialize");
		}
		this.namespace = namespace;
		this.schemaName = schemaName;
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if(log.isLoggable(Level.FINE)) {
			log.fine("resolve for publicId=" + publicId + ", systemId=" + systemId + ", baseLocation=" + baseLocation);
		}
		if(namespace.equals(systemId) || namespace.equals(publicId)) {
			if(log.isLoggable(Level.FINE)) {
				log.fine("got for " + namespace);
			}
			return getSchemaUri().toString();
		}
		return null;
	}
	
	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		if(log.isLoggable(Level.FINE)) {
			log.fine("resolveEntity for " + resourceIdentifier);
		}
		if(namespace.equals(resourceIdentifier.getNamespace())) {
			return new XMLInputSource(namespace, getSchemaUri().toString(), getSchemaUri().toString());
		}
		return null;
	}

	private synchronized URI getSchemaUri() {
		if(this.tempSchemas == null) {
			try {
				Path tempFile = Files.createTempFile(schemaName, ".xsd"); //$NON-NLS-1$
				tempFile.toFile().deleteOnExit();
				try(InputStream is = getClass().getResourceAsStream("/components/10.0.1/" + schemaName + ".xsd")) { //$NON-NLS-1$
					Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
				}
				if(log.isLoggable(Level.FINE)) {
					log.fine("deployed schema to " + tempFile);
				}
				this.tempSchemas = tempFile.toUri();
			} catch (Exception e) {
				if(log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Exception when deploying temp file", e);
				}
				throw new RuntimeException(e);
			}
		}
		return tempSchemas;
	}
}
