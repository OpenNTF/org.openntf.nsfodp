/**
 * Copyright © 2018-2023 Jesse Gallagher
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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.m2e.core.internal.project.registry.AbstractMavenDependencyResolver;
import org.eclipse.m2e.core.internal.project.registry.Capability;
import org.eclipse.m2e.core.internal.project.registry.ILifecycleMapping2;
import org.eclipse.m2e.core.internal.project.registry.RequiredCapability;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractCustomizableLifecycleMapping;
import org.eclipse.m2e.core.project.configurator.ILifecycleMapping;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.openntf.nsfodp.eclipse.nature.OnDiskProjectNature;

@SuppressWarnings("restriction")
public class ODPLifecycleMapping extends AbstractCustomizableLifecycleMapping implements ILifecycleMapping, ILifecycleMapping2 {
	
    private static final AbstractMavenDependencyResolver NOOP_DEPENDENCY_RESOLVER =
            new AbstractMavenDependencyResolver()
            {
    	// No longer present in newer versions
//                @Override
                @SuppressWarnings("unused")
				public void resolveProjectDependencies( IMavenProjectFacade facade, MavenExecutionRequest mavenRequest,
                                                        Set<Capability> capabilities, Set<RequiredCapability> requirements,
                                                        IProgressMonitor monitor )
                    throws CoreException
                {
                }
            };

    @Override
    public void configure(ProjectConfigurationRequest request, IProgressMonitor mon) throws CoreException {
    	super.configure(request, mon);
    	
    	// m2e in Eclipse 2022-09 changed these methods significantly
    	MavenProject mavenProject;
    	IProject project;
    	try {
    		Method getMavenProject = request.getClass().getMethod("mavenProject"); //$NON-NLS-1$
    		mavenProject = (MavenProject)getMavenProject.invoke(request);
    		Method getMavenProjectFacade = request.getClass().getMethod("mavenProjectFacade"); //$NON-NLS-1$
    		Object mavenProjectFacade = getMavenProjectFacade.invoke(request);
    		Method getProject = mavenProjectFacade.getClass().getMethod("getProject"); //$NON-NLS-1$
    		project = (IProject)getProject.invoke(mavenProjectFacade);
    	} catch(NoSuchMethodException | NoSuchMethodError | IllegalAccessException | InvocationTargetException e) {
    		// Then it's earlier than 2022-09
    		try {
	    		Method getMavenProject = request.getClass().getMethod("getMavenProject"); //$NON-NLS-1$
	    		mavenProject = (MavenProject)getMavenProject.invoke(request);
	    		Method getProject = request.getClass().getMethod("getProject"); //$NON-NLS-1$
	    		project = (IProject)getProject.invoke(request);
    		} catch(Exception e2) {
    			IStatus status = new Status(8, "Exception reading m2e objects", "Encountered exception trying to access pre-2022-09 m2e objects", e2);
    			throw new CoreException(status);
    		}
    	}
    	
    	String packaging = mavenProject.getPackaging();
    	if("domino-nsf".equals(packaging)) { //$NON-NLS-1$
    		ODPPDEUtil.INSTANCE.addPDENature(project, mavenProject, mon);
    		if(!project.hasNature(OnDiskProjectNature.ID)) {
    			CoreUtility.addNatureToProject(project, OnDiskProjectNature.ID, null);
    		}
    		ODPPDEUtil.INSTANCE.markPDEResourcesDerived(project, SubMonitor.convert(mon));
    	}
    }
            
	@Override
	public AbstractMavenDependencyResolver getDependencyResolver(IProgressMonitor monitor) {
		return NOOP_DEPENDENCY_RESOLVER;
	}

}
