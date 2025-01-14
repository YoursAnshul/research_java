package com.proep.api.models.dataaccess;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "Training")
public class Training {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TrainingID")
	private int trainingId;

	@Column(name = "userid")
	private int userId;

	@Column(name = "dempoid")
	private String dempoid;

	@Column(name = "projectid")
	private int projectId;

	@Column(name = "trainingdate")
	private Date trainingDate;

	@Column(name = "EntryDT")
	private Date entryDt;

	@Column(name = "EntryBy")
	private String entryBy;

	@Column(name = "ModDT")
	private Date modDt;

	@Column(name = "ModBy")
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