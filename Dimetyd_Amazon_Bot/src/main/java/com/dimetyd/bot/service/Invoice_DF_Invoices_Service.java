package com.dimetyd.bot.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
import com.dimetyd.bot.process.Invoice_DFinvoiceDetails;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.dimetyd.bot.util.SwitchVCAccount;
import com.dimetyd.bot.util.VCLogin;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class Invoice_DF_Invoices_Service {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	Invoice_DFinvoiceDetails directFullfillmentOrdersPageObj;
	@Autowired
	SwitchVCAccount swAccount;
	@Autowired
	VCLogin VCLogin;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Invoice_DfInvoice")) {

			logger.info("Invoice_DfInvoice bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));
			// **Step 1: Delete old data from financialReport_Input**
			jdbcTemplate.execute("DELETE FROM DFInvoice_Input");
			logger.info("Old DFInvoice_Input data deleted.");

			// **Step 2: Insert new data into financialReport_Input**
			int InsertedRows = jdbcTemplate
					.update("INSERT INTO `DFInvoice_Input` (vendorName, vendorId, `startDate`, `endDate`, `year`, `month`, `status`)"
							+ " SELECT a.vendorName, b.vendorId, DATE_FORMAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH), '%Y-%m-%d') AS `startDate`, DATE_FORMAT(LAST_DAY(DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH)), '%Y-%m-%d') AS `endDate`, YEAR(DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH)) AS `year`, MONTH(DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH)) AS `month`, 'PENDING' AS `status` FROM `Vendor` a JOIN `Client` b ON a.id = b.vendorId AND b.`isASINProfitability` = 1 AND a.`isPaused`='N' WHERE b.vendorId NOT IN ('VN11111111111111', 'VN11111111111112');");

			
			logger.info("Inserted new DFInvoice_Input data : " + InsertedRows);


			while (true) {
				try {
					counter = counter+1;

					StringBuilder sql = new StringBuilder();
					sql.append(
							"SELECT `id`,`vendorId`,`vendorName`,`startDate`,`endDate` FROM `DFInvoice_Input` WHERE STATUS='PENDING' ORDER BY RAND() LIMIT 1");

					List<ShortageJobData> result = this.jdbcTemplate.query(sql.toString(),
							new RowMapper<ShortageJobData>() {
								public ShortageJobData mapRow(ResultSet rs, int rowNum) throws SQLException {
									ShortageJobData jobData = new ShortageJobData();
									jobData.setId(rs.getLong("id"));
									jobData.setVendorName(rs.getString("vendorName"));
									jobData.setVendorId(rs.getString("vendorId"));
									jobData.setStartDate(rs.getString("startDate"));
									jobData.setEndDate(rs.getString("endDate"));

									return jobData;
								}
							}, new Object[] {});
					if (result.size() == 0) {
						logger.info("NO DATA FOUND");

						System.exit(0);

					}
					try {
						Long id = result.get(0).getId();
						String vendorName = result.get(0).getVendorName().trim();
						String vendorId = result.get(0).getVendorId().trim();
						String startDate = result.get(0).getStartDate();
						String endDate = result.get(0).getEndDate();

						logger.info("UPDATE DFInvoice_Input SET STATUS='INPROGRESS' WHERE id='" + id + "'");
						jdbcTemplate.execute("UPDATE DFInvoice_Input SET STATUS='INPROGRESS' WHERE id='" + id + "'");

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(result.get(0).getVendorId(), page,
								result.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();

						if (loginStatus.getRight()) 
						{

						// Path downloadPath = null;
							Path downloadPath = Paths.get("C/Playwright File/OPERATION_Files");
							boolean status = directFullfillmentOrdersPageObj.processPage(page, vendorId, vendorName,
									startDate, endDate, downloadPath);
							if (status) {
								logger.info("UPDATE DFInvoice_Input SET STATUS='COMPLETED' WHERE id='" + id + "'");
								jdbcTemplate
										.execute("UPDATE DFInvoice_Input SET STATUS='COMPLETED' WHERE id='" + id + "'");
							} else {
								logger.info("UPDATE DFInvoice_Input SET STATUS='ERROR' WHERE id='" + id + "'");
								jdbcTemplate.execute("UPDATE DFInvoice_Input SET STATUS='ERROR' WHERE id='" + id + "'");
							}

						} else {
							logger.info("UPDATE DFInvoice_Input SET STATUS='ERROR' WHERE id='" + id + "'");
							jdbcTemplate.execute("UPDATE DFInvoice_Input SET STATUS='ERROR' WHERE id='" + id + "'");
						}

					} catch (IndexOutOfBoundsException e) {
						e.printStackTrace();
					}

				} catch (Exception e) {
					e.printStackTrace();
					// eventRecorder.navigate().to("https://vendorcentral.amazon.com/home/vc");

				}

			}

		}
	}

}
