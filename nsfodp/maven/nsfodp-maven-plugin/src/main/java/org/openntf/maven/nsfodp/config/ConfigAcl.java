/**
 * Copyright © 2018-2019 Jesse Gallagher
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
package org.openntf.maven.nsfodp.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an ODP ACL definition.
 * 
 * @author Jesse Gallagher
 * @since 3.1.0
 */
@XmlRootElement(name="acl", namespace="http://www.lotus.com/dxl")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigAcl {
	@XmlEnum
	public enum AccessLevel {
		noaccess,
		depositor,
		reader,
		author,
		editor,
		designer,
		manager
	}
	@XmlEnum
	public enum NamesFieldsOption {
		none,
		namesfields,
		authorreaderfields
	}
	
	/**
	 * Maximum Internet name and password
	 */
	@XmlAttribute(name="maxinternetaccess")
	private AccessLevel maxInternetAccess;
	/**
	 * Enforce a consistent Access Control List across all replicas
	 */
	@XmlAttribute(name="consistentacl")
	private boolean enforceConsistentAcl;
	/**
	 * 	Administration server
	 */
	@XmlAttribute(name="adminserver")
	private String adminServer;
	/**
	 * Administration server action
	 */
	@XmlAttribute(name="adminservermaymodify")
	private NamesFieldsOption adminServerAction;
	
	@XmlElement(name="role", namespace="http://www.lotus.com/dxl")
	private String[] roles;

	@XmlElement(name="aclentry", namespace="http://www.lotus.com/dxl")
	private ConfigAclEntry[] entries;
	

	public AccessLevel getMaxInternetAccess() {
		return maxInternetAccess;
	}

	public void setMaxInternetAccess(AccessLevel maxInternetAccess) {
		this.maxInternetAccess = maxInternetAccess;
	}

	public boolean isEnforceConsistentAcl() {
		return enforceConsistentAcl;
	}

	public void setEnforceConsistentAcl(boolean enforceConsistentAcl) {
		this.enforceConsistentAcl = enforceConsistentAcl;
	}

	public String getAdminServer() {
		return adminServer;
	}

	public void setAdminServer(String adminServer) {
		this.adminServer = adminServer;
	}

	public NamesFieldsOption getAdminServerAction() {
		return adminServerAction;
	}

	public void setAdminServerAction(NamesFieldsOption adminServerAction) {
		this.adminServerAction = adminServerAction;
	}

	public ConfigAclEntry[] getEntries() {
		return entries;
	}

	public void setEntries(ConfigAclEntry[] entries) {
		this.entries = entries;
	}
	
	public String[] getRoles() {
		return roles;
	}
	public void setRoles(String[] roles) {
		this.roles = roles;
	}
}
