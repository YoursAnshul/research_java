package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.BlockOutDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface BlockOutDateRepository extends JpaRepository<BlockOutDate, Integer> {
	@Query("SELECT b FROM BlockOutDate b " + "WHERE b.blockOutDay >= :currentDate " + "ORDER BY b.blockOutDay")
	List<BlockOutDate> getBlockOutDatesAfterCurrentDate(OffsetDateTime currentDate);

}
