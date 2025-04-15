package com.pro.api.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${isLocal}")
	private Boolean isLocal;

	public GeneralResponse saveSchedule(List<ShiftScheduleRequest> list) {
		GeneralResponse response = new GeneralResponse();
		Set<String> errorMessages = new LinkedHashSet<>();
		int successCount = 0;
		int duplicateCount = 0;

		String checkQuery = "SELECT COUNT(*) FROM core.schedules WHERE dempoId = ? AND scheduleDate = ? "
				+ "AND startDateTime < ? AND endDateTime > ?";

		String insertQuery = "INSERT INTO core.schedules (dempoId, scheduleDate, projectId, comments, startDateTime, endDateTime, status, entryby, entrydt, machinename) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
		ZoneId localZone = ZoneId.systemDefault(); // or specify your expected input zone, e.g.,
													// ZoneId.of("Asia/Kolkata")
		ZoneId utcZone = ZoneOffset.UTC;

		for (ShiftScheduleRequest request : list) {
			try {
				if (request.getScheduleDate() == null || request.getStartTime() == null
						|| request.getEndTime() == null) {
					errorMessages.add("Missing required fields for DempoId: " + request.getDempoId());
					continue;
				}

				LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate());
				LocalTime startTime = LocalTime.parse(request.getStartTime(), timeFormatter);
				LocalTime endTime = LocalTime.parse(request.getEndTime(), timeFormatter);

				// Combine into LocalDateTime in local zone
				LocalDateTime localStartDateTime = LocalDateTime.of(scheduleDate, startTime);
				LocalDateTime localEndDateTime = LocalDateTime.of(scheduleDate, endTime);

				if (!localEndDateTime.isAfter(localStartDateTime)) {
					errorMessages.add("End time must be after start time for DempoId: " + request.getDempoId());
					continue;
				}

				ZonedDateTime zonedStart = localStartDateTime.atZone(localZone);
				ZonedDateTime zonedEnd = localEndDateTime.atZone(localZone);

				ZonedDateTime utcStart = zonedStart.withZoneSameInstant(utcZone).plusHours(4);
				ZonedDateTime utcEnd = zonedEnd.withZoneSameInstant(utcZone).plusHours(4);
				if(isLocal) {
					 utcStart = zonedStart.withZoneSameInstant(utcZone);
					 utcEnd = zonedEnd.withZoneSameInstant(utcZone);	
				}else {
					 utcStart = zonedStart.withZoneSameInstant(utcZone).plusHours(4);
					 utcEnd = zonedEnd.withZoneSameInstant(utcZone).plusHours(4);
				}

				LocalDateTime startDateTimeUtc = utcStart.toLocalDateTime();
				LocalDateTime endDateTimeUtc = utcEnd.toLocalDateTime();

				Integer existingCount = this.jdbcTemplate.queryForObject(checkQuery, Integer.class,
						request.getDempoId(), scheduleDate, endDateTimeUtc, startDateTimeUtc);

				if (existingCount != null && existingCount > 0) {
					duplicateCount++;
					errorMessages.add("Duplicate schedule found for DempoId: " + request.getDempoId());
					continue;
				}

				this.jdbcTemplate.update(insertQuery, request.getDempoId(), scheduleDate, request.getProjectId(),
						request.getComments(), startDateTimeUtc, endDateTimeUtc, "0", request.getEntryby(), new Date(),
						"NA");

				successCount++;

			} catch (DateTimeParseException e) {
				errorMessages
						.add("Invalid date/time format for DempoId: " + request.getDempoId() + " -> " + e.getMessage());
			} catch (Exception e) {
				errorMessages
						.add("Error processing request for DempoId: " + request.getDempoId() + " -> " + e.getMessage());
			}
		}

		if (successCount > 0 && duplicateCount == 0) {
			response.Message = "Schedules saved successfully!";
		} else if (duplicateCount > 0) {
			response.Message = "Schedule already exists for this user!";
		} else {
			response.Message = "No schedules were saved.";
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

		if (dempoId != null && !dempoId.isBlank()) {
			query.append(" AND s.dempoid = ? ");
			params.add(dempoId);
		}

		if (projectId != null && projectId > 0) {
			query.append(" AND p.projectid = ? ");
			params.add(projectId);
		}
		query.append("ORDER BY userName desc ");

		return jdbcTemplate.query(query.toString(), (rs, rowNum) -> {
			Timestamp startTime = rs.getTimestamp("startdatetime");
			Timestamp endTime = rs.getTimestamp("enddatetime");
			System.out.println("startTime------------" + startTime);
			System.out.println("endTime------------" + endTime);
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
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

		try {
			if (request.getScheduleDate() == null || request.getStartTime() == null || request.getEndTime() == null
					|| request.getId() == null) {
				response.Message = "Missing required fields.";
				return response;
			}

			LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate());
			LocalTime startTime = LocalTime.parse(request.getStartTime(), timeFormatter);
			LocalTime endTime = LocalTime.parse(request.getEndTime(), timeFormatter);

			if (!endTime.isAfter(startTime)) {
				response.Message = "End time must be after start time.";
				return response;
			}

			// Convert to UTC
			ZoneId localZone = ZoneId.systemDefault(); // or ZoneId.of("America/New_York")
			ZoneId utcZone = ZoneOffset.UTC;

			LocalDateTime localStartDateTime = LocalDateTime.of(scheduleDate, startTime);
			LocalDateTime localEndDateTime = LocalDateTime.of(scheduleDate, endTime);

			ZonedDateTime zonedStart = localStartDateTime.atZone(localZone);
			ZonedDateTime zonedEnd = localEndDateTime.atZone(localZone);

			ZonedDateTime utcStart = zonedStart.withZoneSameInstant(utcZone).plusHours(4);
			ZonedDateTime utcEnd = zonedEnd.withZoneSameInstant(utcZone).plusHours(4);
			if(isLocal) {
				utcStart = zonedStart.withZoneSameInstant(utcZone);
				utcEnd = zonedEnd.withZoneSameInstant(utcZone);	
			}else {
				utcStart = zonedStart.withZoneSameInstant(utcZone).plusHours(4);
				utcEnd = zonedEnd.withZoneSameInstant(utcZone).plusHours(4);
			}

			LocalDateTime startDateTimeUtc = utcStart.toLocalDateTime();
			LocalDateTime endDateTimeUtc = utcEnd.toLocalDateTime();

			String fetchQuery = "SELECT preschedulekey, startDateTime, endDateTime FROM core.schedules WHERE dempoId = ? AND scheduleDate = ? AND preschedulekey != ?";
			List<Map<String, Object>> existingSchedules = this.jdbcTemplate.queryForList(fetchQuery,
					request.getDempoId(), scheduleDate, request.getId());

			for (Map<String, Object> schedule : existingSchedules) {
				LocalDateTime existingStart = ((Timestamp) schedule.get("startDateTime")).toLocalDateTime();
				LocalDateTime existingEnd = ((Timestamp) schedule.get("endDateTime")).toLocalDateTime();

				if (existingStart.equals(startDateTimeUtc) && existingEnd.equals(endDateTimeUtc)) {
					response.Message = "Schedule exists with the same time range.";
					return response;
				}

				if (startDateTimeUtc.isBefore(existingEnd) && endDateTimeUtc.isAfter(existingStart)) {
					response.Message = "Schedule exists with the same time range.";
					return response;
				}
			}

			String updateQuery = "UPDATE core.schedules SET scheduleDate = ?, projectId = ?, comments = ?, startDateTime = ?, endDateTime = ?, status = ?, entryby = ?, entrydt = ?, machinename = ? WHERE preschedulekey = ?";
			this.jdbcTemplate.update(updateQuery, scheduleDate, request.getProjectId(), request.getComments(),
					startDateTimeUtc, endDateTimeUtc, "0", request.getEntryby(), new Date(), "NA", request.getId());

			response.Message = "Schedule updated successfully!";
		} catch (DateTimeParseException e) {
			response.Message = "Invalid date/time format: " + e.getMessage();
		} catch (Exception e) {
			response.Message = "Error updating schedule: " + e.getMessage();
		}

		return response;
	}

	@Override
	public GeneralResponse deleteSchedule(Long id) {
		GeneralResponse response = new GeneralResponse();
		response.Message = "";

		if (id == null) {
			response.Message = "Error: Missing Schedule ID.";
			return response;
		}

		String deleteQuery = "DELETE FROM core.schedules WHERE preschedulekey = ?";

		try {
			int rowsDeleted = this.jdbcTemplate.update(deleteQuery, id);

			if (rowsDeleted == 0) {
				response.Message = "No matching schedule found to delete.";
			} else {
				response.Message = "Schedule deleted successfully.";
			}
		} catch (Exception e) {
			response.Message = "Error deleting schedule: " + e.getMessage();
		}

		return response;
	}

}
