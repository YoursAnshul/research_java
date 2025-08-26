package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.ShortageInvoice;
import com.dimetyd.bot.util.CommonUtil;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

@Component
public class InvoiceSummaryPage {

	private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	CommonUtil InvoiceData_CommonUtil;
	
	
	public static final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");

	public boolean processPage(Page page, Long id,  String vendorId, String vendorName,
			Date startDate, Date endDate, int createdBy, Path downloadPath) {
	/*  if (createdBy == 0) {
			return true;
		} else {     */
			Download download = null;
			File tempFile = null;
			String vName = vendorName.substring(0, 2);
			String startDateAsString = null;
			String endDateAsString = null;
			if (vName.startsWith("CA") || vName.startsWith("US")) {
				startDateAsString = formatterForUS.format(startDate);
				endDateAsString = formatterForUS.format(endDate);
			  /*  } else {  */
				startDateAsString = formatterForOthers.format(startDate);
				endDateAsString = formatterForOthers.format(endDate);
			//}
			try {
				for (int retry = 0; retry < 3; retry++) {
					try {
						page.click("//div[@aria-label='Navigation menu']");
						break;
					} catch (Exception e) {
						page.reload();
						Thread.sleep(3000);
					}
				}

				try {
					page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
				} catch (Exception e) {
					page.reload();
					page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
				}

				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Invoices')]");

				while (true) {
					Locator invoicePage = page.locator("#simar-app");
					if (invoicePage.count() > 0) {
						break;
					} else {
						Locator invoicePageWithOldUI = page.locator("//a[text()='View all Invoices']");
						if (invoicePageWithOldUI.count() > 0) {
							break;
						} else {
							page.reload();
							Thread.sleep(3000);
						}
					}
				}
				Locator viewAllInvoices = page.locator("//a[text()='View all Invoices']");
				if (viewAllInvoices.count() > 0) {
					viewAllInvoices.click();
				} else {
					Locator newUi = page.locator("//kat-badge[@class='link-hover']");
					newUi.click();
					viewAllInvoices = page.locator("//a[text()='View all Invoices']");
					viewAllInvoices.click();
				}
				refreshPage(page);
				Locator startDateForInvoicesParent = page.locator("#start-date-calendar");
				Locator startDateForInvoices = startDateForInvoicesParent.locator("#start-date");
				Locator endDateForInvoicesParent = page.locator("#end-date-calendar");
				Locator endDateForInvoices = endDateForInvoicesParent.locator("#end-date");
				Locator serachButtonForInvoices = page.locator("#advancedSearchHarmonicForm-submit");
				startDateForInvoices.click();
				refreshPage(page);
				startDateForInvoices.fill(startDateAsString);
				try {
					page.click("//h1[@class='a-size-extra-large a-spacing-top-micro a-text-bold']");// random click
				} catch (Exception e1) {
					InvoiceData_CommonUtil.closePopup(page);
					page.click("//h1[@class='a-size-extra-large a-spacing-top-micro a-text-bold']");// random click
				}

				endDateForInvoices.fill(endDateAsString);
				try {
					page.click("//h1[@class='a-size-extra-large a-spacing-top-micro a-text-bold']");// random click
				} catch (Exception e1) {
					InvoiceData_CommonUtil.closePopup(page);
					page.click("//h1[@class='a-size-extra-large a-spacing-top-micro a-text-bold']");// random click
				}

				try {
					serachButtonForInvoices.click();
					page.waitForTimeout(5000);
					refreshPage(page);
				} catch (Exception e) {
					InvoiceData_CommonUtil.closePopup(page);

					serachButtonForInvoices.click();
					page.waitForTimeout(5000);
					refreshPage(page);
				}

				File dir = downloadPath.toFile(); // Convert Path to File

				if (dir.isDirectory()) {
					for (File f : dir.listFiles()) {
						if (f.isFile() && f.getName().endsWith(".csv")) {
							logger.info("File Name " + f.getName());
							if (f.delete()) {
								logger.info("Deleted file " + f.getName());
							} else {
								logger.info("Failed to delete file " + f.getName());
							}
						}
					}
				} else {
					logger.info("Provided path is not a directory.");
				}

				Locator advancedSearch = page.locator("#advancedSearchExportAll");
				boolean flag = true;
				while (flag) {
					try {

						try {
							download = page.waitForDownload(() -> {
								advancedSearch.click();
							});
						} catch (PlaywrightException e) {
							System.out.println("Download timed out, retrying...");
							continue;
						} catch (Exception e) {
							InvoiceData_CommonUtil.closePopup(page);
							continue;
						}
						Path filePath = downloadPath.resolve(download.suggestedFilename());
						System.out.println("Downloading file to: " + filePath);

						// Save the downloaded file to the specified path
						download.saveAs(filePath);
						tempFile = filePath.toFile();

						boolean fileExists = InvoiceData_CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));

						if (fileExists) {
							System.out.println("File exists!");
						} else {
							System.out.println("File does not exist within the timeout period.");

						}
						if (fileExists) {
							flag = false;
						}

					} catch (Exception e) {
						e.printStackTrace();
						InvoiceData_CommonUtil.closePopup(page);
					}
				}

//				try {
//					boolean flag = true;
//					while (flag) {
//						try {
//							download = page.waitForDownload(() -> {
//								advancedSearch.click();
//							});
//						} catch (Exception e) {
//							CommonUtil.closePopup(page);
//							download = page.waitForDownload(() -> {
//								advancedSearch.click();
//							});
//						}
//						Path filePath = downloadPath.resolve(download.suggestedFilename());
//						System.out.println("Downloading file to: " + filePath);
//
//						// Save the downloaded file to the specified path
//						download.saveAs(filePath);
//						tempFile = filePath.toFile();
//
//						boolean fileExists = CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));
//
//						if (fileExists) {
//							System.out.println("File exists!");
//						} else {
//							System.out.println("File does not exist within the timeout period.");
//
//						}
//						if (fileExists) {
//							flag = false;
//						}
//					}
//
//				} catch (Exception e) {
//					try {
//						CommonUtil.closePopup(page);
//
//						boolean flag = true;
//						while (flag) {
//							try {
//								download = page.waitForDownload(() -> {
//									advancedSearch.click();
//								});
//							} catch (Exception e1) {
//								CommonUtil.closePopup(page);
//								download = page.waitForDownload(() -> {
//									advancedSearch.click();
//								});
//							}
//							Path filePath = downloadPath.resolve(download.suggestedFilename());
//							System.out.println("Downloading file to: " + filePath);
//
//							// Save the downloaded file to the specified path
//							download.saveAs(filePath);
//							tempFile = filePath.toFile();
//
//							boolean fileExists = CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));
//
//							if (fileExists) {
//								System.out.println("File exists!");
//							} else {
//								System.out.println("File does not exist within the timeout period.");
//
//							}
//							if (fileExists) {
//								flag = false;
//							}
//						}
//					} catch (Exception e1) {
//
//					}
//
//				}

				FileReader reader = null;
				try {
					reader = new FileReader(tempFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				CSVParser csvParser = null;
				try {
					csvParser = new CSVParser(reader,
							CSVFormat.DEFAULT.withHeader("Marketplace", "Invoice Date", "Payment Due date", "Status",
									"Actual Paid Amount", "Payee", "Creation Date", "Invoice number", "Price",
									"Any Deductions").withIgnoreHeaderCase().withTrim());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				SimpleDateFormat sdf1ForUS = new SimpleDateFormat("MM/dd/yyyy");
				SimpleDateFormat sdf1ForOthers = new SimpleDateFormat("dd/MM/yyyy");
				java.util.Date date = null;

				List<ShortageInvoice> invoiceList = new ArrayList<ShortageInvoice>();
				int count = 0;
				int limit = 100;
				for (CSVRecord csvRecord : csvParser) {
					ShortageInvoice invoice1 = new ShortageInvoice();

					if (csvRecord.getRecordNumber() == 1L) {
						logger.info("GetRecoredNumber " + csvRecord.getRecordNumber());
						continue;
					}

					if (count < limit) {
						count++;
						try {
							invoice1.setMarketplace(csvRecord.get("Marketplace"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							String invoiceDate = csvRecord.get("Invoice Date");
							try {
								if (vName.startsWith("CA") || vName.startsWith("US")) {
									date = sdf1ForUS.parse(invoiceDate);
								} else {
									date = sdf1ForOthers.parse(invoiceDate);
								}

							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							invoice1.setInvoiceDate(new java.sql.Date(date.getTime()));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							String paymentDueDate = csvRecord.get("Payment Due date");
							try {
								if (vName.startsWith("CA") || vName.startsWith("US")) {
									date = sdf1ForUS.parse(paymentDueDate);
								} else {
									date = sdf1ForOthers.parse(paymentDueDate);
								}
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							invoice1.setPaymentDueDate(new java.sql.Date(date.getTime()));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							invoice1.setStatus(csvRecord.get("Status"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							String invoiceAmount = csvRecord.get("Price");
							if (invoiceAmount.contains("€")) {
								invoiceAmount = InvoiceData_CommonUtil.replaceCurrency(invoiceAmount).trim();
								int amtCharatcerLength = invoiceAmount.length();
								if (amtCharatcerLength > 3) {
									char thirdLastChar = invoiceAmount.charAt(amtCharatcerLength - 3);
									if (thirdLastChar == ',') {
										invoiceAmount = invoiceAmount.substring(0, amtCharatcerLength - 3) + '.'
												+ invoiceAmount.substring(amtCharatcerLength - 2);
									}
									int lastDotIndex = invoiceAmount.lastIndexOf('.');

									if (lastDotIndex != -1) { // Check if a dot exists in the string
										invoiceAmount = invoiceAmount.replaceAll("\\.(?![^.]*$)", "");
										logger.info("Modified string: " + invoiceAmount);
									} else {
										logger.info("No dot found in the string.");
									}
								}
							}

							String orginialAmt = invoiceAmount.replaceAll("[^\\d.]", "")
									.replaceAll("[^\\n\\r\\t\\p{Print}]", "");
							logger.info("invoiceAmount : " + orginialAmt);
//			String amount = invoiceAmount.replace("$", "").replace("€", "").replace("£", "")
//					.replace("MXN", "").replace("AED", "").replaceAll("[^\\n\\r\\t\\p{Print}]", "")
//					.replace(",", "");
							Double actualAmout = Double.parseDouble(orginialAmt);
							invoice1.setInvoiceAmount(actualAmout);
//			logger.info("InvoiceAmount : "+actualAmout);
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							invoice1.setPayee(csvRecord.get("Payee"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}

						try {
							String creationDate = csvRecord.get("Creation Date");
							try {
								if (vName.startsWith("CA") || vName.startsWith("US")) {
									date = sdf1ForUS.parse(creationDate);
								} else {
									date = sdf1ForOthers.parse(creationDate);
								}
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							invoice1.setInvoiceCreationDate(new java.sql.Date(date.getTime()));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							invoice1.setInvoiceNumber(csvRecord.get("Invoice number"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}

						try {
							invoice1.setAnyDeductions(csvRecord.get("Any Deductions"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						String uniqueKey = vendorId + invoice1.getInvoiceNumber();
						invoice1.setUniqueKey(uniqueKey);
						invoiceList.add(invoice1);

					} else {
						count = 0;
						try {
							invoice1.setMarketplace(csvRecord.get("Marketplace"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							String invoiceDate = csvRecord.get("Invoice Date");
							try {
								if (vName.startsWith("CA") || vName.startsWith("US")) {
									date = sdf1ForUS.parse(invoiceDate);
								} else {
									date = sdf1ForOthers.parse(invoiceDate);
								}
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							invoice1.setInvoiceDate(new java.sql.Date(date.getTime()));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							String paymentDueDate = csvRecord.get("Payment Due date");
							try {
								if (vName.startsWith("CA") || vName.startsWith("US")) {
									date = sdf1ForUS.parse(paymentDueDate);
								} else {
									date = sdf1ForOthers.parse(paymentDueDate);
								}
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							invoice1.setPaymentDueDate(new java.sql.Date(date.getTime()));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							invoice1.setStatus(csvRecord.get("Status"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							String invoiceAmount = csvRecord.get("Price");
							if (invoiceAmount.contains("€")) {
								invoiceAmount = InvoiceData_CommonUtil.replaceCurrency(invoiceAmount).trim();
								int amtCharatcerLength = invoiceAmount.length();
								if (amtCharatcerLength > 3) {
									char thirdLastChar = invoiceAmount.charAt(amtCharatcerLength - 3);
									if (thirdLastChar == ',') {
										invoiceAmount = invoiceAmount.substring(0, amtCharatcerLength - 3) + '.'
												+ invoiceAmount.substring(amtCharatcerLength - 2);
									}
									int lastDotIndex = invoiceAmount.lastIndexOf('.');

									if (lastDotIndex != -1) { // Check if a dot exists in the string
										invoiceAmount = invoiceAmount.replaceAll("\\.(?![^.]*$)", "");
										logger.info("Modified string: " + invoiceAmount);
									} else {
										logger.info("No dot found in the string.");
									}
								}
							}

							String orginialAmt = invoiceAmount.replaceAll("[^\\d.]", "")
									.replaceAll("[^\\n\\r\\t\\p{Print}]", "");
							logger.info("invoiceAmount : " + orginialAmt);
//			String amount = invoiceAmount.replace("$", "").replace("€", "").replace("£", "")
//					.replace("MXN", "").replace("AED", "").replaceAll("[^\\n\\r\\t\\p{Print}]", "")
//					.replace(",", "");
							Double actualAmout = Double.parseDouble(orginialAmt);
							invoice1.setInvoiceAmount(actualAmout);
//			logger.info("InvoiceAmount : "+actualAmout);
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							invoice1.setPayee(csvRecord.get("Payee"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}

						try {
							String creationDate = csvRecord.get("Creation Date");
							try {
								if (vName.startsWith("CA") || vName.startsWith("US")) {
									date = sdf1ForUS.parse(creationDate);
								} else {
									date = sdf1ForOthers.parse(creationDate);
								}
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							invoice1.setInvoiceCreationDate(new java.sql.Date(date.getTime()));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							invoice1.setInvoiceNumber(csvRecord.get("Invoice number"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}

						try {
							invoice1.setAnyDeductions(csvRecord.get("Any Deductions"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						String uniqueKey = vendorId + invoice1.getInvoiceNumber();
						invoice1.setUniqueKey(uniqueKey);
						invoiceList.add(invoice1);
						StringBuilder sb1 = new StringBuilder(
								"INSERT IGNORE INTO `POInvoiceInput`(`vendorId`,`createdDate`  ,`invoiceNumber`,`invoiceDate`,'' ,`invoiceTotalAmount`,`paymentDate`,`invoiceStatus`,`Ukey`,`Status`)\r\n"
								+ " VALUES");
						for (int row1 = 0; row1 < invoiceList.size(); row1++) {

							sb1.append("('" + vendorId + "','" + java.time.LocalDate.now() + "','"
									 + "','" + invoiceList.get(row1).getInvoiceNumber() + "','"
									+ invoiceList.get(row1).getInvoiceDate() + "','"
									+ invoiceList.get(row1).getInvoiceAmount() + "','"
									+ invoiceList.get(row1).getPaymentDueDate() + "','"
									+ invoiceList.get(row1).getStatus() + "','" + invoiceList.get(row1).getUniqueKey()
									+ "','PENDING')");

							sb1.append(",");
						}
						String query = sb1.substring(0, sb1.length() - 1) + ";";

						try {
							logger.info("Query " + query);
							jdbcTemplate.execute(query);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						invoiceList.clear();
					}

				}
				StringBuilder sb1 = new StringBuilder(
						"INSERT IGNORE INTO `POInvoiceInput`(`vendorId`,`createdDate`  ,`invoiceNumber`,`invoiceDate`,`invoiceTotalAmount`,`paymentDate`,`invoiceStatus`,`Ukey`,`Status`)\r\n"
								+ " VALUES");
						for (int row1 = 0; row1 < invoiceList.size(); row1++) {

							sb1.append("('" + vendorId + "','" + java.time.LocalDate.now()
									 + "','" + invoiceList.get(row1).getInvoiceNumber() + "','"
									+ invoiceList.get(row1).getInvoiceDate() + "','"
									+ invoiceList.get(row1).getInvoiceAmount() + "','"
									+ invoiceList.get(row1).getPaymentDueDate() + "','"
									+ invoiceList.get(row1).getStatus() + "','" + invoiceList.get(row1).getUniqueKey()
									+ "','PENDING')");
					sb1.append(",");
				}
				String query = sb1.substring(0, sb1.length() - 1) + ";";

				try {
					logger.info("Query " + query);
					jdbcTemplate.execute(query);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				reader.close();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			 }
			return false;
	
	}

	public void refreshPage(Page page) {
		while (true) {
			Locator invoiceSummaryPage = page.locator("#headerBreadCrumb");
			if (invoiceSummaryPage.count() > 0) {
				break;
			} else {
				page.reload();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
