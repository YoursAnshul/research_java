package com.dimetyd.bot.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Invoice {

	public String getMarketplace() {
		return marketplace;
	}

	public void setMarketplace(String marketplace) {
		this.marketplace = marketplace;
	}

	public java.sql.Date getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(java.sql.Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public java.sql.Date getPaymentDueDate() {
		return paymentDueDate;
	}

	public void setPaymentDueDate(java.sql.Date paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}

	public String getInvoiceStatus() {
		return invoiceStatus;
	}

	public void setInvoiceStatus(String invoiceStatus) {
		this.invoiceStatus = invoiceStatus;
	}

	public double getInvoiceAmount() {
		return invoiceAmount;
	}

	public void setInvoiceAmount(double invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}

	public String getPayee() {
		return payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public java.sql.Date getInvoiceCreationDate() {
		return invoiceCreationDate;
	}

	public void setInvoiceCreationDate(java.sql.Date invoiceCreationDate) {
		this.invoiceCreationDate = invoiceCreationDate;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getAnyDeductions() {
		return anyDeductions;
	}

	public void setAnyDeductions(String anyDeductions) {
		this.anyDeductions = anyDeductions;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
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

	public Integer getRetry() {
		return retry;
	}

	public void setRetry(Integer retry) {
		this.retry = retry;
	}

	private String marketplace;
	private java.sql.Date invoiceDate;
	private java.sql.Date paymentDueDate;
	private String invoiceStatus;
	private double invoiceAmount;
	private String payee;
	private java.sql.Date invoiceCreationDate;
	private String invoiceNumber;
	private String price;
	private String anyDeductions;
	private String uniqueKey;
	private Date modifiedDate;
	private String status;
	private Integer retry;
	public String getActualPaidAmount() {
		return actualPaidAmount;
	}

	public void setActualPaidAmount(String actualPaidAmount) {
		this.actualPaidAmount = actualPaidAmount;
	}

	private String actualPaidAmount;
	
	@Override
	public String toString() {
		return "Invoice [marketplace=" + marketplace + ", invoiceDate=" + invoiceDate + ", paymentDueDate="
				+ paymentDueDate + ", invoiceStatus=" + invoiceStatus + ", invoiceAmount=" + invoiceAmount + ", payee="
				+ payee + ", invoiceCreationDate=" + invoiceCreationDate + ", invoiceNumber=" + invoiceNumber
				+ ", price=" + price + ", anyDeductions=" + anyDeductions + ", uniqueKey=" + uniqueKey
				+ ", modifiedDate=" + modifiedDate + ", status=" + status + ", retry=" + retry + "]";
	}

	
}
