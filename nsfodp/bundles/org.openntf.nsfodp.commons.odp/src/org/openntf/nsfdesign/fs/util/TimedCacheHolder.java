/**
 * Copyright © 2019-2020 Jesse Gallagher
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
import java.util.Map;

/**
 * Utility class to hold a cache map that expires based on a last-modification date.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class TimedCacheHolder {
	private long lastModified = -1;
	private Map<String, Object> cache;
	
	public synchronized Map<String, Object> get(long modTime) {
		if(this.cache == null || this.lastModified == -1 || modTime > this.lastModified) {
			this.cache = new HashMap<>();
			this.lastModified = modTime;
		}
		return this.cache;
	}
}
