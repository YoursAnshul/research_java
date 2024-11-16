package com.pro.api.models.business;

import com.pro.api.models.dataaccess.ViProjectTotal;
import com.pro.api.models.dataaccess.ViProjectTotalsSummed;

import java.util.ArrayList;
import java.util.List;

public class ProjectTotalsReport {
	private List<ViProjectTotal> projectTotals;
	private List<ViProjectTotalsSummed> projectTotalsSummed;

	// Constructors
	public ProjectTotalsReport() {
		this.projectTotals = new ArrayList<>();
		this.projectTotalsSummed = new ArrayList<>();
	}

	// Getters and Setters
	public List<ViProjectTotal> getProjectTotals() {
		return projectTotals;
	}

	public void setProjectTotals(List<ViProjectTotal> projectTotals) {
		this.projectTotals = projectTotals;
	}

	public List<ViProjectTotalsSummed> getProjectTotalsSummed() {
		return projectTotalsSummed;
	}

	public void setProjectTotalsSummed(List<ViProjectTotalsSummed> projectTotalsSummed) {
		this.projectTotalsSummed = projectTotalsSummed;
	}
}
