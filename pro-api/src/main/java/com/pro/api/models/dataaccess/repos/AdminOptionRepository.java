package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.business.AdminOptionVariable;
import com.pro.api.models.dataaccess.AdminOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminOptionRepository extends JpaRepository<AdminOption, Integer> {

	@Query("SELECT new  com.pro.api.models.business.AdminOptionVariable(ao.adminOptionsId, ao.optionValue, ff.fieldType, ff.formOrder, ff.fieldLabel, ff.formFieldId)"
			+ " from AdminOption ao JOIN FormField ff ON ao.formFieldId = ff.formFieldId"
			+ " WHERE (UPPER(:adminOptionName) = 'ALL' OR UPPER(ff.columnName) = UPPER(:adminOptionName))"
			+ " order by ff.formOrder")
	List<AdminOptionVariable> getAdminOptionVariables(String adminOptionName);
	
	AdminOption findByAdminOptionsId(int adminOptionsId);
}
