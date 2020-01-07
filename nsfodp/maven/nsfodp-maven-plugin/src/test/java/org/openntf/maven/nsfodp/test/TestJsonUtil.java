package org.openntf.maven.nsfodp.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openntf.maven.nsfodp.util.JsonUtil;

@SuppressWarnings("nls")
public class TestJsonUtil {
	@Test
	public void testPosixFilePaths() {
		List<String> paths = Arrays.asList("/foo/bar/baz");
		String expected = "[\"\\/foo\\/bar\\/baz\"]";
		String result = JsonUtil.toJson(paths);
		
		assertEquals(expected, result);
		
		List<String> deserialized = JsonUtil.fromJson(result);
		assertEquals(paths, deserialized);
	}
	
	@Test
	public void testWindowsFilePaths() {
		List<String> paths = Arrays.asList("C:\\Users\\work\\org.openntf.nsfodp\\example\\releng\\org.openntf.nsfodp.example.updatesite\\target\\site");
		String expected = "[\"C:\\\\Users\\\\work\\\\org.openntf.nsfodp\\\\example\\\\releng\\\\org.openntf.nsfodp.example.updatesite\\\\target\\\\site\"]";
		String result = JsonUtil.toJson(paths);
		
		assertEquals(expected, result);
		
		List<String> deserialized = JsonUtil.fromJson(result);
		assertEquals(paths, deserialized);
	}
}
