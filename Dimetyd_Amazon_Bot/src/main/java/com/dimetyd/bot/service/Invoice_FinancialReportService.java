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
import com.dimetyd.bot.model.PODetailsFDReportsTransactions;
import com.dimetyd.bot.process.FDReportDownload;
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
public class Invoice_FinancialReportService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	FDReportDownload FinancialDetailsPageObj;
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

		if (GlobalSession.getGlobalSession().getName().equals("Invioce_FinancialReport")) {

			logger.info("Invioce_FinancialReport bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			while (true) {
				try {

					StringBuilder sql = new StringBuilder();
					sql.append(
							"SELECT `id`,`vendorId`,`vendorName`,`startDate`,`endDate` FROM `financialReport_Input` WHERE STATUS='PENDING' ORDER BY RAND() LIMIT 1");

					List<PODetailsFDReportsTransactions> result = this.jdbcTemplate.query(sql.toString(),
							new RowMapper<PODetailsFDReportsTransactions>() {
								public PODetailsFDReportsTransactions mapRow(ResultSet rs, int rowNum) throws SQLException {
									PODetailsFDReportsTransactions jobData = new PODetailsFDReportsTransactions();
									jobData.setId(rs.getString("id"));
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
						String id = result.get(0).getId();
						String vendorName = result.get(0).getVendorName().trim();
						String vendorId = result.get(0).getVendorId().trim();
						String startDate = result.get(0).getStartDate();
						String endDate = result.get(0).getEndDate();

						logger.info("UPDATE financialReport_Input SET STATUS='INPROGRESS' WHERE id='" + id + "'");
						jdbcTemplate.execute("UPDATE financialReport_Input SET STATUS='INPROGRESS' WHERE id='" + id + "'");

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(result.get(0).getVendorId(), page,
								result.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();

						if (loginStatus.getRight()) {

							Path downloadPath = Paths.get("C:\\java codes\\Forecast_DownloadFiles");
							boolean status = FinancialDetailsPageObj.navigateToPurchaseOrders(page,startDate,endDate,vendorId, vendorName,
									 downloadPath.toString(),id);
							if (status) {
								logger.info("UPDATE financialReport_Input SET STATUS='COMPLETED' WHERE id='" + id + "'");
								jdbcTemplate
										.execute("UPDATE financialReport_Input SET STATUS='COMPLETED' WHERE id='" + id + "'");
							} else {
								logger.info("UPDATE financialReport_Input SET STATUS='ERROR' WHERE id='" + id + "'");
								jdbcTemplate.execute("UPDATE financialReport_Input SET STATUS='ERROR' WHERE id='" + id + "'");
							}

						} else {
							logger.info("UPDATE financialReport_Input SET STATUS='ERROR' WHERE id='" + id + "'");
							jdbcTemplate.execute("UPDATE financialReport_Input SET STATUS='ERROR' WHERE id='" + id + "'");
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
