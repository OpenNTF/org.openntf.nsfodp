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
