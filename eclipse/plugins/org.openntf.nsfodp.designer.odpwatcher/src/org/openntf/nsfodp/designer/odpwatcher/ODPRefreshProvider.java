package org.openntf.nsfodp.designer.odpwatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class ODPRefreshProvider extends RefreshProvider {
	public static final int INTERVAL = 10 * 1000; // milliseconds
	
	private final IProgressMonitor nullMon = new NullProgressMonitor();
	
	protected IRefreshMonitor createPollingMonitor(IResource resource) {
		return null;
	}
	
	public IRefreshMonitor installMonitor(IResource resource, IRefreshResult result, IProgressMonitor progressMonitor) {
		if(resource instanceof IProject) {
			IProject project = (IProject)resource;
			if(isOdpProject(project)) {
				PollingRefreshMonitor mon = new PollingRefreshMonitor(result, project);
				mon.schedule(INTERVAL);
				return mon;
			}
		}
		
		return null;
	}
	
	@Override
	public void resetMonitors(IResource resource, IProgressMonitor progressMonitor) {
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
	
	private static class PollingRefreshMonitor extends Job implements IRefreshMonitor {

		private final IRefreshResult result;
		private final IProject project;
		
		public PollingRefreshMonitor(IRefreshResult result, IProject project) {
			super("ODP filesystem refresh monitor for " + project.getName());
			
			this.result = result;
			this.project = project;
		}

		@Override
		public void unmonitor(IResource resource) {
			if(project.equals(resource)) {
				this.cancel();
			}
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Refreshing " + project.getName(), 1);
			
			// Only refresh if the filesystem thinks it's still connected
			//   ...and hope that this doesn't trigger a refresh anyway
			File projectFile = new File(project.getLocationURI());
			if(projectFile.exists() && projectFile.isDirectory()) {
				result.refresh(project);
			}
			
			monitor.worked(1);
			monitor.done();
			
			schedule(INTERVAL);
			return Status.OK_STATUS;
		}
		
	}
	
	private boolean isOdpProject(IProject project) {
		// Check to see if it's a filesystem path
		URI uri = project.getLocationURI();
		if(!"file".equals(uri.getScheme())) { //$NON-NLS-1$
			return false;
		}
		
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
