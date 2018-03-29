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
    	}
    }
            
	@Override
	public AbstractMavenDependencyResolver getDependencyResolver(IProgressMonitor monitor) {
		return NOOP_DEPENDENCY_RESOLVER;
	}

}
