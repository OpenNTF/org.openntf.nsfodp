package org.openntf.nsfodp.example;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

/**
 * @since 1.0.0
 */
public class Activator extends Plugin {
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
}
