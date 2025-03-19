package com.pro.api.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.Projects;
import com.pro.api.response.ScheduleResponse;
import com.pro.api.response.ShiftScheduleRequest;
import com.pro.api.response.User;
import com.pro.api.service.ScheduleService;

@Service
public class ScheduleServiceImpl implements ScheduleService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public GeneralResponse saveSchedule(List<ShiftScheduleRequest> list) {
		GeneralResponse response = new GeneralResponse();
		List<String> errorMessages = new ArrayList<>();
		int successCount = 0;
		int duplicateCount = 0;

		String checkQuery = "SELECT COUNT(*) FROM core.schedules WHERE dempoId = ? AND scheduleDate = ? AND startDateTime = ? AND endDateTime = ?";

		String insertQuery = "INSERT INTO core.schedules (dempoId, scheduleDate, projectId, comments, startDateTime, endDateTime, status, entryby, entrydt, machinename) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		for (ShiftScheduleRequest request : list) {
			try {
				if (request.getScheduleDate() == null || request.getStartTime() == null
						|| request.getEndTime() == null) {
					errorMessages.add("Missing required fields for request: " + request);
					continue;
				}

				LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate());
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

				LocalTime startTime = LocalTime.parse(request.getStartTime(), timeFormatter);
				LocalTime endTime = LocalTime.parse(request.getEndTime(), timeFormatter);

				LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, startTime);
				LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, endTime);

				int existingCount = this.jdbcTemplate.queryForObject(checkQuery,
						new Object[] { request.getDempoId(), scheduleDate, startDateTime, endDateTime }, Integer.class);

				if (existingCount > 0) {
					duplicateCount++;
					continue;
				}

				this.jdbcTemplate.update(insertQuery, request.getDempoId(), scheduleDate, request.getProjectId(),
						request.getComments(), startDateTime, endDateTime, "0", request.getEntryby(), new Date(), "NA");

				successCount++;

			} catch (DateTimeParseException e) {
				errorMessages.add("Invalid date/time format for request: " + request + " -> " + e.getMessage());
			} catch (Exception e) {
				errorMessages.add("Error processing request: " + request + " -> " + e.getMessage());
			}
		}

		if (successCount > 0) {
			response.Message = "Schedules saved successfully!";
		} else {
			response.Message = "No new schedules were saved.";
		}

		if (!errorMessages.isEmpty()) {
			response.Message += " Some errors occurred: " + String.join("; ", errorMessages);
		}

		return response;
	}

	@Override
	public List<ScheduleResponse> getList(String dempoId, Integer projectId, LocalDate scheduleDate, String tabValue) {
		StringBuilder query = new StringBuilder("""
				SELECT u.dempoid, u.userid, CONCAT(u.fname, ' ', u.lname) AS userName,
				       p.projectid, p.projectcolor, p.projectname,
				       s.startdatetime, s.enddatetime,
				       s.comments, s.scheduleDate AS daywisedate
				FROM core.schedules s
				JOIN core.projects p ON s.projectid = p.projectid
				LEFT JOIN core.users u ON s.dempoid = u.dempoid
				WHERE p.active = 1
				""");

		List<Object> params = new ArrayList<>();

		if (dempoId != null && !dempoId.isBlank()) {
			query.append(" AND s.dempoid = ? ");
			params.add(dempoId);
		}

		if (projectId != null && projectId > 0) {
			query.append(" AND p.projectid = ? ");
			params.add(projectId);
		}

		// Filter based on tabValue
		if ("Day".equalsIgnoreCase(tabValue) && scheduleDate != null) {
			query.append(" AND s.scheduleDate = ? ");
			params.add(scheduleDate);
		} else if ("Week".equalsIgnoreCase(tabValue) && scheduleDate != null) {
			LocalDate weekStart = scheduleDate; // Start of the week
			LocalDate weekEnd = weekStart.plusDays(6); // End of the week
			query.append(" AND s.scheduleDate BETWEEN ? AND ? ");
			params.add(weekStart);
			params.add(weekEnd);
		} else if ("Month".equalsIgnoreCase(tabValue) && scheduleDate != null) {
			LocalDate monthStart = scheduleDate; // Current week start date
			LocalDate monthEnd = monthStart.plusWeeks(5); // End of 6th week
			query.append(" AND s.scheduleDate BETWEEN ? AND ? ");
			params.add(monthStart);
			params.add(monthEnd);
		}

		return jdbcTemplate.query(query.toString(), (rs, rowNum) -> {
			Timestamp startTime = rs.getTimestamp("startdatetime");
			Timestamp endTime = rs.getTimestamp("enddatetime");

			double duration = calculateDuration(startTime, endTime);

			return new ScheduleResponse(rs.getString("comments"), formatTime(startTime), formatTime(endTime), duration,
					rs.getDate("daywisedate"),
					new User(rs.getString("dempoid"), rs.getInt("userid"), rs.getString("userName")),
					new Projects(rs.getInt("projectid"), rs.getString("projectcolor"), rs.getString("projectname")));
		}, params.toArray());
	}

	private double calculateDuration(Date start, Date end) {
		if (start == null || end == null)
			return 0.0;

		long diffMs = end.getTime() - start.getTime();
		if (diffMs < 0) {
			end.setTime(end.getTime() + 24 * 60 * 60 * 1000);
			diffMs = end.getTime() - start.getTime();
		}

		double diffHours = diffMs / (1000.0 * 60 * 60);
		return Math.round(diffHours * 100.0) / 100.0;
	}

	private String formatTime(Date date) {
		if (date == null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
		return sdf.format(date);
	}

}
