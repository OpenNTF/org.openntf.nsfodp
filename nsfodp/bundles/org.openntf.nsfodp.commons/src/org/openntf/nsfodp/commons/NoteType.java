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
package org.openntf.nsfodp.commons;

import static org.openntf.nsfodp.commons.h.StdNames.*;
import static org.openntf.nsfodp.commons.NSFODPConstants.*;
import static org.openntf.nsfodp.commons.NoteType.OutputFormat.*;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Represents the various types of notes found in an NSF, with information
 * about their ODP representations.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum NoteType {
	/** The icon note is also represented in "database.properties" */
	IconNote(new String[] { "Resources", "IconNote" }, true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	DBIcon(new String[] { "AppProperties", "$DBIcon" }, true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	DBScript(new String[] { "Code", "dbscript.lsdb" }, true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	XSPDesignProperties(new String[] { "AppProperties", "xspdesign.properties" }, true, RAWFILE), //$NON-NLS-1$ //$NON-NLS-2$
	Java(null, new String[] { "Code", "Java" }, ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	JavaScriptLibrary("js", new String[] { "Code", "ScriptLibraries" }, JAVASCRIPTLIBRARY_CODE, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptLibrary("lss", new String[] { "Code", "ScriptLibraries" }, SCRIPTLIB_ITEM_NAME, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaLibrary("javalib", new String[] { "Code", "ScriptLibraries" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CustomControl("xsp", new String[] { "CustomControls" }, ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	CustomControlProperties("properties", new String[] { "CustomControls" }, ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	XPage("xsp", new String[] { "XPages" }, ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	XPageProperties("properties", new String[] { "XPages" }, ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	Form("form", new String[] { "Forms" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	Frameset("frameset", new String[] { "Framesets" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	ServerJavaScriptLibrary("jss", new String[] { "Code", "ScriptLibraries" }, SERVER_JAVASCRIPTLIBRARY_CODE, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Subform("subform", new String[] { "SharedElements", "Subforms" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Page("page", new String[] { "Pages" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	AboutDocument(new String[] { "Resources", "AboutDocument" }, true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	FileResource(null, new String[] { "Resources", "Files" }, ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	ImageResource(null, new String[] { "Resources", "Images" }, ITEM_NAME_IMAGE_DATA, IMAGE_RESOURCE_IGNORE_PATTERN, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	StyleSheet("css", new String[] { "Resources", "StyleSheets" }, ITEM_NAME_STYLE_SHEET_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Theme(null, new String[] { "Resources", "Themes" }, ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	UsingDocument(new String[] { "Resources", "UsingDocument" }, true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	SharedField("field", new String[] { "SharedElements", "Fields" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Outline("outline", new String[] { "SharedElements", "Outlines" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	View("view", new String[] { "Views" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	JavaAgent("ja", new String[] { "Code", "Agents" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	ImportedJavaAgent("ija", new String[] { "Code", "Agents" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptAgent("lsa", new String[] { "Code", "Agents" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Folder("folder", new String[] { "Folders" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	SharedColumn("column", new String[] { "SharedElements", "Columns" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Jar(null, new String[] { "Code", "Jars" }, ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	JavaWebService("jws", new String[] { "Code", "WebServices" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebService("lws", new String[] { "Code", "WebServices" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SharedActions(new String[] { "Code", "actions", "Shared Actions" }, true, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SimpleActionAgent("aa", new String[] { "Code", "Agents" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	FormulaAgent("fa", new String[] { "Code", "Agents" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Navigator("navigator", new String[] { "SharedElements", "Navigators" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebServiceConsumer("lswsc", new String[] { "Code", "WebServiceConsumer" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaWebServiceConsumer("javalib", new String[] { "Code", "WebServiceConsumer" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	WebContentFile(null, new String[] { "WebContent" }, ITEM_NAME_FILE_DATA, null, false, RAWFILE), //$NON-NLS-1$
	DataConnection("dcr", new String[] { "Data", "DataConnections" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DB2AccessView("db2v", new String[] { "Data", "DB2AccessViews" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Applet("applet", new String[] { "Resources", "Applets" }, false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CompositeApplication("xml", new String[] { "CompositeApplications", "Applications" }, ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CompositeComponent("component", new String[] { "CompositeApplications", "Components" }, ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	WiringProperties("wsdl", new String[] { "CompositeApplications", "WiringProperties" }, ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	/** e.g. plugin.xml, etc. */
	GenericFile(new String[] { "." }, false, RAWFILE), //$NON-NLS-1$
	/** This is wrapped up into the DB properties */
	ACL,
	DesignCollection,
	AgentData,
	ReplicationFormula,
	Unknown;
	
	/**
	 * Characteristics to differentiate the handling of given note types.
	 * 
	 * @author Jesse Gallagher
	 * @since 2.5.0
	 */
	public enum OutputFormat {
		/**
		 * Indicates that the design element is stored as raw file data with an
		 * accompanying ".metadata" file in an ODP.
		 */
		METADATA,
		/**
		 * Indicates that the design element is stored as raw file data but without
		 * an accompanying ".metadata" file in an ODP.
		 */
		RAWFILE,
		/**
		 * Indicates that the design element is stored as a single DXL file.
		 */
		DXL
	}
	
	private final boolean inOdp;
	
	private final String extension;
	private final String[] pathComponents;
	private final String fileItem;
	private final Pattern itemNameIgnorePattern;
	private final OutputFormat outputFormat;
	private final boolean singleton;
	
	private NoteType() {
		this.inOdp = false;
		this.extension = null;
		this.pathComponents = null;
		this.fileItem = ITEM_NAME_FILE_DATA;
		this.itemNameIgnorePattern = null;
		this.outputFormat = null;
		this.singleton = false;
	}
	private NoteType(String[] pathComponents, boolean singleton, OutputFormat outputFormat) {
		this(null, pathComponents, singleton, outputFormat);
	}
	private NoteType(String extension, String[] pathComponents, boolean singleton, OutputFormat outputFormat) {
		this(extension, pathComponents, ITEM_NAME_FILE_DATA, null, singleton, outputFormat);
	}
	private NoteType(String extension, String[] pathComponents, String fileItem, String itemNameIgnorePattern, boolean singleton, OutputFormat outputFormat) {
		this.inOdp = true;
		this.extension = extension;
		this.pathComponents = pathComponents;
		this.fileItem = fileItem;
		this.itemNameIgnorePattern = itemNameIgnorePattern == null ? null : Pattern.compile(itemNameIgnorePattern);
		this.singleton = singleton;
		this.outputFormat = outputFormat;
	}
	
	/**
	 * Whether or not the note type is included in an ODP at all
	 */
	public boolean isInOdp() {
		return inOdp;
	}
	/**
	 * Whether or not there is only one note of this kind in the database
	 */
	public boolean isSingleton() {
		return singleton;
	}
	/**
	 * The relative path for exporting the file, either a directory (for individually-named
	 * elements) or an explicit file path (for singleton elements).
	 * 
	 * @param contextFileSystem the context {@link FileSystem} for generating paths
	 * @return a relative {@link Path} instance for the type
	 * @see #isSingleton()
	 */
	public Path getPath(FileSystem contextFileSystem) {
		return contextFileSystem.getPath(this.pathComponents[0], Arrays.copyOfRange(pathComponents, 1, pathComponents.length));
	}
	/**
	 * The item containing file data to export, if applicable
	 */
	public String getFileItem() {
		return fileItem;
	}
	/**
	 * A regular expression to match against item names to ignore during export, if applicable
	 */
	public Pattern getItemNameIgnorePattern() {
		return itemNameIgnorePattern;
	}

	/**
	 * The extension to use for exported files, or <code>null</code> if there is no
	 * consistent extension to add.
	 */
	public String getExtension() {
		return extension;
	}
	
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}
}
