package com.pro.api.service;

import java.util.Map;

public class CoreHoursRequest {

	private Long forecastHoursId;
	private String date;
	private int coreHours;
	private Long coreHoursId;
	private String entryBy;
	private Long projectId;
	private Map<String, Integer> coreHoursByMonth;
	private String dempoId;

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

	public Map<String, Integer> getCoreHoursByMonth() {
		return coreHoursByMonth;
	}

	public void setCoreHoursByMonth(Map<String, Integer> coreHoursByMonth) {
		this.coreHoursByMonth = coreHoursByMonth;
	}

	public String getDempoId() {
		return dempoId;
	}

	public void setDempoId(String dempoId) {
		this.dempoId = dempoId;
	}

}
