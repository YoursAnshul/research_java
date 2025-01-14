package com.proep.api.models.dataaccess;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CommunicationsHub")
public class CommunicationsHub {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CommunicationsHubId")
	private int communicationsHubId;

	@Column(name = "ProjectId")
	private int projectId;

	@Column(name = "EntryId")
	private int entryId;

	@Column(name = "FormFieldId")
	private int formFieldId;

	@Column(name = "Value")
	private String value;

	@Column(name = "EntryDt")
	private LocalDateTime entryDt;

	@Column(name = "EntryBy")
	private String entryBy;

	@Column(name = "ModDt")
	private LocalDateTime modDt;

	@Column(name = "ModBy")
	private String modBy;

	public CommunicationsHub() {

	}

	public int getCommunicationsHubId() {
		return communicationsHubId;
	}

	public void setCommunicationsHubId(int communicationsHubId) {
		this.communicationsHubId = communicationsHubId;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public int getEntryId() {
		return entryId;
	}

	public void setEntryId(int entryId) {
		this.entryId = entryId;
	}

	public int getFormFieldId() {
		return formFieldId;
	}

	public void setFormFieldId(int formFieldId) {
		this.formFieldId = formFieldId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public LocalDateTime getEntryDt() {
		return entryDt;
	}

	public void setEntryDt(LocalDateTime entryDt) {
		this.entryDt = entryDt;
	}

	public String getEntryBy() {
		return entryBy;
	}

	public void setEntryBy(String entryBy) {
		this.entryBy = entryBy;
	}

	public LocalDateTime getModDt() {
		return modDt;
	}

	public void setModDt(LocalDateTime modDt) {
		this.modDt = modDt;
	}

	public String getModBy() {
		return modBy;
	}

	public void setModBy(String modBy) {
		this.modBy = modBy;
	}
}