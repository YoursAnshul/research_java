package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.ViProjectTotalsSummed;
import com.pro.api.models.dataaccess.ViProjectTotalsSummedId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ViProjectTotalsSummedRepository extends JpaRepository<ViProjectTotalsSummed, ViProjectTotalsSummedId> {

	@Query("SELECT vipts FROM ViProjectTotalsSummed vipts WHERE vipts.startdatetime >= :weekStart "
			+ "AND vipts.startdatetime <= :weekEnd ORDER BY vipts.startdatetime")
	List<ViProjectTotalsSummed> findProjectTotalsSummedByWeekRange(OffsetDateTime weekStart, OffsetDateTime weekEnd);

}
