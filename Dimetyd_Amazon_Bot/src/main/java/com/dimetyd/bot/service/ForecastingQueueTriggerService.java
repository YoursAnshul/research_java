package com.dimetyd.bot.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;

import jakarta.annotation.PostConstruct;

@Service
public class ForecastingQueueTriggerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("ForecastingQueueTrigger")) {

            List<Map<String, Object>> triggerQueueData = getTriggerQueueData();
            
            
            

            for (Map<String, Object> row : triggerQueueData) {
            	
         System.out.println(row.get("vendorId"));
         System.out.println(row.get("startDate"));
            	
                String vendorId = (String) row.get("vendorId");
                
                Date start = (Date) row.get("startDate"); // or java.sql.Date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String startDate = sdf.format(start);
                System.out.println(startDate);
                
                Date end = (Date) row.get("endDate"); // or java.sql.Date
                String endDate = sdf.format(end);
                System.out.println(endDate);
                

                List<Map<String, Object>> forecastDT = getForecastData(vendorId, startDate);
                List<Map<String, Object>> orderqtyDT = getOrderQuantityData(vendorId, startDate, endDate);

                List<Map<String, Object>> matchedData = filterMatchedData(forecastDT, orderqtyDT);

                for (Map<String, Object> data : matchedData) {
                    try {
                        insertIntoForecastOutput(data);
                    } catch (Exception e) {
                        System.err.println("Error inserting data: " + data);
                        e.printStackTrace();
                    }
                }
            }

