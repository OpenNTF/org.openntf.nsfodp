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
package org.openntf.nsfdesign.fs.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.openntf.nsfdesign.fs.NSFFileSystem;
import org.openntf.nsfdesign.fs.NSFFileSystemProvider;
import org.openntf.nsfdesign.fs.NSFPath;
import org.openntf.nsfdesign.fs.db.NSFAccessor;

//import com.ibm.commons.util.PathUtil;
//import com.ibm.commons.util.StringUtil;
//
//import lotus.domino.Database;
//import lotus.domino.Document;
//import lotus.domino.Name;
//import lotus.domino.NotesException;
//import lotus.domino.Session;
//import lotus.domino.DateTime;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
@SuppressWarnings("nls")
public enum NSFPathUtil {
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
	 * @since 1.0.0
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
	 * @since 1.0.0
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
	 * Converts a provided NSF API path to a {@link URI} object referencing the {@code "nsffilestore"}
	 * filesystem.
	 * 
	 * @param apiPath the API path to convert
	 * @return the URI version of the API path
	 * @throws URISyntaxException if there is a problem building the URI
	 * @since 1.0.0
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
		return new URI(NSFFileSystemProvider.SCHEME, userName, host, -1, nsfPath, null, null);
	}
	
	/**
	 * Converts a provided NSF API path to a {@link URI} object referencing the {@code "nsffilestore"}
	 * filesystem.
	 * 
	 * @param apiPath the API path to convert
	 * @return the URI version of the API path
	 * @throws URISyntaxException if there is a problem building the URI
	 * @since 1.0.0
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
		
		return new URI(NSFFileSystemProvider.SCHEME, userName, base.getHost(), -1, pathInfo, null, null);
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
	
//	@FunctionalInterface
//	public static interface NotesDocumentFunction<T> {
//		T apply(Document doc) throws Exception;
//	}
//	
//	@FunctionalInterface
//	public static interface NotesDocumentConsumer {
//		void accept(Document doc) throws Exception;
//	}
//	
//	@FunctionalInterface
//	public static interface NotesDatabaseFunction<T> {
//		T apply(Database doc) throws Exception;
//	}
//	
//	@FunctionalInterface
//	public static interface NotesDatabaseConsumer {
//		void accept(Database doc) throws Exception;
//	}
	
//	/**
//	 * Executes the provided function with a document for the provided path.
//	 * 
//	 * @param <T> the type returned by {@code func}
//	 * @param path the context {@link NSFPath}
//	 * @param cacheId an identifier used to cache the result based on the database modification
//	 * 			time. Pass {@code null} to skip cache
//	 * @param func the function to call
//	 * @return the return value of {@code func}
//	 * @throws RuntimeException wrapping any exception thrown by the main body
//	 */
//	public static <T> T callWithDocument(NSFPath path, String cacheId, NotesDocumentFunction<T> func) {
//		return callWithDatabase(path, cacheId, database-> {
//			Document doc = NSFAccessor.getDocument(path, database);
//			try {
//				return func.apply(doc);
//			} finally {
//				doc.recycle();
//			}
//		});
//	}
	
//	/**
//	 * Executes the provided function with a document for the provided path.
//	 * 
//	 * @param path the context {@link NSFPath}
//	 * @param consumer the consumer to call
//	 * @throws RuntimeException wrapping any exception thrown by the main body
//	 */
//	public static void runWithDocument(NSFPath path, NotesDocumentConsumer consumer) {
//		runWithDatabase(path, database -> {
//			Document doc = NSFAccessor.getDocument(path, database);
//			try {
//				consumer.accept(doc);
//			} finally {
//				doc.recycle();
//			}
//		});
//	}
	
	private static final Map<String, TimedCacheHolder> PER_DATABASE_CACHE = Collections.synchronizedMap(new HashMap<>());

