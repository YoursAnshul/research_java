package com.dimetyd.bot.model;

import java.util.Date;

public class OpenShortageDipsute {
	private int Id;
	private String vendorName;
	private String vendorId;
	private String status;
	private String disputeInvoice;
	private double disputeAmount;
	private Date createdDate;
	private String requesttype;


	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDisputeInvoice() {
		return disputeInvoice;
	}
	public void setDisputeInvoice(String disputeInvoice) {
		this.disputeInvoice = disputeInvoice;
	}
	public double getDisputeAmount() {
		return disputeAmount;
	}
	public void setDisputeAmount(double disputeAmount) {
		this.disputeAmount = disputeAmount;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getRequesttype() {
		return requesttype;
	}
	public void setRequesttype(String requesttype) {
		this.requesttype = requesttype;
	}

}

	