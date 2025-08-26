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
import com.dimetyd.bot.model.ClinetDispute;
import com.dimetyd.bot.process.DisputesFileDownloadInsertProcess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class Open_ShortageDisputeUpdateFileService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	DisputesFileDownloadInsertProcess disputeProcess;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Shortage_Dispute_File_update")) {

			logger.info("Open_Shortage File Dispute Update bot started...");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			String DisputeRequestTableName = "CBClientDispute";
			String DisputeDataTableName = "CB_Open_Shortage_Dispute";

			logger.info("Dispute Shortage Jobs : Inprogress Jobs Updating to PENDING");
			logger.info("UPDATE " + DisputeRequestTableName
					+ " SET  requestStatus = 'PENDING'  WHERE  Type='open_shortage' AND requestStatus='Inprogress' AND STATUS='ACTIVE'");
			jdbcTemplate.execute("UPDATE " + DisputeRequestTableName
					+ " SET  requestStatus"
					+ " = 'PENDING'  WHERE  Type='open_shortage' AND requestStatus='Inprogress' AND STATUS='ACTIVE'");

			logger.info("Page Started...");

			while (true) {
				try {

					logger.info("Query Running");
					StringBuilder sql = new StringBuilder();

					sql.append("SELECT cd.vendorId,v.vendorName"
							+ "  FROM  "+DisputeRequestTableName+" cd JOIN `Vendor` v ON (v.id = cd.vendorId)"
							+ "JOIN `Client` c ON (c.vendorId = cd.vendorId) "
							+ "WHERE requestStatus = 'PENDING' "
							+ "AND cd.STATUS = 'ACTIVE' AND TYPE IN ('open_shortage')"
							+ "AND v.isvendorMenuAccess=1 AND v.`isPaused`='N' "
							+ "GROUP BY cd.vendorId,v.vendorName LIMIT 1");

					List<ClinetDispute> jobList = this.jdbcTemplate.query(sql.toString(),
							new RowMapper<ClinetDispute>() {
								@Override
								public ClinetDispute mapRow(ResultSet rs, int rowNum) throws SQLException {

									ClinetDispute cd = new ClinetDispute();
									cd.setVendorName(rs.getString("vendorName"));
									cd.setVendorId(rs.getString("vendorID"));

									return cd;
								}

							}, new Object[] {});

					if (jobList.size() == 0) {
						logger.info("JOBS ENDED");
						System.exit(0);
						break;

					} else {

						counter = counter + 1;

						int totalPendingJobsPicked = jobList.size();

						logger.info("Processing Vendor Name: " + jobList.get(0).getVendorName() + " vendorId: "
								+ jobList.get(0).getVendorId());
						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
								jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();

						if (loginStatus.getRight()) {

							{

								jdbcTemplate.execute("UPDATE " + DisputeRequestTableName
										+ " SET  requestStatus = 'INPROGRESS' "
										+ " WHERE  vendorId = '" + jobList.get(0).getVendorId() + "' ");

								try {
									disputeProcess.processPage(page, jobList.get(0).getVendorId(),
											jobList.get(0).getVendorName(), jobList.get(0).getType(),
											DisputeDataTableName);
								} catch (Exception ex) {
									ex.printStackTrace();
									jdbcTemplate.execute(
											"UPDATE " + DisputeRequestTableName + " SET `requestStatus` = 'ERROR' "
													+ "WHERE vendorId = '" + jobList.get(0).getVendorId() + "' ");
								}

							}

						} else

						{
							jdbcTemplate.execute("UPDATE " + DisputeRequestTableName + " SET `requestStatus` = 'ERROR' "
									+ "WHERE vendorId = '" + jobList.get(0).getVendorId() + "' ");
						}

					}
				}

				catch (Exception ex) {
					ex.printStackTrace();
					logger.info("JOBS ENDED");
					System.exit(0);
				}
			}

		}

	}

}
