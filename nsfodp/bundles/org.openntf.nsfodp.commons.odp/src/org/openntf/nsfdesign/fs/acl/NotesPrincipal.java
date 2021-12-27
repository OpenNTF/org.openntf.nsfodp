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
package org.openntf.nsfdesign.fs.acl;

import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collections;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.openntf.nsfdesign.fs.db.NSFAccessor;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NotesPrincipal implements UserPrincipal, GroupPrincipal {
	
	private final LdapName ldapName;
	
	public NotesPrincipal(String dominoName) {
		try {
			this.ldapName = new LdapName(NSFAccessor.dominoNameToLdap(dominoName));
		} catch (InvalidNameException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		for(String part : Collections.list(ldapName.getAll())) {
			if(part.startsWith("cn=")) { //$NON-NLS-1$
				return part.substring(3).replace(' ', '+');
			}
		}
		return ldapName.toString();
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
