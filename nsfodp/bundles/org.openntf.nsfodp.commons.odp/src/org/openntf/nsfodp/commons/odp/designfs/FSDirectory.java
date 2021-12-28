package org.openntf.nsfodp.commons.odp.designfs;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.openntf.nsfodp.commons.NoteType;
import org.openntf.nsfodp.commons.h.NsfNote;
import org.openntf.nsfodp.commons.h.StdNames;
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
		Agents(
			Code, NsfNote.NOTE_CLASS_FILTER, StdNames.DFLAGPAT_AGENTSLIST,
			NoteType.FormulaAgent, NoteType.ImportedJavaAgent, NoteType.JavaAgent,
			NoteType.LotusScriptAgent, NoteType.SimpleActionAgent
		),
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
	private final int noteClass;
	private final String pattern;
	private final NoteType[] noteTypes;
	
	private FSDirectory() {
		this(null, 0, null, new NoteType[0]);
	}
	private FSDirectory(FSDirectory parent) {
		this(parent, 0, null, new NoteType[0]);
	}
	private FSDirectory(FSDirectory parent, int noteClass, String pattern, NoteType... noteTypes) {
		this.parent = parent;
		this.noteClass = noteClass;
		this.pattern = pattern;
		this.noteTypes = noteTypes;
	}
	
	public FSDirectory getParent() {
		return parent;
	}
	
	public int getNoteClass() {
		return noteClass;
	}
	
	public String getPattern() {
		return pattern;
	}
	
	public NoteType[] getNoteTypes() {
		return Arrays.copyOf(noteTypes, noteTypes.length);
	}
	
	public Stream<FSDirectory> getChildren() {
		return Stream.of(values())
			.filter(dir -> dir.getParent() == this);
	}
}
