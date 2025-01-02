/*
 * Copyright (c) 2018-2025 Jesse Gallagher
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
package org.openntf.nsfodp.notesapi.darwinonapi;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.darwino.domino.napi.DominoThread;

public class DarwinoNAPIThreadFactory implements ThreadFactory {
	private static AtomicInteger spawnCount = new AtomicInteger(0);

	@Override
	public Thread newThread(Runnable r) {
		int count = spawnCount.incrementAndGet();
		return new DominoThread(r, "DominoThreadFactory Thread " + count); //$NON-NLS-1$
	}

}
