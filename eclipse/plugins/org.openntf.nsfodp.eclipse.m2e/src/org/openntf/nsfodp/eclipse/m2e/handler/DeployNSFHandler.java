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
package org.openntf.nsfodp.eclipse.m2e.handler;

import java.io.PrintWriter;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.openntf.nsfodp.eclipse.m2e.job.DeployNSFJob;

public class DeployNSFHandler extends AbstractNSFODPHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IProject project = getSelectedProject();
			
			DeployNSFJob job = new DeployNSFJob(project);
			job.schedule();
			
		} catch(Throwable t) {
			MessageConsole console = findConsole(getClass().getPackage().getName());
			MessageConsoleStream out = console.newMessageStream();
			try(PrintWriter pw = new PrintWriter(out)) {
				t.printStackTrace(pw);
				pw.flush();
			}
		}
		return null;
	}
}
