package org.openntf.maven.nsfodp.test.fs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openntf.nsfodp.commons.odp.designfs.FSDirectory.Agents;
import static org.openntf.nsfodp.commons.odp.designfs.FSDirectory.AppProperties;
import static org.openntf.nsfodp.commons.odp.designfs.FSDirectory.Code;
import static org.openntf.nsfodp.commons.odp.designfs.FSDirectory.design;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openntf.nsfodp.commons.odp.designfs.FSDirectory;

public class TestFSDirectory {
	public static class LineageProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(
				Arguments.of(design, null),
				Arguments.of(AppProperties, design),
				Arguments.of(Code, design),
				Arguments.of(Agents, Code)
			);
		}
	}
	
	public static class PathProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(
				Arguments.of("/design", design), //$NON-NLS-1$
				Arguments.of("/design/AppProperties", AppProperties), //$NON-NLS-1$
				Arguments.of("/design/Code", Code), //$NON-NLS-1$
				Arguments.of("/design/Code/Agents", Agents), //$NON-NLS-1$
				Arguments.of("/dsfdfdf", null), //$NON-NLS-1$
				Arguments.of("/design/", design), //$NON-NLS-1$
				Arguments.of("/design/dsfdf", null), //$NON-NLS-1$
				Arguments.of("/design/Code/Agents/dfsdf", null) //$NON-NLS-1$
			);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(LineageProvider.class)
	public void testBasic(FSDirectory base, FSDirectory parent) {
		assertEquals(parent, base.getParent());
		
		if(parent != null) {
			assertTrue(parent.getChildren().anyMatch(dir -> dir == base));
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(PathProvider.class)
	public void testFindByPath(String path, FSDirectory dir) {
		assertEquals(dir, FSDirectory.forPath(path));
	}
}
