package com.dimetyd.bot.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.JobData;
import com.dimetyd.bot.model.PODetailsFDReportsTransactions;
import com.dimetyd.bot.model.ProcessStatus;
import com.dimetyd.bot.process.FDReportDownload;
import com.dimetyd.bot.process.CoopAgreementBackupDownload;
import com.dimetyd.bot.process.RequestAllStage2PageNewUi;
import com.dimetyd.bot.process.RequestAllStage3PageNewUi;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class CBRequestAllService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	CoopAgreementBackupDownload CBRequestAllStage1Page;
	@Autowired
	// RequestAllStage2PageOldUi trueconsultingStage2Page;
	RequestAllStage2PageNewUi CBRequestAllStage2Page;
	@Autowired
	RequestAllStage3PageNewUi CBRequestAllStage3Page;
	private int counter = 0;
	static Page page;
	BrowserContext context;
	@Autowired
	FDReportDownload fdreportdownload;

	Map<String, String> accountDetails = new HashMap<String, String>();
	HashMap<String, String> loginHashMap = new HashMap<String, String>();

	int cnt = 0;

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Request_All_Bot")) {
			// stage1

			logger.info("Request All Bot Started...");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));
			boolean status = false;
			Path downloadPath = Paths.get("C:\\Playwright File");
			boolean isAllFDDataDownload = false;
			while (true) {
				cnt = 0;
				try {
					while (true) {

						StringBuilder sql = new StringBuilder();

						sql.append(
								"SELECT * FROM (SELECT cr.id AS id , c.id AS requestId,c.RequestType, c.vendorId, v.vendorName,cr.`retry`,cr.`agreementId`,cr.`startDt`,\r\n"
										+ "cr.`endDt`, c.createdBy FROM CBRequest c JOIN `CBAgreementRequestDetails` cr ON (c.id=cr.requestId) JOIN Vendor v \r\n"
										+ "ON (v.id = c.vendorId) WHERE cr.`status` = 'PENDING' AND v.`isPaused` = 'N' \r\n"
										+ "AND `RequestType` IN ('DROPSHIP','QTY_MISMATCH','FRIEGHT_CHECK') and agreementId IN ('76837755','64466745','65002000','62666420','76837755','75689315') \r\n"
										+ "ORDER BY v.master_Crd_Id LIMIT 100) AS z ORDER BY z.id LIMIT 1");
						System.out.println(sql);
						List<JobData> catlogList = this.jdbcTemplate.query(sql.toString(), new RowMapper<JobData>() {
							@Override
							public JobData mapRow(ResultSet rs, int rowNum) throws SQLException {
								JobData res = new JobData();

								res.setVendorId(rs.getString("vendorId"));
								res.setVendoName(rs.getString("vendorName"));
								res.setAgreementId(rs.getString("agreementId"));
								res.setRequestId(rs.getLong("requestId"));
								res.setId(rs.getLong("id"));
								res.setRequestType(rs.getString("RequestType"));
								res.setStartDate(rs.getDate("startDt"));
								res.setEndDate(rs.getDate("endDt"));
								res.setCreatedBy(rs.getInt("createdBy"));

								return res;
							}
						}, new Object[] {});

						if (catlogList.size() == 0) {
							logger.info("No Data Found in Stage 1");
							// notFound++;

							break;
						}
						counter = counter + 1;
						cnt++;
							JobData jobData = catlogList.get(0);
							String vendorName = jobData.getVendoName();
							logger.info("VendorName : " + vendorName);
							String agreementNo = jobData.getAgreementId();
							String vendorId = jobData.getVendorId();
							Long requestId = jobData.getRequestId();
							Long id = jobData.getId();
							String RequestType = jobData.getRequestType();
							Date startDate = jobData.getStartDate();
							Date endDate = jobData.getEndDate();
							int createdBy = jobData.getCreatedBy();

							jdbcTemplate.execute(
									"update CBRequest set requestStatus='INPROGRESS',processedStartDate =NOW() where id='"
											+ requestId + "'");
							jdbcTemplate.execute(
									"update CBAgreementRequestDetails set status='INPROGRESS',processedStartDate =NOW() where id='"
											+ id + "'");

							Pair<Page, Boolean> loginStatus = loginObj.loginProcess(catlogList.get(0).getVendorId(),
									page, catlogList.get(0).getVendoName(), loginHashMap, counter, browser, context);

							page = loginStatus.getLeft();
							logger.info("login status: " + loginStatus.getRight());

							if (loginStatus.getRight()) {
								// isLoggedIn = "true";
							
//								if (RequestType != "SHORTAGE_RECONCILIATION") {
//									while (true) {
//										String PODetailsQuery = "SELECT p.*,`CBPODetailsFDReports`FROM `CBPODetailsFDReportsTransactions` p JOIN `vendorMenuAccess` vm ON (vm.vendorId=p.vendorId) WHERE p.vendorId='"
//												+ vendorId + "' AND p.status='PENDING' LIMIT 1";
//										jdbcTemplate.execute(PODetailsQuery);
//										System.out.println(sql);
//										List<PODetailsFDReportsTransactions> fdList = this.jdbcTemplate.query(
//												PODetailsQuery.toString(),
//												new RowMapper<PODetailsFDReportsTransactions>() {
//													@Override
//													public PODetailsFDReportsTransactions mapRow(ResultSet rs,
//															int rowNum) throws SQLException {
//														PODetailsFDReportsTransactions fdDetails = new PODetailsFDReportsTransactions();
//
//														fdDetails.setVendorId(rs.getString("vendorId"));
//														fdDetails.setId(rs.getString("id"));
//														fdDetails.setStartDate(rs.getString("startDate"));
//														fdDetails.setEndDate(rs.getString("endDate"));
//														fdDetails.setVendorName(rs.getString("vendorName"));
//														fdDetails.setFileRowCount(rs.getString("comment"));
//														fdDetails.setYear(rs.getString("year"));
//														fdDetails.setIsFDReport(rs.getString("FinancialReports"));
//														logger.info(PODetailsQuery);
//														return fdDetails;
//													}
//												}, new Object[] {});
//
//										if (fdList.size() == 0) {
//
//											isAllFDDataDownload = true;
//
//											logger.info(
//													"No Transaction Found to fetch this vendor Fd Data " + vendorId);
//
//											break;
//										} else {
//											logger.info(fdList.get(0).getIsFDReport());
//											if (fdList.get(0).getIsFDReport().equals("1")) {
//												logger.info("Financial Report Access Found");
//												if (fdList.get(0).getFileRowCount() != null) {
//													jdbcTemplate.execute(
//															"DELETE FROM `CBPODetailsFDReports` WHERE vendorId='"
//																	+ fdList.get(0).getVendorId()
//																	+ "' AND YEAR(invoiceDate)='"
//																	+ fdList.get(0).getYear() + "'\r\n" + "");
//												}
//
//												logger.info(
//														"update CBPODetailsFDReportsTransactions set status='INPROGRESS' where id='"
//																+ fdList.get(0).getId() + "'");
//												jdbcTemplate.execute(
//														"update CBPODetailsFDReportsTransactions set status='INPROGRESS' where id='"
//																+ fdList.get(0).getId() + "'");
//
//												boolean isUpdated = fdreportdownload.navigateToPurchaseOrders(page,
//														fdList.get(0).getStartDate(), fdList.get(0).getEndDate(),
//														fdList.get(0).getVendorId(), fdList.get(0).getVendorName(),
//														downloadPath.toString(), fdList.get(0).getId());
//												if (isUpdated) {
//													logger.info(
//															"update CBPODetailsFDReportsTransactions set status='COMPLETED',modifiedDate=NOW() where id='"
//																	+ fdList.get(0).getId() + "'");
//													jdbcTemplate.execute(
//															"update CBPODetailsFDReportsTransactions set status='COMPLETED',modifiedDate=NOW() where id='"
//																	+ fdList.get(0).getId() + "'");
//
//													logger.info("One Year Data Inserted");
//												} else {
//													logger.info(
//															"update CBPODetailsFDReportsTransactions set status='ERROR',modifiedDate=NOW() where id='"
//																	+ fdList.get(0).getId() + "'");
//													jdbcTemplate.execute(
//															"update CBPODetailsFDReportsTransactions set status='ERROR',modifiedDate=NOW() where id='"
//																	+ fdList.get(0).getId() + "'");
//												}
//
//											} else {
//
//												logger.info("Financial Report Access not Found : "
//														+ fdList.get(0).getVendorId());
//												logger.info(
//														"update CBPODetailsFDReportsTransactions set status='COMPLETED',comment='Financial Report Access not Found' where id='"
//																+ fdList.get(0).getId() + "'");
//												jdbcTemplate.execute(
//														"update CBPODetailsFDReportsTransactions set status='COMPLETED',comment='Financial Report Access not Found' where id='"
//																+ fdList.get(0).getId() + "'");
//
//											}
//										}
//
//									}
//
//								}
								switch (RequestType) {

								case "DROPSHIP": {
									logger.info("----------------REQUEST TYPE : DROPSHIP----------------");

									try {
										if (isAllFDDataDownload) {
											status = CBRequestAllStage1Page.processPage(page, id, agreementNo,
													vendorId, vendorName, requestId, downloadPath);
										}
									} catch (Exception ex) {
										status = false;
										ex.printStackTrace();
									}
									if (status) {
										logger.info("update CBAgreementRequestDetails set status='COMPLETED' where id='"
												+ id + "'");
										jdbcTemplate.execute(
												"update CBAgreementRequestDetails set status='COMPLETED' where id='" + id
														+ "'");

									}

									else {
										logger.info("update CBAgreementRequestDetails set status='ERROR' where id='" + id
												+ "'");
										jdbcTemplate
												.execute("update CBAgreementRequestDetails set status='ERROR' where id='"
														+ id + "'");
									}

								}
									break;

								case "QTY_MISMATCH": {

									logger.info("----------------REQUEST TYPE : QTY_MISMATCH----------------");
									try {
										
											status = CBRequestAllStage1Page.processPage(page, id, agreementNo,
													vendorId, vendorName, requestId, downloadPath);
										
									} catch (Exception ex) {
										status = false;
										ex.printStackTrace();
									}
									if (status) {
										logger.info("update CBAgreementRequestDetails set status='COMPLETED' where id='"
												+ id + "'");
										jdbcTemplate.execute(
												"update CBAgreementRequestDetails set status='COMPLETED' where id='" + id
														+ "'");
									

										logger.info(
												"INSERT IGNORE  INTO `CBPORequestV2` (`uniqueKey`,`PO`,`vendorId`,`agreementId`,`requestId`,`status`,`retry`,`createdDate`)\r\n"
														+ "SELECT CONCAT(cp.PO,cp.vendorId),cp.PO,cp.vendorId,cp.agreementID,cp.requestId,'PENDING',0,NOW() FROM  CBAgreementInvoiceDetails cp \r\n"
														+ "LEFT JOIN CBPODetailsFDReports fd ON(cp.POASIN=fd.POASIN) WHERE cp.vendorId='"
														+ vendorId
														+ "' AND fd.POASIN IS NULL AND cp.rebate>0 AND CHAR_LENGTH(cp.PO)=8 AND\r\n"
														+ "transactionType IN ('Distributor Shipment','Amazon Payables','Amazon Payments') \r\n"
														+ "AND cp.`vendorId` = '" + vendorId + "' GROUP BY cp.POASIN");
										jdbcTemplate.execute(
												"INSERT IGNORE  INTO `CBPORequestV2` (`uniqueKey`,`PO`,`vendorId`,`agreementId`,`requestId`,`status`,`retry`,`createdDate`)\r\n"
														+ "SELECT CONCAT(cp.PO,cp.vendorId),cp.PO,cp.vendorId,cp.agreementID,cp.requestId,'PENDING',0,NOW() FROM  CBAgreementInvoiceDetails cp \r\n"
														+ "LEFT JOIN CBPODetailsFDReports fd ON(cp.POASIN=fd.POASIN) WHERE cp.vendorId='"
														+ vendorId
														+ "' AND fd.POASIN IS NULL AND cp.rebate>0 AND CHAR_LENGTH(cp.PO)=8 AND\r\n"
														+ "transactionType IN ('Distributor Shipment','Amazon Payables','Amazon Payments')   \r\n"
														+ "AND cp.`vendorId` = '" + vendorId + "' GROUP BY cp.POASIN");

									}

									else {
										logger.info("update CBAgreementRequestDetails set status='ERROR' where id='" + id
												+ "'");
										jdbcTemplate
												.execute("update CBAgreementRequestDetails set status='ERROR' where id='"
														+ id + "'");
									}

								}

									// assigned to another job

									break;

								case "FRIEGHT_CHECK": {
									logger.info("----------------REQUEST TYPE : FRIEGHT_CHECK----------------");

									try {
										status = CBRequestAllStage1Page.processPage(page, id, agreementNo, vendorId,
												vendorName, requestId, downloadPath);
									} catch (Exception ex) {
										status = false;
										ex.printStackTrace();
									}
									if (status && isAllFDDataDownload) {
										logger.info("update CBAgreementRequestDetails set status='COMPLETED' where id='"
												+ id + "'");
										jdbcTemplate.execute(
												"update CBAgreementRequestDetails set status='COMPLETED' where id='" + id
														+ "'");


										logger.info(
												"INSERT IGNORE  INTO `CBPORequestV2` (`uniqueKey`,`PO`,`vendorId`,`agreementId`,`requestId`,`status`,`retry`,`createdDate`)\r\n"
														+ "SELECT CONCAT(cp.PO,cp.vendorId),cp.PO,cp.vendorId,cp.agreementID,cp.requestId,'PENDING',0,NOW() FROM  CBAgreementInvoiceDetails cp \r\n"
														+ "LEFT JOIN CBPODetailsFDReports fd ON(cp.POASIN=fd.POASIN) WHERE cp.vendorId='"
														+ vendorId
														+ "' AND fd.POASIN IS NULL AND cp.rebate>0 AND CHAR_LENGTH(cp.PO)=8 AND\r\n"
														+ "transactionType IN ('Distributor Shipment','Amazon Payables','Amazon Payments') \r\n"
														+ "AND cp.`vendorId` = '" + vendorId + "' GROUP BY cp.POASIN");
										jdbcTemplate.execute(
												"INSERT IGNORE  INTO `CBPORequestV2` (`uniqueKey`,`PO`,`vendorId`,`agreementId`,`requestId`,`status`,`retry`,`createdDate`)\r\n"
														+ "SELECT CONCAT(cp.PO,cp.vendorId),cp.PO,cp.vendorId,cp.agreementID,cp.requestId,'PENDING',0,NOW() FROM  CBAgreementInvoiceDetails cp \r\n"
														+ "LEFT JOIN CBPODetailsFDReports fd ON(cp.POASIN=fd.POASIN) WHERE cp.vendorId='"
														+ vendorId
														+ "' AND fd.POASIN IS NULL AND cp.rebate>0 AND CHAR_LENGTH(cp.PO)=8 AND\r\n"
														+ "transactionType IN ('Distributor Shipment','Amazon Payables','Amazon Payments') \r\n"
														+ "AND cp.`vendorId` = '" + vendorId + "' GROUP BY cp.POASIN");

									}

									else {
										logger.info("update CBAgreementRequestDetails set status='ERROR' where id='" + id
												+ "'");
										jdbcTemplate
												.execute("update CBAgreementRequestDetails set status='ERROR' where id='"
														+ id + "'");
									}
								}

									break;


							}

						} else {
							logger.info("update CBAgreementRequestDetails set status='PENDING' where id='"
									+ catlogList.get(0).getId() + "'");
							jdbcTemplate.execute("update CBAgreementRequestDetails set status='PENDING' where id='"
									+ catlogList.get(0).getId() + "'");
						}

						if (cnt > 100)
							break;
					
					}
					//
					cnt = 0;
					// stage2
					
					
					while (true) {

						StringBuilder sql=new StringBuilder();

						sql.append("SELECT z.vendorId, z.vendorName, GROUP_CONCAT(CONCAT(\"'\", z.PO, \"'\") ORDER BY z.PO ASC SEPARATOR ',') AS PO, MAX(z.requestId) AS requestId, MAX(z.uniqueKey) AS uniqueKey, MAX(z.retry) AS retry, MAX(z.jobPriority) AS jobPriority, MAX(z.master_crd) AS master_crd, MAX(z.isQtyZero) AS isQtyZero FROM (SELECT cpr.PO, cpr.vendorId, v.vendorName, cpr.requestId, cpr.uniqueKey, cpr.retry, v.jobPriority, v.master_Crd_Id AS master_crd, cpr.isQtyMissmatch AS isQtyZero FROM CBPORequestV2 cpr JOIN Vendor v ON cpr.vendorId = v.id WHERE cpr.status = 'PENDING' "
								+ "AND v.isPaused = 'N'   ORDER BY v.jobPriority ASC LIMIT 100) AS z GROUP BY z.vendorId, z.vendorName ORDER BY MAX(z.jobPriority) ASC, MAX(z.master_crd) ASC LIMIT 1;");

               System.out.println(sql);
						List<JobData> catlogList = this.jdbcTemplate.query(sql.toString(), new RowMapper<JobData>() {

							@Override
							public JobData mapRow(ResultSet rs, int rowNum) throws SQLException {
								JobData res = new JobData();

								res.setVendorId(rs.getString("vendorId"));
								res.setPo(rs.getString("PO"));
								res.setVendoName(rs.getString("vendorName"));
								// res.setId(rs.getLong("id"));
								res.setRequestId(rs.getLong("requestId"));
								res.setUniqueKey(rs.getString("uniqueKey"));
								res.setRetry(rs.getInt("retry"));
								res.setQty(rs.getString("isQtyZero"));

								return res;
							}
						}, new Object[] {});

						if (catlogList.size() == 0) {
							logger.info("No Data Found In Stage 2");
							break;
						}


							cnt++;
							counter = counter + 1;
							JobData jobData = catlogList.get(0);
							 String vendorName = jobData.getVendoName();
							logger.info("VendorName : " + vendorName);
							String poNumber = jobData.getPo();
							if (poNumber.charAt(poNumber.length() - 2) == ',') {
							    // Your logic here, for example, removing the trailing comma
							    poNumber = poNumber.substring(0, poNumber.length() - 2);
							}
							//poNumber=poNumber.substring(0,poNumber.length()-2);
							 String vendorId = jobData.getVendorId();
							/// Long id = jobData.getId();
							 int retry = jobData.getRetry();
							String uniqueKey = jobData.getUniqueKey();
							 String isQtyZero = jobData.getQty();
							// checkJarFile.checkJarFile(driver,
							// MachineName));
							if (retry > 3) {
								jdbcTemplate.execute("UPDATE CBPORequestV2 SET STATUS = 'ERROR' WHERE PO in("
										+ poNumber + ") and vendorId='" + vendorId + "'");
							} else {
								jdbcTemplate.execute("UPDATE CBPORequestV2 SET STATUS = 'PENDING',retry=" + retry
										+ "+1 WHERE PO in(" + poNumber + ") and vendorId='" + vendorId + "'");
							}

							logger.info("update CBPORequestV2 set status='INPROGRESS' where PO in(" + poNumber
									+ ") and vendorId='" + vendorId + "'");
							jdbcTemplate.execute("update CBPORequestV2 set status='INPROGRESS' where PO in("
									+ poNumber + ") and vendorId='" + vendorId + "'");

							Pair<Page, Boolean> loginStatus = loginObj.loginProcess(catlogList.get(0).getVendorId(),
									page, catlogList.get(0).getVendoName(), loginHashMap, counter, browser, context);
							page = loginStatus.getLeft();
							if (loginStatus.getRight()) {
								// isLoggedIn = "true";
							
								String vnderNameTrimed = vendorName.substring(0, 2).trim();
								logger.info("vnderNameTrimed : " + vnderNameTrimed);

							
								status = CBRequestAllStage2Page.processPage(page, poNumber, vendorId,
										jobData.getRequestId(), jobData.getRetry(), vendorName, downloadPath, isQtyZero,
										uniqueKey);
								

								if (status) {
									logger.info("update CBPORequestV2 set status='COMPLETED'  where PO in(" + poNumber
											+ ") and vendorId='" + vendorId + "'");
									jdbcTemplate.execute("update CBPORequestV2 set status='COMPLETED'  where PO in("
											+ poNumber + ") and vendorId='" + vendorId + "'");
								}

								else {
									logger.info("update CBPORequestV2 set status='ERROR'  where PO in(" + poNumber
											+ ") and vendorId='" + vendorId + "'");
									jdbcTemplate.execute("update CBPORequestV2 set status='ERROR'  where PO in("
											+ poNumber + ") and vendorId='" + vendorId + "'");
								}

							} else {
								logger.info("update CBPORequestV2 set status='PENDING'  where PO in(" + poNumber
										+ ") and vendorId='" + vendorId + "'");
								jdbcTemplate.execute("update CBPORequestV2 set status='PENDING'  where PO in("
										+ poNumber + ") and vendorId='" + vendorId + "'");
							}

							if (cnt > 100)
								break;

						

					
					}
					cnt = 0;
					while (true) {

						try {
							StringBuilder sql=new StringBuilder();

							sql.append("SELECT * FROM (SELECT pn.id, pn.vendorId, pn.PO,pn.`POinvoice`,pn.retry ,v.`vendorName`,\r\n"
									+ "v.`jobPriority`,\r\n"
									+ "v.master_Crd_Id AS master_crd FROM CBPOInvoiceDetailsV2 pn\r\n"
									+ "JOIN `Vendor` v ON (pn.vendorId= v.id) WHERE pn.Status = 'PENDING'   AND v.`isPaused` = 'N'\r\n"
									+ "ORDER BY jobpriority ASC LIMIT 100) AS z ORDER BY jobPriority,master_Crd ASC LIMIT 1");

							logger.info("SELECT * FROM (SELECT pn.id, pn.vendorId, pn.PO,pn.`POinvoice`,pn.retry ,v.`vendorName`,\r\n"
									+ "v.`jobPriority`,\r\n"
									+ "v.master_Crd_Id AS master_crd FROM CBPOInvoiceDetailsV2 pn\r\n"
									+ "JOIN `Vendor` v ON (pn.vendorId= v.id) WHERE pn.Status = 'PENDING'  AND  v.`isPaused` = 'N'\r\n"
									+ "ORDER BY jobpriority ASC LIMIT 100) AS z ORDER BY jobPriority,master_Crd ASC LIMIT 1");
							List<JobData> catlogList = this.jdbcTemplate.query(sql.toString(),
									new RowMapper<JobData>() {
										@Override
										public JobData mapRow(ResultSet rs, int rowNum) throws SQLException {
											JobData res = new JobData();

											res.setVendorId(rs.getString("vendorId"));
											res.setPo(rs.getString("PO"));
											res.setVendoName(rs.getString("vendorName"));
											res.setId(rs.getLong("id"));
											res.setPoInvoice(rs.getString("POinvoice"));
											res.setRetry(rs.getInt("retry"));
											return res;
										}
									}, new Object[] {});

							if (catlogList.size() == 0) {
								logger.info("No Data Found in Stage 3");
								logger.info("JOBS ENDED");
								System.exit(0);
							}

								counter = counter + 1;
								JobData jobData = catlogList.get(0);
								String vendorName = jobData.getVendoName();
								logger.info("VendorName : " + vendorName);
								String poNumber = jobData.getPoInvoice();
								
								// String po = jobData.getPo();
								String vendorId = jobData.getVendorId();
								Long id = jobData.getId();
								Integer retry = jobData.getRetry();

								if (retry > 3) {
									jdbcTemplate.execute(
											"UPDATE CBPOInvoiceDetailsV2 SET Status = 'ERROR' where id='" + id + "'");
								} else {
									jdbcTemplate.execute("UPDATE CBPOInvoiceDetailsV2 SET Status = 'Pending', retry = "
											+ retry + "+1,`machineName` = NULL where id='" + id + "'");
								}

								jdbcTemplate.execute(
										"update CBPOInvoiceDetailsV2 set status='INPROGRESS' where id='" + id + "'");

								StringBuilder sql1 = new StringBuilder();
								sql1.append("SELECT count(*) as cnt FROM `CBPODetailsV2` WHERE POInvoice='"
										+ poNumber + "' and vendorId='" + vendorId + "'");

								List<JobData> catlogList1 = this.jdbcTemplate.query(sql1.toString(),
										new RowMapper<JobData>() {
											@Override
											public JobData mapRow(ResultSet rs, int rowNum) throws SQLException {
												JobData res = new JobData();
												res.setRetry(rs.getInt("cnt"));

												return res;
											}
										}, new Object[] {});

								boolean process = true;
								if (catlogList1.size() > 0) {
									if (catlogList1.get(0).getRetry() > 0) {

										jdbcTemplate.execute(
												"update `CBPOInvoiceDetailsV2`  set status='COMPLETED' where id='" + id + "'");

										process = false;
									}
								}

								if (process) {
									Pair<Page, Boolean> loginStatus = loginObj.loginProcess(
											catlogList.get(0).getVendorId(), page, catlogList.get(0).getVendoName(),
											loginHashMap, counter, browser, context);
									page = loginStatus.getLeft();

									if (loginStatus.getRight()) {
										// isLoggedIn = "true";
										
										try {
											String vnderNameTrimed = vendorName.substring(0, 2).trim();
											logger.info("vnderNameTrimed : " + vnderNameTrimed);
											ProcessStatus status1;

										
											status1 = CBRequestAllStage3Page.processPage(page, poNumber, vendorId,
													vendorName, downloadPath, context, id);

											logger.info("IN stage 3");
											if (status1.getStatus()) {
												logger.info(
														"update CBPOInvoiceDetailsV2 set , status='COMPLETED'  where id='"
																+ id + "'");
												jdbcTemplate.execute(
														"update CBPOInvoiceDetailsV2 set status='COMPLETED'  where id='" + id
																+ "'");
											}

											else {
												logger.info(
														"update CBPOInvoiceDetailsV2 set , comment='" + status1.getComment()
																+ "' , status='ERROR'  where id='" + id + "'");
												jdbcTemplate.execute(
														"update CBPOInvoiceDetailsV2 set comment='" + status1.getComment()
																+ "' , status='ERROR'  where id='" + id + "'");
											}

										} catch (Exception e) {
											e.printStackTrace();
											jdbcTemplate
													.execute("update CBPOInvoiceDetailsV2 set status='ERROR'   where id='"
															+ id + "'");

										}
									} else {
										logger.info(
												"update CBPOInvoiceDetailsV2 set status='PENDING' where id='" + id + "'");
										jdbcTemplate.execute(
												"update CBPOInvoiceDetailsV2 set status='PENDING' where id='" + id + "'");
									}

								}
								if (cnt > 300)
									break;
							

						} catch (Exception e) {
							e.printStackTrace();

						}

					}

				}
				
				catch (Exception ex) {
					ex.printStackTrace();
					logger.info("JOBS ENDED");
					break;
				}
			}
	
			System.exit(0);
		}
	}
}
