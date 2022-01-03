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
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;

/**
 * An implementation of {@link JvmEnvironment} that uses the Notes/Domino
 * JVM.
 * 
 * @author Jesse Gallagher
 * @since 3.7.0
 */
public class NotesJvmEnvironment extends AbstractJvmEnvironment {

	@Override
	public boolean isActive(Path notesProgram) {
		return !SystemUtils.IS_OS_MAC;
	}

	@Override
	public Path getJavaHome(Path notesProgram) {
		Path jvmHome = notesProgram.resolve("jvm"); //$NON-NLS-1$
		if(!Files.isDirectory(jvmHome)) {
			throw new RuntimeException("Could not find JVM at " + jvmHome);
		}
		return jvmHome;
	}

}
