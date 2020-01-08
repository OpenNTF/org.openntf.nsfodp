package org.openntf.nsfodp.exporter.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.openntf.nsfodp.commons.io.SwiperOutputStream;

import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.Format;

/**
 * This subclass of {@link SwiperOutputStream} serializes via IBM Commons {@link DOMUtil} instead
 * of the JRE's default serializer.
 * 
 * <p>This provides for more-consistent indentation than the varying implementations that can be
 * provided normally.</p>
 * 
 * @since 3.0.0
 */
public class CommonsSwiperOutputStream extends SwiperOutputStream {
	
	public CommonsSwiperOutputStream(Path path, boolean isSwiper) throws IOException {
		super(path, isSwiper);
	}
	
	@Override
	protected void transform(Transformer transformer, InputStream is, Path destination) throws Exception {
		DOMResult result = new DOMResult();
		transformer.transform(new StreamSource(is), result);
		try(OutputStream os = Files.newOutputStream(destination)) {
			DOMUtil.serialize(os, result.getNode(), Format.defaultFormat);
		}
	}
}