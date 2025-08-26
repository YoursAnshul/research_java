package com.dimetyd.bot.model;

public class ItemizedShortageStage1 {

	private int jobId;
	private String Vendorname;
	private String vendorId;
	private String status;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
