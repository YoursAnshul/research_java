package com.dimetyd.bot.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.OpenShortageDipsute;
import com.dimetyd.bot.process.DisputeUpdateProccess;
import com.dimetyd.bot.process.OpenShortageDisputeSearchProcess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;

@Component
public class OpenShortageDisputeSearchService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	OpenShortageDisputeSearchProcess InvoicedisputeProcess;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Open_Shortage_Dispute_Search")) {

			logger.info("Shortage Invoice Dispute Update Bot Started...");

			long startTime = System.currentTimeMillis(); // Start time in milliseconds
			long oneHour = 120 * 60 * 1000; // 2 hour in milliseconds

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

				/*
				 * sql.append(
				 * "SELECT a.vendorid,v.vendorName, a.InvoiceNumber FROM `CBClientShortageInvoiceDispute` a JOIN Vendor v ON (a.vendorId = v.id)\r\n"
				 * + "WHERE  a.`status`='PENDING'  ORDER BY RAND() LIMIT 1;");
				 */

				sql.append(
						"SELECT a.vendorid,v.vendorName, a.InvoiceNumber FROM `CBClientShortageInvoiceDispute` a JOIN Vendor v ON (a.vendorId = v.id)\r\n"
								+ "WHERE `status`='PENDING' AND  v.id =(SELECT vendorid FROM `CBClientShortageInvoiceDispute` WHERE `status`='PENDING'  GROUP BY vendorid LIMIT 1) LIMIT 50");

				List<OpenShortageDipsute> jobList = this.jdbcTemplate.query(sql.toString(),
						new RowMapper<OpenShortageDipsute>() {
							@Override
							public OpenShortageDipsute mapRow(ResultSet rs, int rowNum) throws SQLException {

								OpenShortageDipsute cd = new OpenShortageDipsute();
								cd.setVendorId(rs.getString("vendorID"));
								cd.setVendorName(rs.getString("vendorName"));
								cd.setDisputeInvoice(rs.getString("InvoiceNumber"));
								return cd;
							}

						}, new Object[] {});

				if (jobList.size() == 0) {
					logger.info("No job found...Waiting for new job");
					System.exit(0);

				} else {
					counter = counter + 1;

					int totalPickedPendingJobs = jobList.size();
					logger.info(
							"Processing InvoiceDispute Id: " + jobList.get(0).getDisputeInvoice() + "  Vendor Name: "
									+ jobList.get(0).getVendorName() + " vendorId: " + jobList.get(0).getVendorId());

					logger.info("Query Running End");

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
							jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();
					logger.info("login status: " + loginStatus.getRight());
					logger.info("Total PENDING jobs PICKED : " + totalPickedPendingJobs);
					if (loginStatus.getRight()) {

						for (int i = 0; i < totalPickedPendingJobs; i++) {
							InvoicedisputeProcess.InvoicedisputeProcess(page, jobList.get(i).getDisputeInvoice(),
									jobList.get(i).getVendorId(), jobList.get(i).getVendorName(), i);
						}
					} else

					{

						String sql1 = "update CBClientShortageInvoiceDispute set `createdDate`=NOW(), `comment`='LOGIN ISSUE' where vendorId='"
								+ jobList.get(0).getVendorId() + "' and InvoiceNumber='"
								+ jobList.get(0).getDisputeInvoice() + "'";
						logger.info(sql1);
						jdbcTemplate.execute(sql1);
					}

				}
			}

		}

	}

}
