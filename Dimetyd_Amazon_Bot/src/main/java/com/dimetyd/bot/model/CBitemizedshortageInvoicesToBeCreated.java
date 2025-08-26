package com.dimetyd.bot.model;

public class CBitemizedshortageInvoicesToBeCreated {

	private int jobId;
	private String Vendorname;
	private String vendorId;
	private String qtyVarianceAmount;
	private String invoiceNumber;
	private String invoiceAmount;
	private String invoiceDate;
	private String paymentDueDate;
	private String payee;

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public String getPaymentDueDate() {
		return paymentDueDate;
	}

	public void setPaymentDueDate(String paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}

	public String getPayee() {
		return payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public String getVendorname() {
		return Vendorname;
	}

	public void setVendorname(String vendorname) {
		Vendorname = vendorname;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public String getQtyVarianceAmountl() {
		return qtyVarianceAmount;
	}

	public void setQtyVarianceAmount(String qtyVarianceAmountl) {
		this.qtyVarianceAmount = qtyVarianceAmountl;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getInvoiceAmount() {
		return invoiceAmount;
	}

	public void setInvoiceAmount(String invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}

}