            // Exit after all processing
            System.out.println("All transactions completed. Exiting...");
            System.exit(0);
        }
    }

    

    private List<Map<String, Object>> getTriggerQueueData() {
        String query = "SELECT v.id AS vendorId, v.vendorName, w.startDate, w.endDate, 'Weekly' AS reportingPeriod " +
                       "FROM Vendor v " +
                       "JOIN Client c ON (c.vendorId = v.id) " +
                       "JOIN WeekRange w " +
                       "WHERE c.isForecasting = 1 " +
                       "AND w.startDate > DATE(NOW() - INTERVAL 7 DAY) " +
                       "AND w.endDate > DATE(NOW()) " +
                       "AND v.vcAccount IN ('upstartusa', 'dimetyd')  AND v.id IN('VN01032024072900','VN01032024082900','VN02022024022400','VN06032024022600','VN07022024022400','VN07022024022500','VN07022024022600','VN08022024022600','VN09022024022600','VN11012024022400','VN14022024023700','VN14022024023800','VN17012023022400','VN19012024032400','VN19012024042400','VN20022024022600','VN20022024023800','VN20220223051340','VN20220901060111','VN20230822121829','VN20240124021119','VN20240131062438','VN20240131122524','VN20240213115854','VN20240308022618','VN20240308022619','VN20240312120248','VN20240312120249','VN20240314120253','VN20240314120832','VN20240315060737','VN20240327030534','VN20240328044816','VN20240329060252','VN20240329062328','VN20240403041909','VN20240404040945','VN20240404040946','VN20240417042613','VN20240418102303','VN20240430063311','VN20240430063312','VN20240430063313','VN20240430063314','VN20240430063315','VN20240430063317','VN20240430063318','VN20240430063319','VN20240503074257','VN20240508065816','VN20240508065817','VN20240508065818','VN20240508065820','VN20240508065821','VN20240508072819','VN20240517120709','VN20240520070216','VN20240520070217','VN20240521054759','VN20240521054800','VN20240522061409','VN20240522061411','VN20240522061412','VN20240524090221','VN20240531032639','VN20240604061704','VN20240605050108','VN20240605050109','VN20240611033132','VN20240614032928','VN20240617100200','VN20240621082837','VN20240621082838','VN20240621084536','VN20240702040616','VN20240708083848','VN20240709092407','VN20240710042025','VN20240710042026','VN20240710042027','VN20240710042028','VN20240710042029','VN20240710042031','VN20240710042032','VN20240710042033','VN20240710093516','VN20240712092459','VN20240712100423','VN20240819100425','VN20240821020425','VN20250415080149','VN20252304158305','VN21022024022700','VN29012024112600','VN29012024112601','VN29012024112602','VN29012024112603')" +
                       "GROUP BY v.id";
        System.out.println("Trigger queue query: " + query);
        return jdbcTemplate.queryForList(query);
    }

    private List<Map<String, Object>> getForecastData(String vendorId, String startDate) {
        String query = "SELECT * FROM AsinForecast_import WHERE vendorId = '" + vendorId + "' AND startDate = '" + startDate + "'";
        System.out.println("Forecast query: " + query);
        return jdbcTemplate.queryForList(query);
    }

    private List<Map<String, Object>> getOrderQuantityData(String vendorId, String startDate, String endDate) {
        String query = "SELECT ASIN, IFNULL(SUM(quantityRequested), 0) AS qty FROM " +
                       "(SELECT a.ASIN, a.quantityRequested, p.Order_Date " +
                       " FROM CBPOHistoryData a " +
                       " RIGHT JOIN CBPOData p ON (a.PO = p.PO AND a.vendorId = p.vendorId) " +
                       " WHERE p.Order_Date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
                       " AND p.vendorId = '" + vendorId + "' " +
                       " UNION ALL " +
                       " SELECT c.ASIN, c.quantityRequested, p.Order_Date " +
                       " FROM ConfirmedPO c " +
                       " RIGHT JOIN CBPOData p ON (c.PO = p.PO AND c.vendorId = p.vendorId) " +
                       " WHERE p.Order_Date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
                       " AND p.vendorId = '" + vendorId + "') AS z " +
                       "GROUP BY ASIN";
        System.out.println("Order quantity query: " + query);
        return jdbcTemplate.queryForList(query);
    }

    private List<Map<String, Object>> filterMatchedData(List<Map<String, Object>> forecastDT, List<Map<String, Object>> orderqtyDT) {
        return forecastDT.stream()
            .filter(forecast -> orderqtyDT.stream()
                .anyMatch(order -> forecast.get("ASIN").equals(order.get("ASIN"))))
            .map(forecast -> {
                forecast.put("actualOrderedQty", orderqtyDT.stream()
                    .filter(order -> forecast.get("ASIN").equals(order.get("ASIN")))
                    .findFirst()
                    .map(order -> order.get("qty"))
                    .orElse(0));
                return forecast;
            }).collect(Collectors.toList());
    }

    private void insertIntoForecastOutput(Map<String, Object> data) {
        String vendorId = (String) data.get("vendorId");
        String ASIN = (String) data.get("ASIN");
        String startDate = formatDate((String) data.get("startDate"));
        String endDate = formatDate((String) data.get("endDate"));

        String query = "INSERT IGNORE INTO `ForecastOutput` " +
            "(`vendorId`,`startDate`,`endDate`,`ASIN`,`title`,`week1Forcast`,`week2Forcast`,`week3Forcast`,`week4Forcast`,`week5Forcast`,`week6Forcast`," +
            "`week7Forcast`,`week8Forcast`,`week9Forcast`,`week10Forcast`,`week11Forcast`,`week12Forcast`,`week13Forcast`,`week14Forcast`,`week15Forcast`," +
            "`week16Forcast`,`week17Forcast`,`week18Forcast`,`week19Forcast`,`week20Forcast`,`week21Forcast`,`week22Forcast`,`week23Forcast`,`week24Forcast`," +
            "`week25Forcast`,`week26Forcast`,`createdDate`,`UKey`) " +
            "SELECT vendorId,startDate,endDate,ASIN,productTitle,week_0, week_1,week_2,week_3,week_4,week_5,week_6,week_7,week_8,week_9,week_10,week_11," +
            "week_12,week_13,week_14,week_15,week_16,week_17,week_18,week_19,week_20,week_21,week_22,week_23,week_24,week_25, NOW(), " +
            "CONCAT(vendorId,ASIN,startDate) " +
            "FROM (SELECT vendorId,startDate,endDate,ASIN,productTitle,`statView`, " +
            "MIN(ABS(`week_0`-0)) AS diff0, week_0, MIN(ABS(`week_1`-0)) AS diff1 , week_1, " +
            "MIN(ABS(`week_2`-0)) AS diff2, week_2, MIN(ABS(`week_3`-0)) AS diff3, week_3, " +
            "MIN(ABS(`week_4`-0)) AS diff4, week_4, MIN(ABS(`week_5`-0)) AS diff5, week_5, " +
            "MIN(ABS(`week_6`-0)) AS diff6, week_6, MIN(ABS(`week_7`-0)) AS diff7, week_7, " +
            "MIN(ABS(`week_8`-0)) AS diff8, week_8, MIN(ABS(`week_9`-0)) AS diff9, week_9, " +
            "MIN(ABS(`week_10`-0)) AS diff10, week_10, MIN(ABS(`week_11`-0)) AS diff11, week_11, " +
            "MIN(ABS(`week_12`-0)) AS diff12, week_12, MIN(ABS(`week_13`-0)) AS diff13, week_13, " +
            "MIN(ABS(`week_14`-0)) AS diff14, week_14, MIN(ABS(`week_15`-0)) AS diff15, week_15, " +
            "MIN(ABS(`week_16`-0)) AS diff16, week_16, MIN(ABS(`week_17`-0)) AS diff17, week_17, " +
            "MIN(ABS(`week_18`-0)) AS diff18, week_18, MIN(ABS(`week_19`-0)) AS diff19, week_19, " +
            "MIN(ABS(`week_20`-0)) AS diff20, week_20, MIN(ABS(`week_21`-0)) AS diff21, week_21, " +
            "MIN(ABS(`week_22`-0)) AS diff22, week_22, MIN(ABS(`week_23`-0)) AS diff23, week_23, " +
            "MIN(ABS(`week_24`-0)) AS diff24, week_24, MIN(ABS(`week_25`-0)) AS diff25, week_25 " +
            "FROM `AsinForecast_import` " +
            "WHERE vendorId = '" + vendorId + "' " +
            "AND ASIN = '" + ASIN + "' " +
            "AND DATE(`startDate`) = '" + startDate + "' " +
            "AND DATE(`endDate`) = '" + endDate + "') AS z";

       logger.info(query);
        jdbcTemplate.update(query);
        System.out.println("Insert forecast output query: " + query);
    }

    private String formatDate(String inputDate) {
        try {
            if (inputDate == null || inputDate.trim().isEmpty()) {
                throw new IllegalArgumentException("Input date is null or empty");
            }
            inputDate = inputDate.trim();  // important
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            inputFormat.setLenient(false); // strict parsing
            Date date = inputFormat.parse(inputDate);
            return inputFormat.format(date); // same format in/out
        } catch (Exception e) {
            System.err.println("Failed to format date: " + inputDate);
            e.printStackTrace();
            throw new RuntimeException("Invalid date format: " + inputDate, e); // force bean failure with clear reason
        }
    }

}
