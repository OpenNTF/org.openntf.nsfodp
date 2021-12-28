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
package org.openntf.nsfodp.commons.odp.designfs.util;

import static org.openntf.nsfodp.commons.NSFODPUtil.matchesFlagsPattern;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_ACL;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_ICON;
import static org.openntf.nsfodp.commons.h.NsfNote.NOTE_CLASS_NONPRIV;
import static org.openntf.nsfodp.commons.h.StdNames.ASSIST_TYPE_JAVA;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGEXT_WEBCONTENTFILE;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGEXT_WEBSERVICELIB;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_DATABASESCRIPT;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_HIDEFROMDESIGNLIST;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JARFILE;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JAVA_AGENT;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_JAVA_RESOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_LOTUSSCRIPT_AGENT;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAG_PROPFILE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_COMPAPP;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_COMPDEF;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_DATA_CONNECTION_RESOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_DB2ACCESSVIEW;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_FILE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_FOLDER_DESIGN;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_FRAMESET;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_IMAGE_RESOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_JAVAFILE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_JAVA_WEBSERVICE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_LS_WEBSERVICE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SACTIONS_DESIGN;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SCRIPTLIB_JAVA;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SCRIPTLIB_JS;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SCRIPTLIB_LS;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SCRIPTLIB_SERVER_JS;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SHARED_COLS;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SITEMAP;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_STYLEKIT;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_STYLE_SHEET_RESOURCE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_SUBFORM_DESIGN;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_VIEWMAP_DESIGN;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_WEBPAGE;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_WIDGET;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_XSPCC;
import static org.openntf.nsfodp.commons.h.StdNames.DFLAGPAT_XSPPAGE;
import static org.openntf.nsfodp.commons.h.StdNames.IMAGE_NEW_DBICON_NAME;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.openntf.nsfodp.commons.NoteType;
import org.openntf.nsfodp.commons.h.NsfNote;
import org.openntf.nsfodp.commons.odp.designfs.DesignFileSystem;
import org.openntf.nsfodp.commons.odp.designfs.DesignFileSystemProvider;
import org.openntf.nsfodp.commons.odp.designfs.DesignPath;
import org.openntf.nsfodp.commons.odp.designfs.db.DesignAccessor;
import org.openntf.nsfodp.commons.odp.notesapi.NDatabase;
import org.openntf.nsfodp.commons.odp.notesapi.NNote;
import org.openntf.nsfodp.commons.odp.notesapi.NViewEntry;
import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;

import com.ibm.commons.util.StringUtil;

/**
 * 
 * @author Jesse Gallagher
 * @since 4.0.0
 */
@SuppressWarnings("nls")
public enum DesignPathUtil {
	;
	
	public static final String LOCAL_SERVER = "LOCALSERVER"; //$NON-NLS-1$
	
	private static final Function<String, String> encoder = path -> StringUtil.isEmpty(path) ? "" : //$NON-NLS-1$
		Base64.getUrlEncoder().encodeToString(path.getBytes()).replace('=', '-') + "END"; //$NON-NLS-1$
	private static final Function<String, String> decoder = enc -> StringUtil.isEmpty(enc) ? "" : //$NON-NLS-1$
		new String(Base64.getUrlDecoder().decode(enc.replace('-', '=').substring(0, enc.length()-"END".length()).getBytes()));; //$NON-NLS-1$
	
	/**
	 * Extracts the NSF API path from the provided URI. For example:
	 * 
	 * <ul>
	 *   <li>{@code "nsffile:///foo.nsf/bar} &rarr; {@code "foo.nsf"}</li>
	 *   <li>{@code "nsffile://someserver/foo.nsf/bar} &rarr; {@code "someserver!!foo.nsf"}
	 * </ul> 
	 * 
	 * @param uri the URI from which to extract the NSF path
	 * @return the NSF path in API format
	 * @throws IllegalArgumentException if {@code uri} is {@code null} or does not contain an NSF name
	 */
	public static String extractApiPath(URI uri) {
		Objects.requireNonNull(uri, "uri cannot be null"); //$NON-NLS-1$
		
		String host = decoder.apply(uri.getHost());
		if(LOCAL_SERVER.equals(host)) {
			host = null;
		}
		String pathInfo = uri.getPath().substring(1);
		if(pathInfo == null || pathInfo.isEmpty()) {
			throw new IllegalArgumentException("URI path info cannot be empty");
		}
		
		String nsfPath;
		int nsfIndex = pathInfo.indexOf('/');
		if(nsfIndex < 0) {
			nsfPath = decoder.apply(pathInfo);
		} else {
			nsfPath = decoder.apply(pathInfo.substring(0, nsfIndex));
		}
		if(host == null || host.isEmpty()) {
			return nsfPath;
		} else {
			return host + "!!" + nsfPath; //$NON-NLS-1$
		}
	}
	
