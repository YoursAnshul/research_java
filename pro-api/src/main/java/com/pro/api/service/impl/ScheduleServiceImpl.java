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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
		Set<String> errorMessages = new LinkedHashSet<>();
		int successCount = 0;
		int duplicateCount = 0;

		String checkQuery = "SELECT COUNT(*) FROM core.schedules WHERE dempoId = ? AND scheduleDate = ? "
				+ "AND ((startDateTime <= ? AND endDateTime > ?) " + "OR (startDateTime < ? AND endDateTime >= ?) "
				+ "OR (startDateTime >= ? AND endDateTime <= ?))";

		String insertQuery = "INSERT INTO core.schedules (dempoId, scheduleDate, projectId, comments, startDateTime, endDateTime, status, entryby, entrydt, machinename) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		for (ShiftScheduleRequest request : list) {
			try {
				if (request.getScheduleDate() == null || request.getStartTime() == null
						|| request.getEndTime() == null) {
					errorMessages.add("Missing required fields for DempoId: " + request.getDempoId());
					continue;
				}

				LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate());
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

				LocalTime startTime = LocalTime.parse(request.getStartTime(), timeFormatter);
				LocalTime endTime = LocalTime.parse(request.getEndTime(), timeFormatter);

				LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, startTime);
				LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, endTime);

				Integer existingCount = this.jdbcTemplate.queryForObject(checkQuery, Integer.class,
						request.getDempoId(), scheduleDate, startDateTime, endDateTime, startDateTime, endDateTime,
						startDateTime, endDateTime);

				if (existingCount != null && existingCount > 0) {
					duplicateCount++;
					errorMessages.add("Schedule already exists for DempoId: " + request.getDempoId() + ", Date: "
							+ scheduleDate + ", Time: " + request.getStartTime() + " - " + request.getEndTime());
					continue;
				}

				this.jdbcTemplate.update(insertQuery, request.getDempoId(), scheduleDate, request.getProjectId(),
						request.getComments(), startDateTime, endDateTime, "0", request.getEntryby(), new Date(), "NA");

				successCount++;

			} catch (DateTimeParseException e) {
				errorMessages
						.add("Invalid date/time format for DempoId: " + request.getDempoId() + " -> " + e.getMessage());
			} catch (Exception e) {
				errorMessages
						.add("Error processing request for DempoId: " + request.getDempoId() + " -> " + e.getMessage());
			}
		}

		if (duplicateCount > 0 && successCount == 0) {
			response.Message = "Schedule already exists for the given day, time, and DempoId.";
		} else if (successCount > 0) {
			response.Message = "Schedules saved successfully!";
		}

		if (!errorMessages.isEmpty()) {
			response.Message += " Some errors occurred: " + String.join("; ", errorMessages);
		}

		return response;
	}

	@Override
	public List<ScheduleResponse> getList(String dempoId, Integer projectId, LocalDate scheduleDate, String tabValue,
			LocalDate startDate, LocalDate endDate, int year, int month) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT s.preschedulekey, u.dempoid, u.userid, CONCAT(u.fname, ' ', u.lname) AS userName, ");
		query.append("p.projectid, p.projectcolor, p.projectname, ");
		query.append("s.startdatetime, s.enddatetime, ");
		query.append("s.comments, s.scheduleDate AS daywisedate ");
		query.append("FROM core.schedules s ");
		query.append("JOIN core.projects p ON s.projectid = p.projectid ");
		query.append("LEFT JOIN core.users u ON s.dempoid = u.dempoid ");
		query.append("WHERE p.active = 1  ");
		List<Object> params = new ArrayList<>();

		query.append("AND (EXTRACT(YEAR FROM s.startdatetime)=? OR EXTRACT(YEAR FROM s.startdatetime) = ? ");
		params.add(year);
		params.add(year);
		query.append("OR EXTRACT(YEAR FROM s.startdatetime) = ?) ");
		params.add(year);
