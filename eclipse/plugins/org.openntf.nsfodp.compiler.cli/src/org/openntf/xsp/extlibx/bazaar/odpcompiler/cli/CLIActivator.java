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
package org.openntf.xsp.extlibx.bazaar.odpcompiler.cli;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.ODPCompiler;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.odp.OnDiskProject;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.update.FilesystemUpdateSite;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.update.UpdateSite;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CLIActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		System.out.println(getClass().getName() + " starting up");
		
		Bundle systemBundle = bundleContext.getBundle(0);
		System.out.println("System bundle: " + systemBundle);
		System.out.println("headers: " + systemBundle.getHeaders().get("Export-Package"));
		
		String odpDir = System.getProperty(CLIApp.class.getPackage().getName() + "-odp");
		String siteDir = System.getProperty(CLIApp.class.getPackage().getName() + "-updateSite");
		Path odpFile = Paths.get(odpDir);
		OnDiskProject odp = new OnDiskProject(odpFile);
		File siteFile = new File(siteDir);
		UpdateSite updateSite = new FilesystemUpdateSite(siteFile);
		
		ODPCompiler compiler = new ODPCompiler(bundleContext, odp, System.out);
		compiler.addUpdateSite(updateSite);
		Path nsf = compiler.compile(getClass().getClassLoader());
		System.out.println("Created NSF " + nsf);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		System.out.println(getClass().getName() + " shutting down");
	}

}