	/**
	 * Extracts the in-NSF file path from the provided URI. For example:
	 * 
	 * <ul>
	 *   <li>{@code "nsffile:///foo.nsf/bar} &rarr; {@code "/bar"}</li>
	 *   <li>{@code "nsffile://someserver/foo.nsf/bar/baz} &rarr; {@code "/bar/baz"}
	 * </ul> 
	 * 
	 * @param uri the URI from which to extract the file path
	 * @return the relative file path
	 * @throws IllegalArgumentException if {@code uri} is {@code null} or does not contain an NSF name
	 */
	public static String extractPathInfo(URI uri) {
		Objects.requireNonNull(uri, "uri cannot be null");
		
		String pathInfo = uri.getPath();
		if(pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) { //$NON-NLS-1$
			throw new IllegalArgumentException("URI path info cannot be empty");
		}
		pathInfo = pathInfo.substring(1); // Chop off the initial /
		
		int nsfIndex = pathInfo.indexOf('/');
		if(nsfIndex < 0) {
			return ""; //$NON-NLS-1$
		} else {
			return pathInfo.substring(nsfIndex);
		}
	}
	
	/**
	 * Converts a provided NSF API path to a {@link URI} object referencing the
	 * {@value DesignFileSystemProvider#SCHEME} filesystem.
	 * 
	 * @param apiPath the API path to convert
	 * @return the URI version of the API path
	 * @throws URISyntaxException if there is a problem building the URI
	 * @throws IllegalArgumentException if {@code apiPath} is empty
	 */
	public static URI toFileSystemURI(String userName, String apiPath) throws URISyntaxException {
		if(StringUtil.isEmpty(apiPath)) {
			throw new IllegalArgumentException("apiPath cannot be empty");
		}
		
		int bangIndex = apiPath.indexOf("!!"); //$NON-NLS-1$
		String host;
		String nsfPath;
		if(bangIndex < 0) {
			host = LOCAL_SERVER;
			nsfPath = apiPath;
		} else {
			host = apiPath.substring(0, bangIndex);
			nsfPath = apiPath.substring(bangIndex+2);
		}
		host = encoder.apply(host);
		nsfPath = "/" + encoder.apply(nsfPath); //$NON-NLS-1$
		return new URI(DesignFileSystemProvider.SCHEME, userName, host, -1, nsfPath, null, null);
	}
	
	/**
	 * Converts a provided NSF API path to a {@link URI} object referencing the
	 * {@value DesignFileSystemProvider#SCHEME} filesystem.
	 * 
	 * @param apiPath the API path to convert
	 * @return the URI version of the API path
	 * @throws URISyntaxException if there is a problem building the URI
	 * @throws IllegalArgumentException if {@code apiPath} is empty
	 */
	public static URI toFileSystemURI(String userName, String apiPath, String pathBit, String... morePathBits) throws URISyntaxException {
		if(StringUtil.isEmpty(apiPath)) {
			throw new IllegalArgumentException("apiPath cannot be empty");
		}
		
		URI base = toFileSystemURI(userName, apiPath);
		
		String pathInfo = concat("/", base.getPath()); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(pathBit)) {
			pathInfo = concat(pathInfo, pathBit);
		}
		for(String bit : morePathBits) {
			if(StringUtil.isNotEmpty(bit)) {
				pathInfo = concat(pathInfo, bit);
			}
		}
		
