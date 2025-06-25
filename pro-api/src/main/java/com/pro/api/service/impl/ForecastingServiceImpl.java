package com.pro.api.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
	public PageResponse<ForecastingResponse> getList() {
		String sql = "SELECT u.dempoid, u.fname, u.lname, " + "c.month1, c.corehours1, " + "c.month2, c.corehours2, "
				+ "c.month3, c.corehours3, " + "c.month4, c.corehours4, " + "c.month5, c.corehours5, "
				+ "c.month6, c.corehours6, " + "c.month7, c.corehours7, " + "c.month8, c.corehours8, "
				+ "c.month9, c.corehours9, " + "c.month10, c.corehours10, " + "c.month11, c.corehours11, "
				+ "c.month12, c.corehours12, " + "c.month13, c.corehours13, " + "c.month14, c.corehours14 "
				+ "FROM  core.corehours c INNER JOIN core.users u ON u.dempoid = c.dempoid " + "WHERE  u.status = '1'";

		List<ForecastingResponse> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
			ForecastingResponse response = new ForecastingResponse();
			response.setDempoid(rs.getString("dempoid"));
			response.setFname(rs.getString("fname"));
			response.setLname(rs.getString("lname"));

			Map<LocalDate, Integer> monthHoursMap = new LinkedHashMap<>();
			for (int i = 1; i <= 14; i++) {
				Date date = rs.getDate("month" + i);
				Integer hours = rs.getObject("corehours" + i, Integer.class);
				if (hours == null)
					hours = 0;
				if (date != null) {
					monthHoursMap.put(date.toLocalDate(), hours);
				}
			}

			response.setCoreHoursByMonth(monthHoursMap);
			return response;
		});

		PageResponse<ForecastingResponse> response = new PageResponse<>();
		response.setData(result);
		response.setCount(result.size());
		return response;
	}

	public GeneralResponse updateCoreHours(List<CoreHoursRequest> requests) {
		GeneralResponse response = new GeneralResponse();

		if (requests != null && !requests.isEmpty()) {
			for (CoreHoursRequest request : requests) {
				String dempoid = request.getDempoId();

				String selectSql = "SELECT * FROM core.corehours WHERE dempoid = ?";
				List<Map<String, Object>> records = jdbcTemplate.queryForList(selectSql, dempoid);
				if (records.isEmpty()) {
					continue;
				}

				Map<String, Object> record = records.get(0);
				LocalDate baseDate = LocalDate.now();

				for (int i = 0; i < 14; i++) {
					YearMonth targetMonth = YearMonth.from(baseDate.plusMonths(i));
					String monthColumn = "month" + (i + 1);
					String coreHourColumn = "corehours" + (i + 1);

					Date existingDate = (Date) record.get(monthColumn);
					Object coreHourValue = record.get(coreHourColumn);

					boolean needsUpdate = false;
					Object[] params = new Object[4];

					if (existingDate == null || !YearMonth.from(existingDate.toLocalDate()).equals(targetMonth)) {
						params[1] = targetMonth.atDay(1);
						needsUpdate = true;
					} else {
						params[1] = existingDate.toLocalDate();
					}

					if (coreHourValue == null
							|| (coreHourValue instanceof Number && ((Number) coreHourValue).intValue() == 0)) {
						params[0] = request.getCoreHours();
						needsUpdate = true;
					} else {
						params[0] = coreHourValue;
					}

					if (needsUpdate) {
						params[2] = LocalDate.now();
						params[3] = dempoid;

						String updateSql = "UPDATE core.corehours SET " + coreHourColumn + " = ?, " + monthColumn
								+ " = ?, ModDt = ? WHERE dempoid = ?";
						jdbcTemplate.update(updateSql, params);
					}
				}
			}
		}

		response.Status = "success";
		response.Message = "Core hours updated successfully";
		return response;
	}

	@Override
	public PageResponse<ForecastingResponse> getProjectCoreHoursList() {
		String sql = "SELECT p.projectid, p.projectname, p.projectcolor, fh.month1, fh.forecasthours1, fh.month2, fh.forecasthours2, fh.month3, fh.forecasthours3, "
				+ " fh.month4, fh.forecasthours4, fh.month5, fh.forecasthours5, fh.month6, fh.forecasthours6, "
				+ " fh.month7, fh.forecasthours7, fh.month8, fh.forecasthours8, fh.month9, fh.forecasthours9, "
				+ " fh.month10, fh.forecasthours10, fh.month11, fh.forecasthours11, fh.month12, fh.forecasthours12, "
				+ " fh.month13, fh.forecasthours13, fh.month14, fh.forecasthours14 FROM core.projects p "
				+ " INNER JOIN core.forecasthours fh ON p.projectid = fh.projectid "
				+ " WHERE p.active = 1 and p.projecttype = 2 ";

		List<ForecastingResponse> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
			ForecastingResponse response = new ForecastingResponse();
			response.setProjectColor(rs.getString("projectcolor"));
			response.setProjectName(rs.getString("projectname"));

			Map<LocalDate, Integer> monthHoursMap = new LinkedHashMap<>();
			for (int i = 1; i <= 14; i++) {
				Date date = rs.getDate("month" + i);
				Integer hours = rs.getObject("forecasthours" + i, Integer.class);
				if (hours == null)
					hours = 0;
				if (date != null) {
					monthHoursMap.put(date.toLocalDate(), hours);
				}
			}
			response.setCoreHoursByMonth(monthHoursMap);
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

}
