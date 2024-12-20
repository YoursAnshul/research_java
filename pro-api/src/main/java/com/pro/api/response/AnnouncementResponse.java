package com.pro.api.response;

import java.util.Date;
import java.util.List;

public class AnnouncementResponse {

	private String icon;
	private String title;
	private String bodyText;
	private Long authorId;
	private Boolean isAuthor;
	private Date startDate;
	private Date expireDate;
	private List<Long> projectIds;
	private Long announcementId;
	private List<String> projectNames;
	private String authorName;
	private List<String> projectColor;
	private List<ProjectResponse> projectObject;
	private Boolean isAnyProjectsSelected;
	

	public Boolean getIsAnyProjectsSelected() {
		return isAnyProjectsSelected;
	}

	public void setIsAnyProjectsSelected(Boolean isAnyProjectsSelected) {
		this.isAnyProjectsSelected = isAnyProjectsSelected;
	}

	public List<ProjectResponse> getProjectObject() {
		return projectObject;
	}

	public void setProjectObject(List<ProjectResponse> projectObject) {
		this.projectObject = projectObject;
	}

	public List<String> getProjectColor() {
		return projectColor;
	}

	public void setProjectColor(List<String> projectColor) {
		this.projectColor = projectColor;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public Boolean getIsAuthor() {
		return isAuthor;
	}

	public void setIsAuthor(Boolean isAuthor) {
		this.isAuthor = isAuthor;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public List<Long> getProjectIds() {
		return projectIds;
	}

	public void setProjectIds(List<Long> projectIds) {
		this.projectIds = projectIds;
	}

	public Long getAnnouncementId() {
		return announcementId;
	}

	public void setAnnouncementId(Long announcementId) {
		this.announcementId = announcementId;
	}

	public List<String> getProjectNames() {
		return projectNames;
	}

	public void setProjectNames(List<String> projectNames) {
		this.projectNames = projectNames;
	}

}
