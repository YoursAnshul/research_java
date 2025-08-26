package com.dimetyd.bot.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.ShortageJobData;
import com.dimetyd.bot.process.EmailService;
import com.dimetyd.bot.process.GoGreen_RemmittanceData;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class ShortageJobGoGreenService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	GoGreen_RemmittanceData shortagePage;

	@Autowired
	EmailService Mailobj;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("GoGreenRemmitanceEmailService")) {

			logger.info("Shortage bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			// **Step 1: Delete old data from financialReport_Input**
			jdbcTemplate.execute("DELETE FROM CronJob_Input");
			logger.info("Old financial report data deleted.");

			// **Step 2: Insert new data into financialReport_Input**
			int InsertedRows = jdbcTemplate
					.update("INSERT INTO CronJob_Input(vendorName, vendorId, year, startDate, endDate, status) "
							+ "SELECT vendorName, b.vendorId, '2025', DATE_ADD(NOW(), INTERVAL -2 DAY), NOW(), 'Pending' "
							+ "FROM Vendor a JOIN Client b ON a.id = b.vendorId "
							+ "AND b.vendorId IN ('VN20221020035647')");
			logger.info("Inserted new financial report data : " + InsertedRows);

			logger.info("Page Started...");

			while (true) {

				StringBuilder sql = new StringBuilder();
				sql.append(
						"SELECT cd.id,cd.`vendorName`,cd.`vendorId`,cd.`year`,cd.`startDate`,cd.`endDate`,cd.`status`\r\n"
								+ "FROM `CronJob_Input` cd JOIN `Vendor` v ON (v.id = cd.vendorId) \r\n"
								+ "JOIN `Client` c ON (c.vendorId = cd.vendorId) \r\n"
								+ "WHERE cd.`status` = 'PENDING' \r\n"
								+ "AND v.isvendorMenuAccess=1 AND v.`isPaused`='N'  ORDER BY `master_Crd_Id` ASC LIMIT 1");
				List<ShortageJobData> catlogList = this.jdbcTemplate.query(sql.toString(),
						new RowMapper<ShortageJobData>() {
							@Override
							public ShortageJobData mapRow(ResultSet rs, int rowNum) throws SQLException {
								ShortageJobData res = new ShortageJobData();
								res.setId(rs.getLong("id"));
								res.setVendorId(rs.getString("vendorId"));
								res.setVendorName(rs.getString("vendorName"));
								res.setYear(rs.getInt("year"));
								res.setStartDate(rs.getString("startDate"));
								res.setEndDate(rs.getString("endDate"));
								res.setStatus(rs.getString("status"));

								return res;
							}
						}, new Object[] {});

				if (catlogList.size() == 0) {
					logger.info("No Data Found in Stage");
					System.exit(0);
					
				} else {
					counter = counter + 1;
					// insertCBBotTransactionDetails(processName, transName, vendorName);

					jdbcTemplate.execute("update CronJob_Input set status='INPROGRESS' " + "where id='"
							+ catlogList.get(0).getId() + "'");

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(catlogList.get(0).getVendorId(), page,
							catlogList.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();

					if (loginStatus.getRight()) {

						boolean status = shortagePage.processPage(page, catlogList.get(0).getId(),
								catlogList.get(0).getVendorId(), catlogList.get(0).getVendorName(),
								catlogList.get(0).getStartDate(), catlogList.get(0).getEndDate(),
								Paths.get("C:\\PalyWrightFile"));

						if (status) {
							logger.info("update CronJob_Input set status='COMPLETED' where id='"
									+ catlogList.get(0).getId() + "'");
							jdbcTemplate.execute("update CronJob_Input set status='COMPLETED' where id='"
									+ catlogList.get(0).getId() + "'");

							String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

							String[] toRecipients = {"rbhattacharya@threecolts.com", "kchawla@threecolts.com"};
							String[] ccRecipients = {"asingh@threecolts.com", "pshinde@threecolts.com"};
							String subject = "US - GoGreen Power Cron Job on " + currentDate;

							String pickQuery = "SELECT v.vendorName,`vendorId`,`paymentNumber`,`invoiceNumber`,`invoiceDate`,`Description`,`invoiceAmount`,`termsDiscountTaken`,`amountPaid`,\r\n"
									+ "`remainingAmountAsOf`,c.`createdDate`,`paymentDate`,`currency` FROM `CBShortageReconciliation_cron` c JOIN Vendor v ON(c.vendorId=v.id)\r\n"
									+ "WHERE `mailSent`='' OR `mailSent` IS NULL";

							List<Map<String, Object>> reportData = jdbcTemplate.queryForList(pickQuery);
							if (!reportData.isEmpty()) {
								File csvFile = generateCSVFile(reportData);
								String body = "Hello Team,\n\nPlease find the attached payment reconciliation for GoGreen Power Cron Job report for today.\n\nRegards,\nDimetyd Team";
								Mailobj.sendMailToClientWithAttchment(toRecipients, ccRecipients, subject, body, csvFile);
								jdbcTemplate.execute(
										"UPDATE CBShortageReconciliation_cron SET mailSent='YES' WHERE mailSent='' OR mailSent IS NULL");
								logger.info("Payment reconciliation report sent successfully.");
							} else {
								String body = "Hello Team,\n\nThis is to inform you that no new payment data was found for today’s GoGreen Power Cron Job.\n\nRegards,\nDimetyd Team";
								Mailobj.sendMailToClientWithAttchment(toRecipients, ccRecipients, subject, body, null);
								logger.info("No data found. Notification email sent.");
							}

						}

						else {
							logger.info("update CronJob_Input set status='ERROR' where id='" + catlogList.get(0).getId()
									+ "'");
							jdbcTemplate.execute("update CronJob_Input set status='ERROR' where id='"
									+ catlogList.get(0).getId() + "'");

						}

					}

				}

			}

		}

	}

	public File generateCSVFile(List<Map<String, Object>> reportData) {
		String directoryPath = "C:\\PlaywrightFiles\\"; // Ensure this directory exists
		String fileName = "GoGreen_Payment_Reconciliation_Cron"
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";
		File file = new File(directoryPath + fileName);

		// **Step 1: Create directory if it does not exist**
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdirs(); // Creates directory (including parent dirs if needed)
		} // Ensure this directory exists
		try (FileWriter writer = new FileWriter(file)) {
			if (!reportData.isEmpty()) {
				// **Write Header**
				Map<String, Object> firstRow = reportData.get(0);
				writer.append(String.join(",", firstRow.keySet()));
				writer.append("\n");

				// **Write Data Rows**
				for (Map<String, Object> row : reportData) {
					writer.append(String.join(",",
							row.values().stream()
									.map(value -> value != null ? value.toString().replaceAll(",", " ") : "") // Remove
																												// commas
																												// from
																												// data
									.toArray(String[]::new)));
					writer.append("\n");
				}
			}
		} catch (IOException e) {
			logger.error("Error while creating CSV file", e);
		}

		return file;
	}

}
