package com.pro.api.response;

import java.time.LocalDate;
import java.util.Map;

public class ForecastingResponse {

	private String dempoid;
	private String fname;
	private String lname;

	private Map<LocalDate, Integer> coreHoursByMonth;

	public String getDempoid() {
		return dempoid;
	}

	public void setDempoid(String dempoid) {
		this.dempoid = dempoid;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public Map<LocalDate, Integer> getCoreHoursByMonth() {
		return coreHoursByMonth;
	}

	public void setCoreHoursByMonth(Map<LocalDate, Integer> coreHoursByMonth) {
		this.coreHoursByMonth = coreHoursByMonth;
	}

}
