package com.dimetyd.bot.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.process.ShortageDisputeUpdateProccess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.dimetyd.bot.model.ClinetDispute;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class Price_Claim_Dispute_Update_Service {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	ShortageDisputeUpdateProccess disputeProcess;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Price_Claim_Dispute_Update")) {

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
			/*
			 * context = browser.newContext(new Browser.NewContextOptions()
			 * .setViewportSize(1280, 800) .setIgnoreHTTPSErrors(true)
			 * .setJavaScriptEnabled(true) .setLocale("en-US") );
			 */

			// context.addInitScript("window.chrome = { runtime: {} };");

			// context = browser.newContext();
			// page = context.newPage();
			System.out.println("Page Started...");

			while (true) {

				System.out.println("Query Running");
				StringBuilder sql = new StringBuilder();
				sql.append(
						"SELECT cd.vendorId,v.vendorName,cd.disputeId,cd.disputeAmount,cd.`modifiedDate`,cd.currency,cd.type,cd.reason ,cd.isLookbackOrReRun,priority,cd.clientId "
								+ "FROM `CBClientDispute` cd JOIN `Vendor` v ON (v.id = cd.vendorId) \r\n"
								+ "JOIN `Client` c ON (c.vendorId = cd.vendorId) \r\n"
								+ "WHERE requestStatus = 'PENDING' \r\n"
								+ "AND cd.STATUS = 'ACTIVE' AND TYPE IN ('Price claim') \r\n"
								+ "AND v.isvendorMenuAccess=1 AND v.`isPaused`='N' \r\n"
								+ "ORDER BY `priority`,`master_Crd_Id`,`modifiedDate` ASC LIMIT 1");

				List<ClinetDispute> jobList = this.jdbcTemplate.query(sql.toString(), new RowMapper<ClinetDispute>() {
					@Override
					public ClinetDispute mapRow(ResultSet rs, int rowNum) throws SQLException {

						ClinetDispute cd = new ClinetDispute();
						cd.setVendorName(rs.getString("vendorName"));
						cd.setVendorId(rs.getString("vendorID"));
						cd.setDisputeId(rs.getString("disputeId"));
						cd.setModifiedDate(rs.getDate("modifiedDate"));
						cd.setCurrency(rs.getString("currency"));
						cd.setType(rs.getString("type"));
						cd.setReason(rs.getString("reason"));
						cd.setIsLookbackOrReRun(rs.getString("isLookbackOrReRun"));
						cd.setPriority(rs.getInt("priority"));
						cd.setDisputeAmount(rs.getDouble("disputeAmount"));
						cd.setClientId(rs.getInt("clientId"));

						return cd;
					}

				}, new Object[] {});

				if (jobList.size() == 0) {
					System.out.println("No job found...Waiting for new job");
					System.exit(0);

				} else {
					counter = counter + 1;

					System.out.println("Processing Dispute Id: " + jobList.get(0).getDisputeId() + "  Vendor Name: "
							+ jobList.get(0).getVendorName() + " vendorId: " + jobList.get(0).getVendorId());

					jdbcTemplate
							.execute("UPDATE CBClientDispute SET  requestStatus = 'INPROGRESS' , modifiedDate = now() "
									+ " WHERE  disputeId = '" + jobList.get(0).getDisputeId() + "' ");

					System.out.println("Query Running End");
					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
							jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();
					logger.info("login status: " + loginStatus.getRight());

					if (loginStatus.getRight()) {

						disputeProcess.processPage(page, jobList.get(0).getDisputeId(), jobList.get(0).getVendorId(),
								jobList.get(0).getVendorName(), jobList.get(0).getCurrency(),
								jobList.get(0).getReason(), jobList.get(0).getIsLookbackOrReRun(),
								jobList.get(0).getDisputeAmount(), jobList.get(0).getClientId());

					} else

					{
						jdbcTemplate.execute("UPDATE `CBClientDispute` SET `requestStatus` = 'ERROR' "
								+ "WHERE disputeId = '" + jobList.get(0).getDisputeId() + "' ");
					}

				}
			}

		}

	}

}
