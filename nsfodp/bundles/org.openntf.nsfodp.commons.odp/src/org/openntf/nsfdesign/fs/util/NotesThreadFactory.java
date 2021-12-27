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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

//import com.ibm.domino.napi.NException;
//import com.ibm.domino.napi.c.Os;
//
//import lotus.domino.NotesException;
//import lotus.domino.NotesFactory;
//import lotus.domino.NotesThread;
//import lotus.domino.Session;

/**
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NotesThreadFactory implements ThreadFactory {
	public static final NotesThreadFactory instance = new NotesThreadFactory();
	public static final ExecutorService executor = Executors.newCachedThreadPool(instance);
	public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5, instance);
	
//	@FunctionalInterface
//	public static interface NotesFunction<T> {
//		T apply(Session session) throws Exception;
//	}
//	
//	@FunctionalInterface
//	public static interface NotesConsumer {
//		void accept(Session session) throws Exception;
//	}
	
//	private static final ThreadLocal<Map<String, Session>> THREAD_SESSION_MAP = ThreadLocal.withInitial(HashMap::new);
//	private static final ThreadLocal<Set<Long>> THREAD_HANDLES = ThreadLocal.withInitial(HashSet::new);
	
//	/**
//	 * Evaluates the provided function in a separate {@link NotesThread} with
//	 * a {@link Session} for the active Notes ID.
//	 * 
//	 * @param <T> the type of object returned by {@code func}
//	 * @param func the function to call
//	 * @return the return value of {@code func}
//	 * @throws RuntimeException wrapping any exception thrown by the main body
//	 */
//	public static <T> T call(NotesFunction<T> func) {
//		try {
//			return NotesThreadFactory.executor.submit(() -> {
//				Session session = THREAD_SESSION_MAP.get().computeIfAbsent(null, key -> {
//					try {
//						return NotesFactory.createSession();
//					} catch (NotesException e) {
//						throw new RuntimeException(e);
//					}
//				});
//				return func.apply(session);
//			}).get();
//		} catch (InterruptedException | ExecutionException e) {
//			throw new RuntimeException(e);
//		}
//	}
	
//	/**
//	 * Evaluates the provided consumer in a separate {@link NotesThread} with
//	 * a {@link Session} for the active Notes ID.
//	 * 
//	 * @param func the consumer to call
//	 * @throws RuntimeException wrapping any exception thrown by the main body
//	 */
//	public static void run(NotesConsumer func) {
//		call(session -> {
//			func.accept(session);
//			return null;
//		});
//	}
	
//	/**
//	 * Evaluates the provided function in a separate {@link NotesThread} with
//	 * a {@link Session} for the provided Notes user name.
//	 * 
//	 * @param <T> the type of object returned by {@code func}
//	 * @param userName the user to run the provided function as
//	 * @param func the function to call
//	 * @return the return value of {@code func}
//	 * @throws RuntimeException wrapping any exception thrown by the main body
//	 */
//	public static <T> T callAs(String userName, NotesFunction<T> func) {
//		try {
//			return NotesThreadFactory.executor.submit(() -> {
//				Session session = THREAD_SESSION_MAP.get().computeIfAbsent(userName, key -> {
//					return SudoUtils.getSessionAs(key, THREAD_HANDLES.get());
//				});
//				return func.apply(session);
//			}).get();
//		} catch (InterruptedException | ExecutionException e) {
//			throw new RuntimeException(e);
//		}
//	}
	
//	/**
//	 * Evaluates the provided consumer in a separate {@link NotesThread} with
//	 * a {@link Session} for the provided Notes user name.
//	 * 
//	 * @param userName the user to run the provided function as
//	 * @param func the consumer to call
//	 * @throws RuntimeException wrapping any exception thrown by the main body
//	 */
//	public static void runAs(String userName, NotesConsumer func) {
//		callAs(userName, session -> {
//			func.accept(session);
//			return null;
//		});
//	}

	@Override
	public Thread newThread(Runnable r) {
//		return new NotesThread(r) {
//			@Override
//			public void termThread() {
//				for(Session session : THREAD_SESSION_MAP.get().values()) {
//					try {
//						session.recycle();
//					} catch(NotesException e) {
//					}
//				}
//				THREAD_SESSION_MAP.get().clear();
//				for(long hName : THREAD_HANDLES.get()) {
//					try {
//						Os.OSUnlock(hName);
//						Os.OSMemFree(hName);
//					} catch (NException e) {
//					}
//				}
//				THREAD_HANDLES.get().clear();
//				
//				super.termThread();
//			}
//		};
	  return new Thread(r);
	}

	public static void term() {
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.MINUTES);
		} catch(InterruptedException e) {
		}
		scheduler.shutdown();
		try {
			scheduler.awaitTermination(1, TimeUnit.MINUTES);
		} catch(InterruptedException e) {
		}
	}
}
