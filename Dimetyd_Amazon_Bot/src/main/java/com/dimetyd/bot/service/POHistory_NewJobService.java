package com.dimetyd.bot.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.CBPOHistoryInput;
import com.dimetyd.bot.process.PoHistoryPage;
import com.dimetyd.bot.process.PoHistory_NewPage;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class POHistory_NewJobService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	PoHistory_NewPage popage1;

	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("PO_History")) {

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

			BrowserContext context = null; // Create context without setting download path
			Page page = null;
			while (true) {
				while (true) {
					try {

						StringBuilder sql = new StringBuilder();
						sql.append(
								"SELECT a.id,v.vendorName,a.vendorID,a.reportingperiod,a.inputClick,a.year,a.startDate, a.endDate FROM CBPOHistoryInput a INNER JOIN Vendor v ON (a.vendorId=v.id) WHERE a.STATUS='PENDING' AND v.`isPaused` = 'N' ORDER BY RAND() LIMIT 1");
						List<CBPOHistoryInput> result = this.jdbcTemplate.query(sql.toString(),
								new RowMapper<CBPOHistoryInput>() {
									@Override
									public CBPOHistoryInput mapRow(ResultSet rs, int rowNum) throws SQLException {
										CBPOHistoryInput poHistoryInput = new CBPOHistoryInput();
										poHistoryInput.setId(rs.getLong("id"));
										poHistoryInput.setVendorName(rs.getString("vendorName"));
										poHistoryInput.setVendorID(rs.getString("vendorID"));
										poHistoryInput.setReportingPeriod(rs.getString("reportingperiod"));
										poHistoryInput.setInputClick(rs.getString("inputClick"));
										poHistoryInput.setYear(rs.getInt("year"));
										poHistoryInput.setStartDate(rs.getDate("startDate"));
										poHistoryInput.setEndDate(rs.getDate("endDate"));
										// poHistoryInput.setBusinessUnit(rs.getString("businessUnit"));
										return poHistoryInput;
									}
								}, new Object[] {});
						if (result.size() == 0) {

							logger.info("No jobs Found in Stage 1");

							System.exit(0);

						} else {
							counter = counter + 1;

							String vendorName = result.get(0).getVendorName();
							long id = result.get(0).getId();
							String vendorId = result.get(0).getVendorID();
							String reportingPeriod = result.get(0).getReportingPeriod();
							String inputClick = result.get(0).getInputClick();
							int year = result.get(0).getYear();
							logger.info("Year : " + year);
							Date startDate = result.get(0).getStartDate();
							Date endDate = result.get(0).getEndDate();
							String date = startDate + "-" + endDate;

							// checkLastLoginTime(vendorId);
							logger.info("Update CBPOHistoryInput Set STATUS='INPROGRESS' where id='" + id + "'");
							jdbcTemplate
									.execute("Update CBPOHistoryInput Set STATUS='INPROGRESS' where id='" + id + "'");

							// switchVendor(vendorName);

							Pair<Page, Boolean> loginStatus = loginObj.loginProcess(result.get(0).getVendorID(), page,
									result.get(0).getVendorName(), loginHashMap, counter, browser, context);
							page = loginStatus.getLeft();
							logger.info("login status: " + loginStatus.getRight());
							if (loginStatus.getRight()) {

								Path downloadPath = Paths.get("C:\\PalyWrightFile");
								boolean status = popage1.processPage(page, vendorName, reportingPeriod, year, id,
										vendorId, inputClick, downloadPath, startDate, endDate);
								if (status) {
									jdbcTemplate.execute(
											"INSERT IGNORE INTO `CBPOData`(`Order_Date`,`PO`,`month`,`year`,`Ukey`,`windowType`,vendorId) "
													+ "SELECT `poOrderDate`,`PO`,MONTH(`poOrderDate`),YEAR(`poOrderDate`),CONCAT(`PO`, '"
													+ vendorId + "'),`windowType`,`vendorId` "
													+ "FROM `CBPOHistoryData` WHERE YEAR(`poOrderDate`)=" + year + " "
													+ "AND MONTH(`poOrderDate`)=" + convertMonthToInt(inputClick) + " "
													+ "AND `vendorId`='" + vendorId + "'");
									
									logger.info("Update CBPOHistoryInput Set STATUS='COMPLETED',`fileDownload`='Yes' where id='" + id + "'");
									jdbcTemplate.execute(
											"Update CBPOHistoryInput Set STATUS='COMPLETED',`fileDownload`='Yes' where id='" + id + "'");
								} else {
									logger.info("Update CBPOHistoryInput Set STATUS='ERROR' ,`fileDownload`='No'  where id='" + id + "'");
									jdbcTemplate.execute(
											"Update CBPOHistoryInput Set STATUS='ERROR',`fileDownload`='No' where id='" + id + "'");
								}

							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						// eventRecorder.navigate().to("https://vendorcentral.amazon.com/home/vc");

					}

				}

			}

		}
	}

	public static int convertMonthToInt(String monthName) {
		// Normalize input (case-insensitive match)
		monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase();
		for (Month month : Month.values()) {
			if (month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).equals(monthName)
					|| month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).equals(monthName)) {
				return month.getValue(); // January = 1
			}
		}
		throw new IllegalArgumentException("Invalid month name: " + monthName);
	}

}
