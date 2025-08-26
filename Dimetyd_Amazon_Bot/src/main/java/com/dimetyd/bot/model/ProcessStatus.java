package com.dimetyd.bot.model;

public class ProcessStatus {
	private Boolean status;
	private String comment;

	public ProcessStatus(Boolean status, String comment) {
		this.status = status;
		this.comment = comment;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
