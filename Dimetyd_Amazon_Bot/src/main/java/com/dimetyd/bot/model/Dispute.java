package com.dimetyd.bot.model;

import java.util.Date;

public class Dispute {

	private String disputeType;
	private String disputeReason;
	private Date disputeDate;
	private String disputeStatus;
	private String disputedAmount;
	private String approvedAmount;

	public String getDisputeType() {
		return disputeType;
	}

	public void setDisputeType(String disputeType) {
		this.disputeType = disputeType;
	}

	public String getDisputeReason() {
		return disputeReason;
	}

	public void setDisputeReason(String disputeReason) {
		this.disputeReason = disputeReason;
	}

	public Date getDisputeDate() {
		return disputeDate;
	}

	public void setDisputeDate(Date disputeDate) {
		this.disputeDate = disputeDate;
	}

	public String getDisputeStatus() {
		return disputeStatus;
	}

	public void setDisputeStatus(String disputeStatus) {
		this.disputeStatus = disputeStatus;
	}

	public String getDisputedAmount() {
		return disputedAmount;
	}

	public void setDisputedAmount(String disputedAmount) {
		this.disputedAmount = disputedAmount;
	}

	public String getApprovedAmount() {
		return approvedAmount;
	}

	public void setApprovedAmount(String approvedAmount) {
		this.approvedAmount = approvedAmount;
	}

}
