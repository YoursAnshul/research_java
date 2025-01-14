package com.proep.api.models.dataaccess.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proep.api.models.dataaccess.CommunicationsHubAuditTable;

@Repository
public interface CommunicationsHubAuditTableRepository extends JpaRepository<CommunicationsHubAuditTable, Integer> {

	@Query("SELECT cha FROM CommunicationsHubAuditTable cha " + "WHERE cha.communicationsHubId = :communicationsHubId "
			+ "AND cha.auditAction = 'DELETE'")
	Optional<CommunicationsHubAuditTable> findFirstByCommunicationsHubIdAndAuditAction(Integer communicationsHubId);
}
