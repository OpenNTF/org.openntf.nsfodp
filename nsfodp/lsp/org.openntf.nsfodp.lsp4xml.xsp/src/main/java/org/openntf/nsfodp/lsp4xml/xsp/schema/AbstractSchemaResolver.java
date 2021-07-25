/**
 * Copyright © 2018-2021 Jesse Gallagher
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
import java.net.URI;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.uriresolver.URIResolverExtension;
import org.openntf.nsfodp.lsp4xml.xsp.XspPlugin;

public abstract class AbstractSchemaResolver implements URIResolverExtension {
	private static final Logger log = XspPlugin.log;
	private final String namespace;
	private final String schemaName;
	private URI schemaLoc;
	
	public AbstractSchemaResolver(String namespace, String schemaName) {
		if(log.isLoggable(Level.INFO)) {
			log.info(MessageFormat.format("{0} initialize", getClass().getName()));
		}
		this.namespace = namespace;
		this.schemaName = schemaName;
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if(log.isLoggable(Level.FINE)) {
			log.fine(MessageFormat.format("resolve for publicId={0}, systemId={1}, baseLocation={2}", publicId, systemId, baseLocation));
		}
		if(namespace.equals(systemId) || namespace.equals(publicId)) {
			if(log.isLoggable(Level.FINE)) {
				log.fine(MessageFormat.format("got for {0}", namespace));
			}
			return getSchemaUri().toString();
		}
		return null;
	}
	
	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		if(log.isLoggable(Level.FINE)) {
			log.fine(MessageFormat.format("resolveEntity for {0}", resourceIdentifier));
		}
		if(namespace.equals(resourceIdentifier.getNamespace())) {
			return new XMLInputSource(namespace, getSchemaUri().toString(), getSchemaUri().toString());
		}
		return null;
	}

	private synchronized URI getSchemaUri() {
		if(this.schemaLoc == null) {
			try {
				// TODO resolve from xsp.properties if set
				String version = "10.0.1"; //$NON-NLS-1$
				this.schemaLoc = getClass().getResource(MessageFormat.format("/components/{0}/{1}.xsd", version, schemaName)).toURI(); //$NON-NLS-1$
			} catch (Exception e) {
				if(log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Exception when reading schema file", e);
				}
				throw new RuntimeException(e);
			}
		}
		return schemaLoc;
	}
}
