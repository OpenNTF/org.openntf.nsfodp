package org.openntf.nsfodp.cli;

import java.io.Console;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.equinox.http.jetty.JettyConstants;
import org.openntf.nsfodp.compiler.ODPCompiler;
import org.openntf.nsfodp.compiler.odp.OnDiskProject;
import org.openntf.nsfodp.compiler.update.FilesystemUpdateSite;
import org.openntf.nsfodp.compiler.update.UpdateSite;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class CLIApplication implements IApplication {
	Class<?> configurator;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		int httpPort = 8888;
		
		System.out.println("Listening for compiler requests on port " + httpPort);
		
		Bundle notesJar = Platform.getBundle("com.ibm.notes.java.api");
		System.out.println("notesJar: " + notesJar);
		System.out.println("jar status: " + notesJar.getState());
		System.out.println("Base: " + notesJar.loadClass("lotus.domino.Base"));
		
		Bundle jetty = Platform.getBundle("org.eclipse.equinox.http.jetty");
		configurator = jetty.loadClass("org.eclipse.equinox.http.jetty.JettyConfigurator");
		Dictionary<String, String> jettyConfig = new Hashtable<>();
		jettyConfig.put(JettyConstants.HTTP_PORT, String.valueOf(httpPort));
		configurator.getMethod("startServer", String.class, Dictionary.class).invoke(null, getClass().getPackage().getName(), jettyConfig);
		
		System.in.read();
		
		return EXIT_OK;
	}

	@Override
	public void stop() {
		try {
			configurator.getMethod("stopServer", String.class).invoke(null, getClass().getPackage().getName());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
