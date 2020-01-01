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
package org.openntf.nsfodp.deployment;

import com.ibm.domino.osgi.core.context.ContextInfo;
import java.nio.file.Path;
import java.util.Objects;

import org.eclipse.core.runtime.NullProgressMonitor;

import lotus.domino.AdministrationProcess;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Handles the deployment of an NSF to a designated path on the server, optionally
 * replacing the design of an existing database.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class DeployNSFTask implements Runnable {

	private final Path nsfFile;
	private final String destPath;
	private final boolean replaceDesign;

	/**
	 * @param nsfFile the path to the NSF/NTF file on disk
	 * @param destPath the destination path of the database in Notes API format (e.g. "server!!file.nsf")
	 * @param replaceDesign whether the deployment should replace the design of an existing database if present
	 */
	public DeployNSFTask(Path nsfFile, String destPath, boolean replaceDesign) {
		this.nsfFile = Objects.requireNonNull(nsfFile, Messages.DeployNSFTask_nsfFileNull);
		this.destPath = Objects.requireNonNull(destPath, Messages.DeployNSFTask_destPathNull);
		this.replaceDesign = replaceDesign;
	}

	@Override
	public void run() {
		try {
			Session session = ContextInfo.getUserSession();
			
			String server, filePath;
			int bangIndex = destPath.indexOf("!!"); //$NON-NLS-1$
			if(bangIndex > -1) {
				server = destPath.substring(0, bangIndex);
				filePath = destPath.substring(bangIndex+2);
			} else {
				server = ""; //$NON-NLS-1$
				filePath = destPath;
			}
			Database dest = session.getDatabase(server, filePath, true);
			if(dest.isOpen() && !replaceDesign) {
				throw new IllegalStateException(Messages.DeployNSFTask_dbExists + destPath);
			}
			
			if(dest.isOpen()) {
				// Then do a replace design
				ReplaceDesignTaskLocal task = new ReplaceDesignTaskLocal(filePath, nsfFile, new NullProgressMonitor());
				task.run();
			} else {
				Database source = session.getDatabase("", nsfFile.toAbsolutePath().toString()); //$NON-NLS-1$
				dest = source.createFromTemplate(server, filePath, false);
			}
			AdministrationProcess adminp = session.createAdministrationProcess(server);
			adminp.signDatabaseWithServerID(server, filePath);
			session.sendConsoleCommand(server, "tell adminp p im"); //$NON-NLS-1$
			
			
		} catch(NotesException ne) {
			throw new RuntimeException(Messages.DeployNSFTask_exceptionDeploying, ne);
		}
	}

}
