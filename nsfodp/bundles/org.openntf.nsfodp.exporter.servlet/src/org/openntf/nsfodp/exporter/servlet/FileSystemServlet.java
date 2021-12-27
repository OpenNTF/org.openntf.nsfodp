package org.openntf.nsfodp.exporter.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.nsfodp.commons.odp.designfs.util.DesignPathUtil;

import lotus.domino.NotesFactory;
import lotus.domino.Session;

public class FileSystemServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		
		PrintWriter w = resp.getWriter();
		try {
			// TODO use the current user
			Session session = NotesFactory.createSession();
			try {
				URI uri = DesignPathUtil.toFileSystemURI(session.getEffectiveUserName(), "dev/design.nsf");
				w.println(uri);
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
