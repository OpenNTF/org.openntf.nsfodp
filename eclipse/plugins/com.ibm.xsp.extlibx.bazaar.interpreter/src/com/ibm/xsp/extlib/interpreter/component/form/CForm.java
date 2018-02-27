/*
 * © Copyright IBM Corp. 2010
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package com.ibm.xsp.extlib.interpreter.component.form;

import java.util.ArrayList;
import java.util.List;


/**
 * XPageFragment control.
 * 
 * @author priand
 */
public class CForm extends CObject {
	
	public static final String ACTIONS_SAVE					= "Save";			// Save Cancel	
	public static final String ACTIONS_CANCEL				= "Cancel";			// Cancel
	public static final String ACTIONS_DELETE				= "Delete";		// Save Cancel Delete
	public static final String ACTIONS_SAVEASDRAFT			= "SaveAsDraft";	// SaveAsDraft Submit Cancel	
	
	private String title;
	private String description;
	private List<CFormRow> rows;
	private String actions;
	
	public CForm() {
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<CFormRow> getRows() {
		return rows;
	}
	public void setRows(List<CFormRow> rows) {
		this.rows = rows;
	}
	public void addRow(CFormRow row) {
		if(rows==null) {
			rows = new ArrayList<CFormRow>();
		}
		rows.add(row);
	}
	
	public String getActions() {
		return actions;
	}
	public void setActions(String actions) {
		this.actions = actions;
	}
}
