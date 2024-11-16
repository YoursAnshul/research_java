package com.pro.api.models.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserMin {
	private short Userid;
	private String Dempoid;
	private String Fname;
	private String Lname;
	private String Preferredfname;
	private String Preferredlname;
	private String Language;
	private String Trainedon;
	private List<String> TrainedOnArray;
	private Boolean Active;
	private Boolean CanEdit;
	private String DisplayName;
	private Integer Role;
	private Boolean Buddy;
	private Short Employmenttype;
	private Integer Schedulinglevel;

	public UserMin() {
		TrainedOnArray = new ArrayList<>();
	}

	public UserMin(short _userid, String _dempoid, String _fname, String _lname, String _language, String _trainedon,
                   Boolean _active, Boolean _canEdit, String _preferredfname, String _preferredlname, Integer _role,
                   Boolean _buddy, Short _employmenttype, Integer _schedulinglevel) {
		this.Userid = _userid;
		this.Dempoid = _dempoid;
		this.Fname = _fname;
		this.Lname = _lname;
		this.Language = _language;
		this.Trainedon = _trainedon;
		this.TrainedOnArray = new ArrayList<>();
		if (_trainedon != null) {
			if (_trainedon.contains("|")) {
				this.TrainedOnArray = Arrays.asList(_trainedon.split("\\|"));
			} else if (_trainedon.length() > 0) {
				this.TrainedOnArray.add(_trainedon);
			}
		}
		this.Active = _active;
		this.CanEdit = _canEdit;
		this.Preferredfname = _preferredfname;
		this.Preferredlname = _preferredlname;
		this.DisplayName = String.format("%s %s", (!isNullOrWhiteSpace(Preferredfname) ? Preferredfname : Fname),
				(!isNullOrWhiteSpace(Preferredlname) ? Preferredlname : Lname));
		this.Role = _role;
		this.Buddy = _buddy;
		this.Employmenttype = _employmenttype;
		this.Schedulinglevel = _schedulinglevel;
	}

	private static boolean isNullOrWhiteSpace(String str) {
		return str == null || str.trim().isEmpty();
	}

	public short getUserid() {
		return Userid;
	}

	public void setUserid(short userid) {
		Userid = userid;
	}

	public String getDempoid() {
		return Dempoid;
	}

	public void setDempoid(String dempoid) {
		Dempoid = dempoid;
	}

	public String getFname() {
		return Fname;
	}

	public void setFname(String fname) {
		Fname = fname;
	}

	public String getLname() {
		return Lname;
	}

	public void setLname(String lname) {
		Lname = lname;
	}

	public String getPreferredfname() {
		return Preferredfname;
	}

	public void setPreferredfname(String preferredfname) {
		Preferredfname = preferredfname;
	}

	public String getPreferredlname() {
		return Preferredlname;
	}

	public void setPreferredlname(String preferredlname) {
		Preferredlname = preferredlname;
	}

	public String getLanguage() {
		return Language;
	}

	public void setLanguage(String language) {
		Language = language;
	}

	public String getTrainedon() {
		return Trainedon;
	}

	public void setTrainedon(String trainedon) {
		Trainedon = trainedon;
	}

	public List<String> getTrainedOnArray() {
		return TrainedOnArray;
	}

	public void setTrainedOnArray(List<String> trainedOnArray) {
		TrainedOnArray = trainedOnArray;
	}

	public Boolean getActive() {
		return Active;
	}

	public void setActive(Boolean active) {
		Active = active;
	}

	public Boolean getCanEdit() {
		return CanEdit;
	}

	public void setCanEdit(Boolean canEdit) {
		CanEdit = canEdit;
	}

	public String getDisplayName() {
		return DisplayName;
	}

	public void setDisplayName(String displayName) {
		DisplayName = displayName;
	}

	public Integer getRole() {
		return Role;
	}

	public void setRole(Integer role) {
		Role = role;
	}

	public Boolean getBuddy() {
		return Buddy;
	}

	public void setBuddy(Boolean buddy) {
		Buddy = buddy;
	}

	public Short getEmploymenttype() {
		return Employmenttype;
	}

	public void setEmploymenttype(Short employmenttype) {
		Employmenttype = employmenttype;
	}

	public Integer getSchedulinglevel() {
		return Schedulinglevel;
	}

	public void setSchedulinglevel(Integer schedulinglevel) {
		Schedulinglevel = schedulinglevel;
	}
}
