package com.dimetyd.bot.process;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.dimetyd.bot.model.CBReturn;
import com.dimetyd.bot.model.CBReturnDetails;

@Component
public class ProductReturnPageStage1 {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public boolean processPage(Page page, Path downloadPath, String vendorId, long requestId, String vendorName,
			String startDate, String endDate) {
		try {
			String vName = vendorName.substring(0, 2);
			SimpleDateFormat outputFormatUS = new SimpleDateFormat("M/d/yyyy");
			SimpleDateFormat outputFormatCA = new SimpleDateFormat("yyyy/M/d");
			SimpleDateFormat outputFormatOthers = new SimpleDateFormat("d/M/yyyy");
			String formattedStartDate = null;
			String formattedEndDate = null;
			if (vName.startsWith("US")) {
				// Format the Date object
				formattedStartDate = outputFormatUS.format(startDate);
				formattedEndDate = outputFormatUS.format(endDate);
			} else if (vName.startsWith("CA")) {
				formattedStartDate = outputFormatCA.format(startDate);
				formattedEndDate = outputFormatCA.format(endDate);
			} else {
				formattedStartDate = outputFormatOthers.format(startDate);
				formattedEndDate = outputFormatOthers.format(endDate);
			}
			page.click("//div[@aria-label='Navigation menu']");
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
			page.click("//span[@class='flyout-menu-item-label' and contains(text(),'Product returns')]");
			page.waitForTimeout(3000);
			page.click("kat-popover[id='date-range-calendar-dropdown-disable-popover'] div[class='select-header']");
			page.click(
					"kat-popover[id='date-range-calendar-dropdown-disable-popover'] kat-option[value='Custom date range']");
			Locator from = page.locator("div[id='date-range-picker'] input[aria-label='From:']");
			from.fill(formattedStartDate);
			Locator to = page.locator("div[id='date-range-picker'] input[aria-label='To:']");
			to.fill(formattedEndDate);
			Locator applyBtn = page.locator("kat-button[id='data-range-modal-apply-button'] button[class='button']");
			applyBtn.click();
			Locator exportAllSummeryBtn = page.locator("kat-button[id='file-download-button'] button[class='button']");

			Download download = page.waitForDownload(() -> exportAllSummeryBtn.click());
			Path filePathReturnSummery = downloadPath.resolve(download.suggestedFilename());
			download.saveAs(filePathReturnSummery);
			try (FileInputStream file = new FileInputStream(filePathReturnSummery.toFile());
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
						List<CBReturn> cbReturnList = new ArrayList<CBReturn>();
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
							CBReturn cBReturn = new CBReturn();
							try {
								cBReturn.setReturnId(
										currentRow.getCell(firstRow.get("return id")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturn.setWarehouse(
										currentRow.getCell(firstRow.get("warehouse")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturn.setVendorCode(
										currentRow.getCell(firstRow.get("vendor code")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturn.setInvoiceNumber(
										currentRow.getCell(firstRow.get("invoice number")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturn.setReturnQuantity((int) currentRow.getCell(firstRow.get("return quantity"))
										.getNumericCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturn.setMarketPlace(
										currentRow.getCell(firstRow.get("marketplace")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturn.setTotalCost(Double.parseDouble(
										currentRow.getCell(firstRow.get("total cost")).getStringCellValue()));
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturn.setShipmentRequestId(
										currentRow.getCell(firstRow.get("shipment request id")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								Date returnDate = currentRow.getCell(firstRow.get("return date")).getDateCellValue();
								//cBReturn.setReturnDate(returnDate);
								if(vName.startsWith("US")||vName.startsWith("CA")) {
									SimpleDateFormat sdf = new SimpleDateFormat("M-d-yyyy"); 
									String formattedDate = sdf.format(returnDate);
									cBReturn.setReturnDate(formattedDate);
									 System.out.println("Formatted Date: " + formattedDate);
								}else {
									SimpleDateFormat sdf = new SimpleDateFormat("d-M-yyyy"); 
									String formattedDate = sdf.format(returnDate);
									cBReturn.setReturnDate(formattedDate);
									 System.out.println("Formatted Date: " + formattedDate);
								}
							
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturn.setCurrency(
										currentRow.getCell(firstRow.get("currency code")).getStringCellValue());
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							try {
								cBReturn.setUniqueKey(cBReturn.getReturnId() + vendorId);
							} catch (NullPointerException | IllegalStateException e1) {
								e1.printStackTrace();
							}
							cbReturnList.add(cBReturn);
						}
						if (cbReturnList.size() == 0)
							break;
						StringBuilder sql = new StringBuilder();
						sql.append(
								"INSERT IGNORE INTO `CBReturn`(`returnId`,`requestId`,`vendorId`,`warehouse`,`vendorCode`,`invoiceNumber`,`returnQuantity`,`markletPlace`,`totalCost`,`shipmentRequestId`,`returnDate`,`currency`,`createdDate`,`status`,`Ukey`) VALUES");

						for (int row = 0; row < cbReturnList.size(); row++) {
							if(vName.startsWith("US")||vName.startsWith("CA")) {
								sql.append("('" + cbReturnList.get(row).getReturnId() + "','" + requestId + "','"
										+ vendorId + "','" + cbReturnList.get(row).getWarehouse() + "','"
										+ cbReturnList.get(row).getVendorCode() + "','"
										+ cbReturnList.get(row).getInvoiceNumber() + "','"
										+ cbReturnList.get(row).getReturnQuantity() + "','"
										+ cbReturnList.get(row).getMarketPlace() + "','"
										+ cbReturnList.get(row).getTotalCost() + "','"
										+ cbReturnList.get(row).getShipmentRequestId() + "',STR_TO_DATE('"+ cbReturnList.get(row).getReturnDate() + "','%m-%d-%Y'),'"
										+ cbReturnList.get(row).getCurrency() + "',NOW(),'PENDING','"
										+ cbReturnList.get(row).getUniqueKey() + "')");
								sql.append(",");
							}else {
								sql.append("('" + cbReturnList.get(row).getReturnId() + "','" + requestId + "','"
										+ vendorId + "','" + cbReturnList.get(row).getWarehouse() + "','"
										+ cbReturnList.get(row).getVendorCode() + "','"
										+ cbReturnList.get(row).getInvoiceNumber() + "','"
										+ cbReturnList.get(row).getReturnQuantity() + "','"
										+ cbReturnList.get(row).getMarketPlace() + "','"
										+ cbReturnList.get(row).getTotalCost() + "','"
										+ cbReturnList.get(row).getShipmentRequestId() + "',STR_TO_DATE('"+ cbReturnList.get(row).getReturnDate() + "','%d-%m-%Y'),'"
										+ cbReturnList.get(row).getCurrency() + "',NOW(),'PENDING','"
										+ cbReturnList.get(row).getUniqueKey() + "')");
								sql.append(",");
							}
								
						}
						String query = sql.substring(0, sql.length() - 1) + ";";

						try {
							logger.info("Query : " + query);
							jdbcTemplate.execute(query);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						cbReturnList.clear();

					}

				}
				file.close();
				filePathReturnSummery.toFile().delete();
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
