package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
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

import com.dimetyd.bot.model.ShipmentBotInput;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class ShipmentDataPage {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public boolean processPage(Page page, String vendorId, String vendorName, Path downloadPath) {
		try {
			page.click("//div[@aria-label='Navigation menu']");

			try {
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Orders')]");
			} catch (Exception e) {
				page.reload();
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Orders')]");
			}
			page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Shipments')]");
			Locator downloadOrExportBtn = page.locator("//kat-link[@label='Download/export data']");
			 page.waitForTimeout(10000);
			
			if (downloadOrExportBtn.count() > 0) {
				downloadOrExportBtn.click();
			} else {
				downloadOrExportBtn = page.locator("//kat-link[@label='Download or export data']");
				downloadOrExportBtn.click();
			}

			Locator dateRange = page.locator("#num-days-dd");
			dateRange.click();
			Locator ninentyDays = dateRange.locator("kat-option[value='30']");
			ninentyDays.click();
			Locator downloadBtn = page.locator("//kat-button[@label='Download']");

			Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(120000), () -> {
				// Click with increased timeout
				downloadBtn.click(new Locator.ClickOptions().setTimeout(120000));
			});

			// Handle the download object if needed
			if (download != null) {
				System.out.println("Download started: " + download.path());
			} else {
				System.out.println("Download did not start.");
			}
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
//						CSVFormat.DEFAULT.withHeader("Shipment ID (ARN)", "ASN #", "Creation date", "Status",
//								"Date/detail", "Alert", "POs", "Stacked pallets", "Unstacked pallets", "Cartons",
//								"Carrier SCAC", "Carrier name", "Mode", "Ship from", "Ship to", "Reference ID", "VRID",
//								"Bill of lading").withIgnoreHeaderCase().withTrim());
				csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader() // Specify the first
																								// record as the header
						.withIgnoreHeaderCase().withTrim());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  Map<String, String> headerMap = new HashMap<>();
		        for (String header : csvParser.getHeaderMap().keySet()) {
		            headerMap.put(header.replaceAll("[^\\p{Print}]", "").trim().toLowerCase(), header);//[^\\p{Print}]==Any character that is not printable. This includes:Control characters (like newline \n, carriage return \r, tab \t, etc.) Any other non-visible characters that might accidentally be present in your input data.
		        }
		        System.out.println("Headers: " + headerMap.keySet());
			List<ShipmentBotInput> shipmentBotInputList = new ArrayList<ShipmentBotInput>();
			int count = 0;
			int limit = 100;
			for (CSVRecord csvRecord : csvParser) {
				ShipmentBotInput shipmentBotInputObj = new ShipmentBotInput();

//				if (csvRecord.getRecordNumber() == 1L) {
//					logger.info("GetRecoredNumber " + csvRecord.getRecordNumber());
//					continue;
//				}

				if (count < limit) {
					count++;
					try {
						shipmentBotInputObj.setaRN(csvRecord.get(headerMap.get("shipment id (arn)")));//""
					} catch (NullPointerException | IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						shipmentBotInputObj.setaSN(csvRecord.get("asn #"));
					} catch (NullPointerException | IllegalArgumentException e) {
						try {
							shipmentBotInputObj.setaSN(csvRecord.get("asn number"));
						} catch (NullPointerException | IllegalArgumentException e1) {
							e.printStackTrace();
						}
					}
					try {
						shipmentBotInputObj.setaSNStatus(csvRecord.get("status"));
					} catch (NullPointerException | IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						shipmentBotInputObj.setDate_Details(csvRecord.get("date/detail"));
					} catch (NullPointerException | IllegalArgumentException e) {
						try {
							shipmentBotInputObj.setDate_Details(csvRecord.get("date or detail"));
						} catch (NullPointerException | IllegalArgumentException e1) {
							e.printStackTrace();
						}
					}
					try {
						shipmentBotInputObj.setpOs(csvRecord.get("pos"));
					} catch (NullPointerException | IllegalArgumentException e) {
						try {
							shipmentBotInputObj.setpOs(csvRecord.get("purchase orders"));
						} catch (NullPointerException | IllegalArgumentException e1) {
							e.printStackTrace();
						}

					}
					try {
						shipmentBotInputObj.setShipfrom(csvRecord.get("ship from"));
					} catch (NullPointerException | IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						shipmentBotInputObj.setUniqueKey(shipmentBotInputObj.getaSN() + shipmentBotInputObj.getaRN());
					} catch (NullPointerException | IllegalArgumentException e) {
						e.printStackTrace();
					}
					shipmentBotInputList.add(shipmentBotInputObj);

				} else {
					count = 0;
					try {
						shipmentBotInputObj.setaRN(csvRecord.get(headerMap.get("shipment id (arn)")));//""
					} catch (NullPointerException | IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						shipmentBotInputObj.setaSN(csvRecord.get("asn #"));
					} catch (NullPointerException | IllegalArgumentException e) {
						try {
							shipmentBotInputObj.setaSN(csvRecord.get("asn number"));
						} catch (NullPointerException | IllegalArgumentException e1) {
							e.printStackTrace();
						}
					}
					try {
						shipmentBotInputObj.setaSNStatus(csvRecord.get("status"));
					} catch (NullPointerException | IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						shipmentBotInputObj.setDate_Details(csvRecord.get("date/detail"));
					} catch (NullPointerException | IllegalArgumentException e) {
						try {
							shipmentBotInputObj.setDate_Details(csvRecord.get("date or detail"));
						} catch (NullPointerException | IllegalArgumentException e1) {
							e.printStackTrace();
						}
					}
					try {
						shipmentBotInputObj.setpOs(csvRecord.get("pos"));
					} catch (NullPointerException | IllegalArgumentException e) {
						try {
							shipmentBotInputObj.setpOs(csvRecord.get("purchase orders"));
						} catch (NullPointerException | IllegalArgumentException e1) {
							e.printStackTrace();
						}

					}
					try {
						shipmentBotInputObj.setShipfrom(csvRecord.get("ship from"));
					} catch (NullPointerException | IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						shipmentBotInputObj.setUniqueKey(shipmentBotInputObj.getaSN() +shipmentBotInputObj.getaRN());
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					shipmentBotInputList.add(shipmentBotInputObj);
					StringBuilder sb1 = new StringBuilder(
							"INSERT IGNORE INTO `ShippmentBotInput` (`ASN`,`ARN`,`POs`,`ASNStatus`,`Date/Details`,`vendorId`,`vendorName`,`DataStatus`,`CreatedDate`,`uniqueKey`,priority) VALUES");
					for (int row1 = 0; row1 < shipmentBotInputList.size(); row1++) {
						if (shipmentBotInputList.get(row1).getaSN().equals("")) {
							sb1.append("('" + shipmentBotInputList.get(row1).getaSN() + "','"
									+ shipmentBotInputList.get(row1).getaRN() + "','"
									+ shipmentBotInputList.get(row1).getpOs() + "','"
									+ shipmentBotInputList.get(row1).getaSNStatus() + "','"
									+ shipmentBotInputList.get(row1).getDate_Details() + "','" + vendorId + "','"
									+ vendorName + "','NoNeedToRun',NOW(),'"
									+ shipmentBotInputList.get(row1).getUniqueKey() + "',1)");
						} else {
							sb1.append("('" + shipmentBotInputList.get(row1).getaSN() + "','"
									+ shipmentBotInputList.get(row1).getaRN() + "','"
									+ shipmentBotInputList.get(row1).getpOs() + "','"
									+ shipmentBotInputList.get(row1).getaSNStatus() + "','"
									+ shipmentBotInputList.get(row1).getDate_Details() + "','" + vendorId + "','"
									+ vendorName + "','PENDING',NOW(),'" + shipmentBotInputList.get(row1).getUniqueKey()
									+  "',1)");
						}

						sb1.append(",");
					}
					String query = sb1.substring(0, sb1.length() - 1) + ";";

					try {
						logger.info("Query " + query);
						jdbcTemplate.execute(query);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					shipmentBotInputList.clear();
				}

			}
			StringBuilder sb1 = new StringBuilder(
					"INSERT  IGNORE INTO `ShippmentBotInput` (`ASN`,`ARN`,`POs`,`ASNStatus`,`Date/Details`,`vendorId`,`vendorName`,`DataStatus`,`CreatedDate`,`uniqueKey`,priority) VALUES");
			for (int row1 = 0; row1 < shipmentBotInputList.size(); row1++) {

				if (shipmentBotInputList.get(row1).getaSN().equals("")) {
					sb1.append("('" + shipmentBotInputList.get(row1).getaSN() + "','"
							+ shipmentBotInputList.get(row1).getaRN() + "','" + shipmentBotInputList.get(row1).getpOs()
							+ "','" + shipmentBotInputList.get(row1).getaSNStatus() + "','"
							+ shipmentBotInputList.get(row1).getDate_Details() + "','" + vendorId + "','" + vendorName
							+ "','NoNeedToRun',NOW(),'" + shipmentBotInputList.get(row1).getUniqueKey()  + "',1)");
				} else {
					sb1.append("('" + shipmentBotInputList.get(row1).getaSN() + "','"
							+ shipmentBotInputList.get(row1).getaRN() + "','" + shipmentBotInputList.get(row1).getpOs()
							+ "','" + shipmentBotInputList.get(row1).getaSNStatus() + "','"
							+ shipmentBotInputList.get(row1).getDate_Details() + "','" + vendorId + "','" + vendorName
							+ "','PENDING',NOW(),'" + shipmentBotInputList.get(row1).getUniqueKey() +  "',1)");
				}
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

}
