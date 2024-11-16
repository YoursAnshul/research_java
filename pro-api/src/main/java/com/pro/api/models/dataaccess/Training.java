package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "training")
public class Training {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "trainingid")
	private int trainingId;

	@Column(name = "userid")
	private int userId;

	@Column(name = "dempoid")
	private String dempoid;

	@Column(name = "projectid")
	private int projectId;

	@Column(name = "trainingdate")
	private Date trainingDate;

	@Column(name = "entrydt")
	private Date entryDt;

	@Column(name = "entryby")
	private String entryBy;

	@Column(name = "moddt")
	private Date modDt;

	@Column(name = "modby")
	private String modBy;

	// Default constructor
	public Training() {
	}

	public int getTrainingId() {
		return trainingId;
	}

	public void setTrainingId(int trainingId) {
		this.trainingId = trainingId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getDempoid() {
		return dempoid;
	}

	public void setDempoid(String dempoid) {
		this.dempoid = dempoid;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public Date getTrainingDate() {
		return trainingDate;
	}

	public void setTrainingDate(Date trainingDate) {
		this.trainingDate = trainingDate;
	}

	public Date getEntryDt() {
		return entryDt;
	}

	public void setEntryDt(Date entryDt) {
		this.entryDt = entryDt;
	}

	public String getEntryBy() {
		return entryBy;
	}

	public void setEntryBy(String entryBy) {
		this.entryBy = entryBy;
	}

	public Date getModDt() {
		return modDt;
	}

	public void setModDt(Date modDt) {
		this.modDt = modDt;
	}

	public String getModBy() {
		return modBy;
	}

	public void setModBy(String modBy) {
		this.modBy = modBy;
	}

}
