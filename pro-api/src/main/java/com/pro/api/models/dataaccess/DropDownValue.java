package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

@Entity
@Table(name = "dropdownvalues")
public class DropDownValue implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "dropdownvalueid")
	private int dropDownValueId;

	@Column(name = "sortorder")
	private int sortOrder;

	@Column(name = "codevalues")
	private int codeValues;

	@Column(name = "formfieldid")
	private Integer formFieldId;

	@Column(name = "dropdownitem")
	private String dropDownItem;

	@Column(name = "colorcode")
	private String colorCode;

	@Column(name = "abbr")
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
