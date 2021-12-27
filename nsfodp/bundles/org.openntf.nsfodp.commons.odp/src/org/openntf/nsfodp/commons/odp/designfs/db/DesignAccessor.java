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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.openntf.nsfodp.commons.odp.designfs.DesignPath;
import org.openntf.nsfodp.commons.odp.designfs.attribute.DesignFileAttributes;
import org.openntf.nsfodp.commons.odp.designfs.attribute.DesignFileAttributes.Type;
import org.openntf.nsfodp.commons.odp.designfs.util.DesignPathUtil;
import org.openntf.nsfodp.commons.odp.designfs.util.NotesThreadFactory;
import org.openntf.nsfodp.commons.odp.designfs.util.StringUtil;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NNote;

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
//		String cacheId = "entries-" + dir; //$NON-NLS-1$
//		return NSFPathUtil.callWithDatabase(dir, cacheId, database -> {
//			View filesByParent = database.getView(VIEW_FILESBYPARENT);
//			try {
//				filesByParent.setAutoUpdate(false);
//				filesByParent.refresh();
//				
//				String category = dir.toAbsolutePath().toString();
//				ViewNavigator nav = filesByParent.createViewNavFromCategory(category);
//				try {
//					nav.setBufferMaxEntries(400);
//					List<String> result = new ArrayList<>(nav.getCount());
//					ViewEntry entry = nav.getFirst();
//					while(entry != null) {
//						entry.setPreferJavaDates(true);
//						String name = String.valueOf(entry.getColumnValues().get(VIEW_FILESBYPARENT_INDEX_NAME));
//						result.add(name);
//						
//						ViewEntry tempEntry = entry;
//						entry = nav.getNext();
//						tempEntry.recycle();
//					}
//					
//					return result;
//				} finally {
//					nav.recycle();
//				}
//			} finally {
//				if(filesByParent != null) {
//					filesByParent.recycle();
//				}
//			}
//		});
	  return Collections.emptyList();
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
		}
		String cacheId = "exists-" + path; //$NON-NLS-1$
		return DesignPathUtil.callWithDatabase(path, cacheId, database -> {
			return getDocument(path, database) != null;
		});
	}
	
	public static DesignFileAttributes readAttributes(DesignPath path) {
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
//		View view = database.getView(VIEW_FILESBYPATH);
//		try {
//			view.setAutoUpdate(false);
//			view.refresh();
//			Document doc = view.getDocumentByKey(path.toAbsolutePath().toString(), true);
//			if(doc == null) {
//				doc = database.createDocument();
//				doc.replaceItemValue(ITEM_PARENT, path.getParent().toAbsolutePath().toString());
//				doc.replaceItemValue(NotesConstants.ITEM_META_TITLE, path.getFileName().toString());
//			}
//			return doc;
//		} finally {
//			if(view != null) {
//				view.recycle();
//			}
//		}
		return null;
	}
}
