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
package org.openntf.nsfodp.lsp4xml.schemas;

import java.io.IOException;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.uriresolver.URIResolverExtension;

public class DominoSchemasResolver implements URIResolverExtension {
	public static final String DXL_NS = "http://www.lotus.com/dxl"; //$NON-NLS-1$
	private static final String SCHEMA_NAME = "domino_10_0_1.xsd"; //$NON-NLS-1$
	
	private String schemaLoc;

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if(DXL_NS.equals(systemId) || DXL_NS.equals(publicId)) {
			return getSchemaUri();
		}
		return null;
	}
	
	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		if(DXL_NS.equals(resourceIdentifier.getNamespace())) {
			return new XMLInputSource(DXL_NS, getSchemaUri(), getSchemaUri());
		}
		return null;
	}
	
	private synchronized String getSchemaUri() {
		if(this.schemaLoc == null) {
			this.schemaLoc = getClass().getResource("/dominoschemas/" + SCHEMA_NAME).toString(); //$NON-NLS-1$
		}
		return schemaLoc;
	}
}
