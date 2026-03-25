/*
 * Copyright © 2018-2026 Contributors to the NSF ODP Tooling Project
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.openntf.nsfodp.commons.odp.util.ODPUtil;

@SuppressWarnings("nls")
public class TestODPUtil {
	@Test
	public void testToJavaClassName() {
		Path filePath = Paths.get("org/openntf/nsfodp/example/ExampleClass.java");
		String expected = "org.openntf.nsfodp.example.ExampleClass";
		
		assertEquals(expected, ODPUtil.toJavaClassName(filePath));
	}
	
	@Test
	public void testDxlTime() {
		ZonedDateTime now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1774459564169l), ZoneId.of("America/New_York"));
		assertEquals("20260325T012604,16-04", ODPUtil.DXL_DATETIME.format(now));
	}
}
