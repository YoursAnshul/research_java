package com.dimetyd.bot.model;

import java.util.Date;

public class ShortageJobData {
	


	private Long id;
	private Long requestId;
	private String vendorName;
	private String month;
	private String status;
	private Integer year;
	private String reportingPeriod;
	private String vendorId;
	private Date poStartDate;
	private Date poEndDate;
	private String po;
	private String poInvoice;
	private String requestIdForStage2;
	private String uniqueKey;
	private Integer retry;
	private String agreementId;
	private String requestType;
	private String mechineName;
	private String returnId;
	private String markletPlace;
	private String startDate;
	private String endDate;
	private String invoiceNumber;
	private double amountFromDB;
	private String businessUnit;
	private Date invoiceCreateDate;
	private Date cronTriggerDate;
	private String isOtpIssue;
	private int retryCounter;
	private int coolDownPeriod;
	private java.sql.Timestamp coolDownPeriodStartTime;
	private String otp;
	private int coolingPeriod;
	private java.sql.Timestamp coolingPeriodStartTime;
	private java.sql.Timestamp lastLoginTime;
	private String fileDownLoad;
	private int createdBy;
	
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getRequestId() {
		return requestId;
	}
	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public String getReportingPeriod() {
		return reportingPeriod;
	}
	public void setReportingPeriod(String reportingPeriod) {
		this.reportingPeriod = reportingPeriod;
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public Date getPoStartDate() {
		return poStartDate;
	}
	public void setPoStartDate(Date poStartDate) {
		this.poStartDate = poStartDate;
	}
	public Date getPoEndDate() {
		return poEndDate;
	}
	public void setPoEndDate(Date poEndDate) {
		this.poEndDate = poEndDate;
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
	public String getRequestIdForStage2() {
		return requestIdForStage2;
	}
	public void setRequestIdForStage2(String requestIdForStage2) {
		this.requestIdForStage2 = requestIdForStage2;
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
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public double getAmountFromDB() {
		return amountFromDB;
	}
	public void setAmountFromDB(double amountFromDB) {
		this.amountFromDB = amountFromDB;
	}
	public String getBusinessUnit() {
		return businessUnit;
	}
	public void setBusinessUnit(String businessUnit) {
		this.businessUnit = businessUnit;
	}
	public Date getInvoiceCreateDate() {
		return invoiceCreateDate;
	}
	public void setInvoiceCreateDate(Date invoiceCreateDate) {
		this.invoiceCreateDate = invoiceCreateDate;
	}
	public Date getCronTriggerDate() {
		return cronTriggerDate;
	}
	public void setCronTriggerDate(Date cronTriggerDate) {
		this.cronTriggerDate = cronTriggerDate;
	}
	public String getIsOtpIssue() {
		return isOtpIssue;
	}
	public void setIsOtpIssue(String isOtpIssue) {
		this.isOtpIssue = isOtpIssue;
	}
	public int getRetryCounter() {
		return retryCounter;
	}
	public void setRetryCounter(int retryCounter) {
		this.retryCounter = retryCounter;
	}
	public int getCoolDownPeriod() {
		return coolDownPeriod;
	}
	public void setCoolDownPeriod(int coolDownPeriod) {
		this.coolDownPeriod = coolDownPeriod;
	}
	public java.sql.Timestamp getCoolDownPeriodStartTime() {
		return coolDownPeriodStartTime;
	}
	public void setCoolDownPeriodStartTime(java.sql.Timestamp coolDownPeriodStartTime) {
		this.coolDownPeriodStartTime = coolDownPeriodStartTime;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
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
	public String getFileDownLoad() {
		return fileDownLoad;
	}
	public void setFileDownLoad(String fileDownLoad) {
		this.fileDownLoad = fileDownLoad;
	}




}
