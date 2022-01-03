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
package org.openntf.nsfodp.compiler.dxl;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * JAX-B representation of a DXL importer log.
 * 
 * @author Jesse Gallagher
 */
@XmlRootElement(name="DXLImporterLog")
@XmlAccessorType(XmlAccessType.FIELD)
public class DxlImporterLog {
	public static DxlImporterLog forXml(String xml) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(DxlImporterLog.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			try(StringReader r = new StringReader(xml)) {
				return (DxlImporterLog) unmarshaller.unmarshal(r);
			}
		} catch (JAXBException e) {
			throw new RuntimeException("Encountered exception parsing DXL log: " + xml, e);
		}
	}
	
	@XmlRootElement(name="error")
	public static class DXLError {
		private int id;
		private String text;
		private String source;
		private int line;
		private int column;
		
		public int getId() {
			return id;
		}
		@XmlAttribute
		public void setId(int id) {
			this.id = id;
		}
		
		@XmlValue
		public void setText(String text) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
		
		public String getSource() {
			return source;
		}
		@XmlAttribute
		public void setSource(String source) {
			this.source = source;
		}
		
		public int getLine() {
			return line;
		}
		@XmlAttribute
		public void setLine(int line) {
			this.line = line;
		}
		
		public int getColumn() {
			return column;
		}
		@XmlAttribute
		public void setColumn(int column) {
			this.column = column;
		}
		
		@Override
		public String toString() {
			return MessageFormat.format("Error: id={0}: {1}", id, text);
		}
	}
	
	@XmlRootElement(name="fatalerror")
	public static class DXLFatalError {
		private String source;
		private int line;
		private int column;
		private String text;
		
		@XmlAttribute
		public void setSource(String source) {
			this.source = source;
		}
		public String getSource() {
			return source;
		}
		@XmlAttribute
		public void setLine(int line) {
			this.line = line;
		}
		public int getLine() {
			return line;
		}
		@XmlAttribute
		public void setColumn(int column) {
			this.column = column;
		}
		public int getColumn() {
			return column;
		}
		
		@XmlValue
		public void setText(String text) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
		
		@Override
		public String toString() {
			return MessageFormat.format("Fatal Error: source={0}, line={1}, column={2}: {3}", source, line, column, text);
		}
	}

	@XmlElement(name="fatalerror")
	private List<DXLFatalError> fatalErrors;
	@XmlElement(name="error")
	private List<DXLError> errors;
	
	public void setFatalErrors(List<DXLFatalError> fatalErrors) {
		this.fatalErrors = fatalErrors;
	}
	public List<DXLFatalError> getFatalErrors() {
		return fatalErrors;
	}
	
	public void setErrors(List<DXLError> errors) {
		this.errors = errors;
	}
	public List<DXLError> getErrors() {
		return errors;
	}
}