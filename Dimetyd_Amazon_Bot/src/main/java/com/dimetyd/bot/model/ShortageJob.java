package com.dimetyd.bot.model;

import java.sql.Time;

public class ShortageJob {
	


	private Long id;
	private Long requestId;
	private Long requestType;
	private String processName;
	private String transName;
	private String vendorName;
	private String vendorId;
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	private Time modifiedTime;
	private String hostName;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getRequestId() {
		return requestId;
	}
	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}
	public Long getRequestType() {
		return requestType;
	}
	public void setRequestType(Long requestType) {
		this.requestType = requestType;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getTransName() {
		return transName;
	}
	public void setTransName(String transName) {
		this.transName = transName;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public Time getModifiedTime() {
		return modifiedTime;
	}
	public void setModifiedTime(Time modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	


}
