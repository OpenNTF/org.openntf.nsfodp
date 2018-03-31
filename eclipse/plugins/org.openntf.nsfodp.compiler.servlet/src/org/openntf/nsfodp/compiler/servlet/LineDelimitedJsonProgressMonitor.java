package org.openntf.nsfodp.compiler.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import javax.servlet.ServletOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonObject;

/**
 * An implementation of {@link IProgressMonitor} sends monitor messages to an {@link OutputStream}
 * as line-delimited JSON.
 *  
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class LineDelimitedJsonProgressMonitor implements IProgressMonitor {
	private final ServletOutputStream out;
	private boolean canceled = false;
	
	public LineDelimitedJsonProgressMonitor(ServletOutputStream out) {
		this.out = Objects.requireNonNull(out);
	}

	@Override
	public void beginTask(String name, int totalWork) {
		try {
			out.println(message(
				"type", "beginTask",
				"name", name,
				"totalWork", totalWork
				));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void done() {
		try {
			out.println(message(
				"type", "done"
				));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void internalWorked(double work) {
		try {
			out.println(message(
				"type", "internalWorked",
				"work", work
				));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isCanceled() {
		return this.canceled;
	}

	@Override
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
		if(canceled) {
			try {
				out.println(message(
					"type", "cancel"
					));
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void setTaskName(String name) {
		try {
			out.println(message(
				"type", "task",
				"name", name
				));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void subTask(String name) {
		try {
			out.println(message(
				"type", "subTask",
				"name", name
				));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void worked(int work) {
		try {
			out.println(message(
				"type", "worked",
				"work", work
				));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	// *******************************************************************************
	// * Utility methods
	// *******************************************************************************
	
	public static String message(Object... parts) {
		JsonObject json = new JsonJavaObject();
		for(int i = 0; i < parts.length; i++) {
			String key = StringUtil.toString(parts[i]);
			Object val = i < parts.length-1 ? parts[i+1] : null;
			json.putJsonProperty(key, val);
		}
		
		return json.toString();
	}
}
