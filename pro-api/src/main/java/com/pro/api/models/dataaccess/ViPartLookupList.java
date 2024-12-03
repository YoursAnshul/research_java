package com.pro.api.models.dataaccess;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(ViPartLookupId.class)
@Table(name = "vi_partlookuplist")
public class ViPartLookupList {

	@Id 
	private String phonetype;
	@Id 
	private Integer upid;
	//@Id 
	private Integer partcontactsid;
	private String dob;
	private String project;
	private String participantno;
	private String participantfname;
	private String participantlname;
	private String phone;

	private String contactsource;
	private String contactfname;
	private String contactlname;

	public Integer getUpid() {
		return upid;
	}

	public void setUpid(Integer upid) {
		this.upid = upid;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getParticipantfname() {
		return participantfname;
	}

	public void setParticipantfname(String participantfname) {
		this.participantfname = participantfname;
	}

	public String getParticipantlname() {
		return participantlname;
	}

	public void setParticipantlname(String participantlname) {
		this.participantlname = participantlname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhonetype() {
		return phonetype;
	}

	public void setPhonetype(String phonetype) {
		this.phonetype = phonetype;
	}

	public String getContactsource() {
		return contactsource;
	}

	public void setContactsource(String contactsource) {
		this.contactsource = contactsource;
	}

	public String getContactfname() {
		return contactfname;
	}

	public void setContactfname(String contactfname) {
		this.contactfname = contactfname;
	}

	public String getContactlname() {
		return contactlname;
	}

	public void setContactlname(String contactlname) {
		this.contactlname = contactlname;
	}

	public String getParticipantno() {
		return participantno;
	}

	public void setParticipantno(String participantno) {
		this.participantno = participantno;
	}

	public Integer getPartcontactsid() {
		return partcontactsid;
	}

	public void setPartcontactsid(Integer partcontactsid) {
		this.partcontactsid = partcontactsid;
	}

}
