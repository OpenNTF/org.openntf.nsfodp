/*
 * Copyright (c) 2018-2025 Jesse Gallagher
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ILifecycleMappingConfiguration;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.openntf.nsfodp.eclipse.nature.OnDiskProjectNature;

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
		// m2e in Eclipse 2022-09 changed these methods significantly
		MavenProject mavenProject;
		IProject project;
		try {
			Method getMavenProject = request.getClass().getMethod("mavenProject"); //$NON-NLS-1$
			mavenProject = (MavenProject) getMavenProject.invoke(request);
			Method getMavenProjectFacade = request.getClass().getMethod("mavenProjectFacade"); //$NON-NLS-1$
			Object mavenProjectFacade = getMavenProjectFacade.invoke(request);
			Method getProject = mavenProjectFacade.getClass().getMethod("getProject"); //$NON-NLS-1$
			project = (IProject) getProject.invoke(mavenProjectFacade);
		} catch (NoSuchMethodException | NoSuchMethodError | IllegalAccessException | InvocationTargetException e) {
			// Then it's earlier than 2022-09
			try {
				Method getMavenProject = request.getClass().getMethod("getMavenProject"); //$NON-NLS-1$
				mavenProject = (MavenProject) getMavenProject.invoke(request);
				Method getProject = request.getClass().getMethod("getProject"); //$NON-NLS-1$
				project = (IProject) getProject.invoke(request);
			} catch (Exception e2) {
				IStatus status = new Status(8, "Exception reading m2e objects",
						"Encountered exception trying to access pre-2022-09 m2e objects", e2);
				throw new CoreException(status);
			}
		}

		ODPPDEUtil.INSTANCE.addPDENature(project, mavenProject, monitor);

		if(!project.hasNature(OnDiskProjectNature.ID)) {
			CoreUtility.addNatureToProject(project, OnDiskProjectNature.ID, null);
		}
		
		ODPPDEUtil.INSTANCE.markPDEResourcesDerived(project, SubMonitor.convert(monitor));
	}

	@Override
	public boolean hasConfigurationChanged(IMavenProjectFacade newFacade,
			ILifecycleMappingConfiguration oldProjectConfiguration, MojoExecutionKey key, IProgressMonitor monitor) {
		return false;
	}
}
