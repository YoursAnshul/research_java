package com.dimetyd.bot.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;

import jakarta.annotation.PostConstruct;

@Service
public class ASINProfitabilityService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("ASINProfitabilityService")) {
           /* String query = "SELECT c.vendorId, v.vendorName, 'Monthly' AS reportingPeriod, " +
                    "YEAR(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)) AS year, " +
                    "MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)) AS month " +
                    "FROM Vendor v JOIN Client c ON v.Id = c.vendorId WHERE c.isASINProfitability = 1 " +
                    "UNION " +
                    "SELECT c.vendorId, v.vendorName, 'Yearly' AS reportingPeriod, " +
                    "YEAR(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)) AS year, '' AS month " +
                    "FROM Vendor v JOIN Client c ON v.Id = c.vendorId WHERE c.isASINProfitability = 1";
            */
            String query = "SELECT c.vendorId, v.vendorName, 'Monthly' AS reportingPeriod, YEAR(CURRENT_DATE) AS YEAR, 6 AS MONTH FROM Vendor v JOIN `Client` c ON v.Id = c.vendorId WHERE c.isASINProfitability = 1 UNION SELECT c.vendorId, v.vendorName, 'Yearly' AS reportingPeriod, YEAR(CURRENT_DATE) AS YEAR, '' AS MONTH FROM Vendor v JOIN `Client` c ON v.Id = c.vendorId WHERE c.isASINProfitability = 1;\r\n"
            		+ "";
            

            List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
            System.out.print("input query is: " + query);

