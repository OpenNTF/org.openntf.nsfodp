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
		Header contentType = res.getFirstHeader("Content-Type");
		if(contentType != null && String.valueOf(contentType.getValue()).startsWith("text/html")) {
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
			switch(obj.get("type").getAsString()) {
			case "beginTask":
				if(log.isInfoEnabled()) {
					log.info("Begin task: " + obj.get("name").getAsString());
				}
				break;
			case "internalWorked":
				// Ignore
				break;
			case "task":
				if(log.isInfoEnabled()) {
					log.info("Begin task: " + obj.get("name").getAsString());
				}
				break;
			case "subTask":
				if(log.isInfoEnabled()) {
					log.info(obj.get("name").getAsString());
				}
				break;
			case "work":
				// Ignore
				break;
			case "cancel":
				throw new RuntimeException("Work was canceled on the server");
			case "done":
				return;
			case "error":
				System.err.println(obj.get("stackTrace").getAsString());
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
