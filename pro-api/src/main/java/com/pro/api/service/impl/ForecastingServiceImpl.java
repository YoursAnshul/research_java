package com.pro.api.service.impl;

import java.util.List;

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
		StringBuilder sql = new StringBuilder(
				"SELECT c.dempoid, u.fname, u.lname, SUM(COALESCE(corehours1, 0)) AS corehours1, "
						+ "SUM(COALESCE(corehours2, 0)) AS corehours2, "
						+ "SUM(COALESCE(corehours3, 0)) AS corehours3, "
						+ "SUM(COALESCE(corehours4, 0)) AS corehours4, "
						+ "SUM(COALESCE(corehours5, 0)) AS corehours5, "
						+ "SUM(COALESCE(corehours6, 0)) AS corehours6, "
						+ "SUM(COALESCE(corehours7, 0)) AS corehours7, "
						+ "SUM(COALESCE(corehours8, 0)) AS corehours8, "
						+ "SUM(COALESCE(corehours9, 0)) AS corehours9, "
						+ "SUM(COALESCE(corehours10, 0)) AS corehours10, "
						+ "SUM(COALESCE(corehours11, 0)) AS corehours11, "
						+ "SUM(COALESCE(corehours12, 0)) AS corehours12, "
						+ "SUM(COALESCE(corehours13, 0)) AS corehours13, "
						+ "SUM(COALESCE(corehours14, 0)) AS corehours14 " + "FROM core.corehours c "
						+ "JOIN core.users u ON c.dempoid = u.dempoid " + "WHERE u.active = 'true' "
						+ "GROUP BY c.dempoid, u.fname, u.lname");

		List<ForecastingResponse> result = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			ForecastingResponse response = new ForecastingResponse();
			response.setDempoid(rs.getString("dempoid"));
			response.setFname(rs.getString("fname"));
			response.setLname(rs.getString("lname"));
			response.setCorehours1(rs.getDouble("corehours1"));
			response.setCorehours2(rs.getDouble("corehours2"));
			response.setCorehours3(rs.getDouble("corehours3"));
			response.setCorehours4(rs.getDouble("corehours4"));
			response.setCorehours5(rs.getDouble("corehours5"));
			response.setCorehours6(rs.getDouble("corehours6"));
			response.setCorehours7(rs.getDouble("corehours7"));
			response.setCorehours8(rs.getDouble("corehours8"));
			response.setCorehours9(rs.getDouble("corehours9"));
			response.setCorehours10(rs.getDouble("corehours10"));
			response.setCorehours11(rs.getDouble("corehours11"));
			response.setCorehours12(rs.getDouble("corehours12"));
			response.setCorehours13(rs.getDouble("corehours13"));
			response.setCorehours14(rs.getDouble("corehours14"));
			return response;
		});

		PageResponse<ForecastingResponse> response = new PageResponse<>();
		response.setData(result);
		response.setCount(result.size());
		return response;
	}

}
