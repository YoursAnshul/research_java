package com.proep.api.models.dataaccess;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CommunicationsHub_AuditTable") // Name of the database table
public class CommunicationsHubAuditTable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "auditkey")
	private int auditkey;

	@Column(name = "audit_action")
	private String auditAction;

	@Column(name = "audit_date")
	private LocalDateTime auditDate;

	@Column(name = "audit_user")
	private String auditUser;

	@Column(name = "CommunicationsHubId")
	private Integer communicationsHubId;

	@Column(name = "ProjectId")
	private Integer projectId;

	@Column(name = "EntryId")
	private Integer entryId;

	@Column(name = "FormFieldId")
	private Integer formFieldId;

	@Column(name = "Value")
	private String value;

	@Column(name = "EntryDT")
	private LocalDateTime entryDt;

	@Column(name = "EntryBy")
	private String entryBy;

	@Column(name = "ModDT")
	private LocalDateTime modDt;

	@Column(name = "ModBy")
	private String modBy;

	// Getters and Setters

	public int getAuditkey() {
		return auditkey;
	}

	public void setAuditkey(int auditkey) {
		this.auditkey = auditkey;
	}

	public String getAuditAction() {
		return auditAction;
	}

	public void setAuditAction(String auditAction) {
		this.auditAction = auditAction;
	}

	public LocalDateTime getAuditDate() {
		return auditDate;
	}

	public void setAuditDate(LocalDateTime auditDate) {
		this.auditDate = auditDate;
	}

	public String getAuditUser() {
		return auditUser;
	}

	public void setAuditUser(String auditUser) {
		this.auditUser = auditUser;
	}

	public Integer getCommunicationsHubId() {
		return communicationsHubId;
	}

	public void setCommunicationsHubId(Integer communicationsHubId) {
		this.communicationsHubId = communicationsHubId;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public Integer getEntryId() {
		return entryId;
	}

	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	public Integer getFormFieldId() {
		return formFieldId;
	}

	public void setFormFieldId(Integer formFieldId) {
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
