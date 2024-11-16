package com.pro.api.models.business;

import java.util.List;

public class SuccessResponse {
	private int successCount;
	private List<String> errors;

	public SuccessResponse(int successCount, List<String> errors) {
		this.successCount = successCount;
		this.errors = errors;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

}
