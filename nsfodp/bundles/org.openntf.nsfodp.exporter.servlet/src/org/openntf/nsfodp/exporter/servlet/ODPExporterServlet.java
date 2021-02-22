/**
 * Copyright © 2018-2021 Jesse Gallagher
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
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.nsfodp.commons.LineDelimitedJsonProgressMonitor;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;
import org.openntf.nsfodp.exporter.ODPExporter;
import org.openntf.nsfodp.exporter.ODPExporter.ODPType;

import com.ibm.commons.util.StringUtil;

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
			
			
			try(NotesAPI session = NotesAPI.get()) {
				NDatabase database;
				
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
						Files.copy(reqInputStream, nsfFile, StandardCopyOption.REPLACE_EXISTING);
					}
					
					database = session.openDatabase(nsfFile.toString());
				} else {
					// Then look for an NSF path in the headers
					String databasePath = req.getHeader(NSFODPConstants.HEADER_DATABASE_PATH);
					if(StringUtil.isEmpty(databasePath)) {
						throw new IllegalArgumentException(MessageFormat.format(Messages.ODPExporterServlet_dbPathMissing, NSFODPConstants.HEADER_DATABASE_PATH));
					}
					
					// Verify that the user can indeed export this DB
					try(NotesAPI userApi = NotesAPI.get(user.getName(), false, false)) {
						try(NDatabase userDb = userApi.openDatabase(databasePath)) {
							if(userDb.getCurrentAccessLevel() < 5) { // Designer access
								throw new UnsupportedOperationException(MessageFormat.format(Messages.ODPExporterServlet_insufficientAccess, user.getName(), databasePath));
							}
						}
					}

					database = session.openDatabase(databasePath);
				}
				
				try {
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
					exporter.setProjectName(req.getHeader(NSFODPConstants.HEADER_PROJECT_NAME));
					
					exporter.setOdpType(ODPType.ZIP);
					Path result = exporter.export();
					cleanup.add(result);
					mon.done();
					
					Files.copy(result, os);
				} finally {
					if(post) {
						String filePath = database.getFilePath();
						database.close();
						session.deleteDatabase(filePath);
					}
				}
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
