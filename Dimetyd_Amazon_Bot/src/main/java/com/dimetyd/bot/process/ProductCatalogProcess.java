package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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

import com.dimetyd.bot.model.ProductCatalog;
import com.dimetyd.bot.service.Product_Catalog_Service;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class ProductCatalogProcess {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	private static final Logger logger = LoggerFactory.getLogger(Product_Catalog_Service.class);

	public boolean navigateForecast(Page page, String vendorName, String vendorId) throws IOException {

		/*
		 * System.out.println("vendor Switch"); page.getByText(vendorName).click();
		 * System.out.println("vendor Selected");
		 */

		System.out.println("Now go through the navigation Reports Product Catalog");

		try {
			page.waitForTimeout(1000);
			System.out.println("click on Main menu");
			page.click("//*[@id=\"navbar\"]/div[1]/div[1]/div/img");
			System.out.println("click on Reports");
			page.click("//span[text()='Reports']");
			page.waitForTimeout(2000);

			page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Retail Analytics')]");
			page.waitForTimeout(2000);

			System.out.println("click on product catalog");
			page.click("//a[text()='Catalog' or text()='Catalogue']");

			page.waitForTimeout(2000);

			System.out.println("after product catalog click go through distributorView of productCatalog");

			boolean isVisible = page.isVisible("//a[text()='Catalog' or text()='Catalogue']");
			if (isVisible) {
				System.out.println("selector is visible now");

			} else {
				System.out.println("Selector not visible Refresh Page...........");
				page.reload();
				page.waitForTimeout(1000);

			}

			String[] dropdownItems = { "Manufacturing", "Sourcing" };

			for (String dropdownItem : dropdownItems) {
				System.out.println("Selecting: " + dropdownItem);
				page.click("#distributorView");
				page.waitForTimeout(2000);
				page.locator(".standard-option-content").getByText(dropdownItem).last().click();

				page.waitForTimeout(2000);

				page.locator(".button").getByText("Apply").click();

				page.locator(".button").getByText("csv").click();
				page.click("//a[text()='View and manage your downloads.']");

				IndexFiledownLoaddown(page, vendorId, dropdownItem);
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public void IndexFiledownLoaddown(Page page, String vendorId, String dropdown) throws IOException {
		Locator firstReport;
		while (true) {
			try {
				List<Locator> reportNameList = page
						.locator("//kat-table-body[@role='rowgroup']//kat-table-row[@role='row']").all();
				firstReport = reportNameList.get(0);
				break;
			} catch (Exception e) {
				// Retry if the locator is not ready
			}
		}

		Locator downloadAvailable = firstReport.locator("text='Download'");
		Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(200000), () -> {
			downloadAvailable.click(new Locator.ClickOptions().setTimeout(200000));
		});

		if (download != null) {
			System.out.println("Download started: " + download.path());
		} else {
			System.out.println("Download did not start.");
		}

		// Save downloaded file
		String downloadDirectory = "c:\\java codes\\Forecast_DownloadFiles";
		Path targetPath = Paths.get(downloadDirectory, download.suggestedFilename());
		download.saveAs(targetPath);
		System.out.println("Path " + targetPath);

		// Process CSV file
		File csvFile = targetPath.toFile();
		System.out.print(csvFile);
		try (FileReader reader = new FileReader(csvFile);
				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

			List<ProductCatalog> invoiceList = new ArrayList<>();
			int skipFirstRow = 0;

			// After download, click on the cancel button
			page.locator(".ltr-1upyiy5").click();

			int count = 0;
			int limit = 100;
			String startDate = null;
			String endDate = null;

			for (CSVRecord csvRecord : csvParser) {
				ProductCatalog invoice = new ProductCatalog();
				if (skipFirstRow == 0) {
					skipFirstRow++;
					continue;
				}
				// Using column indexing instead of names
				try {

					invoice.setASIN(csvRecord.get(0)); // ASIN
					invoice.setProductTitle(csvRecord.get(1)); // Product Title
					invoice.setManufacturerCode(csvRecord.get(2)); // manufacturerCode

					invoice.setParentASIN(csvRecord.get(3)); // parentASIN
					invoice.setUPC(csvRecord.get(4)); // UPC
					invoice.setEAN(csvRecord.get(5)); // EAN
					invoice.setISBN13(csvRecord.get(6)); // ISBN13
					invoice.setModelStyleNumber(csvRecord.get(7)); // modelStyleNumber
					invoice.setBrand(csvRecord.get(8)); // Brand
					invoice.setBrandCode(csvRecord.get(9)); // brandCode
					invoice.setProductGroup(csvRecord.get(12)); // productGroup
					invoice.setReleaseDate(csvRecord.get(13)); // releaseDate
					invoice.setReplenishmentCode(csvRecord.get(14)); // replenishmentCode
					invoice.setPrepInstructionsRequired(csvRecord.get(15)); // prepInstructionsRequired
					invoice.setPrepInstructionsVendorState(csvRecord.get(16)); // prepInstructionsVendorState

					// Use column names instead of indices

					/*
					 * invoice.setASIN(csvRecord.get("ASIN")); // ASIN
					 * invoice.setProductTitle(csvRecord.get("Product title")); // Product Title
					 * invoice.setManufacturerCode(csvRecord.get("Manufacturer Code")); //
					 * Manufacturer Code invoice.setParentASIN(csvRecord.get("Parent ASIN")); //
					 * Parent ASIN invoice.setUPC(csvRecord.get("UPC")); // UPC
					 * invoice.setEAN(csvRecord.get("EAN")); // EAN
					 * invoice.setISBN13(csvRecord.get("ISBN-13")); // ISBN-13
					 * invoice.setModelStyleNumber(csvRecord.get("Model number")); // Model/Style
					 * Number invoice.setBrand(csvRecord.get("Brand")); // Brand
					 * invoice.setBrandCode(csvRecord.get("Brand Code")); // Brand Code
					 * invoice.setProductGroup(csvRecord.get("Product Group")); // Product Group
					 * invoice.setReleaseDate(csvRecord.get("Release date")); // Release Date
					 * invoice.setReplenishmentCode(csvRecord.get("Replenishment Category")); //
					 * Replenishment Code invoice.setPrepInstructionsRequired(csvRecord.
					 * get("Prep Instructions Required")); // Prep Instructions Required
					 * invoice.setPrepInstructionsVendorState(csvRecord.
					 * get("Prep Instructions Vendor State")); // Prep Instructions Vendor State
					 */} catch (IndexOutOfBoundsException e) {
					logger.error("Column index out of bounds. Please check the CSV structure.", e);
				}

				invoice.setUniqueKey(invoice.getASIN() + vendorId + dropdown); // Unique Key
				invoiceList.add(invoice);

				if (count == limit) {
					count = 0;
					saveToDatabase(invoiceList, vendorId, dropdown, startDate, endDate); // Pass drop down(statView)
					invoiceList.clear();
				} else {
					count++;
				}
			}

			// Save remaining records
			if (!invoiceList.isEmpty()) {
				saveToDatabase(invoiceList, vendorId, dropdown, null, null); // Pass dropdown (statView)
			}

		} catch (IOException e) {
			logger.error("Error processing CSV file", e);
		}
		// Delete the file after processing
		if (targetPath != null && Files.exists(targetPath)) {
			try {
				Files.delete(targetPath);
				System.out.println("File deleted: " + targetPath);
			} catch (IOException e) {
				logger.error("Failed to delete file: " + targetPath, e);
			}
		}
	}

	private void saveToDatabase(List<ProductCatalog> invoiceList, String vendorId, String dropdown, String startDate,
			String endDate) {
		StringBuilder sb = new StringBuilder(
				"INSERT IGNORE  INTO `ProductCatalog`(`vendorId`,`ASIN`,`productTitle`,`manufacturerCode`,`parentASIN`,`UPC`,`EAN`,`ISBN13`,`modelStyleNumber`,`Brand`,`brandCode`,`productGroup`,`releaseDate`,`replenishmentCode`,`prepInstructionsRequired`,`prepInstructionsVendorState`,`distibuterView`,`UniqueKey`,`createdDate`) VALUES");

		for (ProductCatalog invoice : invoiceList) {
			sb.append("('").append(cleanValue(vendorId)).append("', '").append(cleanValue(invoice.getASIN()))
					.append("', '").append(cleanValue(invoice.getProductTitle())).append("', '")
					.append(cleanValue(invoice.getManufacturerCode())).append("', '")
					.append(cleanValue(invoice.getParentASIN())).append("', '").append(cleanValue(invoice.getUPC()))
					.append("', '").append(cleanValue(invoice.getEAN())).append("', '")
					.append(cleanValue(invoice.getISBN13())).append("', '")
					.append(cleanValue(invoice.getModelStyleNumber())).append("', '")
					.append(cleanValue(invoice.getBrand())).append("', '").append(cleanValue(invoice.getBrandCode()))
					.append("', '").append(cleanValue(invoice.getProductGroup())).append("', '")
					.append(cleanValue(invoice.getReleaseDate())).append("', '")
					.append(cleanValue(invoice.getReplenishmentCode())).append("', '")
					.append(cleanValue(invoice.getPrepInstructionsRequired())).append("', '")
					.append(cleanValue(invoice.getPrepInstructionsVendorState())).append("', '")
					.append(cleanValue(dropdown)).append("', '").append(invoice.getUniqueKey()).append("', '")
					.append(LocalDate.now()).append("'), ");
			System.out.print("Inserted query is" + sb);
		}

		String sql = sb.substring(0, sb.length() - 2); // Remove last comma
		jdbcTemplate.update(sql); // Execute the SQL query

	}

	private String cleanValue(String value) {
		return value == null ? "" : value.replace("'", "").replace(",", "");
	}

}