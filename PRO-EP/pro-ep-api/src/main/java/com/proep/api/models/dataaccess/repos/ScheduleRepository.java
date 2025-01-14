package com.proep.api.models.dataaccess.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proep.api.models.dataaccess.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
	@Query("SELECT s FROM Schedule s "
			+ "WHERE (YEAR(s.startdatetime) = :lastYear AND YEAR(s.enddatetime) = :nextYear) "
			+ "OR (YEAR(s.startdatetime) = :currentYear AND YEAR(s.enddatetime) = :currentYear) "
			+ "OR (YEAR(s.startdatetime) = :nextYear AND YEAR(s.enddatetime) = :nextYear)")
	List<Schedule> findSchedulesByYearRange(int lastYear, int currentYear, int nextYear);

	@Query("SELECT s FROM Schedule s WHERE s.preschedulekey IN :scheduleKeys ORDER BY s.startdatetime")
	List<Schedule> findSchedulesByScheduleKeys(@Param("scheduleKeys") List<String> scheduleKeys);

	@Query("SELECT s FROM Schedule s WHERE LOWER(s.dempoid) = LOWER(:dempoId) AND YEAR(s.startdatetime) = :year "
			+ "AND MONTH(s.startdatetime) = :month AND s.startdatetime IS NOT NULL AND s.enddatetime IS NOT NULL")
	List<Schedule> findSchedulesByDempoIdAndYearMonth(@Param("dempoId") String dempoId, @Param("year") int year,
			@Param("month") int month);

	@Query("SELECT s FROM Schedule s WHERE TIMESTAMPDIFF(HOUR, s.startdatetime, s.enddatetime) < 4 AND s.startdatetime IS NOT NULL AND s.enddatetime IS NOT NULL")
	List<Schedule> findSchedulesWithDurationLessThanFourHours();

	@Query("SELECT s FROM Schedule s WHERE TIMESTAMPDIFF(HOUR, s.startdatetime, s.enddatetime) > 4 AND s.startdatetime IS NOT NULL AND s.enddatetime IS NOT NULL")
	List<Schedule> findSchedulesWithDurationGreaterThanSevenHours();

	@Query("SELECT s FROM Schedule s WHERE TIMESTAMPDIFF(HOUR, s.startdatetime, s.enddatetime) = 8 AND s.startdatetime IS NOT NULL AND s.enddatetime IS NOT NULL")
	List<Schedule> findSchedulesWithDurationEqualsToEightHours();

	@Query(value = "SELECT s.* FROM schedules s WHERE DATEPART(dw, s.startdatetime) NOT IN (1, 7) AND DATEPART(hour, s.startdatetime) < :hour", nativeQuery = true)
	List<Schedule> findSchedulesNotOnWeekendAndTime(@Param("hour") Integer hour);
}
