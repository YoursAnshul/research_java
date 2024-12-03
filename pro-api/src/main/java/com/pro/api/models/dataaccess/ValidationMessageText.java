package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

@Entity
@Table(name = "ValidationMessageText")
public class ValidationMessageText {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ValidationMessageTextId")
	private int validationMessageTextId;

	@Column(name = "MessageId")
	private int messageId;

	@Column(name = "OrderId")
	private int orderId;

	@Column(columnDefinition="text", length=10485760, name = "MessageText")
	private String messageText;

	@Column(name = "ScheduleKeys")
	private String scheduleKeys;

	// Constructors, Getters, and Setters

	public ValidationMessageText() {
		// Default constructor
	}

	public int getValidationMessageTextId() {
		return validationMessageTextId;
	}

	public void setValidationMessageTextId(int validationMessageTextId) {
		this.validationMessageTextId = validationMessageTextId;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public String getScheduleKeys() {
		return scheduleKeys;
	}

	public void setScheduleKeys(String scheduleKeys) {
		this.scheduleKeys = scheduleKeys;
	}
}
