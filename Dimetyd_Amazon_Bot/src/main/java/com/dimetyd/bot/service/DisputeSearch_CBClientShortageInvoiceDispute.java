package com.dimetyd.bot.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.OpenShortageDipsute;
import com.dimetyd.bot.process.MissingDisputeSearchProcess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.*;

import jakarta.annotation.PostConstruct;

@Component
public class DisputeSearch_CBClientShortageInvoiceDispute {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    LoginVendorCentral loginObj;

    @Autowired
    MissingDisputeSearchProcess dispute_searchProcess;

    static Page page;
    BrowserContext context;
    HashMap<String, String> loginHashMap = new HashMap<>();
    private int counter = 0;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void startService() {

        if (GlobalSession.getGlobalSession().getName().equals("search_Open_Shortage_Dispute_Search")) {

            logger.info("search open  Shortage dispute Update Bot Started...");

            long startTime = System.currentTimeMillis();
            long oneHour = 120 * 60 * 1000; // 2 hours

            List<String> list = new ArrayList<>();
            list.add("--disable-webauthn");
            list.add("--disable-features=PasswordlessLogin");

            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

            while (true) {

                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= oneHour) {
                    logger.info("Time limit reached. Stopping Bot.");
                    System.exit(0);
                }

                logger.info("Query Running");

                StringBuilder sql = new StringBuilder();
                sql.append("SELECT a.vendorid,v.vendorName, a.InvoiceNumber FROM `DisputeSearch_CBClientShortageInvoiceDispute` a ")
                   .append("JOIN Vendor v ON (a.vendorId = v.id) ")
                   .append("WHERE a.`status`='PENDING' AND v.isPaused = 'N' AND v.id =(SELECT vendorid FROM `DisputeSearch_CBClientShortageInvoiceDispute` c JOIN Vendor v ON(c.vendorId=v.id)")
                   .append("WHERE status = 'PENDING' AND v.isPaused = 'N' GROUP BY vendorid LIMIT 1) LIMIT 50");

                List<OpenShortageDipsute> jobList = jdbcTemplate.query(sql.toString(),
                        (ResultSet rs, int rowNum) -> {
                            OpenShortageDipsute cd = new OpenShortageDipsute();
                            cd.setVendorId(rs.getString("vendorID"));
                            cd.setVendorName(rs.getString("vendorName"));
                            cd.setDisputeInvoice(rs.getString("InvoiceNumber"));
                            return cd;
                        });

                if (jobList.isEmpty()) {
                    logger.info("No job found...Waiting for new job");
                    System.exit(0);
                }

                counter++;
                int totalPickedPendingJobs = jobList.size();

                logger.info("Processing InvoiceDispute Id: " + jobList.get(0).getDisputeInvoice()
                        + "  Vendor Name: " + jobList.get(0).getVendorName()
                        + " vendorId: " + jobList.get(0).getVendorId());

                //  all invoices as IN_PROGRESS
                for (OpenShortageDipsute job : jobList) {
                    String markInProgress = "UPDATE DisputeSearch_CBClientShortageInvoiceDispute " +
                            "SET status='IN_PROGRESS', createdDate=NOW() " +
                            "WHERE vendorId='" + job.getVendorId() + "' AND InvoiceNumber='" + job.getDisputeInvoice() + "'";
                    jdbcTemplate.execute(markInProgress);
                }

                Pair<Page, Boolean> loginStatus = loginObj.loginProcess(
                        jobList.get(0).getVendorId(), page, jobList.get(0).getVendorName(),
                        loginHashMap, counter, browser, context);
                page = loginStatus.getLeft();
                logger.info("login status: " + loginStatus.getRight());
                logger.info("Total PENDING jobs PICKED : " + totalPickedPendingJobs);

                if (loginStatus.getRight()) {
                    for (OpenShortageDipsute job : jobList) {
                    	String comment=null;
                        try {
                        	comment= dispute_searchProcess.InvoicedisputeProcess(
                                    page,
                                    job.getDisputeInvoice(),
                                    job.getVendorId(),
                                    job.getVendorName(),
                                   0,
                                   "disputesearch"
                            );

                            String markCompleted = "UPDATE DisputeSearch_CBClientShortageInvoiceDispute " +
                                    "SET status='COMPLETED', comment='"+comment+"', createdDate=NOW() " +
                                    "WHERE vendorId='" + job.getVendorId() + "' AND InvoiceNumber='" + job.getDisputeInvoice() + "'";
                            jdbcTemplate.execute(markCompleted);
                        } catch (Exception e) {
                        	logger.error("Error processing invoice " + job.getDisputeInvoice(), e);

                        	 comment = "Process error : " + e.getMessage().replace("'", "");
                        	String markError = "UPDATE DisputeSearch_CBClientShortageInvoiceDispute " +
                        	        "SET status='ERROR', comment='" + comment + "', createdDate=NOW() " +
                        	        "WHERE vendorId='" + job.getVendorId() + "' AND InvoiceNumber='" + job.getDisputeInvoice() + "'";

                        	jdbcTemplate.execute(markError);
                        }
                    }
                } else {
                    // login failure or dispute not found  as error only for the first invoice
                    String markLoginError = "UPDATE DisputeSearch_CBClientShortageInvoiceDispute " +
                            "SET status='ERROR', comment='LOGIN ISSUE', createdDate=NOW() " +
                            "WHERE vendorId='" + jobList.get(0).getVendorId() + "' AND InvoiceNumber='" + jobList.get(0).getDisputeInvoice() + "'";
                    jdbcTemplate.execute(markLoginError);
                }
            }
        }
    }
}
