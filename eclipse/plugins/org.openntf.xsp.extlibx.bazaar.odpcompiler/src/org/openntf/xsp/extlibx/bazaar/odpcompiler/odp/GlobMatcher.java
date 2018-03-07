package org.openntf.xsp.extlibx.bazaar.odpcompiler.odp;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Function;

/**
 * Represents a pairing of a design-element-matching glob to a provider
 * that creates a new design element object for each matched path.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
class GlobMatcher {
	/** Platform-specific PathMatcher separator, escaped in the case of Windows */
	public static final String MATCH_SEP = File.separatorChar == '\\' ? "\\\\" : File.separator;
	
	private final PathMatcher matcher;
	private final Function<Path, ? extends AbstractSplitDesignElement> elementProvider;

	/**
	 * @param glob a filesystem glob in Unix format, e.g. "Resources/Images/*" or "WebContent/**"
	 * @param elementProvider a function that provides an element object for a given path
	 */
	public GlobMatcher(String glob, Function<Path, ? extends AbstractSplitDesignElement> elementProvider) {
		this.matcher = glob(glob);
		this.elementProvider = elementProvider;
	}

	public PathMatcher getMatcher() {
		return matcher;
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractSplitDesignElement> T getElement(Path path) {
		return (T)elementProvider.apply(path);
	}
	
	public static final PathMatcher glob(String unixGlob) {
		return FileSystems.getDefault().getPathMatcher("glob:" + unixGlob.replace("/", MATCH_SEP));
	}
}
