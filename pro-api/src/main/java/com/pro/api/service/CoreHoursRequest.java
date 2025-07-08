package com.pro.api.service;

public class CoreHoursRequest {

	private String dempoId;
	private String date;
	private int coreHours;

	public int getCoreHours() {
		return coreHours;
	}

	public void setCoreHours(int coreHours) {
		this.coreHours = coreHours;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDempoId() {
		return dempoId;
	}

	public void setDempoId(String dempoId) {
		this.dempoId = dempoId;
	}
}
