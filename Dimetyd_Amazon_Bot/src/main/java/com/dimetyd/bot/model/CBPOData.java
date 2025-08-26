package com.dimetyd.bot.model;

import java.util.Date;


public class CBPOData {

	private String Order_Date;
	private String PO;
	private String vendorId;
	private Date createdDate;
	private String month;
	private String year;
	private String status;
	private String Ukey;
	private String wndowType;
	public String getWndowType() {
		return wndowType;
	}
	public void setWndowType(String wndowType) {
		this.wndowType = wndowType;
	}
	public String getOrder_Date() {
		return Order_Date;
	}
	public void setOrder_Date(String order_Date) {
		Order_Date = order_Date;
	}
	public String getPO() {
		return PO;
	}
	public void setPO(String pO) {
		PO = pO;
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUkey() {
		return Ukey;
	}
	public void setUkey(String ukey) {
		Ukey = ukey;
	}
	
}
