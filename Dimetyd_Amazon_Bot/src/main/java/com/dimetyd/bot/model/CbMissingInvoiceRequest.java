package com.dimetyd.bot.model;

import java.util.Date;

public class CbMissingInvoiceRequest {

	private Long id;
	private String vendorId;
	private String vendorName;
	private String businessUnit;
	private Date invoiceCreateDate;
	private Date cronTriggerDate;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
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
	

}
