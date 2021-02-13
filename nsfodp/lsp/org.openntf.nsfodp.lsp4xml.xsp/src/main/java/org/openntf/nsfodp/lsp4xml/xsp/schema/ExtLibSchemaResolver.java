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

public class ExtLibSchemaResolver extends AbstractSchemaResolver {
	public static final String NAMESPACE = "http://www.ibm.com/xsp/coreex"; //$NON-NLS-1$
	public static final String SCHEMA_NAME = "xe"; //$NON-NLS-1$
	
	public ExtLibSchemaResolver() {
		super(NAMESPACE, SCHEMA_NAME);
	}
}
