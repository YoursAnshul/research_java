package com.dimetyd.bot.process;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.dimetyd.bot.util.PONewUiCommonExcelService;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

@Component
public class PoHistory_NewPage {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	PONewUiCommonExcelService commonService;
	@Autowired
	CommonUtil commonUtil;

	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");

	public boolean processPage(Page page, String vendorName, String reportingPeriod, Integer year, Long id,
			String vendorId, String month, Path downloadPath, Date startDate, Date endDate) {
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
			logger.info("MANAGE PO Clicked");

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
//				logger.info(
//				"DELETE CBPOHistoryData FROM `CBPOHistoryData` INNER JOIN CBPOData b ON (CBPOHistoryData.PO=b.PO AND CBPOHistoryData.vendorId=b.vendorId) WHERE b.`Order_Date` BETWEEN '"
//						+ startDate + "' AND '" + endDate + "' AND b.vendorId = '" + vendorId + "'");

				String deleteQuery = "DELETE  From `CBPOHistoryData`  WHERE `poOrderDate` BETWEEN '" + startDate
						+ "' AND '" + endDate + "' AND vendorId = '" + vendorId + "'";

				logger.info(deleteQuery);
				jdbcTemplate.execute(deleteQuery);

//
//		jdbcTemplate.execute("DELETE FROM `CBPOData` WHERE `Order_Date` BETWEEN '" + startDate + "' AND '"
//				+ endDate + "' AND vendorId = '" + vendorId + "'");
				commonService.readPurchaseOrderFile(filePath, vendorName, vendorId, id);
				logger.info("CBPODATA File click " + year);

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
