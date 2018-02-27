/*
 * © Copyright IBM Corp. 2010
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package com.ibm.xsp.extlib.library;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.xsp.library.AbstractXspLibrary;


/**
 * Bazaar XPages Library
 */
public class BazaarLibrary extends AbstractXspLibrary {

	private List<BazaarFragment> fragments;

	public BazaarLibrary() {
	}

	@Override
	public String getLibraryId() {
        return "com.ibm.xsp.extlibx.bazaar.library"; // $NON-NLS-1$
    }

    public boolean isDefault() {
		return true;
	}

    @Override
	public String getPluginId() {
        return "com.ibm.xsp.extlibx.bazaar"; // $NON-NLS-1$
    }
    
    @Override
	public String[] getDependencies() {
        return new String[] {
            "com.ibm.xsp.core.library",     // $NON-NLS-1$
            "com.ibm.xsp.extsn.library",    // $NON-NLS-1$
            "com.ibm.xsp.domino.library",   // $NON-NLS-1$
            "com.ibm.xsp.extlib.library",   // $NON-NLS-1$
            //"com.ibm.xsp.extlibx.library",  // $NON-NLS-1$
        };
    }
    
    @Override
	public String[] getXspConfigFiles() {
        String[] files = new String[] {
                "com/ibm/xsp/extlib/config/extlib-bazaar.xsp-config", // $NON-NLS-1$
            };
        List<BazaarFragment> fragments = getBazaarFragments();
        for( BazaarFragment fragment: fragments) {
        	files = fragment.getXspConfigFiles(files);
        }
        return files;
    }
    
    @Override
	public String[] getFacesConfigFiles() {
        String[] files = new String[] {
                "com/ibm/xsp/extlib/config/extlib-bazaar-faces-config.xml", // $NON-NLS-1$
            };
        List<BazaarFragment> fragments = getBazaarFragments();
        for( BazaarFragment fragment: fragments) {
        	files = fragment.getFacesConfigFiles(files);
        }
        return files;
    }

    private List<BazaarFragment> getBazaarFragments() {
    	if(fragments==null) {
            List<BazaarFragment> frags = ExtensionManager.findServices(null,
                    BazaarFragment.class.getClassLoader(),
                    BazaarFragment.EXTENSION_NAME, 
                    BazaarFragment.class);
            // note, sorting the fragments alphabetically by className 
            // so the fragment ordering is deterministic - the 
            // default random ordering was making JUnit test fail 
            // orderings un-repeatable.
            Collections.sort(frags, new Comparator<BazaarFragment>() {
                @Override
				public int compare(BazaarFragment o1, BazaarFragment o2) {
                    String className1 = null == o1? "null":o1.getClass().getName(); //$NON-NLS-1$
                    String className2 = null == o2? "null":o2.getClass().getName(); //$NON-NLS-1$
                    return className1.compareTo(className2);
                }
            });
            fragments = frags;
    	}
		return fragments;
	}
}
