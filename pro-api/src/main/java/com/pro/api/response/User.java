package com.pro.api.response;

public class User {
	private String dempoId;
	private Integer userId;
	private String userName;

	public User() {
	}

	public User(String dempoId, Integer userId, String userName) {
		this.dempoId = dempoId;
		this.userId = userId;
		this.userName = userName;
	}

	public String getDempoId() {
		return dempoId;
	}

	public void setDempoId(String dempoId) {
		this.dempoId = dempoId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}