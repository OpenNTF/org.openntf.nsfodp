/**
 * Copyright © 2018-2022 Jesse Gallagher
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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.commons.odp.messages"; //$NON-NLS-1$
	public static String AbstractSplitDesignElement_cannotLocateDxl;
	public static String AbstractSplitDesignElement_cannotReadFile;
	public static String FileResource_noNameProvider;
	public static String ODPUtil_bundleInInstalledState;
	public static String ODPUtil_cannotInferClassName;
	public static String OnDiskProject_classpathNotAFile;
	public static String OnDiskProject_dbPropertiesDoesNotExist;
	public static String OnDiskProject_dbPropertiesNotAFile;
	public static String OnDiskProject_pluginDoesNotExist;
	public static String OnDiskProject_pluginNotAFile;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
