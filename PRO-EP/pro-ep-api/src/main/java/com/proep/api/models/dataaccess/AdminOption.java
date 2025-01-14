package com.proep.api.models.dataaccess;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "AdminOptions") // Specify the table name if it's different
public class AdminOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AdminOptionsId")
    private int adminOptionsId;

    @Column(name = "OptionValue")
    private String optionValue;

    @Column(name = "FormFieldId")
    private Integer formFieldId;

    // Constructors, Getters, and Setters

    public AdminOption() {
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

    public Integer getFormFieldId() {
        return formFieldId;
    }

    public void setFormFieldId(Integer formFieldId) {
        this.formFieldId = formFieldId;
    }

    @Override
    public String toString() {
        return "AdminOption{" +
                "adminOptionsId=" + adminOptionsId +
                ", optionValue='" + optionValue + '\'' +
                ", formFieldId=" + formFieldId +
                '}';
    }
}
