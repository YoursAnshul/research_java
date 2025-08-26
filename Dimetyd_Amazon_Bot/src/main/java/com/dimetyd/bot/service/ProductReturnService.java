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
import com.dimetyd.bot.model.ShortageJobData;
import com.dimetyd.bot.process.ProductReturnPageStage1;
import com.dimetyd.bot.process.ProductReturnPageStage2;
import com.dimetyd.bot.util.CommonUtil;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class ProductReturnService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	CommonUtil commonobj;

	@Autowired
	ProductReturnPageStage1 stage1;

	@Autowired
	ProductReturnPageStage2 stage2;

	Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Product_Return_Jobs")) {

			logger.info("Product_Return_Dispute_Creation...");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			while (true) {

				logger.info("Checking transcation in database..");
				logger.info("Checking Stage 1 transcation........");
				StringBuilder sql = new StringBuilder();
				sql.append(
						" SELECT cr.id AS id , c.id AS requestId,c.RequestType, c.vendorId, v.vendorName,cr.`retry`,cr.`agreementId`,cr.`startDt`,cr.`endDt`,c.createdBy FROM CBRequest c JOIN `CBAgreementRequestDetails` cr ON (c.id=cr.requestId) "
								+ "JOIN Vendor v ON (v.id = c.vendorId) WHERE cr.`status` = 'PENDING' "
								+ "AND v.`isPaused` = 'N' AND RequestType = 'RETURNS_VALIDATION' ORDER BY v.`master_Crd_Id` LIMIT 1 ");

				List<ShortageJobData> jobList = this.jdbcTemplate.query(sql.toString(),
						new RowMapper<ShortageJobData>() {
							@Override
							public ShortageJobData mapRow(ResultSet rs, int rowNum) throws SQLException {

								ShortageJobData its = new ShortageJobData();
								;
								its.setId(rs.getLong("cr.id"));
								its.setRequestId(rs.getLong("requestId"));
								its.setVendorId(rs.getString("c.vendorId"));
								its.setVendorName(rs.getString("v.vendorName"));
								its.setStartDate(rs.getString("cr.startDt"));
								its.setEndDate(rs.getString("cr.endDt"));
								return its;

							}

						}, new Object[] {});

				if (jobList.size() == 0) {

					logger.info("Checking Stage 2 transcation........");
					StringBuilder sql1 = new StringBuilder();
					sql1.append(
							"SELECT r.id,`returnId`, `markletPlace`, `vendorId`,v.vendorName,`Ukey` FROM CBReturn r JOIN Vendor v ON (r.vendorId = v.id) WHERE STATUS = 'PENDING'  AND v.`isPaused` = 'N'  Order By RAND() LIMIT 1");

					List<ShortageJobData> jobList1 = this.jdbcTemplate.query(sql1.toString(),
							new RowMapper<ShortageJobData>() {
								@Override
								public ShortageJobData mapRow(ResultSet rs, int rowNum) throws SQLException {

									ShortageJobData res = new ShortageJobData();

									res.setId(rs.getLong("r.id"));
									res.setVendorId(rs.getString("vendorId"));
									res.setVendorName(rs.getString("v.vendorName"));
									res.setReturnId(rs.getString("returnId"));
									return res;

								}

							}, new Object[] {});

					if (jobList1.size() == 0) {
						logger.info("Jobs not found.. Bot is stopping");
						System.exit(0);
					}

					else {
						counter = counter + 1;
						logger.info("Processing Vendor: " + jobList1.get(0).getVendorName());

						jdbcTemplate.execute(
								"UPDATE CBReturn SET STATUS='INPROGRESS' WHERE id='" + jobList.get(0).getId() + "'");

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList1.get(0).getVendorId(), page,
								jobList1.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();
						logger.info("login status: " + loginStatus.getRight());

						if (loginStatus.getRight()) {

							try {
								Path downloadPath = Paths.get("C:\\Users\\sphin\\Documents");
								boolean status = stage2.processPage(page, jobList.get(0).getVendorName(), downloadPath,
										jobList.get(0).getVendorId(), jobList.get(0).getReturnId());
								if (status) {
									logger.info("UPDATE CBReturn SET STATUS='COMPLETED' WHERE id='"
											+ jobList.get(0).getId() + "'");
									jdbcTemplate.execute("UPDATE CBReturn SET STATUS='COMPLETED' WHERE id='"
											+ jobList.get(0).getId() + "'");

								} else {
									logger.info("UPDATE CBReturn SET STATUS='ERROR' WHERE id='" + jobList.get(0).getId()
											+ "'");
									jdbcTemplate.execute("UPDATE CBReturn SET STATUS='ERROR' WHERE id='"
											+ jobList.get(0).getId() + "'");
								}

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();

								jdbcTemplate.execute("UPDATE CBReturn SET  status = 'ERROR'  " + " WHERE  id = '"
										+ jobList.get(0).getId() + "' ");
							}

						} else {

							jdbcTemplate.execute("UPDATE CBReturn SET  status = 'ERROR'  " + " WHERE  id = '"
									+ jobList.get(0).getId() + "' ");

						}

					}

				} else {
					counter = counter + 1;
					logger.info("Processing Vendor: " + jobList.get(0).getVendorName());

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
							jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();
					logger.info("login status: " + loginStatus.getRight());

					if (loginStatus.getRight()) {

						try {
							Path downloadPath = Paths.get("C:\\Users\\sphin\\Documents");
							boolean status = stage1.processPage(page, downloadPath, jobList.get(0).getVendorId(),
									jobList.get(0).getRequestId(), jobList.get(0).getVendorName(),
									jobList.get(0).getStartDate(), jobList.get(0).getEndDate());
							if (status) {
								logger.info("UPDATE CBAgreementRequestDetails SET STATUS='COMPLETED' WHERE id='"
										+ jobList.get(0).getId() + "'");
								jdbcTemplate
										.execute("UPDATE CBAgreementRequestDetails SET STATUS='COMPLETED' WHERE id='"
												+ jobList.get(0).getId() + "'");

							} else {
								logger.info("UPDATE CBAgreementRequestDetails SET STATUS='ERROR' WHERE id='"
										+ jobList.get(0).getId() + "'");
								jdbcTemplate.execute("UPDATE CBAgreementRequestDetails SET STATUS='ERROR' WHERE id='"
										+ jobList.get(0).getId() + "'");
							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

							jdbcTemplate.execute(
									"UPDATE CBAgreementRequestDetails SET  status = 'ERROR' , modifiedDate = now() "
											+ " WHERE  id = '" + jobList.get(0).getId() + "' ");
						}

					} else {

						jdbcTemplate.execute(
								"UPDATE CBAgreementRequestDetails SET  status = 'ERROR' , modifiedDate = now() "
										+ " WHERE  id = '" + jobList.get(0).getId() + "' ");

					}

				}

			}

		}
	}

}
