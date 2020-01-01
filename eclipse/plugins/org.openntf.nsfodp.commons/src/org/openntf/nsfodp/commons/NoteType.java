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
package org.openntf.nsfodp.commons;

import static org.openntf.nsfodp.commons.h.StdNames.*;
import static org.openntf.nsfodp.commons.NSFODPConstants.*;
import static org.openntf.nsfodp.commons.NoteType.OutputFormat.*;

import java.nio.file.Path;
import java.nio.file.Paths;
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
	IconNote(Paths.get("Resources", "IconNote"), true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	DBIcon(Paths.get("AppProperties", "$DBIcon"), true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	DBScript(Paths.get("Code", "dbscript.lsdb"), true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	XSPDesignProperties(Paths.get("AppProperties", "xspdesign.properties"), true, RAWFILE), //$NON-NLS-1$ //$NON-NLS-2$
	Java(null, Paths.get("Code", "Java"), ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	JavaScriptLibrary("js", Paths.get("Code", "ScriptLibraries"), JAVASCRIPTLIBRARY_CODE, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptLibrary("lss", Paths.get("Code", "ScriptLibraries"), SCRIPTLIB_ITEM_NAME, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaLibrary("javalib", Paths.get("Code", "ScriptLibraries"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CustomControl("xsp", Paths.get("CustomControls"), ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	CustomControlProperties("properties", Paths.get("CustomControls"), ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	XPage("xsp", Paths.get("XPages"), ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	XPageProperties("properties", Paths.get("XPages"), ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	Form("form", Paths.get("Forms"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	Frameset("frameset", Paths.get("Framesets"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	ServerJavaScriptLibrary("jss", Paths.get("Code", "ScriptLibraries"), SERVER_JAVASCRIPTLIBRARY_CODE, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Subform("subform", Paths.get("SharedElements", "Subforms"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Page("page", Paths.get("Pages"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	AboutDocument(Paths.get("Resources", "AboutDocument"), true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	FileResource(null, Paths.get("Resources", "Files"), ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	ImageResource(null, Paths.get("Resources", "Images"), ITEM_NAME_IMAGE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	StyleSheet("css", Paths.get("Resources", "StyleSheets"), ITEM_NAME_STYLE_SHEET_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Theme(null, Paths.get("Resources", "Themes"), ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	UsingDocument(Paths.get("Resources", "UsingDocument"), true, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	SharedField("field", Paths.get("SharedElements", "Fields"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Outline("outline", Paths.get("SharedElements", "Outlines"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	View("view", Paths.get("Views"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	JavaAgent("ja", Paths.get("Code", "Agents"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	ImportedJavaAgent("ija", Paths.get("Code", "Agents"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptAgent("lsa", Paths.get("Code", "Agents"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Folder("folder", Paths.get("Folders"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$
	SharedColumn("column", Paths.get("SharedElements", "Columns"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Jar(null, Paths.get("Code", "Jars"), ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$
	JavaWebService("jws", Paths.get("Code", "WebServices"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebService("lws", Paths.get("Code", "WebServices"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SharedActions(Paths.get("Code", "actions", "Shared Actions"), true, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SimpleActionAgent("aa", Paths.get("Code", "Agents"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	FormulaAgent("fa", Paths.get("Code", "Agents"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Navigator("navigator", Paths.get("SharedElements", "Navigators"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebServiceConsumer("lswsc", Paths.get("Code", "WebServiceConsumer"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaWebServiceConsumer("javalib", Paths.get("Code", "WebServiceConsumer"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	WebContentFile(null, Paths.get("WebContent"), ITEM_NAME_FILE_DATA, null, false, RAWFILE), //$NON-NLS-1$
	DataConnection("dcr", Paths.get("Data", "DataConnections"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DB2AccessView("db2v", Paths.get("Data", "DB2AccessViews"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Applet("applet", Paths.get("Resources", "Applets"), false, DXL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CompositeApplication("xml", Paths.get("CompositeApplications", "Applications"), ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CompositeComponent("component", Paths.get("CompositeApplications", "Components"), ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	WiringProperties("wsdl", Paths.get("CompositeApplications", "WiringProperties"), ITEM_NAME_FILE_DATA, null, false, METADATA), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	/** e.g. plugin.xml, etc. */
	GenericFile(Paths.get("."), false, RAWFILE), //$NON-NLS-1$
	/** This is wrapped up into the DB properties */
	ACL,
	DesignCollection,
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
	private final Path path;
	private final String fileItem;
	private final Pattern itemNameIgnorePattern;
	private final OutputFormat outputFormat;
	private final boolean singleton;
	
	private NoteType() {
		this.inOdp = false;
		this.extension = null;
		this.path = null;
		this.fileItem = null;
		this.itemNameIgnorePattern = null;
		this.outputFormat = null;
		this.singleton = false;
	}
	private NoteType(Path path, boolean singleton, OutputFormat outputFormat) {
		this(null, path, singleton, outputFormat);
	}
	private NoteType(String extension, Path path, boolean singleton, OutputFormat outputFormat) {
		this(extension, path, null, null, singleton, outputFormat);
	}
	private NoteType(String extension, Path path, String fileItem, String itemNameIgnorePattern, boolean singleton, OutputFormat outputFormat) {
		this.inOdp = true;
		this.extension = extension;
		this.path = path;
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
	 * @see #isSingleton()
	 */
	public Path getPath() {
		return path;
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
