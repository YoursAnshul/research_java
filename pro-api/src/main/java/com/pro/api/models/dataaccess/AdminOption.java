package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

@Entity
@Table(name = "adminoptions") // Specify the table name if it's different
public class AdminOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adminoptionsid")
    private int adminOptionsId;

    @Column(name = "optionvalue")
    private String optionValue;

    @Column(name = "formfieldid")
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
