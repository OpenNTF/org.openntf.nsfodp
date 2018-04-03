package org.openntf.nsfodp.cli;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.openntf.nsfodp.compiler.ODPCompiler;
import org.openntf.nsfodp.compiler.odp.OnDiskProject;
import org.openntf.nsfodp.compiler.update.FilesystemUpdateSite;
import org.openntf.nsfodp.compiler.update.UpdateSite;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class CLIApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
//		BundleContext bundleContext = CLIActivator.getDefault().getBundle().getBundleContext();
//		Bundle systemBundle = bundleContext.getBundle(0);
//		System.out.println("System bundle: " + systemBundle);
//		System.out.println("headers: " + systemBundle.getHeaders().get("Export-Package"));
//		
//		String odpDir = System.getProperty(CLIApp.class.getPackage().getName() + "-odp");
//		String siteDir = System.getProperty(CLIApp.class.getPackage().getName() + "-updateSite");
//		Path odpFile = Paths.get(odpDir);
//		OnDiskProject odp = new OnDiskProject(odpFile);
//		File siteFile = new File(siteDir);
//		UpdateSite updateSite = new FilesystemUpdateSite(siteFile);
//		
//		ODPCompiler compiler = new ODPCompiler(bundleContext, odp, null);
//		compiler.addUpdateSite(updateSite);
//		Path nsf = compiler.compile(getClass().getClassLoader());
//		System.out.println("Created NSF " + nsf);
		
		System.out.println("Hellooooo");
		
		return EXIT_OK;
	}

	@Override
	public void stop() {

	}

}
