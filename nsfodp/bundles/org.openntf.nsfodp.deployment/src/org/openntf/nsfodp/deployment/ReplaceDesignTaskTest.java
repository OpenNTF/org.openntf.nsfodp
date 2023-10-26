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
package org.openntf.nsfodp.deployment;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ibm.commons.util.StringUtil;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

/**
 * Replaces a target database's design from a source template.
 * 
 * @author Jesse Gallagher
 * @since 1.0
 * @see <a href="http://searchdomino.techtarget.com/tip/Programmatically-replace-the-design-of-Lotus-Notes-databases">http://searchdomino.techtarget.com/tip/Programmatically-replace-the-design-of-Lotus-Notes-databases</a>
 */
public class ReplaceDesignTaskTest extends Job {
	private final String sourcePath;
	private final String targetPath;
	
	public ReplaceDesignTaskTest(String sourcePath, String targetPath) {
		super(Messages.ReplaceDesignTaskTest_label);
		
		if(StringUtil.isEmpty(sourcePath)) {
			throw new IllegalArgumentException("sourcePath cannot be empty"); //$NON-NLS-1$
		}
		if(StringUtil.isEmpty(targetPath)) {
			throw new IllegalArgumentException("targetPath cannot be empty"); //$NON-NLS-1$
		}
		
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		try {
			Session session = NotesFactory.createSession();
			try {
				Database sourceDb = getDatabase(session, sourcePath);
				Database targetDb = getDatabase(session, targetPath);
				
				// Delete elements from the target database
				// Ignore elements having "prohibit design..." property or Master Template defined
				NoteCollection targetNoteColl = targetDb.createNoteCollection(true);
				
				targetNoteColl.selectAllAdminNotes(false);
				targetNoteColl.selectAllDataNotes(false);
				targetNoteColl.setSelectIcon(false);
				targetNoteColl.setSelectHelpAbout(false);
				targetNoteColl.setSelectHelpIndex(false);
				targetNoteColl.setSelectHelpUsing(false);
				
				// Select notes without "prohibit design" or master templates
				targetNoteColl.setSelectionFormula(" !(@IsAvailable($Class) | @Contains($Flags; 'P')) "); //$NON-NLS-1$
				
				// Remove selected notes
				targetNoteColl.buildCollection();
				String noteId = targetNoteColl.getFirstNoteID();
				while(StringUtil.isNotEmpty(noteId)) {
					Document note = targetDb.getDocumentByID(noteId);
	//				System.out.println(Messages.getString("ReplaceDesignTaskTest.consoleRemoving", noteId, note.getItemValueString("$TITLE"))); //$NON-NLS-1$ //$NON-NLS-2$
					note.remove(true);
					
					noteId = targetNoteColl.getNextNoteID(noteId);
				}
				
				// Check for Help About and Help Using
				boolean isHelpAbout = false, isHelpUsing = false;
				
				targetNoteColl.selectAllNotes(false);
				targetNoteColl.clearCollection();
				targetNoteColl.setSelectHelpAbout(true);
				targetNoteColl.buildCollection();
				if(targetNoteColl.getCount() > 0) {
					noteId = targetNoteColl.getFirstNoteID();
					Document helpAbout = targetDb.getDocumentByID(noteId);
					if(helpAbout.getItemValueString("$Flags").contains("R")) { //$NON-NLS-1$ //$NON-NLS-2$
	//					System.out.println(Messages.getString("ReplaceDesignTaskTest.consoleRemovingHelpAbout")); //$NON-NLS-1$
						helpAbout.remove(true);
						isHelpAbout = true;
					}
				}
	
				targetNoteColl.selectAllNotes(false);
				targetNoteColl.clearCollection();
				targetNoteColl.setSelectHelpUsing(true);
				targetNoteColl.buildCollection();
				if(targetNoteColl.getCount() > 0) {
					noteId = targetNoteColl.getFirstNoteID();
					Document helpUsing = targetDb.getDocumentByID(noteId);
					if(helpUsing.getItemValueString("$Flags").contains("R")) { //$NON-NLS-1$ //$NON-NLS-2$
	//					System.out.println(Messages.getString("ReplaceDesignTaskTest.consoleRemovingHelpUsing")); //$NON-NLS-1$
						helpUsing.remove(true);
						isHelpUsing = true;
					}
				}
				
				// Set "More Fields" option
				targetDb.setOption(Database.DBOPT_MOREFIELDS, true);
				
				// Copy all design elements from source DB to target DB except Help About and Help Using
				NoteCollection sourceNoteColl = sourceDb.createNoteCollection(true);
				sourceNoteColl.selectAllAdminNotes(false);
				sourceNoteColl.selectAllDataNotes(false);
				sourceNoteColl.setSelectIcon(false);
				sourceNoteColl.setSelectHelpAbout(isHelpAbout);
				sourceNoteColl.setSelectHelpUsing(isHelpUsing);
			
				return Status.OK_STATUS;
			} finally {
				session.recycle();
			}
		} catch(Exception e) {
			return new Status(IStatus.ERROR, Messages.ReplaceDesignTaskTest_errorReplacingDesign, e.toString(), e);
		}
	}

	// ******************************************************************************
	// * Internal utility methods
	// ******************************************************************************
	
	private static Database getDatabase(Session session, String path) throws NotesException {
		int bangIndex = path.indexOf("!!"); //$NON-NLS-1$
		String server, filePath;
		if(bangIndex > -1) {
			server = path.substring(0, bangIndex);
			filePath = path.substring(bangIndex+2);
		} else {
			server = ""; //$NON-NLS-1$
			filePath = path;
		}
		return session.getDatabase(server, filePath);
	}
}