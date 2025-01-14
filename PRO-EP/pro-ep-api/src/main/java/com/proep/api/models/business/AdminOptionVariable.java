package com.proep.api.models.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.proep.api.models.dataaccess.DropDownValue;

public class AdminOptionVariable {

    private int adminOptionsId;
    private String optionValue;
    private String[] optionValueMulti;
    private String fieldType;
    private int sortOrder;
    private String fieldLabel;
    private List<DropDownValue> dropDownValues;
    private int formFieldId;

    public AdminOptionVariable() {
        dropDownValues = new ArrayList<>();
    }

    public AdminOptionVariable(int adminOptionsId, String optionValue, String fieldType, int sortOrder, String fieldLabel, int formFieldId) {
        this.adminOptionsId = adminOptionsId;
        this.optionValue = optionValue;
        this.optionValueMulti = optionValue.split("\\|"); // Split using pipe character, escaped in Java
        this.fieldType = fieldType;
        this.sortOrder = sortOrder;
        this.fieldLabel = fieldLabel;
        this.formFieldId = formFieldId;
        this.dropDownValues = new ArrayList<>();
    }

    // Getters and Setters
    public int getAdminOptionsId() {
        return adminOptionsId;
    }

    public void setAdminOptionsId(int adminOptionsId) {
        this.adminOptionsId = adminOptionsId;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public String[] getOptionValueMulti() {
        return optionValueMulti;
    }

    public void setOptionValueMulti(String[] optionValueMulti) {
        this.optionValueMulti = optionValueMulti;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public List<DropDownValue> getDropDownValues() {
        return dropDownValues;
    }

    public void setDropDownValues(List<DropDownValue> dropDownValues) {
        this.dropDownValues = dropDownValues;
    }

    public int getFormFieldId() {
        return formFieldId;
    }

    public void setFormFieldId(int formFieldId) {
        this.formFieldId = formFieldId;
    }

    @Override
    public String toString() {
        return "AdminOptionVariable{" +
                "adminOptionsId=" + adminOptionsId +
                ", optionValue='" + optionValue + '\'' +
                ", optionValueMulti=" + Arrays.toString(optionValueMulti) +
                ", fieldType='" + fieldType + '\'' +
                ", sortOrder=" + sortOrder +
                ", fieldLabel='" + fieldLabel + '\'' +
                ", dropDownValues=" + dropDownValues +
                ", formFieldId=" + formFieldId +
                '}';
    }
}
