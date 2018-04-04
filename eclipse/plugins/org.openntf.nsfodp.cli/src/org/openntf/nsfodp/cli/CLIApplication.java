package org.openntf.nsfodp.cli;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.http.jetty.JettyConstants;
import org.osgi.framework.Bundle;

import lotus.domino.NotesThread;

public class CLIApplication implements IApplication {
	Class<?> configurator;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		int httpPort = 8888;
		
		System.out.println("Listening for compiler requests on port " + httpPort);
		
		NotesThread.sinitThread();
		
		// Do this reflectively because otherwise there's a ClassNotFoundException for some reason
		Bundle jetty = Platform.getBundle("org.eclipse.equinox.http.jetty");
		configurator = jetty.loadClass("org.eclipse.equinox.http.jetty.JettyConfigurator");
		Dictionary<String, String> jettyConfig = new Hashtable<>();
		jettyConfig.put(JettyConstants.HTTP_PORT, String.valueOf(httpPort));
		jettyConfig.put(JettyConstants.HTTP_HOST, "127.0.0.1");
		configurator.getMethod("startServer", String.class, Dictionary.class).invoke(null, getClass().getPackage().getName(), jettyConfig);
		
		System.in.read();
		
		return EXIT_OK;
	}

	@Override
	public void stop() {
		try {
			configurator.getMethod("stopServer", String.class).invoke(null, getClass().getPackage().getName());
			
			NotesThread.stermThread();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
