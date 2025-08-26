package com.dimetyd.bot.model;

public class VendorCrd {

	private int id;
	private String userName;
	private String password;
	private String authenticationKeyAccount;
	private String authenticationKeySecret;
	private String url;
	private String status;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNeedToRerunDaily() {
		return NeedToRerunDaily;
	}

	public void setNeedToRerunDaily(String needToRerunDaily) {
		NeedToRerunDaily = needToRerunDaily;
	}

	private String NeedToRerunDaily;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthenticationKeyAccount() {
		return authenticationKeyAccount;
	}

	public void setAuthenticationKeyAccount(String authenticationKeyAccount) {
		this.authenticationKeyAccount = authenticationKeyAccount;
	}

	public String getAuthenticationKeySecret() {
		return authenticationKeySecret;
	}

	public void setAuthenticationKeySecret(String authenticationKeySecret) {
		this.authenticationKeySecret = authenticationKeySecret;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
