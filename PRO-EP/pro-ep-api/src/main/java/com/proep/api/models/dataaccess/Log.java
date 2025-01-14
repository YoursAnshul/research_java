package com.proep.api.models.dataaccess;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Logs")
public class Log {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "LogId")
	private int logId;
	@Column(name = "NetId")
	private String netId;
	@Column(name = "Type")
	private String type;
	@Column(name = "MessageLocation")
	private String messageLocation;
	@Column(name = "Message")
	private String message;
	@Column(name = "FullMessage")
	private String fullMessage;
	@Column(name = "LogDate")
	private OffsetDateTime logDate;

	public Log() {

	}

	public Log(String netId, String type, String messageLocation, String message, String fullMessage,
		OffsetDateTime logDate) {
		this.netId = netId;
		this.type = type;
		this.messageLocation = messageLocation;
		this.message = message;
		this.fullMessage = fullMessage;
		this.logDate = logDate;
	}

	public int getLogId() {
		return logId;
	}

	public void setLogId(int logId) {
		this.logId = logId;
	}

	public String getNetId() {
		return netId;
	}

	public void setNetId(String netId) {
		this.netId = netId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessageLocation() {
		return messageLocation;
	}

	public void setMessageLocation(String messageLocation) {
		this.messageLocation = messageLocation;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public void setFullMessage(String fullMessage) {
		this.fullMessage = fullMessage;
	}

	public OffsetDateTime getLogDate() {
		return logDate;
	}

	public void setLogDate(OffsetDateTime logDate) {
		this.logDate = logDate;
	}

	@Override
	public String toString() {
		return "Log{" + "logId=" + logId + ", netId='" + netId + '\'' + ", type='" + type + '\'' + ", messageLocation='"
				+ messageLocation + '\'' + ", message='" + message + '\'' + ", fullMessage='" + fullMessage + '\''
				+ ", logDate=" + logDate + '}';
	}
}
