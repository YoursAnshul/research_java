package com.pro.api.response;

public class ProjectResponse {

	private Long projectId;
	private String projectColor;
	private String projectName;
	private Integer projectType;
	private Boolean active;
	private Long defualtProject;
	private String dempoId;

	public String getDempoId() {
		return dempoId;
	}

	public void setDempoId(String dempoId) {
		this.dempoId = dempoId;
	}

	public Long getDefualtProject() {
		return defualtProject;
	}

	public void setDefualtProject(Long defualtProject) {
		this.defualtProject = defualtProject;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
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

	public Integer getProjectType() {
		return projectType;
	}

	public void setProjectType(Integer projectType) {
		this.projectType = projectType;
	}

}
