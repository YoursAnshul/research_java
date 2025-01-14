package com.proep.api.models.dataaccess.repos;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proep.api.models.dataaccess.ViProjectTotalsSummed;
import com.proep.api.models.dataaccess.ViProjectTotalsSummedId;

@Repository
public interface ViProjectTotalsSummedRepository extends JpaRepository<ViProjectTotalsSummed, ViProjectTotalsSummedId> {

	@Query("SELECT vipts FROM ViProjectTotalsSummed vipts WHERE vipts.startdatetime >= :weekStart "
			+ "AND vipts.startdatetime <= :weekEnd ORDER BY vipts.startdatetime")
	List<ViProjectTotalsSummed> findProjectTotalsSummedByWeekRange(OffsetDateTime weekStart, OffsetDateTime weekEnd);

}
