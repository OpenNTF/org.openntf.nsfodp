/*
 * Copyright © 2018-2025 Jesse Gallagher
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
 * Represents an individual entry within a ODP ACL definition.
 * 
 * @author Jesse Gallagher
 * @since 3.1.0
 */
@XmlRootElement(name="aclentry", namespace="http://www.lotus.com/dxl")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigAclEntry {
	@XmlEnum
	public enum EntryType {
		unspecified,
		person,
		persongroup,
		server,
		servergroup,
		mixedgroup
	}

	@XmlAttribute(name="level")
	private ConfigAcl.AccessLevel level;
	@XmlAttribute(name="default")
	private boolean defaultEntry = false;
	@XmlAttribute(name="name")
	private String name;

	@XmlAttribute(name="writepublicdocs")
	private Boolean writePublicDocs;
	@XmlAttribute(name="readpublicdocs")
	private Boolean readPublicDocs;
	@XmlAttribute(name="noreplicate")
	private Boolean noReplicate;
	@XmlAttribute(name="createdocuments")
	private Boolean createDocuments;
	@XmlAttribute(name="createlsjavaagents")
	private Boolean createLsJavaAgents;
	@XmlAttribute(name="createpersonalagents")
	private Boolean createPersonalAgents;
	@XmlAttribute(name="createsharedviews")
	private Boolean createSharedViews;
	@XmlAttribute(name="createpersonalviews")
	private Boolean createPersonalViews;
	@XmlAttribute(name="deletedocs")
	private Boolean deleteDocs;

	@XmlElement(name="role", namespace="http://www.lotus.com/dxl")
	private String[] roles;
	
	public ConfigAcl.AccessLevel getLevel() {
		return level;
	}
	public void setLevel(ConfigAcl.AccessLevel level) {
		this.level = level;
	}
	public boolean isDefaultEntry() {
		return defaultEntry;
	}
	public void setDefaultEntry(boolean defaultEntry) {
		this.defaultEntry = defaultEntry;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getWritePublicDocs() {
		return writePublicDocs;
	}
	public void setWritePublicDocs(Boolean writePublicDocs) {
		this.writePublicDocs = writePublicDocs;
	}
	public Boolean getReadPublicDocs() {
		return readPublicDocs;
	}
	public void setReadPublicDocs(Boolean readPublicDocs) {
		this.readPublicDocs = readPublicDocs;
	}
	public Boolean getNoReplicate() {
		return noReplicate;
	}
	public void setNoReplicate(Boolean noReplicate) {
		this.noReplicate = noReplicate;
	}
	public Boolean getCreateDocuments() {
		return createDocuments;
	}
	public void setCreateDocuments(Boolean createDocuments) {
		this.createDocuments = createDocuments;
	}
	public Boolean getCreateLsJavaAgents() {
		return createLsJavaAgents;
	}
	public void setCreateLsJavaAgents(Boolean createLsJavaAgents) {
		this.createLsJavaAgents = createLsJavaAgents;
	}
	public Boolean getCreatePersonalAgents() {
		return createPersonalAgents;
	}
	public void setCreatePersonalAgents(Boolean createPersonalAgents) {
		this.createPersonalAgents = createPersonalAgents;
	}
	public Boolean getCreateSharedViews() {
		return createSharedViews;
	}
	public void setCreateSharedViews(Boolean createSharedViews) {
		this.createSharedViews = createSharedViews;
	}
	public Boolean getCreatePersonalViews() {
		return createPersonalViews;
	}
	public void setCreatePersonalViews(Boolean createPersonalViews) {
		this.createPersonalViews = createPersonalViews;
	}
	public Boolean getDeleteDocs() {
		return deleteDocs;
	}
	public void setDeleteDocs(Boolean deleteDocs) {
		this.deleteDocs = deleteDocs;
	}
	
	public String[] getRoles() {
		return roles;
	}
	public void setRoles(String[] roles) {
		this.roles = roles;
	}
}
