package com.dimetyd.bot.model;

import java.util.Date;

public class JobData {
	public String getQty() {
		return Qty;
	}
	public void setQty(String qty) {
		Qty = qty;
	}
	public String Qty;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPo() {
		return po;
	}
	public void setPo(String po) {
		this.po = po;
	}
	public String getPoInvoice() {
		return poInvoice;
	}
	public void setPoInvoice(String poInvoice) {
		this.poInvoice = poInvoice;
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public String getVendoName() {
		return vendoName;
	}
	public void setVendoName(String vendoName) {
		this.vendoName = vendoName;
	}
	public long getRequestId() {
		return requestId;
	}
	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
	public String getUniqueKey() {
		return uniqueKey;
	}
	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	public Integer getRetry() {
		return retry;
	}
	public void setRetry(Integer retry) {
		this.retry = retry;
	}
	public String getAgreementId() {
		return agreementId;
	}
	public void setAgreementId(String agreementId) {
		this.agreementId = agreementId;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getMechineName() {
		return mechineName;
	}
	public void setMechineName(String mechineName) {
		this.mechineName = mechineName;
	}
	public String getReturnId() {
		return returnId;
	}
	public void setReturnId(String returnId) {
		this.returnId = returnId;
	}
	public String getMarkletPlace() {
		return markletPlace;
	}
	public void setMarkletPlace(String markletPlace) {
		this.markletPlace = markletPlace;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public int getCoolingPeriod() {
		return coolingPeriod;
	}
	public void setCoolingPeriod(int coolingPeriod) {
		this.coolingPeriod = coolingPeriod;
	}
	public java.sql.Timestamp getCoolingPeriodStartTime() {
		return coolingPeriodStartTime;
	}
	public void setCoolingPeriodStartTime(java.sql.Timestamp coolingPeriodStartTime) {
		this.coolingPeriodStartTime = coolingPeriodStartTime;
	}
	public java.sql.Timestamp getLastLoginTime() {
		return lastLoginTime;
	}
	public void setLastLoginTime(java.sql.Timestamp lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getIsLoggedIn() {
		return isLoggedIn;
	}
	public void setIsLoggedIn(String isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}
	private Long id;
	private String po;
	private String poInvoice;
	private String vendorId;
	private String vendoName;
	private long requestId;
	private String uniqueKey;
	private Integer retry;
	private String agreementId;
	private String requestType;
	private String mechineName;
	private String returnId;
	private String markletPlace;
	private Date startDate;
	private Date endDate;
	private String otp;
	private String invoiceNumber;
	private int coolingPeriod;
	private java.sql.Timestamp coolingPeriodStartTime;
	private java.sql.Timestamp lastLoginTime;
	private int createdBy;
	private String instanceId;
	private String fileName;
	private String processName;
	private String isLoggedIn;

	
}
