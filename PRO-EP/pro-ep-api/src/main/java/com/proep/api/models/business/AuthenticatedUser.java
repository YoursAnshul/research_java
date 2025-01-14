package com.proep.api.models.business;

import com.proep.api.models.dataaccess.User;

public class AuthenticatedUser {
	public String displayName;
	public String duDukeID;
	public String eppn;
	public String netID;
	public Boolean interviewer;
	public Boolean resourceGroup;
	public Boolean admin;
	public String[] userRoles;

	public AuthenticatedUser() {
	}

	public AuthenticatedUser(User user) {
		setDisplayName(String.format("%s %s", user.getFname(), user.getLname()));
		setDuDukeID(user.getUniqueid());
		setEppn(user.getEmailaddr());
		setNetID(user.getDempoid());
	}

	public String[] getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(String[] userRoles) {
		this.userRoles = userRoles;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDuDukeID() {
		return duDukeID;
	}

	public void setDuDukeID(String duDukeID) {
		this.duDukeID = duDukeID;
	}

	public String getEppn() {
		return eppn;
	}

	public void setEppn(String eppn) {
		this.eppn = eppn;
	}

	public String getNetID() {
		return netID;
	}

	public void setNetID(String netID) {
		this.netID = netID;
	}

	public Boolean getInterviewer() {
		return interviewer;
	}

	public void setInterviewer(Boolean interviewer) {
		this.interviewer = interviewer;
	}

	public Boolean getResourceGroup() {
		return resourceGroup;
	}

	public void setResourceGroup(Boolean resourceGroup) {
		this.resourceGroup = resourceGroup;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

}
