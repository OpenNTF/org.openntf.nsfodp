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
package org.openntf.nsfodp.lsp4xml.xsp.completion;

import java.util.Collection;

import org.eclipse.lemminx.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItem;
import org.openntf.nsfodp.lsp4xml.xsp.completion.model.AbstractComponent;
import org.openntf.nsfodp.lsp4xml.xsp.completion.model.ComponentProperty;

/**
 * XML Completion participant to provide custom controls and attributes.
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public class XspCompletionParticipant extends CompletionParticipantAdapter {
	public static final String NS_XC = "http://www.ibm.com/xsp/custom"; //$NON-NLS-1$
	
	@Override
	public void onTagOpen(ICompletionRequest request, ICompletionResponse response) throws Exception {
		if(ContentAssistUtil.isXsp(request.getXMLDocument())) {
			// Custom controls
			ComponentCache.getCustomControls(request.getXMLDocument().getDocumentURI()).stream()
				.map(AbstractComponent::getPrefixedName)
				.filter(name -> name != null && !name.isEmpty())
				.map(CompletionItem::new)
				.forEach(response::addCompletionItem);
			
			
			if(request.getParentElement() != null) {
				String parentTag = request.getParentElement().getTagName();
				ComponentCache.getCustomControls(request.getXMLDocument().getDocumentURI()).stream()
					.filter(component -> component.getPrefixedName().equals(parentTag))
					.map(AbstractComponent::getProperties)
					.flatMap(Collection::stream)
					.map(ComponentProperty::getName)
					.filter(name -> name != null && !name.isEmpty())
					.map(name -> request.getParentElement().getPrefix() + ":this." + name) //$NON-NLS-1$
					.map(CompletionItem::new)
					.forEach(response::addCompletionItem);
			}
		}
	}

	@Override
	public void onAttributeName(boolean generateValue, ICompletionRequest request, ICompletionResponse response) throws Exception {
		if(ContentAssistUtil.isXsp(request.getXMLDocument())) {
			String tag = request.getCurrentTag();

			// Custom controls
			ComponentCache.getCustomControls(request.getXMLDocument().getDocumentURI()).stream()
				.filter(component -> component.getPrefixedName().equals(tag))
				.map(AbstractComponent::getProperties)
				.flatMap(Collection::stream)
				.map(ComponentProperty::getName)
				.filter(name -> name != null && !name.isEmpty())
				.map(CompletionItem::new)
				.forEach(response::addCompletionItem);
		}
	}

	@Override
	public void onAttributeValue(String valuePrefix, ICompletionRequest request, final ICompletionResponse response) throws Exception {
		if(ContentAssistUtil.isXsp(request.getXMLDocument())) {
			String parentTag = request.getParentElement().getTagName();
			String attribute = request.getCurrentAttributeName();
			ComponentCache.getCustomControls(request.getXMLDocument().getDocumentURI()).stream()
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
