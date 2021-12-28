/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.nsfodp.commons.odp.designfs.db;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openntf.nsfodp.commons.NoteType;
import org.openntf.nsfodp.commons.odp.designfs.DesignPath;
import org.openntf.nsfodp.commons.odp.designfs.FSDirectory;
import org.openntf.nsfodp.commons.odp.designfs.attribute.DesignFileAttributes;
import org.openntf.nsfodp.commons.odp.designfs.attribute.DesignFileAttributes.Type;
import org.openntf.nsfodp.commons.odp.designfs.attribute.DirectoryFileAttributes;
import org.openntf.nsfodp.commons.odp.designfs.util.DesignPathUtil;
import org.openntf.nsfodp.commons.odp.designfs.util.PathUtil;
import org.openntf.nsfodp.commons.odp.designfs.util.StringUtil;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NNote;
import org.openntf.nsfodp.commons.odp.notesapi.NViewEntry;

/**
 * Central class for NSF access methods.
 * 
 * @author Jesse Gallagher
 * @since 4.0.0
 */
public enum DesignAccessor {
	;
	
	/**
	 * Returns a list of file names for files within the provided directory.
	 * 
	 * @param dir the directory to list
	 * @return a {@link List} of individual file names, in alphabetical order
	 */
	public static List<String> getDirectoryEntries(DesignPath dir) {
		String path = PathUtil.toPathString(dir);
		
		if("/".equals(path)) { //$NON-NLS-1$
			// Root directory: just the design
			return Collections.singletonList(FSDirectory.design.toString());
		}
		
		List<String> result = new ArrayList<>();

		String cacheId = "entries-" + dir; //$NON-NLS-1$
		DesignPathUtil.callWithDatabase(dir, cacheId, database -> {
			FSDirectory fsdir = FSDirectory.forPath(path);
			if(fsdir != null) {
				// Add any static design folders
				fsdir.getChildren()
					.map(String::valueOf)
					.forEach(result::add);
				
				// If the entry has a note pattern, add such notes
				String pattern = fsdir.getPattern();
				if(StringUtil.isNotEmpty(pattern)) {
					database.eachDesignEntry(fsdir.getNoteClass(), pattern, entry -> {
						// TODO handle the case of multiple design notes with the same title
						result.add(encodeDesignTitle(entry));
					});
				}
			}

			// TODO add named files
			
			return null;
		});
		
		return result;
	}
	
	/**
	 * Extracts the attachment from the provided NSF path.
	 * 
	 * <p>The extracted file will have the same name as the {code path}'s file name, and
	 * will be housed within a temporary directory. Both the returned file and its parent
	 * should be deleted when no longer needed.</p>
	 * 
	 * @param path the path of the file to extract
	 * @return a {@link Path} to a temporary file holding the attachment contents
	 */
	public static Path extractAttachment(DesignPath path) {
//		return NSFPathUtil.callWithDocument(path, null, doc -> {
//			Path resultParent = Files.createTempDirectory(path.getFileName().toString());
//			Path result = resultParent.resolve(path.getFileName().toString());
//			if(doc.hasItem(ITEM_FILE)) {
//				// TODO add sanity checks
//				RichTextItem rtitem = (RichTextItem)doc.getFirstItem(ITEM_FILE);
//				try {
//					EmbeddedObject eo = (EmbeddedObject) rtitem.getEmbeddedObjects().get(0);
//					try(InputStream is = eo.getInputStream()) {
//						Files.copy(is, result, StandardCopyOption.REPLACE_EXISTING);
//					} finally {
//						eo.recycle();
//					}
//				} finally {
//					rtitem.recycle();
//				}
//			} else {
//				Files.createFile(result);
//			}
//			
//			return result;
//		});
	  return null;
	}
	
