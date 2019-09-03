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
		
		getLog().log(new Status(IStatus.WARNING, context.getBundle().getSymbolicName(), "!!!! NSF ODP Watcher Start"));
	}
	
	public void log(int severity, String message, Object... params) {
		getLog().log(new Status(severity, getBundle().getSymbolicName(), MessageFormat.format(message, params)));
	}
}
