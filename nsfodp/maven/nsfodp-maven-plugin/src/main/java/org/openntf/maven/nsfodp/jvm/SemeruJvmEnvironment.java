/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.maven.nsfodp.jvm;

import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;

/**
 * An implementation of {@link JvmEnvironment} that uses an auto-downloaed
 * IBM Semeru OpenJ9 Java runtime when using Notes V12 on macOS.
 * 
 * @author Jesse Gallagher
 * @since 3.7.0
 */
public class SemeruJvmEnvironment extends AbstractMacGitHubJvmEnvironment {
	public static final String API_RELEASES = "https://api.github.com/repos/ibmruntimes/semeru{0}-binaries/releases?per_page=100"; //$NON-NLS-1$
	public static final String PROVIDER_NAME = "IBM Semeru"; //$NON-NLS-1$
	public static final String SHORT_NAME = "semeru"; //$NON-NLS-1$
	
	@Override
	protected String getProviderName() {
		return PROVIDER_NAME;
	}
	
	@Override
	protected String getReleasesApi() {
		return API_RELEASES;
	}
	
	@Override
	protected String getShortName() {
		return SHORT_NAME;
	}

	@Override
	public boolean isActive(Path notesProgram) {
		// Semeru should be active on macOS with Notes 12.0.0 and 12.0.1, but not 12.0.2
		if(SystemUtils.IS_OS_MAC) {
			return isMac12(notesProgram);
		} else {
			return false;
		}
	}

}
