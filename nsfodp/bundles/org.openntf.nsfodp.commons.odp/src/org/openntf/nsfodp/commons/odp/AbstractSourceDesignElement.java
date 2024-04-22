/**
 * Copyright © 2018-2023 Jesse Gallagher
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.odp.util.ODPUtil;

/**
 * This class represents a text-based source file and its accompanying
 * DXL metadata file.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class AbstractSourceDesignElement extends AbstractSplitDesignElement {
	public AbstractSourceDesignElement(Path dataFile) {
		super(dataFile);
	}

	public String getSource() {
		return ODPUtil.readFile(this.getDataFile());
	}
	
	/**
	 * @return the source of the design element as an {@link InputStream}
	 * @throws IOException if there is a problem opening the underlying file
	 * @since 3.5.0
	 */
	public InputStream getSourceAsStream() throws IOException {
		return NSFODPUtil.newInputStream(this.getDataFile());
	}
}
