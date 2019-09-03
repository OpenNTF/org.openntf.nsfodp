package org.openntf.nsfodp.designer.odpwatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.refresh.IRefreshMonitor;
import org.eclipse.core.resources.refresh.IRefreshResult;
import org.eclipse.core.resources.refresh.RefreshProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

public class ODPRefreshProvider extends RefreshProvider {
	public static final int INTERVAL = 10; // seconds
	
	private final IProgressMonitor nullMon = new NullProgressMonitor();
	
	private final ScheduledExecutorService exec = Executors.newScheduledThreadPool(5);
	
	protected IRefreshMonitor createPollingMonitor(IResource resource) {
		Activator.getDefault().log(IStatus.INFO, ">>> asked createPollingMonitor for " + resource);
		return null;
	}
	
	public IRefreshMonitor installMonitor(IResource resource, IRefreshResult result, IProgressMonitor progressMonitor) {
		if(resource instanceof IProject) {
			IProject project = (IProject)resource;
			if(isOdpProject(project)) {
				PollingRefreshMonitor mon = new PollingRefreshMonitor(result, project);
				mon.setFuture(exec.scheduleAtFixedRate(mon, 1, INTERVAL, TimeUnit.SECONDS));
				return mon;
			}
		}
		
		return null;
	}
	
	@Override
	public void resetMonitors(IResource resource, IProgressMonitor progressMonitor) {
		Activator.getDefault().log(IStatus.INFO, ">>> asked resetMonitors for " + resource);
	}
	
	// *******************************************************************************
	// * Original API
	// *******************************************************************************
	
	@Override
	@Deprecated
	public IRefreshMonitor installMonitor(IResource resource, IRefreshResult result) {
		return installMonitor(resource, result, nullMon);
	}
	
	@Override
	@Deprecated
	public void resetMonitors(IResource resource) {
		resetMonitors(resource, nullMon);
	}
	
	// *******************************************************************************
	// * Internal utilities
	// *******************************************************************************
	
	private class PollingRefreshMonitor implements Runnable, IRefreshMonitor {

		private ScheduledFuture<?> future;
		private final IRefreshResult result;
		private final IProject project;
		
		public PollingRefreshMonitor(IRefreshResult result, IProject project) {
			this.result = result;
			this.project = project;
		}
		
		@Override
		public void run() {
			Activator.getDefault().log(IStatus.WARNING, "Refreshing " + project);
			result.refresh(project);
		}
		
		public void setFuture(ScheduledFuture<?> future) {
			this.future = future;
		}

		@Override
		public void unmonitor(IResource resource) {
			if(project.equals(resource)) {
				future.cancel(true);
			}
		}
		
	}
	
	private boolean isOdpProject(IProject project) {
		// Since there's no project nature, check in the .project file
		IFile projectFile = project.getFile(".project"); //$NON-NLS-1$
		if(projectFile.exists()) {
			try {
				// Do a naive search for the sync builder name
				StringBuilder contents = new StringBuilder();
				try(InputStream is = projectFile.getContents()) {
					try(BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
						String line;
						while((line = r.readLine()) != null) {
							contents.append(line);
						}
					}
				}
				return contents.indexOf("com.ibm.designer.domino.team.builder.PhysicalToNsfSynBuilder") > -1; //$NON-NLS-1$
			} catch(IOException | CoreException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}
}
