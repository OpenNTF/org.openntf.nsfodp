package org.openntf.maven.nsfodp.jvm;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;
import org.openntf.maven.nsfodp.Messages;
import org.openntf.nsfodp.commons.jvm.JvmEnvironment;

public abstract class AbstractJvmEnvironment implements JvmEnvironment {

	@Override
	public Path getJavaBin(Path notesProgram) {
		Path jvmBin = getJavaHome(notesProgram).resolve("bin"); //$NON-NLS-1$
		
		String javaBinName;
		if(SystemUtils.IS_OS_WINDOWS) {
			javaBinName = "java.exe"; //$NON-NLS-1$
		} else {
			javaBinName = "java"; //$NON-NLS-1$
		}
		Path javaBin = jvmBin.resolve(javaBinName);
		if(!Files.exists(javaBin)) {
			throw new RuntimeException(Messages.getString("EquinoxMojo.unableToLocateJava", javaBin)); //$NON-NLS-1$
		}
		
		return javaBin;
	}

}
