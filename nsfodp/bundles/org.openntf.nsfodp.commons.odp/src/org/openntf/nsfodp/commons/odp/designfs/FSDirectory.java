package org.openntf.nsfodp.commons.odp.designfs;

import java.nio.file.Path;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.openntf.nsfodp.commons.odp.designfs.util.PathUtil;

/**
 * Defines folder names used for the Design FileSystem implementation.
 * 
 * @author Jesse Gallagher
 * @since 4.0.0
 */
public enum FSDirectory {
	design,
	
	AppProperties(design),
	Code(design),
		Agents(Code),
		Jars(Code),
		ScriptLibraries(Code),
		WebServiceConsumer(Code),
		WebServices(Code),
		actions(Code),
	CompositeApplications(design),
		Applications(CompositeApplications),
		Components(CompositeApplications),
		WiringProperties(CompositeApplications),
	CustomControls(design),
	Data(design),
		DB2AccessViews(Data),
		DataConnections(Data),
	Folders(design),
	Forms(design),
	Framesets(design),
	Pages(design),
	Resources(design),
		Applets(Resources),
		Files(Resources),
		Images(Resources),
		StyleSheets(Resources),
		Themes(Resources),
	SharedElements(design),
		Columns(SharedElements),
		Fields(SharedElements),
		Navigators(SharedElements),
		Outlines(SharedElements),
		Subforms(SharedElements),
	Views(design),
	WebContent(design),
	XPages(design);
	
	public static FSDirectory forPath(String path) {
		if(path == null || path.isEmpty() || "/".equals(path)) { //$NON-NLS-1$
			return null;
		}
		FSDirectory parent = null;
		// Split the path after the initial "/"
		StringTokenizer tokens = new StringTokenizer(path.substring(1), "/"); //$NON-NLS-1$
		while(tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			
			try {
				FSDirectory val = valueOf(token);
				if(val.getParent() == parent && !tokens.hasMoreTokens()) {
					return val;
				} else {
					parent = val;
				}
			} catch(IllegalArgumentException e) {
				return null;
			}
		}
		
		return null;
	}
	public static FSDirectory forPath(Path path) {
		return forPath(PathUtil.toPathString(path));
	}
	
	private final FSDirectory parent;
	
	private FSDirectory() {
		this.parent = null;
	}
	private FSDirectory(FSDirectory parent) {
		this.parent = parent;
	}
	
	public FSDirectory getParent() {
		return parent;
	}
	
	public Stream<FSDirectory> getChildren() {
		return Stream.of(values())
			.filter(dir -> dir.getParent() == this);
	}
}
