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
package org.openntf.nsfodp.eclipse;

public enum ODPIcon {
	CUSTOM_CONTROL("icons/emblem-system.png"),
	STOCK_COMPONENT("icons/system-file-manager.png"),
	XPAGE("icons/text-html.png"),
	SCRIPT("icons/text-x-script.png"),
	STYLESHEET("icons/font-x-generic.png"),
	IMAGE("icons/image-x-generic.png"),
	THEME("icons/applications-graphics.png"),
	FORM("icons/text-x-generic.png"),
	FOLDER("icons/folder.png"),
	APP_PROPERTIES("icons/document-properties.png"),
	USING_DOCUMENT("icons/help-browser.png"),
	ABOUT_DOCUMENT("icons/emblem-important.png"),
	COMPOSITE_APPLICATIONS("icons/preferences-system-windows.png"),
	CODE("icons/applications-development.png"),
	VIEW("icons/x-office-spreadsheet.png"),
	DATABASE_SCRIPT("icons/application-x-executable.png"),
	WEB_SERVICE_CONSUMER("icons/network-receive.png"),
	WEB_SERVICE_PROVIDER("icons/network-transmit.png"),
	AGENT("icons/dialog-information.png"),
	SHARED_ACTIONS("icons/weather-storm.png"),
	PAGE("icons/format-justify-fill.png"),
	FRAMESET("icons/applications-games.png"),
	SHARED_ELEMENTS("icons/camera-video.png"),
	DATA("icons/network-server.png"),
	DATA_CONNECTION("icons/network-workgroup.png"),
	DB2_ACCESS_VIEW("icons/x-office-spreadsheet-template.png"),
	RESOURCES("icons/user-home.png"),
	FILE("icons/mail-attachment.png"),
	APPLET("icons/image-missing.png"),
	DATABASE_ICON("icons/applications-office.png"),
	OUTLINE("icons/format-justify-left.png"),
	NAVIGATOR("icons/mail-send-receive.png"),
	COLUMN("icons/tab-new.png"),
	FIELD("icons/accessories-text-editor.png"),
	SUBFORM("icons/internet-news-reader.png"),
	WIRING_PROPERTIES("icons/network-wired.png"),
	COMPOSITE_APPLICATION("icons/image-loading.png"),
	COMPOSITE_COMPONENT("icons/folder-visiting.png"),
	APP_CONFIGURATION("icons/preferences-desktop.png"),
	BLUEMIX_CONFIG("icons/weather-overcast.png"),
	FACES_CONFIG("icons/x-office-drawing.png"),
	
	GENERIC("icons/text-html.png");

	private final String path;
	private ODPIcon(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
}
