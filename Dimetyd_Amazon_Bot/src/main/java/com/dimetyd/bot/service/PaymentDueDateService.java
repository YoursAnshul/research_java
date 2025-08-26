package com.dimetyd.bot.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.CBMissingInvoiceOutput;
import com.dimetyd.bot.process.InvoiceDataUpdatePage;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class PaymentDueDateService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LoginVendorCentral loginObj;

    private InvoiceDataUpdatePage invoiceDataUpdatePage;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HashMap<String, String> loginHashMap = new HashMap<>();
    private int counter = 0;

    @PostConstruct
    public void startService() {
    	if (GlobalSession.getGlobalSession().getName().equals("Payment_due_date")) {

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

			BrowserContext context = null; // Create context without setting download path
			Page page = null;
			while (true) {
            try {
                
                // Update Invoice Status
             
                StringBuilder sql = new StringBuilder();
                sql.append(
                    "SELECT a.`invoiceNumber`, v.`vendorName`, a.`vendorId` " +
                    "FROM `CBMissingInvoiceOutput` a " +
                    "JOIN `Vendor` v ON (a.`vendorId` = v.`id`) " +
                    "JOIN `Client` c ON (c.`vendorId` = v.`id`) " +
                    "WHERE a.`updateInvoiceStatus` = 'PENDING' " +
                    "AND v.`isPaused` = 'N' " +
                    "ORDER BY RAND() LIMIT 1"
                );

                List<CBMissingInvoiceOutput> jobs = this.jdbcTemplate.query(
                    sql.toString(),
                    new RowMapper<CBMissingInvoiceOutput>() {
                        @Override
                        public CBMissingInvoiceOutput mapRow(ResultSet rs, int rowNum) throws SQLException {
                            CBMissingInvoiceOutput res = new CBMissingInvoiceOutput();
                            res.setInvoiceNumber(rs.getString("invoiceNumber"));
                            res.setVendorName(rs.getString("vendorName"));
            
                            res.setVendorId(rs.getString("vendorId"));
                            return res;
                        }
                    }
                );

                if (jobs.isEmpty()) {
                    logger.info(" No Data FoundStatus PENDING. Sleeping...");
                    Thread.sleep(1500);
                    continue;
                }

                CBMissingInvoiceOutput job = jobs.get(0);
                counter++;

                // Mark INPROGRESS for this invoice
                jdbcTemplate.execute(
                    "UPDATE `CBMissingInvoiceOutput` " +
                    "SET `updateInvoiceStatus` = 'INPROGRESS' " +
                    "WHERE `invoiceNumber` = '" + job.getInvoiceNumber() + "'"
                );

                // Login (reuses your existing login flow and session reuse counter)
                logger.info("Attempting login for vendorId: {}", job.getVendorId());
                var loginStatus = loginObj.loginProcess(
                    job.getVendorId(),
                    page,
                    job.getVendorName(),
                    loginHashMap,
                    counter,
                    browser,
                    context
                );
                page = loginStatus.getLeft();
                logger.info("Login status: {}", loginStatus.getRight());

                if (!loginStatus.getRight()) {
                    // Reset counter to force fresh session on next try
                    counter = 0;
                    logger.warn("Login failed. Reverting record to PENDING for retry.");
                    jdbcTemplate.execute(
                        "UPDATE `CBMissingInvoiceOutput` " +
                        "SET `updateInvoiceStatus` = 'PENDING' " +
                        "WHERE `invoiceNumber` = '" + job.getInvoiceNumber() + "'"
                    );
                    continue;
                }

                // Process the invoice update
                String result = invoiceDataUpdatePage.processPage(
                    page,
                    job.getInvoiceNumber(),
                    job.getVendorName()
                );

                if ("true".equalsIgnoreCase(result)) {
                    jdbcTemplate.execute(
                        "UPDATE `CBMissingInvoiceOutput` " +
                        "SET `updateInvoiceStatus` = 'COMPLETED' " +
                        "WHERE `invoiceNumber` = '" + job.getInvoiceNumber() + "'"
                    );
                    logger.info(" Invoice {} update COMPLETED.", job.getInvoiceNumber());
                } else if ("false".equalsIgnoreCase(result)) {
                    jdbcTemplate.execute(
                        "UPDATE `CBMissingInvoiceOutput` " +
                        "SET `updateInvoiceStatus` = 'ERROR' " +
                        "WHERE `invoiceNumber` = '" + job.getInvoiceNumber() + "'"
                    );
                    logger.info("Invoice {} update ERROR.", job.getInvoiceNumber());
                } else if ("NULL".equalsIgnoreCase(result)) {
                    jdbcTemplate.execute(
                        "UPDATE `CBMissingInvoiceOutput` " +
                        "SET `updateInvoiceStatus` = 'ERROR', `comments` = 'No Invoice Found' " +
                        "WHERE `invoiceNumber` = '" + job.getInvoiceNumber() + "'"
                    );
                    logger.info("Invoice {} update ERROR (No Invoice Found).", job.getInvoiceNumber());
                } else {
                    // Unknown return value: be conservative and mark as ERROR for visibility
                    jdbcTemplate.execute(
                        "UPDATE `CBMissingInvoiceOutput` " +
                        "SET `updateInvoiceStatus` = 'ERROR', `comments` = 'Unexpected result: " + result + "' " +
                        "WHERE `invoiceNumber` = '" + job.getInvoiceNumber() + "'"
                    );
                    logger.warn(" Invoice {} update ERROR (Unexpected result: {}).", job.getInvoiceNumber(), result);
                }

            } catch (Exception e) {
                logger.error("invoice page stage  loop error: ", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }
    }
}}
