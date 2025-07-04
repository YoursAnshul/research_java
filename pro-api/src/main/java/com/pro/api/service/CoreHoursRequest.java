package com.pro.api.service;

public class CoreHoursRequest {

	private Long forecastHoursId;
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

	public Long getForecastHoursId() {
		return forecastHoursId;
	}

	public void setForecastHoursId(Long forecastHoursId) {
		this.forecastHoursId = forecastHoursId;
	}

}
