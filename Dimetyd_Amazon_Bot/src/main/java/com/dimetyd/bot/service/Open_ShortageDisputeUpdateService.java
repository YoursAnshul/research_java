package com.dimetyd.bot.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.ClinetDispute;
import com.dimetyd.bot.process.Open_ShortageDisputeUpdateProccess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.*;

import jakarta.annotation.PostConstruct;

@Service
public class Open_ShortageDisputeUpdateService {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	Open_ShortageDisputeUpdateProccess disputeProcess;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {
		if (!GlobalSession.getGlobalSession().getName().equals("Open_Shortage_Dispute_update")) {
			return;
		}

		logger.info("Shortage_Dispute_update bot started.");

		List<String> launchArgs = new ArrayList<>();
		launchArgs.add("--disable-webauthn");
		launchArgs.add("--disable-features=PasswordlessLogin");

		Playwright playwright = Playwright.create();
		Browser browser = playwright.chromium()
				.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(launchArgs));

		String lastVendorId = null;
		
		
		long startTime = System.currentTimeMillis(); // Start time in milliseconds
		long oneHour = 60 * 120 * 1000; // 1 hour in milliseconds

		while (true) {
			
			
			long elapsedTime = System.currentTimeMillis() - startTime;

			if (elapsedTime >= oneHour) {
				logger.info("Time limit reached. Stopping Bot.");
				System.exit(0);

			}
			
			
			logger.info("Query Running for next 50 disputes");

			String sql = " SELECT cd.vendorId, v.vendorName, cd.disputeId, cd.disputeAmount, cd.modifiedDate,\r\n"
					+ "                           cd.currency, cd.type, cd.reason, cd.isLookbackOrReRun, priority, cd.clientId\r\n"
					+ "                    FROM CBClientDispute cd\r\n"
					+ "                    JOIN Vendor v ON v.id = cd.vendorId\r\n"
					+ "                    JOIN Client c ON c.vendorId = cd.vendorId\r\n"
					+ "                    WHERE requestStatus IN('PENDING','INPROGRESS')\r\n"
					+ "                      AND cd.STATUS = 'ACTIVE'\r\n"
					+ "                      AND TYPE IN ('open_shortage')\r\n"
					+ "                      AND v.isvendorMenuAccess = 1\r\n"
					+ "                      AND v.isPaused = 'N' AND cd.disputeId IN('DSPT10349405535','DSPT20124192351','DSPT20090637919','DSPT21969686111','DSPT22070398559','DSPT21667712607','DSPT20979871327','DSPT20761734751','DSPT20392685151','DSPT10953408863','DSPT21751606879','DSPT20929523295','DSPT21046978143')"
					+ "                    ORDER BY cd.vendorId, master_Crd_Id, modifiedDate\r\n"
					+ "                    LIMIT 50";

			List<ClinetDispute> jobList = jdbcTemplate.query(sql, new RowMapper<ClinetDispute>() {
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
			});

			if (jobList.isEmpty()) {
				logger.info("No disputes found. Retrying after 1 minute...");
				try {
					Thread.sleep(60000); // wait 60 seconds
				} catch (InterruptedException e) {
					logger.error("Sleep interrupted", e);
				}
				continue;
			}

			for (ClinetDispute job : jobList) {
				logger.info("Preparing dispute: {}", job.getDisputeId());
				jdbcTemplate.execute("UPDATE CBClientDispute SET requestStatus = 'INPROGRESS', modifiedDate = NOW() "
						+ "WHERE disputeId = '" + job.getDisputeId() + "'");
			}

			for (ClinetDispute job : jobList) {
				String vendorId = job.getVendorId();
				String vendorName = job.getVendorName();

				counter++;

				// Login only if vendor changes
				if (lastVendorId == null || !lastVendorId.equals(vendorId)) {
					logger.info("Logging in for vendor: {} ({})", vendorName, vendorId);

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(vendorId, page, vendorName, loginHashMap,
							counter, browser, context);
					page = loginStatus.getLeft();

					if (!loginStatus.getRight()) {
						logger.warn("Login failed for vendor: {}", vendorName);
						jdbcTemplate.execute("UPDATE CBClientDispute SET requestStatus = 'ERROR' "
								+ "WHERE disputeId = '" + job.getDisputeId() + "'");
						continue;
					}

					lastVendorId = vendorId;
					loginHashMap.put(vendorId, "LOGGED_IN");
				} else {
					logger.info("Already logged in for vendor: {}", vendorName);
				}

				try {
					disputeProcess.processPage(page, job.getDisputeId(), job.getVendorId(), job.getVendorName(),
							job.getCurrency(), job.getReason(), job.getIsLookbackOrReRun(), job.getDisputeAmount(),
							job.getClientId());
				} catch (Exception e) {
					logger.error("Error processing dispute ID: {}", job.getDisputeId(), e);
					jdbcTemplate.execute("UPDATE CBClientDispute SET requestStatus = 'ERROR' " + "WHERE disputeId = '"
							+ job.getDisputeId() + "'");
				}
			}
		}
	}
}
