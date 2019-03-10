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
package org.openntf.nsfodp.eclipse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Activator extends AbstractUIPlugin {
	public static final String ICON_CUSTOM_CONTROL = "icon-custom-control"; //$NON-NLS-1$
	public static final String ICON_STOCK_COMPONENT = "icon-stock-component"; //$NON-NLS-1$
	public static final String ICON_HTML = "icon-html"; //$NON-NLS-1$
	public static final String ICON_SCRIPT = "icon-script"; //$NON-NLS-1$
	public static final String ICON_ACCESSORIES = "icon-accessories"; //$NON-NLS-1$
	public static final String ICON_FONT = "icon-font"; //$NON-NLS-1$
	public static final String ICON_IMAGE = "icon-image"; //$NON-NLS-1$
	
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
		
		reg.put(ICON_CUSTOM_CONTROL, imageDescriptorFromPlugin(instance.getBundle().getSymbolicName(), "icons/emblem-system.png")); //$NON-NLS-1$
		reg.put(ICON_STOCK_COMPONENT, imageDescriptorFromPlugin(instance.getBundle().getSymbolicName(), "icons/system-file-manager.png")); //$NON-NLS-1$
		reg.put(ICON_HTML, imageDescriptorFromPlugin(instance.getBundle().getSymbolicName(), "icons/text-html.png")); //$NON-NLS-1$
		reg.put(ICON_SCRIPT, imageDescriptorFromPlugin(instance.getBundle().getSymbolicName(), "icons/text-x-script.png")); //$NON-NLS-1$
		reg.put(ICON_ACCESSORIES, imageDescriptorFromPlugin(instance.getBundle().getSymbolicName(), "icons/applications-accessories.png")); //$NON-NLS-1$
		reg.put(ICON_IMAGE, imageDescriptorFromPlugin(instance.getBundle().getSymbolicName(), "icons/image-x-generic.png")); //$NON-NLS-1$
		reg.put(ICON_FONT, imageDescriptorFromPlugin(instance.getBundle().getSymbolicName(), "icons/font-x-generic.png")); //$NON-NLS-1$
	}
	
	public static void logError(String message, Throwable throwable) {
		log.log(new Status(Status.ERROR, instance.getBundle().getSymbolicName(), message, throwable));
	}
	
	public static void log(String message) {
		log.log(new Status(Status.INFO, message, message));
	}
}
