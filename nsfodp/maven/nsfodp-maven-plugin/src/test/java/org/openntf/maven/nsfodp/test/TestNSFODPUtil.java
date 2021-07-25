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
