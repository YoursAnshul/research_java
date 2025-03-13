package com.pro.api.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ShiftScheduleRequest;
import com.pro.api.service.ScheduleService;

@Service
public class ScheduleServiceImpl implements ScheduleService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public GeneralResponse saveSchedule(ShiftScheduleRequest request) {
		GeneralResponse response = new GeneralResponse();

		try {
			// Convert String to LocalDate
			LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate());

			// Define time formatter
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

			// Convert startTime and endTime from String to LocalTime
			LocalTime startTime = LocalTime.parse(request.getStartTime(), timeFormatter);
			LocalTime endTime = LocalTime.parse(request.getEndTime(), timeFormatter);

			// Create LocalDateTime objects
			LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, startTime);
			LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, endTime);

			System.out.println("Start DateTime: " + startDateTime);
			System.out.println("End DateTime: " + endDateTime);

			// Corrected query with LocalDate & LocalDateTime
			String query = "INSERT INTO core.schedules (dempoId, scheduleDate, projectId, comments, startDateTime, endDateTime) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";

			this.jdbcTemplate.update(query, request.getDempoId(), scheduleDate, // ✅ Passing LocalDate instead of String
					request.getProjectId(), request.getComments(), startDateTime, // ✅ Passing LocalDateTime instead of
																					// formatted String
					endDateTime // ✅ Passing LocalDateTime instead of formatted String
			);

			response.Message = "Schedule saved successfully!";
		} catch (DateTimeParseException e) {
			response.Message = "Invalid date or time format: " + e.getMessage();
		}

		return response;
	}

}
