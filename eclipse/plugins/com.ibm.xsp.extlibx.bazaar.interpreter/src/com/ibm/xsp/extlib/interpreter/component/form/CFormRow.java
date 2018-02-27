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
 * Field used by a form.
 * 
 * @author priand
 */
public class CFormRow extends CObject {
	
	public static class Choice {
		private String value;
		private String label;
		public Choice() {
		}
		public Choice(String value, String label) {
			this.value = value;
			this.label = label;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
	}
	
	
	public static final String TYPE_TEXT			= "Text";
	public static final String TYPE_MULTILINETEXT	= "MultilineText";
	public static final String TYPE_RICHTEXT		= "RichText";
	public static final String TYPE_NUMBER			= "Number";
	public static final String TYPE_BOOLEAN			= "Boolean";
	public static final String TYPE_RADIOBUTTONS	= "RadioButtons";
	public static final String TYPE_DROPDOWN		= "DropDown";
	public static final String TYPE_PICKER			= "Picker";
	public static final String TYPE_PERSON			= "Person";
	public static final String TYPE_DATE			= "Date";
	public static final String TYPE_TIME			= "Time";
	public static final String TYPE_DATETIME		= "DateTime";
	public static final String TYPE_URL				= "Url";
	
	private String type;
	private String field;
	private String label;
	
	// View
	private boolean view;
	private boolean sorted;
	
	// Multiple values
	private boolean multiple;
	
	// Validation
	private boolean required;

	// List of choices
	private List<Choice> choices;
	
	// Text Area
	private int rows;
	private int cols;
	
	public CFormRow() {
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isView() {
		return view;
	}
	public void setView(boolean view) {
		this.view = view;
	}

	public boolean isSorted() {
		return sorted;
	}
	public void setSorted(boolean sorted) {
		this.sorted = sorted;
	}

	public boolean isMultiple() {
		return multiple;
	}
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}

	public List<Choice> getChoices() {
		return choices;
	}
	public void addChoice(Choice choice) {
		if(choices==null) {
			choices = new ArrayList<Choice>();
		}
		this.choices.add(choice);
	}

	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getCols() {
		return cols;
	}
	public void setCols(int cols) {
		this.cols = cols;
	}
}
