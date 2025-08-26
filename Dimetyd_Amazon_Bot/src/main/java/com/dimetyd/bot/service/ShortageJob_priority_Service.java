package com.dimetyd.bot.service;

import java.nio.file.Paths;
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
import com.dimetyd.bot.model.ShortageJobData;
import com.dimetyd.bot.process.ShortagePage;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class ShortageJob_priority_Service {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	ShortagePage shortagePage;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Shortage_Job_Priority")) {

			logger.info("Shortage bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			logger.info("Page Started...");

			long startTime = System.currentTimeMillis(); // Start time in milliseconds
			long oneHour = 60 * 60 * 1000; // 1 hour in milliseconds

			while (true) {

				long elapsedTime = System.currentTimeMillis() - startTime;

				if (elapsedTime >= oneHour) {
					logger.info("Time limit reached. Stopping Bot.");
					System.exit(0);

				}

				StringBuilder sql = new StringBuilder();
				sql.append(
						"SELECT cr.id AS id, c.id AS requestId, c.RequestType, c.vendorId, v.vendorName, cr.agreementId, cr.startDt, cr.endDt, c.createdBy FROM CBRequest c JOIN CBAgreementRequestDetails cr ON c.id = cr.requestId JOIN Vendor v ON v.id = c.vendorId WHERE cr.status = 'PENDING' AND v.isPaused = 'N' AND c.RequestType IN ('SHORTAGE_RECONCILIATION') ORDER BY CASE WHEN v.jobPriority = 111 THEN 0 ELSE 1 END, v.jobPriority, RAND() LIMIT 1;");
				List<ShortageJobData> catlogList = this.jdbcTemplate.query(sql.toString(),
						new RowMapper<ShortageJobData>() {
							@Override
							public ShortageJobData mapRow(ResultSet rs, int rowNum) throws SQLException {
								ShortageJobData res = new ShortageJobData();

								res.setVendorId(rs.getString("vendorId"));
								res.setVendorName(rs.getString("vendorName"));
								res.setAgreementId(rs.getString("agreementId"));
								res.setRequestId(rs.getLong("requestId"));
								res.setId(rs.getLong("id"));
								res.setRequestType(rs.getString("RequestType"));
								res.setStartDate(rs.getString("startDt"));
								res.setEndDate(rs.getString("endDt"));
								res.setCreatedBy(rs.getInt("createdBy"));

								return res;
							}
						}, new Object[] {});

				if (catlogList.size() == 0) {
					logger.info("Jobs not found stoppig bot...");
					System.exit(0);
				} else {
					counter = counter + 1;
					// insertCBBotTransactionDetails(processName, transName, vendorName);
					jdbcTemplate.execute(
							"update CBRequest set requestStatus='INPROGRESS',processedStartDate =NOW() where id='"
									+ catlogList.get(0).getRequestId() + "'");
					jdbcTemplate.execute(
							"update CBAgreementRequestDetails set status='INPROGRESS',processedStartDate =NOW() "
									+ "where id='" + catlogList.get(0).getId() + "'");

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(catlogList.get(0).getVendorId(), page,
							catlogList.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();

					if (loginStatus.getRight()) {

						boolean status = shortagePage.processPage(page, catlogList.get(0).getId(),
								catlogList.get(0).getVendorId(), catlogList.get(0).getVendorName(),
								catlogList.get(0).getRequestId().toString(), catlogList.get(0).getStartDate(),
								catlogList.get(0).getEndDate(), catlogList.get(0).getCreatedBy(),
								Paths.get("C:\\PalyWrightFile"));

						if (status) {
							logger.info("update CBAgreementRequestDetails set status='COMPLETED' where id='"
									+ catlogList.get(0).getId() + "'");
							jdbcTemplate.execute("update CBAgreementRequestDetails set status='COMPLETED' where id='"
									+ catlogList.get(0).getId() + "'");

						}

						else {
							logger.info("update CBAgreementRequestDetails set status='ERROR' where id='"
									+ catlogList.get(0).getId() + "'");
							jdbcTemplate.execute("update CBAgreementRequestDetails set status='ERROR' where id='"
									+ catlogList.get(0).getId() + "'");

						}

					}

				}

			}

		}

	}

}