            for (Map<String, Object> row : results) {
                String vendorId = (String) row.get("vendorId");
                String reportingPeriod = (String) row.get("reportingPeriod");
                int year = (int) row.get("year");
                String month = row.get("month") != null ? row.get("month").toString() : null;

                switch (reportingPeriod) {
                    case "Monthly" -> processMonthly(vendorId, year, month);
                    case "Yearly" -> processYearly(vendorId, year);
                }
            }
        }
    }

    private void processMonthly(String vendorId, int year, String month) {
        String shippedQuery = "SELECT CASE WHEN isCase = 1 THEN SUM(caseQty * quantityReceived * unitCost) " +
                "ELSE SUM(quantityReceived * unitCost) END AS shippedAmount " +
                "FROM CBPOHistoryData a JOIN CBPOData b ON a.PO = b.PO " +
                "WHERE a.vendorId = '" + vendorId + "' AND YEAR(b.Order_Date) = '" + year + "' AND MONTH(b.Order_Date) = '" + month + "'";

        Double shippedAmount = jdbcTemplate.queryForObject(shippedQuery, Double.class);
        System.out.println("Monthly shipped amount query: " + shippedQuery);

        if (shippedAmount == null || shippedAmount == 0.0) {
            System.out.println("Skipping Monthly for vendor " + vendorId + " due to zero shippedAmount.");
            return;
        }

        String deleteQuery = "DELETE FROM CBASINProfitability WHERE vendorId = '" + vendorId + "' AND month = '" + month + "' AND year = '" + year + "' AND reportingPeriod = 'Monthly'";
        jdbcTemplate.update(deleteQuery);
        System.out.println("Delete query is: " + deleteQuery);

        String insertQuery = "INSERT INTO CBASINProfitability (`vendorId`, `asin`, `inNetworkReceipt`, `dfReceipt`, `totalReceipt`, `percentageContribution`, `reportingPeriod`, `month`, `year`) " +
                "SELECT a.vendorId, a.ASIN, " +
                "CASE WHEN isCase = 1 THEN SUM(caseQty * quantityReceived * unitCost) ELSE SUM(quantityReceived * unitCost) END AS inNetworkReceipt, " +
                "IFNULL(z.Dfshipped, 0) AS dfReceipt, " +
                "CASE WHEN isCase = 1 THEN SUM(caseQty * quantityReceived * unitCost) ELSE SUM(quantityReceived * unitCost) END + IFNULL(z.Dfshipped, 0) AS totalReceipt, " +
                "((CASE WHEN isCase = 1 THEN SUM(caseQty * quantityReceived * unitCost) ELSE SUM(quantityReceived * unitCost) END / " + shippedAmount + ") * 100) AS percentageContribution, " +
                "'Monthly' AS reportingPeriod, '" + month + "' AS `month`, '" + year + "' AS `year` " +
                "FROM CBPOHistoryData a " +
                "JOIN CBPOData b ON (a.PO = b.PO AND a.vendorId = b.vendorId) " +
                "LEFT JOIN (SELECT ASIN, SUM(quantity * itemCost) AS Dfshipped FROM DirectFullFillmntInvoices " +
                "WHERE vendorId = '" + vendorId + "' AND YEAR(invoiceDate) = " + year + " AND MONTH(invoiceDate) = '" + month + "' GROUP BY ASIN) AS z " +
                "ON z.ASIN = a.ASIN WHERE a.vendorId = '" + vendorId + "' AND YEAR(b.Order_Date) = '" + year + "' AND MONTH(b.Order_Date) = '" + month + "' GROUP BY a.ASIN";

        jdbcTemplate.update(insertQuery);
        System.out.println("Insert query is: " + insertQuery);

        updateDeductions(vendorId, year, month, "Monthly");
        updateNetProfit(vendorId, year, month, "Monthly");
    }

    private void processYearly(String vendorId, int year) {
        String shippedQuery = "SELECT CASE WHEN isCase = 1 THEN SUM(caseQty * quantityReceived * unitCost) " +
                "ELSE SUM(quantityReceived * unitCost) END AS shippedAmount " +
                "FROM CBPOHistoryData a JOIN CBPOData b ON a.PO = b.PO " +
                "WHERE a.vendorId = '" + vendorId + "' AND YEAR(b.Order_Date) = '" + year + "'";

        Double shippedAmount = jdbcTemplate.queryForObject(shippedQuery, Double.class);
        System.out.println("Yearly shipped amount query: " + shippedQuery);

        if (shippedAmount == null || shippedAmount == 0.0) {
            System.out.println("Skipping Yearly for vendor " + vendorId + " due to zero shippedAmount.");
            return;
        }

        String deleteQuery = "DELETE FROM CBASINProfitability WHERE vendorId = '" + vendorId + "' AND year = '" + year + "' AND reportingPeriod = 'Yearly'";
        jdbcTemplate.update(deleteQuery);
        System.out.println("Delete yearly query: " + deleteQuery);

        String insertQuery = "INSERT INTO CBASINProfitability (`vendorId`, `asin`, `inNetworkReceipt`, `dfReceipt`, `totalReceipt`, `percentageContribution`, `reportingPeriod`, `month`, `year`) " +
                "SELECT a.vendorId, a.ASIN, " +
                "CASE WHEN isCase = 1 THEN SUM(caseQty * quantityReceived * unitCost) ELSE SUM(quantityReceived * unitCost) END AS inNetworkReceipt, " +
                "IFNULL(z.Dfshipped, 0) AS dfReceipt, " +
                "CASE WHEN isCase = 1 THEN SUM(caseQty * quantityReceived * unitCost) ELSE SUM(quantityReceived * unitCost) END + IFNULL(z.Dfshipped, 0) AS totalReceipt, " +
                "((CASE WHEN isCase = 1 THEN SUM(caseQty * quantityReceived * unitCost) ELSE SUM(quantityReceived * unitCost) END / " + shippedAmount + ") * 100) AS percentageContribution, " +
                "'Yearly' AS reportingPeriod, '' AS month, '" + year + "' AS year " +
                "FROM CBPOHistoryData a " +
                "JOIN CBPOData b ON a.PO = b.PO AND a.vendorId = b.vendorId " +
                "LEFT JOIN (SELECT ASIN, SUM(quantity * itemCost) AS Dfshipped FROM DirectFullFillmntInvoices " +
                "WHERE vendorId = '" + vendorId + "' AND YEAR(invoiceDate) = '" + year + "' GROUP BY ASIN) AS z " +
                "ON z.ASIN = a.ASIN WHERE a.vendorId = '" + vendorId + "' AND YEAR(b.Order_Date) = '" + year + "' GROUP BY a.ASIN";

        jdbcTemplate.update(insertQuery);
        System.out.println("Insert yearly query is: " + insertQuery);

        updateDeductions(vendorId, year, "", "Yearly");
        updateNetProfit(vendorId, year, "", "Yearly");
    }

    private void updateDeductions(String vendorId, int year, String month, String reportingPeriod) {
        String[] deductionTypes = {
            "AMS", "C2FO Credit Memo", "CoOp deduction", "Post Audit Deduction", "Price Claim Deduction",
            "Return Freight Deduction", "Shortage Deduction", "Provision Deduction",
            "Returns Deduction", "Operational Chargeback Deduction"
        };

        for (String deduction : deductionTypes) {
            String amountPaidQuery = "SELECT IFNULL(SUM(amountPaid), 0) AS amountPaid FROM CBFinancialDashboardData " +
                    "WHERE vendorId = '" + vendorId + "' AND YEAR(paymentDate) = '" + year + "' " +
                    (month != null && !month.isEmpty() ? "AND MONTH(paymentDate) = '" + month + "' " : "") +
                    "AND invoiceType IN ('" + deduction + "')";

            Double amountPaid = jdbcTemplate.queryForObject(amountPaidQuery, Double.class);
            System.out.println("Amount paid query: " + amountPaidQuery);

            String updateDeductionQuery = String.format("UPDATE CBASINProfitability SET %s = (percentageContribution * %s) / 100 " +
                    "WHERE vendorId = '%s' AND year = '%d' %s AND reportingPeriod = '%s'",
                    mapDeductionField(deduction), amountPaid, vendorId, year,
                    (month != null && !month.isEmpty() ? "AND month = '" + month + "' " : ""),
                    reportingPeriod);

            jdbcTemplate.update(updateDeductionQuery);
            System.out.println("Update deduction query: " + updateDeductionQuery);
        }
    }

    private String mapDeductionField(String deduction) {
        return switch (deduction) {
            case "AMS" -> "ams";
            case "C2FO Credit Memo" -> "c2foCreditMemo";
            case "CoOp deduction" -> "netCoopDeduction";
            case "Post Audit Deduction" -> "netPostAuditDeduction";
            case "Price Claim Deduction" -> "netPriceClaim";
            case "Return Freight Deduction" -> "netReturnFreightAndHandlingCharges";
            case "Shortage Deduction" -> "netShortages";
            case "Provision Deduction" -> "netProvisionDeduction";
            case "Returns Deduction" -> "netReturnsDeduction";
            case "Operational Chargeback Deduction" -> "netOperationalChargebacks";
            default -> throw new IllegalArgumentException("Unknown deduction type: " + deduction);
        };
    }

    private void updateNetProfit(String vendorId, int year, String month, String reportingPeriod) {
        String whereClause = "WHERE vendorId = '" + vendorId + "' AND year = '" + year + "' " +
                (month != null && !month.isEmpty() ? "AND month = '" + month + "' " : "") +
                "AND reportingPeriod = '" + reportingPeriod + "'";

        String netProfitQuery = "UPDATE CBASINProfitability SET netProfit = totalReceipt - ABS(" +
                "IFNULL(ams, 0) + IFNULL(c2foCreditMemo, 0) + IFNULL(netCoopDeduction, 0) + " +
                "IFNULL(netPostAuditDeduction, 0) + IFNULL(netPriceClaim, 0) + " +
                "IFNULL(netReturnFreightAndHandlingCharges, 0) + IFNULL(netShortages, 0) + " +
                "IFNULL(netProvisionDeduction, 0) + IFNULL(netReturnsDeduction, 0) + IFNULL(netOperationalChargebacks, 0))," +
                "netProfitPercentage = CASE WHEN totalReceipt = 0 THEN 0 ELSE ((totalReceipt - ABS(" +
                "IFNULL(ams, 0) + IFNULL(c2foCreditMemo, 0) + IFNULL(netCoopDeduction, 0) + " +
                "IFNULL(netPostAuditDeduction, 0) + IFNULL(netPriceClaim, 0) + " +
                "IFNULL(netReturnFreightAndHandlingCharges, 0) + IFNULL(netShortages, 0) + " +
                "IFNULL(netProvisionDeduction, 0) + IFNULL(netReturnsDeduction, 0) + IFNULL(netOperationalChargebacks, 0))) / totalReceipt) * 100 END " +
                whereClause;

        jdbcTemplate.update(netProfitQuery);
        System.out.println("Net profit query: " + netProfitQuery);
    }
}
