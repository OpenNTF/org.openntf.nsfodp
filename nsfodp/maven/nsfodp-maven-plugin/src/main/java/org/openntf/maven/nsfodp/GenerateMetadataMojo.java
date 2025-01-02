/*
 * Copyright © 2018-2025 Jesse Gallagher
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
package org.openntf.maven.nsfodp;

import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGS;
import static org.openntf.nsfodp.commons.h.StdNames.DESIGN_FLAGS_EXTENDED;
import static org.openntf.nsfodp.commons.h.StdNames.FIELD_TITLE;
import static org.openntf.nsfodp.commons.h.StdNames.FILTER_COMMENT_ITEM;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_FILE_MIMECHARSET;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_FILE_MIMETYPE;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_FILE_MODINFO;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_FILE_NAMES;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_IMAGES_COLORIZE;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_IMAGES_HIGH;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_IMAGES_WIDE;
import static org.openntf.nsfodp.commons.h.StdNames.ITEM_NAME_IMAGE_NAMES;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.MessageFormat;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.openntf.nsfodp.commons.NoteType;
import org.openntf.nsfodp.commons.dxl.DXLUtil;
import org.openntf.nsfodp.commons.io.SwiperOutputStream;
import org.openntf.nsfodp.commons.xml.NSFODPDomUtil;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.commons.util.StringUtil;

/**
 * Generates ".metadata" companion DXL files for raw-data type that should have them but do
 * not in the ODP.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
@Mojo(name="generate-metadata")
public class GenerateMetadataMojo extends AbstractMojo {
	@Parameter(defaultValue="${project}", readonly=true, required=false)
	protected MavenProject project;
	
	/**
	 * Location of the ODP directory.
	 */
	@Parameter(defaultValue="odp", required=true)
	private File odpDirectory;
	
	@Component
	private BuildContext buildContext;
	
	Log log;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();
		if(!project.getPackaging().equals("domino-nsf")) { //$NON-NLS-1$
			if(log.isInfoEnabled()) {
				log.info(Messages.getString("GeneratePDEStructureMojo.skip")); //$NON-NLS-1$
			}
			return;
		}
		
		try {
			generateMetadata();
		} catch(IOException e) {
			throw new MojoExecutionException("Exception while generating build.properties", e);
		}
	}
	
	private void generateMetadata() throws IOException {
		Path odpDir = odpDirectory.toPath();
		
		for(NoteType type : NoteType.values()) {
			if(type.getOutputFormat() == NoteType.OutputFormat.METADATA) {
				Path noteDir = odpDir.resolve(type.getPath(odpDir.getFileSystem()));
				if(Files.isDirectory(noteDir)) {
					Files.walk(noteDir, Integer.MAX_VALUE)
						.filter(Files::isRegularFile)
						.filter(this::notKnownBad)
						.filter(p -> !p.getFileName().toString().endsWith(".metadata")) //$NON-NLS-1$
						.filter(p -> matchesExtension(p, type))
						.filter(this::needsMetadata)
						.forEach(p -> createMetadataFile(noteDir, p, type));
				}
			}
		}
	}
	
	private boolean notKnownBad(Path file) {
		String name = file.getFileName().toString();
		if(".DS_Store".equals(name)) { //$NON-NLS-1$
			return false;
		} else if("Thumbs.db".equals(name)) { //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	private boolean needsMetadata(Path file) {
		Path metaFile = file.getParent().resolve(file.getFileName().toString() + ".metadata"); //$NON-NLS-1$
		return !Files.exists(metaFile);
	}
	private boolean matchesExtension(Path file, NoteType type) {
		String ext = type.getExtension();
		if(StringUtil.isEmpty(ext)) {
			return true;
		} else {
			return file.getFileName().toString().toLowerCase().endsWith(ext.toLowerCase());
		}
	}
	
	private void createMetadataFile(Path odpDir, Path file, NoteType type) {
		if(log.isInfoEnabled()) {
			log.info(MessageFormat.format("Creating metadata for {0} {1}", type, odpDir.relativize(file)));
		}

		try {
			Document dxl = createBaseDxl();
			Element note = dxl.getDocumentElement();

			switch(type) {
			case WiringProperties: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "34567C:Q"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				break;
			}
			case CompositeApplication: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "34567C|Q"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, FILTER_COMMENT_ITEM, true, ""); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS_EXTENDED, true, "1"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, ""); //$NON-NLS-1$
				break;
			}
			case CompositeComponent: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "345CgQ_"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				break;
			}
			case XPage: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "gC~4K"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				break;
			}
			case XPageProperties: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "gC~4K2"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				break;
			}
			case CustomControl: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "gC~4;");
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				break;
			}
			case CustomControlProperties: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "gC~4;2"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				break;
			}
			case FileResource: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "345CgQ"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS_EXTENDED, true, "D"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				String mimeType = Files.probeContentType(file);
				if(StringUtil.isEmpty(mimeType)) {
					mimeType = new MimetypesFileTypeMap().getContentType(file.toFile());
				}
				if(StringUtil.isEmpty(mimeType)) {
					mimeType = "application/octet-stream"; //$NON-NLS-1$
				}
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_MIMETYPE, true, mimeType);
				
				FileTime mod = Files.getLastModifiedTime(file);
				DXLUtil.writeItemDateTime(dxl, ITEM_NAME_FILE_MODINFO, true, mod.toInstant());
				break;
			}
			case ImageResource: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "34CiQ"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS_EXTENDED, true, "D"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_MIMETYPE, true, Files.probeContentType(file));
				
				FileTime mod = Files.getLastModifiedTime(file);
				DXLUtil.writeItemDateTime(dxl, ITEM_NAME_FILE_MODINFO, true, mod.toInstant());
				
				DXLUtil.writeItemNumber(dxl, ITEM_NAME_IMAGES_WIDE, 1);
				DXLUtil.writeItemNumber(dxl, ITEM_NAME_IMAGES_HIGH, 1);
				DXLUtil.writeItemNumber(dxl, ITEM_NAME_IMAGES_COLORIZE, 0);
				DXLUtil.writeItemString(dxl, ITEM_NAME_IMAGE_NAMES, true, file.getFileName().toString());
				
				break;
			}
			case Jar: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "34567Cg~,"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				break;
			}
			case Java: {
				Path relativePath = odpDir.relativize(file);
				
				String separator = odpDir.getFileSystem().getSeparator();
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, relativePath.toString().replace(separator, "/")); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "34567Cg~["); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, relativePath.toString().replace(separator, "/")); //$NON-NLS-1$
				break;
			}
			case JavaScriptLibrary: {
				note.setAttribute("class", "filter"); //$NON-NLS-1$ //$NON-NLS-2$
				String name = file.getFileName().toString();
				if(name.endsWith('.' + type.getExtension())) {
					name = name.substring(0, name.length()-1-type.getExtension().length());
				}
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, name);
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "h534Q"); //$NON-NLS-1$
				break;
			}
			case LotusScriptLibrary: {
				note.setAttribute("class", "filter"); //$NON-NLS-1$ //$NON-NLS-2$
				String name = file.getFileName().toString();
				if(name.endsWith('.' + type.getExtension())) {
					name = name.substring(0, name.length()-1-type.getExtension().length());
				}
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, name);
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "s34Q"); //$NON-NLS-1$
				break;
			}
			case ServerJavaScriptLibrary: {
				note.setAttribute("class", "filter"); //$NON-NLS-1$ //$NON-NLS-2$
				String name = file.getFileName().toString();
				if(name.endsWith('.' + type.getExtension())) {
					name = name.substring(0, name.length()-1-type.getExtension().length());
				}
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, name);
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, ".5834Q"); //$NON-NLS-1$
				break;
			}
			case StyleSheet: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "34C=Q"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_MIMETYPE, true, "text/css"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_MIMECHARSET, true, "UTF-8"); //$NON-NLS-1$
				break;
			}
			case Theme: {
				note.setAttribute("class", "form"); //$NON-NLS-1$ //$NON-NLS-2$
				DXLUtil.writeItemString(dxl, FIELD_TITLE, true, file.getFileName().toString());
				DXLUtil.writeItemString(dxl, DESIGN_FLAGS, true, "34567Cg~`"); //$NON-NLS-1$
				DXLUtil.writeItemString(dxl, ITEM_NAME_FILE_NAMES, true, file.getFileName().toString());
				break;
			}
			default:
				if(log.isWarnEnabled()) {
					log.warn(MessageFormat.format("Encountered unhandled file of type {0}: {1}", type, file));
				}
				return;
			}
			
			Path metaFile = file.getParent().resolve(file.getFileName().toString() + ".metadata"); //$NON-NLS-1$
			Transformer transformer = SwiperOutputStream.createTransformer();
			
			DOMSource source = new DOMSource(dxl);
			DOMResult result = new DOMResult();
			transformer.transform(source, result);
			
			try(OutputStream os = buildContext.newFileOutputStream(metaFile.toFile())) {
				NSFODPDomUtil.serialize((OutputStream) os, (Node) result.getNode(), null);
			}
		} catch(TransformerException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Document createBaseDxl() {
		Document doc = NSFODPDomUtil.createDocument();
		Element note = NSFODPDomUtil.createElement((Document) doc, (String) "note"); //$NON-NLS-1$
		note.setAttribute("xmlns", "http://www.lotus.com/dxl"); //$NON-NLS-1$ //$NON-NLS-2$
		return doc;
	}
	
}
