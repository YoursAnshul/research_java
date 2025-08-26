package com.dimetyd.bot.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;

import jakarta.annotation.PostConstruct;

@Service
public class UpdateDisputesTrigger {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("UpdateDisputesTrigger")) {
            logger.info("UpdateInvoiceShipmnetDispute Input bot started.");
            logger.info("Updating statuses...");
            updatePriorityDisputes();
        }

       
    }

    private void updatePriorityDisputes() {
        try {
            // 1. Select disputeIds matching your conditions
            String selectQuery = 
                "SELECT a.disputeId FROM CBClientDispute a "
                + "LEFT JOIN Vendor b ON (a.vendorId = b.id) "
                + "LEFT JOIN CBDispute c ON (a.disputeId = c.disputeId) "
                + "LEFT JOIN VendorCredentials d ON (a.vendorId = d.vendorId) "
                + "WHERE (DATE(a.modifiedDate) != CURDATE() OR a.modifiedDate IS NULL) "
                + "AND a.requestStatus NOT IN ('COMPLETED','RESOLVED','PARTIALYRESOLVED') "
                + "AND a.type = 'coop' "
                + "AND a.status = 'ACTIVE' "
                + "AND c.disputeStatus != 'RESOLVED'";

            // Execute select query and get disputeIds
            List<String> disputeIds = jdbcTemplate.query(selectQuery, (rs, rowNum) -> rs.getString("disputeId"));

            if (disputeIds.isEmpty()) {
                logger.info("No disputes found for updating requestStatus.");
                return;
            }

            // 2. Prepare IN clause for update
            String inClause = disputeIds.stream()
                                       .map(id -> "'" + id + "'")
                                       .collect(Collectors.joining(","));

            // 3. Update disputes with those disputeIds
            String updateQuery = "UPDATE CBClientDispute "
                               + "SET requestStatus = 'PENDING' "
                               + "WHERE requestStatus IN ('ERROR','INPROGRESS') "
                               + "AND status = 'ACTIVE' "
                               + "AND disputeId IN (" + inClause + ")";

            int updatedRows = jdbcTemplate.update(updateQuery);
            logger.info("Updated {} disputes' requestStatus to PENDING", updatedRows);

        } catch (Exception e) {
            logger.error("Error updating priority disputes", e);
        }
    }
}
