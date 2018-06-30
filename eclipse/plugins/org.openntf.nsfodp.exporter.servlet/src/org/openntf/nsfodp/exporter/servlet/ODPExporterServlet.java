package org.openntf.nsfodp.exporter.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.nsfodp.commons.LineDelimitedJsonProgressMonitor;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.exporter.ODPExporter;

import com.ibm.commons.util.io.StreamUtil;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesSession;

public class ODPExporterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static boolean ALLOW_ANONYMOUS = "true".equals(System.getProperty("org.openntf.nsfodp.allowAnonymous")); //$NON-NLS-1$ //$NON-NLS-2$

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
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
				os.println("Anonymous access disallowed");
				return;
			}
			
			String contentType = req.getContentType();
			if(!"application/octet-stream".equals(contentType)) { //$NON-NLS-1$
				throw new IllegalArgumentException("Content must be application/octet-stream");
			}
			
			Path nsfFile = Files.createTempFile(NSFODPUtil.getTempDirectory(), getClass().getName(), ".nsf"); //$NON-NLS-1$
			cleanup.add(nsfFile);
			try(InputStream reqInputStream = req.getInputStream()) {
				try(OutputStream packageOut = Files.newOutputStream(nsfFile)) {
					StreamUtil.copyStream(reqInputStream, packageOut);
				}
			}
			
			NotesSession session = new NotesSession();
			try {
				NotesDatabase database = session.getDatabaseByPath(nsfFile.toString());
				try {
					database.open();
					
					ODPExporter exporter = new ODPExporter(database);
					
					String binaryDxl = req.getHeader(NSFODPConstants.HEADER_BINARY_DXL);
					if("true".equals(binaryDxl)) { //$NON-NLS-1$
						exporter.setBinaryDxl(true);
					}
					String swiperFilter = req.getHeader(NSFODPConstants.HEADER_SWIPER_FILTER);
					if("true".equals(swiperFilter)) { //$NON-NLS-1$
						exporter.setSwiperFilter(true);
					}
					
					Path result = exporter.export();
					cleanup.add(result);
					resp.setContentType("application/zip"); //$NON-NLS-1$
					
					try(ZipOutputStream zos = new ZipOutputStream(os)) {
						Files.walk(result)
							.forEach(path -> {
								ZipEntry entry = new ZipEntry(result.relativize(path).toString().replace('\\', '/'));
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
					database.delete();
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

}
