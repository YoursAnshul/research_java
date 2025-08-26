package com.dimetyd.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.dimetyd.bot.helper.GlobalSession;
import jakarta.annotation.PostConstruct;

@Service
public class UpdateInvoiceShipmnetDisputeService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("UpdateInvoice_Shipmnet_claimDispute")) {
            logger.info("UpdateInvoiceShipmnetDispute Input bot started.");
            logger.info("Updating statuses...");
            updateStatuses();
        }

       
    }

    private void updateStatuses() {
        try {
            String todayDateCondition = "CURDATE()"; // Equivalent to '"+TodayDate+"'

            String[] queries = {
                "UPDATE CBitemizedshortage_input SET `status`='PENDING' WHERE DATE(createdDate) = " + todayDateCondition + " AND STATUS IN ('ERROR','INPROGRESS')",
                "UPDATE CBClientDispute a JOIN Vendor v ON (v.id=a.vendorId) SET requestStatus = 'PENDING' WHERE requestStatus = 'INPROGRESS' AND a.STATUS = 'ACTIVE' AND a.`type` IN('Price claim','Coop','open_shortage')",
                "UPDATE `ShippmentBotInput` SET DataStatus='Pending' WHERE DataStatus IN('INPROGRESS','ERROR')",
                "UPDATE POInvoiceInput SET STATUS='PENDING' WHERE STATUS IN('ERROR','InProgress')",
                "UPDATE CBPriceClaimRequest SET `status`='PENDING'",
                "UPDATE `CBMasterLoginCredentials` SET  `status`='PENDING' WHERE `status` IN('INPROGRESS','ERROR')",
                "UPDATE CBPriceClaimRequestDetails SET STATUS='PENDING' WHERE STATUS IN('Inprogress','ERROR')",
                "DELETE FROM input_POInvoiceInput",
                "INSERT IGNORE INTO input_POInvoiceInput(`vendorId`,`vendorName`,`startDt`,`endDt`,`status`)\r\n"
                + "\r\n"
                + "SELECT b.vendorId,`vendorName`,DATE_ADD(NOW(), INTERVAL -90 DAY)  startDate,NOW() endDate,'PENDING' AS 'STATUS'\r\n"
                + "FROM `Vendor` a JOIN `Client` b ON (a.id=b.vendorId AND b.`isInvoice`=1)\r\n"
                + " AND b.vendorId NOT IN('VN11111111111111','VN11111111111112')",
                "UPDATE input_POInvoiceInput SET STATUS='PENDING' WHERE STATUS IN('ERROR','InProgress') ",
                "UPDATE ShipmentBotQueue SET STATUS='PENDING' WHERE STATUS IN('ERROR','InProgress','COMPLETED') ",
                "UPDATE CBMasterLoginCredentials SET STATUS='PENDING' WHERE STATUS IN('ERROR','InProgress','COMPLETED')",
                "UPDATE CBitemizedshortageInvoicesToBeCreated SET `status`='PENDING' WHERE DATE(createdDate)="+todayDateCondition+" AND STATUS IN('INPROGRESS','ERROR')",
                "UPDATE `CBitemizedshortageInvoicesToBeCreated` SET STATUS='PENDING' WHERE  STATUS IN('PENDING','ERROR','INPROGRESS') AND DATE(`createdDate`)> '2025-02-01' ",
                "INSERT IGNORE INTO `CBitemizedshortage_input` (`vendorId`, `vendorName`, `status`, `createdDate`, uKey) " +
                        "SELECT c.vendorId, v.vendorName, 'PENDING' AS STATUS, NOW(), CONCAT(`vendorId`, DATE(NOW())) " +
                        "FROM `Client` c " +
                        "JOIN Vendor v ON (c.vendorId = v.id) " +
                        "WHERE `isShowFinopsShortage` = 1 AND v.`isPaused`='N' AND `autoDispute` = 1"
                
            };

            for (String query : queries) {
                int rowsUpdated = jdbcTemplate.update(query);
                logger.info("Executed query: {} | Rows updated: {}", query, rowsUpdated);
            }

        } catch (Exception e) {
            logger.error("Error executing status update queries", e);
        }
    }
}
