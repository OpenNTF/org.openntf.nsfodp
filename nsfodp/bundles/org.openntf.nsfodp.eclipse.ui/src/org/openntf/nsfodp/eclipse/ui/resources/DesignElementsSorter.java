/**
 * Copyright © 2018-2021 Jesse Gallagher
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

import java.text.Collator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

@SuppressWarnings("deprecation")
public class DesignElementsSorter extends ViewerSorter {
	public DesignElementsSorter() {
	}

	public DesignElementsSorter(Collator collator) {
		super(collator);
	}
	
	@Override
	public int category(Object element) {
		if (element instanceof DesignElementNode) {
			return ((DesignElementNode)element).getType().isContainer() ? 1 : 0;
		}
		return 1;
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof DesignElementNode && e2 instanceof IResource) {
			return -1;
		} else if (e2 instanceof DesignElementNode && e1 instanceof IResource) {
			return 1;
		}
		if (e1 instanceof IFolder && e2 instanceof IFile) {
			return -1;
		} else if (e2 instanceof IFolder && e1 instanceof IFile) {
			return 1;
		}
		if(e1 instanceof DesignElementNode && e2 instanceof DesignElementNode) {
			DesignElementType t1 = ((DesignElementNode)e1).getType();
			DesignElementType t2 = ((DesignElementNode)e2).getType();
			return t1.compareTo(t2);
		}

		return super.compare(viewer, e1, e2);
	}
}
