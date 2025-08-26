package com.dimetyd.bot.model;

public class CBOperationalPerformanceJobData {
	
	
	

	private String id;
	private String vendorId;
    private String vendorName;
    private String inEndDate;
    private String inStartDate;
    private String reportingPeriod;
    private String status;

    // Getters and Setters for each field
    
    public String getVendorId() { return vendorId; }
    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getInEndDate() { return inEndDate; }
    public void setInEndDate(String inEndDate) { this.inEndDate = inEndDate; }

    public String getInStartDate() { return inStartDate; }
    public void setInStartDate(String inStartDate) { this.inStartDate = inStartDate; }

    public String getReportingPeriod() { return reportingPeriod; }
    public void setReportingPeriod(String reportingPeriod) { this.reportingPeriod = reportingPeriod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


}
