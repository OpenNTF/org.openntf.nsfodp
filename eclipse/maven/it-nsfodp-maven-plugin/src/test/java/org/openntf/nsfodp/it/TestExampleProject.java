package org.openntf.nsfodp.it;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

@SuppressWarnings("nls")
public class TestExampleProject {

	@Test
	public void testExample() throws VerificationException, IOException {
		// File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-xxxx" );
		
		Path exampleProject = Paths.get("/Users/jesse/Projects/org.openntf.nsfodp/example");
		Verifier verifier = new Verifier(exampleProject.toString());
		
		verifier.deleteArtifacts("org.openntf.nsfodp.example");
		
		List<String> cliOptions = new ArrayList<>();
		verifier.setCliOptions(cliOptions);
		verifier.executeGoal("install");
		
		verifier.verifyErrorFreeLog();
		
		verifier.resetStreams();
	}

}
