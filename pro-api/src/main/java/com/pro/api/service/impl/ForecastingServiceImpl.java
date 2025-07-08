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
	public PageResponse<ForecastingResponse> getProjectCoreHoursList() {
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

}