		return new URI(DesignFileSystemProvider.SCHEME, userName, base.getHost(), -1, pathInfo, null, null);
	}
	
	public static String concat(final char delim, final String... parts) {
		if(parts == null || parts.length == 0) {
			return StringUtil.EMPTY_STRING;
		}
		String path = parts[0];
		for(int i = 1; i < parts.length; i++) {
			path = PathUtil.concat(path, parts[i], delim);
		}
		return path;
	}

	public static String concat(final String... parts) {
		return concat('/', parts);
	}
	
	@FunctionalInterface
	public static interface NotesDocumentFunction<T> {
		T apply(NNote doc) throws Exception;
	}
	
	@FunctionalInterface
	public static interface NotesDocumentConsumer {
		void accept(NNote doc) throws Exception;
	}
	
	@FunctionalInterface
	public static interface NotesDatabaseFunction<T> {
		T apply(NDatabase doc) throws Exception;
	}
	
	@FunctionalInterface
	public static interface NotesDatabaseConsumer {
		void accept(NDatabase doc) throws Exception;
	}
	
	/**
	 * Executes the provided function with a document for the provided path.
	 * 
	 * @param <T> the type returned by {@code func}
	 * @param path the context {@link DesignPath}
	 * @param cacheId an identifier used to cache the result based on the database modification
	 * 			time. Pass {@code null} to skip cache
	 * @param func the function to call
	 * @return the return value of {@code func}
	 * @throws RuntimeException wrapping any exception thrown by the main body
	 */
	public static <T> T callWithDocument(DesignPath path, String cacheId, NotesDocumentFunction<T> func) {
		return callWithDatabase(path, cacheId, database-> {
			try(NNote doc = DesignAccessor.getDocument(path, database)) {
				return func.apply(doc);
			}
		});
	}
	
	/**
	 * Executes the provided function with a document for the provided path.
	 * 
	 * @param path the context {@link DesignPath}
	 * @param consumer the consumer to call
	 * @throws RuntimeException wrapping any exception thrown by the main body
	 */
	public static void runWithDocument(DesignPath path, NotesDocumentConsumer consumer) {
		runWithDatabase(path, database -> {
			try(NNote doc = DesignAccessor.getDocument(path, database)) {
				consumer.accept(doc);
			}
		});
	}
	
	private static final Map<String, TimedCacheHolder> PER_DATABASE_CACHE = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Executes the provided function with the database for the provided path.
	 * 
	 * @param <T> the type returned by {@code func}
	 * @param path the context {@link DesignPath}
	 * @param cacheId an identifier used to cache the result based on the database modification
	 * 			time. Pass {@code null} to skip cache
	 * @param func the function to call
	 * @return the return value of {@code func}
	 * @throws RuntimeException wrapping any exception thrown by the main body
	 */
	@SuppressWarnings("unchecked")
	public static <T> T callWithDatabase(DesignPath path, String cacheId, NotesDatabaseFunction<T> func) {
		return NotesThreadFactory.callAs(dn(path.getFileSystem().getUserName()), session -> {
			NDatabase database = getDatabase(session, path.getFileSystem());
			if(StringUtil.isEmpty(cacheId)) {
				return func.apply(database);
			} else {
				long modTime = database.getLastModified().toEpochMilli();
				String dbKey = database.getFilePath() + "//" + session.getEffectiveUserName(); //$NON-NLS-1$
				TimedCacheHolder cacheHolder = PER_DATABASE_CACHE.computeIfAbsent(dbKey, key -> new TimedCacheHolder());
				return (T)cacheHolder.get(modTime).computeIfAbsent(cacheId, key -> {
					try {
						return func.apply(database);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
			}
		});
	}
	
	/**
	 * Invalidates any in-memory cache for the provided database.
	 * 
	 * @param database 
	 */
	public static synchronized void invalidateDatabaseCache(NDatabase database) {
		String dbKeyPrefix = database.getFilePath();
		Iterator<String> iter = PER_DATABASE_CACHE.keySet().iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			if(key.startsWith(dbKeyPrefix+"//")) { //$NON-NLS-1$
				iter.remove();
			}
		}
	}
	
	private static final ThreadLocal<Map<String, NDatabase>> THREAD_DATABASES = ThreadLocal.withInitial(HashMap::new);

	/**
	 * Executes the provided function with the database for the provided path.
	 * 
	 * @param path the context {@link DesignPath}
	 * @param consumer the function to call
	 * @throws RuntimeException wrapping any exception thrown by the main body
	 */
	public static void runWithDatabase(DesignPath path, NotesDatabaseConsumer consumer) {
		NotesThreadFactory.runAs(dn(path.getFileSystem().getUserName()), session -> {
			NDatabase database = getDatabase(session, path.getFileSystem());
			consumer.accept(database);
		});
	}
	
	public static String shortCn(String name) {
		try(NotesAPI session = NotesAPI.get()) {
			return session.toCn(name).replaceAll("\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	// *******************************************************************************
	// * Internal utilities
	// *******************************************************************************
	
	private static String dn(String name) {
		try(NotesAPI session = NotesAPI.get()) {
			return session.toDn(name);
		}
	}
	
	private static NDatabase getDatabase(NotesAPI session, DesignFileSystem fileSystem) {
		String nsfPath = fileSystem.getNsfPath();
		String key = session.getEffectiveUserName() + nsfPath;
		return THREAD_DATABASES.get().computeIfAbsent(key, k -> {
			int bangIndex = nsfPath.indexOf("!!"); //$NON-NLS-1$
			String server;
			String dbPath;
			if(bangIndex < 0) {
				server = ""; //$NON-NLS-1$
				dbPath = nsfPath;
			} else {
				server = nsfPath.substring(0, bangIndex);
				dbPath = nsfPath.substring(bangIndex+2);
			}
			if(isReplicaID(dbPath)) {
				throw new UnsupportedOperationException("Opening by replica ID not implemented");
			} else {
				return session.openDatabase(server, dbPath);
			}
		});
	}
	
	public static NoteType noteTypeForEntry(NViewEntry entry) {
		Object[] columnValues = entry.getColumnValues();
		String flags = (String)columnValues[4];
		String title = extractTitleValue((String)columnValues[0]);
		String flagsExt = (String)columnValues[17];
		int noteClass = entry.getNoteClass();
		
		if(flags.indexOf('X') > -1) {
			return NoteType.AgentData;
		}
		
		
		switch(noteClass & NOTE_CLASS_NONPRIV) {
		case NOTE_CLASS_ACL:
			return NoteType.ACL;
		case NsfNote.NOTE_CLASS_DESIGN:
			return NoteType.DesignCollection;
		case NOTE_CLASS_ICON:
			return NoteType.IconNote;
		case NsfNote.NOTE_CLASS_VIEW:
			if(matchesFlagsPattern(flags, DFLAGPAT_FOLDER_DESIGN)) {
				return NoteType.Folder;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_VIEWMAP_DESIGN)) {
				return NoteType.Navigator;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SHARED_COLS)) {
				return NoteType.SharedColumn;
			} else {
				return NoteType.View;
			}
		case NsfNote.NOTE_CLASS_FIELD:
			return NoteType.SharedField;
		case NsfNote.NOTE_CLASS_HELP:
			return NoteType.UsingDocument;
		case NsfNote.NOTE_CLASS_INFO:
			return NoteType.AboutDocument;
		case NsfNote.NOTE_CLASS_FILTER:
			// "filter" is a dumping ground for pre-XPages code elements
			
			if(flags.indexOf(DESIGN_FLAG_DATABASESCRIPT) > -1) {
				return NoteType.DBScript;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SITEMAP)) {
				return NoteType.Outline;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_LS)) {
				if(flagsExt.indexOf(DESIGN_FLAGEXT_WEBSERVICELIB) > -1) {
					return NoteType.LotusScriptWebServiceConsumer;
				} else {
					return NoteType.LotusScriptLibrary; 
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_JAVA)) {
				if(flagsExt.indexOf(DESIGN_FLAGEXT_WEBSERVICELIB) > -1) {
					return NoteType.JavaWebServiceConsumer;
				} else {
					return NoteType.JavaLibrary;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_JS)) {
				return NoteType.JavaScriptLibrary;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SCRIPTLIB_SERVER_JS)) {
				return NoteType.ServerJavaScriptLibrary;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_JAVA_WEBSERVICE)) {
				return NoteType.JavaWebService;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_LS_WEBSERVICE)) {
				return NoteType.LotusScriptWebService;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_DATA_CONNECTION_RESOURCE)) {
				return NoteType.DataConnection;
			}
			
			// Determine from here what kind of agent it is
			int assistType = 0;
			Number assistTypeNum = ((Number)columnValues[9]);
			assistType = assistTypeNum == null ? 0 : assistTypeNum.intValue();
			
			if(flags.indexOf(DESIGN_FLAG_LOTUSSCRIPT_AGENT) > -1) {
				return NoteType.LotusScriptAgent;
			} else if(flags.indexOf(DESIGN_FLAG_JAVA_AGENT) > -1 || flags.indexOf(DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE) > -1 || assistType == ASSIST_TYPE_JAVA) {
				// There's not a proper pattern for distinguishing between these two, so look for another marker
				// TODO see if there's a better marker that wouldn't require opening the note
				return NoteType.JavaAgent;
//				if(flags.indexOf(DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE) > -1 || note.hasItem(ITEM_NAME_JAVA_COMPILER_SOURCE)) {
//					return NoteType.JavaAgent;
//				} else {
//					return NoteType.ImportedJavaAgent;
//				}
			} else if(assistType == -1) {
				return NoteType.SimpleActionAgent;
			} else {
				return NoteType.FormulaAgent;
			}
		case NsfNote.NOTE_CLASS_FORM:
			// Pretty much everything is a form nowadays
			if(flags.isEmpty()) {
				// Definitely an actual form
				return NoteType.Form;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				if(IMAGE_NEW_DBICON_NAME.equals(title)) {
					return NoteType.DBIcon;
				}
				return NoteType.ImageResource;
			} else if(flags.indexOf(DESIGN_FLAG_JARFILE) > -1) {
				return NoteType.Jar;			
			} else if(matchesFlagsPattern(flags, DFLAGPAT_COMPDEF)) {
				return NoteType.WiringProperties;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_COMPAPP)) {
				return NoteType.CompositeApplication;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_WIDGET)) {
				return NoteType.CompositeComponent;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_XSPCC)) {
				if(flags.indexOf(DESIGN_FLAG_PROPFILE) > -1) {
					return NoteType.CustomControlProperties;
				} else {
					return NoteType.CustomControl;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_XSPPAGE)) {
				if(flags.indexOf(DESIGN_FLAG_PROPFILE) > -1) {
					return NoteType.XPageProperties;
				} else {
					return NoteType.XPage;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_STYLEKIT)) {
				return NoteType.Theme;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_WEBPAGE)) {
				return NoteType.Page;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_IMAGE_RESOURCE)) {
				return NoteType.ImageResource;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_STYLE_SHEET_RESOURCE)) {
				return NoteType.StyleSheet;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SUBFORM_DESIGN)) {
				return NoteType.Subform;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_FRAMESET)) {
				return NoteType.Frameset;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_DB2ACCESSVIEW)) {
				return NoteType.DB2AccessView;
			} else if(matchesFlagsPattern(flags, DFLAGPAT_FILE)) {
				// xspdesign.properties needs special handling, but is distinguished only by file name
				// TODO check if this handles xspdesign.properties correctly
				
				if(flags.indexOf(DESIGN_FLAG_HIDEFROMDESIGNLIST) == -1) {
					return NoteType.FileResource;
				} else if("xspdesign.properties".equals(title)) { //$NON-NLS-1$
					return NoteType.XSPDesignProperties;
				} else if(flagsExt.indexOf(DESIGN_FLAGEXT_WEBCONTENTFILE) > -1) {
					return NoteType.WebContentFile;
				} else if(matchesFlagsPattern(flags, DFLAGPAT_JAVAFILE)) {
					return NoteType.Java;
				} else {
					return NoteType.GenericFile;
				}
			} else if(matchesFlagsPattern(flags, DFLAGPAT_SACTIONS_DESIGN)) {
				return NoteType.SharedActions;
			} else if(flags.indexOf(DESIGN_FLAG_JAVA_RESOURCE) > -1) {
				return NoteType.Applet;
			} else {
				return NoteType.Form;
			}
		case NsfNote.NOTE_CLASS_REPLFORMULA:
			return NoteType.ReplicationFormula;
		}
		
		return NoteType.Unknown;
	}
	
	/**
	 * Retrieves the applicable title value from the provided $TITLE string,
	 * which may contain aliases.
	 * 
	 * @param title the compressed $TITLE value
	 * @return the applicable title
	 */
	public static String extractTitleValue(String title) {
		int barIndex = title.lastIndexOf('|');
		if(barIndex > -1 && barIndex != title.length()-1) {
			return title.substring(barIndex+1);
		} else {
			return title;
		}
		
	}
	
	private static boolean isReplicaID(String dbPath) {
		String id = normalizeReplicaID(dbPath);
		if (id == null) {
			return false;
		} else {
			for (int i = 0; i < 16; ++i) {
				if ("0123456789ABCDEF".indexOf(id.charAt(i)) < 0) { //$NON-NLS-1$
					return false;
				}
			}

			return true;
		}
	}
	
	private static String normalizeReplicaID(String id) {
		String replicaId = id;
		if (StringUtil.isNotEmpty(replicaId)) {
			if (replicaId.indexOf(':') == 8 && replicaId.length() == 17) {
				replicaId = replicaId.substring(0, 8) + replicaId.substring(9, 17);
			}

			if (replicaId.length() == 16) {
				return replicaId.toUpperCase();
			}
		}

		return null;
	}
}
