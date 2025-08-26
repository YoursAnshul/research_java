package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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

import com.dimetyd.bot.model.CBOperationalPerformance;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class OperationalChargeback_File_Download_Import {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private Logger logger = LoggerFactory.getLogger(getClass());

	// Define a map to hold date formats for different vendors
	private Map<String, DateTimeFormatter> dateFormatMap = new HashMap<>();

	public void FileDownload() {
		// Initialize date formats for different vendors
		dateFormatMap.put("DE", DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		dateFormatMap.put("CA", DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		dateFormatMap.put("FR", DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		dateFormatMap.put("GB", DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		dateFormatMap.put("IT", DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		dateFormatMap.put("ES", DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		dateFormatMap.put("BE", DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		dateFormatMap.put("US", DateTimeFormatter.ofPattern("MMM dd, yyyy"));
		dateFormatMap.put("AU", DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public String File_Download_Import(Page page, String id,String vendorId, String inStartDate, String inEndDate,
			String vendorName) {
		page.click("(//span[@class='a-button-inner'])[3]");
		page.waitForTimeout(2000);

		Locator isDataFound = page.locator("//div[@class='a-alert-content' and contains(text(),'No results')]");
		if (isDataFound.count() != 0) {
			logger.info("UPDATE CBRequestOperationalChargeBack SET COMMENT='NO Data Found' WHERE id='"+id+"'");
			 jdbcTemplate.execute("UPDATE CBRequestOperationalChargeBack SET COMMENT='NO Data Found' WHERE id='"+id+"'");
			logger.info("No Data Found");
			return "0 - 0 of 0 total transactions";
		} else {
			
			String LineItems=page.locator("//span[@id='pageNumberInfo']").innerText();
			
			
			logger.info("UPDATE CBRequestOperationalChargeBack SET COMMENT='"+LineItems+"' WHERE id='"+id+"'");
			  jdbcTemplate.execute("UPDATE CBRequestOperationalChargeBack SET COMMENT='"+LineItems+"' WHERE id='"+id+"'");
			while (true) {
				Locator isExportFileDownloadPage = page.locator("//h1[@id='chargebackListHeader']");
				if (isExportFileDownloadPage.count() > 0) {
					break;
				} else {
					logger.info("Page Reloaded");
					page.reload();
					page.waitForTimeout(2000);
				}
			}
			Download download = page.waitForDownload(() -> {
				System.out.println("After click apply download Excel");
				page.click("#exportToExcel");
			});

			// Handle file download
			try {
				// Sanitize filename and create file object
				String sanitizedFilename = download.suggestedFilename().replaceAll("[\\\\/:*?\"<>|]", "_");
				File tempFile = new File(
						Paths.get("C:\\Playwright File\\OPERATION_Files", sanitizedFilename).toString());

				logger.info("Attempting to save file: {}", tempFile.getAbsolutePath());
				download.saveAs(tempFile.toPath());
				logger.info("File downloaded successfully: {}", tempFile.getAbsolutePath());

				logger.info("DELETE FROM `CBOperationalPerformance_import` WHERE DATE(`creationDate`) BETWEEN '"
						+ inStartDate + "' AND  '" + inEndDate + "' " + "AND `vendorId`= '" + vendorId + "'");

				int deletedrows = jdbcTemplate
						.update("DELETE FROM `CBOperationalPerformance_import` WHERE DATE(`creationDate`) BETWEEN '"
								+ inStartDate + "' AND  '" + inEndDate + "' " + "AND `vendorId`= '" + vendorId + "'");
				logger.info("Deleted Rows: " + deletedrows);
				processDownloadedFile(tempFile, vendorId, inStartDate, inEndDate, vendorName);

				// Delete the file after processing
				if (tempFile.exists() && !tempFile.delete()) {
					logger.warn("Failed to delete temp file: {}", tempFile.getAbsolutePath());
				}

			} catch (NullPointerException e) {
				logger.error("File or download object was null", e);
			} catch (SecurityException e) {
				logger.error("Permission denied to delete file", e);
			} catch (Exception e) {
				logger.error("Unexpected error while saving or processing file", e);
			}
			return LineItems;
		}
	}

	private void processDownloadedFile(File tempFile, String vendorId, String inStartDate, String inEndDate,
			String vendorName) {
		String query = null;
		StringBuilder sb1 = new StringBuilder(
				"INSERT IGNORE INTO CBOperationalPerformance_import (vendorId, issueID, financialCharge, Quantity, vendorCode, issueType, creationDate, disputeBy, chargeInvoice, status, purchaseOrder , subtypenonCompliance, ASIN, fulfillmentCenter, createdDate, startDate, endDate, uniqueKey) VALUES ");
		FileReader reader = null;
		try {
			reader = new FileReader(tempFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		CSVParser csvParser = null;
		try {
			csvParser = new CSVParser(reader,
					CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, String> headerMap = new HashMap<>();
		for (String header : csvParser.getHeaderMap().keySet()) {
			headerMap.put(header.replaceAll("[^\\p{Print}]", "").trim(), header);
		}

		List<CBOperationalPerformance> invoiceList = new ArrayList<>();
		int count = 0;
		int limit = 100;

		for (CSVRecord csvRecord : csvParser) {
			CBOperationalPerformance invoice1 = new CBOperationalPerformance();

			if (count <= limit) {
				try {
					invoice1.setIssueID(csvRecord.get(headerMap.get("Issue ID")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setFinancialCharge(csvRecord.get(headerMap.get("Financial charge")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setQuantity(csvRecord.get(headerMap.get("Quantity")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setVendorCode(csvRecord.get(headerMap.get("Vendor code")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setIssueType(csvRecord.get(headerMap.get("Issue type")));
				} catch (NullPointerException e) {

				}

				// Vendor-specific date parsing
				try {
					String countryCode = vendorName.substring(0, 4);

					System.out.println("vName : " + countryCode);
					if (countryCode.equals("US -")) {
						DateTimeFormatter inputFormatter = dateFormatMap.getOrDefault(countryCode,
								DateTimeFormatter.ofPattern("MMM d, yyyy"));
						String creationDate = csvRecord.get(headerMap.get("Creation date"));
						logger.info("Creation date: " + creationDate);
						/*
						 * if(creationDate.contains("Sep")) { creationDate = creationDate.replace("Sep",
						 * "Sept"); }
						 */
						LocalDate date = LocalDate.parse(creationDate, inputFormatter);
						String formattedDate = date.format(outputFormatter);
						logger.info(formattedDate);
						invoice1.setCreationDate(formattedDate);
					} else {
						System.out.println("vName : " + countryCode);

						DateTimeFormatter inputFormatter = dateFormatMap.getOrDefault(countryCode,
								DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
						String creationDate = csvRecord.get(headerMap.get("Creation date"));
						logger.info("Creation date: " + creationDate);
						/*
						 * if(creationDate.contains("Sep")) { creationDate = creationDate.replace("Sep",
						 * "Sept"); }
						 */
						LocalDate date = LocalDate.parse(creationDate, inputFormatter);
						String formattedDate = date.format(outputFormatter);
						logger.info(formattedDate);
						invoice1.setCreationDate(formattedDate);
					}
				} catch (DateTimeParseException | NullPointerException e) {
					invoice1.setCreationDate(null);
				}

				try {
					invoice1.setDisputeBy(csvRecord.get(headerMap.get("Dispute By")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setChargeInvoice(csvRecord.get(headerMap.get("Charge Invoice #")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setStatus(csvRecord.get(headerMap.get("Status")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setPurchaseOrder(csvRecord.get(headerMap.get("Purchase order #")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setSubtypenonCompliance(csvRecord.get(headerMap.get("Sub-type of the non-compliance")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setASIN(csvRecord.get(headerMap.get("ASIN")));
				} catch (NullPointerException e) {

				}

				try {
					invoice1.setFulfillmentCenter(csvRecord.get(headerMap.get("Fulfillment center")));
				} catch (NullPointerException e) {
					try {
						invoice1.setFulfillmentCenter(csvRecord.get(headerMap.get("Fulfilment centre")));
					} catch (Exception ex) {

					}

				}

				invoice1.setUniqueKey(vendorId + invoice1.getPurchaseOrder() + invoice1.getIssueID());
				invoiceList.add(invoice1);
				count++;
			} else {
				count = 0;
				// Same processing for additional records here
				invoiceList.add(invoice1);
			}

			for (int row1 = 0; row1 < invoiceList.size(); row1++) {
				sb1.append("('" + vendorId + "','" + invoiceList.get(row1).getIssueID() + "','"
						+ invoiceList.get(row1).getFinancialCharge() + "','" + invoiceList.get(row1).getQuantity()
						+ "','" + invoiceList.get(row1).getVendorCode() + "','" + invoiceList.get(row1).getIssueType()
						+ "','" + invoiceList.get(row1).getCreationDate() + "','" + invoiceList.get(row1).getDisputeBy()
						+ "','" + invoiceList.get(row1).getChargeInvoice() + "','" + invoiceList.get(row1).getStatus()
						+ "','" + invoiceList.get(row1).getPurchaseOrder() + "','"
						+ invoiceList.get(row1).getSubtypenonCompliance() + "','" + invoiceList.get(row1).getASIN()
						+ "','" + invoiceList.get(row1).getFulfillmentCenter() + "','" + java.time.LocalDate.now()
						+ "','" + inStartDate + "','" + inEndDate + "','" + invoiceList.get(row1).getUniqueKey()
						+ "')");

				sb1.append(",");
			}
			query = sb1.substring(0, sb1.length() - 1) + ";";

			try {

				if (count == 100) {
					logger.info("Query " + query);
					jdbcTemplate.execute(query);
					logger.info("Batch Inserted");
					sb1 = new StringBuilder(
							"INSERT IGNORE INTO CBOperationalPerformance_import (vendorId, issueID, financialCharge, Quantity, vendorCode, issueType, creationDate, disputeBy, chargeInvoice, status, purchaseOrder , subtypenonCompliance, ASIN, fulfillmentCenter, createdDate, startDate, endDate, uniqueKey) VALUES ");
					count = 0;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			invoiceList.clear();
		}
		logger.info("Last Batch Query " + query);
		jdbcTemplate.execute(query);

		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