	/**
	 * Stores the provided attachment data in the named path.
	 * 
	 * @param path the path to the file inside the NSF
	 * @param attachmentData the path to the attachment data stored on disk
	 * @throws IOException if there is a problem attaching the data
	 */
	public static void storeAttachment(DesignPath path, Path attachmentData) throws IOException {
//		try {
//			NSFPathUtil.runWithDocument(path, doc -> {
//				if(doc.isNewNote()) {
//					doc.replaceItemValue(NotesConstants.FIELD_FORM, ITEM_FILE);
//				}
//				if(doc.hasItem(ITEM_FILE)) {
//					doc.removeItem(ITEM_FILE);
//				}
//				RichTextItem item = doc.createRichTextItem(ITEM_FILE);
//				try {
//					item.embedObject(EmbeddedObject.EMBED_ATTACHMENT, "", attachmentData.toAbsolutePath().toString(), null); //$NON-NLS-1$
//				} finally {
//					item.recycle();
//				}
//				doc.computeWithForm(false, false);
//				doc.save();
//				NSFPathUtil.invalidateDatabaseCache(doc.getParentDatabase());
//			});
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}
	
	/**
	 * Creates a directory entry for the provided path, if it doesn't currently exist.
	 * 
	 * @param dir the path of the desired directory
	 * @param attrs the attributes of the directory to create
	 * @throws IOException if there is a problem creating the directory document
	 */
	public static void createDirectory(DesignPath dir, FileAttribute<?>... attrs) throws IOException {
		// TODO support attrs
//		try {
//			NSFPathUtil.runWithDocument(dir, doc -> {
//				if(doc.isNewNote()) {
//					doc.replaceItemValue(NotesConstants.FIELD_FORM, FORM_FOLDER);
//					doc.computeWithForm(false, false);
//					doc.save();
//					NSFPathUtil.invalidateDatabaseCache(doc.getParentDatabase());
//				}
//			});
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}

	/**
	 * Deletes the file or folder at the provided path, if it exists.
	 * 
	 * @param path the path of the file to delete
	 * @throws IOException if there is a problem deleting the file
	 */
	public static void delete(DesignPath path) throws IOException {
		// TODO throw exception if it is a non-empty directory
//		try {
//			NSFPathUtil.runWithDocument((NSFPath)path, doc -> {
//				if(!doc.isNewNote()) {
//					if(doc.getParentDatabase().isDocumentLockingEnabled()) {
//						doc.lock();
//					}
//					Database db = doc.getParentDatabase();
//					doc.remove(false);
//					NSFPathUtil.invalidateDatabaseCache(db);
//				}
//			});
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}
	
	/**
	 * Copies the provided source note to the target, deleting the target if it exists.
	 * 
	 * @param source the source path to copy
	 * @param target the target path
	 * @param options Java NIO copy options
	 * @throws IOException if there is a database problem copying the file
	 */
	public static void copy(DesignPath source, DesignPath target, CopyOption... options) throws IOException {
		// TODO respect options
//		try {
//			NSFPathUtil.runWithDatabase(source, database -> {
//				Document targetDoc = NSFAccessor.getDocument(target, database);
//				if(!targetDoc.isNewNote()) {
//					if(targetDoc.getParentDatabase().isDocumentLockingEnabled()) {
//						targetDoc.lock();
//					}
//					targetDoc.remove(false);
//					targetDoc.recycle();
//				}
//				
//				Document doc = NSFAccessor.getDocument(source, database);
//				try {
//					targetDoc = doc.copyToDatabase(database);
//					try {
//						targetDoc.replaceItemValue(ITEM_PARENT, target.getParent().toAbsolutePath().toString());
//						targetDoc.replaceItemValue(NotesConstants.ITEM_META_TITLE, target.getFileName().toString());
//						targetDoc.computeWithForm(false, false);
//						targetDoc.save();
//					} finally {
//						targetDoc.recycle();
//					}
//				} finally {
//					doc.recycle();
//				}
//				NSFPathUtil.invalidateDatabaseCache(database);
//			});
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}

