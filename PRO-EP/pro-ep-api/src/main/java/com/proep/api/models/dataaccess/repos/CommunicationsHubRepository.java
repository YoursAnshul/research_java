package com.proep.api.models.dataaccess.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proep.api.models.dataaccess.CommunicationsHub;

@Repository
public interface CommunicationsHubRepository extends JpaRepository<CommunicationsHub, Integer> {

	@Query("SELECT COUNT(ch) FROM CommunicationsHub ch JOIN FormField ff on ch.formFieldId = ff.formFieldId"
			+ " WHERE ff.projectId = :projectId AND UPPER(ff.tableName) = UPPER(:tableName)")
	int countByProjectIdAndTableName(int projectId, String tableName);
	
	@Query("SELECT COUNT(ch) FROM CommunicationsHub ch JOIN FormField ff on ch.formFieldId = ff.formFieldId"
			+ " WHERE ff.formFieldId = :formFieldId AND UPPER(ff.tableName) = UPPER(:tableName)")
	int countByFormFieldIdAndTableName(int formFieldId, String tableName);
	
	 List<CommunicationsHub> findAllByOrderByEntryId();
	 
	 @Query("SELECT MAX(c.entryId) FROM CommunicationsHub c")
	 Integer findMaxEntryId();
	
}
