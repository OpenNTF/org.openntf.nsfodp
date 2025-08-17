/*
 * Copyright (c) 2018-2025 Jesse Gallagher
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
package org.openntf.nsfodp.compiler.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.ibm.xsp.library.XspLibrary;

/**
 * This library provides a hard-coded sorting order for the stock IBM libraries. This is
 * important to ensure that overriding libraries (like extsn over core) are loaded in
 * proper order.
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class LibraryWeightComparator implements Comparator<XspLibrary> {
	public static final LibraryWeightComparator INSTANCE = new LibraryWeightComparator();
	
	private static final List<String> weightedNames = Arrays.asList(
		"com.ibm.xsp.core.library", //$NON-NLS-1$
		"com.ibm.xsp.extsn.library", //$NON-NLS-1$
		"com.ibm.xsp.domino.library", //$NON-NLS-1$
		"com.ibm.xsp.designer.library", //$NON-NLS-1$
		"com.ibm.xsp.extlib.library" //$NON-NLS-1$
	);
	
	@Override
	public int compare(XspLibrary lib1, XspLibrary lib2) {
		String libName1 = lib1.getLibraryId();
		String libName2 = lib2.getLibraryId();
		
		int loc1 = weightedNames.indexOf(libName1);
		int loc2 = weightedNames.indexOf(libName2);
		if(loc1 > -1 && loc2 == -1) {
			return -1;
		} else if(loc1 == -1 && loc2 > -1) {
			return 1;
		} else if(loc1 > -1 && loc2 > -1) {
			return Integer.compare(loc1, loc2);
		}
		
		return libName1.compareTo(libName2);
	}
	
}