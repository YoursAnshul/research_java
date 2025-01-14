package com.proep.api.models.business;

import java.util.List;

public class ConfigurationVariable {
	private List<AdminOptionVariable> adminOptionsVariables;
	private List<FormFieldVariable> formFields;

	public ConfigurationVariable(List<AdminOptionVariable> adminOptionsVariables, List<FormFieldVariable> formFields) {
		this.adminOptionsVariables = adminOptionsVariables;
		this.formFields = formFields;
	}

	public List<AdminOptionVariable> getAdminOptionsVariables() {
		return adminOptionsVariables;
	}

	public void setAdminOptionsVariables(List<AdminOptionVariable> adminOptionsVariables) {
		this.adminOptionsVariables = adminOptionsVariables;
	}

	public List<FormFieldVariable> getFormFields() {
		return formFields;
	}

	public void setFormFields(List<FormFieldVariable> formFields) {
		this.formFields = formFields;
	}

}
