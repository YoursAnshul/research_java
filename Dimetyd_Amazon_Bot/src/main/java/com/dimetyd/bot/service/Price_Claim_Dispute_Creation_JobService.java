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
import com.dimetyd.bot.process.Stage_1_Price_Claim_Download_Invoices;
import com.dimetyd.bot.process.Stage_2_Price_Claim_Submit_Dispute;

import com.dimetyd.bot.util.LoginVendorCentral;
import com.dimetyd.bot.model.CBitemizedshortageInvoicesToBeCreated;
import com.dimetyd.bot.model.ItemizedShortageStage1;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class Price_Claim_Dispute_Creation_JobService {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	Stage_1_Price_Claim_Download_Invoices stage1;

	@Autowired
	Stage_2_Price_Claim_Submit_Dispute stage2;

	Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;

	private Logger logger = LoggerFactory.getLogger(getClass());

	
	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Price_Claim_Dispute_Creation")) {

			logger.info("Price_Claim Dispute Creation Bot Started...");
			


			/*Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
			context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 800)
					.setIgnoreHTTPSErrors(true).setJavaScriptEnabled(true).setLocale("en-US"));
			

			context.addInitScript("window.chrome = { runtime: {} };");

			page = context.newPage();
			logger.info("Page Started...");
			*/
		

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			//context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 800).setIgnoreHTTPSErrors(true).setJavaScriptEnabled(true).setLocale("en-US"));


		
			//page = context.newPage();
	

			while (true) {

				logger.info("Checking transcation in database..");
				logger.info("Checking Stage 1 transcation........");
				StringBuilder sql = new StringBuilder();
				sql.append(
						" SELECT a.id AS jobId,`vendorId`,v.`vendorName`,a.`status` FROM `CBPriceClaimRequest` a JOIN Vendor v ON (a.vendorId=v.id) \r\n"
						+ "WHERE \r\n"
						+ "STATUS = 'PENDING' AND \r\n"
						+ "v.`isPaused`='N'  ORDER BY v.`master_Crd_Id` LIMIT 1");

				List<ItemizedShortageStage1> jobList = this.jdbcTemplate.query(sql.toString(),
						new RowMapper<ItemizedShortageStage1>() {
							@Override
							public ItemizedShortageStage1 mapRow(ResultSet rs, int rowNum) throws SQLException {

								ItemizedShortageStage1 its = new ItemizedShortageStage1();
								its.setVendorname(rs.getString("vendorName"));
								its.setVendorId(rs.getString("vendorId"));
								its.setJobId(rs.getInt("jobId"));

								return its;
							}

						}, new Object[] {});

				if (jobList.size() == 0) {

					logger.info("Checking Stage 2 transcation........");
					StringBuilder sql1 = new StringBuilder();
					sql1.append(
							"SELECT a.id AS jobId,v.vendorName,a.`vendorId`,a.`qtyVarianceAmount`,a.`invoiceNumber`,a.invoiceAmount,a.`invoiceDate`,a.`payee`,a.`paymentDueDate`"
									+ " FROM `CBPriceClaimRequestDetails` a JOIN Vendor v ON (a.vendorId=v.id) \r\n"
									+ "WHERE STATUS = 'PENDING'  AND  v.isPaused='N' ORDER BY v.master_Crd_Id LIMIT 1 ");

					List<CBitemizedshortageInvoicesToBeCreated> jobList1 = this.jdbcTemplate.query(sql1.toString(),
							new RowMapper<CBitemizedshortageInvoicesToBeCreated>() {
								@Override
								public CBitemizedshortageInvoicesToBeCreated mapRow(ResultSet rs1, int rowNum)
										throws SQLException {

									CBitemizedshortageInvoicesToBeCreated intc = new CBitemizedshortageInvoicesToBeCreated();
									intc.setVendorname(rs1.getString("vendorName"));
									intc.setVendorId(rs1.getString("vendorId"));
									intc.setJobId(rs1.getInt("jobId"));
									intc.setQtyVarianceAmount(rs1.getString("qtyVarianceAmount"));
									intc.setInvoiceNumber(rs1.getString("invoiceNumber"));
									intc.setInvoiceAmount(rs1.getString("invoiceAmount"));
									intc.setInvoiceDate(rs1.getString("invoiceDate"));
									intc.setPaymentDueDate(rs1.getString("paymentDueDate"));
									intc.setPayee(rs1.getString("payee"));

									return intc;
								}

							}, new Object[] {});

					if (jobList1.size() == 0) {
						logger.info("No job found...Waiting for new job");
						System.exit(0);
					}

					else {
						counter = counter + 1;
						logger.info("Processing Vendor: " + jobList1.get(0).getVendorname());

						jdbcTemplate.execute("UPDATE CBPriceClaimRequestDetails SET  status = 'INPROGRESS' "
								+ " WHERE  id = '" + jobList1.get(0).getJobId() + "' ");

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList1.get(0).getVendorId(), page,
								jobList1.get(0).getVendorname(), loginHashMap, counter, browser, context);
						page= loginStatus.getLeft();
						logger.info("login status: " + loginStatus.getRight() );

						if (loginStatus.getRight()) {

							try {

								String disputeStatus = stage2.SubmitDispute(page, jobList1.get(0).getInvoiceNumber(),
										jobList1.get(0).getVendorId(), jobList1.get(0).getVendorname(),jobList1.get(0).getQtyVarianceAmountl(),
										jobList1.get(0).getInvoiceAmount(),jobList1.get(0).getInvoiceDate(),jobList1.get(0).getPaymentDueDate(),jobList1.get(0).getPayee());

								jdbcTemplate.execute("UPDATE CBPriceClaimRequestDetails SET  status = '"
										+ disputeStatus + "' WHERE  id = '" + jobList1.get(0).getJobId() + "'");

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();

								jdbcTemplate.execute("UPDATE CBPriceClaimRequestDetails SET  status = 'ERROR'  "
										+ " WHERE  id = '" + jobList1.get(0).getJobId() + "' ");
							}

						} else {

							jdbcTemplate.execute("UPDATE CBPriceClaimRequestDetails SET  status = 'ERROR'  "
									+ " WHERE  id = '" + jobList1.get(0).getJobId() + "' ");

						}

					}

					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					counter = counter + 1;
					logger.info("Processing Vendor: " + jobList.get(0).getVendorname());

					jdbcTemplate
							.execute("UPDATE CBPriceClaimRequest SET  status = 'INPROGRESS' , modifiedDate = now() "
									+ " WHERE  id = '" + jobList.get(0).getJobId() + "' ");

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
							jobList.get(0).getVendorname(), loginHashMap, counter, browser, context);
					page= loginStatus.getLeft();
					logger.info("login status: " + loginStatus.getRight() );

					if (loginStatus.getRight()) {

						try {
							stage1.downloadInvoice(page, jobList.get(0).getVendorId());

							jdbcTemplate.execute(
									"UPDATE CBPriceClaimRequest SET  status = 'COMPLETED' , modifiedDate = now() "
											+ " WHERE  id = '" + jobList.get(0).getJobId() + "' ");

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

							jdbcTemplate
									.execute("UPDATE CBPriceClaimRequest SET  status = 'ERROR' , modifiedDate = now() "
											+ " WHERE  id = '" + jobList.get(0).getJobId() + "' ");
						}

					} else {

						jdbcTemplate.execute("UPDATE CBPriceClaimRequest SET  status = 'ERROR' , modifiedDate = now() "
								+ " WHERE  id = '" + jobList.get(0).getJobId() + "' ");

					}

				}

			}

		

		}
	}
}
