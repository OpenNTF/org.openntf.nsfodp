/**
 * Copyright © 2018-2022 Jesse Gallagher
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.openntf.nsfodp.commons.LineDelimitedJsonProgressMonitor;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;
import org.openntf.nsfodp.commons.osgi.EquinoxRunner;
import org.openntf.nsfodp.compiler.servlet.jvm.ServerJvmEnvironment;
import org.osgi.framework.Bundle;

import com.ibm.domino.napi.c.Os;

import lotus.domino.NotesThread;

public class ODPCompilerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Pattern SITE_ZIP_PATTERN = Pattern.compile("^site\\d*\\.zip$"); //$NON-NLS-1$
	
	public static boolean ALLOW_ANONYMOUS = "true".equals(System.getProperty("org.openntf.nsfodp.allowAnonymous")); //$NON-NLS-1$ //$NON-NLS-2$
	
	private ExecutorService exec;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		this.exec = Executors.newSingleThreadExecutor(NotesThread::new);
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
				os.println(Messages.ODPCompilerServlet_anonymousDisallowed);
				return;
			}
			
			// Developer's note: multipart/form-data with files broken out would be nice,
			//   but Domino as of 9.0.1FP10 behaves poorly with them; for now, it's safer
			//   to use a combined ZIP and pass options in headers
			String contentType = req.getContentType();
			if(!"application/zip".equals(contentType)) { //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.ODPCompilerServlet_contentMustBeZip);
			}
			
			Path packageFile = Files.createTempFile(NSFODPUtil.getTempDirectory(), "package", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			cleanup.add(packageFile);
			try(InputStream reqInputStream = req.getInputStream()) {
				Files.copy(reqInputStream, packageFile, StandardCopyOption.REPLACE_EXISTING);
			}
			
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
			EquinoxRunner runner = new EquinoxRunner();
			
			Path odpDir = NSFODPUtil.expandZip(odpZip);
			cleanup.add(odpDir);

			runner.addEnvironmentVar(NSFODPConstants.PROP_ODPDIRECTORY, odpDir.toString());
			Path nsf = Files.createTempFile(NSFODPUtil.getTempDirectory(), getClass().getName(), ".nsf"); //$NON-NLS-1$
			Files.deleteIfExists(nsf);
			runner.addEnvironmentVar(NSFODPConstants.PROP_OUTPUTFILE, nsf.toString());
			cleanup.add(nsf);

			String notesDir = exec.submit(() -> {
				return Os.OSGetExecutableDirectory();
			}).get();
			Path notesProgram = Paths.get(notesDir);
			JvmEnvironment jvm = new ServerJvmEnvironment();
			runner.setJvmEnvironment(jvm);
			runner.setNotesProgram(notesProgram);
			
			jvm.getJvmProperties(notesProgram).forEach(runner::addJvmLaunchProperty);
			
			classPathJars.forEach(runner::addClasspathJar);
			
			Path framework = Files.createTempDirectory(NSFODPUtil.getTempDirectory(), getClass().getName());
			runner.setWorkingDirectory(framework);
			System.out.println("working dir is " + framework);
			// TODO cleanup
			
			Stream.of(
				getDependencyRef("org.openntf.nsfodp.commons", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.notesapi.darwinonapi", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.commons.dxl", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.commons.odp", -1), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.compiler", 2), //$NON-NLS-1$
				getDependencyRef("org.openntf.nsfodp.compiler.equinox", -1), //$NON-NLS-1$
				getDependencyRef("com.ibm.xsp.extlibx.bazaar", -1), //$NON-NLS-1$
				getDependencyRef("com.ibm.xsp.extlibx.bazaar.interpreter", -1) //$NON-NLS-1$
			).forEach(runner::addPlatformEntry);
			
			// Read in the rcp OSGi directory
			{
				Path rcp = notesProgram.resolve("osgi").resolve("rcp").resolve("eclipse"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if(!Files.exists(rcp)) {
					throw new IllegalStateException(MessageFormat.format("rcp directory not present at expected location: {0}", rcp)); //$NON-NLS-1$
				}
				Path notesPlugins = rcp.resolve("plugins"); //$NON-NLS-1$
				if(!Files.exists(notesPlugins)) {
					throw new IllegalStateException(MessageFormat.format("rcp plugins directory not present at expected location: {0}", rcp)); //$NON-NLS-1$
				}
				String[] osgiBundle = new String[1];
				try(Stream<Path> pluginsStream = Files.list(notesPlugins)) {
					pluginsStream.filter(ODPCompilerServlet::isBundle)
					.filter(p -> {
						if(p.getFileName().toString().startsWith("org.eclipse.osgi_")) { //$NON-NLS-1$
							osgiBundle[0] = p.toUri().toString();
							return false;
						}
						return true;
					})
					.map(p -> getPathRef(p, -1))
					.forEach(runner::addPlatformEntry);
				}
				if(osgiBundle[0] == null) {
					throw new IllegalStateException("Unable to locate org.eclipse.osgi bundle");
				}
				runner.setOsgiBundle(osgiBundle[0]);
			}
			
			// Do similarly for shared
			{
				Path shared = notesProgram.resolve("osgi").resolve("shared").resolve("eclipse"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if(!Files.exists(shared)) {
					throw new IllegalStateException(MessageFormat.format("shared directory not present at expected location: {0}", shared)); //$NON-NLS-1$
				}
				Path sharedPlugins = shared.resolve("plugins"); //$NON-NLS-1$
				if(!Files.exists(sharedPlugins)) {
					throw new IllegalStateException(MessageFormat.format("shared plugins directory not present at expected location: {0}", sharedPlugins)); //$NON-NLS-1$
				}
				try(Stream<Path> pluginsStream = Files.list(sharedPlugins)) {
					pluginsStream.filter(ODPCompilerServlet::isBundle)
					.map(p -> getPathRef(p, -1))
					.forEach(runner::addPlatformEntry);
				}
			}
			
			if(siteZips != null && !siteZips.isEmpty()) {
				for(Path siteZip : siteZips) {
					Path siteFile = NSFODPUtil.expandZip(siteZip);
					cleanup.add(siteFile);
					Path sitePlugins = siteFile.resolve("plugins"); //$NON-NLS-1$
					if(Files.isDirectory(sitePlugins)) {
						try(Stream<Path> pluginsStream = Files.list(sitePlugins)) {
							pluginsStream.filter(p -> p.getFileName().toString().endsWith(".jar")) //$NON-NLS-1$
								.map(p -> getPathRef(p, -1))
								.forEach(runner::addPlatformEntry);
						}
					}
				}
			}
			
			String compilerLevel = req.getHeader(NSFODPConstants.HEADER_COMPILER_LEVEL);
			runner.addEnvironmentVar(NSFODPConstants.PROP_COMPILERLEVEL, compilerLevel);
			String appendTimestamp = req.getHeader(NSFODPConstants.HEADER_APPEND_TIMESTAMP);
			runner.addEnvironmentVar(NSFODPConstants.PROP_APPENDTIMESTAMPTOTITLE, appendTimestamp);
			String templateName = req.getHeader(NSFODPConstants.HEADER_TEMPLATE_NAME);
			runner.addEnvironmentVar(NSFODPConstants.PROP_TEMPLATENAME, templateName);
			String templateVersion = req.getHeader(NSFODPConstants.HEADER_TEMPLATE_VERSION);
			runner.addEnvironmentVar(NSFODPConstants.PROP_TEMPLATEVERSION, templateVersion);
			String setXspOptions = req.getHeader(NSFODPConstants.HEADER_SET_PRODUCTION_XSP);
			runner.addEnvironmentVar(NSFODPConstants.PROP_SETPRODUCTIONXSPOPTIONS, setXspOptions);
			String odsRelease = req.getHeader(NSFODPConstants.HEADER_ODS_RELEASE);
			runner.addEnvironmentVar(NSFODPConstants.PROP_ODSRELEASE, odsRelease);
			String compileBasicLs = req.getHeader(NSFODPConstants.HEADER_COMPILE_BASICLS);
			runner.addEnvironmentVar(NSFODPConstants.PROP_COMPILEBASICLS, compileBasicLs);
			
			runner.start("org.openntf.nsfodp.compiler.equinox.CompilerApplication", //$NON-NLS-1$
				line -> mon.subTask(line),
				err -> mon.subTask(err)
			);
			
			// Now stream the NSF
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
	
	private static String getDependencyRef(String bundleName, int startLevel) throws IOException {
		Bundle b = Platform.getBundle(bundleName);
		if(b == null) {
			throw new IllegalStateException("Unable to locate bundle: " + bundleName);
		}
		File bundleFile = FileLocator.getBundleFile(b);
		if(bundleFile == null || !bundleFile.exists()) {
			throw new IllegalStateException("Unable to locate path for bundle: " + b);
		}
		
		return getPathRef(bundleFile.toPath(), startLevel);
	}
	
	private static String getPathRef(Path path, int startLevel) {
		if(startLevel < 1) {
			return "reference:" + path.toUri(); //$NON-NLS-1$
		} else {
			return "reference:" + path.toUri() + "@" + startLevel + ":start"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	private static boolean isBundle(Path p) {
		// TODO verify directory bundles further
		return p.getFileName().toString().endsWith(".jar") || Files.isDirectory(p); //$NON-NLS-1$
	}
}
