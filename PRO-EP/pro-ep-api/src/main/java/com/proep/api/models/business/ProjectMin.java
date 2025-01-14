package com.proep.api.models.business;

import java.util.ArrayList;
import java.util.List;

public class ProjectMin {
	private int projectID;
	private String projectName;
	private String projectAbbr;
	private String projectDisplayId;
	private String displayedIn;
	private Boolean active;
	private Boolean projectStatus;
	private String projectColor;
	private String projectType;
	private List<String> trainedOnUsers;
	private List<String> notTrainedOnUsers;

	public ProjectMin() {

	}

	public ProjectMin(int ProjectID, String ProjectName, String ProjectAbbr, String ProjectDisplayId,
			String DisplayedIn, int Active, String ProjectStatus, String ProjectColor, String ProjectType) {
		this.projectID = ProjectID;
		this.projectName = ProjectName;
		this.projectAbbr = ProjectAbbr;
		this.projectDisplayId = ProjectDisplayId;
		this.displayedIn = DisplayedIn;
		this.active = (Active == 1);
		this.projectStatus = (ProjectStatus != null && ProjectStatus.equals("1"));
		this.projectColor = ProjectColor;
		this.projectType = ProjectType;
		this.trainedOnUsers = new ArrayList<>();
		this.notTrainedOnUsers = new ArrayList<>();
	}

	// Getters and Setters

	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int ProjectID) {
		this.projectID = ProjectID;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String ProjectName) {
		this.projectName = ProjectName;
	}

	public String getProjectAbbr() {
		return projectAbbr;
	}

	public void setProjectAbbr(String ProjectAbbr) {
		this.projectAbbr = ProjectAbbr;
	}

	public String getProjectDisplayId() {
		return projectDisplayId;
	}

	public void setProjectDisplayId(String ProjectDisplayId) {
		this.projectDisplayId = ProjectDisplayId;
	}

	public String getDisplayedIn() {
		return displayedIn;
	}

	public void setDisplayedIn(String DisplayedIn) {
		this.displayedIn = DisplayedIn;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean Active) {
		this.active = Active;
	}

	public Boolean getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(Boolean ProjectStatus) {
		this.projectStatus = ProjectStatus;
	}

	public String getProjectColor() {
		return projectColor;
	}

	public void setProjectColor(String ProjectColor) {
		this.projectColor = ProjectColor;
	}

	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String ProjectType) {
		this.projectType = ProjectType;
	}

	public List<String> getTrainedOnUsers() {
		return trainedOnUsers;
	}

	public void setTrainedOnUsers(List<String> TrainedOnUsers) {
		this.trainedOnUsers = TrainedOnUsers;
	}

	public List<String> getNotTrainedOnUsers() {
		return notTrainedOnUsers;
	}

	public void setNotTrainedOnUsers(List<String> NotTrainedOnUsers) {
		this.notTrainedOnUsers = NotTrainedOnUsers;
	}
}