	/**
	 * Moves the provided source note to the target, deleting the target if it exists.
	 * 
	 * @param source the source path to move
	 * @param target the target path
	 * @param options Java NIO copy options
	 * @throws IOException if there is a database problem moving the file
	 */
	public static void move(DesignPath source, DesignPath target, CopyOption... options) throws IOException {
//		try {
//			NSFPathUtil.runWithDatabase(source, database -> {
//				Document targetDoc = NSFAccessor.getDocument(target, database);
//				if(!targetDoc.isNewNote()) {
//					if(targetDoc.getParentDatabase().isDocumentLockingEnabled()) {
//						targetDoc.lock();
//					}
//					targetDoc.remove(false);
//					targetDoc.recycle();
//				}
//				
//				Document doc = NSFAccessor.getDocument((NSFPath)source, database);
//				try {
//					doc.replaceItemValue(ITEM_PARENT, target.getParent().toAbsolutePath().toString());
//					doc.replaceItemValue(NotesConstants.ITEM_META_TITLE, target.getFileName().toString());
//					doc.computeWithForm(false, false);
//					doc.save();
//				} finally {
//					doc.recycle();
//				}
//				NSFPathUtil.invalidateDatabaseCache(database);
//			});
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}
	
	/**
	 * Checks if the provided path exists in the database.
	 * 
	 * @param path the path of the file or folder to check
	 * @return whether the file currently exists in the database
	 */
	public static boolean exists(DesignPath path) {
		if("/".equals(path.toString())) { //$NON-NLS-1$
			return true;
		} else if(FSDirectory.forPath(path) != null) {
			return true;
		}
		String cacheId = "exists-" + path; //$NON-NLS-1$
		return DesignPathUtil.callWithDatabase(path, cacheId, database -> {
			return getDocument(path, database) != null;
		});
	}
	
	public static BasicFileAttributes readAttributes(DesignPath path) {
		String pathString = PathUtil.toPathString(path);
		FSDirectory fsdir = FSDirectory.forPath(pathString);
		if(fsdir != null) {
			return DirectoryFileAttributes.instance;
		}
		
		String cacheId = "attrs-" + path; //$NON-NLS-1$
		return DesignPathUtil.callWithDocument(path, cacheId, doc -> {
			Type type = Type.File;
			FileTime lastModified = FileTime.from(doc.getLastModified());
			FileTime lastAccessed = FileTime.from(Instant.now());
			FileTime created = FileTime.from(doc.getCreated());
			long size = 0;

			return new DesignFileAttributes(type, lastModified, lastAccessed, created, size);
		});
	}

	/**
	 * Retrieves the document for the provided path, creating a new in-memory document
	 * if needed.
	 * 
	 * @param path the path to find the document for
	 * @param database the database housing the document
	 * @return a document representing the note
	 */
	public static NNote getDocument(DesignPath path, NDatabase database) {
		// TODO handle the case of full-named files
		
		FSDirectory fsdir = FSDirectory.forPath(path.getParent());
		String pattern = fsdir.getPattern();
		if(StringUtil.isNotEmpty(pattern)) {
			String title = decodeDesignTitle(fsdir, path.getFileName().toString());
			
			return database.findDesignNote(fsdir.getNoteClass(), pattern, title);
		}
		return null;
	}
	
	/**
	 * Encodes the provided note title for use inside a filesystem path.
	 * 
	 * @param title a $TITLE value to encode
	 * @return a filesystem-safe encoded value
	 */
	public static String encodeDesignTitle(NViewEntry entry) {
		// TODO actually encode this
		String title = DesignPathUtil.extractTitleValue((String)entry.getColumnValues()[0]);
		NoteType noteType = DesignPathUtil.noteTypeForEntry(entry);
		String ext = noteType.getExtension();
		if(ext != null && !title.endsWith('.' + ext)) {
			title += '.' + ext;
		}
		
		return title;
	}
	
	/**
	 * Decodes a note title value that had previously been encoded with
	 * {@link #encodeDesignTitle(String)}.
	 * 
	 * @parent parent the parent {@link FSDirectory}, if known
	 * @param title an encoded title value
	 * @return the original $TITLE value
	 */
	public static String decodeDesignTitle(FSDirectory parent, String title) {
		// TODO actually decode this
		if(parent != null) {
			for(NoteType noteType : parent.getNoteTypes()) {
				String ext = noteType.getExtension();
				// TODO don't chop off .xsp, etc.
				if(ext != null && title.endsWith('.' + ext)) {
					title = title.substring(0, title.length()-ext.length()-1);
				}
			}
		}
		return title;
	}
}
