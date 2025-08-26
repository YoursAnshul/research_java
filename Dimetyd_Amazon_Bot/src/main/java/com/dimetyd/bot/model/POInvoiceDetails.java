package com.dimetyd.bot.model;

import org.springframework.stereotype.Component;

@Component
public class POInvoiceDetails {

	public String getPOs() {
		return POs;
	}
	public void setPOs(String pOs) {
		POs = pOs;
	}
	private String POs;
	private String invoiceDate;
	private String paymentDueDate;
	private double invoiceAmount;
	private String poInvoice;
	private double actualAmountPaid;
	private String ukey;
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
	public double getInvoiceAmount() {
		return invoiceAmount;
	}
	public void setInvoiceAmount(double invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}
	public String getPoInvoice() {
		return poInvoice;
	}
	public void setPoInvoice(String poInvoice) {
		this.poInvoice = poInvoice;
	}
	public double getActualAmountPaid() {
		return actualAmountPaid;
	}
	public void setActualAmountPaid(double actualAmountPaid) {
		this.actualAmountPaid = actualAmountPaid;
	}
	public String getUkey() {
		return ukey;
	}
	public void setUkey(String ukey) {
		this.ukey = ukey;
	}
	
}
