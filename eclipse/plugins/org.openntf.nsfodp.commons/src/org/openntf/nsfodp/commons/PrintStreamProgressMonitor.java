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
package org.openntf.nsfodp.commons;

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An implementation of {@link IProgressMonitor} sends monitor messages to a
 * designated {@link PrintStream}.
 *  
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class PrintStreamProgressMonitor implements IProgressMonitor {
	private final PrintStream out;
	private boolean canceled;

	public PrintStreamProgressMonitor(PrintStream out) {
		this.out = out;
	}

	@Override
	public void beginTask(String name, int totalWork) {
		out.println(name);
	}

	@Override
	public void done() {
	}

	@Override
	public void internalWorked(double work) {

	}

	@Override
	public boolean isCanceled() {
		return this.canceled;
	}

	@Override
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
		if(canceled) {
			out.println("Canceled!");
		}
	}

	@Override
	public void setTaskName(String name) {
		out.println(name);
	}

	@Override
	public void subTask(String name) {
		out.println(name);
	}

	@Override
	public void worked(int work) {

	}

}
