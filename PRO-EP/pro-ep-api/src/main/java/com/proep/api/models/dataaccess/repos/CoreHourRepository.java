package com.proep.api.models.dataaccess.repos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proep.api.models.dataaccess.CoreHour;

@Repository
public interface CoreHourRepository extends JpaRepository<CoreHour, Integer> {

	CoreHour findFirstByDempoid(String dempoid);

	List<CoreHour> findAllByDempoidAndCoreHoursIdNot(String dempoid, int coreHoursId);

	@Query("SELECT ch FROM CoreHour ch JOIN User u on ch.dempoid=u.dempoid WHERE u.active = true AND u.dempoid IS NOT NULL")
	List<CoreHour> findActiveCoreHours();

	@Query("SELECT ch FROM CoreHour ch " + "WHERE ch.modDt < :currentMonthStart " + "AND ch.dempoid IS NOT NULL "
			+ "AND ch.dempoid <> ''")
	List<CoreHour> findByModDtBeforeAndDempoidIsNotNullAndDempoidIsNot(LocalDate currentMonthStart);

	default boolean hasFilteredCoreHours(LocalDate currentMonthStart) {
		List<CoreHour> filteredCoreHours = findByModDtBeforeAndDempoidIsNotNullAndDempoidIsNot(currentMonthStart);
		return !filteredCoreHours.isEmpty();
	}
}
