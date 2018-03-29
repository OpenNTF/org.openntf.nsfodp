package org.openntf.xsp.extlibx.bazaar.odpcompiler.util;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.ibm.xsp.library.DirectoryResourceBundleSource;
import com.ibm.xsp.registry.config.ResourceBundleSource;

public class MultiPathResourceBundleSource implements ResourceBundleSource {
	private final Collection<DirectoryResourceBundleSource> sources;
	
	public MultiPathResourceBundleSource(Collection<Path> paths) {
		this.sources = Objects.requireNonNull(paths).stream()
			.map(path -> new DirectoryResourceBundleSource(path.toFile()))
			.collect(Collectors.toList());
	}

	@Override
	public ResourceBundle getBundle(String bundleName) {
		return sources.stream()
			.map(source -> source.getBundle(bundleName))
			.filter(Objects::nonNull)
			.findFirst().orElse(null);
	}
}
