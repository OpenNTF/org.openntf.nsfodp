package org.openntf.nsfodp.deployment;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.domino.osgi.core.context.ContextInfo;

import lotus.domino.Session;

public class ReplaceDesignTaskLocal implements Runnable {

	private final String targetDbName;
	private final Path templatePath;
	private final IProgressMonitor monitor;
	
	public ReplaceDesignTaskLocal(String targetDbName, Path templatePath, IProgressMonitor monitor) {
		this.targetDbName = Objects.requireNonNull(targetDbName, "targetDbName cannot be null");
		this.templatePath = Objects.requireNonNull(templatePath, "templatePath cannot be null");
		this.monitor = monitor;
	}

	@Override
	public void run() {
		monitor.setTaskName("Replace Design");
		
		try {
			Session session = ContextInfo.getUserSession();
			
			String command = MessageFormat.format("load convert -d {0} * {1}", targetDbName, templatePath.toAbsolutePath().toString());
			session.sendConsoleCommand("", command);
			
			monitor.done();
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
