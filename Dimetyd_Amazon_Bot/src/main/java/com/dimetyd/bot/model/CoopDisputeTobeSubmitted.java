package com.dimetyd.bot.model;

public class CoopDisputeTobeSubmitted {
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
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
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getIsLookBackOrReRun() {
		return isLookBackOrReRun;
	}
	public void setIsLookBackOrReRun(String isLookBackOrReRun) {
		this.isLookBackOrReRun = isLookBackOrReRun;
	}
	private String jobId;
	private String vendorId;
	private String vendorName;
	private String userId;
	private String isLookBackOrReRun;

}
