/**
 * Copyright © 2018-2019 Jesse Gallagher
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
package org.openntf.nsfodp.exporter.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.nsfodp.commons.LineDelimitedJsonProgressMonitor;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.odp.util.ODPUtil;
import org.openntf.nsfodp.exporter.ODPExporter;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.designer.domino.napi.util.NotesUtils;
import com.ibm.domino.osgi.core.context.ContextInfo;

import lotus.domino.ACL;
import lotus.domino.Database;
import lotus.domino.Session;

public class ODPExporterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static boolean ALLOW_ANONYMOUS = "true".equals(System.getProperty("org.openntf.nsfodp.allowAnonymous")); //$NON-NLS-1$ //$NON-NLS-2$

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handle(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handle(req, resp);
	}
	
	protected void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean post = "POST".equals(req.getMethod()); //$NON-NLS-1$
		
		Principal user = req.getUserPrincipal();
		resp.setBufferSize(0);
		resp.setStatus(HttpServletResponse.SC_OK);
		
		ServletOutputStream os = resp.getOutputStream();
		
		Set<Path> cleanup = new HashSet<>();
		try {
			if(!ALLOW_ANONYMOUS && "Anonymous".equalsIgnoreCase(user.getName())) { //$NON-NLS-1$
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				resp.setContentType("text/plain"); //$NON-NLS-1$
				os.println(Messages.ODPExporterServlet_anonymousAccessDisallowed);
				return;
			}
			
			
			NotesSession session = new NotesSession();
			try {
				NotesDatabase database;
				
				if(post) {
					// Then read the NSF from the body
					
					String contentType = req.getContentType();
					if(!"application/octet-stream".equals(contentType)) { //$NON-NLS-1$
						throw new IllegalArgumentException(MessageFormat.format(
								Messages.ODPExporterServlet_mismatchedContentType,
								NSFODPConstants.HEADER_DATABASE_PATH, contentType));
					}
					
					Path nsfFile = Files.createTempFile(NSFODPUtil.getTempDirectory(), getClass().getName(), ".nsf"); //$NON-NLS-1$
					cleanup.add(nsfFile);
					try(InputStream reqInputStream = req.getInputStream()) {
						try(OutputStream packageOut = Files.newOutputStream(nsfFile)) {
							StreamUtil.copyStream(reqInputStream, packageOut);
						}
					}
					
					database = session.getDatabaseByPath(nsfFile.toString());
				} else {
					// Then look for an NSF path in the headers
					String databasePath = req.getHeader(NSFODPConstants.HEADER_DATABASE_PATH);
					if(StringUtil.isEmpty(databasePath)) {
						throw new IllegalArgumentException(MessageFormat.format(Messages.ODPExporterServlet_dbPathMissing, NSFODPConstants.HEADER_DATABASE_PATH));
					}
					
					// Verify that the user can indeed export this DB
					Session lotusSession = ContextInfo.getUserSession();
					Database lotusDatabase = ODPUtil.getDatabase(lotusSession, databasePath);
					if(!lotusDatabase.isOpen()) {
						throw new UnsupportedOperationException(MessageFormat.format(Messages.ODPExporterServlet_unableToOpenDb, databasePath));
					} else if(lotusDatabase.queryAccess(lotusSession.getEffectiveUserName()) < ACL.LEVEL_DESIGNER) {
						// Note: this uses queryAccess to skip past Maximum Internet Access levels
						throw new UnsupportedOperationException(MessageFormat.format(Messages.ODPExporterServlet_insufficientAccess, NotesUtils.DNAbbreviate(lotusSession.getEffectiveUserName()), databasePath));
					}

					database = session.getDatabaseByPath(databasePath);
				}
				
				try {
					database.open();
					
					IProgressMonitor mon = new LineDelimitedJsonProgressMonitor(os);
					
					ODPExporter exporter = new ODPExporter(database);
					
					String binaryDxl = req.getHeader(NSFODPConstants.HEADER_BINARY_DXL);
					if("true".equals(binaryDxl)) { //$NON-NLS-1$
						exporter.setBinaryDxl(true);
					}
					String swiperFilter = req.getHeader(NSFODPConstants.HEADER_SWIPER_FILTER);
					if("true".equals(swiperFilter)) { //$NON-NLS-1$
						exporter.setSwiperFilter(true);
					}
					String richTextAsItemData = req.getHeader(NSFODPConstants.HEADER_RICH_TEXT_AS_ITEM_DATA);
					if("true".equals(richTextAsItemData)) { //$NON-NLS-1$
						exporter.setRichTextAsItemData(true);
					}
					
					Path result = exporter.export();
					cleanup.add(result);
					mon.done();
					
					try(ZipOutputStream zos = new ZipOutputStream(os)) {
						Files.walk(result)
							.forEach(path -> {
								String name = result.relativize(path).toString().replace('\\', '/');
								if(Files.isDirectory(path)) {
									name += '/';
								}
								ZipEntry entry = new ZipEntry(name);
								try {
									zos.putNextEntry(entry);
	
									if(Files.isRegularFile(path)) {
										try(InputStream is = Files.newInputStream(path)) {
											StreamUtil.copyStream(is, zos);
										}
									}
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							});
					}
					
				} finally {
					if(post) {
						database.delete();
					}
				}
			} finally {
				session.recycle();
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
