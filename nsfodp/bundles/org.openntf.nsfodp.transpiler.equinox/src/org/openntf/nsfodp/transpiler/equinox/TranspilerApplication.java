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
package org.openntf.nsfodp.transpiler.equinox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.openntf.nsfodp.commons.NSFODPConstants;
import org.openntf.nsfodp.commons.PrintStreamProgressMonitor;
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;
import org.openntf.nsfodp.compiler.update.FilesystemUpdateSite;
import org.openntf.nsfodp.transpiler.TranspilerActivator;
import org.openntf.nsfodp.transpiler.XspTranspiler;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import lotus.domino.NotesThread;

/**
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public class TranspilerApplication implements IApplication {
	private static final ExecutorService exec = Executors.newSingleThreadExecutor(NotesThread::new);
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		String notesIni = System.getenv(NSFODPConstants.PROP_NOTESINI);
		if(notesIni != null && !notesIni.isEmpty()) {
			String execDir = System.getenv("Notes_ExecDirectory"); //$NON-NLS-1$
			NotesAPI.get().NotesInitExtended(execDir, "=" + notesIni); //$NON-NLS-1$
		}
		
		NotesThread.sinitThread();
		try {
			Path xspSourceRoot = toPath(System.getenv(NSFODPConstants.PROP_XSP_SOURCE_ROOT));
			Path ccSourceRoot = toPath(System.getenv(NSFODPConstants.PROP_CC_SOURCE_ROOT));
			List<Path> updateSites = toPaths(System.getenv(NSFODPConstants.PROP_UPDATESITE));
			Path outputDirectory = toPath(System.getenv(NSFODPConstants.PROP_OUTPUTFILE));
			
			IProgressMonitor mon = new PrintStreamProgressMonitor(System.out);
			XspTranspiler transpiler = new XspTranspiler(TranspilerActivator.instance.getBundle().getBundleContext(), xspSourceRoot, ccSourceRoot, mon);
			
			if(updateSites != null && !updateSites.isEmpty()) {
				updateSites.stream()
					.map(FilesystemUpdateSite::new)
					.forEach(transpiler::addUpdateSite);
			}
			
			exec.submit(() -> {
				try {
					Path javaSourceRoot = transpiler.transpile();
					Files.walk(javaSourceRoot, FileVisitOption.FOLLOW_LINKS).forEach(p -> {
						Path relativePath = javaSourceRoot.relativize(p);
						Path dest = outputDirectory.resolve(relativePath);
						try {
							if(Files.isDirectory(p)) {
								Files.createDirectories(dest);
							} else {
								Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING);
							}
						} catch(IOException e) {
							throw new UncheckedIOException(e);
						}
					});
					
					mon.done();
				} catch(RuntimeException e) {
					throw e;
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}).get();
			System.out.println(getClass().getName() + "#end"); //$NON-NLS-1$
			
			exec.shutdownNow();
			exec.awaitTermination(30, TimeUnit.SECONDS);
			
			return EXIT_OK;
		} finally {
			NotesThread.stermThread();
		}
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
