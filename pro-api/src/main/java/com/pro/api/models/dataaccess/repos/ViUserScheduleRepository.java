package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.ViUserSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ViUserScheduleRepository extends JpaRepository<ViUserSchedule, Integer> {
    
    @Query("SELECT v FROM ViUserSchedule v WHERE " +
           "EXTRACT(YEAR FROM v.startdatetime) = :year AND " +
           "EXTRACT(MONTH FROM v.startdatetime) = :month")
    List<ViUserSchedule> findByYearAndMonth(
        @Param("year") int year,
        @Param("month") int month
    );

    @Query("SELECT v FROM ViUserSchedule v WHERE v.startdatetime >= :startDate AND v.enddatetime <= :endDate")
    List<ViUserSchedule> findUserSchedulesBetweenDates(
        @Param("startDate") OffsetDateTime startDate,
        @Param("endDate") OffsetDateTime endDate
    );

    @Query("SELECT v FROM ViUserSchedule v WHERE " +
           "(:dempoid IS NULL OR v.dempoid = :dempoid) AND " +
           "(:projectId IS NULL OR v.projectid = :projectId) AND " +
           "EXTRACT(YEAR FROM v.startdatetime) = :year AND " +
           "EXTRACT(MONTH FROM v.startdatetime) = :month")
    List<ViUserSchedule> findSchedules(
        @Param("dempoid") String dempoid,
        @Param("projectId") Integer projectId,
        @Param("year") int year,
        @Param("month") int month
    );

    @Query("SELECT v FROM ViUserSchedule v WHERE " +
           "v.dempoid = :dempoid AND " +
           "EXTRACT(YEAR FROM v.startdatetime) = :year AND " +
           "EXTRACT(MONTH FROM v.startdatetime) = :month")
    List<ViUserSchedule> findSchedulesByUserAndMonth(
        @Param("dempoid") String dempoid,
        @Param("year") int year,
        @Param("month") int month
    );
}
