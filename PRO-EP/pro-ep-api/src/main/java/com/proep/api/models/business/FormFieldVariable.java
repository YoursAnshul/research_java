package com.proep.api.models.business;


import java.util.ArrayList;
import java.util.List;

import com.proep.api.models.dataaccess.DropDownValue;
import com.proep.api.models.dataaccess.FormField;

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

