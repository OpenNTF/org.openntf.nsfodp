/**
 * Copyright © 2018-2023 Jesse Gallagher
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletConfig;
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
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;
import org.openntf.nsfodp.compiler.ODPCompiler;
import org.openntf.nsfodp.compiler.ODPCompilerActivator;
import org.openntf.nsfodp.compiler.update.FilesystemUpdateSite;
import org.openntf.nsfodp.compiler.update.UpdateSite;

import com.ibm.commons.util.StringUtil;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

public class ODPCompilerContainerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Pattern SITE_ZIP_PATTERN = Pattern.compile("^site\\d*\\.zip$"); //$NON-NLS-1$
	
	public static boolean ALLOW_CONTAINER = "true".equals(System.getProperty("org.openntf.nsfodp.allowContainerBuild")); //$NON-NLS-1$ //$NON-NLS-2$
	
	private ExecutorService exec;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		this.exec = Executors.newSingleThreadExecutor(NotesThread::new);
		
		try {
			this.exec.submit(() -> {
				// Show the OSGi status to help diagnose update-site trouble
				try {
					Session session = NotesFactory.createSession();
					if(session.isOnServer()) {
						session.sendConsoleCommand("", "tell http osgi diag"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch(NotesException e) {
					e.printStackTrace();
				}
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		this.exec.shutdownNow();
		try {
			this.exec.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setBufferSize(0);
		resp.setStatus(HttpServletResponse.SC_OK);
		
		ServletOutputStream os = resp.getOutputStream();
		
		Set<Path> cleanup = new HashSet<>();
		try {
			if(!ALLOW_CONTAINER) {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				resp.setContentType("text/plain"); //$NON-NLS-1$
				os.println(Messages.ODPCompilerServlet_anonymousDisallowed);
				return;
			}
			
			if("ping".equals(req.getParameter("mode"))) { //$NON-NLS-1$ //$NON-NLS-2$
				// Used to tell when the container is initialized
				os.println("up"); //$NON-NLS-1$
				return;
			}
			
			Path packageFile = Paths.get(req.getHeader(NSFODPConstants.HEADER_CONTAINER_PACKAGE));
			
			// Look for an ODP item
			Path odpZip = null;
			List<Path> siteZips = new ArrayList<>();
			List<Path> classPathJars = new ArrayList<>();
			try(ZipFile packageZip = new ZipFile(packageFile.toFile(), StandardCharsets.UTF_8)) {
				ZipEntry odpEntry = packageZip.getEntry("odp.zip"); //$NON-NLS-1$
				if(odpEntry == null) {
					// Then the package is itself the ODP
					odpZip = packageFile;
				} else {
					// Then extract the ODP
					odpZip = Files.createTempFile(NSFODPUtil.getTempDirectory(), "odp", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
					cleanup.add(odpZip);
					try(InputStream odpIs = packageZip.getInputStream(odpEntry)) {
						Files.copy(odpIs, odpZip, StandardCopyOption.REPLACE_EXISTING);
					}
					
					// Look for any embedded update sites and classpath entries
					packageZip.stream()
						.forEach(entry -> {
							try {
								if(SITE_ZIP_PATTERN.matcher(entry.getName()).matches()) {
									// Then add it as an update site
									Path siteZip = Files.createTempFile(NSFODPUtil.getTempDirectory(), "site", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
									cleanup.add(siteZip);
									try(InputStream siteIs = packageZip.getInputStream(entry)) {
										Files.copy(siteIs, siteZip, StandardCopyOption.REPLACE_EXISTING);
									}
									siteZips.add(siteZip);
								} else if(entry.getName().startsWith("classpath/")) { //$NON-NLS-1$
									// Then add it as an individual JAR
									Path cpJar = Files.createTempFile(NSFODPUtil.getTempDirectory(), "classpathJar", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
									cleanup.add(cpJar);
									try(InputStream jarIs = packageZip.getInputStream(entry)) {
										Files.copy(jarIs, cpJar, StandardCopyOption.REPLACE_EXISTING);
									}
									classPathJars.add(cpJar);
								}
							} catch(IOException e) {
								throw new RuntimeException(e);
							}
						});
				}
			}
			
			IProgressMonitor mon = new LineDelimitedJsonProgressMonitor(os);

			Path nsf;
			try(FileSystem fs = NSFODPUtil.openZipPath(odpZip)) {
				Path odpFile = fs.getPath("/"); //$NON-NLS-1$
				
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
				String odsRelease = req.getHeader(NSFODPConstants.HEADER_ODS_RELEASE);
				if(StringUtil.isNotEmpty(odsRelease)) {
					compiler.setOdsRelease(odsRelease);
				}
				String compileBasicLs = req.getHeader(NSFODPConstants.HEADER_COMPILE_BASICLS);
				if("true".equals(compileBasicLs)) { //$NON-NLS-1$
					compiler.setCompileBasicElementLotusScript(true);
				}
				
				if(siteZips != null && !siteZips.isEmpty()) {
					for(Path siteZip : siteZips) {
						Path siteFile = NSFODPUtil.expandZip(siteZip);
						cleanup.add(siteFile);
						UpdateSite updateSite = new FilesystemUpdateSite(siteFile);
						compiler.addUpdateSite(updateSite);
					}
				}
				classPathJars.forEach(compiler::addClassPathEntry);
				
				nsf = this.exec.submit(() -> {
					Path result = compiler.compile();
					mon.done();
					return result;
				}).get();
			}
			
			// Now stream the NSF
			cleanup.add(nsf);
			try(OutputStream gzos = new GZIPOutputStream(os)) {
				Files.copy(nsf, gzos);
			}
			resp.flushBuffer();
			
			// Delete the NSF via the Notes API
			try(NotesAPI api = NotesAPI.get()) {
				api.deleteDatabase(nsf.toString());
			}
			
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
			NSFODPUtil.deltree(cleanup);
		}
	}
}
