package org.openntf.nsfodp.exporter.equinox;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ExporterEquinoxActivator implements BundleActivator {
	private static ExporterEquinoxActivator instance;
	
	public static ExporterEquinoxActivator getDefault() {
		return instance;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}

}
