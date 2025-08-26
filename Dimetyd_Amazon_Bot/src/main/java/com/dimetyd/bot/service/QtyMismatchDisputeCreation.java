package com.dimetyd.bot.service;

import java.nio.file.Path;
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
import com.dimetyd.bot.model.CoopDisputeTobeSubmitted;
import com.dimetyd.bot.process.Stage_1_FileDownloadAndGetInvoiceId;
import com.dimetyd.bot.process.Stage_2_Submit_Dispute_Coop;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class QtyMismatchDisputeCreation {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	Stage_1_FileDownloadAndGetInvoiceId FileDownloadInsert;

	@Autowired
	Stage_2_Submit_Dispute_Coop createDispute;

	Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("QTY_Mismatch_Dispute_Creation")) {

			logger.info("QTY_Mismatch_Dispute_Creation...");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");
			
			Path downloadPath = Paths.get("C:\\Users\\sphinx\\Documents");

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			//context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 800)
			//		.setIgnoreHTTPSErrors(true).setJavaScriptEnabled(true).setLocale("en-US"));

			//page = context.newPage();

			while (true) {

				logger.info("Checking transcation in database..");
				logger.info("Checking Stage 1 transcation........");
				StringBuilder sql = new StringBuilder();
				sql.append(
						" SELECT c.`id`,c.`vendorId`,v.`vendorName`,c.`userId`,c.`isLookBackOrReRun` FROM `CBCoopDisputeTobeSubmitted` c JOIN red_T_Vendor v ON(c.vendorId=v.id) WHERE `disputeSubmittedStatus`='PENDING' GROUP BY `id` LIMIT 1");

				List<CoopDisputeTobeSubmitted> jobList = this.jdbcTemplate.query(sql.toString(),
						new RowMapper<CoopDisputeTobeSubmitted>() {
							@Override
							public CoopDisputeTobeSubmitted mapRow(ResultSet rs, int rowNum) throws SQLException {

								CoopDisputeTobeSubmitted cbs = new CoopDisputeTobeSubmitted();
								 cbs.setJobId(rs.getString("id"));
								cbs.setVendorName(rs.getString("vendorName"));
								cbs.setVendorId(rs.getString("vendorId"));
								cbs.setUserId(rs.getString("userId"));
								cbs.setIsLookBackOrReRun(rs.getString("isLookBackOrReRun"));

								return cbs;
							}

						}, new Object[] {});

				if (jobList.size() == 0) {
 
						logger.info("Jobs not found.. Bot is stopping");
						System.exit(0);
						
				}
					

					else {
						counter = counter + 1;
						logger.info("Processing Vendor: " + jobList.get(0).getVendorName());

						jdbcTemplate.execute("UPDATE CBCoopDisputeTobeSubmitted SET  disputeSubmittedStatus = 'INPROGRESS' "
								+ " WHERE  id = '" + jobList.get(0).getJobId() + "' ");

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
								jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page= loginStatus.getLeft();
						logger.info("login status: " + loginStatus.getRight() );

						if (loginStatus.getRight()) {

							try {
								
								
							
								String InvoiceIds=FileDownloadInsert.getInvoiceid(page, jobList.get(0).getJobId(),downloadPath ,jobList.get(0).getVendorId(),jobList.get(0).getVendorName());
								logger.info("Invoice Id's in File\n"+InvoiceIds);
								
								if(InvoiceIds.equals(null))
								{
									logger.info("INVOICE IDS NOT FOUND");
									jdbcTemplate
									.execute("UPDATE CBCoopDisputeTobeSubmitted SET  disputeSubmittedStatus = 'ERROR' ) "
											+ " WHERE  id = '" + jobList.get(0).getJobId() + "' ");
									logger.error("----- Updated Status : ERROR -----");
									logger.info("<><><><><><><><><><><><><><><><><><><><><><><><><><><>");
									logger.info("");
								}
								else
								{
									createDispute.SubmitDispute(page, InvoiceIds, jobList.get(0).getVendorId(),jobList.get(0).getJobId(),downloadPath);

									jdbcTemplate.execute(
											"UPDATE CBCoopDisputeTobeSubmitted SET  disputeSubmittedStatus = 'COMPLETED'  "
													+ " WHERE  id = '" + jobList.get(0).getJobId() + "' ");
									logger.info("----- Updated Status : COMPLETED -----");
									logger.info("<><><><><><><><><><><><><><><><><><><><><><><><><><><>");
									logger.info("");
								}
								

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();

								jdbcTemplate
										.execute("UPDATE CBCoopDisputeTobeSubmitted SET  disputeSubmittedStatus = 'ERROR' ) "
												+ " WHERE  id = '" + jobList.get(0).getJobId() + "' ");
								logger.error("----- Updated Status : ERROR -----");
								logger.info("<><><><><><><><><><><><><><><><><><><><><><><><><><><>");
								logger.info("");
							}


						} else {

							jdbcTemplate.execute("UPDATE CBitemizedshortageInvoicesToBeCreated SET  status = 'ERROR'  "
									+ " WHERE  id = '" + jobList.get(0).getJobId() + "' ");

						}

					}

				
				}

	

		}
	}

}
