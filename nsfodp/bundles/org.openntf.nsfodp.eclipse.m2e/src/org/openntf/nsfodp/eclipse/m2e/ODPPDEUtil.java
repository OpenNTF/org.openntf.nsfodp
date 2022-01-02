/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.nsfodp.eclipse.m2e;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.MavenProjectUtils;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.ClasspathComputer;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelDelta;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.core.util.CoreUtility;

@SuppressWarnings("restriction")
public enum ODPPDEUtil {
	INSTANCE;

    private static boolean isListeningForPluginModelChanges = false;
    private static final List<IProject> projectsForUpdateClasspath = new ArrayList<IProject>();
	private static final IPluginModelListener classpathUpdater = new IPluginModelListener()
    {
        public void modelsChanged( PluginModelDelta delta )
        {
            synchronized ( projectsForUpdateClasspath )
            {
                if ( projectsForUpdateClasspath.size() == 0 )
                {
                    return;
                }

                Iterator<IProject> projectsIter = projectsForUpdateClasspath.iterator();
                while ( projectsIter.hasNext() )
                {
                    IProject project = projectsIter.next();
                    IPluginModelBase model = PluginRegistry.findModel( project );
                    if ( model == null )
                    {
                        continue;
                    }

                    UpdateClasspathWorkspaceJob job = new UpdateClasspathWorkspaceJob( project, model );
                    job.schedule();
                    projectsIter.remove();
                }
            }
        }
    };

	private static class UpdateClasspathWorkspaceJob extends WorkspaceJob {
		private final IProject project;

		private final IPluginModelBase model;

		public UpdateClasspathWorkspaceJob(IProject project, IPluginModelBase model) {
			super(Messages.ODPPDEUtil_updatingClasspath);
			this.project = project;
			this.model = model;
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
			setClasspath(project, model, monitor);
			return Status.OK_STATUS;
		}
	}
	
	public void addPDENature(IProject project, MavenProject mavenProject, IProgressMonitor monitor) throws CoreException {
		// Add the project natures
		if(!project.hasNature(PDE.PLUGIN_NATURE)) {
			CoreUtility.addNatureToProject(project, PDE.PLUGIN_NATURE, null);
		}
		if(!project.hasNature(JavaCore.NATURE_ID)) {
			CoreUtility.addNatureToProject(project, JavaCore.NATURE_ID, null);
		}

        // PDE can't handle default JDT classpath
        IJavaProject javaProject = JavaCore.create(project);
        javaProject.setOutputLocation(getOutputLocation(project, mavenProject, monitor), monitor);
		
		 // see org.eclipse.pde.internal.ui.wizards.tools.UpdateClasspathJob
        // PDE populates the model cache lazily from WorkspacePluginModelManager.visit() ResourceChangeListenter
        // That means the model may be available or not at this point in the lifecycle.
        // If it is, update its classpath right away.
        // If not add the project to the list to be updated later based on model change events.
        IPluginModelBase model = PluginRegistry.findModel(project);
        if(model != null) {
            setClasspath(project, model, monitor);
        } else {
            addProjectForUpdateClasspath(project);
        }
	}
	
	private IPath getOutputLocation(IProject project, MavenProject mavenProject, IProgressMonitor monitor)
			throws CoreException {
		File outputDirectory = new File(mavenProject.getBuild().getOutputDirectory());
		outputDirectory.mkdirs();
		IPath relPath = MavenProjectUtils.getProjectRelativePath(project, mavenProject.getBuild().getOutputDirectory());
		IFolder folder = project.getFolder(relPath);
		folder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		return folder.getFullPath();
	}

	private void addProjectForUpdateClasspath(IProject project) {
		synchronized (projectsForUpdateClasspath) {
			projectsForUpdateClasspath.add(project);
			if (!isListeningForPluginModelChanges) {
				PDECore.getDefault().getModelManager().addPluginModelListener(classpathUpdater);
				isListeningForPluginModelChanges = true;
			}
		}
	}
	
