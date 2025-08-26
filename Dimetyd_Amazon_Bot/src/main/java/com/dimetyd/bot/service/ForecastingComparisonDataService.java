package com.dimetyd.bot.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;

import jakarta.annotation.PostConstruct;

@Service
public class ForecastingComparisonDataService {
	  @Autowired
	    private JdbcTemplate jdbcTemplate;


    	@PostConstruct
        public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("ForecastingComparison")) {
        try {
            // Step 1: Get distinct vendor IDs
            String vendorQuery = "SELECT DISTINCT(vendorId) FROM ForecastOutput";
            List<String> vendorIds = jdbcTemplate.queryForList(vendorQuery, String.class);

            System.out.println(vendorQuery);

            for (String vendorId : vendorIds) {
                String cleanVendorId = cleanVendorId(vendorId);

                // Step 2: Get date ranges for the vendor ID
                String dateQuery = "SELECT DISTINCT(startDate), endDate FROM ForecastOutput WHERE vendorId = ?";
                System.out.println(dateQuery);

                List<Map<String, Object>> dateRanges = jdbcTemplate.queryForList(dateQuery, cleanVendorId);

                for (Map<String, Object> dateRange : dateRanges) {
                    String startDate = dateRange.get("startDate").toString();
                    String endDate = dateRange.get("endDate").toString();

                    // Step 3: Execute forecast query
                    executeForecastQuery(cleanVendorId, startDate, endDate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    	}

    private void executeForecastQuery(String vendorId, String startDate, String endDate) {
        try {
            String forecastQuery = "SELECT a.ASIN, a.week1Forcast, IFNULL(actualDT.actualOrder, 0), " +
                    "IFNULL((IFNULL(actualDT.actualOrder, 0) / IFNULL(a.week1Forcast, 0)) * 100, 0) AS FillRate, " +
                    "vendorId, startDate FROM ForecastOutput a " +
                    "LEFT JOIN (SELECT ASIN, IFNULL(SUM(quantityRequested), 0) AS actualOrder FROM (" +
                    "SELECT a.ASIN, a.quantityRequested, p.Order_Date FROM CBPOHistoryData a " +
                    "RIGHT JOIN CBPOData p ON (a.PO = p.PO AND a.vendorId = p.vendorId) " +
                    "WHERE p.Order_Date BETWEEN ? AND ? AND p.vendorId = ? " +
                    "UNION ALL SELECT c.ASIN, c.quantityRequested, p.Order_Date FROM ConfirmedPO c " +
                    "RIGHT JOIN CBPOData p ON (c.PO = p.PO AND c.vendorId = p.vendorId) " +
                    "WHERE p.Order_Date BETWEEN ? AND ? AND p.vendorId = ?) AS z GROUP BY ASIN) " +
                    "AS actualDT ON (actualDT.ASIN = a.ASIN) " +
                    "WHERE a.vendorId = ? AND a.startDate = ?";

            System.out.println(forecastQuery);

            List<Map<String, Object>> forecastResults = jdbcTemplate.queryForList(
                    forecastQuery,
                    adjustDate(startDate, 7),
                    adjustDate(endDate, 7),
                    vendorId,
                    adjustDate(startDate, 7),
                    adjustDate(endDate, 7),
                    vendorId,
                    vendorId,
                    startDate
            );

            for (Map<String, Object> result : forecastResults) {
                // Step 4: Insert data into `CBForecastCompariosum`
                insertForecastData(result, vendorId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertForecastData(Map<String, Object> forecastResult, String vendorId) {
        try {
            String insertQuery = "INSERT IGNORE INTO CBForecastCompariosum " +
                    "(`ASIN`, `forecast`, `ActualOrder`, `fillRate`, `vendorId`, `startDate`, `Ukey`) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CONCAT(?, ?, ?))";

            System.out.println(insertQuery);

            jdbcTemplate.update(
                    insertQuery,
                    forecastResult.get("ASIN"),
                    forecastResult.get("week1Forcast"),
                    forecastResult.get("actualOrder"),
                    forecastResult.get("FillRate"),
                    vendorId,
                    forecastResult.get("startDate"),
                    vendorId,
                    forecastResult.get("ASIN"),
                    forecastResult.get("startDate")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String cleanVendorId(String vendorId) {
        return vendorId.replace("$", "").replace(",", "").replace("%", "");
    }

    private String adjustDate(String date, int daysToAdd) {
        try {
            // Handle input with a possible time component
            if (date.contains("T")) {
                date = date.split("T")[0]; // Remove time part
            }
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return localDate.plusDays(daysToAdd).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to adjust date: " + date, e);
        }
    }
}
