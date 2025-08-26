package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class InvoiceSummaryNewUi {

	private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
    public static final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");


	public boolean processPage(Page page, Long id, String vendorId, String vendorName, String startDate, String endDate,
			int createdBy, Path downloadPath) throws InterruptedException {

		try {
			Download download = null;
			File tempFile = null;
			String vName = vendorName.substring(0, 2);
		     String startDateAsString = null;
	            String endDateAsString = null;
	            Date startdateASDate = inputFormat.parse(startDate);
			    Date endDateAsDate = inputFormat.parse(endDate);
	            if (vName.startsWith("CA") || vName.startsWith("US")) {
	                startDateAsString = formatterForUS.format(startdateASDate);
	                endDateAsString = formatterForUS.format(endDateAsDate);
	            } else {
	                startDateAsString = formatterForOthers.format(startdateASDate);
	                endDateAsString = formatterForOthers.format(endDateAsDate);
	            }

			
			/*String startDateAsString = null;
			String endDateAsString = null;
			if (vName.startsWith("CA") || vName.startsWith("US")) {
				startDateAsString = formatterForUS.format(startDate);
				endDateAsString = formatterForUS.format(endDate);
			} else {
				startDateAsString = formatterForOthers.format(startDate);
				endDateAsString = formatterForOthers.format(endDate);
			}*/

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

			Locator oldUi = page.locator("//div[@class='a-alert-content']//button");
			if (oldUi.count() > 0) {
				oldUi.click();
			}

			reFreshPage(page);

			Locator invoiePageLoaded = page.locator("#invoices-label");
			if (invoiePageLoaded.count() == 0) {
				page.reload();
			}

			try {
				try {
					page.click("#input-box");
				} catch (Exception e) {
					try {
						Locator parentLocator = page.locator("#hmd2f-trigger-tab");

						Locator cancelButtonLocator = parentLocator.locator(".vc-footer-hmd-feedback-link");

						cancelButtonLocator.click();
					} catch (Exception e1) {

					}
					page.click("#input-box");
				}
				page.waitForTimeout(1500);
				Locator startDateInvoice = page.locator("#search-date-range-container>>input[aria-label='Start date']");
				startDateInvoice.fill(startDateAsString);
				invoiePageLoaded.click();
				page.waitForTimeout(1500);
				Locator endDateInvoice = page.locator("#search-date-range-container>>input[aria-label='End date']");
				endDateInvoice.fill(endDateAsString);
				invoiePageLoaded.click();
				page.waitForTimeout(1500);
				Locator elementToScroll = page.locator("#download-csv");
				try {
					elementToScroll.scrollIntoViewIfNeeded(); // Scroll into view if needed
				} catch (PlaywrightException e) {
					// Handle the case where the element is not found
					Locator alternativeElement = page.locator("#download-csv-details");
					alternativeElement.scrollIntoViewIfNeeded(); // Scroll the alternative element into view
				}
				download = page.waitForDownload(() -> {
					try {
						page.click("#download-csv");// download Btn
					} catch (PlaywrightException e) {
						page.click("#download-csv-details");
					} catch (Exception e) {
						try {
							Locator parentLocator = page.locator("#hmd2f-trigger-tab");
							Locator cancelButtonLocator = parentLocator.locator(".vc-footer-hmd-feedback-link");
							cancelButtonLocator.click();
						} catch (Exception e1) {

						}
						try {
							page.click("#download-csv");// download Btn

						} catch (PlaywrightException e1) {
							page.click("#download-csv-details");
						}
					}
					try {
						// Locate the error message element and get its text
						Locator errorElement = page.locator("//div[@id='download-csv']//p[@class='left-align']");
						String errorText = errorElement.innerText();

						// Check if the error message matches the expected text
						if (errorText.equalsIgnoreCase("Error Occurred. Please Try Again Later.")) {
							// Locate the close icon inside the shadow DOM
							page.click("[class='close']");
							Locator searchIcon = page.locator("//kat-icon[@name='search']");
							searchIcon.click();
							Locator downloadBtn = page.locator("#download-csv");
							if (downloadBtn.isVisible()) {
								downloadBtn.click();
							} else {
								// If the download button is not found, try the alternative button
								Locator downloadBtnAlt = page.locator("#download-csv-details");
								if (downloadBtnAlt.isVisible()) {
									downloadBtnAlt.click();
								}
							}
						}
					} catch (PlaywrightException e) {
						// Handle the case where the error element or other elements are not found
						System.out.println("An error occurred: " + e.getMessage());
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				return false;

			}
			page.click("[class='close']");
			Path filePath = downloadPath.resolve(download.suggestedFilename());
			System.out.println("Downloading file to: " + filePath);
			download.saveAs(filePath);
			tempFile = filePath.toFile();

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
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String, String> headerMap = new HashMap<>();
			for (String header : csvParser.getHeaderMap().keySet()) {
				headerMap.put(header.replaceAll("[^\\p{Print}]", "").trim().toLowerCase(), header);
			}
			SimpleDateFormat sdf1ForUS = new SimpleDateFormat("MM/dd/yyyy");
			SimpleDateFormat sdf1ForOthers = new SimpleDateFormat("dd/MM/yyyy");
			java.util.Date date = null;

			List<ShortageInvoice> invoiceList = new ArrayList<ShortageInvoice>();
			int count = 0;
			int limit = 100;
			for (CSVRecord csvRecord : csvParser) {
				ShortageInvoice invoice1 = new ShortageInvoice();
				if (count < limit) {
					count++;
					try {
						invoice1.setMarketplace(csvRecord.get(0));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						String invoiceDate = csvRecord.get(headerMap.get("invoice date"));
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
						String paymentDueDate = csvRecord.get(headerMap.get("payment due date"));
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
						invoice1.setStatus(csvRecord.get("invoice status"));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						String invoiceAmount = csvRecord.get(headerMap.get("invoice amount"));
						invoice1.setInvoiceAmount(Double.parseDouble(invoiceAmount));

					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						invoice1.setPayee(csvRecord.get("payee code"));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}

					try {
						String creationDate = csvRecord.get("invoice creation date");
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
						invoice1.setInvoiceNumber(csvRecord.get("invoice number"));
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
						invoice1.setMarketplace(csvRecord.get(0));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						String invoiceDate = csvRecord.get(headerMap.get("invoice date"));
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
						String paymentDueDate = csvRecord.get(headerMap.get("payment due date"));
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
						invoice1.setStatus(csvRecord.get("invoice status"));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						String invoiceAmount = csvRecord.get(headerMap.get("invoice amount"));
						invoice1.setInvoiceAmount(Double.parseDouble(invoiceAmount));

					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						invoice1.setPayee(csvRecord.get("payee code"));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}

					try {
						String creationDate = csvRecord.get("invoice creation date");
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
						invoice1.setInvoiceNumber(csvRecord.get("invoice number"));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					String uniqueKey = vendorId + invoice1.getInvoiceNumber();
					invoice1.setUniqueKey(uniqueKey);
					invoiceList.add(invoice1);
					StringBuilder sb1 = new StringBuilder(
							"INSERT IGNORE INTO `POInvoiceInput`(`vendorId`,`createdDate`,`invoiceNumber`,`invoiceDate`,`invoiceTotalAmount`,`paymentDate`,`invoiceStatus`,`Ukey`,`Status`)\r\n"
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
					invoiceList.clear();
				}

			}
			StringBuilder sb1 = new StringBuilder(
					"INSERT IGNORE INTO `POInvoiceInput`(`vendorId`,`createdDate`,`invoiceNumber`,`invoiceDate`,`invoiceTotalAmount`,`paymentDate`,`invoiceStatus`,`Ukey`,`Status`)\r\n"
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

			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tempFile.delete();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void reFreshPage(Page page) {
		while (true) {

			Locator locatorError = page.locator("//div[@class='a-box a-alert a-alert-error a-spacing-top-mini']");
			if (locatorError.count() > 0) {
				page.reload();
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				logger.info("NoSuchElemenExcpn in a-box a-alert a-alert-error a-spacing-top-mini page ");
				locatorError = page.locator("mons-error-page-template");
				if (locatorError.count() > 0) {
					page.reload();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Locator pageLoaded = page.locator("#input-box");
					if (pageLoaded.count() > 0) {
						logger.info("Page properly Loaded");
						break;
					} else {
						Locator previousUiPageLaoded = page.locator("text='View all Invoices'");
						if (previousUiPageLaoded.count() > 0) {
							logger.info("Previous UI Page properly Loaded");
							try {
								page.click("//div[@class='a-alert-content']//button");// got to new UI click btn
							} catch (PlaywrightException e) {

							}
						} else {
							page.reload();
							try {
								Thread.sleep(1500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}

				}
			}
		}
	}

}
