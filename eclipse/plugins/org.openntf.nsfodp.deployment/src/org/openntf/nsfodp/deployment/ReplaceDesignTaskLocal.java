/**
 * Copyright © 2018 Jesse Gallagher
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

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.domino.osgi.core.context.ContextInfo;

import lotus.domino.Session;

public class ReplaceDesignTaskLocal implements Runnable {

	private final String targetDbName;
	private final Path templatePath;
	private final IProgressMonitor monitor;
	
	public ReplaceDesignTaskLocal(String targetDbName, Path templatePath, IProgressMonitor monitor) {
		this.targetDbName = Objects.requireNonNull(targetDbName, "targetDbName cannot be null");
		this.templatePath = Objects.requireNonNull(templatePath, "templatePath cannot be null");
		this.monitor = monitor;
	}

	@Override
	public void run() {
		monitor.setTaskName("Replace Design");
		
		try {
			Session session = ContextInfo.getUserSession();
			
			String command = MessageFormat.format("load convert -d {0} * {1}", targetDbName, templatePath.toAbsolutePath().toString());
			session.sendConsoleCommand("", command);
			
			monitor.done();
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
