package com.dimetyd.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.dimetyd.bot.helper.GlobalSession;
import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class Forecasting_OperationalInsertInputService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("UpdateForecastData_OperationalChargebackData")) {
            logger.info("Forecasting Input bot started.");
            logger.info("Updating statuses...");
            updateStatuses();
        }
    }

    private void updateStatuses() {
        try {
            String todayDateCondition = "CURDATE()";

            // Existing queries
            String[] existingQueries = {
                "DELETE FROM CBForecastRequest",
                "INSERT IGNORE INTO CBForecastRequest (`vendorId`,`vendorName`,`dataStatus`,`createdDate`) " +
                "SELECT c.`vendorId`,a.`vendorName`,'PENDING' AS dataStatus ,NOW() " +
                "FROM Vendor a JOIN `Client` c ON (c.vendorId=a.id) " +
                "JOIN `VendorCredentials` d ON(d.`amazonVCName`=a.vendorName) " +
                "WHERE c.`isShowAsinForcasting` = 1 AND c.vendorId != 'VN11111111111111' AND a.`isPaused`='N' AND  `isFinopsActive` = 1",
                
                "INSERT IGNORE INTO CBForecastRequest (`vendorId`,`vendorName`,`dataStatus`,`createdDate`) " +
                "SELECT c.`vendorId`,a.`vendorName`,'PENDING' AS dataStatus ,NOW() " +
                "FROM Vendor a JOIN `Client` c ON (c.vendorId=a.id) " +
                "JOIN `VendorCredentials` d ON(d.`amazonVCName`=a.vendorName) " +
                "WHERE c.`isShowAsinForcasting` = 1 AND c.vendorId != 'VN11111111111111' " +
                "AND a.`isPaused`='N'  AND c.`trailPeriodStartDate` IS NOT NULL AND c.`trailPeriodEndDate` IS NULL"
            };

            // Check if today is Sunday
            boolean isSunday = LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY;

            // Queries based on Sunday condition
            String[] newQueries;
            if (isSunday) {
                newQueries = new String[]{
                    "INSERT IGNORE INTO `CBRequestOperationalChargeBack` (`vendorId`,`vendorName`,`startDate`,`endDate`,`createdDate`,`status`) " +
                    "SELECT a.id AS vendorId, a.vendorName AS VendorName, z.startDate  AS inStartDate,z.enddate AS  inEndDate, NOW() , 'PENDING' AS  STATUS " +
                    "FROM Vendor a JOIN `Client` c ON (c.vendorId=a.id)  JOIN (SELECT `startDate`,`endDate` FROM `MonthRange` " +
                    "WHERE startDate > DATE_SUB(NOW() ,INTERVAL 364 DAY ) AND `endDate` < NOW() AND `reportingPeriod` = 'Monthly' " +
                    "UNION SELECT startDate, DATE(DATE) AS endDate  FROM (SELECT `startDate`,`endDate`, DATE(NOW()-1)AS DATE ,DATE(NOW()-1) AS todaysDate " +
                    "FROM `MonthRange` WHERE `reportingPeriod` = 'Monthly') AS z WHERE todaysDate  BETWEEN startDate AND endDate) AS z " +
                    "WHERE c.`isShowOperationalChargeback` = 1 AND `isFinopsActive` = 1  AND a.`isVendorMenuAccess` = 1 " +
                    "AND vendorId NOT IN ('VN11111111111111','VN11111111111112') AND a.`isPaused`='N' AND  a.`ocDataScrapingCycle` = 'Monthly'",

                    "INSERT IGNORE INTO `CBRequestOperationalChargeBack` (`vendorId`,`vendorName`,`startDate`,`endDate`,`createdDate`,`status`) " +
                    "SELECT a.id AS vendorId, a.vendorName AS VendorName,CURDATE() - INTERVAL 364 DAY AS " +
                    "inStartDate, CURDATE() AS inEndDate, NOW() , 'PENDING' AS STATUS " +
                    "FROM Vendor a JOIN `Client` c ON (c.vendorId=a.id) " +
                    "WHERE c.`isShowOperationalChargeback` = 1  AND a.`isVendorMenuAccess` = 1 " +
                    "AND a.`ocDataScrapingCycle` = 'Yearly'AND a.`isPaused`='N' AND vendorId  NOT IN ('VN11111111111111','VN11111111111112')"
                };
            } else {
                newQueries = new String[]{
                    "INSERT IGNORE INTO `CBRequestOperationalChargeBack` (`vendorId`,`vendorName`,`startDate`,`endDate`,`createdDate`,`status`) " +
                    "SELECT a.id AS vendorId, a.vendorName AS VendorName, z.startDate  AS inStartDate,z.enddate AS  inEndDate, NOW() , 'PENDING' AS  STATUS " +
                    "FROM Vendor a JOIN `Client` c ON (c.vendorId=a.id)  JOIN (SELECT `startDate`,`endDate` FROM `MonthRange` " +
                    "WHERE startDate > DATE_SUB(NOW() ,INTERVAL 365 DAY ) AND `endDate` < NOW() AND `reportingPeriod` = 'Monthly' " +
                    "UNION SELECT startDate, DATE(DATE) AS endDate  FROM (SELECT `startDate`,`endDate`, DATE(NOW()-1)AS DATE ,DATE(NOW()-1) AS todaysDate " +
                    "FROM `MonthRange` WHERE `reportingPeriod` = 'Monthly') AS z WHERE todaysDate  BETWEEN startDate AND endDate) AS z " +
                    "WHERE c.`isShowOperationalChargeback` = 1 AND `isFinopsActive` = 1 AND a.`isPaused`='N' AND vendorId NOT IN('VN11111111111111','VN11111111111112') AND a.`ocDataScrapingCycle` = 'Monthly'",

                    "INSERT IGNORE INTO `CBRequestOperationalChargeBack` (`vendorId`,`vendorName`,`startDate`,`endDate`,`createdDate`,`status`) " +
                    "SELECT a.id AS vendorId, a.vendorName AS VendorName,CURDATE() - INTERVAL 364 DAY AS " +
                    "inStartDate, CURDATE() AS inEndDate, NOW() , 'PENDING' AS STATUS " +
                    "FROM Vendor a JOIN `Client` c ON (c.vendorId=a.id) " +
                    "WHERE c.`isShowOperationalChargeback` = 1 AND `isFinopsActive` = 1 AND a.`ocDataScrapingCycle` = 'Yearly' " +
                    "AND a.`isPaused`='N' AND  vendorId NOT IN ('VN11111111111111','VN11111111111112')"
                };
            }

            // Execute existing queries
            for (String query : existingQueries) {
                int rowsUpdated = jdbcTemplate.update(query);
                logger.info("Executed query: {} | Rows updated: {}", query, rowsUpdated);
            }

            // Execute new queries
            for (String query : newQueries) {
                int rowsUpdated = jdbcTemplate.update(query);
                logger.info("Executed query: {} | Rows updated: {}", query, rowsUpdated);
            }

        } catch (Exception e) {
            logger.error("Error executing status update queries", e);
        }
    }
}
