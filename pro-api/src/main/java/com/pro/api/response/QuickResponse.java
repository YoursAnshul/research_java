package com.pro.api.response;

public class QuickResponse {

	public String getTollfreenumber() {
		return tollfreenumber;
	}

	public void setTollfreenumber(String tollfreenumber) {
		this.tollfreenumber = tollfreenumber;
	}

	public String getProjectinfo() {
		return projectinfo;
	}

	public void setProjectinfo(String projectinfo) {
		this.projectinfo = projectinfo;
	}

	public String getStudyemailaddress() {
		return studyemailaddress;
	}

	public void setStudyemailaddress(String studyemailaddress) {
		this.studyemailaddress = studyemailaddress;
	}

	private String projectName;
	private String projectColor;
	private String voiceMailNumber;
	private String voiceMailPin;
	private String active;
	private String tollfreenumber;
	private String projectinfo;
	private String studyemailaddress;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectColor() {
		return projectColor;
	}

	public void setProjectColor(String projectColor) {
		this.projectColor = projectColor;
	}

	public String getVoiceMailNumber() {
		return voiceMailNumber;
	}

	public void setVoiceMailNumber(String voiceMailNumber) {
		this.voiceMailNumber = voiceMailNumber;
	}

	public String getVoiceMailPin() {
		return voiceMailPin;
	}

	public void setVoiceMailPin(String voiceMailPin) {
		this.voiceMailPin = voiceMailPin;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

}
