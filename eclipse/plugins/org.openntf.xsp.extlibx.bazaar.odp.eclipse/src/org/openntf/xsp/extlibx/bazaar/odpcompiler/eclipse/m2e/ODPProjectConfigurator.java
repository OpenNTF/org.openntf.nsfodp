package org.openntf.xsp.extlibx.bazaar.odpcompiler.eclipse.m2e;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ILifecycleMappingConfiguration;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.eclipse.nature.OnDiskProjectNature;

@SuppressWarnings("restriction")
public class ODPProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

	@Override
	public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor) throws CoreException {

	}

	@Override
	public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath, IProgressMonitor monitor) throws CoreException {

	}

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		IProject project = request.getProject();
    	MavenProject mavenProject = request.getMavenProject();
		ODPPDEUtil.INSTANCE.addPDENature(project, mavenProject, monitor);

		if(!project.hasNature(OnDiskProjectNature.ID)) {
			CoreUtility.addNatureToProject(project, OnDiskProjectNature.ID, null);
		}
	}

	@Override
	public boolean hasConfigurationChanged(IMavenProjectFacade newFacade,
			ILifecycleMappingConfiguration oldProjectConfiguration, MojoExecutionKey key, IProgressMonitor monitor) {
		return false;
	}
}
