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
package org.openntf.nsfodp.designer.odpwatcher;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
	private static Activator instance;
	
	public static Activator getDefault() {
		return instance;
	}
	
	public Activator() {
		instance = this;
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		getLog().log(new Status(IStatus.INFO, context.getBundle().getSymbolicName(), "Loading OpenNTF ODP Watcher"));
	}
	
	public void log(int severity, String message, Object... params) {
		getLog().log(new Status(severity, getBundle().getSymbolicName(), MessageFormat.format(message, params)));
	}
	public void log(Throwable t) {
		if(t != null) {
			getLog().log(new Status(IStatus.ERROR, getBundle().getSymbolicName(), t.getLocalizedMessage(), t));
		}
	}
}
