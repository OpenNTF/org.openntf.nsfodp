package org.openntf.xsp.extlibx.bazaar.odpcompiler.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.xsp.extlibx.bazaar.odpcompiler.ODPCompiler;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.ODPCompilerActivator;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.odp.OnDiskProject;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.update.FilesystemUpdateSite;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.update.UpdateSite;

import com.ibm.commons.util.io.StreamUtil;

public class ODPCompilerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static boolean ALLOW_ANONYMOUS = false;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Principal user = req.getUserPrincipal();
		resp.setBufferSize(0);
		
		OutputStream os = resp.getOutputStream();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			if(!ALLOW_ANONYMOUS && "Anonymous".equalsIgnoreCase(user.getName())) {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				resp.setContentType("text/plain");
				os.write("Anonymous access disallowed".getBytes());
				return;
			}
			
			String contentType = req.getContentType();
			if(!"application/zip".equals(contentType)) {
				throw new IllegalArgumentException("Content must be application/zip");
			}
			
			Path packageFile = Files.createTempFile("package", ".zip");
			try(InputStream reqInputStream = req.getInputStream()) {
				try(OutputStream packageOut = Files.newOutputStream(packageFile)) {
					StreamUtil.copyStream(reqInputStream, packageOut);
				}
			}
			
			// Look for an ODP item
			Path odpZip = null, siteZip = null;
			try(ZipFile packageZip = new ZipFile(packageFile.toFile())) {
				ZipEntry odpEntry = packageZip.getEntry("odp.zip");
				if(odpEntry == null) {
					// Then the package is itself the ODP
					odpZip = packageFile;
				} else {
					// Then extract the ODP
					odpZip = Files.createTempFile("odp", ".zip");
					try(InputStream odpIs = packageZip.getInputStream(odpEntry)) {
						try(OutputStream odpOs = Files.newOutputStream(odpZip)) {
							StreamUtil.copyStream(odpIs, odpOs);
						}
					}
					
					// Look for an embedded update site
					ZipEntry siteEntry = packageZip.getEntry("site.zip");
					if(siteEntry != null) {
						siteZip = Files.createTempFile("site", ".zip");
						try(InputStream siteIs = packageZip.getInputStream(siteEntry)) {
							try(OutputStream siteOs = Files.newOutputStream(siteZip)) {
								StreamUtil.copyStream(siteIs, siteOs);
							}
						}
					}
				}
			}
			
			Path odpFile = expandZip(odpZip);
			
			OnDiskProject odp = new OnDiskProject(odpFile);
			ODPCompiler compiler = new ODPCompiler(ODPCompilerActivator.instance.getBundle().getBundleContext(), odp, System.out);
			
			if(siteZip != null) {
				Path siteFile = expandZip(siteZip);
				UpdateSite updateSite = new FilesystemUpdateSite(siteFile.toFile());
				compiler.addUpdateSite(updateSite);
			}
			
			Path nsf = compiler.compile();
			out.println("Created NSF " + nsf);
			
			out.println("done");
			
			// Now stream the NSF
			try(InputStream is = Files.newInputStream(nsf)) {
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.setContentType("application/octet-stream");
				StreamUtil.copyStream(is, os);
			}
		} catch(Throwable e) {
			e.printStackTrace(out);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.setContentType("text/plain");
			os.write(baos.toByteArray());
		} finally {
			out.flush();
			out.close();
		}
	}
	
	public static Path expandZip(Path zipFilePath) throws IOException {
		Path result = Files.createTempDirectory("zipFile");
		
		try(ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
			for(ZipEntry entry : Collections.list(zipFile.entries())) {
				Path subFile = result.resolve(entry.getName());
				if(entry.isDirectory()) {
					Files.createDirectories(subFile);
				} else {
					Files.createDirectories(subFile.getParent());
					try(InputStream is = zipFile.getInputStream(entry)) {
						Files.copy(is, subFile);
					}
				}
			}
		}
		
		return result;
	}
}
