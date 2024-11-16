package com.pro.api.models.dataaccess;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects") // Specify the table name here
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "projectid") // Specify the column name for ProjectId
	@JsonProperty("projectid")
	private int projectId;

	@Column(name = "projectname")
	private String projectName;

	@Column(name = "projectleadid")
	private Integer projectLeadId;

	@Column(name = "projectlead")
	private String projectLead;

	@Column(name = "projectcoordinatorid")
	private Integer projectCoordinatorId;

	@Column(name = "projectcoordinator")
	private String projectCoordinator;

	@Column(name = "active")
	private Integer active;

	@Column(name = "projectstatus")
	private String projectStatus;

	@Column(name = "projectdisplayId")
	private String projectDisplayId;

	@Column(name = "interview")
	private Integer interview;

	@Column(name = "projectcolor")
	private String projectColor;

	@Column(name = "projectcolor_iview")
	private String projectColorIview;

	@Column(name = "projectedstartdate")
	private LocalDate projectedStartDate;

	@Column(name = "projectedenddate")
	private LocalDate projectedEndDate;

	@Column(name = "projectempcode")
	private String projectEpmcode;

	@Column(name = "projecttype")
	private Integer projectType;

	@Column(name = "sponsor")
	private String sponsor;

	@Column(name = "principalinvestigator")
	private String principalInvestigator;

	@Column(name = "totalbudget")
	private BigDecimal totalBudget;

	@Column(columnDefinition="text", length=10485760 ,name = "comments")
	private String comments;

	@Column(name = "scheduledisplay")
	private Integer scheduleDisplay;

	@Column(name = "ssdatabasename")
	private String ssdatabaseName;

	@Column(name = "forecast")
	private Integer forecast;

	@Column(name = "tollfreenumber")
	private String tollFreeNumber;

	@Column(name = "studyemailaddress")
	private String studyEmailAddress;

	@Column(name = "pdrivelocation")
	private String pdriveLocation;

	@Column(name = "tdrivelocation")
	private String tdriveLocation;

	@Column(name = "ibrnumber")
	private String irbnumber;

	@Column(name = "fundcode")
	private String fundCode;

	@Column(name = "sistercode")
	private String sisterCode;

	@Column(name = "passthrucode")
	private String passThruCode;

	@Column(name = "rescue")
	private Integer rescue;

	@Column(name = "orphan")
	private Integer orphan;

	@Column(name = "edcsystem")
	private String edcsystem;

	@Column(name = "therapeuticarea")
	private String therapeuticArea;

	@Column(name = "projectabbr")
	private String projectAbbr;

	@Column(name = "projectmanager")
	private String projectManager;

	@Column(name = "FaxNumber")
	private String faxNumber;

	@Column(name = "faxlocation")
	private String faxLocation;

	@Column(name = "maxenrollment")
	private Integer maxEnrollment;

	@Column(name = "firstenrolldate")
	private LocalDate firstEnrollDate;

	@Column(name = "visitdate")
	private String visitDate;

	@Column(name = "totalprojected")
	private Integer totalProjected;

	@Column(name = "actualfollowup")
	private Integer actualFollowUp;

	@Column(name = "numofintervals")
	private Integer numofIntervals;

	@Column(name = "entryformname")
	private String entryFormName;

	@Column(name = "entrydt")
	private LocalDateTime entryDt;

	@Column(name = "entryby")
	private String entryBy;

	@Column(name = "moddt")
	private LocalDateTime modDt;

	@Column(name = "modby")
	private String modBy;

	public Project() {
		
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Integer getProjectLeadId() {
		return projectLeadId;
	}

	public void setProjectLeadId(Integer projectLeadId) {
		this.projectLeadId = projectLeadId;
	}

	public String getProjectLead() {
		return projectLead;
	}

	public void setProjectLead(String projectLead) {
		this.projectLead = projectLead;
	}

	public Integer getProjectCoordinatorId() {
		return projectCoordinatorId;
	}

	public void setProjectCoordinatorId(Integer projectCoordinatorId) {
		this.projectCoordinatorId = projectCoordinatorId;
	}

	public String getProjectCoordinator() {
		return projectCoordinator;
	}

	public void setProjectCoordinator(String projectCoordinator) {
		this.projectCoordinator = projectCoordinator;
	}

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public String getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(String projectStatus) {
		this.projectStatus = projectStatus;
	}

	public String getProjectDisplayId() {
		return projectDisplayId;
	}

	public void setProjectDisplayId(String projectDisplayId) {
		this.projectDisplayId = projectDisplayId;
	}

	public Integer getInterview() {
		return interview;
	}

	public void setInterview(Integer interview) {
		this.interview = interview;
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

	public LocalDate getProjectedStartDate() {
		return projectedStartDate;
	}

	public void setProjectedStartDate(LocalDate projectedStartDate) {
		this.projectedStartDate = projectedStartDate;
	}

	public LocalDate getProjectedEndDate() {
		return projectedEndDate;
	}

	public void setProjectedEndDate(LocalDate projectedEndDate) {
		this.projectedEndDate = projectedEndDate;
	}

	public String getProjectEpmcode() {
		return projectEpmcode;
	}

	public void setProjectEpmcode(String projectEpmcode) {
		this.projectEpmcode = projectEpmcode;
	}

	public Integer getProjectType() {
		return projectType;
	}

	public void setProjectType(Integer projectType) {
		this.projectType = projectType;
	}

	public String getSponsor() {
		return sponsor;
	}

	public void setSponsor(String sponsor) {
		this.sponsor = sponsor;
	}

	public String getPrincipalInvestigator() {
		return principalInvestigator;
	}

	public void setPrincipalInvestigator(String principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	public BigDecimal getTotalBudget() {
		return totalBudget;
	}

	public void setTotalBudget(BigDecimal totalBudget) {
		this.totalBudget = totalBudget;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Integer getScheduleDisplay() {
		return scheduleDisplay;
	}

	public void setScheduleDisplay(Integer scheduleDisplay) {
		this.scheduleDisplay = scheduleDisplay;
	}

	public String getSsdatabaseName() {
		return ssdatabaseName;
	}

	public void setSsdatabaseName(String ssdatabaseName) {
		this.ssdatabaseName = ssdatabaseName;
	}

	public Integer getForecast() {
		return forecast;
	}

	public void setForecast(Integer forecast) {
		this.forecast = forecast;
	}

	public String getTollFreeNumber() {
		return tollFreeNumber;
	}

	public void setTollFreeNumber(String tollFreeNumber) {
		this.tollFreeNumber = tollFreeNumber;
	}

	public String getStudyEmailAddress() {
		return studyEmailAddress;
	}

	public void setStudyEmailAddress(String studyEmailAddress) {
		this.studyEmailAddress = studyEmailAddress;
	}

	public String getPdriveLocation() {
		return pdriveLocation;
	}

	public void setPdriveLocation(String pdriveLocation) {
		this.pdriveLocation = pdriveLocation;
	}

	public String getTdriveLocation() {
		return tdriveLocation;
	}

	public void setTdriveLocation(String tdriveLocation) {
		this.tdriveLocation = tdriveLocation;
	}

	public String getIrbnumber() {
		return irbnumber;
	}

	public void setIrbnumber(String irbnumber) {
		this.irbnumber = irbnumber;
	}

	public String getFundCode() {
		return fundCode;
	}

	public void setFundCode(String fundCode) {
		this.fundCode = fundCode;
	}

	public String getSisterCode() {
		return sisterCode;
	}

	public void setSisterCode(String sisterCode) {
		this.sisterCode = sisterCode;
	}

	public String getPassThruCode() {
		return passThruCode;
	}

	public void setPassThruCode(String passThruCode) {
		this.passThruCode = passThruCode;
	}

	public Integer getRescue() {
		return rescue;
	}

	public void setRescue(Integer rescue) {
		this.rescue = rescue;
	}

	public Integer getOrphan() {
		return orphan;
	}

	public void setOrphan(Integer orphan) {
		this.orphan = orphan;
	}

	public String getEdcsystem() {
		return edcsystem;
	}

	public void setEdcsystem(String edcsystem) {
		this.edcsystem = edcsystem;
	}

	public String getTherapeuticArea() {
		return therapeuticArea;
	}

	public void setTherapeuticArea(String therapeuticArea) {
		this.therapeuticArea = therapeuticArea;
	}

	public String getProjectAbbr() {
		return projectAbbr;
	}

	public void setProjectAbbr(String projectAbbr) {
		this.projectAbbr = projectAbbr;
	}

	public String getProjectManager() {
		return projectManager;
	}

	public void setProjectManager(String projectManager) {
		this.projectManager = projectManager;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public String getFaxLocation() {
		return faxLocation;
	}

	public void setFaxLocation(String faxLocation) {
		this.faxLocation = faxLocation;
	}

	public Integer getMaxEnrollment() {
		return maxEnrollment;
	}

	public void setMaxEnrollment(Integer maxEnrollment) {
		this.maxEnrollment = maxEnrollment;
	}

	public LocalDate getFirstEnrollDate() {
		return firstEnrollDate;
	}

	public void setFirstEnrollDate(LocalDate firstEnrollDate) {
		this.firstEnrollDate = firstEnrollDate;
	}

	public String getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(String visitDate) {
		this.visitDate = visitDate;
	}

	public Integer getTotalProjected() {
		return totalProjected;
	}

	public void setTotalProjected(Integer totalProjected) {
		this.totalProjected = totalProjected;
	}

	public Integer getActualFollowUp() {
		return actualFollowUp;
	}

	public void setActualFollowUp(Integer actualFollowUp) {
		this.actualFollowUp = actualFollowUp;
	}

	public Integer getNumofIntervals() {
		return numofIntervals;
	}

	public void setNumofIntervals(Integer numofIntervals) {
		this.numofIntervals = numofIntervals;
	}

	public String getEntryFormName() {
		return entryFormName;
	}

	public void setEntryFormName(String entryFormName) {
		this.entryFormName = entryFormName;
	}

	public LocalDateTime getEntryDt() {
		return entryDt;
	}

	public void setEntryDt(LocalDateTime entryDt) {
		this.entryDt = entryDt;
	}

	public String getEntryBy() {
		return entryBy;
	}

	public void setEntryBy(String entryBy) {
		this.entryBy = entryBy;
	}

	public LocalDateTime getModDt() {
		return modDt;
	}

	public void setModDt(LocalDateTime modDt) {
		this.modDt = modDt;
	}

	public String getModBy() {
		return modBy;
	}

	public void setModBy(String modBy) {
		this.modBy = modBy;
	}

}
