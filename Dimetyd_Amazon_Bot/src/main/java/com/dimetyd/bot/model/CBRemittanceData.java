package com.dimetyd.bot.model;

import java.util.Date;

public class CBRemittanceData {
	private Integer requestId;
	private String paymentNumber;
	private String vendorId;
	private String invoiceNumber;
	private String parentInvoice;
	private String invoiceDate;
	private String Description;
	private Double invoiceAmount;
	private Double termsDiscountTaken;
	private Double amountPaid;
	private String remainingAmountAsOf;
	private String invoiceType;
	private Date createdDate;
	private Date modifiedDate;
	private String status;
	private String comments;
	private Byte Match;
	private Integer matches;
	private String paymentDate;
	private String currency;
	private String uniqueKey;
	private String invoiceTypeDetailed;
	private Date paymentDueDate;
	private String vendorCode;
	public Integer getRequestId() {
		return requestId;
	}
	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}
	public String getPaymentNumber() {
		return paymentNumber;
	}
	public void setPaymentNumber(String paymentNumber) {
		this.paymentNumber = paymentNumber;
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public String getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public Double getInvoiceAmount() {
		return invoiceAmount;
	}
	public void setInvoiceAmount(Double invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}
	public Double getTermsDiscountTaken() {
		return termsDiscountTaken;
	}
	public void setTermsDiscountTaken(Double termsDiscountTaken) {
		this.termsDiscountTaken = termsDiscountTaken;
	}
	public Double getAmountPaid() {
		return amountPaid;
	}
	public void setAmountPaid(Double amountPaid) {
		this.amountPaid = amountPaid;
	}
	public String getRemainingAmountAsOf() {
		return remainingAmountAsOf;
	}
	public void setRemainingAmountAsOf(String remainingAmountAsOf) {
		this.remainingAmountAsOf = remainingAmountAsOf;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getPaymentDate() {
		return paymentDate;
	}
	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
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
	public String getParentInvoice() {
		return parentInvoice;
	}
	public void setParentInvoice(String parentInvoice) {
		this.parentInvoice = parentInvoice;
	}
	public String getInvoiceType() {
		return invoiceType;
	}
	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Byte getMatch() {
		return Match;
	}
	public void setMatch(Byte match) {
		Match = match;
	}
	public Integer getMatches() {
		return matches;
	}
	public void setMatches(Integer matches) {
		this.matches = matches;
	}
	public String getInvoiceTypeDetailed() {
		return invoiceTypeDetailed;
	}
	public void setInvoiceTypeDetailed(String invoiceTypeDetailed) {
		this.invoiceTypeDetailed = invoiceTypeDetailed;
	}
	public Date getPaymentDueDate() {
		return paymentDueDate;
	}
	public void setPaymentDueDate(Date paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}
	public String getVendorCode() {
		return vendorCode;
	}
	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}
	
}
