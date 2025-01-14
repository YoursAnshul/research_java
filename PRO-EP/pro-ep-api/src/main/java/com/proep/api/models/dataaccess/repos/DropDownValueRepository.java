package com.proep.api.models.dataaccess.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proep.api.models.dataaccess.DropDownValue;

public interface DropDownValueRepository extends JpaRepository<DropDownValue, Integer> {

	@Query("select dv from DropDownValue dv where dv.formFieldId IS NOT NULL and"
			+ " dv.formFieldId IN :formFieldIds order by dv.sortOrder")
	List<DropDownValue> getDropDownValuesByFormFieldIds(List<Integer> formFieldIds);

	List<DropDownValue> findByFormFieldId(Integer formFieldId);

	@Query("SELECT dv FROM DropDownValue dv JOIN FormField ff ON dv.formFieldId = ff.formFieldId "
			+ "WHERE ff.tableName = :tableName AND ff.columnName = :columnName")
	List<DropDownValue> findByTableNameAndColumnName(String tableName, String columnName);
	
	DropDownValue findFirstByCodeValues(Integer codeValues);
}
