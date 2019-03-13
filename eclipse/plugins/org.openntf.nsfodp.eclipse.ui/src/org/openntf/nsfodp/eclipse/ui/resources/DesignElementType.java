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
package org.openntf.nsfodp.eclipse.ui.resources;

import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.openntf.nsfodp.eclipse.ODPIcon;

public enum DesignElementType {
	Forms("Forms", ODPIcon.FORM, "Forms"),
	Views("Views", ODPIcon.VIEW, "Views"),
	Folders("Folders", ODPIcon.FOLDER, "Folders"),
	XPages("XPages", ODPIcon.XPAGE, "XPages", f -> f.getFileExtension().equals("xsp")),
	CustomControls("Custom Controls", ODPIcon.CUSTOM_CONTROL, "CustomControls", f -> f.getFileExtension().equals("xsp")),
	Framesets("Framesets", ODPIcon.FRAMESET, "Framesets"),
	Pages("Pages", ODPIcon.PAGE, "Pages"),
	SharedElements("Shared Elements", ODPIcon.SHARED_ELEMENTS),
		Subforms("Subforms", ODPIcon.SUBFORM, "SharedElements/Subforms"),
		Fields("Fields", ODPIcon.FIELD, "SharedElements/Fields"),
		Columns("Columns", ODPIcon.COLUMN, "SharedElements/Columns"),
		Outlines("Outlines", ODPIcon.OUTLINE, "SharedElements/Outlines"),
		Navigators("Navigators", ODPIcon.NAVIGATOR, "SharedElements/Navigators"),
	Code("Code", ODPIcon.CODE),
		Agents("Agents", ODPIcon.AGENT, "Code/Agents"),
		SharedActions("Shared Actions", ODPIcon.SHARED_ACTIONS, "Code/actions", f -> f.getName().equals("Shared Actions")),
		ScriptLibraries("Script Libraries", ODPIcon.SCRIPT, "Code/ScriptLibraries"),
		DatabaseScript("DatabaseScript", ODPIcon.DATABASE_SCRIPT, "Code", f -> f.getName().equals("dbscript.lsdb")),
		WebServiceProviders("WebServiceProviders", ODPIcon.WEB_SERVICE_PROVIDER, "Code/WebServices"),
		WebServiceConsumers("WebServiceConsumers", ODPIcon.WEB_SERVICE_CONSUMER, "Code/WebServiceConsumer"),
	Data("Data", ODPIcon.DATA),
		DataConnections("Data Connections", ODPIcon.DATA_CONNECTION, "Data/DataConnections"),
		DB2AccessViews("DB2 Access Views", ODPIcon.DB2_ACCESS_VIEW, "Data/DB2AccessViews"),
	Resources("Resources", ODPIcon.RESOURCES),
		Images("Images", ODPIcon.IMAGE, "Resources/Images"),
		Files("Files", ODPIcon.FILE, "Resources/Files"),
		Applets("Applets", ODPIcon.APPLET, "Resources/Applets"),
		StyleSheets("Style Sheets", ODPIcon.STYLESHEET, "Resources/StyleSheets"),
		Themes("Themes", ODPIcon.THEME, "Resources/Themes"),
		AboutDocument("About Document", ODPIcon.ABOUT_DOCUMENT, "Resources", f -> f.getName().equals("AboutDocument")),
		UsingDocument("Using Document", ODPIcon.USING_DOCUMENT, "Resources", f -> f.getName().equals("UsingDocument")),
		Icon("Icon", ODPIcon.DATABASE_ICON, "AppProperties", f -> f.getName().equals("$DBIcon")),
	CompositeApplications("Composite Applications", ODPIcon.COMPOSITE_APPLICATIONS),
		WiringProperties("Wiring Properties", ODPIcon.WIRING_PROPERTIES, "CompositeApplications/WiringProperties"),
		Applications("Applications", ODPIcon.COMPOSITE_APPLICATION, "CompositeApplications/Applications"),
		Components("Components", ODPIcon.COMPOSITE_COMPONENT, "CompositeApplications/Components"),
	ApplicationConfiguration("Application Configuration", ODPIcon.APP_CONFIGURATION),
		ApplicationProperties("Application Properties", ODPIcon.APP_PROPERTIES, "AppProperties", f -> f.getName().equals("database.properties")),
		XspProperties("XSP Properties", ODPIcon.APP_PROPERTIES, "WebContent/WEB-INF", f -> f.getName().equals("xsp.properties")),
		FacesConfig("Faces Config", ODPIcon.FACES_CONFIG, "WebContent/WEB-INF", f -> f.getName().equals("faces-config.xml"))
	;

	private final String label;
	private final ODPIcon icon;
	private final String designElementPath;
	private final Predicate<IFile> filter;
	
	private DesignElementType(String label, ODPIcon icon) {
		this(label, icon, null, null);
	}

	private DesignElementType(String label, ODPIcon icon, String designElementPath) {
		this(label, icon, designElementPath, f -> !"metadata".equals(f.getFileExtension()));
	}
	
	private DesignElementType(String label, ODPIcon icon, String designElementPath, Predicate<IFile> filter) {
		this.label = label;
		this.icon = icon;
		this.designElementPath = designElementPath;
		this.filter = filter;
	}
	
	public String getDesignElementPath() {
		return designElementPath;
	}
	
	public Predicate<IFile> getFilter() {
		return filter;
	}
	
	public boolean isContainer() {
		return this.filter == null;
	}
	
	public String getLabel() {
		return label;
	}
	
	public ODPIcon getIcon() {
		return icon;
	}
}
