package org.openntf.maven.nsfodp.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

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
}
