package com.dimetyd.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.dimetyd.bot.helper.GlobalSession;

import jakarta.annotation.PostConstruct;

@Service
public class ShortageCreationInputService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("ShortageCreationInput")) {
            logger.info("ShortageCreation Input bot started.");
            logger.info("Starting Itemized Shortage Creation Report...");
            executeShortageInsertion();
        }

        
    }

    private void executeShortageInsertion() {
        try {
            String insertQuery = "INSERT INTO `CBitemizedshortage_input` (`vendorId`, `vendorName`, `status`, `createdDate`, uKey) " +
                                 "SELECT c.vendorId, v.vendorName, 'PENDING' AS STATUS, NOW(), CONCAT(`vendorId`, DATE(NOW())) " +
                                 "FROM `Client` c " +
                                 "JOIN Vendor v ON (c.vendorId = v.id) " +
                                 "WHERE `isShowFinopsShortage` = 1 AND `autoDispute` = 1";

            int rowsInserted = jdbcTemplate.update(insertQuery);
            logger.info("Shortage creation input insertion completed. Rows inserted: {}", rowsInserted);
        } catch (Exception e) {
            logger.error("Error executing shortage input insertion", e);
        }
    }
}