//		params.add(month);
//		query.append("OR EXTRACT(MONTH FROM s.startdatetime) = ? OR EXTRACT(MONTH FROM s.startdatetime) = ?) ");
//		params.add(month);
//		params.add(month);

		if (dempoId != null && !dempoId.isBlank()) {
			query.append(" AND s.dempoid = ? ");
			params.add(dempoId);
		}

		if (projectId != null && projectId > 0) {
			query.append(" AND p.projectid = ? ");
			params.add(projectId);
		}

		// if ("Day".equalsIgnoreCase(tabValue) && scheduleDate != null) {
		// query.append(" AND s.scheduleDate = ? ");
		// System.out.println("scheduleDate---->" + scheduleDate);
		// params.add(scheduleDate);
		// } else if ("Week".equalsIgnoreCase(tabValue) && startDate != null && endDate
		// != null) {
		// LocalDate weekStart = startDate;
		// LocalDate weekEnd = endDate;
		// query.append(" AND s.scheduleDate BETWEEN ? AND ? ");
		// System.out.println("weekStart---->" + weekStart);
		// System.out.println("weekEnd---->" + weekEnd);
		// params.add(weekStart);
		// params.add(weekEnd);
		// } else if ("Month".equalsIgnoreCase(tabValue) && startDate != null && endDate
		// != null) {
		// LocalDate monthStart = startDate;
		// LocalDate monthEnd = endDate;
		// LocalDate fiveWeeksLater = monthEnd.plusWeeks(5);
		// query.append(" AND s.scheduleDate BETWEEN ? AND ? ");
		// System.out.println("weekStart---->" + monthStart);
		// System.out.println("weekEnd---->" + fiveWeeksLater);
		// params.add(monthStart);
		// params.add(fiveWeeksLater);
		// }
		return jdbcTemplate.query(query.toString(), (rs, rowNum) -> {
			Timestamp startTime = rs.getTimestamp("startdatetime");
			Timestamp endTime = rs.getTimestamp("enddatetime");

			double duration = calculateDuration(startTime, endTime);

			return new ScheduleResponse(rs.getString("comments"), formatTime(startTime), formatTime(endTime), duration,
					rs.getDate("daywisedate"),
					new User(rs.getString("dempoid"), rs.getInt("userid"), rs.getString("userName")),
					new Projects(rs.getInt("projectid"), rs.getString("projectcolor"), rs.getString("projectname")),
					rs.getLong("preschedulekey"));
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

	@Override
	public GeneralResponse updateSchedule(ShiftScheduleRequest request) {
		GeneralResponse response = new GeneralResponse();
		response.Message = "";
		Set<String> errorMessages = new LinkedHashSet<>();

		if (request.getId() == null) {
			response.Message = "Error: Missing Schedule ID.";
			return response;
		}
		String updateQuery = """
				    UPDATE core.schedules
				    SET projectId = ?, comments = ?, startDateTime = ?, endDateTime = ?,
				        status = ?, entryby = ?, entrydt = NOW()
				    WHERE scheduleId = ?
				""";

		try {
			LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate());
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

			LocalTime startTime = LocalTime.parse(request.getStartTime(), timeFormatter);
			LocalTime endTime = LocalTime.parse(request.getEndTime(), timeFormatter);

			LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, startTime);
			LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, endTime);

			int rowsUpdated = this.jdbcTemplate.update(updateQuery, request.getProjectId(), request.getComments(),
					startDateTime, endDateTime, "0", request.getEntryby(), request.getId());

			if (rowsUpdated == 0) {
				response.Message = "No matching schedule found to update.";
			} else {
				response.Message = "Schedule updated successfully.";
			}

		} catch (DateTimeParseException e) {
			response.Message = "Invalid date/time format: " + e.getMessage();
		} catch (Exception e) {
			response.Message = "Error processing request: " + e.getMessage();
		}

		return response;
	}

}
