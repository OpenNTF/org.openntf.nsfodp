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
package org.openntf.nsfodp.compiler.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.nsfodp.commons.LineDelimitedJsonProgressMonitor;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.odp.OnDiskProject;
import org.openntf.nsfodp.compiler.ODPCompiler;
import org.openntf.nsfodp.compiler.ODPCompilerActivator;
import org.openntf.nsfodp.compiler.update.FilesystemUpdateSite;
import org.openntf.nsfodp.compiler.update.UpdateSite;

import com.ibm.commons.util.io.StreamUtil;
import com.mindoo.domino.jna.utils.Ref;
import com.mindoo.domino.jna.utils.StringUtil;

import lotus.domino.NotesThread;

public class ODPCompilerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static boolean ALLOW_ANONYMOUS = "true".equals(System.getProperty("org.openntf.nsfodp.allowAnonymous")); //$NON-NLS-1$ //$NON-NLS-2$

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Principal user = req.getUserPrincipal();
		resp.setBufferSize(0);
		resp.setStatus(HttpServletResponse.SC_OK);
		
		ServletOutputStream os = resp.getOutputStream();
		
		Set<Path> cleanup = new HashSet<>();
		try {
			if(!ALLOW_ANONYMOUS && "Anonymous".equalsIgnoreCase(user.getName())) { //$NON-NLS-1$
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				resp.setContentType("text/plain"); //$NON-NLS-1$
				os.println("Anonymous access disallowed");
				return;
			}
			
			// Developer's note: multipart/form-data with files broken out would be nice,
			//   but Domino as of 9.0.1FP10 behaves poorly with them; for now, it's safer
			//   to use a combined ZIP and pass options in headers
			String contentType = req.getContentType();
			if(!"application/zip".equals(contentType)) { //$NON-NLS-1$
				throw new IllegalArgumentException("Content must be application/zip");
			}
			
			Path packageFile = Files.createTempFile(NSFODPUtil.getTempDirectory(), "package", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			cleanup.add(packageFile);
			try(InputStream reqInputStream = req.getInputStream()) {
				try(OutputStream packageOut = Files.newOutputStream(packageFile)) {
					StreamUtil.copyStream(reqInputStream, packageOut);
				}
			}
			
			// Look for an ODP item
			Path odpZip = null, siteZip = null;
			try(ZipFile packageZip = new ZipFile(packageFile.toFile())) {
				ZipEntry odpEntry = packageZip.getEntry("odp.zip"); //$NON-NLS-1$
				if(odpEntry == null) {
					// Then the package is itself the ODP
					odpZip = packageFile;
				} else {
					// Then extract the ODP
					odpZip = Files.createTempFile(NSFODPUtil.getTempDirectory(), "odp", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
					cleanup.add(odpZip);
					try(InputStream odpIs = packageZip.getInputStream(odpEntry)) {
						try(OutputStream odpOs = Files.newOutputStream(odpZip)) {
							StreamUtil.copyStream(odpIs, odpOs);
						}
					}
					
					// Look for an embedded update site
					ZipEntry siteEntry = packageZip.getEntry("site.zip"); //$NON-NLS-1$
					if(siteEntry != null) {
						siteZip = Files.createTempFile(NSFODPUtil.getTempDirectory(), "site", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
						cleanup.add(siteZip);
						try(InputStream siteIs = packageZip.getInputStream(siteEntry)) {
							try(OutputStream siteOs = Files.newOutputStream(siteZip)) {
								StreamUtil.copyStream(siteIs, siteOs);
							}
						}
					}
				}
			}
			
			IProgressMonitor mon = new LineDelimitedJsonProgressMonitor(os);
			
			Path odpFile = expandZip(odpZip, cleanup);
			
			OnDiskProject odp = new OnDiskProject(odpFile);
			ODPCompiler compiler = new ODPCompiler(ODPCompilerActivator.instance.getBundle().getBundleContext(), odp, mon);
			
			// See if the client requested a specific compiler level
			String compilerLevel = req.getHeader(NSFODPConstants.HEADER_COMPILER_LEVEL);
			if(StringUtil.isNotEmpty(compilerLevel)) {
				compiler.setCompilerLevel(compilerLevel);
			}
			String appendTimestamp = req.getHeader(NSFODPConstants.HEADER_APPEND_TIMESTAMP);
			if("true".equals(appendTimestamp)) { //$NON-NLS-1$
				compiler.setAppendTimestampToTitle(true);
			}
			String templateName = req.getHeader(NSFODPConstants.HEADER_TEMPLATE_NAME);
			if(StringUtil.isNotEmpty(templateName)) {
				compiler.setTemplateName(templateName);
				String templateVersion = req.getHeader(NSFODPConstants.HEADER_TEMPLATE_VERSION);
				if(StringUtil.isNotEmpty(templateVersion)) {
					compiler.setTemplateVersion(templateVersion);
				}
			}
			String setXspOptions = req.getHeader(NSFODPConstants.HEADER_SET_PRODUCTION_XSP);
			if("true".equals(setXspOptions)) { //$NON-NLS-1$
				compiler.setSetProductionXspOptions(true);
			}
			
			if(siteZip != null) {
				Path siteFile = expandZip(siteZip, cleanup);
				UpdateSite updateSite = new FilesystemUpdateSite(siteFile.toFile());
				compiler.addUpdateSite(updateSite);
			}
			
			Ref<Path> nsf = new Ref<>();
			NotesThread notes = new NotesThread(() -> {
				try {
					nsf.set(compiler.compile());
					mon.done();
				} catch(RuntimeException e) {
					throw e;
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			});
			notes.run();
			notes.join();
			
			
			// Now stream the NSF
			cleanup.add(nsf.get());
			try(InputStream is = Files.newInputStream(nsf.get())) {
				try(OutputStream gzos = new GZIPOutputStream(os)) {
					StreamUtil.copyStream(is, gzos);
				}
			}
			resp.flushBuffer();
		} catch(Throwable e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(baos);
			e.printStackTrace(out);
			out.flush();
			os.println(LineDelimitedJsonProgressMonitor.message(
				"type", "error", //$NON-NLS-1$ //$NON-NLS-2$
				"stackTrace", baos.toString() //$NON-NLS-1$
				)
			);
		} finally {
			for(Path path : cleanup) {
				if(Files.isDirectory(path)) {
					Files.walk(path)
					    .sorted(Comparator.reverseOrder())
					    .map(Path::toFile)
					    .forEach(File::delete);
				}
				Files.deleteIfExists(path);
			}
		}
	}
	
	public static Path expandZip(Path zipFilePath, Collection<Path> cleanup) throws IOException {
		Path result = Files.createTempDirectory(NSFODPUtil.getTempDirectory(), "zipFile"); //$NON-NLS-1$
		cleanup.add(result);
		
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
