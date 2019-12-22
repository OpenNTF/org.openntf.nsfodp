package org.openntf.nsfodp.lsp4xml.xsp;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4xml.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;

public class XspCompletionParticipant extends CompletionParticipantAdapter {
	
	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response) throws Exception {
		super.onXMLContent(request, response);
	}
	
	@Override
	public void onTagOpen(ICompletionRequest completionRequest, ICompletionResponse completionResponse) throws Exception {
	}

	@Override
	public void onAttributeName(boolean generateValue, ICompletionRequest request, ICompletionResponse response) throws Exception {
		
	}

	@Override
	public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response) throws Exception {
		// TODO figure out quoting
//		response.addCompletionAttribute(new CompletionItem("hello"));
		response.addCompletionItem(new CompletionItem("there"));
	}

}
