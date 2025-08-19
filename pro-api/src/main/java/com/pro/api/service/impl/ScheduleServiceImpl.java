package com.pro.api.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.models.dataaccess.AdminOption;
import com.pro.api.models.dataaccess.CoreHour;
import com.pro.api.models.dataaccess.repos.AdminOptionRepository;
import com.pro.api.models.dataaccess.repos.CoreHourRepository;
import com.pro.api.response.Projects;
import com.pro.api.response.ScheduleResponse;
import com.pro.api.response.ShiftScheduleRequest;
import com.pro.api.response.User;
import com.pro.api.service.AuditService;
import com.pro.api.service.ScheduleService;

@Service
public class ScheduleServiceImpl implements ScheduleService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${isLocal}")
	private Boolean isLocal;

	@Autowired
	private AdminOptionRepository adminOptionRepository;

	@Autowired
	private CoreHourRepository coreHourRepository;

	@Autowired
	private AuditService auditService;

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
		ZoneId localZone = ZoneId.systemDefault();

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
				if (isLocal) {
					utcStart = zonedStart.withZoneSameInstant(utcZone);
					utcEnd = zonedEnd.withZoneSameInstant(utcZone);
				} else {
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
				this.auditService.updateNetId(request.getEntryby());
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
			String sql = "SELECT preScheduleKey FROM core.schedules ORDER BY preScheduleKey DESC LIMIT 1";
			Long preScheduleKey = jdbcTemplate.queryForObject(sql, Long.class);
			ScheduleResponse res = new ScheduleResponse();
			res.setPreschedulekey(preScheduleKey);
			response.Subject = res;
			response.Message = "Schedule saved successfully!";
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
		query.append(
				"SELECT s.preschedulekey, u.language, u.dempoid, u.userid, CONCAT(u.fname, ' ', u.lname) AS userName, ");
		query.append("p.projectid, p.projectcolor, p.projectname, ");
		query.append("s.startdatetime, s.enddatetime, ");
		query.append("s.comments, s.scheduleDate AS daywisedate, ch.corehours1 ");
		query.append("FROM core.schedules s ");
		query.append("JOIN core.projects p ON s.projectid = p.projectid ");
		query.append("LEFT JOIN core.users u ON s.dempoid = u.dempoid ");
		query.append("LEFT JOIN core.corehours ch ON s.dempoid = ch.dempoid ");
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

			Timestamp timestamp = rs.getTimestamp("daywisedate");
			this.isDstEndDate(timestamp);
			if (timestamp != null) {
				boolean isDstEnd = isDstEndDate(timestamp);
				if (isDstEnd) {
					startTime = addOneHour(startTime);
					endTime = addOneHour(endTime);
				}
			}

			double duration = calculateDuration(startTime, endTime);

			return new ScheduleResponse(rs.getString("comments"), formatTime(startTime), formatTime(endTime), duration,
					rs.getDate("daywisedate"),
					new User(rs.getString("dempoid"), rs.getInt("userid"), rs.getString("userName")),
					new Projects(rs.getInt("projectid"), rs.getString("projectcolor"), rs.getString("projectname")),
					rs.getLong("preschedulekey"), rs.getString("language"), rs.getInt("corehours1"));
		}, params.toArray());
	}

	public Timestamp addOneHour(Timestamp original) {
		if (original == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(original.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		return new Timestamp(cal.getTimeInMillis());
	}

	public boolean isDstEndDate(Timestamp timestamp) {
		if (timestamp == null)
			return false;

		TimeZone tz = TimeZone.getDefault();

		// Use Calendar to extract the year properly
		Calendar calendar = Calendar.getInstance(tz);
		calendar.setTime(timestamp);
		int year = calendar.get(Calendar.YEAR);

		Calendar cal = Calendar.getInstance(tz);
		cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Date prevDate = cal.getTime();
		boolean prevInDst = tz.inDaylightTime(prevDate);

		Date dstStart = null;
		Date dstEnd = null;

		for (int day = 1; day <= 366; day++) {
			cal.add(Calendar.DAY_OF_YEAR, 1);
			Date currentDate = cal.getTime();
			boolean currentInDst = tz.inDaylightTime(currentDate);

			if (prevInDst != currentInDst) {
				if (currentInDst) {
					dstStart = currentDate;
				} else {
					dstEnd = currentDate;
				}
			}
			prevInDst = currentInDst;
		}

		if (dstEnd != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(dstEnd);
			c.add(Calendar.DAY_OF_YEAR, -1);
			dstEnd = c.getTime();
			System.out.println("Year: " + year);
			Date inputDateMidnight = truncateTime(timestamp);
			Date dstEndMidnight = truncateTime(dstEnd);

			System.out.println("DST End Date: " + dstEnd);
			System.out.println("Input Date: " + inputDateMidnight);
			return inputDateMidnight.equals(dstEndMidnight);

		} else {
			return false;
		}

	}

	private Date truncateTime(Date date) {
		if (date == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
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

			ZoneId localZone = ZoneId.systemDefault();
			ZoneId utcZone = ZoneOffset.UTC;

			LocalDateTime localStartDateTime = LocalDateTime.of(scheduleDate, startTime);
			LocalDateTime localEndDateTime = LocalDateTime.of(scheduleDate, endTime);

			ZonedDateTime zonedStart = localStartDateTime.atZone(localZone);
			ZonedDateTime zonedEnd = localEndDateTime.atZone(localZone);

			ZonedDateTime utcStart = zonedStart.withZoneSameInstant(utcZone).plusHours(4);
			ZonedDateTime utcEnd = zonedEnd.withZoneSameInstant(utcZone).plusHours(4);
			if (isLocal) {
				utcStart = zonedStart.withZoneSameInstant(utcZone);
				utcEnd = zonedEnd.withZoneSameInstant(utcZone);
			} else {
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
			this.auditService.updateNetId(request.getEntryby());
			response.Message = "Schedule updated successfully!";
		} catch (DateTimeParseException e) {
			e.printStackTrace();
			response.Message = "Invalid date/time format: " + e.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			response.Message = "Error updating schedule: " + e.getMessage();
		}

		return response;
	}

	@Override
	public GeneralResponse deleteSchedule(Long id, String netId) {
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
				this.auditService.updateNetId(netId);
				response.Message = "Schedule deleted successfully!";
			}
		} catch (Exception e) {
			response.Message = "Error deleting schedule: " + e.getMessage();
		}

		return response;
	}

	@Override
	public GeneralResponse getOptionValue() {
		AdminOption obj = adminOptionRepository.findByAdminOptionsId(9);
		GeneralResponse res = new GeneralResponse();
		res.Subject = obj;
		return res;
	}

	@Override
	public GeneralResponse getCoreHours(LocalDate scheduleDate, String dempoId) {
		int month = scheduleDate.getMonthValue();
		int year = scheduleDate.getYear();

		CoreHour coreHour = coreHourRepository.findFirstByDempoidAndMonthYear(dempoId, month, year);
		GeneralResponse response = new GeneralResponse();
		Integer coreHoursValue = 0;

		if (coreHour == null) {
			response.Subject = coreHoursValue;
			return response;
		}

		YearMonth target = YearMonth.from(scheduleDate);

		if (coreHour.getMonth1() != null && YearMonth.from(coreHour.getMonth1()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours1();
		} else if (coreHour.getMonth2() != null && YearMonth.from(coreHour.getMonth2()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours2();
		} else if (coreHour.getMonth3() != null && YearMonth.from(coreHour.getMonth3()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours3();
		} else if (coreHour.getMonth4() != null && YearMonth.from(coreHour.getMonth4()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours4();
		} else if (coreHour.getMonth5() != null && YearMonth.from(coreHour.getMonth5()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours5();
		} else if (coreHour.getMonth6() != null && YearMonth.from(coreHour.getMonth6()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours6();
		} else if (coreHour.getMonth7() != null && YearMonth.from(coreHour.getMonth7()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours7();
		} else if (coreHour.getMonth8() != null && YearMonth.from(coreHour.getMonth8()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours8();
		} else if (coreHour.getMonth9() != null && YearMonth.from(coreHour.getMonth9()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours9();
		} else if (coreHour.getMonth10() != null && YearMonth.from(coreHour.getMonth10()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours10();
		} else if (coreHour.getMonth11() != null && YearMonth.from(coreHour.getMonth11()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours11();
		} else if (coreHour.getMonth12() != null && YearMonth.from(coreHour.getMonth12()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours12();
		} else if (coreHour.getMonth13() != null && YearMonth.from(coreHour.getMonth13()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours13();
		} else if (coreHour.getMonth14() != null && YearMonth.from(coreHour.getMonth14()).equals(target)) {
			coreHoursValue = coreHour.getCoreHours14();
		}

		response.Subject = coreHoursValue;
		return response;
	}

}
