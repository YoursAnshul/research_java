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
import com.dimetyd.bot.model.CBOperationalPerformanceJobData;
import com.dimetyd.bot.process.OperationalChargebackProcess;
import com.dimetyd.bot.util.CommonUtil;
import com.dimetyd.bot.util.LoginVendorCentral;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import jakarta.annotation.PostConstruct;

@Service
public class OperationalChargebackJobService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	OperationalChargebackProcess ocProcess;
	@Autowired
	CommonUtil util;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Operational_Chargeback")) {

			logger.info("Operational_Chargeback Bot Started...");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			
			String updateInprogressQuery="UPDATE `CBRequestOperationalChargeBack` SET STATUS='PENDING'"
					+ " WHERE STATUS='INPROGRESS' AND DATE(createdDate)=CURDATE()";
			
			
			int UpdatedInprogressRows=jdbcTemplate.update(updateInprogressQuery);
			logger.info("Transactions Inprogress Updated to PENDING Rows : "+UpdatedInprogressRows);

			while (true) {
				String sql = "SELECT cb.id, cb.vendorId, cb.vendorName, cb.endDate AS inEndDate, "
						+ "cb.startDate AS inStartDate, 'Monthly' AS reportingPeriod, cb.status "
						+ "FROM CBRequestOperationalChargeBack cb JOIN Vendor vc ON vc.id = cb.vendorId  "
						+ "WHERE cb.STATUS = 'PENDING'  AND vc.isVendorMenuAccess = 1 AND vc.isPaused = 'N' ORDER BY vc.master_Crd_Id,cb.vendorName  DESC LIMIT 1";

				List<CBOperationalPerformanceJobData> jobList = this.jdbcTemplate.query(sql,
						new RowMapper<CBOperationalPerformanceJobData>() {
							@Override
							public CBOperationalPerformanceJobData mapRow(ResultSet rs, int rowNum)
									throws SQLException {
								CBOperationalPerformanceJobData res = new CBOperationalPerformanceJobData();
								res.setId(rs.getString("id"));
								res.setVendorId(rs.getString("vendorId"));
								res.setVendorName(rs.getString("vendorName"));
								res.setInEndDate(rs.getString("inEndDate"));
								res.setInStartDate(rs.getString("inStartDate"));
								res.setReportingPeriod(rs.getString("reportingPeriod"));
								res.setStatus(rs.getString("status"));
								return res;
							}
						});

				if (jobList.isEmpty()) {
					logger.info("JOBS ENDED");
					System.exit(0);

				} else {
					counter = counter + 1;
					String vendorId=jobList.get(0).getVendorId();
					String startDate=jobList.get(0).getInStartDate();
					String endDate=jobList.get(0).getInEndDate();
					logger.info("  Vendor Name: " + jobList.get(0).getVendorName() + " vendorId: "
							+ vendorId);

					jdbcTemplate
							.execute(" UPDATE `CBRequestOperationalChargeBack` SET `Status` = 'INPROGRESS' WHERE id = '"
									+ jobList.get(0).getId() + "'");

					logger.info("Process start..");

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(vendorId, page,
							jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();
					logger.info("login status: " + loginStatus.getRight());
					if (loginStatus.getRight()) {
						feedbackpage(page);
						util.reminemepopup(page);
						
						int uiRows=ocProcess.OperationalChargebackProcessNavigate(page, jobList.get(0).getId(),vendorId,jobList.get(0).getVendorName(),
								startDate, endDate);

						if(uiRows>0)
						{
							
							int dbRows = getOperationalChargebackDataCount(vendorId,startDate,endDate);
							logger.info("ROWS CHECK ---- UI Rows : "+uiRows+" DB Rows : "+dbRows);
							if(uiRows==dbRows)
							{
								String deleteOldOCdata="DELETE FROM CBOperationalPerformance WHERE vendorId='"+vendorId+"'";

								String InsertLatestData="INSERT IGNORE INTO CBOperationalPerformance (`vendorId`,`issueID`,`financialCharge`,`quantity`,`vendorCode`,`issueType`,`fulfillmentCenter`,`createdDate`,`disputeBy`,`chargeInvoice`,`status`,`purchaseOrder`,`subtypenonCompliance`,`ASIN`,`creationDate`,`startDate`,`endDate`,`uniqueKey`)"+
										"SELECT `vendorId`,`issueID`,`financialCharge`,`quantity`,`vendorCode`,`issueType`,`fulfillmentCenter`,`createdDate`,`disputeBy`,`chargeInvoice`,`status`,`purchaseOrder`,`subtypenonCompliance`,`ASIN`,`creationDate`,`startDate`,`endDate`,`uniqueKey`"+
										"FROM CBOperationalPerformance_import WHERE vendorId='"+vendorId+"' and startDate BETWEEN '"+startDate+"' AND '"+endDate+"'";
								
								logger.info(deleteOldOCdata);
								jdbcTemplate.execute(deleteOldOCdata);
								
								logger.info(InsertLatestData);
								int LatestInsertedRows=jdbcTemplate.update(InsertLatestData);
								logger.info("Latest Inserted Rows Count "+LatestInsertedRows);
								jdbcTemplate.execute(
										" UPDATE `CBRequestOperationalChargeBack` SET `Status` = 'COMPLETED',`QAStatus`='All OK', comment ='UI Rows : "+uiRows+" DB Rows : "+dbRows+"' WHERE id = '"
												+ jobList.get(0).getId() + "'");
							}
							else
							{

								logger.info(" UPDATE `CBRequestOperationalChargeBack` SET `Status` = 'COMPLETED',`QAStatus`='Need To Check', comment ='UI Rows : "+uiRows+" DB Rows : "+dbRows+"' WHERE id = '"
												+ jobList.get(0).getId() + "'");
								jdbcTemplate.execute(
										" UPDATE `CBRequestOperationalChargeBack` SET `Status` = 'COMPLETED',`QAStatus`='Need To Check', comment ='UI Rows : "+uiRows+" DB Rows : "+dbRows+"' WHERE id = '"
												+ jobList.get(0).getId() + "'");
							}
						
						}
						else if(uiRows==0)
						{logger.info("######## ROWS : "+uiRows+ " No Need to Insert Data ########");
							jdbcTemplate.execute(
									" UPDATE `CBRequestOperationalChargeBack` SET `Status` = 'COMPLETED',`QAStatus`='No Data Found' WHERE id = '"
											+ jobList.get(0).getId() + "'");
						}
						else
						{
							jdbcTemplate
							.execute(" UPDATE `CBRequestOperationalChargeBack` SET `Status` = 'ERROR',`QAStatus`='Issue Found' WHERE id = '"
									+ jobList.get(0).getId() + "'");
						}

					}

					else {
						jdbcTemplate
								.execute(" UPDATE `CBRequestOperationalChargeBack` SET `Status` = 'ERROR',`QAStatus`='Login or Switch Vendor Issue Found' WHERE id = '"
										+ jobList.get(0).getId() + "'");
					}
				}

			}

		}

	}
	public void feedbackpage(Page page)
	{
		//feedback poup handled
		Locator feedbackpagePopup = page.locator("//div[@id='vibes-modal-header']//h3[contains(text(), 'We love feedback!')]");

		if (feedbackpagePopup.isVisible()) {
		    logger.info("Feedback is visible");
		    page.locator("kat-button.vibes-form__button button:not([type='submit'])").click();
		} else {
		    logger.info("Feedback not visible. Refreshing page...");
		    page.reload();
		    page.waitForLoadState(LoadState.DOMCONTENTLOADED); // or NETWORKIDLE
		}

	}
	 private int getOperationalChargebackDataCount(String vendorId,String startDate,String endDate) {
	        String sql = "SELECT COUNT(*) FROM CBOperationalPerformance_import WHERE vendorId = ?  AND DATE(`creationDate`)  BETWEEN ? AND ?";
	        return jdbcTemplate.queryForObject(sql, Integer.class, vendorId,startDate,endDate);
	    }
}
