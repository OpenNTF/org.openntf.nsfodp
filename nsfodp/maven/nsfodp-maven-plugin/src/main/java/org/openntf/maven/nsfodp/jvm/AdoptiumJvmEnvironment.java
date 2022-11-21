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
package org.openntf.maven.nsfodp.jvm;

import static java.text.MessageFormat.format;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;

/**
 * An implementation of {@link JvmEnvironment} that uses an auto-downloaed
 * Eclipse Adoptium Java runtime when using Notes pre-V12 on macOS.
 * 
 * @author Jesse Gallagher
 * @since 3.7.0
 */
public class AdoptiumJvmEnvironment extends AbstractMacGitHubJvmEnvironment {
	public static final String API_RELEASES = "https://api.github.com/repos/adoptium/temurin{0}-binaries/releases?per_page=100"; //$NON-NLS-1$
	// 0 = Java version, 1 = OS, 2 = arch, 3 = jvm impl (hotspot), 4 = vendor
	public static final String API_LATEST = "https://api.adoptium.net/v3/binary/latest/{0}/ga/{1}/{2}/jdk/{3}/normal/{4}?project=jdk"; //$NON-NLS-1$
	
	public static final String PROVIDER_NAME = "Adoptium Temurin"; //$NON-NLS-1$
	public static final String SHORT_NAME = "temurin"; //$NON-NLS-1$
	
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
	public Path getJavaHome(Path notesProgram) {
		Path userHome = SystemUtils.getUserHome().toPath();
		Path jvmDir = userHome.resolve(".nsfodp").resolve("jvm").resolve(getShortName()); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.isDirectory(jvmDir)) {
			String latestUrl = format(API_LATEST, "8", getOsName(), getOsArch(), "hotspot", "eclipse"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			download(latestUrl, "application/gzip", jvmDir); //$NON-NLS-1$
			
			markExecutables(jvmDir);
		}
		return jvmDir.resolve("Contents").resolve("Home"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean isActive(Path notesProgram) {
		// Temurin should be active on macOS with Notes 12.0.0 and 12.0.1, but not 12.0.2
		if(SystemUtils.IS_OS_MAC) {
			return !isMac12(notesProgram);
		} else {
			return false;
		}
	}

}
