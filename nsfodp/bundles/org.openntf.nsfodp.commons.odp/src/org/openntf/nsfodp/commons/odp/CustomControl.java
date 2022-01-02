/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.nsfodp.commons.odp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.openntf.nsfodp.commons.odp.util.ODPUtil;
import org.w3c.dom.Document;

/**
 * This class represents the files that make up a custom control: the XSP
 * source, the xsp-config file, and the DXL metadata.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class CustomControl extends XPage {
	public static final String EXT_XSPCONFIG = ".xsp-config"; //$NON-NLS-1$
	
	private final Path xspConfigFile;
	
	public CustomControl(Path xspSourceFile) {
		super(xspSourceFile);
		String fileName = xspSourceFile.getFileName().toString();
		String baseName = fileName.substring(0, fileName.length()-EXT_XSP.length());
		this.xspConfigFile = xspSourceFile.getParent().resolve(baseName+EXT_XSPCONFIG);
	}
	
	public Path getXspConfigFile() {
		return xspConfigFile;
	}
	
	public String getXspConfigSource() {
		return ODPUtil.readFile(xspConfigFile);
	}
	
	public Optional<Document> getXspConfig() {
		if(Files.isRegularFile(xspConfigFile)) {
			return Optional.ofNullable(ODPUtil.readXml(xspConfigFile));
		} else {
			return Optional.empty();
		}
	}
	
	public String getControlName() {
		return this.getPageBaseName();
	}
}
