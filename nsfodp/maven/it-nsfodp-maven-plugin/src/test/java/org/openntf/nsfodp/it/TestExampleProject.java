/**
 * Copyright © 2018-2020 Jesse Gallagher
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
