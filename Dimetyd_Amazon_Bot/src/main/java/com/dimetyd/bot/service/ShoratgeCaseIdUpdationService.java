package com.dimetyd.bot.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import com.dimetyd.bot.model.ShortageCaseId;
import com.dimetyd.bot.model.Vendor;
import com.dimetyd.bot.process.CaseIdStatusUpdateProccess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class ShoratgeCaseIdUpdationService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	CaseIdStatusUpdateProccess CaseIdProcess;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("BulkShortageCaseStatus_update")) {

			logger.info("BulkShortageCaseStatus update bot started...");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			while (true) {
				String BotStage = "NO STAGE";
				System.out.println("Query Running");
				StringBuilder sql = new StringBuilder();
				sql.append(
						"SELECT c.id,c.vendorId,c.vendorName, c.caseId,c.currentCaseStatus FROM `CaseIdUpdationLog` c JOIN Vendor v ON(c.vendorId=v.id) JOIN "
								+ "CBShortageSummaryPayeeCode s ON (c.caseId=s.caseId AND c.vendorId=s.vendorId) WHERE  jobStatus='PENDING' AND ( s.isSettle =  0 OR s.isSettle IS NULL) "
								+ "AND v.`isVendorMenuAccess`='1'  AND v.`active`='Y' AND v.`isPaused`='N' ORDER BY v.master_Crd_Id LIMIT 1");

				List<ShortageCaseId> jobList = this.jdbcTemplate.query(sql.toString(), new RowMapper<ShortageCaseId>() {
					@Override
					public ShortageCaseId mapRow(ResultSet rs, int rowNum) throws SQLException {

						ShortageCaseId cd = new ShortageCaseId();
						cd.setId(rs.getString("id"));
						cd.setVendorName(rs.getString("vendorName"));
						cd.setVendorId(rs.getString("vendorID"));
						cd.setCaseId(rs.getString("caseId"));
						cd.setCurrentCaseStatus(rs.getString("currentCaseStatus"));
						return cd;
					}

				}, new Object[] {});

				if (jobList.size() == 0) {
					System.out.println("No job found in First Stage...Waiting for new job");
					StringBuilder sql2 = new StringBuilder();
					sql2.append(
							"SELECT c.id,c.vendorId,c.vendorName, c.caseId,c.currentCaseStatus FROM `CaseIdUpdationLog` c JOIN Vendor v ON(c.vendorId=v.id) JOIN "
									+ "CBShortageSummaryPayeeCode s ON (c.caseId=s.caseId AND c.vendorId=s.vendorId) WHERE  jobStatus='QUEUE' AND ( s.isSettle =  0 OR s.isSettle IS NULL) "
									+ "AND v.`isVendorMenuAccess`='1'  AND v.`active`='Y' AND v.`isPaused`='N' ORDER BY v.master_Crd_Id LIMIT 1");

					List<ShortageCaseId> jobListStage2 = this.jdbcTemplate.query(sql2.toString(),
							new RowMapper<ShortageCaseId>() {
								@Override
								public ShortageCaseId mapRow(ResultSet rs, int rowNum) throws SQLException {

									ShortageCaseId cd = new ShortageCaseId();
									cd.setId(rs.getString("id"));
									cd.setVendorName(rs.getString("vendorName"));
									cd.setVendorId(rs.getString("vendorID"));
									cd.setCaseId(rs.getString("caseId"));
									cd.setCurrentCaseStatus(rs.getString("currentCaseStatus"));
									return cd;
								}

							}, new Object[] {});
					if (jobListStage2.size() == 0) {
						System.out.println("No job found in First and Second Stage...Waiting for new job");
						System.exit(0);
					} else {
						System.out.println("STAGE 2");
						BotStage = "STAGE 2";

						String sql21 = "SELECT v.vendorName,vendorId FROM `VendorCredentials`  a JOIN Vendor v ON (a.vendorId=v.id)\r\n"
								+ "WHERE `userName` = 'audit@dimetyd.com' AND v.`isVendorMenuAccess` = 1 AND v.`active` = 'Y'\r\n"
								+ "LIMIT 1";

						List<Vendor> vendorList = this.jdbcTemplate.query(sql21, new RowMapper<Vendor>() {
							@Override
							public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
								Vendor job = new Vendor();

								job.setVendorId(rs.getString("vendorId"));
								job.setVendorName(rs.getString("vendorName"));

								return job;
							}
						});

						counter = counter + 1;
						logger.info(" Vendor Name: " + jobListStage2.get(0).getVendorName() + " vendorId: "
								+ jobListStage2.get(0).getVendorId());

						jdbcTemplate.execute(
								"UPDATE CaseIdUpdationLog SET jobStatus='INPROGRESS',   `lastUpdatedDateTime` = now() WHERE `caseId` = '"
										+ jobListStage2.get(0).getCaseId() + "'");

						logger.info("Query Running End");

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(vendorList.get(0).getVendorId(), page,
								vendorList.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();
						logger.info("login status: " + loginStatus.getRight());
						if (loginStatus.getRight()) {

							CaseIdProcess.updateStatusPage(page, jobListStage2.get(0).getId(),
									jobListStage2.get(0).getVendorId(), jobListStage2.get(0).getVendorName(),
									jobListStage2.get(0).getCaseId(), jobListStage2.get(0).getCurrentCaseStatus(),
									BotStage);

						}

					}
				} else {

					System.out.println("STAGE 1");
					BotStage = "STAGE 1";

					counter = counter + 1;
					logger.info(" Vendor Name: " + jobList.get(0).getVendorName() + " vendorId: "
							+ jobList.get(0).getVendorId());

					jdbcTemplate.execute(
							"UPDATE CaseIdUpdationLog SET jobStatus='INPROGRESS',   `lastUpdatedDateTime` = now() WHERE `caseId` = '"
									+ jobList.get(0).getCaseId() + "'");

					logger.info("Query Running End");

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
							jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();
					logger.info("login status: " + loginStatus.getRight());
					if (loginStatus.getRight()) {

						CaseIdProcess.updateStatusPage(page, jobList.get(0).getId(), jobList.get(0).getVendorId(),
								jobList.get(0).getVendorName(), jobList.get(0).getCaseId(),
								jobList.get(0).getCurrentCaseStatus(), BotStage);

					}

				}

			}

		}
	}

}
