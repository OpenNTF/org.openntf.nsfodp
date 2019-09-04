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
