package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "logs")
public class Log {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "logid")
	private int logId;
	@Column(name = "netid")
	private String netId;
	@Column(name = "type")
	private String type;
	@Column(name = "messagelocation")
	private String messageLocation;
	@Column(name = "message")
	private String message;
	@Column(name = "fullmessage")
	private String fullMessage;
	@Column(name = "logdate")
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
