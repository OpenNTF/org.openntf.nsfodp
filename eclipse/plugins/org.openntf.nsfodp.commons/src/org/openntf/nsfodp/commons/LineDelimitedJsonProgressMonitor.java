/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.nsfodp.commons;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.com.eclipsesource.json.JsonObject;

/**
 * An implementation of {@link IProgressMonitor} sends monitor messages to an {@link OutputStream}
 * as line-delimited JSON.
 *  
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class LineDelimitedJsonProgressMonitor implements IProgressMonitor {
	private final OutputStream out;
	private boolean canceled = false;
	
	public LineDelimitedJsonProgressMonitor(OutputStream out) {
		this.out = Objects.requireNonNull(out);
	}

	@Override
	public void beginTask(String name, int totalWork) {
		try {
			println(message(
				"type", "beginTask", //$NON-NLS-1$ //$NON-NLS-2$
				"name", name, //$NON-NLS-1$
				"totalWork", totalWork //$NON-NLS-1$
				));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void done() {
		try {
			println(message(
				"type", "done" //$NON-NLS-1$ //$NON-NLS-2$
				));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void internalWorked(double work) {
		try {
			println(message(
				"type", "internalWorked", //$NON-NLS-1$ //$NON-NLS-2$
				"work", work //$NON-NLS-1$
				));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isCanceled() {
		return this.canceled;
	}

	@Override
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
		if(canceled) {
			try {
				println(message(
					"type", "cancel" //$NON-NLS-1$ //$NON-NLS-2$
				));
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void setTaskName(String name) {
		try {
			println(message(
				"type", "task", //$NON-NLS-1$ //$NON-NLS-2$
				"name", name //$NON-NLS-1$
			));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void subTask(String name) {
		try {
			println(message(
				"type", "subTask", //$NON-NLS-1$ //$NON-NLS-2$
				"name", name //$NON-NLS-1$
			));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void worked(int work) {
		try {
			println(message(
				"type", "worked", //$NON-NLS-1$ //$NON-NLS-2$
				"work", work //$NON-NLS-1$
			));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	// *******************************************************************************
	// * Utility methods
	// *******************************************************************************
	
	private void println(String message) throws IOException {
		out.write(message.getBytes());
		out.write('\r');
		out.write('\n');
	}
	
	public static String message(Object... parts) {
		JsonObject json = new JsonObject();
		for(int i = 0; i < parts.length; i += 2) {
			String key = parts[i] == null ? "" : parts[i].toString(); //$NON-NLS-1$
			Object val = i < parts.length-1 ? parts[i+1] : null;
			
			if(val instanceof Integer) {
				json.add(key, ((Integer)val).intValue());
			} else if(val instanceof Number) {
				json.add(key, ((Number)val).doubleValue());
			} else if(val instanceof Boolean) {
				json.add(key, ((Boolean)val).booleanValue());
			} else {
				json.add(key, val == null ? null : val.toString());
			}
		}
		
		return json.toString();
	}
}
