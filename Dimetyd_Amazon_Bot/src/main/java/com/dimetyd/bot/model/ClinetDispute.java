package com.dimetyd.bot.model;

import java.util.Date;

public class ClinetDispute {

	private String vendorName;
	private String vendorId;
	private String disputeId;
	private double disputeAmount;
	private Date modifiedDate;
	private String currency;
	private String type;
	private String reason;
	private String isLookbackOrReRun;
	private int priority;
	private int clientId;

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public String getDisputeId() {
		return disputeId;
	}

	public void setDisputeId(String disputeId) {
		this.disputeId = disputeId;
	}

	public double getDisputeAmount() {
		return disputeAmount;
	}

	public void setDisputeAmount(double disputeAmount) {
		this.disputeAmount = disputeAmount;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getIsLookbackOrReRun() {
		return isLookbackOrReRun;
	}

	public void setIsLookbackOrReRun(String isLookbackOrReRun) {
		this.isLookbackOrReRun = isLookbackOrReRun;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

}