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

import static org.openntf.nsfodp.commons.odp.designfs.DesignFileSystemConstants.*;

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

//import com.ibm.commons.util.StringUtil;
//import com.ibm.designer.domino.napi.NotesConstants;
//
//import lotus.domino.Database;
//import lotus.domino.DateTime;
//import lotus.domino.Document;
//import lotus.domino.EmbeddedObject;
//import lotus.domino.Item;
//import lotus.domino.Name;
//import lotus.domino.NotesException;
//import lotus.domino.RichTextItem;
//import lotus.domino.Session;
//import lotus.domino.View;
//import lotus.domino.ViewEntry;
//import lotus.domino.ViewNavigator;

/**
 * Central class for NSF access methods.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
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
//		if("/".equals(path.toString())) { //$NON-NLS-1$
//			return true;
//		}
//		String cacheId = "exists-" + path; //$NON-NLS-1$
//		return NSFPathUtil.callWithDatabase(path, cacheId, database -> {
//			View view = database.getView(VIEW_FILESBYPATH);
//			try {
//				view.setAutoUpdate(false);
//				view.refresh();
//				ViewEntry entry = view.getEntryByKey(path.toAbsolutePath().toString(), true);
//				try {
//					return entry != null;
//				} finally {
//					if(entry != null) {
//						entry.recycle();
//					}
//				}
//			} finally {
//				if(view != null) {
//					view.recycle();
//				}
//			}
//		});
	  return false;
	}
	
	/**
	 * <p>Takes an Domino-format name and converts it to LDAP format.</p>
	 * 
	 * <p>If the provided value is not a valid Domino name, the original value is returned.</p>
	 */
	public static String dominoNameToLdap(String value) {
		// There's not a convenient class handy for this
		// TODO see if the ODA stuff can be co-opted
//		try {
//			if(StringUtil.isEmpty(value)) {
//				return value;
//			} else if(!value.contains("/")) { //$NON-NLS-1$
//				if(!value.contains("=")) { //$NON-NLS-1$
//					return "cn=" + value; //$NON-NLS-1$
//				} else {
//					// Then it should be an LDAP-type name already
//					return value;
//				}
//			}
//			return NotesThreadFactory.call(session -> {
//				Name name = session.createName(value);
//				try {
//					String dn = name.getCanonical();
//					if(!dn.contains("=")) { //$NON-NLS-1$
//						return dn;
//					}
//					StringBuilder result = new StringBuilder();
//					for(String component : dn.split("/")) { //$NON-NLS-1$
//						if(result.length() > 0) {
//							result.append(',');
//						}
//						int indexEq = component == null ? -1 : component.indexOf('=');
//						if(component != null && indexEq > -1) {
//							result.append(component.substring(0, indexEq).toLowerCase());
//							result.append('=');
//							result.append(component.substring(indexEq+1));
//						} else {
//							result.append(component);
//						}
//					}
//					return result.toString();
//				} finally {
//					name.recycle();
//				}
//			});
//		} catch(Exception e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
	  return null;
	}
	
	/**
	 * <p>Takes an LDAP-format distinguished name and converts it to Domino format.</p>
	 * 
	 * <p>If the provided value is not a valid LDAP name, the original value is returned.</p>
	 */
	public static String ldapNameToDomino(String value) {
//		if(StringUtil.isEmpty(value)) {
//			return ""; //$NON-NLS-1$
//		} else {
//			// Make sure it's actually an LDAP name. We'll assume that an un-escaped slash is indicative of a Domino name
//			int slashIndex = value.indexOf('/');
//			while(slashIndex > -1) {
//				if(slashIndex == 0 || value.charAt(slashIndex-1) != '\\') {
//					// Then it's probably a Domino name
//					return value;
//				}
//				slashIndex = value.indexOf('/', slashIndex+1);
//			}
//			
//			try {
//				LdapName dn = new LdapName(value);
//				StringBuilder result = new StringBuilder();
//				// LdapName lists components in increasing-specificity order
//				for(int i = dn.size()-1; i >= 0; i--) {
//					if(result.length() > 0) {
//						result.append("/"); //$NON-NLS-1$
//					}
//					
//					String component = dn.get(i);
//					// Domino likes the component name capitalized - probably not REQUIRED, but it shouldn't hurt
//					int indexEq = component == null ? -1 : component.indexOf('=');
//					if(component != null && indexEq > -1) {
//						result.append(component.substring(0, indexEq).toUpperCase());
//						result.append('=');
//						result.append(component.substring(indexEq+1));
//					} else {
//						result.append(component);
//					}
//				}
//				return result.toString();
//			} catch(InvalidNameException e) {
//				throw new RuntimeException(e);
//			}
//		}
	  return null;
	}
	
	public static DesignFileAttributes readAttributes(DesignPath path) {
//		String cacheId = "attrs-" + path; //$NON-NLS-1$
//		return NSFPathUtil.callWithDocument(path, cacheId, doc -> {
//			NotesPrincipal owner;
//			NotesPrincipal group;
//			Type type;
//			FileTime lastModified;
//			FileTime lastAccessed;
//			FileTime created;
//			long size;
//			Set<PosixFilePermission> permissions;
//			
//			if(!doc.isNewNote()) {
//				owner = new NotesPrincipal(doc.getItemValueString(ITEM_OWNER));
//				group = new NotesPrincipal(doc.getItemValueString(ITEM_GROUP));
//				
//				String form = doc.getItemValueString(NotesConstants.FIELD_FORM);
//				if(StringUtil.isNotEmpty(form)) {
//					type = Type.valueOf(form);
//				} else {
//					type = null;
//				}
//				if(doc.hasItem(ITEM_MODIFIED)) {
//					@SuppressWarnings("unchecked")
//					Vector<DateTime> mod = (Vector<DateTime>)doc.getItemValueDateTimeArray(ITEM_MODIFIED);
//					try {
//						lastModified = FileTime.fromMillis(mod.get(0).toJavaDate().getTime());
//					} finally {
//						doc.recycle(mod);;
//					}
//				} else {
//					lastModified = FileTime.from(Instant.now());
//				}
//				DateTime acc = doc.getLastAccessed();
//				if(acc != null) {
//					try {
//						lastAccessed = FileTime.fromMillis(acc.toJavaDate().getTime());
//					} finally {
//						acc.recycle();
//					}
//				} else {
//					lastAccessed = FileTime.from(Instant.now());
//				}
//				if(doc.hasItem(ITEM_CREATED)) {
//					@SuppressWarnings("unchecked")
//					Vector<DateTime> c = (Vector<DateTime>)doc.getItemValueDateTimeArray(ITEM_CREATED);
//					try {
//						created = FileTime.fromMillis(c.get(0).toJavaDate().getTime());
//					} finally {
//						doc.recycle(c);
//					}
//				} else {
//					created = FileTime.from(Instant.now());
//				}
//				
//				if(doc.hasItem(ITEM_FILE)) {
//					RichTextItem item = (RichTextItem)doc.getFirstItem(ITEM_FILE);
//					try {
//						@SuppressWarnings("unchecked")
//						Vector<EmbeddedObject> eos = item.getEmbeddedObjects();
//						try {
//							if(!eos.isEmpty()) {
//								size = eos.get(0).getFileSize();
//							} else {
//								size = 0;
//							}
//						} finally {
//							item.recycle(eos);
//						}
//					} finally {
//						item.recycle();
//					}
//				} else {
//					size = 0;
//				}
//				
//				permissions = PosixFilePermissions.fromString(doc.getItemValueString(ITEM_PERMISSIONS));
//			} else {
//				owner = new NotesPrincipal("CN=root"); //$NON-NLS-1$
//				group = new NotesPrincipal("CN=wheel"); //$NON-NLS-1$
//				type = Type.File;
//				lastModified = FileTime.from(Instant.EPOCH);
//				lastAccessed = FileTime.from(Instant.EPOCH);
//				created = FileTime.from(Instant.EPOCH);
//				size = 0;
//				permissions = EnumSet.allOf(PosixFilePermission.class);
//			}
//			
//			return new NSFFileAttributes(owner, group, type, lastModified, lastAccessed, created, size, permissions);
//		});
	  return null;
	}
	
	/**
	 * Sets the owner of the provided path to the provided name.
	 * 
	 * @param path the path of the file or folder to set
	 * @param owner the new owner name
	 * @throws IOException if there is a database problem setting the owner
	 */
	public static void setOwner(DesignPath path, UserPrincipal owner) throws IOException {
//		try {
//			NSFPathUtil.runWithDocument(path, doc -> {
//				doc.replaceItemValue(ITEM_OWNER, owner.getName());
//				doc.save();
//				NSFPathUtil.invalidateDatabaseCache(doc.getParentDatabase());
//			});
//		} catch(RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}
	
	/**
	 * Sets the group of the provided path to the provided name.
	 * 
	 * @param path the path of the file or folder to set
	 * @param group the new group name
	 * @throws IOException if there is a database problem setting the group
	 */
	public static void setGroup(DesignPath path, UserPrincipal group) throws IOException {
//		try {
//			NSFPathUtil.runWithDocument(path, doc -> {
//				doc.replaceItemValue(ITEM_GROUP, group.getName());
//				doc.save();
//				NSFPathUtil.invalidateDatabaseCache(doc.getParentDatabase());
//			});
//		} catch(RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}
	
	/**
	 * Sets the permissions of the provided path to the provided name.
	 * 
	 * @param path the path of the file or folder to set
	 * @param perms the new permissions
	 * @throws IOException if there is a database problem setting the permissions
	 */
	public static void setPermissions(DesignPath path, Set<PosixFilePermission> perms) throws IOException {
//		try {
//			NSFPathUtil.runWithDocument(path, doc -> {
//				doc.replaceItemValue(ITEM_PERMISSIONS, PosixFilePermissions.toString(perms));
//				doc.save();
//				NSFPathUtil.invalidateDatabaseCache(doc.getParentDatabase());
//			});
//		} catch(RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}
	
	/**
	 * Sets the modified and/or creation time of the provided path.
	 * 
	 * @param path the path of the file to set
	 * @param lastModifiedTime the last modified time, if desired
	 * @param createTime the creation time, if desired
	 * @throws IOException if there is a database problem setting the metadata
	 */
	public static void setTimes(DesignPath path, FileTime lastModifiedTime, FileTime createTime) throws IOException {
//		try {
//			NSFPathUtil.runWithDocument(path, doc -> {
//				Session session = doc.getParentDatabase().getParent();
//				if(lastModifiedTime != null) {
//					DateTime mod = session.createDateTime(new Date(lastModifiedTime.toMillis()));
//					try {
//						doc.replaceItemValue(ITEM_MODIFIED, mod);
//					} finally {
//						mod.recycle();
//					}
//				}
//				if(createTime != null) {
//					DateTime created = session.createDateTime(new Date(createTime.toMillis()));
//					try {
//						doc.replaceItemValue(ITEM_CREATED, created);
//					} finally {
//						created.recycle();
//					}
//				}
//				
//				doc.save();
//				NSFPathUtil.invalidateDatabaseCache(doc.getParentDatabase());
//			});
//		} catch(RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}
	
	/**
	 * Lists the names of any user-defined attributes on the provided path
	 * 
	 * @param path the path to check
	 * @return a {@link List} of attribute names
	 * @throws IOException if there is a DB problem reading the names
	 */
	@SuppressWarnings("unchecked")
	public static List<String> listUserDefinedAttributes(DesignPath path) throws IOException {
//		try {
//			String cacheId = "userAttrs-" + path; //$NON-NLS-1$
//			return NSFPathUtil.callWithDocument(path, cacheId, doc ->
//				((List<Item>)doc.getItems()).stream()
//					.map(item -> {
//						try {
//							return item.getName();
//						} catch (NotesException e) {
//							throw new RuntimeException(e);
//						}
//					})
//					.filter(name -> name.startsWith(PREFIX_USERITEM) && name.length() > PREFIX_USERITEM.length())
//					.map(name -> name.substring(PREFIX_USERITEM.length()))
//					.collect(Collectors.toList())
//			);
//		} catch(RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	  return Collections.emptyList();
	}
	
	/**
	 * Writes the provided data to the named user-defined attribute in the path
	 * 
	 * @param path the path of the file to amend
	 * @param name the name of the user-defined attribute
	 * @param src a buffer containing the data
	 * @return the number of bytes written
	 * @throws IOException if there is a DB problem writing the data
	 */
	public static int writeUserDefinedAttribute(DesignPath path, String name, ByteBuffer src) throws IOException {
//		try {
//			return NSFPathUtil.callWithDocument(path, null, doc -> {
//				String itemName = PREFIX_USERITEM + name;
//				byte[] data = src.array();
//				doc.replaceItemValueCustomDataBytes(itemName, DATATYPE_NAME, data);
//				doc.computeWithForm(false, false);
//				NSFPathUtil.invalidateDatabaseCache(doc.getParentDatabase());
//				return data.length;
//			});
//		} catch(RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	  return 0;
	}

	/**
	 * Deletes the named user-defined attribute from the provided path
	 * 
	 * @param path the path of the file to adjust
	 * @param name the name of the user-defined attribute
	 * @throws IOException if there is a DB problem deleting the data
	 */
	public static void deleteUserDefinedAttribute(DesignPath path, String name) throws IOException {
//		try {
//			NSFPathUtil.runWithDocument(path, doc -> {
//				String itemName = PREFIX_USERITEM + name;
//				if(doc.hasItem(itemName)) {
//					doc.removeItem(itemName);
//					doc.computeWithForm(false, false);
//					doc.save();
//					NSFPathUtil.invalidateDatabaseCache(doc.getParentDatabase());
//				}
//			});
//		} catch(RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	}
	
	/**
	 * Reads the provided user-defined attribute from the provided path.
	 * 
	 * @param path the path of the file to read
	 * @param name the name of the attribute to read
	 * @return the attribute data as a byte array
	 * @throws IOException if there is a DB problem reading the data
	 */
	public static byte[] getUserDefinedAttribute(DesignPath path, String name) throws IOException {
//		try {
//			String cacheId = "userAttrVal-" + path + name; //$NON-NLS-1$
//			return NSFPathUtil.callWithDocument(path, cacheId, doc -> {
//				String itemName = PREFIX_USERITEM + name;
//				Item item = doc.getFirstItem(itemName);
//				if(item == null) {
//					return new byte[0];
//				} else {
//					switch(item.getType()) {
//					case Item.TEXT:
//						return item.getValueString().getBytes();
//					case Item.USERDATA:
//						return item.getValueCustomDataBytes(DATATYPE_NAME);
//					default:
//						return new byte[0];
//					}
//				}
//			});
//		} catch(RuntimeException e) {
//			e.printStackTrace();
//			throw new IOException(e);
//		}
	  return null;
	}

//	/**
//	 * Retrieves the document for the provided path, creating a new in-memory document
//	 * if needed.
//	 * 
//	 * @param path the path to find the document for
//	 * @param database the database housing the document
//	 * @return a document representing the note
//	 * @throws NotesException 
//	 */
//	public static Document getDocument(NSFPath path, Database database) throws NotesException {
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
//	}
}
