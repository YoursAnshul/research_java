package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.ProjectTotalId;
import com.pro.api.models.dataaccess.ViProjectTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ViProjectTotalRepository extends JpaRepository<ViProjectTotal, ProjectTotalId> {

	@Query("SELECT vipt FROM ViProjectTotal vipt WHERE vipt.startdatetime >= :weekStart "
			+ "AND vipt.enddatetime <= :weekEnd ORDER BY vipt.projectName, vipt.startdatetime")
	List<ViProjectTotal> findProjectTotalsByWeekRange(OffsetDateTime weekStart, OffsetDateTime weekEnd);

}
