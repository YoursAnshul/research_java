package com.dimetyd.bot.model;

import java.util.Date;

public class CBShortageReconciliation {


	private Integer requestId;
	private String paymentNumber;
	private String vendorId;
	private String invoiceNumber;
	private String invoiceDate;
	private String Description;
	private Double invoiceAmount;
	private Double termsDiscountTaken;
	private Double amountPaid;
	private String remainingAmountAsOf;
	private Date createdDate;
	private String paymentDate;
	private String currency;
	private String uniqueKey;

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


	
	

}
