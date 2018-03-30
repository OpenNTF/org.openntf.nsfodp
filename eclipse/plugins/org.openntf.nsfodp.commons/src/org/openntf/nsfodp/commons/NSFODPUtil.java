package org.openntf.nsfodp.commons;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum NSFODPUtil {
	;

	/**
	 * Returns an appropriate temp directory for the system. On Windows, this is
	 * equivalent to <code>System.getProperty("java.io.tmpdir")</code>. On
	 * Linux, however, since this seems to return the data directory in some
	 * cases, it uses <code>/tmp</code>.
	 *
	 * @return an appropriate temp directory for the system
	 */
	public static Path getTempDirectory() {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Linux") || osName.startsWith("LINUX")) {
			return Paths.get("/tmp"); //$NON-NLS-1$
		} else {
			return Paths.get(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
		}
	}
}
