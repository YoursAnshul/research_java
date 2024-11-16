package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "schedules")
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "preschedulekey")
	private int preschedulekey;

	@Column(name = "dempoid")
	private String dempoid;

	@Column(name = "scheduledate")
	private LocalDate scheduledate;

	@Column(columnDefinition="text",name = "status")
	private String status;

	@Column(name = "projectid")
	private Integer projectid;

	@Column(name = "comments")
	private String comments;

	@Column(columnDefinition="text",name = "entryby")
	private String entryBy;

	@Column(name = "entrydt")
	private LocalDate entryDt;

	@Column(columnDefinition="text",name = "modby")
	private String modBy;

	@Column(name = "moddt")
	private LocalDateTime modDt;

	@Column(columnDefinition="text", name = "machinename")
	private String machineName;

	@Column(name = "startdatetime")
	private OffsetDateTime startdatetime;

	@Column(name = "enddatetime")
	private OffsetDateTime enddatetime;

	public Schedule() {
		
	}

	public int getPreschedulekey() {
		return preschedulekey;
	}

	public void setPreschedulekey(int preschedulekey) {
		this.preschedulekey = preschedulekey;
	}

	public String getDempoid() {
		return dempoid;
	}

	public void setDempoid(String dempoid) {
		this.dempoid = dempoid;
	}

	public LocalDate getScheduledate() {
		return scheduledate;
	}

	public void setScheduledate(LocalDate scheduledate) {
		this.scheduledate = scheduledate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getProjectid() {
		return projectid;
	}

	public void setProjectid(Integer projectid) {
		this.projectid = projectid;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getEntryBy() {
		return entryBy;
	}

	public void setEntryBy(String entryBy) {
		this.entryBy = entryBy;
	}

	public LocalDate getEntryDt() {
		return entryDt;
	}

	public void setEntryDt(LocalDate entryDt) {
		this.entryDt = entryDt;
	}

	public String getModBy() {
		return modBy;
	}

	public void setModBy(String modBy) {
		this.modBy = modBy;
	}

	public LocalDateTime getModDt() {
		return modDt;
	}

	public void setModDt(LocalDateTime modDt) {
		this.modDt = modDt;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public OffsetDateTime getStartdatetime() {
		return startdatetime;
	}

	public void setStartdatetime(OffsetDateTime startdatetime) {
		this.startdatetime = startdatetime;
	}

	public OffsetDateTime getEnddatetime() {
		return enddatetime;
	}

	public void setEnddatetime(OffsetDateTime enddatetime) {
		this.enddatetime = enddatetime;
	}
}
