package org.openntf.nsfodp.exporter.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.nsfodp.commons.odp.designfs.DesignFileSystemProvider;
import org.openntf.nsfodp.commons.odp.designfs.util.DesignPathUtil;

import lotus.domino.NotesFactory;
import lotus.domino.Session;

public class FileSystemServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain"); //$NON-NLS-1$
		
		PrintWriter w = resp.getWriter();
		try {
			// TODO use the current user
			Session session = NotesFactory.createSession();
			try {
				URI uri = DesignPathUtil.toFileSystemURI(session.getEffectiveUserName(), "dev/design.nsf"); //$NON-NLS-1$
				
				try(FileSystem fs = DesignFileSystemProvider.instance.getOrCreateFileSystem(uri, Collections.emptyMap())) {
					Path root = fs.getPath("/"); //$NON-NLS-1$
					
					Files.walk(root).forEach(p -> w.println(p));
				}
			} finally {
				session.recycle();
			}
		} catch(Throwable t) {
			t.printStackTrace(w);
		} finally {
			w.close();
		}
	}
}
