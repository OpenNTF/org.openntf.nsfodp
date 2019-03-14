package org.openntf.nsfodp.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This OutputStream implementation toggles its behavior depending on whether or not Swiper is
 * enabled for this exporter.
 * 
 * @since 1.4.0
 */
public class SwiperOutputStream extends OutputStream {
	
	static {
		try(InputStream is = SwiperOutputStream.class.getResourceAsStream("/res/SwiperDXLClean.xsl")) { //$NON-NLS-1$
			swiper = TransformerFactory.newInstance().newTemplates(new StreamSource(is));
		} catch (TransformerConfigurationException | TransformerFactoryConfigurationError | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final Templates swiper;
	
	private final Path path;
	private OutputStream os;
	private final boolean isSwiper;
	
	public SwiperOutputStream(Path path, boolean isSwiper) throws IOException {
		this.path = path;
		this.isSwiper = isSwiper;
		if(this.isSwiper) {
			os = new ByteArrayOutputStream();
		} else {
			os = Files.newOutputStream(path);
		}
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		
		// Either close the underlying stream and be done or do Swiper transformations
		if(this.isSwiper) {
			os.close();
			byte[] xml = ((ByteArrayOutputStream)os).toByteArray();
			try(InputStream is = new ByteArrayInputStream(xml)) {
				try {
					Transformer transformer = createTransformer();
					
					try(OutputStream os = Files.newOutputStream(path)) {
						StreamResult result = new StreamResult(os);
						transformer.transform(new StreamSource(is), result);
					}
				} catch (TransformerException e) {
					throw new IOException(e);
				}
			}
		} else {
			os.close();
		}
	}
	
	public static Transformer createTransformer() throws TransformerConfigurationException {
		Transformer transformer = swiper.newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$

		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes"); //$NON-NLS-1$
		
		return transformer;
	}
}