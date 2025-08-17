/*
 * Copyright (c) 2018-2025 Jesse Gallagher
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
import org.openntf.nsfodp.eclipse.ui.Messages;
import org.openntf.nsfodp.eclipse.ui.ODPIcon;

public enum DesignElementType {
	Forms(Messages.DesignElementType_Forms, ODPIcon.FORM, "Forms"), //$NON-NLS-1$
	Views(Messages.DesignElementType_Views, ODPIcon.VIEW, "Views"), //$NON-NLS-1$
	Folders(Messages.DesignElementType_Folders, ODPIcon.FOLDER, "Folders"), //$NON-NLS-1$
	XPages(Messages.DesignElementType_XPages, ODPIcon.XPAGE, "XPages"), //$NON-NLS-1$
	CustomControls(Messages.DesignElementType_CustomControls, ODPIcon.CUSTOM_CONTROL, "CustomControls"), //$NON-NLS-1$
	Framesets(Messages.DesignElementType_Framesets, ODPIcon.FRAMESET, "Framesets"), //$NON-NLS-1$
	Pages(Messages.DesignElementType_Pages, ODPIcon.PAGE, "Pages"), //$NON-NLS-1$
	SharedElements(Messages.DesignElementType_SharedElements, ODPIcon.SHARED_ELEMENTS),
		Subforms(Messages.DesignElementType_Subforms, ODPIcon.SUBFORM, "SharedElements/Subforms"), //$NON-NLS-1$
		Fields(Messages.DesignElementType_Fields, ODPIcon.FIELD, "SharedElements/Fields"), //$NON-NLS-1$
		Columns(Messages.DesignElementType_Columns, ODPIcon.COLUMN, "SharedElements/Columns"), //$NON-NLS-1$
		Outlines(Messages.DesignElementType_Outlines, ODPIcon.OUTLINE, "SharedElements/Outlines"), //$NON-NLS-1$
		Navigators(Messages.DesignElementType_Navigators, ODPIcon.NAVIGATOR, "SharedElements/Navigators"), //$NON-NLS-1$
	Code(Messages.DesignElementType_Code, ODPIcon.CODE),
		Agents(Messages.DesignElementType_Agents, ODPIcon.AGENT, "Code/Agents"), //$NON-NLS-1$
		SharedActions(Messages.DesignElementType_SharedActions, ODPIcon.SHARED_ACTIONS, "Code/actions", f -> f.getName().equals("Shared Actions")), //$NON-NLS-1$ //$NON-NLS-2$
		ScriptLibraries(Messages.DesignElementType_ScriptLibraries, ODPIcon.SCRIPT, "Code/ScriptLibraries"), //$NON-NLS-1$
		DatabaseScript(Messages.DesignElementType_DatabaseScript, ODPIcon.DATABASE_SCRIPT, "Code", f -> f.getName().equals("dbscript.lsdb")), //$NON-NLS-1$ //$NON-NLS-2$
		WebServiceProviders(Messages.DesignElementType_WebServiceProviders, ODPIcon.WEB_SERVICE_PROVIDER, "Code/WebServices"), //$NON-NLS-1$
		WebServiceConsumers(Messages.DesignElementType_WebServiceConsumers, ODPIcon.WEB_SERVICE_CONSUMER, "Code/WebServiceConsumer"), //$NON-NLS-1$
	Data(Messages.DesignElementType_Data, ODPIcon.DATA),
		DataConnections(Messages.DesignElementType_DataConnections, ODPIcon.DATA_CONNECTION, "Data/DataConnections"), //$NON-NLS-1$
		DB2AccessViews(Messages.DesignElementType_DB2AccessViews, ODPIcon.DB2_ACCESS_VIEW, "Data/DB2AccessViews"), //$NON-NLS-1$
	Resources(Messages.DesignElementType_Resources, ODPIcon.RESOURCES),
		Images(Messages.DesignElementType_Images, ODPIcon.IMAGE, "Resources/Images"), //$NON-NLS-1$
		Files(Messages.DesignElementType_Files, ODPIcon.FILE, "Resources/Files"), //$NON-NLS-1$
		Applets(Messages.DesignElementType_Applets, ODPIcon.APPLET, "Resources/Applets"), //$NON-NLS-1$
		StyleSheets(Messages.DesignElementType_StyleSheets, ODPIcon.STYLESHEET, "Resources/StyleSheets"), //$NON-NLS-1$
		Themes(Messages.DesignElementType_Themes, ODPIcon.THEME, "Resources/Themes"), //$NON-NLS-1$
		AboutDocument(Messages.DesignElementType_AboutDocument, ODPIcon.ABOUT_DOCUMENT, "Resources", f -> f.getName().equals("AboutDocument")), //$NON-NLS-1$ //$NON-NLS-2$
		UsingDocument(Messages.DesignElementType_UsingDocument, ODPIcon.USING_DOCUMENT, "Resources", f -> f.getName().equals("UsingDocument")), //$NON-NLS-1$ //$NON-NLS-2$
		Icon(Messages.DesignElementType_Icon, ODPIcon.DATABASE_ICON, "AppProperties", f -> f.getName().equals("$DBIcon")), //$NON-NLS-1$ //$NON-NLS-2$
	CompositeApplications(Messages.DesignElementType_CompositeApplications, ODPIcon.COMPOSITE_APPLICATIONS),
		WiringProperties(Messages.DesignElementType_CAWiringProperties, ODPIcon.WIRING_PROPERTIES, "CompositeApplications/WiringProperties"), //$NON-NLS-1$
		Applications(Messages.DesignElementType_CAApplications, ODPIcon.COMPOSITE_APPLICATION, "CompositeApplications/Applications"), //$NON-NLS-1$
		Components(Messages.DesignElementType_CAComponents, ODPIcon.COMPOSITE_COMPONENT, "CompositeApplications/Components"), //$NON-NLS-1$
	ApplicationConfiguration(Messages.DesignElementType_ApplicationConfiguration, ODPIcon.APP_CONFIGURATION),
		ApplicationProperties(Messages.DesignElementType_ApplicationProperties, ODPIcon.APP_PROPERTIES, "AppProperties", f -> f.getName().equals("database.properties")), //$NON-NLS-1$ //$NON-NLS-2$
		XspProperties(Messages.DesignElementType_XSPProperties, ODPIcon.APP_PROPERTIES, "WebContent/WEB-INF", f -> f.getName().equals("xsp.properties")), //$NON-NLS-1$ //$NON-NLS-2$
		FacesConfig(Messages.DesignElementType_FacesConfig, ODPIcon.FACES_CONFIG, "WebContent/WEB-INF", f -> f.getName().equals("faces-config.xml")) //$NON-NLS-1$ //$NON-NLS-2$
	;

	private final String label;
	private final ODPIcon icon;
	private final String designElementPath;
	private final Predicate<IFile> filter;
	
	private DesignElementType(String label, ODPIcon icon) {
		this(label, icon, null, null);
	}

	private DesignElementType(String label, ODPIcon icon, String designElementPath) {
		this(label, icon, designElementPath, f -> !"metadata".equals(f.getFileExtension())); //$NON-NLS-1$
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
