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
public class MissingInvoicePoPage_New {
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
				page.click("//kat-navigation[@class='navigation-bar']//a[@slot and contains(text(),'Manage POs')]");
			} catch (Exception e) {
				page.reload();
				page.click("//kat-navigation[@class='navigation-bar']//a[@slot and contains(text(),'Manage POs')]");
			}
			logger.info("Manage PO Clicked");

			page.waitForTimeout(2000);
			page.click("kat-button-group[class='view-selector']>>kat-button[value='po-items']");
			page.waitForTimeout(1000);

			try {
				page.click("//*[@id=\"root\"]/div/div/div/div[3]/kat-tabs/kat-tab[3]");
			} catch (Exception e) {
				page.click("kat-tab[tab-id='closed'] div[class='closed']");

			}
			logger.info("Closed PO File clicked");
			page.waitForTimeout(2000);

			// page.waitForLoadState(LoadState.NETWORKIDLE);
			String startDateAsString = null;
			String endDateAsString = null;

			do {
				if (page.isVisible("kat-date-picker[class='date-picker-start'] input[type='text']")) {
					logger.info("startdate element is visible now");
					break;
				}
			} while (true);
			String DatePlaceHolder = page.locator("kat-date-picker[class='date-picker-start'] input[type='text']")
					.getAttribute("placeholder").toLowerCase();
			DatePlaceHolder = DatePlaceHolder.replace("mm", "MM");

			logger.info("Date Format : " + DatePlaceHolder);
			// Now use the placeholder format to format the date
			SimpleDateFormat outputFormat = new SimpleDateFormat(DatePlaceHolder);
			startDateAsString = outputFormat.format(startDate);
			endDateAsString = outputFormat.format(endDate);

			Locator fromDateParent = page.locator("kat-date-picker[class='date-picker-start'] input[type='text']");
			fromDateParent.fill(startDateAsString);

			Locator toDateParent = page.locator("kat-date-picker[class='date-picker-end'] input[type='text']");
			toDateParent.fill(endDateAsString);

			page.waitForTimeout(3000);
			try {
				page.locator("kat-button[class='submit-button']>>button").click();
			} catch (Exception ex) {
				logger.info("");
			}
			page.waitForTimeout(3000);
			Locator exportPoToExcel = page
					.locator("//kat-dropdown-button[@data-cy='manage-po-export-selection-dropdown-closedPosResults']");
			page.waitForTimeout(2000);

			if (exportPoToExcel.count() > 0) {

				/*Download download = page.waitForDownload(() -> {
					exportPoToExcel.click();
				});// Wait until the download starts*/
				Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(120000), () -> {
					
					
					// Click with increased timeout
					exportPoToExcel.click();
				});
					// exportPoToExcel.click(new Locator.ClickOptions().setTimeout(1000)); // 5 s
				logger.info("Clicked on Export To Excel.");

				do {
					boolean isSuccessExport = page.isVisible("//kat-alert[@variant='success']");
					if (isSuccessExport) {
						logger.info("Export is Ready to download.");
						page.waitForTimeout(2000);
						break;
					} else {
						logger.info("Waiting For Download..........");
						page.waitForTimeout(2000);
					}
				} while (true);

				if (!Files.exists(downloadPath)) {
					Files.createDirectories(downloadPath); // Create directories if they do not
															// exist
				}

				Path filePath = downloadPath.resolve(download.suggestedFilename());
				System.out.println("Downloading file to: " + filePath);
				download.saveAs(filePath);


				String deleteQuery = "DELETE  From `CBMissingInvoiePODetails`  WHERE `poOrderDate` BETWEEN '" + startDate
						+ "' AND '" + endDate + "' AND vendorId = '" + vendorId + "'";

				logger.info(deleteQuery);
				jdbcTemplate.execute(deleteQuery);


				commonService.readPurchaseOrderFile_missing(filePath, vendorName, vendorId, requestId);
				logger.info("Podata File click " + year);

				return true;

			}

			else {
				logger.info("Export File not Clicekd.");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}