package com.pro.api.models.dataaccess;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "vi_userschedules")
public class ViUserSchedule {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "preschedulekey")
	private int preschedulekey;

	@Column(name = "dempoid")
	private String dempoid;

	@Column(name = "fname")
	private String fname;

	@Column(name = "lname")
	private String lname;

	@Column(name = "preferredfname")
	private String preferredfname;

	@Column(name = "preferredlname")
	private String preferredlname;

	@Column(name = "displayname")
	private String displayName;

	@Column(name = "projectid")
	private Integer projectid;

	@Column(name = "projectname")
	private String projectName;

	@Column(name = "projectcolor")
	private String projectColor;

	@Column(name = "projectcolor_iview")
	private String projectColorIview;

	@Column(name = "scheduledate")
    private LocalDate scheduledate;

	@Column(name = "comments")
	private String comments;

	@Column(name = "startdatetime")
	private OffsetDateTime startdatetime;

	@Column(name = "enddatetime")
	private OffsetDateTime enddatetime;

	@Column(name = "daynumber")
	private Integer dayNumber;

	@Column(name = "weeknumber")
	private Integer weekNumber;

	@Column(name = "weekstart")
	private LocalDateTime weekStart;

	@Column(name = "weekend")
	private LocalDateTime weekEnd;

	@Column(name = "dayofweek")
	private Integer dayOfWeek;

	@Column(name = "monthnumber")
	private Integer monthNumber;

	@Column(name = "month")
	private String month;

	@Column(name = "scheduleyear")
	private Integer scheduleyear;

	@Column(name = "scheduledhours")
	private Double scheduledHours;

	@Column(name = "expr1")
	private String expr1;

	@Column(name = "requestid")
	private Integer requestId;

	@Column(name = "requestdetails")
	private String requestDetails;

	@Column(name = "requestcodeid")
	private Integer requestCodeId;

	@Column(name = "requestcode")
	private String requestCode;

	@Column(name = "userid")
	private short userid;

	@Column(name = "trainedon")
	private String trainedon;

	@Column(name = "language")
	private String language;

	@Column(columnDefinition="text", length=10485760 ,name = "entryby")
	private String entryBy;

	@Column(name = "entrydt")
	private LocalDate entryDt;

	public ViUserSchedule() {

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

	public String getPreferredfname() {
		return preferredfname;
	}

	public void setPreferredfname(String preferredfname) {
		this.preferredfname = preferredfname;
	}

	public String getPreferredlname() {
		return preferredlname;
	}

	public void setPreferredlname(String preferredlname) {
		this.preferredlname = preferredlname;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Integer getProjectid() {
		return projectid;
	}

	public void setProjectid(Integer projectId) {
		this.projectid = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectColor() {
		return projectColor;
	}

	public void setProjectColor(String projectColor) {
		this.projectColor = projectColor;
	}

	public String getProjectColorIview() {
		return projectColorIview;
	}

	public void setProjectColorIview(String projectColorIview) {
		this.projectColorIview = projectColorIview;
	}

	public LocalDate getScheduledate() {
		return scheduledate;
	}

	public void setScheduledate(LocalDate scheduledate) {
		this.scheduledate = scheduledate;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
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

	public LocalDateTime getWeekStart() {
		return weekStart;
	}

	public void setWeekStart(LocalDateTime weekStart) {
		this.weekStart = weekStart;
	}

	public LocalDateTime getWeekEnd() {
		return weekEnd;
	}

	public void setWeekEnd(LocalDateTime weekEnd) {
		this.weekEnd = weekEnd;
	}

	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public Integer getMonthNumber() {
		return monthNumber;
	}

	public void setMonthNumber(Integer monthNumber) {
		this.monthNumber = monthNumber;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public Integer getScheduleyear() {
		return scheduleyear;
	}

	public void setScheduleyear(Integer scheduleyear) {
		this.scheduleyear = scheduleyear;
	}

	public Double getScheduledHours() {
		return scheduledHours;
	}

	public void setScheduledHours(Double scheduledHours) {
		this.scheduledHours = scheduledHours;
	}

	public String getExpr1() {
		return expr1;
	}

	public void setExpr1(String expr1) {
		this.expr1 = expr1;
	}

	public Integer getRequestId() {
		return requestId;
	}

	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}

	public String getRequestDetails() {
		return requestDetails;
	}

	public void setRequestDetails(String requestDetails) {
		this.requestDetails = requestDetails;
	}

	public Integer getRequestCodeId() {
		return requestCodeId;
	}

	public void setRequestCodeId(Integer requestCodeId) {
		this.requestCodeId = requestCodeId;
	}

	public String getRequestCode() {
		return requestCode;
	}

	public void setRequestCode(String requestCode) {
		this.requestCode = requestCode;
	}

	public short getUserid() {
		return userid;
	}

	public void setUserid(short userid) {
		this.userid = userid;
	}

	public String getTrainedon() {
		return trainedon;
	}

	public void setTrainedon(String trainedon) {
		this.trainedon = trainedon;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
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

}
