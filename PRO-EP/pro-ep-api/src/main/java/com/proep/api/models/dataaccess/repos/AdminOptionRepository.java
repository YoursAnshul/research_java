package com.proep.api.models.dataaccess.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proep.api.models.business.AdminOptionVariable;
import com.proep.api.models.dataaccess.AdminOption;

@Repository
public interface AdminOptionRepository extends JpaRepository<AdminOption, Integer> {

	@Query("SELECT new  com.proep.api.models.business.AdminOptionVariable(ao.adminOptionsId, ao.optionValue, ff.fieldType, ff.formOrder, ff.fieldLabel, ff.formFieldId)"
			+ " from AdminOption ao JOIN FormField ff ON ao.formFieldId = ff.formFieldId"
			+ " WHERE (UPPER(:adminOptionName) = 'ALL' OR UPPER(ff.columnName) = UPPER(:adminOptionName))"
			+ " order by ff.formOrder")
	List<AdminOptionVariable> getAdminOptionVariables(String adminOptionName);
	
	AdminOption findByAdminOptionsId(int adminOptionsId);
}
