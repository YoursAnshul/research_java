package com.pro.api.models.business;


import com.pro.api.models.dataaccess.DropDownValue;
import com.pro.api.models.dataaccess.FormField;

import java.util.ArrayList;
import java.util.List;

public class FormFieldVariable {
    private FormField formField;
    private List<DropDownValue> dropDownValues;

    public FormFieldVariable() {
        this.dropDownValues = new ArrayList<>();
    }

    public FormFieldVariable(FormField formField) {
        this.formField = formField;
        this.dropDownValues = new ArrayList<>();
    }

    public FormField getFormField() {
        return formField;
    }

    public void setFormField(FormField formField) {
        this.formField = formField;
    }

    public List<DropDownValue> getDropDownValues() {
        return dropDownValues;
    }

    public void setDropDownValues(List<DropDownValue> dropDownValues) {
        this.dropDownValues = dropDownValues;
    }
}

