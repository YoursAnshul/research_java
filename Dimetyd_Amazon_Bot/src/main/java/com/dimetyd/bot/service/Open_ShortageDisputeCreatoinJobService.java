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
import com.dimetyd.bot.model.OpenitemizedshortageInvoicesToBeCreated;
import com.dimetyd.bot.process.Open_Shortage_Create_Dispute;
import com.dimetyd.bot.process.Open_Shortage_Create_Re_Dispute;
import com.dimetyd.bot.util.CommonUtil;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class Open_ShortageDisputeCreatoinJobService {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	CommonUtil commonobj;

	@Autowired
	Open_Shortage_Create_Re_Dispute stage1;

	@Autowired
	Open_Shortage_Create_Dispute stage2;

	Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<>();
	private int counter = 0;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Open_Shortage_Dispute_Creation")) {

			logger.info("Open Shortage Dispute Creation Bot Started...");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			String lastVendorId = null;

			long startTime = System.currentTimeMillis(); // Start time in milliseconds
			long oneHour = 60 * 120 * 1000; // 1 hour in milliseconds

			while (true) {

				long elapsedTime = System.currentTimeMillis() - startTime;

				if (elapsedTime >= oneHour) {
					logger.info("Time limit reached. Stopping Bot.");
					System.exit(0);

				}

				logger.info("Checking Stage 1 transactions...");

				String sql = "SELECT a.id as JobId, v.vendorName, vendorId, payee, invoiceNumber, disputeId, submitType, invoiceDate "
						+ "FROM CB_Open_Shortage_Disputes_To_Submit a " + "JOIN Vendor v ON a.vendorid = v.id "
						+ "WHERE status = 'PENDING' AND v.isPaused = 'N' ORDER BY v.vendorName,v.master_Crd_Id LIMIT 1";
								

				List<OpenitemizedshortageInvoicesToBeCreated> jobList = jdbcTemplate.query(sql,
						new RowMapper<OpenitemizedshortageInvoicesToBeCreated>() {
							@Override
							public OpenitemizedshortageInvoicesToBeCreated mapRow(ResultSet rs, int rowNum)
									throws SQLException {
								OpenitemizedshortageInvoicesToBeCreated job = new OpenitemizedshortageInvoicesToBeCreated();
								job.setJobId(rs.getString("JobId"));
								job.setVendorname(rs.getString("vendorName"));
								job.setVendorId(rs.getString("vendorId"));
								job.setPayee(rs.getString("payee"));
								job.setinvoiceNumber(rs.getString("invoiceNumber"));
								job.setInvoiceDate(rs.getString("invoiceDate"));
								job.setDisputeId(rs.getString("disputeId"));
								job.setSubmitType(rs.getString("submitType"));
								return job;
							}
						});

				if (jobList.isEmpty()) {

					logger.info(
							"INSERT IGNORE INTO `CB_Open_Shortage_Dispute` (`disputeId`,`vendorId`,`disputeType`,`disputeReason`,`disputeDate`,`disputeStatus`,`totalDisputedAmount`)\r\n"
									+ "select a.`disputeId`,a.`vendorId`,b.`submitType`,'These units were shipped',date(a.`disputeDate`),'Pending Amazon action',a.`disputeAmount`\r\n"
									+ "from  `CBClientDispute` a join `CB_Open_Shortage_Disputes_To_Submit` b on (a.`disputeId`=b.`newSubmittedDisputeId`)\r\n"
									+ "where `type` = 'open_shortage'");
					jdbcTemplate.execute(
							"INSERT IGNORE INTO `CB_Open_Shortage_Dispute` (`disputeId`,`vendorId`,`disputeType`,`disputeReason`,`disputeDate`,`disputeStatus`,`totalDisputedAmount`)\r\n"
									+ "select a.`disputeId`,a.`vendorId`,b.`submitType`,'These units were shipped',date(a.`disputeDate`),'Pending Amazon action',a.`disputeAmount`\r\n"
									+ "from  `CBClientDispute` a join `CB_Open_Shortage_Disputes_To_Submit` b on (a.`disputeId`=b.`newSubmittedDisputeId`)\r\n"
									+ "where `type` = 'open_shortage'");
					logger.info("No pending jobs found. Stopping the bot.");
					System.exit(0);
				}

				OpenitemizedshortageInvoicesToBeCreated job = jobList.get(0);
				counter++;

				// job as INPROGRESS using raw SQL string
				jdbcTemplate.execute(
						"UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = 'INPROGRESS' " + "WHERE vendorId = '"
								+ job.getVendorId() + "' AND invoiceNumber = '" + job.getInvoiceNumber() + "'");

				logger.info("Processing Vendor: " + job.getVendorname());

				// Login only if vendor is new
				if (lastVendorId == null || !lastVendorId.equals(job.getVendorId())) {
					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(job.getVendorId(), page,
							job.getVendorname(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();
					logger.info("Login status: " + loginStatus.getRight());

					if (!loginStatus.getRight()) {
						jdbcTemplate.execute("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = 'ERROR' "
								+ "WHERE vendorId = '" + job.getVendorId() + "' AND invoiceNumber = '"
								+ job.getInvoiceNumber() + "'");
						continue;
					}

					lastVendorId = job.getVendorId();
				}

				try {
					String disputeStatus = "";

					if ("Shortage Dispute".equalsIgnoreCase(job.getSubmitType())) {
						disputeStatus = stage2.SubmitDispute(page, job.getInvoiceNumber(), job.getVendorId(),
								job.getInvoiceAmount(), job.getVendorname(), job.getDisputeId(), job.getPayee(),
								job.getSubmitType());

					} else if ("Shortage Re-dispute".equalsIgnoreCase(job.getSubmitType())
							|| "Shortage Invoice Re-Dispute".equalsIgnoreCase(job.getSubmitType())) {
						disputeStatus = stage1.SubmitDispute(page, job.getDisputeId(), job.getVendorId(),
								job.getInvoiceAmount(), job.getVendorname(), job.getPayee(), job.getInvoiceNumber(),
								job.getSubmitType());

					} else {
						logger.warn("Unknown submitType: " + job.getSubmitType());
						disputeStatus = "UNKNOWN_SUBMIT_TYPE";
					}

					/*
					 * // Update with final status jdbcTemplate.execute(
					 * "UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = '" + disputeStatus +
					 * "' " + "WHERE vendorId = '" + job.getVendorId() + "' AND invoiceNumber = '" +
					 * job.getInvoiceNumber() + "'");
					 * logger.info("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = '" +
					 * disputeStatus + "' " + "WHERE vendorId = '" + job.getVendorId() +
					 * "' AND invoiceNumber = '" + job.getInvoiceNumber() + "'");
					 * 
					 * 
					 * 
					 * 
					 * 
					 * } catch (Exception e) { logger.error("Error during dispute submission", e);
					 * jdbcTemplate.execute(
					 * "UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = 'ERROR' " +
					 * "WHERE vendorId = '" + job.getVendorId() + "' AND invoiceNumber = '" +
					 * job.getInvoiceNumber() + "'"); }
					 */
					// Final status update based on submitType
					if ("Shortage Re-dispute".equalsIgnoreCase(job.getSubmitType())
							|| "Shortage Invoice Re-Dispute".equalsIgnoreCase(job.getSubmitType())) {
						jdbcTemplate.execute("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = '" + disputeStatus
								+ "' " + "WHERE vendorId = '" + job.getVendorId() + "' AND disputeId = '"
								+ job.getDisputeId() + "'");
						logger.info("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = '" + disputeStatus + "' "
								+ "WHERE vendorId = '" + job.getVendorId() + "' AND disputeId = '" + job.getDisputeId()
								+ "'");
					} else {
						jdbcTemplate.execute("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = '" + disputeStatus
								+ "' " + "WHERE vendorId = '" + job.getVendorId() + "' AND invoiceNumber = '"
								+ job.getInvoiceNumber() + "'");
						logger.info("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = '" + disputeStatus + "' "
								+ "WHERE vendorId = '" + job.getVendorId() + "' AND invoiceNumber = '"
								+ job.getInvoiceNumber() + "'");
					}

				} catch (Exception e) {
					logger.error("Error during dispute submission", e);

					if ("Shortage Re-dispute".equalsIgnoreCase(job.getSubmitType())
							|| "Shortage Invoice Re-Dispute".equalsIgnoreCase(job.getSubmitType())) {
						jdbcTemplate.execute("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = 'ERROR' "
								+ "WHERE vendorId = '" + job.getVendorId() + "' AND disputeId = '" + job.getDisputeId()
								+ "'");
						logger.info("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = 'ERROR' "
								+ "WHERE vendorId = '" + job.getVendorId() + "' AND disputeId = '" + job.getDisputeId()
								+ "'");
					} else {
						jdbcTemplate.execute("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = 'ERROR' "
								+ "WHERE vendorId = '" + job.getVendorId() + "' AND invoiceNumber = '"
								+ job.getInvoiceNumber() + "'");
						logger.info("UPDATE CB_Open_Shortage_Disputes_To_Submit SET status = 'ERROR' "
								+ "WHERE vendorId = '" + job.getVendorId() + "' AND invoiceNumber = '"
								+ job.getInvoiceNumber() + "'");
					}
				}

			}
		}
	}
}
