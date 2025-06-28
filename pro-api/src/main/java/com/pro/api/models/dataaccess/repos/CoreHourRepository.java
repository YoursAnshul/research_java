package com.pro.api.models.dataaccess.repos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pro.api.models.dataaccess.CoreHour;

@Repository
public interface CoreHourRepository extends JpaRepository<CoreHour, Integer> {

	CoreHour findFirstByDempoid(String dempoid);

	List<CoreHour> findAllByDempoidAndCoreHoursIdNot(String dempoid, int coreHoursId);

	@Query("SELECT ch FROM CoreHour ch JOIN User u on ch.dempoid=u.dempoid WHERE u.status = 1 AND u.dempoid IS NOT NULL")
	List<CoreHour> findActiveCoreHours();

	@Query("SELECT ch FROM CoreHour ch " + "WHERE ch.modDt < :currentMonthStart " + "AND ch.dempoid IS NOT NULL "
			+ "AND ch.dempoid <> ''")
	List<CoreHour> findByModDtBeforeAndDempoidIsNotNullAndDempoidIsNot(LocalDate currentMonthStart);

	default boolean hasFilteredCoreHours(LocalDate currentMonthStart) {
		List<CoreHour> filteredCoreHours = findByModDtBeforeAndDempoidIsNotNullAndDempoidIsNot(currentMonthStart);
		return !filteredCoreHours.isEmpty();
	}

	@Query(value = """
			SELECT * FROM CoreHours
			WHERE dempoid = :dempoid AND (
			    (EXTRACT(MONTH FROM month1) = :month AND EXTRACT(YEAR FROM month1) = :year) OR
			    (EXTRACT(MONTH FROM month2) = :month AND EXTRACT(YEAR FROM month2) = :year) OR
			    (EXTRACT(MONTH FROM month3) = :month AND EXTRACT(YEAR FROM month3) = :year) OR
			    (EXTRACT(MONTH FROM month4) = :month AND EXTRACT(YEAR FROM month4) = :year) OR
			    (EXTRACT(MONTH FROM month5) = :month AND EXTRACT(YEAR FROM month5) = :year) OR
			    (EXTRACT(MONTH FROM month6) = :month AND EXTRACT(YEAR FROM month6) = :year) OR
			    (EXTRACT(MONTH FROM month7) = :month AND EXTRACT(YEAR FROM month7) = :year) OR
			    (EXTRACT(MONTH FROM month8) = :month AND EXTRACT(YEAR FROM month8) = :year) OR
			    (EXTRACT(MONTH FROM month9) = :month AND EXTRACT(YEAR FROM month9) = :year) OR
			    (EXTRACT(MONTH FROM month10) = :month AND EXTRACT(YEAR FROM month10) = :year) OR
			    (EXTRACT(MONTH FROM month11) = :month AND EXTRACT(YEAR FROM month11) = :year) OR
			    (EXTRACT(MONTH FROM month12) = :month AND EXTRACT(YEAR FROM month12) = :year) OR
			    (EXTRACT(MONTH FROM month13) = :month AND EXTRACT(YEAR FROM month13) = :year) OR
			    (EXTRACT(MONTH FROM month14) = :month AND EXTRACT(YEAR FROM month14) = :year)
			)
			LIMIT 1
			""", nativeQuery = true)
	CoreHour findFirstByDempoidAndMonthYear(@Param("dempoid") String dempoId, @Param("month") int month,
			@Param("year") int year);

}
