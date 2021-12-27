/**
 * Copyright © 2019-2020 Jesse Gallagher
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
package org.openntf.nsfdesign.fs.util;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;

//import com.ibm.domino.napi.c.NotesUtil;
//import com.ibm.domino.napi.c.xsp.XSPNative;
//
//import lotus.domino.Session;

/**
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class SudoUtils {

//	/**
//	 * @param userName
//	 *            the user name to create the session as
//	 * @param handleTracker
//	 *            a collection to hold the allocated Domino handle, for later
//	 *            discarding
//	 * @return the created session
//	 * @author Tim Tripcony
//	 */
//	public static Session getSessionAs(final String userName, final Collection<Long> handleTracker) {
//		Session result = null;
//		try {
//			result = AccessController.doPrivileged((PrivilegedExceptionAction<Session>) () -> {
//				long hList = NotesUtil.createUserNameList(userName);
//				if (handleTracker != null) {
//					handleTracker.add(hList);
//				}
//				return XSPNative.createXPageSession(userName, hList, true, false);
//			});
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return result;
//	}
}

