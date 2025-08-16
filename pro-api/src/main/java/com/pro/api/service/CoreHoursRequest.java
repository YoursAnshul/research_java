package com.pro.api.service;

public class CoreHoursRequest {

	private Long forecastHoursId;
	private String date;
	private int coreHours;
	private Long coreHoursId;
	private String entryBy;
	private Long projectId;

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

	public Long getForecastHoursId() {
		return forecastHoursId;
	}

	public void setForecastHoursId(Long forecastHoursId) {
		this.forecastHoursId = forecastHoursId;
	}

	public Long getCoreHoursId() {
		return coreHoursId;
	}

	public void setCoreHoursId(Long coreHoursId) {
		this.coreHoursId = coreHoursId;
	}

	public String getEntryBy() {
		return entryBy;
	}

	public void setEntryBy(String entryBy) {
		this.entryBy = entryBy;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

}
