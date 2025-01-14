package com.proep.api.models.dataaccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Projects") // Specify the table name here
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ProjectID") // Specify the column name for ProjectId
	@JsonProperty("projectID")
	private int projectId;

	@Column(name = "ProjectName")
	private String projectName;

	@Column(name = "ProjectLeadID")
	private Integer projectLeadId;

	@Column(name = "ProjectLead")
	private String projectLead;

	@Column(name = "ProjectCoordinatorID")
	private Integer projectCoordinatorId;

	@Column(name = "ProjectCoordinator")
	private String projectCoordinator;

	@Column(name = "Active")
	private Integer active;

	@Column(name = "ProjectStatus")
	private String projectStatus;

	@Column(name = "ProjectDisplayId")
	private String projectDisplayId;

	@Column(name = "Interview")
	private Integer interview;

	@Column(name = "ProjectColor")
	private String projectColor;

	@Column(name = "ProjectColor_iview")
	private String projectColorIview;

	@Column(name = "ProjectedStartDate")
	private LocalDate projectedStartDate;

	@Column(name = "ProjectedEndDate")
	private LocalDate projectedEndDate;

	@Column(name = "ProjectEPMCode")
	private String projectEpmcode;

	@Column(name = "ProjectType")
	private Integer projectType;

	@Column(name = "Sponsor")
	private String sponsor;

	@Column(name = "PrincipalInvestigator")
	private String principalInvestigator;

	@Column(name = "TotalBudget")
	private BigDecimal totalBudget;

	@Column(name = "Comments")
	private String comments;

	@Column(name = "ScheduleDisplay")
	private Integer scheduleDisplay;

	@Column(name = "SSDatabaseName")
	private String ssdatabaseName;

	@Column(name = "Forecast")
	private Integer forecast;

	@Column(name = "TollFreeNumber")
	private String tollFreeNumber;

	@Column(name = "StudyEmailAddress")
	private String studyEmailAddress;

	@Column(name = "PDriveLocation")
	private String pdriveLocation;

	@Column(name = "TDriveLocation")
	private String tdriveLocation;

	@Column(name = "IRBNumber")
	private String irbnumber;

	@Column(name = "FundCode")
	private String fundCode;

	@Column(name = "SisterCode")
	private String sisterCode;

	@Column(name = "PassThruCode")
	private String passThruCode;

	@Column(name = "Rescue")
	private Integer rescue;

	@Column(name = "Orphan")
	private Integer orphan;

	@Column(name = "EDCSystem")
	private String edcsystem;

	@Column(name = "TherapeuticArea")
	private String therapeuticArea;

	@Column(name = "ProjectAbbr")
	private String projectAbbr;

	@Column(name = "ProjectManager")
	private String projectManager;

	@Column(name = "FaxNumber")
	private String faxNumber;

	@Column(name = "FaxLocation")
	private String faxLocation;

	@Column(name = "MaxEnrollment")
	private Integer maxEnrollment;

	@Column(name = "FirstEnrollDate")
	private LocalDate firstEnrollDate;

	@Column(name = "VisitDate")
	private String visitDate;

	@Column(name = "TotalProjected")
	private Integer totalProjected;

	@Column(name = "ActualFollowUp")
	private Integer actualFollowUp;

	@Column(name = "NumofIntervals")
	private Integer numofIntervals;

	@Column(name = "EntryFormName")
	private String entryFormName;

	@Column(name = "EntryDT")
	private LocalDateTime entryDt;

	@Column(name = "EntryBy")
	private String entryBy;

	@Column(name = "ModDT")
	private LocalDateTime modDt;

	@Column(name = "ModBy")
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