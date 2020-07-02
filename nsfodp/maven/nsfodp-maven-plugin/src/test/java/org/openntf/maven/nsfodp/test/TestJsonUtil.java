/**
 * Copyright © 2018-2019 Jesse Gallagher
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
