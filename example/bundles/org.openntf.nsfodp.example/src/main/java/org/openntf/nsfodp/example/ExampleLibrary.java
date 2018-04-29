/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.nsfodp.example;

import com.ibm.xsp.library.AbstractXspLibrary;

/**
 * @since 1.0.0
 */
public class ExampleLibrary extends AbstractXspLibrary {

	@Override
	public String getLibraryId() {
	    return Activator.class.getPackage().getName() + ".library"; //$NON-NLS-1$
	}

	@Override
    public String getPluginId() {
        return Activator.getDefault().getBundle().getSymbolicName();
    }

    @Override
    public String getTagVersion() {
        return "1.0.0"; //$NON-NLS-1$
    }

    @Override
    public String[] getDependencies() {
        return new String[] {
                "com.ibm.xsp.core.library", //$NON-NLS-1$
                "com.ibm.xsp.extsn.library", //$NON-NLS-1$
                "com.ibm.xsp.domino.library", //$NON-NLS-1$
                "com.ibm.xsp.designer.library", //$NON-NLS-1$
                "com.ibm.xsp.extlib.library" //$NON-NLS-1$
        };
    }

    @Override
    public boolean isGlobalScope() {
        return false;
    }
}
