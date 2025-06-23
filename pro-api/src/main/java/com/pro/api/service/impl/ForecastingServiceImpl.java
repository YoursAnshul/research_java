package com.pro.api.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.models.dataaccess.CoreHour;
import com.pro.api.models.dataaccess.repos.CoreHourRepository;
import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;
import com.pro.api.service.ForecastingService;

@Service
public class ForecastingServiceImpl implements ForecastingService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private CoreHourRepository coreHourRepository;

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

	public GeneralResponse updateCoreHours(String dempoId, LocalDate date, int coreHours) {
		GeneralResponse response = new GeneralResponse();
		CoreHour coreHourObj = coreHourRepository.findFirstByDempoid(dempoId);
		for (int i = 1; i <= 14; i++) {
			LocalDate monthValue = getMonthValue(coreHourObj, i);
			if (monthValue != null && monthValue.getYear() == date.getYear()
					&& monthValue.getMonthValue() == date.getMonthValue()) {
				setCoreHourValue(coreHourObj, i, coreHours);
				break;
			}
		}
		return response;
	}

	private LocalDate getMonthValue(CoreHour coreHour, int index) {
		switch (index) {
		case 1:
			return coreHour.getMonth1();
		case 2:
			return coreHour.getMonth2();
		case 3:
			return coreHour.getMonth3();
		case 4:
			return coreHour.getMonth4();
		case 5:
			return coreHour.getMonth5();
		case 6:
			return coreHour.getMonth6();
		case 7:
			return coreHour.getMonth7();
		case 8:
			return coreHour.getMonth8();
		case 9:
			return coreHour.getMonth9();
		case 10:
			return coreHour.getMonth10();
		case 11:
			return coreHour.getMonth11();
		case 12:
			return coreHour.getMonth12();
		case 13:
			return coreHour.getMonth13();
		case 14:
			return coreHour.getMonth14();
		default:
			return null;
		}
	}

	private void setCoreHourValue(CoreHour coreHour, int index, int value) {
		switch (index) {
		case 1:
			coreHour.setCoreHours1(value);
			break;
		case 2:
			coreHour.setCoreHours2(value);
			break;
		case 3:
			coreHour.setCoreHours3(value);
			break;
		case 4:
			coreHour.setCoreHours4(value);
			break;
		case 5:
			coreHour.setCoreHours5(value);
			break;
		case 6:
			coreHour.setCoreHours6(value);
			break;
		case 7:
			coreHour.setCoreHours7(value);
			break;
		case 8:
			coreHour.setCoreHours8(value);
			break;
		case 9:
			coreHour.setCoreHours9(value);
			break;
		case 10:
			coreHour.setCoreHours10(value);
			break;
		case 11:
			coreHour.setCoreHours11(value);
			break;
		case 12:
			coreHour.setCoreHours12(value);
			break;
		case 13:
			coreHour.setCoreHours13(value);
			break;
		case 14:
			coreHour.setCoreHours14(value);
			break;
		}
	}

}
