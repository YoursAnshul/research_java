package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.ShippmentDataOutput;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class ShipmentDataDetailPage {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public boolean processPage(Page page, String asn, String vendorId, Path downloadPath) {
		try {
			page.click("//div[@aria-label='Navigation menu']");

			try {
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Orders')]");
			} catch (Exception e) {
				page.reload();
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Orders')]");
			}
			page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Shipments')]");
			Locator asnInput = page.locator("kat-input[id='sqFilterInput'] input[type='text']");
			asnInput.fill(asn);
			Locator searchIcon = page.locator("#sqFilterInputIcon");
			searchIcon.click();
			Locator viewDetailsShadowHost = page.locator("//kat-dropdown-button[@class='sq-actions-button']");
			Locator viewDetails = viewDetailsShadowHost.locator("button[class='button']");
			viewDetails.click();
			Thread.sleep(25000);
			List<Locator> menuList = page
					.locator("//div[@id='sd-page-overview-column']//kat-label[@class='kat-label-dark-text']").all();
			String pickedUpDateValue = menuList.get(1).innerText();
			List<Locator> trackingIdList = page.locator(
					"//div[@id='sd-summary-tab-area-div']//div[@id='sq-sub-component-div']//kat-label[@class='kat-label-dark-text']")
					.all();
			StringBuilder trackingIds = new StringBuilder();
			if (trackingIdList.isEmpty()) {
				trackingIdList = page.locator(
						"//div[@id='sd-summary-tab-area-div']//kat-popover[@kat-aria-behavior='tooltip']//kat-label[@class='kat-label-dark-text']")
						.all();
				for (Locator tId : trackingIdList) {
					trackingIds.append(tId.textContent().trim());
					trackingIds.append(",");
				}
			} else {

				for (Locator tId : trackingIdList) {
					trackingIds.append(tId.textContent());
					trackingIds.append(",");
				}
			}
			String trackingId = trackingIds.substring(0, trackingIds.length() - 1);
			List<Locator> subMenuList = page
					.locator("//div[@class='sd-summary-rows']//kat-label[@class='kat-label-dark-text']").all();
			String carrier = subMenuList.get(4).innerText();
			Locator fileDownload = page.locator("//div[@class='sd-cli-download-export-div']");
			Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(120000), () -> {
				// Click with increased timeout
				fileDownload.click(new Locator.ClickOptions().setTimeout(120000));
			});
			Path filePath = downloadPath.resolve(download.suggestedFilename());
			System.out.println("Downloading file to: " + filePath);
			download.saveAs(filePath);
			File tempFile = filePath.toFile();
			FileReader reader = null;
			try {
				reader = new FileReader(tempFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			CSVParser csvParser = null;
			try {
//				csvParser = new CSVParser(reader,
//						CSVFormat.DEFAULT
//								.withHeader("Purchase order", "UPC, EAN, or ISBN", "Model number", "ASIN",
//										"Product description", "Carton (package) tracking number", "Unit count",
//										"Expiration date (perishable products only)", "Lot number", "ASIN or MSKU")
//								.withIgnoreHeaderCase().withTrim());
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
			System.out.println("Headers: " + headerMap.keySet());
			for (CSVRecord csvRecord : csvParser) {
				ShippmentDataOutput shippmentDataOutputObj = new ShippmentDataOutput();

//				if (csvRecord.getRecordNumber() == 1L) {
//					logger.info("GetRecoredNumber " + csvRecord.getRecordNumber());
//					continue;
//				}
				try {
					shippmentDataOutputObj.setpO(csvRecord.get(headerMap.get("purchase order")));
				} catch (NullPointerException | IllegalArgumentException e) {
					e.printStackTrace();
				}
				try {
					if (csvRecord.isMapped(headerMap.get("asin"))) {
						shippmentDataOutputObj.setaSIN(csvRecord.get(headerMap.get("asin")));
					} else if (csvRecord.isMapped(headerMap.get("msku"))) {
						// Assuming you want to use the same method to set the value
						shippmentDataOutputObj.setaSIN(headerMap.get("msku"));
					} else {
						// Handle the case where neither ASIN nor MSKU is present if needed
						System.out.println("Neither ASIN nor MSKU is present for record: " + csvRecord);
					}
				} catch (NullPointerException | IllegalArgumentException e) {
					e.printStackTrace();
				}
				try {
					shippmentDataOutputObj.setQty(Integer.parseInt(csvRecord.get(headerMap.get("unit count"))));
				} catch (NullPointerException | IllegalArgumentException e) {
					e.printStackTrace();
				}
				try {
					shippmentDataOutputObj
							.setUniqueKey(shippmentDataOutputObj.getpO() + shippmentDataOutputObj.getaSIN() + asn);
				} catch (NullPointerException | IllegalArgumentException e) {
					e.printStackTrace();
				}
				String poAsin = shippmentDataOutputObj.getpO() + shippmentDataOutputObj.getaSIN();
				try {
					jdbcTemplate.execute(
							"INSERT IGNORE INTO `ShippmentDataOutput` (`ASN`,`POASIN`,`PO`,`ASIN`,`PickupDate`,`Qty`,`Carrier`,`TrackingId`,`CreatedDate`,`uniqueKey`,vendorId) VALUES('"
									+ asn + "','" + poAsin + "','" + shippmentDataOutputObj.getpO() + "','"
									+ shippmentDataOutputObj.getaSIN() + "','" + pickedUpDateValue + "','"
									+ shippmentDataOutputObj.getQty() + "','" + carrier + "','" + trackingId
									+ "',NOW(),'" + shippmentDataOutputObj.getUniqueKey() + "','" + vendorId + "')");
					
					logger.info("Executing SQL Query: " + "INSERT IGNORE INTO `ShippmentDataOutput` (`ASN`,`POASIN`,`PO`,`ASIN`,`PickupDate`,`Qty`,`Carrier`,`TrackingId`,`CreatedDate`,`uniqueKey`,vendorId) VALUES('"
							+ asn + "','" + poAsin + "','" + shippmentDataOutputObj.getpO() + "','"
							+ shippmentDataOutputObj.getaSIN() + "','" + pickedUpDateValue + "','"
							+ shippmentDataOutputObj.getQty() + "','" + carrier + "','" + trackingId
							+ "',NOW(),'" + shippmentDataOutputObj.getUniqueKey() + "','" + vendorId + "')");
				} catch (DuplicateKeyException e) {
					jdbcTemplate.execute(
							"UPDATE ShippmentDataOutput SET `Qty` = `Qty` + '" + shippmentDataOutputObj.getQty()
									+ "' WHERE `uniqueKey` = '" + shippmentDataOutputObj.getUniqueKey() + "'");
				}

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
}
