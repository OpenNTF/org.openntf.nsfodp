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
package org.openntf.nsfodp.deployment;

import com.ibm.domino.osgi.core.context.ContextInfo;
import java.nio.file.Path;
import java.util.Objects;

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
		this.nsfFile = Objects.requireNonNull(nsfFile, "nsfFile cannot be null");
		this.destPath = Objects.requireNonNull(destPath, "destPath cannot be null");
		this.replaceDesign = replaceDesign;
	}

	@Override
	public void run() {
		try {
			Session session = ContextInfo.getUserSession();
			Database source = session.getDatabase("", nsfFile.toAbsolutePath().toString());
			
			String server, filePath;
			int bangIndex = destPath.indexOf("!!");
			if(bangIndex > -1) {
				server = destPath.substring(0, bangIndex);
				filePath = destPath.substring(bangIndex+2);
			} else {
				server = "";
				filePath = destPath;
			}
			Database dest = session.getDatabase(server, filePath, true);
			if(dest.isOpen() && !replaceDesign) {
				throw new IllegalStateException("Destination database exists but replaceDesign is false: " + destPath);
			}
			
			// TODO handle replace design
			// TODO sign design
			dest = source.createFromTemplate(server, filePath, false);
		} catch(NotesException ne) {
			throw new RuntimeException("Encountered NotesException while deploying NSF", ne);
		}
	}

}