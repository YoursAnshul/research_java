package com.dimetyd.bot.model;

public class SummaryData {

	


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAgreementScanned() {
		return agreementScanned;
	}
	public void setAgreementScanned(String agreementScanned) {
		this.agreementScanned = agreementScanned;
	}
	public String getInvoiceScanned() {
		return invoiceScanned;
	}
	public void setInvoiceScanned(String invoiceScanned) {
		this.invoiceScanned = invoiceScanned;
	}
	public String getPoScanned() {
		return poScanned;
	}
	public void setPoScanned(String poScanned) {
		this.poScanned = poScanned;
	}
	public String getPoInvScanned() {
		return poInvScanned;
	}
	public void setPoInvScanned(String poInvScanned) {
		this.poInvScanned = poInvScanned;
	}
	public String getOverbillIdentified() {
		return overbillIdentified;
	}
	public void setOverbillIdentified(String overbillIdentified) {
		this.overbillIdentified = overbillIdentified;
	}
	public String getPeriodCoveredFrom() {
		return periodCoveredFrom;
	}
	public void setPeriodCoveredFrom(String periodCoveredFrom) {
		this.periodCoveredFrom = periodCoveredFrom;
	}
	public String getPeriodCoveredTo() {
		return periodCoveredTo;
	}
	public void setPeriodCoveredTo(String periodCoveredTo) {
		this.periodCoveredTo = periodCoveredTo;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getUniqueKey() {
		return uniqueKey;
	}
	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public String getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getOldPOScanned() {
		return oldPOScanned;
	}
	public void setOldPOScanned(String oldPOScanned) {
		this.oldPOScanned = oldPOScanned;
	}
	public String getBilledAmount() {
		return billedAmount;
	}
	public void setBilledAmount(String billedAmount) {
		this.billedAmount = billedAmount;
	}
	public String getDisputedAmount() {
		return disputedAmount;
	}
	public void setDisputedAmount(String disputedAmount) {
		this.disputedAmount = disputedAmount;
	}
	public String getPreviousRefund() {
		return previousRefund;
	}
	public void setPreviousRefund(String previousRefund) {
		this.previousRefund = previousRefund;
	}
	public String getDuplicateOverlapingVendorCode() {
		return duplicateOverlapingVendorCode;
	}
	public void setDuplicateOverlapingVendorCode(String duplicateOverlapingVendorCode) {
		this.duplicateOverlapingVendorCode = duplicateOverlapingVendorCode;
	}
	public String getDuplicateOverlapingProductGroup() {
		return duplicateOverlapingProductGroup;
	}
	public void setDuplicateOverlapingProductGroup(String duplicateOverlapingProductGroup) {
		this.duplicateOverlapingProductGroup = duplicateOverlapingProductGroup;
	}
	private	String	id;
	private	String	vendorId;
	private	String	type;
	private	String	agreementScanned;
	private	String	invoiceScanned;
	private	String	poScanned;
	private	String	poInvScanned;
	private	String	overbillIdentified;
	private	String	periodCoveredFrom;
	private	String	periodCoveredTo;
	private	String	currency;
	private	String	uniqueKey;
	private	String	createdDate;
	private	String	modifiedDate;
	private	String	status;
	private	String	oldPOScanned;
	private	String	billedAmount;
	private	String	disputedAmount;
	private	String	previousRefund;
	private	String	duplicateOverlapingVendorCode;
	private	String	duplicateOverlapingProductGroup;

}
