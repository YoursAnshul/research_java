package com.dimetyd.bot.model;

public class CBMissingInvPOInvoice {
	private Long id;
	private String vendorId;
	private String po;
	private String vendorName;
	private String poInvoice;
	private Integer retry;
	private String businessUnit;

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
	public String getPo() {
		return po;
	}
	public void setPo(String po) {
		this.po = po;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getPoInvoice() {
		return poInvoice;
	}
	public void setPoInvoice(String poInvoice) {
		this.poInvoice = poInvoice;
	}
	public Integer getRetry() {
		return retry;
	}
	public void setRetry(Integer retry) {
		this.retry = retry;
	}
	public String getBusinessUnit() {
		return businessUnit;
	}
	public void setBusinessUnit(String businessUnit) {
		this.businessUnit = businessUnit;
	}
	

}
