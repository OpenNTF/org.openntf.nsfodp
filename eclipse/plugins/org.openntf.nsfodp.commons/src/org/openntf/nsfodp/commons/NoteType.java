/**
 * Copyright © 2018-2019 Jesse Gallagher
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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents the various types of notes found in an NSF, with information
 * about their ODP representations.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum NoteType {
	/** The icon note is also represented in "database.properties" */
	IconNote(Paths.get("Resources", "IconNote"), true), //$NON-NLS-1$ //$NON-NLS-2$
	DBIcon(Paths.get("AppProperties", "$DBIcon"), true), //$NON-NLS-1$ //$NON-NLS-2$
	DBScript(Paths.get("Code", "dbscript.lsdb"), true), //$NON-NLS-1$ //$NON-NLS-2$
	XSPDesignProperties(Paths.get("AppProperties"), true), //$NON-NLS-1$
	Java(null, Paths.get("Code", "Java"), false, ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN), //$NON-NLS-1$ //$NON-NLS-2$
	JavaScriptLibrary("js", Paths.get("Code", "ScriptLibraries"), false, JAVASCRIPTLIBRARY_CODE, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptLibrary("lss", Paths.get("Code", "ScriptLibraries"), false, SCRIPTLIB_ITEM_NAME, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaLibrary("javalib", Paths.get("Code", "ScriptLibraries"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CustomControl(null, Paths.get("CustomControls"), false, ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN), //$NON-NLS-1$
	CustomControlProperties(null, Paths.get("CustomControls"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$
	XPage(null, Paths.get("XPages"), false, ITEM_NAME_FILE_DATA, JAVA_ITEM_IGNORE_PATTERN), //$NON-NLS-1$
	XPageProperties(null, Paths.get("XPages"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$
	Form("form", Paths.get("Forms"), false), //$NON-NLS-1$ //$NON-NLS-2$
	Frameset("frameset", Paths.get("Framesets"), false), //$NON-NLS-1$ //$NON-NLS-2$
	ServerJavaScriptLibrary("jss", Paths.get("Code", "ScriptLibraries"), false, SERVER_JAVASCRIPTLIBRARY_CODE, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Subform("subform", Paths.get("SharedElements", "Subforms"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Page("page", Paths.get("Pages"), false), //$NON-NLS-1$ //$NON-NLS-2$
	AboutDocument(Paths.get("Resources", "AboutDocument"), true), //$NON-NLS-1$ //$NON-NLS-2$
	FileResource(null, Paths.get("Resources", "Files"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	ImageResource(null, Paths.get("Resources", "Images"), false, ITEM_NAME_IMAGE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	StyleSheet("css", Paths.get("Resources", "StyleSheets"), false, ITEM_NAME_STYLE_SHEET_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Theme(null, Paths.get("Resources", "Themes"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	UsingDocument(Paths.get("Resources", "UsingDocument"), true), //$NON-NLS-1$ //$NON-NLS-2$
	SharedField("field", Paths.get("SharedElements", "Fields"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Outline("outline", Paths.get("SharedElements", "Outlines"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	View("view", Paths.get("Views"), false), //$NON-NLS-1$ //$NON-NLS-2$
	JavaAgent("ja", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	ImportedJavaAgent("ija", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptAgent("lsa", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Folder("folder", Paths.get("Folders"), false), //$NON-NLS-1$ //$NON-NLS-2$
	SharedColumn("column", Paths.get("SharedElements", "Columns"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Jar(null, Paths.get("Code", "Jars"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	JavaWebService("jws", Paths.get("Code", "WebServices"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebService("lws", Paths.get("Code", "WebServices"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SharedActions(Paths.get("Code", "actions", "Shared Actions"), true), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SimpleActionAgent("aa", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	FormulaAgent("fa", Paths.get("Code", "Agents"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Navigator("navigator", Paths.get("SharedElements", "Navigators"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LotusScriptWebServiceConsumer("lswsc", Paths.get("Code", "WebServiceConsumer"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	JavaWebServiceConsumer("javalib", Paths.get("Code", "WebServiceConsumer"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	WebContentFile(null, Paths.get("WebContent"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$
	DataConnection("dcr", Paths.get("Data", "DataConnections"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DB2AccessView("db2v", Paths.get("Data", "DB2AccessViews"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Applet("applet", Paths.get("Resources", "Applets"), false), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CompositeApplication("xml", Paths.get("CompositeApplications", "Applications"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	CompositeComponent("component", Paths.get("CompositeApplications", "Components"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	WiringProperties(null, Paths.get("CompositeApplications", "WiringProperties"), false, ITEM_NAME_FILE_DATA, null), //$NON-NLS-1$ //$NON-NLS-2$
	/** e.g. plugin.xml, etc. */
	GenericFile(Paths.get("."), false), //$NON-NLS-1$
	/** This is wrapped up into the DB properties */
	ACL(null, null, false),
	DesignCollection(null, null, false),
	Unknown(null, null, false);
	
	
	
	/**
	 * The extension to use for exported files, or <code>null</code> if there is no
	 * consistent extension to add.
	 */
	public final String extension;
	/**
	 * The relative path for exporting the file, either a directory (for individually-named
	 * elements) or an explicit file path (for singleton elements).
	 * 
	 * {@see #singleton}
	 */
	public final Path path;
	/**
	 * Whether or not there is only one note of this kind in the database
	 */
	public final boolean singleton;
	/**
	 * The item containing file data to export, if applicable
	 */
	public final String fileItem;
	/**
	 * A regular expression to match against item names to ignore during export, if applicable
	 */
	public final String itemNameIgnorePattern;
	
	private NoteType(Path path, boolean singleton) {
		this(null, path, singleton);
	}
	private NoteType(String extension, Path path, boolean singleton) {
		this(extension, path, singleton, null, null);
	}
	private NoteType(String extension, Path path, boolean singleton, String fileItem, String itemNameIgnorePattern) {
		this.extension = extension;
		this.path = path;
		this.singleton = singleton;
		this.fileItem = fileItem;
		this.itemNameIgnorePattern = itemNameIgnorePattern;
	}
}
