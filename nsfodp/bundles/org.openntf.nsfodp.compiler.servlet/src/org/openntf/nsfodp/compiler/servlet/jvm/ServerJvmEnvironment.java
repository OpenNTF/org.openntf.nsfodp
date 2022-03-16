package org.openntf.nsfodp.compiler.servlet.jvm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;
import org.openntf.nsfodp.compiler.servlet.Messages;

import com.ibm.commons.util.StringUtil;

/**
 * {@link JvmEnvironment} implementation that uses the JVM from the active
 * running Servlet container.
 * 
 * @author Jesse Gallagher
 * @since 3.9.0
 */
public class ServerJvmEnvironment implements JvmEnvironment {

	@Override
	public boolean isActive(Path notesProgram) {
		return true;
	}

	@Override
	public Path getJavaHome(Path notesProgram) {
		String path = AccessController.doPrivileged((PrivilegedAction<String>)() ->
			System.getProperty("java.home") //$NON-NLS-1$
		);
		if(StringUtil.isEmpty(path)) {
			throw new IllegalStateException(Messages.ODPCompilerServlet_emptyJavaHome);
		}
		return Paths.get(path);
	}

	@Override
	public Path getJavaBin(Path notesProgram) {
		Path home = getJavaHome(notesProgram);
		if(NSFODPUtil.isOsWindows()) {
			return home.resolve("bin").resolve("java.exe"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return home.resolve("bin").resolve("java"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
