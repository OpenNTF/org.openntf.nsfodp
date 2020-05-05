package org.openntf.nsfodp.deployment.servlet;

import org.openntf.nsfodp.commons.odp.util.DominoThreadFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public class DeployServletActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		DominoThreadFactory.init();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		DominoThreadFactory.term();
	}

}
