package com.pro.api.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ForecastingResponse {

	private String dempoid;
	private String fname;
	private String lname;
	private String projectColor;
	private String projectName;
	private List<Long> userTotalCoreHours;
	private List<Long> projectTotalCoreHours;

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

	public String getProjectColor() {
		return projectColor;
	}

	public void setProjectColor(String projectColor) {
		this.projectColor = projectColor;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public List<Long> getUserTotalCoreHours() {
		return userTotalCoreHours;
	}

	public void setUserTotalCoreHours(List<Long> userTotalCoreHours) {
		this.userTotalCoreHours = userTotalCoreHours;
	}

	public List<Long> getProjectTotalCoreHours() {
		return projectTotalCoreHours;
	}

	public void setProjectTotalCoreHours(List<Long> projectTotalCoreHours) {
		this.projectTotalCoreHours = projectTotalCoreHours;
	}

}
