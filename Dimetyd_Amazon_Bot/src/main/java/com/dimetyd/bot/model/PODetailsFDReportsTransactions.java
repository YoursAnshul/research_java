package com.dimetyd.bot.model;

import org.springframework.stereotype.Component;

@Component
public class PODetailsFDReportsTransactions {
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
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	private String id;
	private String vendorId;
	private String startDate;
	private String endDate;
	public String getIsFDReport() {
		return isFDReport;
	}
	public void setIsFDReport(String isFDReport) {
		this.isFDReport = isFDReport;
	}
	private String isFDReport;
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	private String year;
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	private String vendorName;
	public String getFileRowCount() {
		return FileRowCount;
	}
	public void setFileRowCount(String fileRowCount) {
		FileRowCount = fileRowCount;
	}
	private String FileRowCount;

}
