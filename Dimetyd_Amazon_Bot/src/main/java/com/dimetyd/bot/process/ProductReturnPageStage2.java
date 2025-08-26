package com.dimetyd.bot.process;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.dimetyd.bot.model.CBReturnDetails;

@Component
public class ProductReturnPageStage2 {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public boolean processPage(Page page, String vendorName, Path downloadPath, String vendorId, String returnId) {
		try {
			page.click("//div[@aria-label='Navigation menu']");
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
			page.click("//span[@class='flyout-menu-item-label' and contains(text(),'Product returns')]");
			page.waitForTimeout(3000);
			Locator textArea = page.locator("kat-textarea[id='text-search-text-area'] textarea[part='textarea']");
			textArea.fill(returnId);
			Locator returnsRandomClick = page.locator("//h1[@id='returns-title']");
			returnsRandomClick.click();
			Locator searchIcon = page.locator("kat-button[id='text-search-search-button'] kat-icon[name='search']");
			searchIcon.click();
			Locator returnIdClick = page.locator("td[data-column='RETURN_ID'] span[class='link__inner']");
			Page popUp = page.waitForPopup(() -> {
				returnIdClick.click();
			});

			Locator export = popUp.locator("kat-button[id='file-download-button'] button[class='button']");

			Download download = popUp.waitForDownload(() -> export.click());
			Path filePathReturnItems = downloadPath.resolve(download.suggestedFilename());
			download.saveAs(filePathReturnItems);
			try (FileInputStream file = new FileInputStream(filePathReturnItems.toFile());
					HSSFWorkbook hSSFWorkbook = new HSSFWorkbook(file)) {
				for (int numOfSheet = 0; numOfSheet < hSSFWorkbook.getNumberOfSheets(); numOfSheet++) {
					HSSFSheet sheet = hSSFWorkbook.getSheetAt(numOfSheet);
					Iterator<Row> rows = sheet.iterator();
					int rowNumber = 0;
					HashMap<String, Integer> firstRow = new HashMap<String, Integer>();
					Row r = sheet.getRow(0);
					for (int cn = 0; cn < r.getLastCellNum(); cn++) {
						Cell c = r.getCell(cn);
						if (c == null || c.getCellType() == CellType.BLANK) {
							// Can't be this cell - it's empty
							continue;
						}
						if (c.getCellType() == CellType.STRING) {
							String text = c.getStringCellValue().toLowerCase();
							firstRow.put(text.trim(), cn);

						}

					}
					while (rows.hasNext()) {
						List<CBReturnDetails> cbReturnDetailsList = new ArrayList<CBReturnDetails>();
						for (int rownum = 0; rownum < 100; rownum++) {
							Row currentRow = null;
							try {
								currentRow = rows.next();
							} catch (java.util.NoSuchElementException e) {
								break;
							}

							if (rowNumber == 0) {
								rowNumber++;
								continue;
							}
							rowNumber++;
							CBReturnDetails cBReturnDetails = new CBReturnDetails();

							try {
								cBReturnDetails.setReturnId(String
										.valueOf(currentRow.getCell(firstRow.get("return id")).getNumericCellValue()));
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails
										.setpO(currentRow.getCell(firstRow.get("purchase order")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setDistributorShipment(currentRow
										.getCell(firstRow.get("distributor shipment id")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails
										.setProduct(currentRow.getCell(firstRow.get("product")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setProductLine(
										currentRow.getCell(firstRow.get("product line")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setAsin(currentRow.getCell(firstRow.get("asin")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setEan(currentRow.getCell(firstRow.get("ean")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setUpc(currentRow.getCell(firstRow.get("upc")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails
										.setReason(currentRow.getCell(firstRow.get("reason")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails
										.setCarton(currentRow.getCell(firstRow.get("carton")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails
										.setCarrier(currentRow.getCell(firstRow.get("carrier")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}

							try {
								cBReturnDetails.setTrackingNumber(
										currentRow.getCell(firstRow.get("tracking number")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setQuantity(
										(int) currentRow.getCell(firstRow.get("quantity")).getNumericCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setCostPerUnit(Double.parseDouble(
										currentRow.getCell(firstRow.get("cost per unit")).getStringCellValue()));
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setTotalAmount(Double.parseDouble(
										currentRow.getCell(firstRow.get("total amount")).getStringCellValue()));
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setCurrency(
										currentRow.getCell(firstRow.get("currency code")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturnDetails.setUniqueKey(cBReturnDetails.getReturnId() + cBReturnDetails.getpO()
										+ cBReturnDetails.getAsin() + cBReturnDetails.getDistributorShipment());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							cbReturnDetailsList.add(cBReturnDetails);
						}
						if (cbReturnDetailsList.size() == 0)
							break;
						StringBuilder sql = new StringBuilder();
						sql.append(
								"INSERT IGNORE INTO `CBReturnDetails`(`returnId`,`PO`,`distributorShipmentId`,`product`,`productLine`,`ASIN`,`EAN`,`UPC`,`reason`,`carton`,`carrier`,`trackingNumber`,`quantity`,`costPerUnit`,`totalAmount`,`currency`,`createdDate`,`UKey`)VALUES");

						for (int row = 0; row < cbReturnDetailsList.size(); row++) {

							sql.append("('" + cbReturnDetailsList.get(row).getReturnId() + "','"
									+ cbReturnDetailsList.get(row).getpO() + "','"
									+ cbReturnDetailsList.get(row).getDistributorShipment() + "','"
									+ cbReturnDetailsList.get(row).getProduct() + "','"
									+ cbReturnDetailsList.get(row).getProductLine() + "','"
									+ cbReturnDetailsList.get(row).getAsin() + "','"
									+ cbReturnDetailsList.get(row).getEan() + "','"
									+ cbReturnDetailsList.get(row).getUpc() + "','"
									+ cbReturnDetailsList.get(row).getReason() + "','"
									+ cbReturnDetailsList.get(row).getCarton() + "','"
									+ cbReturnDetailsList.get(row).getCarrier() + "','"
									+ cbReturnDetailsList.get(row).getTrackingNumber() + "','"
									+ cbReturnDetailsList.get(row).getQuantity() + "','"
									+ cbReturnDetailsList.get(row).getCostPerUnit() + "','"
									+ cbReturnDetailsList.get(row).getTotalAmount() + "','"
									+ cbReturnDetailsList.get(row).getCurrency() + "',NOW(),'"
									+ cbReturnDetailsList.get(row).getUniqueKey() + "')");
							sql.append(",");

						}
						String query = sql.substring(0, sql.length() - 1) + ";";

						try {
							logger.info("Query : " + query);
							jdbcTemplate.execute(query);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						cbReturnDetailsList.clear();

					}

				}
				file.close();
				filePathReturnItems.toFile().delete();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
