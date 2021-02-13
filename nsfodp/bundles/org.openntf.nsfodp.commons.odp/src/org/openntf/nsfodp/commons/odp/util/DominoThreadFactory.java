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
package org.openntf.nsfodp.commons.odp.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import lotus.domino.NotesThread;

public class DominoThreadFactory implements ThreadFactory {
	private static int spawnCount = 0;
	private static final Object sync = new Object();

	public static final DominoThreadFactory instance = new DominoThreadFactory();

	private static ExecutorService executor;
	private static ScheduledExecutorService scheduler;
	private static boolean initialized;
	
	public static synchronized ExecutorService getExecutor() {
		init();
		return executor;
	}
	public static synchronized ScheduledExecutorService getScheduler() {
		init();
		return scheduler;
	}
	
	public static synchronized void init() {
		if(!initialized) {
			executor = Executors.newCachedThreadPool(instance);
			scheduler = Executors.newScheduledThreadPool(5, instance);
			initialized = true;
		}
	}
	public static synchronized void term() {
		if(initialized) {
			if(executor != null) {
				executor.shutdownNow();
			}
			if(scheduler != null) {
				scheduler.shutdownNow();
			}
			try {
				if(executor != null) {
					executor.awaitTermination(1, TimeUnit.MINUTES);
				}
				if(scheduler != null) {
					scheduler.awaitTermination(1, TimeUnit.MINUTES);
				}
			} catch (InterruptedException e) {
				// Ignore
			}
			initialized = false;
		}
	}

	@Override
	public Thread newThread(final Runnable runnable) {
		synchronized(sync) {
			spawnCount++;
		}
		return new NotesThread(runnable, "DominoThreadFactory Thread " + spawnCount); //$NON-NLS-1$
	}
}