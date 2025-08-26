package com.dimetyd.bot.process;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.opencsv.CSVReader;

@Component
public class FDReportDownload {

	@Autowired
	JdbcTemplate jdbctemplate;
	@Autowired
	CommonUtil commonobj;
	private String vendorName;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public String convertDateFormat_US(String CurrentDate) {
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// Parse the input string to a LocalDate
		LocalDate date = LocalDate.parse(CurrentDate, inputFormatter);

		DateTimeFormatter outputStartDate_US = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		// Format the date to the desired output format
		return date.format(outputStartDate_US);
	}

	public String convertDateFormat_Other(String CurrentDate) {
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDate date = LocalDate.parse(CurrentDate, inputFormatter);
		DateTimeFormatter outputEndDate_Other = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		return date.format(outputEndDate_Other);
	}

	public boolean navigateToPurchaseOrders(Page page, String startDate, String endDate, String vendorId,
			String vendorName, String downloadPath,String id) {

		String vName = vendorName.substring(0, 4);
		logger.info("vName: " + vName);

		page.locator("//img[@class='small nav-button-icon']").click();
		page.locator(".side-nav-tab-label").getByText("Payments").click();
		page.waitForTimeout(1000);
		page.locator(".flyout-menu-item-label").getByText("Financial Reports").click();
		page.waitForTimeout(1000);

		if (vName.equals("US -")) {
			logger.info("Changing Date format to US");
			String us_formattedStartDate = convertDateFormat_US(startDate);
			String us_formattedEndDate = convertDateFormat_US(endDate);
			logger.info("Formatted start date: " + us_formattedStartDate); // Output: Date in dd/MM/yyyy
			logger.info("Formatted end date: " + us_formattedEndDate); // Output: Date in dd/MM/yyyy

			page.locator("input#katal-id-3[aria-label='Invoice Date From*']").fill(us_formattedStartDate);
			page.waitForTimeout(1000);
			page.locator("input#katal-id-4[aria-label='Invoice Date To*']").fill(us_formattedEndDate);
			page.locator("input#katal-id-4[aria-label='Invoice Date To*']").press("Enter");

		} else {
			logger.info("Changing Date format for Other Countries");
			String other_formattedStartDate = convertDateFormat_Other(startDate);
			String other_formattedEndDate = convertDateFormat_Other(endDate);
			logger.info("Formatted start date: " + other_formattedStartDate); // Output: Date in MM/dd/yyyy
			logger.info("Formatted end date: " + other_formattedEndDate); // Output: Date in MM/dd/yyyy

			page.locator("input#katal-id-3[aria-label='Invoice Date From*']").fill(other_formattedStartDate);
			page.waitForTimeout(1000);
			page.locator("input#katal-id-4[aria-label='Invoice Date To*']").fill(other_formattedEndDate);
			page.locator("input#katal-id-4[aria-label='Invoice Date To*']").press("Enter");

		}

		// page.waitForTimeout(5000);
		page.locator("[data-testid='create_report_form_submit_button']").getByText("Create Report").click();
		page.waitForTimeout(10000); // Give some time for the report to be generated

		Path downloadDir = Paths.get(downloadPath);
		commonobj.deleteFileOnPath(downloadDir);

		if (!Files.exists(downloadDir)) {
			try {
				Files.createDirectories(downloadDir); // Create the directory if it doesn't exist
			} catch (IOException e) {
				System.err.println("Failed to create download directory: " + e.getMessage());
				return false;
			}
		}

		/*
		 * waitForDownloadAndClick(page, "kat-link[data-testid='download-button']",
		 * "span[slot='label']:has-text('In progress')", downloadPath,logger);
		 */

		String downloadButtonLocator = "kat-link[data-testid='download-button']";
		String inProgressIndicatorLocator = "span[slot='label']:has-text('In progress')";

		// Wait until "In Progress" is no longer visible
		Locator inProgressIndicator = page.locator(inProgressIndicatorLocator);

		// Check the visibility of the "In Progress" indicator
		while (inProgressIndicator.isVisible()) {
			logger.info("Waiting for 'In progress' to disappear...");
			page.waitForTimeout(10000); // Wait for 10 seconds before checking again
		}

		// After "In Progress" disappears, check available download buttons
		Locator availableDownloadButtons = page.locator(downloadButtonLocator);
		int buttonCount = availableDownloadButtons.count();
		logger.info("Number of available download buttons: " + buttonCount);

		if (buttonCount > 0) {
			// Wait for the download to complete
			Download download = page.waitForDownload(() -> {
				availableDownloadButtons.first().click(); // Click only the first download button
				logger.info("Clicked on the first download button.");
			});
			logger.info("File downloaded successfully....\n");

			try {
				// Get the downloaded file path
				Path downloadedFilePath = download.path(); // Get the path to the downloaded file
				if (downloadedFilePath != null) {
					// Construct the expected path for the downloaded file
					String fileName = "FDReport_File.csv"; // Assuming the file name ends with .csv
					Path expectedPath = Paths.get(downloadPath, fileName);

					// Move the file to the specified location
					// Files.move(downloadedFilePath, expectedPath);
					logger.info("Download complete. File saved at: " + expectedPath);
					download.saveAs(expectedPath);

					try (CSVReader reader = new CSVReader(new FileReader(expectedPath.toString()));
							Playwright playwright = Playwright.create()) {
						List<String[]> rows = null;
						String InsertFDReportsQuery = "INSERT ignore INTO `CBPODetailsFDReports_test`(`vendorId`,`PO`,`ASIN`,`POInvoice`,`POASIN`,`freightTerm`,`Qty`,`unitCost`,`currency`,`invoiceDate`,`parentInvoiceNumber`,`marketplace`,`payeeCode`,`vendorCode`,`externalID`,`invoiceCreationDate`,`invoiceStatus`,`hasPriceDiscrepancy`,`hasShortage`,`totalAmountExcludingTax`,`taxAmount`,`totalAmount`,`receivedQuantity`,`amountReceived`,`shortageQuantity`,`amountShortage`,`priceDiscrepancyAmount`,`amazonPaidCost`,`Ukey`) VALUES";

						try {
							// Read all rows from the CSV file
							rows = reader.readAll();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						int RowCount = 0;
						// Process each row (skip the header)
						int ExcelRowCount=rows.size()-1;
						logger.info("update financialReport_Input set comment='"+ExcelRowCount+"' where id='"+id+"'");
						jdbctemplate.execute(
								"update financialReport_Input set comment='"+ExcelRowCount+"' where id='"+id+"'");
						 int autoIncrementCounter = 1;
						for (int i = 1; i < ExcelRowCount; i++) {
							String[] row = rows.get(i);

							// Extracting values from the current row
							String invoiceNumber = row[0];
							String parentInvoiceNumber = row[1];
							String marketplace = row[2];
							String payeeCode = row[3];
							String vendorCode = row[4];
							String poNumber = row[5];
							String asin = row[6];
							String externalId = row[7];
							String freightTerm = row[8];
							String invoiceDate = row[9];
							String invoiceCreationDate = row[10];
							String invoiceStatus = row[11];
							String hasPriceDiscrepancy = row[12];
							String hasShortage = row[13];
							String currency = row[14];
							String costPrice = row[15];
							String invoiceQuantity = row[16];
							String totalAmountExcludingTax = row[17];
							String taxAmount = row[18];
							String totalAmount = row[19];
							String receivedQuantity = row[20];
							String amountReceived = row[21];
							String shortageQuantity = row[22];
							String amountShortage = row[23];
							String priceDiscrepancyAmount = row[24];
							String amazonPaidCost = row[25];

							String ukey = vendorId + poNumber + asin + invoiceNumber+ "_" + autoIncrementCounter;
							
							  // Increment the counter
						  autoIncrementCounter++;

							InsertFDReportsQuery = InsertFDReportsQuery + "('" + vendorId + "','" + poNumber + "','"
									+ asin + "','" + invoiceNumber + "','" + poNumber + asin + "','" + freightTerm
									+ "','" + invoiceQuantity + "','" + costPrice + "','" + currency + "','"
									+ invoiceDate + "','" + parentInvoiceNumber + "','" + marketplace + "','"
									+ payeeCode + "','" + vendorCode + "','" + externalId + "','" + invoiceCreationDate
									+ "','" + invoiceStatus + "','" + hasPriceDiscrepancy + "','" + hasShortage + "','"
									+ totalAmountExcludingTax + "','" + taxAmount + "','" + totalAmount + "','"
									+ receivedQuantity + "','" + amountReceived + "','" + shortageQuantity + "','"
									+ amountShortage + "','" + priceDiscrepancyAmount + "','" + amazonPaidCost + "','"
									+ ukey + "'),";

							if (RowCount == 500) {
								InsertFDReportsQuery = InsertFDReportsQuery.substring(0,
										InsertFDReportsQuery.length() - 1) + ";";

								// logger.info(InsertFDReportsQuery);
								try {
									jdbctemplate.execute(InsertFDReportsQuery);
									logger.info("Batch Inserted");
									RowCount = 0;
									InsertFDReportsQuery = "INSERT ignore INTO `CBPODetailsFDReports_test`(`vendorId`,`PO`,`ASIN`,`POInvoice`,`POASIN`,`freightTerm`,`Qty`,`unitCost`,`currency`,`invoiceDate`,`parentInvoiceNumber`,`marketplace`,`payeeCode`,`vendorCode`,`externalID`,`invoiceCreationDate`,`invoiceStatus`,`hasPriceDiscrepancy`,`hasShortage`,`totalAmountExcludingTax`,`taxAmount`,`totalAmount`,`receivedQuantity`,`amountReceived`,`shortageQuantity`,`amountShortage`,`priceDiscrepancyAmount`,`amazonPaidCost`,`Ukey`) VALUES";
									
								} catch (Exception ex) {
									logger.info("Error while inserting Fd data in Query");
									ex.printStackTrace();
									break;
								}

							} else {
								RowCount++;
							}

						}

						try {
							
							InsertFDReportsQuery = InsertFDReportsQuery.substring(0,
									InsertFDReportsQuery.length() - 1) + ";";
							jdbctemplate.execute(InsertFDReportsQuery);
							logger.info("Last Batch Inserted.......");
						} catch (Exception ex) {
							logger.info("Error while inserting Fd data in Last Batch Query");
							ex.printStackTrace();

						}

					} catch (IOException e) {
						e.printStackTrace();
					}

				} else {
					System.err.println("Download path is null, download might have failed.");
					return false;
				}
			} catch (Exception e) {
				System.err.println("Error saving the downloaded file: " + e.getMessage());
				return false;
			}
		} else {
			System.err.println("No download buttons found.\n");
			return false;
		}

		logger.info("File Downloaded Successfully........");
		return true;
	}
}