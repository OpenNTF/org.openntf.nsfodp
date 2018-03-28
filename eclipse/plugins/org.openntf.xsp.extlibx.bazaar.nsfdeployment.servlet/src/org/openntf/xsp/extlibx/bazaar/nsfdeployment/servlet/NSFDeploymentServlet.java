package org.openntf.xsp.extlibx.bazaar.nsfdeployment.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.xsp.extlibx.bazaar.nsfdeployment.DeployNSFTask;

import com.ibm.commons.util.io.StreamUtil;
import com.ibm.xsp.http.fileupload.FileItem;
import com.ibm.xsp.http.fileupload.disk.DiskFileItemFactory;
import com.ibm.xsp.http.fileupload.servlet.ServletFileUpload;

public class NSFDeploymentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * The POST param used for the "replace design" parameter.
	 */
	public static final String PARAM_REPLACE_DESIGN = "replaceDesign";
	/**
	 * The POST param used for the uploaded file.
	 */
	public static final String PARAM_FILE = "file";
	/**
	 * The POST param used for the destination path.
	 */
	public static final String PARAM_DEST_PATH = "destPath";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Principal user = req.getUserPrincipal();
		resp.setBufferSize(0);
		
		OutputStream os = resp.getOutputStream();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			if("Anonymous".equalsIgnoreCase(user.getName())) {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				resp.setContentType("text/plain");
				os.write("Anonymous access disallowed".getBytes());
				return;
			}
			
			if(!ServletFileUpload.isMultipartContent(req)) {
				throw new IllegalArgumentException("POST body must be a multipart upload");
			}
			ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
			Map<String, List<FileItem>> param = upload.parseParameterMap(req);
			
			if(!param.containsKey(PARAM_DEST_PATH)) {
				throw new IllegalArgumentException("Content must include a " + PARAM_DEST_PATH + " component");
			}
			FileItem destFileItem = param.get(PARAM_DEST_PATH).get(0);
			if(!destFileItem.isFormField()) {
				throw new IllegalArgumentException(PARAM_DEST_PATH + " must not be a file");
			}
			String destPath = destFileItem.getString();

			boolean replaceDesign = false;
			if(param.containsKey(PARAM_REPLACE_DESIGN)) {
				FileItem replaceDesignItem = param.get(PARAM_REPLACE_DESIGN).get(0);
				if(!replaceDesignItem.isFormField()) {
					throw new IllegalArgumentException(PARAM_REPLACE_DESIGN + " must not be a file");
				}
				replaceDesign = Boolean.valueOf(replaceDesignItem.getString());
			}
			
			if(!param.containsKey(PARAM_FILE)) {
				throw new IllegalArgumentException("Content must include a " + PARAM_FILE + " component");
			}
			FileItem fileItem = param.get(PARAM_FILE).get(0);
			if(fileItem.isFormField()) {
				throw new IllegalArgumentException(PARAM_FILE + " part must be a file");
			}
			Path nsf = Files.createTempFile("nsfdeployment", ".data");
			nsf.toFile().deleteOnExit();
			try(InputStream reqInputStream = fileItem.getInputStream()) {
				try(OutputStream packageOut = Files.newOutputStream(nsf)) {
					StreamUtil.copyStream(reqInputStream, packageOut);
				}
			}
			if(String.valueOf(fileItem.getContentType()).startsWith("application/zip")) {
				// If it's a ZIP, expand it - otherwise, use the file content as-is
				Path expanded = Files.createTempFile("nsfdeployment", ".nsf");
				try(ZipFile zf = new ZipFile(nsf.toFile())) {
					ZipEntry firstEntry = zf.entries().nextElement();
					if(firstEntry == null) {
						throw new IllegalArgumentException("ZIP file must contain an entry");
					}
					try(InputStream is = zf.getInputStream(firstEntry)) {
						Files.copy(is, expanded, StandardCopyOption.REPLACE_EXISTING);
						expanded.toFile().deleteOnExit();
						nsf = expanded;
					}
				}
			}
			
			DeployNSFTask task = new DeployNSFTask(nsf, destPath, replaceDesign);
			task.run();
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("text/plain");
			os.write(("NSF successfully deployed to " + destPath).getBytes());
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
}
