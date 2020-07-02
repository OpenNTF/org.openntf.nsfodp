/**
 * Copyright © 2018-2020 Jesse Gallagher
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
package org.openntf.nsfodp.eclipse.ui.resources;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

public class DesignElementsLabelProvider extends WorkbenchLabelProvider implements ICommonLabelProvider {

	@Override
	public void restoreState(IMemento memento) {

	}

	@Override
	public void saveState(IMemento memento) {
	}

	@Override
	public String getDescription(Object element) {
		if (element instanceof DesignElementNode) {
			return ((DesignElementNode) element).getType().getLabel();
		}
		return null;
	}

	@Override
	public void init(ICommonContentExtensionSite config) {

	}

}
