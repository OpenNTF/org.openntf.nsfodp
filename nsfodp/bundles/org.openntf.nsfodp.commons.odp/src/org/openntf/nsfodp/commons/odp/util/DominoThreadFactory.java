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
package org.openntf.nsfodp.commons.odp.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.openntf.nsfodp.commons.odp.notesapi.NotesAPI;

public class DominoThreadFactory {
	private static NotesAPI notesApi;
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
			notesApi = NotesAPI.get();
			ThreadFactory fac = notesApi.createThreadFactory();
			executor = Executors.newCachedThreadPool(fac);
			scheduler = Executors.newScheduledThreadPool(5, fac);
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
			notesApi.close();
			initialized = false;
		}
	}
}