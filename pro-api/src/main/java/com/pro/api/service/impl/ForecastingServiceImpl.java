package com.pro.api.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;
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
				+ "FROM core.users u " + "LEFT JOIN core.corehours c ON u.dempoid = c.dempoid "
				+ "WHERE u.active = 'true' AND u.status = '1'";

		List<ForecastingResponse> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
			ForecastingResponse response = new ForecastingResponse();
			response.setDempoid(rs.getString("dempoid"));
			response.setFname(rs.getString("fname"));
			response.setLname(rs.getString("lname"));

			Map<LocalDate, Integer> monthHoursMap = new LinkedHashMap<>();
			for (int i = 1; i <= 14; i++) {
				Date date = rs.getDate("month" + i);
				Integer hours = rs.getObject("corehours" + i, Integer.class);
				if (date != null && hours != null) {
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

}