	private static void setClasspath(IProject project, IPluginModelBase model, IProgressMonitor monitor) throws CoreException {
		IClasspathEntry[] entries = ClasspathComputer.getClasspath(project, model, null, true, true);
		List<IClasspathEntry> resolvedEntries = new ArrayList<>(Arrays.asList(entries));
		
		// This may not have included the source folders from build.properties for some reason,
		//  so compute them manually
		IFile buildProperties = project.getFile("build.properties"); //$NON-NLS-1$
		if(buildProperties.isAccessible()) {
			Properties props = new Properties();
			try(InputStream is = buildProperties.getContents()) {
				props.load(is);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, Messages.ODPPDEUtil_errorBuildProperties, e.toString(), e));
			}
			for(Map.Entry<Object, Object> entry : props.entrySet()) {
				String key = String.valueOf(entry.getKey());
				if(key.startsWith("source.")) { //$NON-NLS-1$
					if(entry.getValue() == null) {
						continue;
					}
					
					String val = String.valueOf(entry.getValue());
					String[] paths = val.split(","); //$NON-NLS-1$
					for(String path : paths) {
						if(
							resolvedEntries.stream()
								.map(e -> e.getPath().makeRelativeTo(project.getFullPath()))
								.map(Object::toString)
								.anyMatch(e -> e.equals(path))
							) {
							continue;
						}
						
						IPath eclipsePath = new Path(path);
						IClasspathEntry classpathEntry = new ClasspathEntry(
								IPackageFragmentRoot.K_SOURCE,
								IClasspathEntry.CPE_SOURCE,
								eclipsePath,
								new IPath[0],
								new IPath[] { new Path("**/*.metadata") }, //$NON-NLS-1$
								null,
								null,
								null, // TODO look up output entry
								true,
								null,
								false,
								new IClasspathAttribute[0]
						);
						resolvedEntries.add(classpathEntry);
						
						// Make sure it exists
						if(!project.exists(eclipsePath)) {
							IFolder folder = project.getFolder(eclipsePath);
							folder.create(false, false, SubMonitor.convert(monitor));
						}
					}
				}
			}
		}
		
		Collections.sort(resolvedEntries, Comparator.comparing(IClasspathEntry::getEntryKind).reversed());
		
		JavaCore.create(project).setRawClasspath(resolvedEntries.toArray(new IClasspathEntry[resolvedEntries.size()]), null);
		
		// Try to kick PDE to update the classpath
		IFile manifest = getBundleManifest(project);
		if (manifest.isAccessible()) {
			manifest.touch(monitor);
		}
	}
	
    /**
     * Returns bundle manifest as known to PDE project metadata. Returned file may not exist in the workspace or on the
     * filesystem. Never returns null.
     */
	public static IFile getBundleManifest(IProject project) throws CoreException {
		// PDE API is very inconvenient, lets use internal classes instead
		IContainer metainf = PDEProject.getBundleRoot(project);
		if (metainf == null || metainf instanceof IProject) {
			metainf = project.getFolder("META-INF"); //$NON-NLS-1$
		} else {
			metainf = metainf.getFolder(new Path("META-INF")); //$NON-NLS-1$
		}

		return metainf.getFile(new Path("MANIFEST.MF")); //$NON-NLS-1$
	}
	
	/**
	 * Resources expected to be generated by the GeneratePDEStructureMojo in Maven
	 * @since 3.3.0
	 */
	private static final String[] DERIVED_FILES = { "build.properties", "META-INF/MANIFEST.MF" }; //$NON-NLS-1$ //$NON-NLS-2$
	/**
	 * Marks resources that are expected to be generated by the GeneratePDEStructureMojo in Maven
	 * as derived in the Eclipse project.
	 * 
	 * @param project the project to process
	 * @param monitor the progress monitor to report to
	 * @throws CoreException if there is a problem processing the derived files
	 * @since 3.3.0
	 */
	public void markPDEResourcesDerived(IProject project, IProgressMonitor monitor) throws CoreException {
		for(String pathName : DERIVED_FILES) {
			IFile file = project.getFile(pathName);
			if(file.exists()) {
				file.setDerived(true, monitor);
			}
		}
	}
	
	/**
	 * Executes the given Maven goal on the provided project and waits for it to complete.
	 * 
	 * @param project the context project
	 * @param monitor a {@link IProgressMonitor} implementation
	 * @param goal the goal to execute, e.g. {@code "nsfodp:compile"}
	 * @return an {@link IStatus} implementation indicating the result
	 * @since 3.4.0
	 */
	public IStatus executeMavenGoal(IProject project, IProgressMonitor monitor, String goal) {
		IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mavenProject = projectManager.getProject(project);
		File pomFile = mavenProject.getPomFile().getAbsoluteFile();
		
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);
			ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(null, Messages.CompileODPJob_executingPOM);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, pomFile.getParent());
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, goal + " -f " + pomFile.getAbsolutePath()); //$NON-NLS-1$
			workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			ILaunch launch = workingCopy.launch("run", monitor, false, true); //$NON-NLS-1$
			synchronized(workingCopy) {
				while(!launch.isTerminated()) {
					try{ workingCopy.wait(500L); } catch(InterruptedException e){}
				}
			}
			
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch(Exception e) {
			return new Status(IStatus.ERROR, Messages.CompileODPJob_errorExecutingMaven, "", e); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}
}
