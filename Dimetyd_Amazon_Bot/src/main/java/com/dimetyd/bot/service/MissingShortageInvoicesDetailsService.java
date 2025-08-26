package com.dimetyd.bot.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.dimetyd.bot.model.JobData;
import com.dimetyd.bot.model.JobType;
import com.dimetyd.bot.process.MissingShortageInvoiceDetails;
import com.dimetyd.bot.util.CommonUtil;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;
@Service
public class MissingShortageInvoicesDetailsService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	MissingShortageInvoiceDetails missingDueDateUpdate;

	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("missing_invoice_shortage_details_Service")) {

			logger.info("missing_invoice_shortage_details_Service Bot Started");
			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
			Path downloadPath = Paths.get("C:\\Playwright File");
			BrowserContext context = null; // Create context without setting download path
			Page page = null;

			int cnt = 1;
			while (true) {

				try {
					StringBuilder sql = new StringBuilder();

					sql.append(
							"SELECT `invoiceNumber`,`vendorId`,v.vendorName AS vendorName FROM invoice_paymentDuedate a JOIN Vendor v ON (a.vendorId = v.id) WHERE STATUS = 'pending' ORDER BY `vendorId` LIMIT 1");


					List<JobData> catlogList = this.jdbcTemplate.query(sql.toString(), new RowMapper<JobData>() {
						@Override
						public JobData mapRow(ResultSet rs, int rowNum) throws SQLException {
							JobData res = new JobData();

							res.setVendorId(rs.getString("vendorId"));
							res.setVendoName(rs.getString("vendorName"));
							res.setPoInvoice(rs.getString("invoiceNumber"));
							//res.setRetry(rs.getInt("retry"));
							return res;
						}
					}, new Object[] {});

					if (catlogList.size() == 0  ) {
						logger.info("No Payment Due Date Missing in Shortage Reconciliations Invoice");
						logger.info("JOBS ENDED");
						System.exit(0);
					}

					if (JobType.ASIN.equalsName("ASIN")) {
						cnt++;
						counter = counter + 1;

						JobData jobData = catlogList.get(0);
						String vendorId = jobData.getVendorId();
						logger.info("VendorId : " + vendorId);
						String vendorName = jobData.getVendoName();
						logger.info("Vendor Name : " + vendorName);
						if(vendorId==null && vendorName==null)
						{
							logger.info("vendorId and vendorName is null");
							logger.info("JOBS ENDED");
							System.exit(0);
						}
						String invoiceNumbers = jobData.getPoInvoice();

						/*Integer retry = jobData.getRetry();
						logger.info("-------REQUEST ALL STAGE 3 OLD UI-------");

						if (retry > 3) {
							jdbcTemplate.execute(
									"UPDATE invoice_paymentDuedate SET Status = 'ERROR' where invoiceNumber in("
											+ invoiceNumbers + ")");
						} 
						jdbcTemplate.execute(
								"update invoice_paymentDuedate set status='INPROGRESS' where invoiceNumber in("
										+ invoiceNumbers + ")");
*/
						StringBuilder sql1 = new StringBuilder();
						logger.info("Parent Invoice Numbers : " + invoiceNumbers);

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(vendorId, page, vendorName,
								loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();

						if (loginStatus.getRight()) {
							// isLoggedIn = "true";
							try {
								CommonUtil.updateTimeStamp();
							} catch (IOException e) {
								e.printStackTrace();
							}
							try {
								String vnderNameTrimed = vendorName.substring(0, 2).trim();
								logger.info("vnderNameTrimed : " + vnderNameTrimed);

								boolean OldUiJobstatus = missingDueDateUpdate.processPage(page, invoiceNumbers,
										vendorId,  vendorName, downloadPath);
								if (OldUiJobstatus) {

									logger.info(
											"UPDATE invoice_paymentDuedate SET Status = 'COMPLETED' where invoiceNumber in("
													+ invoiceNumbers + ")");
									jdbcTemplate.execute(
											"UPDATE invoice_paymentDuedate SET Status = 'COMPLETED' where invoiceNumber in("
													+ invoiceNumbers + ")");
								}

								else {
									logger.info(
											"UPDATE invoice_paymentDuedate SET Status = 'ERROR' where invoiceNumber in("
													+ invoiceNumbers + ")");
									jdbcTemplate.execute(
											"UPDATE invoice_paymentDuedate SET Status = 'ERROR' where invoiceNumber in("
													+ invoiceNumbers + ")");
								}

							} catch (Exception e) {
								e.printStackTrace();
								logger.info(
										"UPDATE invoice_paymentDuedate SET Status = 'ERROR' where invoiceNumber in("
												+ invoiceNumbers + ")");
								jdbcTemplate.execute(
										"UPDATE invoice_paymentDuedate SET Status = 'ERROR' where invoiceNumber in("
												+ invoiceNumbers + ")");

							}
						} else {
							logger.info(
									"UPDATE invoice_paymentDuedate SET Status = 'ERROR',botComment='Bot is having issue' where invoiceNumber in("
											+ invoiceNumbers + ")");
							jdbcTemplate.execute(
									"UPDATE invoice_paymentDuedate SET Status = 'ERROR',botComment='Bot is having issue' where invoiceNumber in("
											+ invoiceNumbers + ")");
							logger.info("ERROR in Login New UI Invoice Page..........");
							System.exit(0);
						}

						if (cnt > 300)
						{
							logger.info("300 Transactions Done.... Bot Stopped");
						    logger.info("JOBS ENDED");
							System.exit(0);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
					logger.info("Flow issue OLD INVOICE UI");
					System.exit(0);

				}

			}

		}

	}

}
