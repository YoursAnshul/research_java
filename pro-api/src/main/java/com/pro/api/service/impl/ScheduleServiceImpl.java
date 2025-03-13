package com.pro.api.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ShiftScheduleRequest;
import com.pro.api.service.ScheduleService;

@Service
public class ScheduleServiceImpl implements ScheduleService {
	public GeneralResponse saveSchedule(ShiftScheduleRequest request) {
		GeneralResponse response = new GeneralResponse();

		try {
			LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate());

			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

			LocalTime startTime = LocalTime.parse(request.getStartTime(), timeFormatter);
			LocalTime endTime = LocalTime.parse(request.getEndTime(), timeFormatter);

			LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, startTime);
			LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, endTime);

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String formattedStartDateTime = startDateTime.format(dateTimeFormatter);
			String formattedEndDateTime = endDateTime.format(dateTimeFormatter);

			System.out.println("Start DateTime: " + formattedStartDateTime);
			System.out.println("End DateTime: " + formattedEndDateTime);

			response.Message = "Schedule saved successfully!";
		} catch (DateTimeParseException e) {
			response.Message = "Invalid date or time format: " + e.getMessage();
		}

		return response;
	}

}
