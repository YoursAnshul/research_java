package com.proep.api.models.dataaccess;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "FormFields")
public class FormField implements Cloneable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FormFieldId")
    private int formFieldId;

    @Column(name = "FieldLabel")
    private String fieldLabel;

    @Column(name = "DefaultLabel")
    private String defaultLabel;

    @Column(name = "ColumnName")
    private String columnName;

    @Column(name = "TableName")
    private String tableName;

    @Column(name = "FormOrder")
    private int formOrder;

    @Column(name = "Tab")
    private String tab;

    @Column(name = "FieldType")
    private String fieldType;

    @Column(name = "Configurable")
    private String configurable;

    @Column(name = "Required")
    private Boolean required;

    @Column(name = "DisplayInTable")
    private Boolean displayInTable;

    @Column(name = "Hidden")
    private Boolean hidden;

    @Column(name = "ProjectId")
    private Integer projectId;

    @Column(name = "RequireConfirmation")
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
