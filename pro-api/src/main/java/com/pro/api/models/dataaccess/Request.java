package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "Requests") // Name of the database table
public class Request {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "RequestID")
	private int requestId;

	@Column(name = "requestCodeID")
	private Integer requestCodeId;

	@Column(name = "interviewerEmpID")
	private String interviewerEmpId;

	@Column(name = "resourceTeamMemberID")
	private String resourceTeamMemberId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "requestDate")
	private Date requestDate;

	@Column(name = "RequestDetails")
	private String requestDetails;

	@Column(name = "decisionID")
	private Integer decisionId;

	@Column(name = "Notes")
	private String notes;

	@Column(name = "EntryBy")
	private String entryBy;

	@Column(name = "EntryDT")
	private LocalDateTime entryDt;

	@Column(name = "ModBy")
	private String modBy;

	@Column(name = "ModDT")
	private LocalDateTime modDt;

	public Request() {

	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public Integer getRequestCodeId() {
		return requestCodeId;
	}

	public void setRequestCodeId(Integer requestCodeId) {
		this.requestCodeId = requestCodeId;
	}

	public String getInterviewerEmpId() {
		return interviewerEmpId;
	}

	public void setInterviewerEmpId(String interviewerEmpId) {
		this.interviewerEmpId = interviewerEmpId;
	}

	public String getResourceTeamMemberId() {
		return resourceTeamMemberId;
	}

	public void setResourceTeamMemberId(String resourceTeamMemberId) {
		this.resourceTeamMemberId = resourceTeamMemberId;
	}

	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public String getRequestDetails() {
		return requestDetails;
	}

	public void setRequestDetails(String requestDetails) {
		this.requestDetails = requestDetails;
	}

	public Integer getDecisionId() {
		return decisionId;
	}

	public void setDecisionId(Integer decisionId) {
		this.decisionId = decisionId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getEntryBy() {
		return entryBy;
	}

	public void setEntryBy(String entryBy) {
		this.entryBy = entryBy;
	}

	public LocalDateTime getEntryDt() {
		return entryDt;
	}

	public void setEntryDt(LocalDateTime entryDt) {
		this.entryDt = entryDt;
	}

	public String getModBy() {
		return modBy;
	}

	public void setModBy(String modBy) {
		this.modBy = modBy;
	}

	public LocalDateTime getModDt() {
		return modDt;
	}

	public void setModDt(LocalDateTime modDt) {
		this.modDt = modDt;
	}
}
