package com.pro.api.service.impl;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;
import com.pro.api.service.CoreHoursRequest;
import com.pro.api.service.ForecastingService;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ForecastingServiceImpl implements ForecastingService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public PageResponse<ForecastingResponse> getList(String codeValues) {
		String sql = "SELECT u.dempoid, u.fname, u.lname, " + "c.corehoursid, "
				+ "c.corehours1, c.corehours2, c.corehours3, c.corehours4, c.corehours5, c.corehours6, "
				+ "c.corehours7, c.corehours8, c.corehours9, c.corehours10, c.corehours11, c.corehours12, "
				+ "c.corehours13, c.corehours14 " + "FROM core.corehours c "
				+ "INNER JOIN core.users u ON u.dempoid = c.dempoid " + "WHERE u.status = '1'";

		if (codeValues != null && !codeValues.isEmpty() && !codeValues.equals("0")) {
			sql += " AND u.role = " + codeValues;
		}

		sql += " ORDER BY u.fname ASC";

		List<ForecastingResponse> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
			ForecastingResponse response = new ForecastingResponse();
			response.setCoreHoursId(rs.getLong("corehoursid"));
			response.setFname(rs.getString("fname"));
			response.setLname(rs.getString("lname"));

			LocalDate baseMonth = LocalDate.now().withDayOfMonth(1);
			List<Pair<LocalDate, Integer>> monthHoursList = new ArrayList<>();

			for (int i = 0; i < 14; i++) {
				Integer hours = rs.getObject("corehours" + (i + 1), Integer.class);
				int value = (hours != null) ? hours : 0;
				monthHoursList.add(Pair.of(baseMonth.plusMonths(i), value));
			}

			response.setCoreHoursByMonth(monthHoursList);
			return response;
		});

		PageResponse<ForecastingResponse> response = new PageResponse<>();
		response.setData(result);
		response.setCount(result.size());
		return response;
	}

	public GeneralResponse updateForeCastingHours(List<CoreHoursRequest> requests) {
		GeneralResponse response = new GeneralResponse();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		for (CoreHoursRequest request : requests) {
			int val = getValue(request.getDate());

			if (val < 1 || val > 14) {
				continue;
			}

			LocalDate parsedDate = LocalDate.parse(request.getDate(), formatter);
			Date sqlDate = Date.valueOf(parsedDate);

			String sql = "UPDATE core.forecasthours SET moddt = NOW(), forecasthours" + val + " = ?, month" + val
					+ " = ?, entryby = ? WHERE forecasthoursid = ?";
			this.jdbcTemplate.update(sql, request.getCoreHours(), sqlDate, request.getEntryBy(),
					request.getForecastHoursId());
		}

		response.Status = "success";
		response.Message = "Core hours updated successfully";
		return response;
	}

	@Override
	public PageResponse<ForecastingResponse> getProjectCoreHoursList(String codeValues) {
		String sql = "SELECT p.projectid, p.projectname, p.projectcolor, fh.forecasthoursid, "
				+ "fh.month1 AS month1, fh.forecasthours1 AS forecasthours1, "
				+ "fh.month2 AS month2, fh.forecasthours2 AS forecasthours2, "
				+ "fh.month3 AS month3, fh.forecasthours3 AS forecasthours3, "
				+ "fh.month4 AS month4, fh.forecasthours4 AS forecasthours4, "
				+ "fh.month5 AS month5, fh.forecasthours5 AS forecasthours5, "
				+ "fh.month6 AS month6, fh.forecasthours6 AS forecasthours6, "
				+ "fh.month7 AS month7, fh.forecasthours7 AS forecasthours7, "
				+ "fh.month8 AS month8, fh.forecasthours8 AS forecasthours8, "
				+ "fh.month9 AS month9, fh.forecasthours9 AS forecasthours9, "
				+ "fh.month10 AS month10, fh.forecasthours10 AS forecasthours10, "
				+ "fh.month11 AS month11, fh.forecasthours11 AS forecasthours11, "
				+ "fh.month12 AS month12, fh.forecasthours12 AS forecasthours12, "
				+ "fh.month13 AS month13, fh.forecasthours13 AS forecasthours13, "
				+ "fh.month14 AS month14, fh.forecasthours14 AS forecasthours14 " + "FROM core.projects p "
				+ "INNER JOIN core.forecasthours fh ON p.projectid = fh.projectid "
				+ "WHERE p.active = 1 AND p.projecttype = 2 ";
		if (codeValues != null && !codeValues.isEmpty() && !codeValues.equals("0")) {
			sql += "AND p.projectid IN (" + codeValues + ") ";
		}

		sql += "ORDER BY p.projectname ASC";
		List<ForecastingResponse> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
			ForecastingResponse response = new ForecastingResponse();
			response.setForecastHoursId(rs.getLong("forecasthoursid"));
			response.setProjectColor(rs.getString("projectcolor"));
			response.setProjectName(rs.getString("projectname"));

			LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
			Map<LocalDate, Integer> monthTotals = new LinkedHashMap<>();
			for (int i = 0; i < 14; i++) {
				monthTotals.put(currentMonth.plusMonths(i), 0);
			}

			for (int i = 1; i <= 14; i++) {
				Date date = rs.getDate("month" + i);
				Integer hours = rs.getObject("forecasthours" + i, Integer.class);
				if (date != null && hours != null) {
					LocalDate recordMonth = date.toLocalDate().withDayOfMonth(1);
					if (monthTotals.containsKey(recordMonth)) {
						monthTotals.put(recordMonth, monthTotals.get(recordMonth) + hours);
					}
				}
			}

			List<Pair<LocalDate, Integer>> monthHoursList = new ArrayList<>();
			for (Map.Entry<LocalDate, Integer> entry : monthTotals.entrySet()) {
				monthHoursList.add(Pair.of(entry.getKey(), entry.getValue()));
			}
			response.setCoreHoursByMonth(monthHoursList);
			return response;
		});

		PageResponse<ForecastingResponse> response = new PageResponse<>();
		response.setData(result);
		response.setCount(result.size());
		return response;
	}

	@Override
	public List<Long> getUserTotalHours(String codeValues) {
		String sql = "SELECT " + "c.corehours1, c.corehours2, c.corehours3, c.corehours4, c.corehours5, c.corehours6, "
				+ "c.corehours7, c.corehours8, c.corehours9, c.corehours10, c.corehours11, c.corehours12, "
				+ "c.corehours13, c.corehours14 " + "FROM core.corehours c "
				+ "INNER JOIN core.users u ON u.dempoid = c.dempoid " + "WHERE u.status = '1'";

		if (codeValues != null && !codeValues.isEmpty() && !codeValues.equals("0")) {
			sql += " AND u.role = " + codeValues;
		}

		return jdbcTemplate.query(sql, rs -> {
			List<Long> totals = new ArrayList<>(Collections.nCopies(14, 0L));

			while (rs.next()) {
				for (int i = 0; i < 14; i++) {
					Integer hours = rs.getObject("corehours" + (i + 1), Integer.class);
					if (hours != null) {
						totals.set(i, totals.get(i) + hours);
					}
				}
			}

			return totals;
		});
	}

	@Override
	public List<Long> getProjectTotalHours(String codeValues) {
		String sql = "SELECT fh.month1, fh.forecasthours1, fh.month2, fh.forecasthours2, "
				+ "fh.month3, fh.forecasthours3, fh.month4, fh.forecasthours4, "
				+ "fh.month5, fh.forecasthours5, fh.month6, fh.forecasthours6, "
				+ "fh.month7, fh.forecasthours7, fh.month8, fh.forecasthours8, "
				+ "fh.month9, fh.forecasthours9, fh.month10, fh.forecasthours10, "
				+ "fh.month11, fh.forecasthours11, fh.month12, fh.forecasthours12, "
				+ "fh.month13, fh.forecasthours13, fh.month14, fh.forecasthours14 " + "FROM core.projects p "
				+ "INNER JOIN core.forecasthours fh ON p.projectid = fh.projectid "
				+ "WHERE p.active = 1 AND p.projecttype = 2 ";
		if (codeValues != null && !codeValues.isEmpty() && !codeValues.equals("0")) {
			sql += " AND p.projectid IN (" + codeValues + ") ";
		}

		return jdbcTemplate.query(sql, rs -> {
			LocalDate baseMonth = LocalDate.now().withDayOfMonth(1);
			List<LocalDate> expectedMonths = new ArrayList<>();
			for (int i = 0; i < 14; i++) {
				expectedMonths.add(baseMonth.plusMonths(i));
			}
			List<Long> totals = new ArrayList<>(Collections.nCopies(14, 0L));

			while (rs.next()) {
				for (int i = 1; i <= 14; i++) {
					Date date = rs.getDate("month" + i);
					Integer hours = rs.getObject("forecasthours" + i, Integer.class);

					if (date != null && hours != null) {
						LocalDate recordMonth = date.toLocalDate().withDayOfMonth(1);
						for (int j = 0; j < 14; j++) {
							if (recordMonth.equals(expectedMonths.get(j))) {
								totals.set(j, totals.get(j) + hours);
								break;
							}
						}
					}
				}
			}
			return totals;
		});
	}

	public int getValue(String date) {
		try {
			LocalDate inputDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			LocalDate now = LocalDate.now();

			LocalDate currentMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
			LocalDate inputMonth = LocalDate.of(inputDate.getYear(), inputDate.getMonth(), 1);

			long monthsBetween = ChronoUnit.MONTHS.between(currentMonth, inputMonth);

			if (monthsBetween >= 0 && monthsBetween < 14) {
				return (int) monthsBetween + 1;
			} else {
				return 0;
			}

		} catch (Exception e) {
			return 0;
		}
	}
}
