/**
 * Copyright © 2018-2019 Jesse Gallagher
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
package org.openntf.nsfodp.eclipse.m2e.job;

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
import org.openntf.nsfodp.eclipse.m2e.Messages;

@SuppressWarnings("restriction")
public class DeployNSFJob extends Job {
	
	private final IProject project;

	public DeployNSFJob(IProject project) {
		super(Messages.DeployNSFJob_label);
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
			ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(null, Messages.DeployNSFJob_executingPom);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, pomFile.getParent());
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, "deploy -f " + pomFile.getAbsolutePath()); //$NON-NLS-1$
			workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			ILaunch launch = workingCopy.launch("run", monitor, false, true); //$NON-NLS-1$
			synchronized(workingCopy) {
				while(!launch.isTerminated()) {
					try{ workingCopy.wait(500L); } catch(InterruptedException e){}
				}
			}
			
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch(Exception e) {
			return new Status(IStatus.ERROR, Messages.DeployNSFJob_errorExecutingMaven, "", e); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}

}
