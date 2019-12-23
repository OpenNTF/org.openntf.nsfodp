package org.openntf.nsfodp.lsp4xml.xsp;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4xml.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.openntf.nsfodp.lsp4xml.xsp.model.AbstractComponent;
import org.openntf.nsfodp.lsp4xml.xsp.model.ComponentProperty;

/**
 * XML Completion participant to provide XSP components and attributes.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class XspCompletionParticipant extends CompletionParticipantAdapter {
	private static final Collection<String> simpleTypes = new TreeSet<>(Arrays.asList(
		"boolean", //$NON-NLS-1$
		"javax.faces.component.UIComponent", //$NON-NLS-1$
		"int", //$NON-NLS-1$
		"double" //$NON-NLS-1$
	));
	
	@Override
	public void onTagOpen(ICompletionRequest request, ICompletionResponse response) throws Exception {
		if(ContentAssistUtil.isXsp(request.getXMLDocument())) {
			// Stock components
			ComponentCache.getStockComponents().stream()
				.map(AbstractComponent::getPrefixedName)
				.map(CompletionItem::new)
				.forEach(response::addCompletionItem);
			
			// Custom controls
			ComponentCache.getCustomControls(request.getXMLDocument().getDocumentURI()).stream()
				.map(AbstractComponent::getPrefixedName)
				.map(CompletionItem::new)
				.forEach(response::addCompletionItem);
			
			
			// Specialized "this.*" properties - allow for any non-simple type
			String parentTag = request.getParentElement().getTagName();
			ComponentCache.getStockComponents().stream()
				.filter(component -> component.getPrefixedName().equals(parentTag))
				.map(AbstractComponent::getProperties)
				.flatMap(Collection::stream)
				.filter(prop -> !simpleTypes.contains(prop.getJavaClassName()))
				.map(ComponentProperty::getName)
				.map(name -> request.getParentElement().getPrefix() + ":this." + name) //$NON-NLS-1$
				.map(CompletionItem::new)
				.forEach(response::addCompletionItem);
		}
	}

	@Override
	public void onAttributeName(boolean generateValue, ICompletionRequest request, ICompletionResponse response) throws Exception {
		if(ContentAssistUtil.isXsp(request.getXMLDocument())) {
			String tag = request.getCurrentTag();
			
			// Stock components
			ComponentCache.getStockComponents().stream()
				.filter(component -> component.getPrefixedName().equals(tag))
				.map(AbstractComponent::getProperties)
				.flatMap(Collection::stream)
				.map(ComponentProperty::getName)
				.map(CompletionItem::new)
				.forEach(response::addCompletionItem);

			// Custom controls
			ComponentCache.getCustomControls(request.getXMLDocument().getDocumentURI()).stream()
				.filter(component -> component.getPrefixedName().equals(tag))
				.map(AbstractComponent::getProperties)
				.flatMap(Collection::stream)
				.map(ComponentProperty::getName)
				.map(CompletionItem::new)
				.forEach(response::addCompletionItem);
		}
	}

	@Override
	public void onAttributeValue(String valuePrefix, ICompletionRequest request, final ICompletionResponse response) throws Exception {
		if(ContentAssistUtil.isXsp(request.getXMLDocument())) {
			String parentTag = request.getParentElement().getTagName();
			String attribute = request.getCurrentAttributeName();
			ComponentCache.getStockComponents().stream()
				.filter(component -> component.getPrefixedName().equals(parentTag))
				.map(AbstractComponent::getProperties)
				.flatMap(Collection::stream)
				.filter(prop -> prop.getName().equals(attribute))
				.forEach(prop -> {
					switch(prop.getJavaClassName()) {
					case "boolean": //$NON-NLS-1$
						response.addCompletionItem(new CompletionItem("\"true\"")); //$NON-NLS-1$
						response.addCompletionItem(new CompletionItem("\"false\"")); //$NON-NLS-1$
						break;
					default:
						// NOP
					}
				});
		}
	}

}
