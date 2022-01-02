package org.openntf.nsfodp.commons.odp.designfs;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openntf.nsfodp.commons.NSFODPUtil;
import org.openntf.nsfodp.commons.NoteType;
import org.openntf.nsfodp.commons.h.NsfNote;
import org.openntf.nsfodp.commons.h.StdNames;
import org.openntf.nsfodp.commons.odp.designfs.util.PathUtil;
import org.openntf.nsfodp.commons.odp.notesapi.NViewEntry;

import com.ibm.commons.util.StringUtil;

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
		Jars(Code, NsfNote.NOTE_CLASS_FORM, StdNames.DFLAGPAT_JARFILE, NoteType.Jar),
		ScriptLibraries(
			Code, NsfNote.NOTE_CLASS_FILTER, StdNames.DFLAGPAT_SCRIPTLIB,
			entry -> {
				// We also have to check FlagsExt
				String flagsExt = StringUtil.toString(entry.getColumnValues()[17]);
				return flagsExt.indexOf(StdNames.DESIGN_FLAGEXT_WEBSERVICELIB) == -1;
			},
			NoteType.JavaLibrary, NoteType.JavaScriptLibrary,
			NoteType.LotusScriptLibrary, NoteType.ServerJavaScriptLibrary
		),
		WebServiceConsumer(Code, NsfNote.NOTE_CLASS_FILTER, StdNames.DFLAGPAT_SCRIPTLIB,
			entry -> {
				// We also have to check FlagsExt
				String flagsExt = StringUtil.toString(entry.getColumnValues()[17]);
				return flagsExt.indexOf(StdNames.DESIGN_FLAGEXT_WEBSERVICELIB) > -1;
			}, NoteType.JavaWebServiceConsumer, NoteType.LotusScriptWebServiceConsumer),
		WebServices(
			Code, NsfNote.NOTE_CLASS_FILTER, StdNames.DFLAGPAT_WEBSERVICE,
			NoteType.JavaWebService, NoteType.LotusScriptWebService
		),
		actions(Code, NsfNote.NOTE_CLASS_FILTER, StdNames.DFLAGPAT_SACTIONS_DESIGN, NoteType.SharedActions),
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
	private final Predicate<NViewEntry> predicate;
	private final NoteType[] noteTypes;
	
	private FSDirectory() {
		this(null, 0, (String)null, new NoteType[0]);
	}
	private FSDirectory(FSDirectory parent) {
		this(parent, 0, (String)null, new NoteType[0]);
	}
	private FSDirectory(FSDirectory parent, int noteClass, String pattern, NoteType... noteTypes) {
		this.parent = parent;
		this.noteClass = noteClass;
		this.pattern = pattern;
		this.predicate = null;
		this.noteTypes = noteTypes;
	}
	private FSDirectory(FSDirectory parent, int noteClass, String pattern, Predicate<NViewEntry> predicate, NoteType... noteTypes) {
		this.parent = parent;
		this.noteClass = noteClass;
		this.pattern = pattern;
		this.predicate = predicate;
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
	
	public Predicate<NViewEntry> getPredicate() {
		return predicate;
	}
	
	public NoteType[] getNoteTypes() {
		return Arrays.copyOf(noteTypes, noteTypes.length);
	}
	
	public Stream<FSDirectory> getChildren() {
		return Stream.of(values())
			.filter(dir -> dir.getParent() == this);
	}
}
