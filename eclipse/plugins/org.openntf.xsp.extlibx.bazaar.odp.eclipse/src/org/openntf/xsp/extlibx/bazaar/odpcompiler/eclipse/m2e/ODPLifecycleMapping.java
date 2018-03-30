/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.xsp.extlibx.bazaar.odpcompiler.eclipse.m2e;

import java.util.Set;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.internal.project.registry.AbstractMavenDependencyResolver;
import org.eclipse.m2e.core.internal.project.registry.Capability;
import org.eclipse.m2e.core.internal.project.registry.ILifecycleMapping2;
import org.eclipse.m2e.core.internal.project.registry.RequiredCapability;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractCustomizableLifecycleMapping;
import org.eclipse.m2e.core.project.configurator.ILifecycleMapping;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.openntf.xsp.extlibx.bazaar.odpcompiler.eclipse.nature.OnDiskProjectNature;

@SuppressWarnings("restriction")
public class ODPLifecycleMapping extends AbstractCustomizableLifecycleMapping implements ILifecycleMapping, ILifecycleMapping2 {
	
    private static final AbstractMavenDependencyResolver NOOP_DEPENDENCY_RESOLVER =
            new AbstractMavenDependencyResolver()
            {
                @Override
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
    	
    	MavenProject mavenProject = request.getMavenProject();
    	IProject project = request.getProject();
    	
    	String packaging = mavenProject.getPackaging();
    	if("domino-nsf".equals(packaging)) {
    		ODPPDEUtil.INSTANCE.addPDENature(project, mavenProject, mon);
    		if(!project.hasNature(OnDiskProjectNature.ID)) {
    			CoreUtility.addNatureToProject(project, OnDiskProjectNature.ID, null);
    		}
    	}
    }
            
	@Override
	public AbstractMavenDependencyResolver getDependencyResolver(IProgressMonitor monitor) {
		return NOOP_DEPENDENCY_RESOLVER;
	}

}
