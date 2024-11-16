package com.pro.api.models.dataaccess;

import jakarta.persistence.*;


@Entity
@Table(name = "formfields")
public class FormField implements Cloneable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formfieldid")
    private int formFieldId;

    @Column(name = "fieldlabel")
    private String fieldLabel;

    @Column(name = "defaultlabel")
    private String defaultLabel;

    @Column(name = "columnname")
    private String columnName;

    @Column(name = "tablename")
    private String tableName;

    @Column(name = "formorder")
    private int formOrder;

    @Column(name = "tab")
    private String tab;

    @Column(name = "fieldtype")
    private String fieldType;

    @Column(name = "configurable")
    private String configurable;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "displayintable")
    private Boolean displayInTable;

    @Column(name = "hidden")
    private Boolean hidden;

    @Column(name = "projectid")
    private Integer projectId;

    @Column(name = "requireconfirmation")
    private Boolean requireConfirmation;

    
    public FormField() {
    }

    public int getFormFieldId() {
        return formFieldId;
    }

    public void setFormFieldId(int formFieldId) {
        this.formFieldId = formFieldId;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }

    public void setDefaultLabel(String defaultLabel) {
        this.defaultLabel = defaultLabel;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getFormOrder() {
        return formOrder;
    }

    public void setFormOrder(int formOrder) {
        this.formOrder = formOrder;
    }

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getConfigurable() {
        return configurable;
    }

    public void setConfigurable(String configurable) {
        this.configurable = configurable;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getDisplayInTable() {
        return displayInTable;
    }

    public void setDisplayInTable(Boolean displayInTable) {
        this.displayInTable = displayInTable;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Boolean getRequireConfirmation() {
        return requireConfirmation;
    }

    public void setRequireConfirmation(Boolean requireConfirmation) {
        this.requireConfirmation = requireConfirmation;
    }

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
