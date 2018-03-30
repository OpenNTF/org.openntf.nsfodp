package org.openntf.xsp.extlibx.bazaar.odpcompiler.eclipse;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

@SuppressWarnings("restriction")
public class CompileODPJob extends Job {
	
	private final IProject project;

	public CompileODPJob(IProject project) {
		super("Compile ODP");
		this.project = project;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mavenProject = projectManager.getProject(project);
		File pomFile = mavenProject.getPomFile().getAbsoluteFile();
		
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);
			ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(null, "Executing POM");
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, pomFile.getParent());
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, "nsfodp:compile -f " + pomFile.getAbsolutePath()); //$NON-NLS-1$
			workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			ILaunch launch = workingCopy.launch("run", monitor, false, true); //$NON-NLS-1$
			synchronized(workingCopy) {
				while(!launch.isTerminated()) {
					try{ workingCopy.wait(500L); } catch(InterruptedException e){}
				}
			}
			
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch(Exception e) {
			return new Status(IStatus.ERROR, "Error while executing Maven", "", e);
		}
		return Status.OK_STATUS;
	}

}
