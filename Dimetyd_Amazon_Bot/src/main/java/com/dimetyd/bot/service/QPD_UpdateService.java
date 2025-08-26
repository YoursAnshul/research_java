package com.dimetyd.bot.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;

import jakarta.annotation.PostConstruct;

@Service
public class QPD_UpdateService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("QPD_FD_Updates")) {

            List<Map<String, Object>> triggerQueueData = getTriggerQueueData();

            for (Map<String, Object> row : triggerQueueData) {
                String vendorId = (String) row.get("vendorId");

                // Run QPD Update Queries
                executeQPDUpdates(vendorId);
            }

            System.out.println("All QPD updates completed. Exiting...");
            System.exit(0);
        }
    }

    private List<Map<String, Object>> getTriggerQueueData() {
        String query = "SELECT v.id AS vendorId, v.vendorName " +
                       "FROM Vendor v " +
                       "WHERE v.vcAccount IN ('upstartusa', 'dimetyd')";
        System.out.println("Trigger vendor query: " + query);
        return jdbcTemplate.queryForList(query);
    }

    private void executeQPDUpdates(String vendorId) {
        String update1 = "UPDATE FDSummary SET quickPayDiscount = " +
                "(SELECT ABS(SUM(termsDiscountTaken)) FROM CBFinancialDashboardData_2025 " +
                "WHERE paymentDate >= '2025-01-01' AND paymentDate <= '2025-12-31' " +
                "AND invoiceTypeDetailed IN ('Sales Invoice Payment', 'Dropship Sales Payment') " +
                "AND vendorId = '" + vendorId + "') " +
                "WHERE YEAR = 2025 AND vendorId = '" + vendorId + "'";

        String update2 = "UPDATE FDSummary SET quickPayDiscountTotal = " +
                "(SELECT SUM(amountPaid) FROM CBFinancialDashboardData_2025 " +
                "WHERE termsDiscountTaken != 0 AND paymentDate >= '2025-01-01' AND paymentDate <= '2025-12-31' " +
                "AND invoiceTypeDetailed IN ('Sales Invoice Payment', 'Dropship Sales Payment') " +
                "AND vendorId = '" + vendorId + "') " +
                "WHERE YEAR = 2025 AND vendorId = '" + vendorId + "'";

        try {
            jdbcTemplate.update(update1);
            System.out.println(" Update1 (quickPayDiscount) executed for vendorId: " + vendorId);

            jdbcTemplate.update(update2);
            System.out.println(" Update2 (quickPayDiscountTotal) executed for vendorId: " + vendorId);

        } catch (Exception e) {
            System.err.println(" Error executing QPD updates for vendorId: " + vendorId);
            e.printStackTrace();
        }
    }
}
