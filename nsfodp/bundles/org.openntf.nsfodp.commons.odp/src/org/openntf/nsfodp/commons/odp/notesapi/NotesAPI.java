/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.nsfodp.commons.odp.notesapi;

import java.text.MessageFormat;
import java.util.concurrent.ThreadFactory;

import org.openntf.nsfodp.commons.odp.util.ODPUtil;

/**
 * This service interface represents an abstracted version of the Notes/Domino API calls required
 * by NSF ODP.
 * 
 * @author Jesse Gallagher
 * @since 3.5.0
 */
public interface NotesAPI extends AutoCloseable {
	
	static NotesAPI get() {
		return ODPUtil.findServices(NotesAPIFactory.class)
			.findFirst()
			.map(NotesAPIFactory::createAPI)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to find implementation for {0}", NotesAPI.class.getName())));
	}
	
	static NotesAPI get(String effectiveUserName, boolean internetSession, boolean fullAccess) {
		return ODPUtil.findServices(NotesAPIFactory.class)
			.findFirst()
			.map(fac -> fac.createAPI(effectiveUserName, internetSession, fullAccess))
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
	
	/**
	 * Converts the provided Notes-format name into just its CN component.
	 * 
	 * @param name the name to convert
	 * @return the CN component of the name
	 * @since 4.0.0
	 */
	String toCn(String name);
	
	/**
	 * Converts the provided Notes-format name to its DN form.
	 * 
	 * @param name the name to convert
	 * @return the DN form of the name
	 * @since 4.0.0
	 */
	String toDn(String name);
	
	/**
	 * Retrieves the effective user name of the API session.
	 * 
	 * @return the name the session is running as
	 * @since 4.0.0
	 */
	String getEffectiveUserName();
	
	@Override void close();
}
