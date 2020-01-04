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
package org.openntf.nsfodp.commons.odp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.function.Function;

import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

/**
 * Represents a "file resource"-type element in the ODP, which may be a file resource,
 * stylesheet, or other loose file.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class FileResource extends AbstractSplitDesignElement {
	private final String flags;
	private final String flagsExt;
	private final Function<Path, String> nameProvider;
	private final Function<Path, String> pathProvider;
	private final boolean copyToClasses;
	
	public FileResource(Path dataFile) {
		this(dataFile, null, null, null);
	}
	
	public FileResource(Path dataFile, boolean copyToClasses) {
		super(dataFile);
		this.flags = null;
		this.flagsExt = null;
		this.nameProvider = null;
		this.pathProvider = null;
		this.copyToClasses = copyToClasses;
	}
	
	public FileResource(Path dataFile, String flags, String flagsExt, Function<Path, String> nameProvider) {
		this(dataFile, flags, flagsExt, nameProvider, nameProvider);
	}
	
	public FileResource(Path dataFile, String flags, String flagsExt, Function<Path, String> nameProvider, Function<Path, String> pathProvider) {
		super(dataFile);
		this.flags = flags;
		this.flagsExt = flagsExt;
		this.nameProvider = nameProvider;
		this.copyToClasses = false;
		this.pathProvider = nameProvider;
	}
	
	@Override
	public Document getDxl() throws XMLException, IOException {
		if(Files.isRegularFile(getDxlFile())) {
			return super.getDxl();
		} else {
			if(nameProvider == null) {
				throw new IllegalStateException(MessageFormat.format(Messages.FileResource_noNameProvider, getDataFile()));
			}
			
			Document dxlDoc = DOMUtil.createDocument();
			Element note = DOMUtil.createElement(dxlDoc, "note"); //$NON-NLS-1$
			note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
			note.setAttribute("xmlns", "http://www.lotus.com/dxl"); //$NON-NLS-1$ //$NON-NLS-2$
			if(StringUtil.isNotEmpty(flags)) {
				DXLUtil.writeItemString(dxlDoc, "$Flags", false, flags); //$NON-NLS-1$
			}
			if(StringUtil.isNotEmpty(flagsExt)) {
				DXLUtil.writeItemString(dxlDoc, "$FlagsExt", false, flagsExt); //$NON-NLS-1$
			}
			String title = nameProvider.apply(getDataFile());
			if(StringUtil.isNotEmpty(title)) {
				DXLUtil.writeItemString(dxlDoc, "$TITLE", false, title); //$NON-NLS-1$
			}
			String path = pathProvider.apply(getDataFile());
			if(StringUtil.isNotEmpty(path)) {
				DXLUtil.writeItemString(dxlDoc, "$FileNames", false, path); //$NON-NLS-1$
			}
			
			return attachFileData(dxlDoc);
		}		
	}
	
	public boolean isCopyToClasses() {
		return copyToClasses;
	}
}
