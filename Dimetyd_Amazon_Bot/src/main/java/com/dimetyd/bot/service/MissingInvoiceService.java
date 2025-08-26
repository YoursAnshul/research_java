package com.dimetyd.bot.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.dimetyd.bot.model.CBMissingInvPOInvoice;
import com.dimetyd.bot.model.CBMissingInvoiceOutput;
import com.dimetyd.bot.model.CBMissingInvoicePoRequest;
import com.dimetyd.bot.model.CBPOJobData;
import com.dimetyd.bot.model.CbMissingInvoiceRequest;
import com.dimetyd.bot.model.ProcessStatus;
import com.dimetyd.bot.process.DimeTydStage2PageNewUi;
import com.dimetyd.bot.process.DimeTydStage3PageNewUi;
import com.dimetyd.bot.process.EmailService;
import com.dimetyd.bot.process.InvoiceCreationPage;
import com.dimetyd.bot.process.InvoiceDataUpdatePage;
import com.dimetyd.bot.process.MissingInvoicePoPage;
import com.dimetyd.bot.process.MissingInvoicePoPage_New;
import com.dimetyd.bot.process.PaymentDataUpdationProcess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.dimetyd.bot.util.PO_CommonUtil;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import jakarta.annotation.PostConstruct;

@Service
public class MissingInvoiceService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	private MissingInvoicePoPage_New poPage;

	@Autowired
	private MissingInvoicePoPage poPageold;
	@Autowired
	private DimeTydStage2PageNewUi dimeTydStage2PageNewUi;
	@Autowired
	private DimeTydStage3PageNewUi dimeTydStage3PageNewUi;
	@Autowired
	private InvoiceCreationPage invoiceCreationPage;
	@Autowired
	private InvoiceDataUpdatePage invoiceDataUpdatePage;
	@Autowired
	private PaymentDataUpdationProcess paymentDataUpdationProcess;
	@Autowired
	private EmailService emailService;
	@Autowired
	private PO_CommonUtil pO_CommonUtil;

	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("IR_Jobs")) {

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

			BrowserContext context = null; // Create context without setting download path
			Page page = null;
			while (true) {
				while (true) {
					try {
						StringBuilder sql = new StringBuilder();
						sql.append(
								"SELECT a.id,b.id,v.vendorName,b.`month`,b.`year`,b.`reportingPeriod`,a.`vendorId`,b.`poStartdate`,b.`poEndDate` ,c.`businessUnit` \r\n"
										+ "FROM `CbMissingInvoiceRequest` a \r\n"
										+ "JOIN CbMissingInvoiceRequestDetails b ON (a.id=b.`CbMissingInvoiceRequestId`) \r\n"
										+ "JOIN Vendor v ON (a.vendorId=v.id) \r\n"
										+ "JOIN `Client` c ON (c.vendorId=v.id) \r\n"
										+ "WHERE b.`jobStatus`= 'PENDING'  AND v.`isPaused` = 'N' AND a.`jobType` IS NULL \r\n"
										+ "ORDER BY v.`jobPriority`, RAND() LIMIT 1\r\n");

						List<CBPOJobData> result = this.jdbcTemplate.query(sql.toString(),
								new RowMapper<CBPOJobData>() {
									@Override
									public CBPOJobData mapRow(ResultSet rs, int rowNum) throws SQLException {
										CBPOJobData jobData = new CBPOJobData();
										jobData.setId(rs.getLong("b.id"));
										jobData.setRequestId(rs.getLong("a.id"));
										jobData.setVendorName(rs.getString("vendorName"));
										jobData.setMonth(rs.getString("month"));
										jobData.setYear(rs.getInt("year"));
										jobData.setReportingPeriod(rs.getString("reportingPeriod"));
										jobData.setVendorId(rs.getString("vendorId"));
										jobData.setBusinessUnit(rs.getString("businessUnit"));
										jobData.setPoStartDate(rs.getDate("poStartdate"));
										jobData.setPoEndDate(rs.getDate("poEndDate"));
										return jobData;
									}
								}, new Object[] {});
						if (result.size() == 0) {
							logger.info("No jobs Found in MissingPo Satge1");
							Thread.sleep(1500);
							break;
						} else {
							counter = counter + 1;
							String vendorName = result.get(0).getVendorName();
							Long id = result.get(0).getId();
							Long requestId = result.get(0).getRequestId();
							String vendorId = result.get(0).getVendorId();
							int year = result.get(0).getYear();
							String month = result.get(0).getMonth();
							Date startDate = result.get(0).getPoStartDate();
							Date endDate = result.get(0).getPoEndDate();
							jdbcTemplate.execute(
									"UPDATE CbMissingInvoiceRequest Set modifiedDate=NOW(),jobStage='Collecting OrderHistoryData' where vendorId='"
											+ vendorId + "'");
							jdbcTemplate.execute(
									"UPDATE CbMissingInvoiceRequest SET jobStatus='INPROGRESS',jobStartDate=NOW() where id='"
											+ requestId + "'");
							logger.info(
									"UPDATE CbMissingInvoiceRequestDetails SET jobStatus='INPROGRESS',jobStartDate=NOW() where id='"
											+ id + "'");
							jdbcTemplate.execute(
									"UPDATE CbMissingInvoiceRequestDetails SET jobStatus='INPROGRESS',jobStartDate=NOW() where id='"
											+ id + "'");

							Pair<Page, Boolean> loginStatus = loginObj.loginProcess(result.get(0).getVendorId(), page,
									result.get(0).getVendorName(), loginHashMap, counter, browser, context);
							page = loginStatus.getLeft();
							logger.info("login status: " + loginStatus.getRight());
							if (loginStatus.getRight()) {

								Path downloadPath = Paths.get("C:\\PalyWrightFile");
								boolean status = poPage.processPage(page, vendorName, year, id, vendorId, month,
										downloadPath, requestId, startDate, endDate);
								if (status) {
									/*
									 * jdbcTemplate.execute(
									 * "INSERT IGNORE INTO `CBMissingPOData`(`Order_Date`,`PO`,`month`,`year`,`Ukey`,`windowType`,vendorId) "
									 * +
									 * "SELECT `poOrderDate`,`PO`,MONTH(`poOrderDate`),YEAR(`poOrderDate`),CONCAT(`PO`, '"
									 * + vendorId + "'),`windowType`,`vendorId` " +
									 * "FROM `CBMissingInvoiePODetails` WHERE YEAR(`poOrderDate`)=" + year + " " +
									 * "AND MONTH(`poOrderDate`)=" +PO_CommonUtil.convertMonthToInt(month) + " " +
									 * "AND `vendorId`='" + vendorId + "'");
									 */

									jdbcTemplate.execute(
											"INSERT IGNORE INTO `CBMissingPOData`(`Order_Date`,`PO`,`month`,`year`,`Ukey`,vendorId) "
													+ "SELECT `poOrderDate`,`PO`,MONTH(`poOrderDate`),YEAR(`poOrderDate`),CONCAT(`PO`, '"
													+ vendorId + "'),`vendorId` "
													+ "FROM `CBMissingInvoiePODetails` WHERE YEAR(`poOrderDate`)="
													+ year + " " + "AND MONTH(`poOrderDate`)="
													+ PO_CommonUtil.convertMonthToInt(month) + " " + "AND `vendorId`='"
													+ vendorId + "'");
									jdbcTemplate.execute(

											"UPDATE CbMissingInvoiceRequestDetails SET jobStatus='COMPLETED',jobEndDate=NOW() where id='"
													+ id + "'");
								} else {
									jdbcTemplate.execute(
											"UPDATE CbMissingInvoiceRequestDetails SET jobStatus='ERROR',jobEndDate=NOW() where id='"
													+ id + "'");
								}

							} else {
								counter = 0;
							}

						}
					} catch (Exception e) {
						counter = 0;
						e.printStackTrace();
					}

				}

				while (true) {

					logger.info("Query Running");
					StringBuilder sql = new StringBuilder();
					sql.append(
							"SELECT cpr.id, cpr.PO,cpr.vendorId,v.vendorName,cpr.requestId,cpr.uniqueKey,cpr.retry ,v.rs_jobPriority ,c.`businessUnit` FROM `CBMissingInvPORequest` cpr JOIN Vendor v ON(cpr.vendorId=v.id) JOIN `Client` c ON (c.vendorId=v.id) WHERE cpr.`status` = 'PENDING' AND v.`isPaused` = 'N'  AND (NOW() > ADDDATE(coolingPeriodStartTime, INTERVAL coolingPeriod MINUTE) OR coolingPeriodStartTime IS NULL) ORDER BY v.`rs_jobPriority`, RAND() LIMIT 1");

					List<CBMissingInvoicePoRequest> jobList = this.jdbcTemplate.query(sql.toString(),
							new RowMapper<CBMissingInvoicePoRequest>() {
								@Override
								public CBMissingInvoicePoRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
									CBMissingInvoicePoRequest res = new CBMissingInvoicePoRequest();

									res.setVendorId(rs.getString("vendorId"));
									res.setPo(rs.getString("PO"));
									res.setVendorName(rs.getString("vendorName"));
									res.setId(rs.getLong("id"));
									res.setRequestId(rs.getString("requestId"));
									res.setUniqueKey(rs.getString("uniqueKey"));
									res.setRetry(rs.getInt("retry"));
									res.setBusinessUnit(rs.getString("businessUnit"));
									return res;
								}
							}, new Object[] {});

					if (jobList.size() == 0) {
						logger.info("No Jobs Found In MissingPO Stage2");
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					} else {
						counter = counter + 1;
						jdbcTemplate.execute(
								"UPDATE CbMissingInvoiceRequest Set modifiedDate=NOW(),jobStage='Collecting POData' where vendorId='"
										+ jobList.get(0).getVendorId() + "'");

						if (jobList.get(0).getRetry() > 3) {
							jdbcTemplate.execute("UPDATE CBMissingInvPORequest SET STATUS = 'ERROR' WHERE uniqueKey ='"
									+ jobList.get(0).getUniqueKey() + "'");
						} else {
							jdbcTemplate.execute("UPDATE CBMissingInvPORequest SET STATUS = 'PENDING',retry="
									+ jobList.get(0).getRetry() + "+1 WHERE uniqueKey ='"
									+ jobList.get(0).getUniqueKey() + "'");
						}
						logger.info("update CBMissingInvPORequest set status='INPROGRESS',jobStartDate=NOW() where id='"
								+ jobList.get(0).getId() + "'");
						jdbcTemplate.execute(
								"update CBMissingInvPORequest set status='INPROGRESS',jobStartDate=NOW() where id='"
										+ jobList.get(0).getId() + "'");
						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
								jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();
						logger.info("login status: " + loginStatus.getRight());
						if (loginStatus.getRight()) {
							Path downloadPath = Paths.get("C:\\PalyWrightFile");
							boolean status = dimeTydStage2PageNewUi.processPage(page, jobList.get(0).getPo(),
									jobList.get(0).getVendorId(), jobList.get(0).getRequestId(),
									jobList.get(0).getRetry(), jobList.get(0).getVendorName(), downloadPath);

							if (status) {
								logger.info(
										"Query : update CBMissingInvPORequest set status='COMPLETED',jobEndDate=NOW() where id='"
												+ jobList.get(0).getId() + "'");
								jdbcTemplate.execute(
										"update CBMissingInvPORequest set status='COMPLETED',jobEndDate=NOW() where id='"
												+ jobList.get(0).getId() + "'");
							}

							else {
								logger.info(
										"Query : update CBMissingInvPORequest set status='ERROR',jobEndDate=NOW() where id='"
												+ jobList.get(0).getId() + "'");
								jdbcTemplate.execute(
										"update CBMissingInvPORequest set status='ERROR',jobEndDate=NOW() where id='"
												+ jobList.get(0).getId() + "'");
							}

						}

					}
				}

				while (true) {

					logger.info("Query Running");
					StringBuilder sql = new StringBuilder();
					sql.append(
							"SELECT pn.id, pn.vendorId, pn.PO,pn.POInvoice,pn.retry ,v.`vendorName`,c.`businessUnit`  FROM CBMissingInvPOInvoice pn JOIN `Vendor` v ON (pn.vendorId= v.id) JOIN `Client` c ON (c.vendorId=v.id) WHERE pn.Status = 'PENDING'  AND v.`isPaused` = 'N'  AND (NOW() > ADDDATE(coolingPeriodStartTime, INTERVAL coolingPeriod MINUTE) OR coolingPeriodStartTime IS NULL) ORDER BY RAND() LIMIT 1");
					List<CBMissingInvPOInvoice> jobList = this.jdbcTemplate.query(sql.toString(),
							new RowMapper<CBMissingInvPOInvoice>() {
								@Override
								public CBMissingInvPOInvoice mapRow(ResultSet rs, int rowNum) throws SQLException {
									CBMissingInvPOInvoice res = new CBMissingInvPOInvoice();
									res.setVendorId(rs.getString("vendorId"));
									res.setPo(rs.getString("PO"));
									res.setVendorName(rs.getString("vendorName"));
									res.setId(rs.getLong("id"));
									res.setPoInvoice(rs.getString("POInvoice"));
									res.setRetry(rs.getInt("retry"));
									res.setBusinessUnit(rs.getString("businessUnit"));
									return res;
								}
							}, new Object[] {});

					if (jobList.size() == 0) {
						logger.info("No Data Found in MissingPO Stage3");
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					} else {
						counter = counter + 1;
						jdbcTemplate.execute(
								"UPDATE CbMissingInvoiceRequest Set modifiedDate=NOW(),jobStage='Collecting POInvoiceData' where vendorId='"
										+ jobList.get(0).getVendorId() + "'");

						if (jobList.get(0).getRetry() > 3) {
							jdbcTemplate.execute("UPDATE CBMissingInvPOInvoice SET Status = 'ERROR' WHERE PO = '"
									+ jobList.get(0).getPo() + "' AND POinvoice = '" + jobList.get(0).getPoInvoice()
									+ "'");
						} else {
							jdbcTemplate.execute("UPDATE CBMissingInvPOInvoice SET Status = 'Pending', retry = '"
									+ jobList.get(0).getRetry() + "'+1,`machineName` = NULL WHERE PO = '"
									+ jobList.get(0).getPo() + "' AND POinvoice = '" + jobList.get(0).getPoInvoice()
									+ "'");
						}
						jdbcTemplate.execute(
								"update CBMissingInvPOInvoice set status='INPROGRESS' ,jobStartDate=NOW() where id='"
										+ jobList.get(0).getId() + "'");
						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
								jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();
						logger.info("login status: " + loginStatus.getRight());
						if (loginStatus.getRight()) {
							Path downloadPath = Paths.get("C:\\PalyWrightFile");
							StringBuilder sql11 = new StringBuilder();
							sql11.append("SELECT count(*) as cnt FROM `CBMissingInvPODetails` WHERE POInvoice='"
									+ jobList.get(0).getPoInvoice() + "' and vendorId='" + jobList.get(0).getVendorId()
									+ "'");

							List<CBMissingInvPOInvoice> jobList1 = this.jdbcTemplate.query(sql11.toString(),
									new RowMapper<CBMissingInvPOInvoice>() {
										@Override
										public CBMissingInvPOInvoice mapRow(ResultSet rs, int rowNum)
												throws SQLException {
											CBMissingInvPOInvoice res = new CBMissingInvPOInvoice();
											res.setRetry(rs.getInt("cnt"));

											return res;
										}
									}, new Object[] {});

							boolean process = true;
							if (jobList1.size() > 0) {
								if (jobList1.get(0).getRetry() > 0) {

									jdbcTemplate.execute(
											"update CBMissingInvPOInvoice set status='COMPLETED',jobEndDate=NOW() where id='"
													+ jobList.get(0).getId() + "'");

									process = false;
								}
							}

							if (process) {
								try {

									ProcessStatus status = dimeTydStage3PageNewUi.processPage(page,
											jobList.get(0).getPoInvoice(), jobList.get(0).getVendorId(),
											jobList.get(0).getVendorName(), downloadPath, context);
									if (status.getStatus()) {
										jdbcTemplate.execute(
												"update CBMissingInvPOInvoice set status='COMPLETED',jobEndDate=NOW()  where id='"
														+ jobList.get(0).getId() + "'");
									}

									else {
										jdbcTemplate.execute(
												"update CBMissingInvPOInvoice set comment='" + status.getComment()
														+ "' , status='ERROR',jobEndDate=NOW()  where id='"
														+ jobList.get(0).getId() + "'");
									}

								} catch (Exception e) {
									e.printStackTrace();

								}
							}
						}

					}

				}

				while (true) {
					try {
						StringBuilder sb = new StringBuilder();
						sb.append(
								"SELECT b.`id`,b.`vendorId`, c.`businessUnit`,a.PO, a.invoiceNumber,v.`vendorName`,a.retry "
										+ "FROM `CBMissingInvoiceOutput` a  "
										+ "JOIN CbMissingInvoiceRequest b ON (a.vendorId=b.vendorId AND b.id=a.requestId "
										+ "AND b.`jobStatus` = 'CREATING_INVOICE') JOIN `Vendor`v ON (a.vendorId=v.id) "
										+ "JOIN `Client` c ON (c.vendorId=v.id)  WHERE a.`status` ='PENDING' \r\n "
										+ "AND v.`isPaused` = 'N'  ORDER BY RAND() LIMIT 1");

						List<CBMissingInvoiceOutput> jobList = this.jdbcTemplate.query(sb.toString(),
								new RowMapper<CBMissingInvoiceOutput>() {
									@Override
									public CBMissingInvoiceOutput mapRow(ResultSet rs, int rowNum) throws SQLException {
										CBMissingInvoiceOutput res = new CBMissingInvoiceOutput();
										res.setId(rs.getLong("id"));
										res.setVendorId(rs.getString("vendorId"));
										res.setVendorName(rs.getString("vendorName"));
										res.setBusinessUnit(rs.getString("businessUnit"));
										res.setPO(rs.getString("PO"));
										res.setInvoiceNumber(rs.getString("invoiceNumber"));
										res.setRetry(rs.getInt("retry"));
										return res;
									}
								}, new Object[] {});

						if (jobList.size() == 0) {
							// fallback to split request query
							logger.info("Invoice creation Query Data not found.....");
							sb = new StringBuilder();
							sb.append(
									"SELECT b.`id`,b.`vendorId`, c.`businessUnit`,a.PO, a.invoiceNumber,v.`vendorName`,a.retry "
											+ "FROM `CBMissingInvoiceOutput` a  "
											+ "JOIN CbMissingInvoiceRequestSplit b ON (a.vendorId=b.vendorId AND b.id=a.`splitRequestId` "
											+ "AND b.`jobStatus` = 'CREATING_INVOICE') JOIN `Vendor`v ON (a.vendorId=v.id) "
											+ "JOIN `Client` c ON (c.vendorId=v.id)  WHERE a.`status`='PENDING'  \r\n "
											+ "AND v.`isPaused` = 'N'  ORDER BY RAND() LIMIT 1");

							jobList = this.jdbcTemplate.query(sb.toString(), new RowMapper<CBMissingInvoiceOutput>() {
								@Override
								public CBMissingInvoiceOutput mapRow(ResultSet rs, int rowNum) throws SQLException {
									CBMissingInvoiceOutput res = new CBMissingInvoiceOutput();
									res.setId(rs.getLong("id"));
									res.setVendorId(rs.getString("vendorId"));
									res.setVendorName(rs.getString("vendorName"));
									res.setBusinessUnit(rs.getString("businessUnit"));
									res.setPO(rs.getString("PO"));
									res.setInvoiceNumber(rs.getString("invoiceNumber"));
									res.setRetry(rs.getInt("retry"));
									return res;
								}
							}, new Object[] {});
							if (jobList.size() == 0) {
								logger.info("No Data Found in MissingPO Stage4");
								break;
							} else {
								counter = counter + 1;

								jdbcTemplate.execute(
										"UPDATE CbMissingInvoiceRequestSplit Set modifiedDate=NOW(),jobStage='Creating Invoice' where vendorId='"
												+ jobList.get(0).getVendorId() + "'");

								logger.info(
										"Query : update CBMissingInvoiceOutput  set status='INPROGRESS',jobStartDate=NOW() where invoiceNumber="
												+ jobList.get(0).getInvoiceNumber()+"' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
								jdbcTemplate.execute(
										"update CBMissingInvoiceOutput  set status='INPROGRESS',jobStartDate=NOW() where invoiceNumber='"
												+ jobList.get(0).getInvoiceNumber()+"' AND vendorId='"+ jobList.get(0).getVendorId() + "'");

								Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(),
										page, jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
								page = loginStatus.getLeft();
								logger.info("login status: " + loginStatus.getRight());
								if (loginStatus.getRight()) {
									feedbackpage(page);
									String status = invoiceCreationPage.processPage(page, jobList.get(0).getPO(),
											jobList.get(0).getInvoiceNumber(), jobList.get(0).getRetry(),
											jobList.get(0).getVendorName(),jobList.get(0).getVendorId());

									if (status.equals("COMPLETED")) {
										jdbcTemplate.execute(
												"update CBMissingInvoiceOutput  set status='COMPLETED', updateInvoiceStatus='PENDING',jobEndDate=NOW()where invoiceNumber='"
														+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
									}
									if (status.equals("ERROR")) {
										jdbcTemplate.execute(
												"update CBMissingInvoiceOutput  set status='ERROR',jobEndDate=NOW() where invoiceNumber='"
														+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
									}
									if (status.equals("ALREADY INVOICED")) {
										jdbcTemplate.execute(
												"update CBMissingInvoiceOutput  set status='ALREADY INVOICED' where invoiceNumber='"
														+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
									}
									if (status.equals("NOT FOUND")) {
										jdbcTemplate.execute(
												"update CBMissingInvoiceOutput  set status='NOT FOUND' where invoiceNumber='"
														+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
									}
									if (status.equals("PENDING")) {
										jdbcTemplate
												.execute("update CBMissingInvoiceOutput  set status='PENDING', retry="
														+ (jobList.get(0).getRetry() + 1) + " where invoiceNumber='"
														+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
									}
								}
							}
						}

						else {
							counter = counter + 1;

							jdbcTemplate.execute(
									"UPDATE CbMissingInvoiceRequest Set modifiedDate=NOW(),jobStage='Creating Invoice' where vendorId='"
											+ jobList.get(0).getVendorId() + "'");

							logger.info(
									"Query : update CBMissingInvoiceOutput  set status='INPROGRESS',jobStartDate=NOW() where invoiceNumber= '"
											+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
							jdbcTemplate.execute(
									"update CBMissingInvoiceOutput  set status='INPROGRESS',jobStartDate=NOW() where invoiceNumber='"
											+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");

							Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
									jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
							page = loginStatus.getLeft();
							logger.info("login status: " + loginStatus.getRight());
							if (loginStatus.getRight()) {
								String status = invoiceCreationPage.processPage(page, jobList.get(0).getPO(),
										jobList.get(0).getInvoiceNumber(), jobList.get(0).getRetry(),
										jobList.get(0).getVendorName(),jobList.get(0).getVendorId());

								if (status.equals("COMPLETED")) {
									jdbcTemplate.execute(
											"update CBMissingInvoiceOutput  set status='COMPLETED', updateInvoiceStatus='PENDING',jobEndDate=NOW() where invoiceNumber='"
													+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
								}
								if (status.equals("ERROR")) {
									jdbcTemplate.execute(
											"update CBMissingInvoiceOutput  set status='ERROR',jobEndDate=NOW() where invoiceNumber='"
													+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
								}
								if (status.equals("ALREADY INVOICED")) {
									jdbcTemplate.execute(
											"update CBMissingInvoiceOutput  set status='ALREADY INVOICED' where invoiceNumber='"
													+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
								}
								if (status.equals("NOT FOUND")) {
									jdbcTemplate.execute(
											"update CBMissingInvoiceOutput  set status='NOT FOUND' where invoiceNumber='"
													+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
								}
								if (status.equals("PENDING")) {
									jdbcTemplate.execute("update CBMissingInvoiceOutput  set status='PENDING', retry="
											+ (jobList.get(0).getRetry() + 1) + " where invoiceNumber='"
											+ jobList.get(0).getInvoiceNumber() + "' AND vendorId='"+ jobList.get(0).getVendorId() + "'");
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				while (true) {
					try {
						StringBuilder sql = new StringBuilder();
						sql.append(
								"SELECT a.`invoiceNumber` ,v.`vendorName`,c.`businessUnit`,a.`vendorId` FROM `CBMissingInvoiceOutput` a JOIN `Vendor`v ON (a.vendorId=v.id) JOIN `Client` c ON (c.vendorId=v.id) WHERE `updateInvoiceStatus` = 'PENDING' AND v.`isPaused` = 'N' ORDER BY RAND() LIMIT 1");
						List<CBMissingInvoiceOutput> catlogList = this.jdbcTemplate.query(sql.toString(),
								new RowMapper<CBMissingInvoiceOutput>() {
									@Override
									public CBMissingInvoiceOutput mapRow(ResultSet rs, int rowNum) throws SQLException {
										CBMissingInvoiceOutput res = new CBMissingInvoiceOutput();
										res.setInvoiceNumber(rs.getString("invoiceNumber"));
										res.setVendorName(rs.getString("vendorName"));
										res.setBusinessUnit(rs.getString("businessUnit"));
										res.setVendorId(rs.getString("vendorId"));
										return res;
									}
								}, new Object[] {});

						if (catlogList.size() == 0) {
							logger.info("No Data Found in MissingPO Stage5");
							break;
						} else {
							counter = counter + 1;

							jdbcTemplate
									.execute("UPDATE CbMissingInvoiceRequest Set modifiedDate=NOW() where vendorId='"
											+ catlogList.get(0).getVendorId() + "'");

							jdbcTemplate.execute(
									"update CBMissingInvoiceOutput  set updateInvoiceStatus='INPROGRESS' where invoiceNumber='"
											+ catlogList.get(0).getInvoiceNumber() + "'");
							Pair<Page, Boolean> loginStatus = loginObj.loginProcess(catlogList.get(0).getVendorId(),
									page, catlogList.get(0).getVendorName(), loginHashMap, counter, browser, context);
							page = loginStatus.getLeft();
							logger.info("login status: " + loginStatus.getRight());
							if (loginStatus.getRight()) {

								String status = invoiceDataUpdatePage.processPage(page,
										catlogList.get(0).getInvoiceNumber(), catlogList.get(0).getVendorName());

								if (status.equals("true")) {
									jdbcTemplate.execute(
											"update CBMissingInvoiceOutput  set updateInvoiceStatus='COMPLETED' where invoiceNumber='"
													+ catlogList.get(0).getInvoiceNumber() + "'");
								} else if (status.equals("false")) {
									jdbcTemplate.execute(
											"update CBMissingInvoiceOutput  set updateInvoiceStatus='ERROR' where invoiceNumber='"
													+ catlogList.get(0).getInvoiceNumber() + "'");
								} else if (status.equals("NULL")) {
									jdbcTemplate.execute(
											"update CBMissingInvoiceOutput  set updateInvoiceStatus='ERROR' , comments='No Invoice Found' where invoiceNumber='"
													+ catlogList.get(0).getInvoiceNumber() + "'");
								}
							}

						}
					} catch (Exception e) {
						e.printStackTrace();

					}

				}

				while (true) {

					logger.info("Query Running");
					StringBuilder sql = new StringBuilder();
					sql.append(
							"SELECT a.id,v.vendorName,a.`vendorId`,a.`invoiceCreateDate`,a.`cronTriggerDate`,c.`businessUnit` "
									+ "FROM `CbMissingInvoiceRequest` a JOIN Vendor v ON (a.vendorId=v.id) JOIN `Client` c ON (a.vendorId=c.vendorId)"
									+ " WHERE `jobStatus` = 'QUEUED_PAYMENTS' AND v.`isPaused` = 'N' "
									+ "AND `cronTriggerDate` < NOW() " + "ORDER BY RAND()");

					List<CbMissingInvoiceRequest> jobList = this.jdbcTemplate.query(sql.toString(),
							new RowMapper<CbMissingInvoiceRequest>() {
								@Override
								public CbMissingInvoiceRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
									CbMissingInvoiceRequest res = new CbMissingInvoiceRequest();

									res.setVendorId(rs.getString("vendorId"));
									res.setVendorName(rs.getString("vendorName"));
									res.setId(rs.getLong("id"));
									res.setBusinessUnit(rs.getString("businessUnit"));
									res.setInvoiceCreateDate(rs.getDate("invoiceCreateDate"));
									res.setCronTriggerDate(rs.getDate("cronTriggerDate"));
									return res;
								}
							}, new Object[] {});

					if (jobList.size() == 0) {
						logger.info("No Data Found In PaymentDataUpdation Stage");

						sql = new StringBuilder();
						sql.append(
								"SELECT a.id,v.vendorName,a.`vendorId`,a.`invoiceCreateDate`,a.`cronTriggerDate`,c.`businessUnit`"
										+ " FROM `CbMissingInvoiceRequestSplit` a JOIN Vendor v ON (a.vendorId=v.id) "
										+ "JOIN `Client` c ON (a.vendorId=c.vendorId) "
										+ "WHERE `jobStatus` = 'QUEUED_PAYMENTS' AND v.`isPaused` = 'N' "
										+ "AND `cronTriggerDate` < NOW() " + "ORDER BY RAND()");

						List<CbMissingInvoiceRequest> jobListsplit = this.jdbcTemplate.query(sql.toString(),
								new RowMapper<CbMissingInvoiceRequest>() {
									@Override
									public CbMissingInvoiceRequest mapRow(ResultSet rs, int rowNum)
											throws SQLException {
										CbMissingInvoiceRequest res = new CbMissingInvoiceRequest();

										res.setVendorId(rs.getString("vendorId"));
										res.setVendorName(rs.getString("vendorName"));
										res.setId(rs.getLong("id"));
										res.setBusinessUnit(rs.getString("businessUnit"));
										res.setInvoiceCreateDate(rs.getDate("invoiceCreateDate"));
										res.setCronTriggerDate(rs.getDate("cronTriggerDate"));
										return res;
									}
								}, new Object[] {});
						if (jobListsplit.size() == 0) {
							logger.info("Both Payment data stage not found..");
							System.exit(0);
						} else {

							counter = counter + 1;
							jdbcTemplate.execute(
									"UPDATE CbMissingInvoiceRequestSplit Set modifiedDate=NOW(),jobStage='Updating Payment Data' where vendorId='"
											+ jobListsplit.get(0).getVendorId() + "'");
							jdbcTemplate
									.execute("update CbMissingInvoiceRequestSplit set status='INPROGRESS' where id='"
											+ jobListsplit.get(0).getId() + "'");
							Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobListsplit.get(0).getVendorId(),
									page, jobListsplit.get(0).getVendorName(), loginHashMap, counter, browser, context);
							page = loginStatus.getLeft();
							logger.info("login status: " + loginStatus.getRight());
							if (loginStatus.getRight()) {
								Path downloadPath = Paths.get("C:\\PalyWrightFile");
								boolean status = paymentDataUpdationProcess.processPage(page,
										jobListsplit.get(0).getId().intValue(), jobListsplit.get(0).getVendorName(),
										jobListsplit.get(0).getVendorId(), jobListsplit.get(0).getInvoiceCreateDate(),
										jobListsplit.get(0).getCronTriggerDate(), downloadPath);

								if (status) {
									jdbcTemplate.execute(
											"update CbMissingInvoiceRequestSplit set jobStatus='PAYMENAT_DATA_UPDATED' where id='"
													+ jobListsplit.get(0).getId() + "'");

									// Setting up necessary details
									String sendTo = "amit@dimetyd.com,namita@dimetyd.com,shradha@dimetyd.com";
									String sendCC = "karan@dimetyd.com,vikram@dimetyd.com,alok@dimetyd.com";
									String body = String.format(
											"Dear Team, \n\n    Payment Data Extracted For {0} \n\nThanks.\n\nRegards, \n\nUiPath and Robotics Team",
											jobListsplit.get(0).getVendorName());
									String subject = "Invoice Reconciliation Payment";

									emailService.sendMailToClient(sendTo, sendCC, body, subject);
								}
							}

						}
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					} else {
						counter = counter + 1;
						jdbcTemplate.execute(
								"UPDATE CbMissingInvoiceRequest Set modifiedDate=NOW(),jobStage='Updating Payment Data' where vendorId='"
										+ jobList.get(0).getVendorId() + "'");
						jdbcTemplate.execute("update CbMissingInvoiceRequest set status='INPROGRESS' where id='"
								+ jobList.get(0).getId() + "'");
						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
								jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();
						logger.info("login status: " + loginStatus.getRight());
						if (loginStatus.getRight()) {
							Path downloadPath = Paths.get("C:\\PalyWrightFile");
							boolean status = paymentDataUpdationProcess.processPage(page,
									jobList.get(0).getId().intValue(), jobList.get(0).getVendorName(),
									jobList.get(0).getVendorId(), jobList.get(0).getInvoiceCreateDate(),
									jobList.get(0).getCronTriggerDate(), downloadPath);

							if (status) {
								jdbcTemplate.execute(
										"update CbMissingInvoiceRequest set jobStatus='PAYMENAT_DATA_UPDATED' where id='"
												+ jobList.get(0).getId() + "'");

								// Setting up necessary details
								String sendTo = "amit@dimetyd.com,namita@dimetyd.com,shradha@dimetyd.com";
								String sendCC = "karan@dimetyd.com,vikram@dimetyd.com,alok@dimetyd.com";
								String body = String.format(
										"Dear Team, \n\n    Payment Data Extracted For {0} \n\nThanks.\n\nRegards, \n\nUiPath and Robotics Team",
										jobList.get(0).getVendorName());
								String subject = "Invoice Reconciliation Payment";

								emailService.sendMailToClient(sendTo, sendCC, body, subject);
							}
						}
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
}