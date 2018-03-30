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
package org.openntf.nsdodp.compiler.eclipse.m2e;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
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
import org.openntf.domino.utils.xml.XMLDocument;
import org.openntf.domino.utils.xml.XMLNodeList;
import org.xml.sax.SAXException;

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
			super("Updating classpath");
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
		// Create stub PDE entities if they don't exist
		IFile buildProperties = project.getFile("build.properties");
		if(buildProperties.exists()) {
			buildProperties.delete(false, null);
		}
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			baos.write(("bin.includes = META-INF/,\\\n" + 
					"               .\n" +
					"output.. = target/classes\n").getBytes());
			baos.write("source.. = ".getBytes());
			List<String> sourceFolders = findSourceFolders(project);
			baos.write(
				sourceFolders.stream()
					.collect(Collectors.joining(",\\\n "))
					.getBytes()
			);
			try(InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
    			buildProperties.create(is, true, null);		
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, "Exception while creating build.properties", "", e));
		}
		IFolder metaInf = project.getFolder("META-INF");
		if(!metaInf.exists()) {
			metaInf.create(false, true, null);
		}
		IFile manifestMf = metaInf.getFile("MANIFEST.MF");
		if(manifestMf.exists()) {
			manifestMf.delete(false, null);
		}
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			baos.write(("Manifest-Version: 1.0\n" + 
					"Bundle-ManifestVersion: 2\n" + 
					"Bundle-Name: " + project.getName() + "\n" + 
					"Bundle-SymbolicName: " + project.getName() + "\n" + 
					"Bundle-Version: 1.0.0.qualifier\n").getBytes()); // TODO translate version
			
			// Look for dependencies in the plugin.xml
			IFolder odp = project.getFolder("odp"); // look up actual source in config
			IFile pluginXmlFile = odp.getFile("plugin.xml");
			if(pluginXmlFile.exists()) {
				try(InputStream is = pluginXmlFile.getContents()) {
					try {
						XMLDocument pluginXml = new XMLDocument();
						pluginXml.loadInputStream(is);
						XMLNodeList nodes = pluginXml.selectNodes("/plugin/requires/import");
						if(nodes.size() > 0) {
							baos.write("Require-Bundle: ".getBytes());
							baos.write(
								nodes.stream()
									.map(e -> e.getAttribute("plugin"))
									.collect(Collectors.joining(",\n "))
									.getBytes()
							);
							baos.write('\n');
						}
					} catch(RuntimeException | SAXException | ParserConfigurationException e) {
						throw new CoreException(new Status(IStatus.ERROR, "Exception while creating MANIFEST.MF", "", e));
					}
				}
			}
			
			// Look for jars in Code/Jars and WebContent/WEB-INF/lib
			List<String> jarPaths = new ArrayList<String>();
			IFolder code = odp.getFolder("Code");
			if(code.exists()) {
				IFolder jars = code.getFolder("Jars");
				if(jars.exists()) {
					for(IResource res : jars.members()) {
						if(res instanceof IFile && res.getFileExtension().equals("jar")) {
							jarPaths.add("odp/Code/Jars/" + res.getName());
						}
					}
				}
			}
			IFolder webContent = odp.getFolder("WebContent");
			if(webContent.exists()) {
				IFolder webInf = webContent.getFolder("WEB-INF");
				if(webInf.exists()) {
					IFolder lib = webInf.getFolder("lib");
					if(lib.exists()) {
						for(IResource res : lib.members()) {
							if(res instanceof IFile && res.getFileExtension().equals("jar")) {
								jarPaths.add("odp/WebContent/WEB-INF/lib/" + res.getName());
							}
						}
					}
				}
			}
			if(!jarPaths.isEmpty()) {
				baos.write("Bundle-Classpath: ".getBytes());
				baos.write(
						jarPaths.stream()
						.collect(Collectors.joining(",\n "))
						.getBytes()
				);
				baos.write('\n');
			}
			
			
			try(InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
				manifestMf.create(is, true, null);		
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, "Exception while creating MANIFEST.MF", "", e));
		}
		
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
	
	private static void setClasspath(IProject project, IPluginModelBase model, IProgressMonitor monitor)
			throws CoreException {
		IClasspathEntry[] entries = ClasspathComputer.getClasspath(project, model, null,
				true /* clear existing entries */, true);
		JavaCore.create(project).setRawClasspath(entries, null);
		// workaround PDE sloppy model management during the first multimodule project
		// import in eclipse session
		// 1. m2e creates all modules as simple workspace projects without JDT or PDE
		// natures
		// 2. first call to
		// org.eclipse.pde.internal.core.PluginModelManager.initializeTable() reads all
		// workspace
		// projects regardless of their natures (see
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319268)
		// 3. going through all projects one by one
		// 3.1. m2e enables JDE and PDE natures and adds PDE classpath container
		// 3.2. org.eclipse.pde.internal.core.PDEClasspathContainer.addProjectEntry
		// ignores all project's dependencies
		// that do not have JAVA nature. at this point PDE classpath is missing some/all
		// workspace dependencies.
		// 4. PDE does not re-resolve classpath when dependencies get JAVA nature
		// enabled

		// as a workaround, touch project bundle manifests to force PDE re-read the
		// model, re-resolve dependencies
		// and recalculate PDE classpath

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
			metainf = project.getFolder("META-INF");
		} else {
			metainf = metainf.getFolder(new Path("META-INF"));
		}

		return metainf.getFile(new Path("MANIFEST.MF"));
	}
    
    private static List<String> findSourceFolders(IProject project) throws FileNotFoundException, IOException, CoreException, SAXException, ParserConfigurationException {
		IFolder odp = project.getFolder("odp"); // TODO read from config
		if(!odp.exists()) {
			return Arrays.asList("odp/Code/Java");
		}
		IFile classpath = odp.getFile(".classpath");
		if(!classpath.exists()) {
			return Arrays.asList("odp/Code/Java");
		}
		
		XMLDocument domDoc = new XMLDocument();
		try(InputStream is = classpath.getContents()) {
			domDoc.loadInputStream(is);
		}
		XMLNodeList xresult = domDoc.selectNodes("/classpath/classpathentry[kind=src]");
		List<String> paths = xresult.stream()
			.map(el -> el.getAttribute("path"))
			.filter(path -> !"Local".equals(path))
			.collect(Collectors.toList());
		paths.add("Code/Java");
		return paths.stream()
			.map(path -> "odp/" + path)
			.collect(Collectors.toList());
	}
}
