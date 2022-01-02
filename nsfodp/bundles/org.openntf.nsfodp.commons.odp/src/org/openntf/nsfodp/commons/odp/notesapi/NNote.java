/**
 * Copyright © 2018-2022 Jesse Gallagher
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

public interface NNote extends AutoCloseable {
	void save();
	void sign();
	
	void compileLotusScript() throws NLotusScriptCompilationException;
	
	boolean hasItem(String itemName);
	
	<T> T get(String itemName, Class<T> valueClass);
	String getAsString(String itemName, char delimiter);
	NCompositeData getCompositeData(String itemName);
	void set(String itemName, Object value);
	
	Iterable<String> getItemNames();
	
	short getNoteClassValue();
	
	int getNoteID();
	
	boolean isRefValid();
	
	@Override void close();
}
