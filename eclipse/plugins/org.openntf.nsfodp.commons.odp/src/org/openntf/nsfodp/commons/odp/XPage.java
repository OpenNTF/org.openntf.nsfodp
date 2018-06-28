/**
 * Copyright © 2018 Jesse Gallagher
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

import java.nio.file.Path;

/**
 * This class represents an XPage source file and its accompanying
 * DXL metadata file.
 * 
 * @author Jesse Gallagher
 *
 */
public class XPage extends AbstractSourceDesignElement {
	public static final String EXT_XSP = ".xsp";
	public static final String PACKAGE_XSP = "xsp";
	
	public XPage(Path xspSourceFile) {
		super(xspSourceFile);
	}
	
	public String getJavaClassName() {
		return PACKAGE_XSP + '.' + getJavaClassSimpleName();
	}
	
	public String getJavaClassSimpleName() {
		String pageName = getPageBaseName();
		String capitalized = pageName.substring(0, 1).toUpperCase() + pageName.substring(1);
		
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < capitalized.length(); i++) {
			char c = capitalized.charAt(i);
			if(!(Character.isAlphabetic(c) || Character.isDigit(c))) {
				result.append('_');
				result.append(String.format("%04x", (int)c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}
	
	public String getPageName() {
		return getDataFile().getFileName().toString();
	}
	
	public String getPageBaseName() {
		String fileName = getPageName();
		return fileName.substring(0, fileName.length()-EXT_XSP.length());
	}
}

