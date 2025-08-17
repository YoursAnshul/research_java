package com.pro.api.response;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.util.Pair;

public class ForecastingResponse {

	private Long forecastHoursId;
	private String fname;
	private String lname;
	private String projectColor;
	private String projectName;
	private List<Long> userTotalCoreHours;
	private List<Long> projectTotalCoreHours;
	private Long coreHoursId;
	private Long projectId;
	private String dempoId;

	List<Pair<LocalDate, Integer>> coreHoursByMonth;

	public List<Pair<LocalDate, Integer>> getCoreHoursByMonth() {
		return coreHoursByMonth;
	}

	public void setCoreHoursByMonth(List<Pair<LocalDate, Integer>> coreHoursByMonth) {
		this.coreHoursByMonth = coreHoursByMonth;
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

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getDempoId() {
		return dempoId;
	}

	public void setDempoId(String dempoId) {
		this.dempoId = dempoId;
	}

}
