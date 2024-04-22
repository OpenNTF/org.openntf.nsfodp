/**
 * Copyright © 2018-2023 Jesse Gallagher
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * JAX-B representation of a DXL exporter log report.
 * 
 * @author Jesse Gallagher
 * @since 3.3.0
 */
@XmlRootElement(name="DXLExporterLog")
public class DxlExporterLog {
	public static DxlExporterLog forXml(String xml) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(DxlExporterLog.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			try(StringReader r = new StringReader(xml)) {
				return (DxlExporterLog) unmarshaller.unmarshal(r);
			}
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	@XmlRootElement(name="error")
	public static class DXLError {
		private int id;
		private String text;
		
		@XmlAttribute
		public void setId(int id) {
			this.id = id;
		}
		public int getId() {
			return id;
		}
		
		@XmlValue
		public void setText(String text) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
	}
	
	private DXLError error;
	
	@XmlElement
	public void setError(DXLError error) {
		this.error = error;
	}
	
	public DXLError getError() {
		return error;
	}
}