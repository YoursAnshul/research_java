package com.proep.api.models.dataaccess;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vi_ProjectTotals")
public class ViProjectTotal {
	
	// @Id
	// @GeneratedValue(strategy = GenerationType.IDENTITY)
	// @Column(name = "ProjectTotalId")
	// private Integer projectTotalId;
	@EmbeddedId
	private ProjectTotalId projectTotalId;

	@Column(name = "projectid")
	private Integer projectid;

	@Column(name = "ProjectName")
	private String projectName;

	@Column(name = "scheduledate")
	private String scheduledate;

	@Column(name = "DayNumber")
	private Integer dayNumber;

	@Column(name = "WeekNumber")
	private Integer weekNumber;

	@Column(name = "YearNumber")
	private Integer yearNumber;

	@Column(name = "startdatetime")
	private OffsetDateTime startdatetime;

	@Column(name = "enddatetime")
	private OffsetDateTime enddatetime;

	@Column(name = "TotalHours")
	private BigDecimal totalHours;

	public ViProjectTotal() {

	}

	public Integer getProjectid() {
		return projectid;
	}

	public void setProjectid(Integer projectid) {
		this.projectid = projectid;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getScheduledate() {
		return scheduledate;
	}

	public void setScheduledate(String scheduledate) {
		this.scheduledate = scheduledate;
	}

	public Integer getDayNumber() {
		return dayNumber;
	}

	public void setDayNumber(Integer dayNumber) {
		this.dayNumber = dayNumber;
	}

	public Integer getWeekNumber() {
		return weekNumber;
	}

	public void setWeekNumber(Integer weekNumber) {
		this.weekNumber = weekNumber;
	}

	public Integer getYearNumber() {
		return yearNumber;
	}

	public void setYearNumber(Integer yearNumber) {
		this.yearNumber = yearNumber;
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

	public BigDecimal getTotalHours() {
		return totalHours;
	}

	public void setTotalHours(BigDecimal totalHours) {
		this.totalHours = totalHours;
	}

}
