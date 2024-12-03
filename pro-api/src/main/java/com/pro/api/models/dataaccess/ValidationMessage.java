package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "ValidationMessages")
public class ValidationMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ValidationMessagesID")
	private int validationMessagesId;

	@Column(name = "DempoID")
	private String dempoId;

	@Column(name = "MessageID")
	private int messageId;

	@Column(name = "InMonth")
	private LocalDate inMonth;

	@Column(name = "ScheduleKeys")
	private String scheduleKeys;

	@Column(name = "Details")
	private String details;

	public ValidationMessage() {
		
	}
	public ValidationMessage(int messageId, LocalDate inMonth, String dempoId, String scheduleKeys) {
		this.dempoId = dempoId;
		this.messageId = messageId;
		this.inMonth = inMonth;
		this.scheduleKeys = scheduleKeys;
	}

	public ValidationMessage(String dempoId, int messageId, LocalDate inMonth, String details) {
		this.dempoId = dempoId;
		this.messageId = messageId;
		this.inMonth = inMonth;
		this.details = details;
	}


	public int getValidationMessagesId() {
		return validationMessagesId;
	}

	public void setValidationMessagesId(int validationMessagesId) {
		this.validationMessagesId = validationMessagesId;
	}

	public String getDempoId() {
		return dempoId;
	}

	public void setDempoId(String dempoId) {
		this.dempoId = dempoId;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}

	public LocalDate getInMonth() {
		return inMonth;
	}

	public void setInMonth(LocalDate inMonth) {
		this.inMonth = inMonth;
	}

	public String getScheduleKeys() {
		return scheduleKeys;
	}

	public void setScheduleKeys(String scheduleKeys) {
		this.scheduleKeys = scheduleKeys;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
}
