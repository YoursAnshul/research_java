package com.pro.api.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;
import com.pro.api.service.CoreHoursRequest;
import com.pro.api.service.ForecastingService;

@Service
public class ForecastingServiceImpl implements ForecastingService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public PageResponse<ForecastingResponse> getList(Integer codeValues) {
		String sql = "SELECT u.dempoid, u.fname, u.lname, " + " c.corehoursid, c.month1, c.corehours1, "
				+ "c.month2, c.corehours2, " + "c.month3, c.corehours3, " + "c.month4, c.corehours4, "
				+ "c.month5, c.corehours5, " + "c.month6, c.corehours6, " + "c.month7, c.corehours7, "
				+ "c.month8, c.corehours8, " + "c.month9, c.corehours9, " + "c.month10, c.corehours10, "
				+ "c.month11, c.corehours11, " + "c.month12, c.corehours12, " + "c.month13, c.corehours13, "
				+ "c.month14, c.corehours14 "
				+ "FROM  core.corehours c INNER JOIN core.users u ON u.dempoid = c.dempoid " + "WHERE  u.status = '1' ";

		if (codeValues != null && codeValues > 0) {
			sql += " AND u.role = " + codeValues;
		}
		sql += " ORDER BY c.corehoursid DESC ";
		List<ForecastingResponse> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
			ForecastingResponse response = new ForecastingResponse();
			response.setCoreHoursId(rs.getLong("corehoursid"));
			response.setFname(rs.getString("fname"));
			response.setLname(rs.getString("lname"));

			List<Pair<LocalDate, Integer>> monthHoursList = new ArrayList<>();
			for (int i = 1; i <= 14; i++) {
				Date date = rs.getDate("month" + i);
				Integer hours = rs.getObject("corehours" + i, Integer.class);
				if (date != null) {
					monthHoursList.add(Pair.of(date.toLocalDate(), hours != null ? hours : 0));
				}
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
					+ " = ? WHERE forecasthoursid = ?";
			this.jdbcTemplate.update(sql, request.getCoreHours(), sqlDate, request.getForecastHoursId());
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
				+ "WHERE p.active = 1 AND p.projecttype = 2 order by fh.forecasthoursid DESC";

		List<ForecastingResponse> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
			ForecastingResponse response = new ForecastingResponse();
			response.setForecastHoursId(rs.getLong("forecasthoursid"));
			response.setProjectColor(rs.getString("projectcolor"));
			response.setProjectName(rs.getString("projectname"));

			List<Pair<LocalDate, Integer>> monthHoursList = new ArrayList<>();
			for (int i = 1; i <= 14; i++) {
				Date date = rs.getDate("month" + i);
				Integer hours = rs.getObject("forecasthours" + i, Integer.class);
				if (date != null) {
					monthHoursList.add(Pair.of(date.toLocalDate(), hours != null ? hours : 0));
				}
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
	public List<Long> getUserTotalHours() {
		String sql = "SELECT  SUM(c.corehours1), SUM(c.corehours2), SUM(c.corehours3), SUM(c.corehours4), SUM(c.corehours5), "
				+ " SUM(c.corehours6), SUM(c.corehours7), SUM(c.corehours8), SUM(c.corehours9), SUM(c.corehours10), SUM(c.corehours11), "
				+ " SUM(c.corehours12), SUM(c.corehours13), SUM(c.corehours14) "
				+ "FROM core.corehours c INNER JOIN core.users u ON u.dempoid = c.dempoid WHERE u.status = '1'";

		return jdbcTemplate.query(sql, rs -> {
			List<Long> result = new ArrayList<>();
			if (rs.next()) {
				for (int i = 1; i <= 14; i++) {
					result.add(rs.getLong(i));
				}
			}
			return result;
		});
	}

	@Override
	public List<Long> getProjectTotalHours() {
		String sql = "SELECT SUM(fh.forecasthours1), SUM(fh.forecasthours2), SUM(fh.forecasthours3), "
				+ "SUM(fh.forecasthours4), SUM(fh.forecasthours5), SUM(fh.forecasthours6), "
				+ "SUM(fh.forecasthours7), SUM(fh.forecasthours8), SUM(fh.forecasthours9), "
				+ "SUM(fh.forecasthours10), SUM(fh.forecasthours11), SUM(fh.forecasthours12), "
				+ "SUM(fh.forecasthours13), SUM(fh.forecasthours14) " + "FROM core.projects p "
				+ "INNER JOIN core.forecasthours fh ON p.projectid = fh.projectid "
				+ "WHERE p.active = 1 AND p.projecttype = 2";

		return jdbcTemplate.query(sql, rs -> {
			List<Long> totalHours = new ArrayList<>();
			if (rs.next()) {
				for (int i = 1; i <= 14; i++) {
					totalHours.add(rs.getLong(i));
				}
			}
			return totalHours;
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

	@Override
	public GeneralResponse updateCoreHours(List<CoreHoursRequest> requests) {
		GeneralResponse response = new GeneralResponse();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		for (CoreHoursRequest request : requests) {
			int val = getValue(request.getDate());

			if (val < 1 || val > 14) {
				continue;
			}

			LocalDate parsedDate = LocalDate.parse(request.getDate(), formatter);
			Date sqlDate = Date.valueOf(parsedDate);

			String sql = "UPDATE core.corehours SET moddt = NOW(), corehours" + val + " = ?, month" + val
					+ " = ? WHERE corehoursid = ?";
			this.jdbcTemplate.update(sql, request.getCoreHours(), sqlDate, request.getCoreHoursId());
		}

		response.Status = "success";
		response.Message = "Core hours updated successfully";
		return response;
	}

}
