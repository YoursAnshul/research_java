package com.proep.api.models.business;

public class FrontEndConfiguration {
	private String ActiveProfile;
	private String Version;
	private String AssertionUrl;
	private String Realm;
	private String AppBaseUrl;
	private String DataAPIUrl;

	public String getActiveProfile() {
		return ActiveProfile;
	}
	public void setActiveProfile(String activeProfile) { ActiveProfile = activeProfile; }
	public String getVersion() {
		return Version;
	}
	public void setVersion(String version) {
		Version = version;
	}
	public String getAssertionUrl() {
		return AssertionUrl;
	}
	public void setAssertionUrl(String assertionUrl) {
		AssertionUrl = assertionUrl;
	}
	public String getRealm() {
		return Realm;
	}
	public void setRealm(String realm) {
		Realm = realm;
	}
	public String getAppBaseUrl() {
		return AppBaseUrl;
	}
	public void setAppBaseUrl(String appBaseUrl) {
		AppBaseUrl = appBaseUrl;
	}
	public String getDataAPIUrl() {
		return DataAPIUrl;
	}
	public void setDataAPIUrl(String dataAPIUrl) {
		DataAPIUrl = dataAPIUrl;
	}
	
	public FrontEndConfiguration() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public FrontEndConfiguration(String activeProfile, String version, String assertionUrl, String realm,
								 String appBaseUrl, String dataAPIUrl) {
		super();
		ActiveProfile = activeProfile;
		Version = version;
		AssertionUrl = assertionUrl;
		Realm = realm;
		AppBaseUrl = appBaseUrl;
		DataAPIUrl = dataAPIUrl;
	}

}
