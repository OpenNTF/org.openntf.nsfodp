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
package org.openntf.nsfodp.deployment;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.nsfodp.deployment.messages"; //$NON-NLS-1$
	public static String DeployNSFTask_dbExists;
	public static String DeployNSFTask_destPathNull;
	public static String DeployNSFTask_exceptionDeploying;
	public static String DeployNSFTask_nsfFileNull;
	public static String ReplaceDesignTaskLocal_label;
	public static String ReplaceDesignTaskLocal_targetDbNameNull;
	public static String ReplaceDesignTaskLocal_templatePathNull;
	public static String ReplaceDesignTaskTest_errorReplacingDesign;
	public static String ReplaceDesignTaskTest_label;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
