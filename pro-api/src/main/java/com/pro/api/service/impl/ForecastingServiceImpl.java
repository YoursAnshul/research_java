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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(ForecastingServiceImpl.class);

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
		if (requests == null || requests.isEmpty()) {
			response.Status = "error";
			response.Message = "No requests provided";
			return response;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate baseMonth = LocalDate.now().withDayOfMonth(1);

		for (CoreHoursRequest request : requests) {
			Map<String, Integer> map = request.getCoreHoursByMonth();
			if (map == null || map.isEmpty())
				continue;

			for (Map.Entry<String, Integer> e : map.entrySet()) {
				String dateStr = e.getKey();
				Integer hours = e.getValue();
				if (hours == null)
					hours = 0;

				LocalDate month = LocalDate.parse(dateStr, formatter).withDayOfMonth(1);
				int slot = (int) ChronoUnit.MONTHS.between(baseMonth, month) + 1;
				if (slot < 1 || slot > 14)
					continue;

				Date sqlDate = Date.valueOf(month);

				String sql = "UPDATE core.forecasthours " + "SET moddt = NOW(), forecasthours" + slot + " = ?, month"
						+ slot + " = ?, modby = ?, entryBY = ? " + "WHERE projectid = ?";

				jdbcTemplate.update(sql, hours, sqlDate, request.getEntryBy(), request.getEntryBy(),
						request.getProjectId());
			}
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
				+ "LEFT OUTER JOIN core.forecasthours fh ON p.projectid = fh.projectid "
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
			response.setProjectId(rs.getLong("projectid"));

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

	@Override
	public GeneralResponse updateCoreHours(List<CoreHoursRequest> requests) {
		GeneralResponse response = new GeneralResponse();
		if (requests == null || requests.isEmpty()) {
			response.Status = "error";
			response.Message = "No requests provided";
			return response;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate baseMonth = LocalDate.now().withDayOfMonth(1);

		for (CoreHoursRequest request : requests) {
			Map<String, Integer> map = request.getCoreHoursByMonth();
			if (map == null || map.isEmpty()) {
				continue;
			}

			for (Map.Entry<String, Integer> e : map.entrySet()) {
				String dateStr = e.getKey();
				Integer hours = e.getValue();
				if (hours == null) {
					hours = 0;
				}

				LocalDate month = LocalDate.parse(dateStr, formatter).withDayOfMonth(1);
				int slot = (int) ChronoUnit.MONTHS.between(baseMonth, month) + 1;
				if (slot < 1 || slot > 14) {
					continue;
				}

				Date sqlDate = Date.valueOf(month);

				String sql = "UPDATE core.corehours " + "SET moddt = NOW(), corehours" + slot + " = ?, month" + slot
						+ " = ?, modby = ? " + "WHERE corehoursid = ?";

				jdbcTemplate.update(sql, hours, sqlDate, request.getEntryBy(), request.getCoreHoursId());
			}
		}

		response.Status = "success";
		response.Message = "Core hours updated successfully";
		return response;
	}

	public void exportForecastingExcel(String codeValues, HttpServletResponse response, String projectIds)
			throws IOException {
		List<ForecastingResponse> userData = getList(codeValues).getData();
		List<ForecastingResponse> projectData = getProjectCoreHoursList(projectIds).getData();
		List<Long> userTotalHours = getUserTotalHours(codeValues);
		List<Long> projectTotalHours = getProjectTotalHours(projectIds);

		try (Workbook workbook = new XSSFWorkbook()) {
			// ========== Define styles ==========
			CellStyle boldStyle = workbook.createCellStyle();
			Font boldFont = workbook.createFont();
			boldFont.setBold(true);
			boldStyle.setFont(boldFont);

			CellStyle boldBlack = workbook.createCellStyle();
			Font boldBlackFont = workbook.createFont();
			boldBlackFont.setBold(true);
			boldBlackFont.setColor(IndexedColors.GREEN.getIndex());
			boldBlack.setFont(boldBlackFont);

			CellStyle boldBlack1 = workbook.createCellStyle();
			Font boldBlackFont1 = workbook.createFont();
			boldBlackFont1.setBold(true);
			boldBlack1.setFont(boldBlackFont1);

			CellStyle boldRed = workbook.createCellStyle();
			Font boldRedFont = workbook.createFont();
			boldRedFont.setBold(true);
			boldRedFont.setColor(IndexedColors.RED.getIndex());
			boldRed.setFont(boldRedFont);

			// ========== Date format ==========
			List<LocalDate> monthDates = new ArrayList<>();
			LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
			DateTimeFormatter monthLabelFormatter = DateTimeFormatter.ofPattern("MMM-yy");
			DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			for (int i = 0; i < 14; i++) {
				monthDates.add(currentMonth.plusMonths(i));
			}

			// ========== Sheet 1: User Forecasting ==========
			Sheet sheet1 = workbook.createSheet("user_forecasting");

			Row headerRow1 = sheet1.createRow(0);
			int col = 0;
			headerRow1.createCell(col++).setCellValue("User (hrs. per week)");
			for (LocalDate month : monthDates) {
				Cell cell = headerRow1.createCell(col++);
				cell.setCellValue(month.format(monthLabelFormatter));
				cell.setCellStyle(boldStyle);
			}

			int rowIdx1 = 1;
			for (ForecastingResponse record : userData) {
				Row row = sheet1.createRow(rowIdx1++);
				int c = 0;
				row.createCell(c++).setCellValue(record.getFname() + " " + record.getLname());

				Map<String, Integer> monthMap = new HashMap<>();
				for (Pair<LocalDate, Integer> entry : record.getCoreHoursByMonth()) {
					monthMap.put(entry.getFirst().format(keyFormatter), entry.getSecond());
				}

				for (LocalDate month : monthDates) {
					Integer hours = monthMap.getOrDefault(month.format(keyFormatter), 0);
					row.createCell(c++).setCellValue(hours);
				}
			}

			// Totals
			Row userTotalRow = sheet1.createRow(rowIdx1++);
			userTotalRow.createCell(0).setCellValue("User Core Hr. Totals");
			userTotalRow.getCell(0).setCellStyle(boldStyle);
			for (int i = 0; i < 14; i++) {
				Cell cell = userTotalRow.createCell(i + 1);
				cell.setCellValue(userTotalHours.get(i));
				cell.setCellStyle(boldStyle);
			}

			Row projectTotalRow = sheet1.createRow(rowIdx1++);
			projectTotalRow.createCell(0).setCellValue("User Project Hr. Totals");
			projectTotalRow.getCell(0).setCellStyle(boldStyle);
			for (int i = 0; i < 14; i++) {
				Cell cell = projectTotalRow.createCell(i + 1);
				cell.setCellValue(projectTotalHours.get(i));
				cell.setCellStyle(boldStyle);
			}

			Row coverageRow1 = sheet1.createRow(rowIdx1++);
			coverageRow1.createCell(0).setCellValue("Coverage Calculation");
			coverageRow1.getCell(0).setCellStyle(boldBlack1);

			for (int i = 0; i < 14; i++) {
				long diff = projectTotalHours.get(i) - userTotalHours.get(i);
				Cell cell = coverageRow1.createCell(i + 1);
				cell.setCellValue(diff);
				cell.setCellStyle(diff < 0 ? boldRed : boldBlack);
			}

			for (int i = 0; i < sheet1.getRow(0).getLastCellNum(); i++) {
				try {
					sheet1.autoSizeColumn(i);
				} catch (Exception e) {
					sheet1.setColumnWidth(i, 15 * 256);
					logger.warn("Auto-sizing failed for column {} in sheet1, using manual width", i);
				}
			}

			// ========== Sheet 2: Project Forecasting ==========
			Sheet sheet2 = workbook.createSheet("project_forecasting");

			Row headerRow2 = sheet2.createRow(0);
			int col2 = 0;
			headerRow2.createCell(col2++).setCellValue("Project (hrs. per week)");
			for (LocalDate month : monthDates) {
				Cell cell = headerRow2.createCell(col2++);
				cell.setCellValue(month.format(monthLabelFormatter));
				cell.setCellStyle(boldStyle);
			}

			int rowIdx2 = 1;
			for (ForecastingResponse record : projectData) {
				Row row = sheet2.createRow(rowIdx2++);
				int c = 0;
				row.createCell(c++).setCellValue(record.getProjectName());

				Map<String, Integer> monthMap = new HashMap<>();
				for (Pair<LocalDate, Integer> entry : record.getCoreHoursByMonth()) {
					monthMap.put(entry.getFirst().format(keyFormatter), entry.getSecond());
				}

				for (LocalDate month : monthDates) {
					String key = month.format(keyFormatter);
					Integer hours = monthMap.getOrDefault(key, 0);
					row.createCell(c++).setCellValue(hours);
				}
			}

			// Totals in reverse order
			Row projectTotalRow2 = sheet2.createRow(rowIdx2++);
			projectTotalRow2.createCell(0).setCellValue("Project Core Hr. Totals");
			projectTotalRow2.getCell(0).setCellStyle(boldStyle);
			for (int i = 0; i < 14; i++) {
				Cell cell = projectTotalRow2.createCell(i + 1);
				cell.setCellValue(projectTotalHours.get(i));
				cell.setCellStyle(boldStyle);
			}

			Row userTotalRow2 = sheet2.createRow(rowIdx2++);
			userTotalRow2.createCell(0).setCellValue("User Core Hr. Totals");
			userTotalRow2.getCell(0).setCellStyle(boldStyle);
			for (int i = 0; i < 14; i++) {
				Cell cell = userTotalRow2.createCell(i + 1);
				cell.setCellValue(userTotalHours.get(i));
				cell.setCellStyle(boldStyle);
			}

			Row coverageRow2 = sheet2.createRow(rowIdx2++);
			coverageRow2.createCell(0).setCellValue("Coverage Calculation");
			coverageRow2.getCell(0).setCellStyle(boldBlack1);
			for (int i = 0; i < 14; i++) {
				long diff = userTotalHours.get(i) - projectTotalHours.get(i); // reversed logic
				Cell cell = coverageRow2.createCell(i + 1);
				cell.setCellValue(diff);
				cell.setCellStyle(diff < 0 ? boldRed : boldBlack);
			}

			for (int i = 0; i < sheet2.getRow(0).getLastCellNum(); i++) {
				try {
					sheet2.autoSizeColumn(i);
				} catch (Exception e) {
					sheet2.setColumnWidth(i, 15 * 256);
					logger.warn("Auto-sizing failed for column {} in sheet2, using manual width", i);
				}
			}

			// ========== Export ==========
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=forecasting.xlsx");
			workbook.write(response.getOutputStream());
		}
	}

}
