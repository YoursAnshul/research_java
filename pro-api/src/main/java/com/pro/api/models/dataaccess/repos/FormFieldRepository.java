package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.business.FormFieldVariable;
import com.pro.api.models.dataaccess.FormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, Integer> {

	@Query("SELECT new com.pro.api.models.business.FormFieldVariable(ff) from FormField ff"
			+ " WHERE UPPER(ff.configurable) IN :configurableOptions and (UPPER(:fieldName) = 'ALL' OR UPPER(ff.columnName) = UPPER(:fieldName))"
			+ " order by ff.formOrder")
	List<FormFieldVariable> getFormFieldVariableByFiledNameAndConfigurableOptions(String fieldName,
			List<String> configurableOptions);

	@Query("SELECT new com.pro.api.models.business.FormFieldVariable(ff) from FormField ff"
			+ " WHERE (UPPER(ff.tableName) = UPPER(:tableName))" + " order by ff.tableName, ff.fieldLabel")
	List<FormFieldVariable> getFormFieldVariableByTableName(String tableName);

	List<FormField> findByTableNameIgnoreCaseAndProjectId(String tableName, int projectId);
	
	FormField findByTableNameIgnoreCaseAndFormFieldId(String tableName, int formFieldId);

	@Query("SELECT ff FROM FormField ff JOIN Project p ON ff.projectId = p.projectId"
			+ " WHERE ff.projectId > 0 AND p.projectType = :projectType"
			+ " AND p.projectDisplayId LIKE %:projectDisplayId% AND UPPER(ff.tableName) = UPPER(:tableName)")
	List<FormField> findDefaultFieldsByProjectTypeAndProjectDisplayIdAndTableName(Integer projectType,
			String projectDisplayId, String tableName);
	@Query("SELECT ff.formFieldId from FormField ff where ff.fieldLabel=(:fieldLabel)")
	Integer findFormFieldByFieldLabel(String fieldLabel);

}
