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