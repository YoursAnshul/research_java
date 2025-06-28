package com.pro.api.response;

import java.util.Date;

public class ScheduleResponse {
	private String comments;
	private String startTime;
	private String endTime;
	private Double duration;
	private Date dayWiseDate;
	private User user;
	private Projects projects;
	private Long preschedulekey;
	private String language;
	private Integer coreHours1;

	public ScheduleResponse() {
	}

	public ScheduleResponse(String comments, String startTime, String endTime, Double duration, Date dayWiseDate,
			User user, Projects projects, Long preschedulekey, String language, Integer coreHours1) {
		this.comments = comments;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.dayWiseDate = dayWiseDate;
		this.user = user;
		this.projects = projects;
		this.preschedulekey = preschedulekey;
		this.language = language;
		this.coreHours1 = coreHours1;
	}

	public Long getPreschedulekey() {
		return preschedulekey;
	}

	public void setPreschedulekey(Long preschedulekey) {
		this.preschedulekey = preschedulekey;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Double getDuration() {
		return duration;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	public Date getDayWiseDate() {
		return dayWiseDate;
	}

	public void setDayWiseDate(Date dayWiseDate) {
		this.dayWiseDate = dayWiseDate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Projects getProjects() {
		return projects;
	}

	public void setProjects(Projects projects) {
		this.projects = projects;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Integer getCoreHours1() {
		return coreHours1;
	}

	public void setCoreHours1(Integer coreHours1) {
		this.coreHours1 = coreHours1;
	}

}
