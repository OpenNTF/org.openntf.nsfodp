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
package org.openntf.nsfodp.compiler.equinox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.PrintStreamProgressMonitor;
import org.openntf.nsfodp.commons.odp.OnDiskProject;
import org.openntf.nsfodp.compiler.ODPCompiler;
import org.openntf.nsfodp.compiler.ODPCompilerActivator;
import org.openntf.nsfodp.compiler.update.FilesystemUpdateSite;
import org.openntf.nsfodp.compiler.update.UpdateSite;

import com.ibm.commons.util.StringUtil;

import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

public class CompilerApplication implements IApplication {
	@Override
	public Object start(IApplicationContext context) throws Exception {
		NotesThread.sinitThread();
		
		
		Path odpDirectory = toPath(System.getProperty(NSFODPConstants.PROP_ODPDIRECTORY));
		Path updateSite = toPath(System.getProperty(NSFODPConstants.PROP_UPDATESITE));
		Path outputFile = toPath(System.getProperty(NSFODPConstants.PROP_OUTPUTFILE));
		
		Session session = NotesFactory.createSession();
		try {
			IProgressMonitor mon = new PrintStreamProgressMonitor(System.out);
			OnDiskProject odp = new OnDiskProject(odpDirectory);
			ODPCompiler compiler = new ODPCompiler(ODPCompilerActivator.instance.getBundle().getBundleContext(), odp, mon);
			
			// See if the client requested a specific compiler level
			String compilerLevel = System.getProperty(NSFODPConstants.PROP_COMPILERLEVEL);
			if(StringUtil.isNotEmpty(compilerLevel)) {
				compiler.setCompilerLevel(compilerLevel);
			}
			String appendTimestamp = System.getProperty(NSFODPConstants.PROP_APPENDTIMESTAMPTOTITLE);
			if("true".equals(appendTimestamp)) { //$NON-NLS-1$
				compiler.setAppendTimestampToTitle(true);
			}
			String templateName = System.getProperty(NSFODPConstants.PROP_TEMPLATENAME);
			if(StringUtil.isNotEmpty(templateName)) {
				compiler.setTemplateName(templateName);
				String templateVersion = System.getProperty(NSFODPConstants.PROP_TEMPLATEVERSION);
				if(StringUtil.isNotEmpty(templateVersion)) {
					compiler.setTemplateVersion(templateVersion);
				}
			}
			String setXspOptions = System.getProperty(NSFODPConstants.PROP_SETPRODUCTIONXSPOPTIONS);
			if("true".equals(setXspOptions)) { //$NON-NLS-1$
				compiler.setSetProductionXspOptions(true);
			}
			
			if(updateSite != null) {
				UpdateSite updateSiteObj = new FilesystemUpdateSite(updateSite.toFile());
				compiler.addUpdateSite(updateSiteObj);
			}
			
			Path[] nsf = new Path[1];
			NotesThread notes = new NotesThread(() -> {
				try {
					nsf[0] = compiler.compile();
					mon.done();
				} catch(RuntimeException e) {
					throw e;
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			});
			notes.run();
			notes.join();
			
			Files.move(nsf[0], outputFile, StandardCopyOption.REPLACE_EXISTING);
		} finally {
			session.recycle();
		}
		
		return EXIT_OK;
	}

	@Override
	public void stop() {
		try {
			NotesThread.stermThread();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Path toPath(String pathString) {
		if(pathString == null || pathString.isEmpty()) {
			return null;
		} else {
			return Paths.get(pathString);
		}
	}
}
