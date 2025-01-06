package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "userid")
	private short userid;

	@Column(name = "dempoid")
	private String dempoid;

	@Column(name = "status")
	private Integer status;

	@Column(name = "corehours")
	private Short corehours;

	@Column(name = "empstatus")
	private Short empstatus;

	@Column(name = "fname")
	private String fname;

	@Column(name = "lname")
	private String lname;

	@Column(name = "permstartdate")
	private LocalDate permstartdate;

	@Column(name = "permenddate")
	private LocalDate permenddate;

	@Column(name = "tempstartdate")
	private LocalDate tempstartdate;

	@Column(name = "tempenddate")
	private LocalDate tempenddate;

	@Column(name = "role")
	private Integer role;

	@Column(name = "schedulinglevel")
	private Integer schedulinglevel;

	@Column(name = "title")
	private String title;

	@Column(name = "defaultproject")
	private Integer defaultproject;

	@Column(name = "phonenumber")
	private String phonenumber;

	@Column(name = "phonenumber1")
	private String phonenumber1;

	@Column(name = "phonenumber2")
	private String phonenumber2;

	@Column(name = "emailaddr")
	private String emailaddr;

	@Column(name = "emailaddr1")
	private String emailaddr1;

	@Column(name = "cubeoffice")
	private Short cubeoffice;

	@Column(name = "language")
	private String language;

	@Column(name = "canedit")
	private Boolean canedit;

	@Column(name = "trainedon")
	private String trainedon;

	@Column(name = "facetofacedate")
	private LocalDate facetofacedate;

	@Column(name = "orientationdate")
	private LocalDate orientationdate;
	
	@Column(name = "referral")
	private String referral;

	@Column(name = "tempagency")
	private String tempagency;

	// @Lob
	// @Column(name = "userimage")
	// private String userImage;

	@Column(columnDefinition="text", length=10485760 ,name = "notes")
	private String notes;

	@Column(name = "emercontactnumber")
	private String emercontactnumber;

	@Column(name = "emercontactnumber2")
	private String emercontactnumber2;

	@Column(name = "emercontactname")
	private String emercontactname;

	@Column(name = "emercontactname2")
	private String emercontactname2;

	@Column(name = "emercontactrel")
	private String emercontactrel;

	@Column(name = "emercontactrel2")
	private String emercontactrel2;

	@Column(name = "dob")
	private LocalDate dob;

	@Column(name = "state")
	private String state;

	@Column(name = "city")
	private String city;

	@Column(name = "zipcode")
	private String zipcode;

	@Column(name = "homeaddress")
	private String homeaddress;

	@Column(name = "phonenumber3")
	private String phonenumber3;

	@Column(name = "computernumber")
	private String computernumber;

	@Column(name = "badgeidnumber")
	private String badgeidnumber;

	@Column(name = "acdagentnumber")
	private String acdagentnumber;

	@Column(name = "uniqueid")
	private String uniqueid;

	@Column(name = "preferredfname")
	private String preferredfname;

	@Column(name = "preferredlname")
	private String preferredlname;

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

	@Column(name = "buddy")
	private Boolean buddy;
	
	@Column(name = "active")
	private Boolean active;
	
	@Column(name = "certification")
	private LocalDate certification;
	
	@Column(name = "miscellaneous")
	private LocalDate miscellaneous;
	
	@Column(columnDefinition="text", length=10485760 ,name = "certnotes")
	private String certnotes;
	
	@Column(name = "manager")
	private String manager;
	
	public User() {

	}

	public short getUserid() {
		return userid;
	}

	public void setUserid(short userid) {
		this.userid = userid;
	}

	public String getDempoid() {
		return dempoid;
	}

	public void setDempoid(String dempoid) {
		this.dempoid = dempoid;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Short getCorehours() {
		return corehours;
	}

	public void setCorehours(Short corehours) {
		this.corehours = corehours;
	}

	public Short getEmpstatus() {
		return empstatus;
	}

	public void setEmpstatus(Short empstatus) {
		this.empstatus = empstatus;
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

	public LocalDate getPermstartdate() {
		return permstartdate;
	}

	public void setPermstartdate(LocalDate permstartdate) {
		this.permstartdate = permstartdate;
	}

	public LocalDate getPermenddate() {
		return permenddate;
	}

	public void setPermenddate(LocalDate permenddate) {
		this.permenddate = permenddate;
	}

	public LocalDate getTempstartdate() {
		return tempstartdate;
	}

	public void setTempstartdate(LocalDate tempstartdate) {
		this.tempstartdate = tempstartdate;
	}

	public LocalDate getTempenddate() {
		return tempenddate;
	}

	public void setTempenddate(LocalDate tempenddate) {
		this.tempenddate = tempenddate;
	}

	public Integer getRole() {
		return role;
	}

	public void setRole(Integer role) {
		this.role = role;
	}

	public Integer getSchedulinglevel() {
		return schedulinglevel;
	}

	public void setSchedulinglevel(Integer schedulinglevel) {
		this.schedulinglevel = schedulinglevel;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getDefaultproject() {
		return defaultproject;
	}

	public void setDefaultproject(Integer defaultproject) {
		this.defaultproject = defaultproject;
	}

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public String getPhonenumber1() {
		return phonenumber1;
	}

	public void setPhonenumber1(String phonenumber1) {
		this.phonenumber1 = phonenumber1;
	}

	public String getPhonenumber2() {
		return phonenumber2;
	}

	public void setPhonenumber2(String phonenumber2) {
		this.phonenumber2 = phonenumber2;
	}

	public String getEmailaddr() {
		return emailaddr;
	}

	public void setEmailaddr(String emailaddr) {
		this.emailaddr = emailaddr;
	}

	public String getEmailaddr1() {
		return emailaddr1;
	}

	public void setEmailaddr1(String emailaddr1) {
		this.emailaddr1 = emailaddr1;
	}

	public Short getCubeoffice() {
		return cubeoffice;
	}

	public void setCubeoffice(Short cubeoffice) {
		this.cubeoffice = cubeoffice;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Boolean getCanedit() {
		return canedit;
	}

	public void setCanedit(Boolean canedit) {
		this.canedit = canedit;
	}

	public String getTrainedon() {
		return trainedon;
	}

	public void setTrainedon(String trainedon) {
		this.trainedon = trainedon;
	}

	public LocalDate getFacetofacedate() {
		return facetofacedate;
	}

	public void setFacetofacedate(LocalDate facetofacedate) {
		this.facetofacedate = facetofacedate;
	}

	public LocalDate getOrientationdate() {
		return orientationdate;
	}

	public void setOrientationdate(LocalDate orientationdate) {
		this.orientationdate = orientationdate;
	}

	public String getReferral() {
		return referral;
	}

	public void setReferral(String referral) {
		this.referral = referral;
	}

	public String getTempagency() {
		return tempagency;
	}

	public void setTempagency(String tempagency) {
		this.tempagency = tempagency;
	}

	// public byte[] getUserImage() {
	// 	return Base64.getDecoder().decode(userImage);
	// }

	// public void setUserImage(byte[] userImage) {
	// 	this.userImage = Base64.getEncoder().encodeToString(userImage);
	// }

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getEmercontactnumber() {
		return emercontactnumber;
	}

	public void setEmercontactnumber(String emercontactnumber) {
		this.emercontactnumber = emercontactnumber;
	}

	public String getEmercontactnumber2() {
		return emercontactnumber2;
	}

	public void setEmercontactnumber2(String emercontactnumber2) {
		this.emercontactnumber2 = emercontactnumber2;
	}

	public String getEmercontactname() {
		return emercontactname;
	}

	public void setEmercontactname(String emercontactname) {
		this.emercontactname = emercontactname;
	}

	public String getEmercontactname2() {
		return emercontactname2;
	}

	public void setEmercontactname2(String emercontactname2) {
		this.emercontactname2 = emercontactname2;
	}

	public String getEmercontactrel() {
		return emercontactrel;
	}

	public void setEmercontactrel(String emercontactrel) {
		this.emercontactrel = emercontactrel;
	}

	public String getEmercontactrel2() {
		return emercontactrel2;
	}

	public void setEmercontactrel2(String emercontactrel2) {
		this.emercontactrel2 = emercontactrel2;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getHomeaddress() {
		return homeaddress;
	}

	public void setHomeaddress(String homeaddress) {
		this.homeaddress = homeaddress;
	}

	public String getPhonenumber3() {
		return phonenumber3;
	}

	public void setPhonenumber3(String phonenumber3) {
		this.phonenumber3 = phonenumber3;
	}

	public String getComputernumber() {
		return computernumber;
	}

	public void setComputernumber(String computernumber) {
		this.computernumber = computernumber;
	}

	public String getBadgeidnumber() {
		return badgeidnumber;
	}

	public void setBadgeidnumber(String badgeidnumber) {
		this.badgeidnumber = badgeidnumber;
	}

	public String getAcdagentnumber() {
		return acdagentnumber;
	}

	public void setAcdagentnumber(String acdagentnumber) {
		this.acdagentnumber = acdagentnumber;
	}

	public String getUniqueid() {
		return uniqueid;
	}

	public void setUniqueid(String uniqueid) {
		this.uniqueid = uniqueid;
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

	public Boolean getBuddy() {
		return buddy;
	}

	public void setBuddy(Boolean buddy) {
		this.buddy = buddy;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public LocalDate getCertification() {
		return certification;
	}

	public void setCertification(LocalDate certification) {
		this.certification = certification;
	}

	public LocalDate getMiscellaneous() {
		return miscellaneous;
	}

	public void setMiscellaneous(LocalDate miscellaneous) {
		this.miscellaneous = miscellaneous;
	}

	public String getCertnotes() {
		return certnotes;
	}

	public void setCertnotes(String certnotes) {
		this.certnotes = certnotes;
	}
	
	public String getManager() {
		return this.manager;
	}
	
	public void setManager(String manager) {
		this.manager = manager;
	}
	
}