//	/**
//	 * Executes the provided function with the database for the provided path.
//	 * 
//	 * @param <T> the type returned by {@code func}
//	 * @param path the context {@link NSFPath}
//	 * @param cacheId an identifier used to cache the result based on the database modification
//	 * 			time. Pass {@code null} to skip cache
//	 * @param func the function to call
//	 * @return the return value of {@code func}
//	 * @throws RuntimeException wrapping any exception thrown by the main body
//	 */
//	@SuppressWarnings("unchecked")
//	public static <T> T callWithDatabase(NSFPath path, String cacheId, NotesDatabaseFunction<T> func) {
//		return NotesThreadFactory.callAs(dn(path.getFileSystem().getUserName()), session -> {
//			Database database = getDatabase(session, path.getFileSystem());
//			if(StringUtil.isEmpty(cacheId)) {
//				return func.apply(database);
//			} else {
//				long modTime;
//				DateTime mod = database.getLastModified();
//				try {
//					modTime = mod.toJavaDate().getTime();
//				} finally {
//					mod.recycle();
//				}
//				String dbKey = database.getFilePath() + "//" + session.getEffectiveUserName(); //$NON-NLS-1$
//				TimedCacheHolder cacheHolder = PER_DATABASE_CACHE.computeIfAbsent(dbKey, key -> new TimedCacheHolder());
//				return (T)cacheHolder.get(modTime).computeIfAbsent(cacheId, key -> {
//					try {
//						return func.apply(database);
//					} catch (Exception e) {
//						throw new RuntimeException(e);
//					}
//				});
//			}
//		});
//	}
	
//	/**
//	 * Invalidates any in-memory cache for the provided database.
//	 * 
//	 * @param database 
//	 */
//	public static synchronized void invalidateDatabaseCache(Database database) throws NotesException {
//		String dbKeyPrefix = database.getFilePath();
//		Iterator<String> iter = PER_DATABASE_CACHE.keySet().iterator();
//		while(iter.hasNext()) {
//			String key = iter.next();
//			if(key.startsWith(dbKeyPrefix+"//")) { //$NON-NLS-1$
//				iter.remove();
//			}
//		}
//	}
	
//	private static final ThreadLocal<Map<String, Database>> THREAD_DATABASES = ThreadLocal.withInitial(HashMap::new);

//	/**
//	 * Executes the provided function with the database for the provided path.
//	 * 
//	 * @param path the context {@link NSFPath}
//	 * @param consumer the function to call
//	 * @throws RuntimeException wrapping any exception thrown by the main body
//	 */
//	public static void runWithDatabase(NSFPath path, NotesDatabaseConsumer consumer) {
//		NotesThreadFactory.runAs(dn(path.getFileSystem().getUserName()), session -> {
//			Database database = getDatabase(session, path.getFileSystem());
//			consumer.accept(database);
//		});
//	}
	
//	public static String shortCn(String name) {
//		return NotesThreadFactory.call(session -> {
//			Name n = session.createName(name);
//			try {
//				return n.getCommon().replaceAll("\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
//			} finally {
//				n.recycle();
//			}
//		});
//	}
	
	// *******************************************************************************
	// * Internal utilities
	// *******************************************************************************
	
//	private static String dn(String name) {
//		return NotesThreadFactory.call(session -> session.createName(name).getCanonical());
//	}
	
//	private static Database getDatabase(Session session, NSFFileSystem fileSystem) throws NotesException {
//		String nsfPath = fileSystem.getNsfPath();
//		String key = session.getEffectiveUserName() + nsfPath;
//		return THREAD_DATABASES.get().computeIfAbsent(key, k -> {
//			try {
//				int bangIndex = nsfPath.indexOf("!!"); //$NON-NLS-1$
//				String server;
//				String dbPath;
//				if(bangIndex < 0) {
//					server = ""; //$NON-NLS-1$
//					dbPath = nsfPath;
//				} else {
//					server = nsfPath.substring(0, bangIndex);
//					dbPath = nsfPath.substring(bangIndex+2);
//				}
//				if(isReplicaID(dbPath)) {
//					Database database = session.getDatabase(null, null);
//					database.openByReplicaID(server, normalizeReplicaID(dbPath));
//					return database;
//				} else {
//					return session.getDatabase(server, dbPath);
//				}
//			} catch(NotesException e) {
//				throw new RuntimeException(e);
//			}
//		});
//	}
	
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
