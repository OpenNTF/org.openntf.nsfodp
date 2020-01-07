package org.openntf.maven.nsfodp.util;

import java.io.IOException;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

/**
 * Contains methods for serializing to/from JSON.
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public enum JsonUtil {
	;
	
	public static String toJson(Object value) {
		if(value == null) {
			return "null"; //$NON-NLS-1$
		}
		try {
			return JsonGenerator.toJson(JsonJavaFactory.instance, value);
		} catch (JsonException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String json) {
		try {
			return (T)JsonParser.fromJson(JsonJavaFactory.instance, json);
		} catch (JsonException e) {
			throw new RuntimeException(e);
		}
	}
}
