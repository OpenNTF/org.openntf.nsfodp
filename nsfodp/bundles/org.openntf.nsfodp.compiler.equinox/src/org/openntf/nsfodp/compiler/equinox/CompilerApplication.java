/**
 * Copyright © 2018-2020 Jesse Gallagher
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.PrintStreamProgressMonitor;
import org.openntf.nsfodp.commons.odp.OnDiskProject;
import org.openntf.nsfodp.compiler.ODPCompiler;
import org.openntf.nsfodp.compiler.ODPCompilerActivator;
import org.openntf.nsfodp.compiler.update.FilesystemUpdateSite;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import lotus.domino.NotesThread;

public class CompilerApplication implements IApplication {
	private static final ExecutorService exec = Executors.newSingleThreadExecutor(NotesThread::new);
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		NotesThread.sinitThread();
		
		
		Path odpDirectory = toPath(System.getProperty(NSFODPConstants.PROP_ODPDIRECTORY));
		List<Path> updateSites = toPaths(System.getProperty(NSFODPConstants.PROP_UPDATESITE));
		Path outputFile = toPath(System.getProperty(NSFODPConstants.PROP_OUTPUTFILE));
		
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
		
		if(updateSites != null && !updateSites.isEmpty()) {
			updateSites.stream()
				.map(Path::toFile)
				.map(FilesystemUpdateSite::new)
				.forEach(compiler::addUpdateSite);
		}
		
		exec.submit(() -> {
			try {
				Path nsf = compiler.compile();
				Files.move(nsf, outputFile, StandardCopyOption.REPLACE_EXISTING);
				mon.done();
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}).get();
		System.out.println(getClass().getName() + "#end");
		
		return EXIT_OK;
	}
	
	@Override
	public void stop() {
		// NOP
	}
	
	private Path toPath(String pathString) {
		if(pathString == null || pathString.isEmpty()) {
			return null;
		} else {
			return Paths.get(pathString);
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<Path> toPaths(String pathString) {
		if(pathString == null || pathString.isEmpty()) {
			return Collections.emptyList();
		} else {
			if(pathString.startsWith("[")) { // Treat it as JSON //$NON-NLS-1$
				try {
					List<String> paths = (List<String>)JsonParser.fromJson(JsonJavaFactory.instance, pathString);
					return paths.stream()
						.map(this::toPath)
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
				} catch (JsonException e) {
					throw new RuntimeException("Error parsing paths JSON array: " + pathString, e);
				}
			} else {
				return Collections.singletonList(toPath(pathString));
			}
		}
	}
}
