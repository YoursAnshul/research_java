package com.dimetyd.bot.model;

public class ShortageCaseId {



	
	private String id;
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
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getCaseId() {
		return CaseId;
	}
	public void setCaseId(String caseId) {
		CaseId = caseId;
	}
	public String getCurrentCaseStatus() {
		return CurrentCaseStatus;
	}
	public void setCurrentCaseStatus(String currentCaseStatus) {
		CurrentCaseStatus = currentCaseStatus;
	}
	private String vendorId;
	private String vendorName;
	private String CaseId;
	private String CurrentCaseStatus;
	
	

}
