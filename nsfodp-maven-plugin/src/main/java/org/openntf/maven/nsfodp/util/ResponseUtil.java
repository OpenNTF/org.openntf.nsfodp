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
package org.openntf.maven.nsfodp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public enum ResponseUtil {
	;
	
	public static HttpEntity checkResponse(Log log, HttpResponse res) throws IOException {
		int status = res.getStatusLine().getStatusCode();
		HttpEntity responseEntity = res.getEntity();
		if(log.isDebugEnabled()) {
			log.debug("Received entity: " + responseEntity);
		}
		if(status < 200 || status >= 300) {
			String errorBody;
			try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				try(InputStream is = responseEntity.getContent()) {
					IOUtil.copy(is, baos);
				}
				errorBody = baos.toString();
			}
			System.err.println("Received error from server:");
			System.err.println(errorBody);
			throw new IOException("Received unexpected HTTP response: " + res.getStatusLine());
		}
		
		// Check for an auth form - Domino returns these as status 200
		Header contentType = res.getFirstHeader("Content-Type"); //$NON-NLS-1$
		if(contentType != null && String.valueOf(contentType.getValue()).startsWith("text/html")) { //$NON-NLS-1$
			throw new IOException("Authentication failed for specified user");
		}
		
		return responseEntity;
	}
	
	/**
	 * Reads the response for line-delimited JSON messages until the object's type
	 * is "done", "cancel", or "error".
	 * 
	 * @param is the response input stream
	 * @throws IOException if there is a problem reading the input stream
	 * @throws RuntimeException if the work was canceled on the server
	 */
	public static void monitorResponse(Log log, InputStream is) throws IOException {
		// Start streaming the JSON responses until done
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		String line;
		JsonParser parser = new JsonParser();
		while((line = readLine(is, buffer)) != null) {
			if(log.isDebugEnabled()) {
				log.debug("Received JSON message: " + line);
			}
			JsonObject obj = parser.parse(line).getAsJsonObject();
			switch(obj.get("type").getAsString()) { //$NON-NLS-1$
			case "beginTask": //$NON-NLS-1$
				if(log.isInfoEnabled()) {
					log.info("Begin task: " + obj.get("name").getAsString()); //$NON-NLS-2$
				}
				break;
			case "internalWorked": //$NON-NLS-1$
				// Ignore
				break;
			case "task": //$NON-NLS-1$
				if(log.isInfoEnabled()) {
					log.info("Begin task: " + obj.get("name").getAsString()); //$NON-NLS-2$
				}
				break;
			case "subTask": //$NON-NLS-1$
				if(log.isInfoEnabled()) {
					log.info(obj.get("name").getAsString()); //$NON-NLS-1$
				}
				break;
			case "work": //$NON-NLS-1$
				// Ignore
				break;
			case "cancel": //$NON-NLS-1$
				throw new RuntimeException("Work was canceled on the server");
			case "done": //$NON-NLS-1$
				return;
			case "error": //$NON-NLS-1$
				System.err.println(obj.get("stackTrace").getAsString()); //$NON-NLS-1$
				throw new RuntimeException("Server reported an error");
			default:
				throw new IllegalArgumentException("Received unexpected JSON message: " + line);
			}
		}
	}
	
	/**
	 * Reads a line of text from the given input stream. This is used in lieu of BufferedReader
	 * in order to not read into the trailing binary data.
	 */
	private static String readLine(InputStream is, ByteArrayOutputStream buffer) throws IOException {
		buffer.reset();
		while(true) {
			int val = is.read();
			if(val == '\n') {
				break;
			} else if(val == '\r') {
				// ignore these
			} else {
				buffer.write(val);
			}
		}
		return buffer.toString();
	}
}
