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
package org.openntf.nsfodp.eclipse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Activator extends AbstractUIPlugin {
	public static final String ICON_EMBLEM_SYSTEM = "icons/emblem-system.png"; //$NON-NLS-1$
	
	private static Activator instance;
	
	public static ILog log;
	
	public static Activator getDefault() {
		return instance;
	}
	
	public Activator() {
		super();
		instance = this;
		log = Platform.getLog(instance.getBundle());
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		
		reg.put(ICON_EMBLEM_SYSTEM, imageDescriptorFromPlugin(instance.getBundle().getSymbolicName(), ICON_EMBLEM_SYSTEM));
	}
	
	public static void logError(String message, Throwable throwable) {
		log.log(new Status(Status.ERROR, instance.getBundle().getSymbolicName(), message, throwable));
	}
	
	public static void log(String message) {
		log.log(new Status(Status.INFO, message, message));
	}
}
