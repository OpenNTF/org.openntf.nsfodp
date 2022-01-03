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

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;
import org.openntf.maven.nsfodp.Messages;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;

public abstract class AbstractJvmEnvironment implements JvmEnvironment {

	@Override
	public Path getJavaBin(Path notesProgram) {
		Path jvmBin = getJavaHome(notesProgram).resolve("bin"); //$NON-NLS-1$
		
		String javaBinName;
		if(SystemUtils.IS_OS_WINDOWS) {
			javaBinName = "java.exe"; //$NON-NLS-1$
		} else {
			javaBinName = "java"; //$NON-NLS-1$
		}
		Path javaBin = jvmBin.resolve(javaBinName);
		if(!Files.exists(javaBin)) {
			throw new RuntimeException(Messages.getString("EquinoxMojo.unableToLocateJava", javaBin)); //$NON-NLS-1$
		}
		
		return javaBin;
	}

}
