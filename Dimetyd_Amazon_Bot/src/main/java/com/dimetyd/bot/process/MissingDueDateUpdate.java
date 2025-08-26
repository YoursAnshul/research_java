package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.Invoice;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.SelectOption;

@Component
public class MissingDueDateUpdate {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private CommonUtil commonUtilobj;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public boolean processPage(Page page, String invoiceNumbers, String vendorId, String vendorName, Path downloadPath) {
		logger.info("Bot is Running in OLD UI");
		Download download = null;
		File tempFile = null;
		String vName = vendorName.substring(0, 2);

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

			page.waitForTimeout(1000);

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
				try {
					viewAllInvoices = page.locator("//a[contains(text(),'View all invoices')]");
					viewAllInvoices.click();
				} catch (Exception ex) {
					viewAllInvoices = page.locator("//a[contains(text(),'View all Invoices')]");
					viewAllInvoices.click();
				}
			}
			page.selectOption("//select [@name='search-criteria']", new SelectOption().setLabel("Invoice Number"));
			refreshPage(page);
			// page.waitForTimeout(1000);
			// page.click("//span[@id='date-range-option']");
			// page.waitForTimeout(1000);

			// page.click("//*[@id='a-popover-1']//ul/li/a[text()='Custom Date']");
			// logger.info("Choose Custome Date");
			Locator invoicceNumberfield = page.locator("//textarea[@id='invoice-number']");
			Locator serachButtonForInvoices = page.locator("#advancedSearchHarmonicForm-submit");
			refreshPage(page);
			invoicceNumberfield.fill(invoiceNumbers);
			// startDateForInvoices.click();
		
			try {
				serachButtonForInvoices.click();
				refreshPage(page);
			} catch (Exception e) {
				commonUtilobj.closePopup(page);

				serachButtonForInvoices.click();
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
						commonUtilobj.closePopup(page);
						continue;
					}
					Path filePath = downloadPath.resolve(download.suggestedFilename());
					System.out.println("Downloading file to: " + filePath);

					// Save the downloaded file to the specified path
					download.saveAs(filePath);
					tempFile = filePath.toFile();

					boolean fileExists = CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));

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
					commonUtilobj.closePopup(page);
				}
			}

			try {
				CommonUtil.updateTimeStamp();
			} catch (IOException e) {
				e.printStackTrace();
			}
			FileReader reader = new FileReader(tempFile);

			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
			    .withHeader("Marketplace", "Invoice Date", "Payment Due date", "Status", "Actual Paid Amount",
			                "Payee", "Creation Date", "Invoice number", "Price", "Any Deductions")
			    .withIgnoreHeaderCase()
			    .withTrim());

			SimpleDateFormat sdfUS = new SimpleDateFormat("MM/dd/yyyy");
			SimpleDateFormat sdfINT = new SimpleDateFormat("dd/MM/yyyy");

			java.util.Date date = null;
			 vName = vendorName.substring(0, 2);

			for (CSVRecord csvRecord : csvParser) {
			    if (csvRecord.getRecordNumber() == 1L) continue; // skip header

			    String invoiceNumber = csvRecord.get("Invoice number");
			    String dueDateStr = csvRecord.get("Payment Due date");

			    try {
			        if (vName.startsWith("CA") || vName.startsWith("US")) {
			            date = sdfUS.parse(dueDateStr);
			        } else {
			            date = sdfINT.parse(dueDateStr);
			        }

			        java.sql.Date sqlDueDate = new java.sql.Date(date.getTime());

			        String query = "UPDATE invoice_paymentDuedate SET paymentDueDate = '" + sqlDueDate +
			                       "' WHERE invoiceNumber = '" + invoiceNumber + "'";

			        logger.info("Executing query: " + query);
			        jdbcTemplate.execute(query);

			    } catch (Exception e) {
			        logger.error("Failed to process invoice: " + invoiceNumber, e);
			    }
			}

			csvParser.close();
			reader.close();

			
		


		

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

//			}

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