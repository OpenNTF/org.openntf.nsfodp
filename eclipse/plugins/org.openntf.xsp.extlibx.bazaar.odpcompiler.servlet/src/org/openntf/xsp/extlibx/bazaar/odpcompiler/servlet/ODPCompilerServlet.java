package org.openntf.xsp.extlibx.bazaar.odpcompiler.servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.ODPCompiler;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.ODPCompilerActivator;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.odp.OnDiskProject;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.update.FilesystemUpdateSite;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.update.UpdateSite;

public class ODPCompilerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Principal user = req.getUserPrincipal();
		resp.setBufferSize(0);
		
		OutputStream os = resp.getOutputStream();
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		
		PrintStream out = new PrintStream(os);
		try {
			Path odpFile = Paths.get("H:\\Projects\\SourceTree\\endeavor\\nsf\\nsf-dashboard");
			OnDiskProject odp = new OnDiskProject(odpFile);
			File siteFile = new File("C:\\temp\\site");
			UpdateSite updateSite = new FilesystemUpdateSite(siteFile);
			
			ODPCompiler compiler = new ODPCompiler(ODPCompilerActivator.instance.getBundle().getBundleContext(), odp, out);
			compiler.addUpdateSite(updateSite);
			Path nsf = compiler.compile();
			out.println("Created NSF " + nsf);
			
			out.println("done");
		} catch(Throwable e) {
			e.printStackTrace(out);
		} finally {
			out.flush();
			out.close();
		}
	}
}
