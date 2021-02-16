package org.openntf.nsfodp.commons.odp.notesapi;

import java.text.MessageFormat;
import java.util.concurrent.ThreadFactory;

import org.openntf.nsfodp.commons.odp.util.ODPUtil;

/**
 * This service interface represents an abstracted version of the Notes/Domino API calls required
 * by NSF ODP. Clients should find an implementation using the {@code org.openntf.nsfodp.commons.odp.notesapi.NotesAPI}
 * extension point.
 * 
 * @author Jesse Gallagher
 * @since 3.5.0
 */
public interface NotesAPI extends AutoCloseable {
	
	static NotesAPI get() {
		return ODPUtil.findServices(NotesAPI.class)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to find implementation for {0}", NotesAPI.class.getName())));
	}
	
	void NotesInitExtended(String... argv);
	
	ThreadFactory createThreadFactory();
	
	NDatabase createDatabase(String filePath);
	NDatabase openDatabase(String apiPath);
	NDatabase openDatabase(String server, String filePath);
	void deleteDatabase(String filePath);
	
	/**
	 * Creates a new DXL importer object. This object is expected to have the following properties
	 * configured:
	 * 
	 * <ul>
	 *   <li>DesignImportOption = REPLACE_ELSE_CREATE</li>
	 *   <li>ACLImportOption = REPLACE_ELSE_IGNORE</li>
	 *   <li>ReplaceDBProperties = true</li>
	 *   <li>ReplicaRequiredForReplaceOrUpdate = false</li>
	 * <ul>
	 * 
	 * @return a newly-created and -configured importer object
	 */
	NDXLImporter createDXLImporter();
	/**
	 * Creates a new DXL exporter object. This object is expected to have the following properties
	 * configured:
	 * 
	 * <ul>
	 *   <li>Charset = UTF-8</li>
	 *   <li>OutputDoctype = false</li>
	 * <ul>
	 * 
	 * @return a newly-created and -configured exporter object
	 */
	NDXLExporter createDXLExporter();
	
	@Override void close();
}
