package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.ViUserSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ViUserScheduleRepository extends JpaRepository<ViUserSchedule, Integer> {
	@Query("SELECT us FROM ViUserSchedule us JOIN User u on us.userid = u.userid WHERE u.active = true "
			+ "AND (YEAR(us.startdatetime) = :year " + "OR YEAR(us.weekStart) = :year "
			+ "OR YEAR(us.weekEnd) = :year) " + "AND (MONTH(us.startdatetime) = :month "
			+ "OR MONTH(us.weekStart) = :month " + "OR MONTH(us.weekEnd) = :month) "
			+ "ORDER BY us.startdatetime, us.enddatetime")
	List<ViUserSchedule> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

	@Query("SELECT usc FROM ViUserSchedule usc JOIN User u on usc.userid = u.userid WHERE u.active = true "
			+ "AND usc.startdatetime >= :startDate AND usc.enddatetime <= :endDate "
			+ "ORDER BY usc.startdatetime, usc.enddatetime")
	List<ViUserSchedule> findUserSchedulesBetweenDates(OffsetDateTime startDate, OffsetDateTime endDate);

	@Query("SELECT usc FROM ViUserSchedule usc JOIN User u on usc.userid = u.userid WHERE u.active = true "
			+ "AND LOWER(u.dempoid) = LOWER(:dempoid) AND (YEAR(usc.startdatetime) = :year "
			+ "OR YEAR(usc.weekStart) = :year OR YEAR(usc.weekEnd) = :year) "
			+ "AND (MONTH(usc.startdatetime) = :month OR MONTH(usc.weekStart) = :month "
			+ "OR MONTH(usc.weekEnd) = :month) AND usc.startdatetime IS NOT NULL "
			+ "AND usc.enddatetime IS NOT NULL AND usc.weekStart IS NOT NULL")
	List<ViUserSchedule> findSchedulesByUserAndMonth(String dempoid, int year, int month);

}
