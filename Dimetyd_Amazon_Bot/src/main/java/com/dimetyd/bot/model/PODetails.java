package com.dimetyd.bot.model;

import org.springframework.stereotype.Component;

@Component
public class PODetails {

	private String po;
	private String asin;
	private String poInvoice;
	private String poAsin;
	private String freightTerm;
	private int qty;
	private double unitCost;
	private String currency;
	private String invoiceDate;
	private String ukey;
	private String asinReceived;
	private int qtyReceived;
	private double amountReceived;
	private double invoicedUnitCost;

	public String getPo() {
		return po;
	}

	public void setPo(String po) {
		this.po = po;
	}

	public String getAsin() {
		return asin;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

	public String getPoInvoice() {
		return poInvoice;
	}

	public void setPoInvoice(String poInvoice) {
		this.poInvoice = poInvoice;
	}

	public String getPoAsin() {
		return poAsin;
	}

	public void setPoAsin(String poAsin) {
		this.poAsin = poAsin;
	}

	public String getFreightTerm() {
		return freightTerm;
	}

	public void setFreightTerm(String freightTerm) {
		this.freightTerm = freightTerm;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public double getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(double unitCost) {
		this.unitCost = unitCost;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public String getUkey() {
		return ukey;
	}

	public void setUkey(String ukey) {
		this.ukey = ukey;
	}

	public String getAsinReceived() {
		return asinReceived;
	}

	public void setAsinReceived(String asinReceived) {
		this.asinReceived = asinReceived;
	}

	public int getQtyReceived() {
		return qtyReceived;
	}

	public void setQtyReceived(int qtyReceived) {
		this.qtyReceived = qtyReceived;
	}

	public double getAmountReceived() {
		return amountReceived;
	}

	public void setAmountReceived(double amountReceived) {
		this.amountReceived = amountReceived;
	}

	public double getInvoicedUnitCost() {
		return invoicedUnitCost;
	}

	public void setInvoicedUnitCost(double invoicedUnitCost) {
		this.invoicedUnitCost = invoicedUnitCost;
	}

}
