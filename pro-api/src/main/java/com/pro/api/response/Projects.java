package com.pro.api.response;

public class Projects {
	private Integer projectId;
	private String projectColor;
	private String projectName;

	public Projects() {
	}

	public Projects(Integer projectId, String projectColor, String projectName) {
		this.projectId = projectId;
		this.projectColor = projectColor;
		this.projectName = projectName;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
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
}
