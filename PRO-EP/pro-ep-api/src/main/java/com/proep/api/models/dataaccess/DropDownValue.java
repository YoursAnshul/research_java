package com.proep.api.models.dataaccess;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "DropDownValues")
public class DropDownValue implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "DropDownValueId")
	private int dropDownValueId;

	@Column(name = "SortOrder")
	private int sortOrder;

	@Column(name = "CodeValues")
	private int codeValues;

	@Column(name = "FormFieldId")
	private Integer formFieldId;

	@Column(name = "DropDownItem")
	private String dropDownItem;

	@Column(name = "ColorCode")
	private String colorCode;

	@Column(name = "Abbr")
	private String abbr;

	public DropDownValue() {
	}

	// Getters and Setters

	public int getDropDownValueId() {
		return dropDownValueId;
	}

	public void setDropDownValueId(int dropDownValueId) {
		this.dropDownValueId = dropDownValueId;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getCodeValues() {
		return codeValues;
	}

	public void setCodeValues(int codeValues) {
		this.codeValues = codeValues;
	}

	public Integer getFormFieldId() {
		return formFieldId;
	}

	public void setFormFieldId(Integer formFieldId) {
		this.formFieldId = formFieldId;
	}

	public String getDropDownItem() {
		return dropDownItem;
	}

	public void setDropDownItem(String dropDownItem) {
		this.dropDownItem = dropDownItem;
	}

	public String getColorCode() {
		return colorCode;
	}

	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}

	public String getAbbr() {
		return abbr;
	}

	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

}
