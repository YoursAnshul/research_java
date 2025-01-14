package com.proep.api.models.dataaccess.repos;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proep.api.models.dataaccess.BlockOutDate;

public interface BlockOutDateRepository extends JpaRepository<BlockOutDate, Integer> {
	@Query("SELECT b FROM BlockOutDate b " + "WHERE b.blockOutDay >= :currentDate " + "ORDER BY b.blockOutDay")
	List<BlockOutDate> getBlockOutDatesAfterCurrentDate(OffsetDateTime currentDate);

}
