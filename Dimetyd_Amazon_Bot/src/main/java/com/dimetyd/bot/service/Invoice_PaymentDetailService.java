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
import com.dimetyd.bot.process.PaymentDetails;
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
public class Invoice_PaymentDetailService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	PaymentDetails paymentDetailsPageObj;
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

		if (GlobalSession.getGlobalSession().getName().equals("Invioce_paymentDetails")) {

			logger.info("PaymentDetails bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));
			
			// **Step 1: Delete old data from financialReport_Input**
			jdbcTemplate.execute("DELETE FROM paymentDetails_Input");
			logger.info("Old paymentDetails_Input data deleted.");

			// **Step 2: Insert new data into financialReport_Input**
			int InsertedRows = jdbcTemplate
					.update("INSERT INTO `paymentDetails_Input`(`vendorName`, vendorId ,`year`,startDate,endDate,`status`)\r\n"
							+ "SELECT `vendorName`,b.vendorId,'2025' AS YEAR,DATE_ADD(NOW(), INTERVAL -90 DAY)  AS  StartDateFulfillment_Month,NOW() AS EndDateFulfillment_Month, 'Pending' AS STATUS FROM `Vendor` a JOIN `Client` b ON (a.id=b.vendorId AND b.`isInvoice`=1) AND b.vendorId NOT IN('VN11111111111111','VN11111111111112')\r\n"
							+ "");
			logger.info("Inserted new paymentDetails_Input data : " + InsertedRows);

			while (true) {
				try {

					StringBuilder sql = new StringBuilder();
					sql.append(
							"SELECT `id`,`vendorId`,`vendorName`,`startDate`,`endDate` FROM `paymentDetails_Input` WHERE STATUS='PENDING' ORDER BY RAND() LIMIT 1");

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
						counter=counter+1;
						Long id = result.get(0).getId();
						String vendorName = result.get(0).getVendorName().trim();
						String vendorId = result.get(0).getVendorId().trim();
						String startDate = result.get(0).getStartDate();
						String endDate = result.get(0).getEndDate();

						logger.info("UPDATE paymentDetails_Input SET STATUS='INPROGRESS' WHERE id='" + id + "'");
						jdbcTemplate.execute("UPDATE paymentDetails_Input SET STATUS='INPROGRESS' WHERE id='" + id + "'");

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(result.get(0).getVendorId(), page,
								result.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();

						if (loginStatus.getRight()) {

							Path downloadPath = Paths.get("C:\\java codes\\Forecast_DownloadFiles");;
							boolean status = paymentDetailsPageObj.processPage(page, vendorId, vendorName,
									startDate, endDate, downloadPath);
							if (status) {
								logger.info("UPDATE paymentDetails_Input SET STATUS='COMPLETED' WHERE id='" + id + "'");
								jdbcTemplate
										.execute("UPDATE paymentDetails_Input SET STATUS='COMPLETED' WHERE id='" + id + "'");
							} else {
								logger.info("UPDATE paymentDetails_Input SET STATUS='ERROR' WHERE id='" + id + "'");
								jdbcTemplate.execute("UPDATE paymentDetails_Input SET STATUS='ERROR' WHERE id='" + id + "'");
							}

						} else {
							logger.info("UPDATE paymentDetails_Input SET STATUS='ERROR' WHERE id='" + id + "'");
							jdbcTemplate.execute("UPDATE paymentDetails_Input SET STATUS='ERROR' WHERE id='" + id + "'");
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
