/*
 * © Copyright Jesse Gallagher 2018
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.util.ODPUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

/**
 * Represents an On-Disk Project version of an NSF.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class OnDiskProject {
	private final File baseDir;
	
	public OnDiskProject(File baseDirectory) {
		this.baseDir = Objects.requireNonNull(baseDirectory);
	}
	
	public File getBaseDirectory() {
		return baseDir;
	}
	
	public File getClasspathFile() {
		File classpath = new File(baseDir, ".classpath");
		if(!classpath.exists()) {
			throw new IllegalStateException("Classpath file does not exist: " + classpath.getAbsolutePath());
		}
		if(!classpath.isFile()) {
			throw new IllegalStateException("Classpath file is not a file: " + classpath.getAbsolutePath());
		}
		return classpath;
	}
	
	public File getPluginFile() {
		File pluginXml = new File(baseDir, "plugin.xml");
		if(!pluginXml.exists()) {
			throw new IllegalStateException("Plugin file does not exist: " + pluginXml.getAbsolutePath());
		}
		if(!pluginXml.isFile()) {
			throw new IllegalStateException("Plugin file is not a file: " + pluginXml.getAbsolutePath());
		}
		return pluginXml;
	}
	
	public List<String> getRequiredBundles() throws XMLException {
		// TODO adapt to FP10 MANIFEST.MF style?
		Document pluginXml = ODPUtil.readXml(getPluginFile());
		return Arrays.stream(DOMUtil.evaluateXPath(pluginXml, "/plugin/requires/import").getNodes())
			.map(Element.class::cast)
			.map(el -> el.getAttribute("plugin"))
			.collect(Collectors.toList());
	}
}
