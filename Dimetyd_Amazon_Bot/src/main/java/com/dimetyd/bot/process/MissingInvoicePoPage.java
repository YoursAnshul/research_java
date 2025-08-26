package com.dimetyd.bot.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.CBPOJobData;
import com.dimetyd.bot.util.MissingInvoicePo_CommonExcelService;
import com.dimetyd.bot.util.PO_CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class MissingInvoicePoPage {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private PO_CommonUtil pO_CommonUtil;

	@Autowired
	MissingInvoicePo_CommonExcelService commonService;
	final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
	final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");

	public boolean processPage(Page page, String vendorName, Integer year, Long id, String vendorId, String month,
			Path downloadPath, Long requestId,Date startDate , Date endDate) {
		System.out.println("ProcessPage in thread: " + Thread.currentThread().getName());

		try {
			String vName = vendorName.substring(0, 2);
			File purchaseOrderItem = new File(downloadPath + "PurchaseOrderItems.xlsx");
			if (purchaseOrderItem.exists()) {
				purchaseOrderItem.delete();
			}
			File orderHistory = new File(downloadPath + "OrderHistory.xlsx");
			if (orderHistory.exists()) {
				orderHistory.delete();
			}
			String yearString = Integer.toString(year).trim();
			logger.info("Year String : " + yearString);
			while (true) {
				try {
					page.click("//div[@aria-label='Navigation menu']");
					break;
				} catch (Exception e) {
					page.reload();
					Thread.sleep(3000);
				}

			}
			while (true) {
				try {
					page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Orders')]");
					break;
				} catch (Exception e) {
					page.reload();
					Thread.sleep(3000);

				}
			}
			while (true) {
				try {
					page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Purchase Orders')]");
					break;
				} catch (Exception e) {
					page.reload();
					Thread.sleep(3000);

				}
			}

			try {
				page.click("//kat-navigation[@class='navigation-bar']//a[@slot and contains(text(),'PO History')]");
			} catch (Exception e) {
				page.reload();
				page.click("//kat-navigation[@class='navigation-bar']//a[@slot and contains(text(),'PO History')]");
			}

			List<Locator> years = page.locator("div.card.card-body.border-info div.form-check input[name='yearGroup']")
					.all();

			List<Locator> yearMonthQuarter = page.locator(
					("//div[@class='card-group']//div[@class='card card-body border-info']//label[@class='form-check-label']"))
					.all();

			boolean inLoopFlag = false;
			for (Locator yearPresent : years) {
				String yearFromSite = null;
				try {
					yearFromSite = yearPresent.getAttribute("id").trim();
				} catch (Exception e) {
					logger.info("Stale element for year");
				}
				logger.info("yearPresent :" + yearFromSite);
				if (yearFromSite != null && yearString.trim().equalsIgnoreCase(yearFromSite)) {
					yearPresent.click();

					for (Locator Month : yearMonthQuarter) {
						if (month.trim().equalsIgnoreCase(Month.innerText().trim())) {
							page.locator("//div[@class='card-title']//input[@type='checkbox']").click();
							page.locator("//div[@class='card-title']//input[@type='checkbox']").click();
							logger.info("Month " + month);
							Month.click();

							inLoopFlag = true;

							Integer Limit = pO_CommonUtil.searchResults(page);
							logger.info("Limit : " + Limit);

							if (Limit == 0) {
								logger.info("Limit is 0");
								return true;
							}
							if (Limit > 1000) {

								downloadOrderHistoryFile(page, id, vendorId, month, yearString, Limit, vName,
										downloadPath,startDate,endDate);

								while (true) {
									page.click(
											"//div[@id='orderHistory']//div[@id='FilterButton']//button[@class='btn']");

									List<Locator> search = page
											.locator(
													"//div[@class='Select-control']//div[@class='Select-input']//input")
											.all();
									logger.info("select PO from CBMissingPOData where vendorId='" + vendorId
											+ "' and month='" + month + "' and year='" + yearString
											+ "' and status='PENDING' LIMIT 100");
									StringBuilder sql = new StringBuilder(
											"select PO from CBMissingPOData where vendorId='" + vendorId
													+ "' and month='" + month + "' and year='" + yearString
													+ "' and status='PENDING' LIMIT 100");
									List<CBPOJobData> poList = this.jdbcTemplate.query(sql.toString(),
											new RowMapper<CBPOJobData>() {
												@Override
												public CBPOJobData mapRow(ResultSet rs, int rowNum)
														throws SQLException {
													CBPOJobData res = new CBPOJobData();
													res.setPo(rs.getString("PO"));
													return res;
												}
											}, new Object[] {});

									logger.info("List PO Size : " + poList.size());
									if (poList.size() == 0) {
										break;
									}
									for (CBPOJobData po : poList) {
										logger.info("data " + po.getPo());
										search.get(0).fill(po.getPo());
										search.get(0).press("Enter");

									}

									page.click(
											"//div[@class='react-grid-HeaderCell react-grid-HeaderCell--frozen']//label[@for='select-all-checkbox']");
									page.click("//kat-button[@label='Open selected POs']");
									try {
										Thread.sleep(7000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									while (true) {

										Locator locator = page.locator(
												"//div[@class='react-grid-HeaderCell-sortable' and contains(text(), 'ASIN')]");
										if (locator.count() > 0) {
											break;
										}

									}
									page.click("kat-dropdown-button[variant='primary'] .indicator");
									Download download = page.waitForDownload(() -> {
										page.click("kat-dropdown-button[variant='primary'] .option");
									});// Wait until the download starts
									if (!Files.exists(downloadPath)) {
										Files.createDirectories(downloadPath); // Create directories if they do not
																				// exist
									}
									// Get the suggested filename and create a Path object for it
									Path filePath = downloadPath.resolve(download.suggestedFilename());
									System.out.println("Downloading file to: " + filePath);

									// Save the downloaded file to the specified path
									download.saveAs(filePath);

									//commonService.readPurchaseOrderFile(filePath, vendorName, vendorId, id, requestId);
									sql = new StringBuilder(
											"UPdate CBMissingPOData SET STATUS='COMPLETED' where vendorId='" + vendorId
													+ "' and Po IN(");
									for (CBPOJobData poupdatestaus : poList) {
										sql.append("'" + poupdatestaus.getPo() + "',");

									}
									String query = sql.substring(0, sql.length() - 1) + ");";
									try {
										logger.info("Query : " + query);
										jdbcTemplate.execute(query);
									} catch (Exception e) {
										e.printStackTrace();
									}

									page.click("//kat-button[@label='Return to Purchase Orders']");
									page.reload();
									Thread.sleep(5000);
									pO_CommonUtil.searchResults(page);
									Thread.sleep(1500);

								}
								break;
							} else {

								downloadOrderHistoryFile(page, id, vendorId, month, yearString, Limit, vName,
										downloadPath, startDate, endDate);
								page.click(
										"//div[@class='react-grid-HeaderCell react-grid-HeaderCell--frozen']//label[@for='select-all-checkbox']");
								page.click("//kat-button[@label='Open selected POs']");
								try {
									Thread.sleep(7000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								while (true) {

									Locator locator = page.locator(
											"//div[@class='react-grid-HeaderCell-sortable' and contains(text(), 'ASIN')]");
									if (locator.count() > 0) {
										break;
									}

								}
								page.click("kat-dropdown-button[variant='primary'] .indicator");
								Download download = page.waitForDownload(() -> {
									page.click("kat-dropdown-button[variant='primary'] .option");
								});// Wait until the download starts
								if (!Files.exists(downloadPath)) {
									Files.createDirectories(downloadPath); // Create directories if they do not exist
								}
								// Get the suggested filename and create a Path object for it
								Path filePath = downloadPath.resolve(download.suggestedFilename());
								System.out.println("Downloading file to: " + filePath);

								// Save the downloaded file to the specified path
								download.saveAs(filePath);

							//	commonService.readPurchaseOrderFile(filePath, vendorName, vendorId, id, requestId);
								break;
							}

						}
					}
				}
			}

			if (inLoopFlag) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void downloadOrderHistoryFile(Page page, Long id, String vendorId, String month, String yearString,
			Integer limit, String vName, Path downloadPath, Date startDate, Date endDate) {
		logger.info("select fileDownload from CbMissingInvoiceRequestDetails where id='" + id + "'");
		StringBuilder sql = new StringBuilder(
				"select fileDownload from CbMissingInvoiceRequestDetails where id='" + id + "'");
		List<CBPOJobData> jobList = this.jdbcTemplate.query(sql.toString(), new RowMapper<CBPOJobData>() {
			@Override
			public CBPOJobData mapRow(ResultSet rs, int rowNum) throws SQLException {
				CBPOJobData res = new CBPOJobData();
				res.setFileDownLoad(rs.getString("fileDownload"));
				return res;
			}
		}, new Object[] {});

		logger.info("fileDownLoad : " + jobList.get(0).getFileDownLoad());
		if (!jobList.isEmpty()
				&& (jobList.get(0).getFileDownLoad() == null || jobList.get(0).getFileDownLoad().equals("NO"))) {
			page.click("kat-dropdown-button[variant='primary'] .indicator");
			Download download = page.waitForDownload(() -> {
				page.click("kat-dropdown-button[variant='primary'] .option");
				jdbcTemplate.execute(
						"DELETE cb FROM `CBMissingInvoiePODetails` cb INNER JOIN CBMissingPOData  b ON (cb.PO=b.PO AND cb.vendorId=b.vendorId) WHERE b.`Order_Date` BETWEEN '"
								+ startDate + "' AND '" + endDate + "' AND b.vendorId = '" + vendorId
								+ "'");
				jdbcTemplate.execute("DELETE from CBMissingPOData  WHERE `Order_Date`  BETWEEN '"
						+ startDate + "' AND '" + endDate + "' AND vendorId = '" + vendorId + "'");
			});// Wait until the download starts
			if (!Files.exists(downloadPath)) {
				try {
					Files.createDirectories(downloadPath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Create directories if they do not exist
			}
			// Get the suggested filename and create a Path object for it
			Path filePath = downloadPath.resolve(download.suggestedFilename());
			System.out.println("Downloading file to: " + filePath);

			// Save the downloaded file to the specified path
			download.saveAs(filePath);
			//commonService.readOrderHistoryFile(id, vendorId, month, yearString, limit, vName, filePath);
		}

	}


}
