/*
 * Copyright © 2018-2025 Jesse Gallagher
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.openntf.nsfodp.commons.NSFODPUtil;

@SuppressWarnings("nls")
public class TestNSFODPUtil {
	@Test
	public void testZipInputStream() throws IOException {
		// Write WORD length + 1
		byte[] data = new byte[0xFFFF + 1];
		for(int i = 0; i < data.length; i++) {
			data[i] = (byte)(i % Byte.MAX_VALUE);
		}
		
		Path tempFile = Files.createTempFile(getClass().getName(), ".zip");
		try {
			// Write the data to a basic ZIP file
			try(
				OutputStream os = Files.newOutputStream(tempFile, StandardOpenOption.TRUNCATE_EXISTING);
				ZipOutputStream zos = new ZipOutputStream(os, StandardCharsets.UTF_8)
			) {
				ZipEntry entry = new ZipEntry("example.bin");
				zos.putNextEntry(entry);
				zos.write(data);
			}
			
			// Read the data back out
			try(FileSystem fs = NSFODPUtil.openZipPath(tempFile)) {
				Path example = fs.getPath("/", "example.bin");
				try(InputStream is = NSFODPUtil.newInputStream(example)) {
					byte[] buf = new byte[0xFFFF + 1];
					is.read(buf);
					assertArrayEquals(data, buf);
				}
			}
		} finally {
			NSFODPUtil.deltree(tempFile);
		}
	}
}
