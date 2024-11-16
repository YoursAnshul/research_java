package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
	private String status;

	@Column(name = "active")
	private Boolean active;

	@Column(name = "corehours")
	private Short corehours;

	@Column(name = "employmenttype")
	private Short employmenttype;

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

	@Column(name = "uinit")
	private String uinit;

	@Column(name = "role")
	private Integer role;

	@Column(name = "schedulinglevel")
	private Integer schedulinglevel;

	@Column(name = "scheduledisplay")
	private Integer scheduledisplay;

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

	@Column(name = "bypasslockout")
	private Integer bypasslockout;

	@Column(name = "aislenumber")
	private Short aislenumber;

	@Column(name = "teamgroup")
	private String teamgroup;

	@Column(name = "workgroup")
	private String workgroup;

	@Column(name = "cubeoffice")
	private Short cubeoffice;

	@Column(name = "Spanish")
	private Integer spanish;

	@Column(name = "language")
	private String language;

	@Column(name = "canedit")
	private Boolean canedit;

	@Column(name = "trainedon")
	private String trainedon;

	@Column(name = "citidate")
	private LocalDate citidate;

	@Column(name = "introemail")
	private Boolean introemail;

	@Column(name = "introemaildate")
	private LocalDate introemaildate;

	@Column(name = "phonescreen")
	private Boolean phonescreen;

	@Column(name = "phonescreendate")
	private LocalDate phonescreendate;

	@Column(name = "facetoface")
	private Boolean facetoface;

	@Column(name = "facetofacedate")
	private LocalDate facetofacedate;

	@Column(name = "welcomeemail")
	private Boolean welcomeemail;

	@Column(name = "welcomeemaildate")
	private LocalDate welcomeemaildate;

	@Column(name = "orientationdate")
	private LocalDate orientationdate;

	@Column(name = "referral")
	private String referral;

	@Column(name = "tempagency")
	private String tempagency;

	@Lob
	@Column(columnDefinition="bytea" ,name = "userimage")
	private byte[] userImage;

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

	@Column(name = "hiatusstartdate")
	private LocalDate hiatusstartdate;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Short getCorehours() {
		return corehours;
	}

	public void setCorehours(Short corehours) {
		this.corehours = corehours;
	}

	public Short getEmploymenttype() {
		return employmenttype;
	}

	public void setEmploymenttype(Short employmenttype) {
		this.employmenttype = employmenttype;
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

	public String getUinit() {
		return uinit;
	}

	public void setUinit(String uinit) {
		this.uinit = uinit;
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

	public Integer getScheduledisplay() {
		return scheduledisplay;
	}

	public void setScheduledisplay(Integer scheduledisplay) {
		this.scheduledisplay = scheduledisplay;
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

	public Integer getBypasslockout() {
		return bypasslockout;
	}

	public void setBypasslockout(Integer bypasslockout) {
		this.bypasslockout = bypasslockout;
	}

	public Short getAislenumber() {
		return aislenumber;
	}

	public void setAislenumber(Short aislenumber) {
		this.aislenumber = aislenumber;
	}

	public String getTeamgroup() {
		return teamgroup;
	}

	public void setTeamgroup(String teamgroup) {
		this.teamgroup = teamgroup;
	}

	public String getWorkgroup() {
		return workgroup;
	}

	public void setWorkgroup(String workgroup) {
		this.workgroup = workgroup;
	}

	public Short getCubeoffice() {
		return cubeoffice;
	}

	public void setCubeoffice(Short cubeoffice) {
		this.cubeoffice = cubeoffice;
	}

	public Integer getSpanish() {
		return spanish;
	}

	public void setSpanish(Integer spanish) {
		this.spanish = spanish;
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

	public LocalDate getCitidate() {
		return citidate;
	}

	public void setCitidate(LocalDate citidate) {
		this.citidate = citidate;
	}

	public Boolean getIntroemail() {
		return introemail;
	}

	public void setIntroemail(Boolean introemail) {
		this.introemail = introemail;
	}

	public LocalDate getIntroemaildate() {
		return introemaildate;
	}

	public void setIntroemaildate(LocalDate introemaildate) {
		this.introemaildate = introemaildate;
	}

	public Boolean getPhonescreen() {
		return phonescreen;
	}

	public void setPhonescreen(Boolean phonescreen) {
		this.phonescreen = phonescreen;
	}

	public LocalDate getPhonescreendate() {
		return phonescreendate;
	}

	public void setPhonescreendate(LocalDate phonescreendate) {
		this.phonescreendate = phonescreendate;
	}

	public Boolean getFacetoface() {
		return facetoface;
	}

	public void setFacetoface(Boolean facetoface) {
		this.facetoface = facetoface;
	}

	public LocalDate getFacetofacedate() {
		return facetofacedate;
	}

	public void setFacetofacedate(LocalDate facetofacedate) {
		this.facetofacedate = facetofacedate;
	}

	public Boolean getWelcomeemail() {
		return welcomeemail;
	}

	public void setWelcomeemail(Boolean welcomeemail) {
		this.welcomeemail = welcomeemail;
	}

	public LocalDate getWelcomeemaildate() {
		return welcomeemaildate;
	}

	public void setWelcomeemaildate(LocalDate welcomeemaildate) {
		this.welcomeemaildate = welcomeemaildate;
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

	public byte[] getUserImage() {
		return userImage;
	}

	public void setUserImage(byte[] userImage) {
		this.userImage = userImage;
	}

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

	public LocalDate getHiatusstartdate() {
		return hiatusstartdate;
	}

	public void setHiatusstartdate(LocalDate hiatusstartdate) {
		this.hiatusstartdate = hiatusstartdate;
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

}
