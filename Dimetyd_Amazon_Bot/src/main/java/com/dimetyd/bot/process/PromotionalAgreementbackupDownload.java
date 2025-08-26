package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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

import com.dimetyd.bot.model.CBPromotionalAgreementInvoiceDetails;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class PromotionalAgreementbackupDownload {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private CommonUtil commonUtilObj;

	public boolean processPage(Page page, Long id, String agreementId, String vendorId, String vendorName,
			Long requestId, Path downloadPath) {

		File backReport = new File(downloadPath + "BackupReport.xls");
		if (backReport.exists()) {
			backReport.delete();
		}
		page.click("//div[@aria-label='Navigation menu']");
		try {
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
		} catch (Exception e) {
			page.reload();
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
		}

		try {
			List<ElementHandle> subMenuList = page.querySelectorAll(".flyout-menu-item-label");
			if (vendorName.contains("CA -")) {
				for (ElementHandle subMenu : subMenuList) {
					// Retrieve HTML content
					String htmlContent = subMenu.innerHTML();
					if ("Co-op".equals(htmlContent)) {
						// Click the element
						subMenu.click();
						System.out.println("Clicked element with Playwright");
						break; // Exit the loop after clicking
					}
				}
			} else {
				for (ElementHandle subMenu : subMenuList) {
					// Retrieve HTML content
					String htmlContent = subMenu.innerHTML();
					if ("CoOp".equals(htmlContent)) {
						// Click the element
						subMenu.click();
						System.out.println("Clicked element with Playwright");
						break; // Exit the loop after clicking
					}
				}
			}

		} catch (Exception e) {
			if (vendorName.contains("US -")) {
				logger.info("Vendor Name Contains - US");
				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'CoOp')]");
			}
			if (vendorName.contains("CA -")) {
				logger.info("Vendor Name Contains - CA");

				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Co-op')]");
			} else {
				logger.info("Not get Vendor Country, is Using US Coop Click...");
				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'CoOp')]");
			}
		}

		try {

			String anErrorOcurred = page.locator(
					"//div[@class='a-container page']//div[@class='a-section']//div[@class='a-box a-alert a-alert-error']//div[@class='a-box-inner a-alert-container']//div[@class='a-alert-content']")
					.innerText().trim();
			logger.info("anErrorOcurred : " + anErrorOcurred);
			if (anErrorOcurred.equals("A problem occurred, please try again.")) {
				page.click("//div[@aria-label='Navigation menu']");
				try {
					page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
				} catch (Exception e) {
					page.reload();
					page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
				}
			}
		} catch (PlaywrightException e) {
			logger.info("No Error Ocurred");
		}
		page.waitForTimeout(2000);
		Locator searchInput = page.locator("#search-input");
		String agreeents[] = agreementId.split(",");
		for (String vAgreementId : agreeents) {
			searchInput.fill(vAgreementId);
			Locator searchButton = page.locator("#search-button-announce");
			searchButton.click();

			try {

				Locator noRecordFound = page.locator("#contra-cogs-no-records-section");
				if (noRecordFound.count() > 0) {
					return true;
				}

				try {
					int noOfDeductions = Integer.parseInt(
							page.locator("#total-deductions-label").innerText().replace("TOTAL DEDUCTIONS", "").trim());
					logger.info("UPDATE CBAgreementRequestDetails Set CountOfInvOrPayementNumber='" + noOfDeductions
							+ "' WHERE id='" + id + "'");
					jdbcTemplate.execute("UPDATE CBAgreementRequestDetails Set CountOfInvOrPayementNumber='"
							+ noOfDeductions + "' WHERE id='" + id + "'");
				} catch (Exception e) {

				}
				ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
				boolean isFirstPage = false;
				while (true) {
					// while (true) {

					File backupReport = new File(downloadPath + "BackupReport.xls");
					if (backupReport.exists()) {
						backupReport.delete();
					}

					String invoiceNumber = null;
					ArrayList<String> data = null;
					try {
						Thread.sleep(5000);
						Locator table = page.locator("//table[@aria-labelledby='katal-id-0']//tbody");
						List<Locator> getRows = null;

						if (isFirstPage == false) {
							getRows = table.locator("*[style='display: table-row;']").all();
							;
						} else {
							getRows = table.locator("*[style='display: table-row; opacity: 1;']").all();
						}

						int getRowSize = getRows.size();
						logger.info("NoOfRows : " + getRows.size());
						if (getRowSize == 0) {
							if (isFirstPage == false) {
								getRows = table.locator("tr").all();
							} else {
								getRows = table.locator("*[style='opacity: 1; display: table-row;']").all();

							}
							logger.info("NoOfRows : " + getRows.size());
						}

						for (Locator getRow : getRows) {

							data = new ArrayList<String>();
							List<Locator> columnsList = getRow.locator("td").all();
							logger.info("ColumnListSize : " + columnsList.size());
							for (Locator i : columnsList) {
								data.add(i.innerText());
							}
							logger.info("row " + data);
							rows.add(data);
							invoiceNumber = data.get(1).trim();
							double Amount = Double.parseDouble(commonUtilObj.processAmount(data.get(5)));
							StringBuilder sql2 = new StringBuilder();
							sql2.append("Select count(*) from `CBPromotionalAgreementDetail` where agreementID='"
									+ agreementId + "' AND invoiceNumber ='" + invoiceNumber + "' AND vendorId ='"
									+ vendorId + "'");
							int noOfRows = jdbcTemplate.queryForObject(sql2.toString(), Integer.class);
							logger.info("Result : " + noOfRows);

							if (noOfRows == 0) {
								Double OriginalAmount = Double.parseDouble(commonUtilObj.processAmount(data.get(5)));
								Locator dropdownContainer = getRow.locator(".a-dropdown-container");
								if (dropdownContainer.count() > 0) {
									dropdownContainer.waitFor(
											new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

									// Locate the dropdown button
									Locator buttonDropdown = dropdownContainer.locator(".a-button.a-button-dropdown");
									if (buttonDropdown.count() > 0) {
										buttonDropdown.evaluate("element => element.scrollIntoView()");
										buttonDropdown.waitFor(
												new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

										// Locate and click the dropdown prompt
										Locator dropdownPrompt = buttonDropdown
												.locator("[data-action='a-dropdown-button']");
										dropdownPrompt.waitFor(
												new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
										dropdownPrompt.hover(); // Hover over the element to trigger any hover-based
																// actions
										page.waitForTimeout(900);
										dropdownPrompt.click();
									} else {
										System.out.println("Dropdown button not found");
									}
								} else {
									System.out.println("Dropdown container not found");
								}

								// Perform actions

								logger.info(
										"InvoiceNumber : " + "//*[@id=\"invoiceDownloads-" + invoiceNumber + "_2\"]");

								List<Locator> backUpReportDropDownList = page
										.locator("//div[@class='a-popover-wrapper']//ul[@role='listbox']//li//a").all();
								for (Locator backupReport1 : backUpReportDropDownList) {
									String ids = backupReport1.getAttribute("id");

									if (ids.contains("invoiceDownloads-" + invoiceNumber + "")) {
										if (backupReport1.innerText().trim().equals("Backup report")
												|| backupReport1.innerText().trim().equals("Backup Report")) {
											backupReport1.click();
											break;
										}
									}

								}

								try {
									String status = isDownloadSectionAvailable(page, vAgreementId, invoiceNumber,
											vendorId, OriginalAmount);
									if (status.equals("false")) {
										continue;
									}

									Locator startDtElement = page
											.locator("//*[@id=\"backup-report-table\"]/tbody/tr[2]/td[1]");
									String startDt = startDtElement.innerText();

									startDtElement = page.locator("//*[@id=\"backup-report-table\"]/tbody/tr[2]/td[2]");
									String endDt = startDtElement.innerText();

									String currency = data.get(5).replaceAll("[\\d.]", "").replaceAll(",", "");
									currency = CommonUtil.getCurrency(currency);
									String amt = data.get(5);
									if (amt.contains("€") || amt.contains("KR") || amt.contains("ZŁ")) {
										amt = CommonUtil.replaceCurrency(amt).trim();
										int amtCharatcerLength = amt.length();
										if (amtCharatcerLength > 3) {
											char thirdLastChar = amt.charAt(amtCharatcerLength - 3);
											if (thirdLastChar == ',') {
												amt = amt.substring(0, amtCharatcerLength - 3) + '.'
														+ amt.substring(amtCharatcerLength - 2);
											}
											int lastDotIndex = amt.lastIndexOf('.');

											if (lastDotIndex != -1) { // Check if a dot exists in the string
												amt = amt.replaceAll("\\.(?![^.]*$)", "");
												logger.info("Modified string: " + amt);
											} else {
												logger.info("No dot found in the string.");
											}
										}
									}

									String Uniquekey = agreementId + invoiceNumber;
									String orginialAmt = commonUtilObj.processAmount(data.get(5));
									logger.info("billedAmount : " + orginialAmt);

									String createDt = "NOW()";

									StringBuilder sb = new StringBuilder(
											"INSERT IGNORE INTO CBAgreementInvoice (vendorId,uniqueKey,agreementId,invoiceNumber,billedAmount,fundingType,startDate,endDate,createdDate,currency) VALUES");
									sb.append("('" + vendorId + "','" + Uniquekey + "','" + vAgreementId + "','"
											+ invoiceNumber + "','" + orginialAmt + "','" + data.get(4) + "','"
											+ startDt + "','" + endDt + "'," + createDt + ",'" + currency + "')");

									try {
										logger.info("Query ; " + sb.toString());
										jdbcTemplate.execute(sb.toString());
									} catch (Exception e) {
										e.printStackTrace();
									}
									OriginalAmount = Double.parseDouble(orginialAmt);
									if (OriginalAmount >= 0) {
										Thread.sleep(3000);
										Locator downloadBackupButton = page
												.locator("//*[@id=\"backup-report-table\"]/tbody/tr[2]/td[3]/a");
										Download download = page.waitForDownload(() -> {
											downloadBackupButton.click();
										});

										try {
											Path filePath = downloadPath.resolve(download.suggestedFilename());
											System.out.println("Downloading file to: " + filePath);

											// Save the downloaded file to the specified path
											download.saveAs(filePath);
											File tempFile = filePath.toFile();

											boolean fileExists = CommonUtil.waitForFile(tempFile,
													Duration.ofSeconds(25));

											if (fileExists) {
												System.out.println("File exists!");
											} else {
												System.out.println("File does not exist within the timeout period.");

											}
											try (FileInputStream file = new FileInputStream(tempFile);
													HSSFWorkbook hSSFWorkbook = new HSSFWorkbook(file)) {

												for (int numOfSheet = 0; numOfSheet < hSSFWorkbook
														.getNumberOfSheets(); numOfSheet++) {
													logger.info("SheetNum : " + numOfSheet);
													logger.info("SheetName : "
															+ hSSFWorkbook.getSheetName(numOfSheet).trim());
													HSSFSheet sheet = hSSFWorkbook.getSheetAt(numOfSheet);
													Iterator<Row> rows1 = sheet.iterator();
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
															String text = c.getStringCellValue();
															firstRow.put(text, cn);

														}
													}
													boolean IsRebateAgreementCurrencyColumn = false;
													for (Map.Entry<String, Integer> entry : firstRow.entrySet()) {
														// Check if the key contains "Rebate In Agreement Currency"
														logger.info(entry.getKey());
														if (entry.getKey().contains("Coupon Code")) {
															logger.info("Found: " + entry.getKey() + " with value: "
																	+ entry.getValue());
															return true;
														}

														if (entry.getKey()
																.contains("Vendor Funding In Agreement Currency")) {
															logger.info("Found: " + entry.getKey() + " with value: "
																	+ entry.getValue());
															IsRebateAgreementCurrencyColumn = true;
															break;
														}
													}
													while (rows1.hasNext()) {
														String sheetName = hSSFWorkbook.getSheetName(numOfSheet).trim();
														if (sheetName.contains("Revised")) {
															logger.info(
																	"UPDATE CBAgreementInvoice set comment='Revised Invoice' where agreementId='"
																			+ vAgreementId + "'and invoiceNumber='"
																			+ invoiceNumber + "'and vendorId='"
																			+ vendorId + "'");
															jdbcTemplate.execute(
																	"UPDATE CBAgreementInvoice set comment='Revised Invoice' where agreementId='"
																			+ vAgreementId + "'and invoiceNumber='"
																			+ invoiceNumber + "'and vendorId='"
																			+ vendorId + "'");

															List<CBPromotionalAgreementInvoiceDetails> rowList = new ArrayList();
															try {
																logger.info("Agreement Rebate Currecncy Column :"
																		+ IsRebateAgreementCurrencyColumn);
																for (int rownum = 0; rownum < 100; rownum++) {
																	Row currentRow;
																	try {
																		currentRow = rows1.next();
																	} catch (Exception ex) {
																		break;
																	}

																	if (rowNumber == 0) {
																		rowNumber++;
																		continue;
																	}
																	rowNumber++;

																	logger.info("");
																	CBPromotionalAgreementInvoiceDetails cbAgreementInvoiceDetails = new CBPromotionalAgreementInvoiceDetails();
																	try {

																		if (currentRow
																				.getCell(firstRow.get("Receive Date"))
																				.getStringCellValue().equals("Total")) {
																			System.out.println("End of file");
																			break;

																		}

																		cbAgreementInvoiceDetails
																				.setOrderDate(currentRow
																						.getCell(firstRow
																								.get("Order Date"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setOrderDate("");
																	}

																	try {
																		cbAgreementInvoiceDetails.setShipDate(currentRow
																				.getCell(firstRow.get("Ship Date"))
																				.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setShipDate("");
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setReturnDate(currentRow
																						.getCell(firstRow
																								.get("Return Date"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setReturnDate("");
																	}

																	try {
																		cbAgreementInvoiceDetails.setCostDate(currentRow
																				.getCell(firstRow.get("Cost Date"))
																				.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setCostDate("");
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setTransactionType(currentRow
																						.getCell(firstRow.get(
																								"Transaction Type"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails
																				.setTransactionType("");
																	}

																	try {

																		cbAgreementInvoiceDetails
																				.setQty((int) currentRow
																						.getCell(firstRow
																								.get("Quantity"))
																						.getNumericCellValue());

																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setQty(0);
																	} catch (IllegalStateException e1) {
																		cbAgreementInvoiceDetails.setQty(0);
																	}

																	try {
																		cbAgreementInvoiceDetails.setNetSales(currentRow
																				.getCell(firstRow.get("Net Sales"))
																				.getNumericCellValue());

																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setNetSales(0.0);
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setNetSalesCurency(currentRow
																						.getCell(firstRow.get(
																								"Net Sales Currency"))
																						.getNumericCellValue());

																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails
																				.setNetSalesCurency(0.0);
																	}
																	try {
																		cbAgreementInvoiceDetails
																				.setListPrice(currentRow
																						.getCell(firstRow
																								.get("List Price"))
																						.getNumericCellValue());

																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setListPrice(0.0);
																	}
																	try {
																		cbAgreementInvoiceDetails.setRebate(
																				(double) currentRow.getCell(firstRow
																						.get("Rebate In Agreement Currency"))
																						.getNumericCellValue());
																	}

																	catch (Exception e1) {

																		cbAgreementInvoiceDetails.setRebate(0.0);
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setPurchaseOrder(currentRow
																						.getCell(firstRow
																								.get("Purchase Order"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setPurchaseOrder("");
																	}
																	try {
																		cbAgreementInvoiceDetails.setAsin(
																				currentRow.getCell(firstRow.get("Asin"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setAsin("");
																	}
																	try {
																		cbAgreementInvoiceDetails.setUPC(
																				currentRow.getCell(firstRow.get("UPC"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setUPC("");
																	}

																	try {
																		cbAgreementInvoiceDetails.setEAN(
																				currentRow.getCell(firstRow.get("EAN"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setEAN("");
																	}
																	try {
																		cbAgreementInvoiceDetails
																				.setManufacturer(currentRow
																						.getCell(firstRow
																								.get("Manufacturer"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setManufacturer("");
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setDistributor(currentRow
																						.getCell(firstRow
																								.get("Distributor"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setDistributor("");
																	}

																	try {
																		Integer productGroup = (int) currentRow
																				.getCell(firstRow.get("Product Group"))
																				.getNumericCellValue();
																		cbAgreementInvoiceDetails.setProductGroup(
																				String.valueOf(productGroup));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setProductGroup("");
																	}
																	try {

																		cbAgreementInvoiceDetails.setCategory(currentRow
																				.getCell(firstRow.get("Category"))
																				.getStringCellValue().replace("\"", "")
																				.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setCategory("");
																	}

																	try {

																		cbAgreementInvoiceDetails
																				.setSubCategory(currentRow
																						.getCell(firstRow
																								.get("Subcategory"))
																						.getStringCellValue()
																						.replace("\"", "")
																						.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setSubCategory("");
																	}

																	try {

																		cbAgreementInvoiceDetails.setTitle(currentRow
																				.getCell(firstRow.get("Title"))
																				.getStringCellValue().replace("\"", "")
																				.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setTitle("");
																	}
																	try {

																		cbAgreementInvoiceDetails
																				.setProductDescription(currentRow
																						.getCell(firstRow.get(
																								"Product Description"))
																						.getStringCellValue()
																						.replace("\"", "")
																						.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails
																				.setProductDescription("");
																	}
																	try {

																		cbAgreementInvoiceDetails.setBinding(currentRow
																				.getCell(firstRow.get("Binding"))
																				.getStringCellValue().replace("\"", "")
																				.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setBinding("");
																	}

																	try {

																		cbAgreementInvoiceDetails
																				.setPromotionId(currentRow
																						.getCell(firstRow
																								.get("Promotion Id"))
																						.getStringCellValue()
																						.replace("\"", "")
																						.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setPromotionId("");
																	}

																	try {

																		cbAgreementInvoiceDetails.setCostType(currentRow
																				.getCell(firstRow.get("Cost Type"))
																				.getStringCellValue().replace("\"", "")
																				.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setCostType("");
																	}
																	try {

																		cbAgreementInvoiceDetails
																				.setOrderCountry(currentRow
																						.getCell(firstRow
																								.get("Order Country"))
																						.getStringCellValue()
																						.replace("\"", "")
																						.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setOrderCountry("");
																	}

																	try {
																		String uniqueKey = vAgreementId + invoiceNumber
																				+ cbAgreementInvoiceDetails
																						.getPurchaseOrder()
																				+ cbAgreementInvoiceDetails.getAsin()
																				+ String.valueOf(
																						cbAgreementInvoiceDetails
																								.getQty())
																				+ cbAgreementInvoiceDetails
																						.getReturnDate()
																				+ String.valueOf(rowNumber)
																				+ String.valueOf(numOfSheet)
																				+ cbAgreementInvoiceDetails.getRebate();

																		cbAgreementInvoiceDetails
																				.setUniqueKey(uniqueKey);

																	} catch (Exception e) {

																		e.printStackTrace();
																	}

																	rowList.add(cbAgreementInvoiceDetails);

																}
															} catch (Exception e1) {
																e1.printStackTrace();
															}

															logger.info("List Size " + rowList.size());
															if (rowList.size() == 0) {
																break;
															}
															boolean rowsAdded = false;
															StringBuilder sb1 = new StringBuilder(
																	"INSERT IGNORE INTO `CBPromotionalAgreementDetail`(`createdDate`,requestId,`invoiceNumber`,`vendorId`,`agreementId`,`orderDate`,`shipDate`,"
																			+ "`returnDate`,`costDate`,`transactionType`,`quantity`,`netSales`,`netSalesCurrency`,`listPrice`,`rebate`,`purchaseOrder`,`asin`,`upc`,"
																			+ "`ean`,`manufacturer`,`distributor`,`productGroup`,`category`,`subcategory`,`title`,`productDescription`,`binding`,`promotionId`,`costType`,"
																			+ "`orderCountry`,`uniqueKey`) VALUES");
															for (int row11 = 0; row11 < rowList.size(); row11++) {

																rowsAdded = true;
																sb1.append("(NOW(),'" + requestId + "','"
																		+ invoiceNumber + "','" + vendorId + "','"
																		+ agreementId + "','"
																		+ rowList.get(row11).getOrderDate() + "','"
																		+ rowList.get(row11).getShipDate() + "','"
																		+ rowList.get(row11).getReturnDate() + "','"
																		+ rowList.get(row11).getCostDate() + "','"
																		+ rowList.get(row11).getTransactionType()
																		+ "','" + rowList.get(row11).getQty() + "','"
																		+ rowList.get(row11).getNetSales() + "','"
																		+ rowList.get(row11).getNetSalesCurency()
																		+ "','" + rowList.get(row11).getListPrice()
																		+ "','" + rowList.get(row11).getRebate() + "','"
																		+ rowList.get(row11).getPurchaseOrder() + "','"
																		+ rowList.get(row11).getAsin() + "','"
																		+ rowList.get(row11).getUPC() + "','"
																		+ rowList.get(row11).getEAN() + "','"
																		+ rowList.get(row11).getManufacturer() + "','"
																		+ rowList.get(row11).getDistributor() + "','"
																		+ rowList.get(row11).getProductGroup() + "','"

																		+ rowList.get(row11).getCategory() + "','"
																		+ rowList.get(row11).getSubCategory() + "','"
																		+ rowList.get(row11).getTitle() + "','"
																		+ rowList.get(row11).getProductDescription()
																		+ "','" + rowList.get(row11).getBinding()
																		+ "','" + rowList.get(row11).getPromotionId()
																		+ "','" + rowList.get(row11).getCostType()
																		+ "','" + rowList.get(row11).getOrderCountry()
																		+ "','" + rowList.get(row11).getUniqueKey()
																		+ "')");

																sb1.append(",");

															}

															String query = sb1.substring(0, sb1.length() - 1) + ";";

															try {
																if (rowsAdded == true) {
																	logger.info("Query : " + query);
																	jdbcTemplate.execute(query);
																}

															} catch (Exception e1) {
																e1.printStackTrace();
															}
															rowList.clear();
														}
														if (!sheetName.contains("Revised")) {

															logger.info(
																	"UPDATE CBAgreementInvoice set comment='Vendor Funding Column : "
																			+ IsRebateAgreementCurrencyColumn
																			+ "' where agreementId='" + vAgreementId
																			+ "'and invoiceNumber='" + invoiceNumber
																			+ "'and vendorId='" + vendorId + "'");
															jdbcTemplate.execute(
																	"UPDATE CBAgreementInvoice set comment='Vendor Funding Column : "
																			+ IsRebateAgreementCurrencyColumn
																			+ "' where agreementId='" + vAgreementId
																			+ "'and invoiceNumber='" + invoiceNumber
																			+ "'and vendorId='" + vendorId + "'");
															List<CBPromotionalAgreementInvoiceDetails> rowList = new ArrayList();
															try {
																logger.info("Agreement Rebate Currecncy Column :"
																		+ IsRebateAgreementCurrencyColumn);
																for (int rownum = 0; rownum < 100; rownum++) {

																	Row currentRow;
																	try {
																		currentRow = rows1.next();
																	} catch (Exception ex) {
																		break;
																	}
																	if (rowNumber == 0) {
																		rowNumber++;
																		continue;
																	}
																	rowNumber++;

																	logger.info("");
																	CBPromotionalAgreementInvoiceDetails cbAgreementInvoiceDetails = new CBPromotionalAgreementInvoiceDetails();
																	try {

																		if (currentRow
																				.getCell(firstRow.get("Receive Date"))
																				.getStringCellValue().equals("Total")) {
																			System.out.println("End of file");
																			break;

																		}

																		cbAgreementInvoiceDetails
																				.setOrderDate(currentRow
																						.getCell(firstRow
																								.get("Order Date"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setOrderDate("");
																	}

																	try {
																		cbAgreementInvoiceDetails.setShipDate(currentRow
																				.getCell(firstRow.get("Ship Date"))
																				.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setShipDate("");
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setReturnDate(currentRow
																						.getCell(firstRow
																								.get("Return Date"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setReturnDate("");
																	}

																	try {
																		cbAgreementInvoiceDetails.setCostDate(currentRow
																				.getCell(firstRow.get("Cost Date"))
																				.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setCostDate("");
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setTransactionType(currentRow
																						.getCell(firstRow.get(
																								"Transaction Type"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails
																				.setTransactionType("");
																	}

																	try {

																		cbAgreementInvoiceDetails
																				.setQty((int) currentRow
																						.getCell(firstRow
																								.get("Quantity"))
																						.getNumericCellValue());

																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setQty(0);
																	} catch (IllegalStateException e1) {
																		cbAgreementInvoiceDetails.setQty(0);
																	}

																	try {
																		cbAgreementInvoiceDetails.setNetSales(currentRow
																				.getCell(firstRow.get("Net Sales"))
																				.getNumericCellValue());

																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setNetSales(0.0);
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setNetSalesCurency(currentRow
																						.getCell(firstRow.get(
																								"Net Sales Currency"))
																						.getNumericCellValue());

																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails
																				.setNetSalesCurency(0.0);
																	}
																	try {
																		cbAgreementInvoiceDetails
																				.setListPrice(currentRow
																						.getCell(firstRow
																								.get("List Price"))
																						.getNumericCellValue());

																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setListPrice(0.0);
																	}
																	try {
																		cbAgreementInvoiceDetails.setRebate(
																				(double) currentRow.getCell(firstRow
																						.get("Rebate In Agreement Currency"))
																						.getNumericCellValue());
																	}

																	catch (Exception e1) {

																		cbAgreementInvoiceDetails.setRebate(0.0);
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setPurchaseOrder(currentRow
																						.getCell(firstRow
																								.get("Purchase Order"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setPurchaseOrder("");
																	}
																	try {
																		cbAgreementInvoiceDetails.setAsin(
																				currentRow.getCell(firstRow.get("Asin"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setAsin("");
																	}
																	try {
																		cbAgreementInvoiceDetails.setUPC(
																				currentRow.getCell(firstRow.get("UPC"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setUPC("");
																	}

																	try {
																		cbAgreementInvoiceDetails.setEAN(
																				currentRow.getCell(firstRow.get("EAN"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setEAN("");
																	}
																	try {
																		cbAgreementInvoiceDetails
																				.setManufacturer(currentRow
																						.getCell(firstRow
																								.get("Manufacturer"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setManufacturer("");
																	}

																	try {
																		cbAgreementInvoiceDetails
																				.setDistributor(currentRow
																						.getCell(firstRow
																								.get("Distributor"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setDistributor("");
																	}

																	try {
																		Integer productGroup = (int) currentRow
																				.getCell(firstRow.get("Product Group"))
																				.getNumericCellValue();
																		cbAgreementInvoiceDetails.setProductGroup(
																				String.valueOf(productGroup));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setProductGroup("");
																	}
																	try {

																		cbAgreementInvoiceDetails.setCategory(currentRow
																				.getCell(firstRow.get("Category"))
																				.getStringCellValue().replace("\"", "")
																				.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setCategory("");
																	}

																	try {

																		cbAgreementInvoiceDetails
																				.setSubCategory(currentRow
																						.getCell(firstRow
																								.get("Subcategory"))
																						.getStringCellValue()
																						.replace("\"", "")
																						.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setSubCategory("");
																	}

																	try {

																		cbAgreementInvoiceDetails.setTitle(currentRow
																				.getCell(firstRow.get("Title"))
																				.getStringCellValue().replace("\"", "")
																				.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setTitle("");
																	}
																	try {

																		cbAgreementInvoiceDetails
																				.setProductDescription(currentRow
																						.getCell(firstRow.get(
																								"Product Description"))
																						.getStringCellValue()
																						.replace("\"", "")
																						.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails
																				.setProductDescription("");
																	}
																	try {

																		cbAgreementInvoiceDetails.setBinding(currentRow
																				.getCell(firstRow.get("Binding"))
																				.getStringCellValue().replace("\"", "")
																				.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setBinding("");
																	}

																	try {

																		cbAgreementInvoiceDetails
																				.setPromotionId(currentRow
																						.getCell(firstRow
																								.get("Promotion Id"))
																						.getStringCellValue()
																						.replace("\"", "")
																						.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setPromotionId("");
																	}

																	try {

																		cbAgreementInvoiceDetails.setCostType(currentRow
																				.getCell(firstRow.get("Cost Type"))
																				.getStringCellValue().replace("\"", "")
																				.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setCostType("");
																	}
																	try {

																		cbAgreementInvoiceDetails
																				.setOrderCountry(currentRow
																						.getCell(firstRow
																								.get("Order Country"))
																						.getStringCellValue()
																						.replace("\"", "")
																						.replace("'", ""));
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setOrderCountry("");
																	}

																	try {
																		String uniqueKey = vAgreementId + invoiceNumber
																				+ cbAgreementInvoiceDetails
																						.getPurchaseOrder()
																				+ cbAgreementInvoiceDetails.getAsin()
																				+ String.valueOf(
																						cbAgreementInvoiceDetails
																								.getQty())
																				+ cbAgreementInvoiceDetails
																						.getReturnDate()
																				+ String.valueOf(rowNumber)
																				+ String.valueOf(numOfSheet)
																				+ cbAgreementInvoiceDetails.getRebate();

																		cbAgreementInvoiceDetails
																				.setUniqueKey(uniqueKey);

																	} catch (Exception e) {

																		e.printStackTrace();
																	}

																	rowList.add(cbAgreementInvoiceDetails);

																}
															} catch (Exception e1) {
																e1.printStackTrace();
															}

															logger.info("List Size " + rowList.size());
															if (rowList.size() == 0) {
																break;
															}
															boolean rowsAdded = false;
															StringBuilder sb1 = new StringBuilder(
																	"INSERT IGNORE INTO `CBPromotionalAgreementDetail`(`createdDate`,requestId,`invoiceNumber`,`vendorId`,`agreementId`,`orderDate`,`shipDate`,"
																			+ "`returnDate`,`costDate`,`transactionType`,`quantity`,`netSales`,`netSalesCurrency`,`listPrice`,`rebate`,`purchaseOrder`,`asin`,`upc`,"
																			+ "`ean`,`manufacturer`,`distributor`,`productGroup`,`category`,`subcategory`,`title`,`productDescription`,`binding`,`promotionId`,`costType`,"
																			+ "`orderCountry`,`uniqueKey`) VALUES");
															for (int row11 = 0; row11 < rowList.size(); row11++) {

																rowsAdded = true;
																sb1.append("(NOW(),'" + requestId + "','"
																		+ invoiceNumber + "','" + vendorId + "','"
																		+ agreementId + "','"
																		+ rowList.get(row11).getOrderDate() + "','"
																		+ rowList.get(row11).getShipDate() + "','"
																		+ rowList.get(row11).getReturnDate() + "','"
																		+ rowList.get(row11).getCostDate() + "','"
																		+ rowList.get(row11).getTransactionType()
																		+ "','" + rowList.get(row11).getQty() + "','"
																		+ rowList.get(row11).getNetSales() + "','"
																		+ rowList.get(row11).getNetSalesCurency()
																		+ "','" + rowList.get(row11).getListPrice()
																		+ "','" + rowList.get(row11).getRebate() + "','"
																		+ rowList.get(row11).getPurchaseOrder() + "','"
																		+ rowList.get(row11).getAsin() + "','"
																		+ rowList.get(row11).getUPC() + "','"
																		+ rowList.get(row11).getEAN() + "','"
																		+ rowList.get(row11).getManufacturer() + "','"
																		+ rowList.get(row11).getDistributor() + "','"
																		+ rowList.get(row11).getProductGroup() + "','"

																		+ rowList.get(row11).getCategory() + "','"
																		+ rowList.get(row11).getSubCategory() + "','"
																		+ rowList.get(row11).getTitle() + "','"
																		+ rowList.get(row11).getProductDescription()
																		+ "','" + rowList.get(row11).getBinding()
																		+ "','" + rowList.get(row11).getPromotionId()
																		+ "','" + rowList.get(row11).getCostType()
																		+ "','" + rowList.get(row11).getOrderCountry()
																		+ "','" + rowList.get(row11).getUniqueKey()
																		+ "')");

																sb1.append(",");

															}

															String query = sb1.substring(0, sb1.length() - 1) + ";";

															try {

																if (rowsAdded == true) {
																	logger.info("Query : " + query);
																	jdbcTemplate.execute(query);
																}

															} catch (Exception e1) {
																e1.printStackTrace();
															}

															rowList.clear();
														}

													}

												}

												logger.info("update query : "
														+ "UPDATE `CBAgreementInvoice` a JOIN (SELECT `invoiceNumber`, `agreementId`,SUM(`netReceipts`) AS netReceipts FROM `CBAgreementInvoiceDetails` WHERE  vendorId IN ('"
														+ vendorId + "') AND agreementId = '" + agreementId
														+ "' GROUP BY `invoiceNumber`) AS z ON (a.invoiceNumber=z.invoiceNumber AND a.agreementId=z.agreementId) SET a.`netReceipt` = z.netReceipts where  a.vendorId IN ('"
														+ vendorId + "')");
												jdbcTemplate.execute(
														" UPDATE `CBAgreementInvoice` a JOIN (SELECT `invoiceNumber`, `agreementId`,SUM(`netReceipts`) AS netReceipts FROM `CBAgreementInvoiceDetails` WHERE  vendorId IN ('"
																+ vendorId + "') AND agreementId = '" + agreementId
																+ "' GROUP BY `invoiceNumber`) AS z ON (a.invoiceNumber=z.invoiceNumber AND a.agreementId=z.agreementId) SET a.`netReceipt` = z.netReceipts where  a.vendorId IN ('"
																+ vendorId + "')");
												file.close();
												tempFile.delete();
											}

										} catch (FileNotFoundException e) {
											e.printStackTrace();
										} catch (IOException e) {
											e.printStackTrace();
										}
									} else {
										logger.info("Negative Coop Invoice Amount " + orginialAmt);
										logger.info("UPDATE CBAgreementInvoice set comment='Negative Amount : "
												+ orginialAmt + "' where agreementId='" + vAgreementId
												+ "'and invoiceNumber='" + invoiceNumber + "'and vendorId='" + vendorId
												+ "'");
										jdbcTemplate.execute("UPDATE CBAgreementInvoice set comment='Negative Amount : "
												+ orginialAmt + "' where agreementId='" + vAgreementId
												+ "'and invoiceNumber='" + invoiceNumber + "'and vendorId='" + vendorId
												+ "'");
									}
								} catch (Exception e) {
									e.printStackTrace();

									String startDt = "";
									String endDt = "";

									String Uniquekey = vAgreementId + invoiceNumber;
									String currency = "USD";

									// String orginialAmt = data.get(5).replaceAll("[^\\d.]", "");
//										String nodatafound = "Y";
//										String comment = "No Data Found";
									String createDt = "NOW()";
									String orginialAmt = commonUtilObj.processAmount(data.get(5));
									logger.info("Amount : " + orginialAmt);
									StringBuilder sb = new StringBuilder(
											"INSERT IGNORE INTO CBAgreementInvoice (vendorId,uniqueKey,agreementId,invoiceNumber,billedAmount,fundingType,startDate,endDate,createdDate,currency) VALUES");
									sb.append("('" + vendorId + "','" + Uniquekey + "','" + vAgreementId + "','"
											+ invoiceNumber + "','" + orginialAmt + "','" + data.get(4) + "','"
											+ startDt + "','" + endDt + "'," + createDt + ",'" + currency + "')");

									jdbcTemplate.execute(sb.toString());

									return false;
								}

								try {
									Thread.sleep(1000);
								} catch (InterruptedException ie) {
								}

								Locator closeButton = page.locator("//*[@id=\"a-popover-2\"]/div/header/button");
								closeButton.click();
							}

						}

						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
						}

						// row++;

					} catch (Exception e) {
						e.printStackTrace();
						break;
					}

					// }

					try {
						Locator pagination = page
								.locator("kat-pagination[id='pagination'] span[part='pagination-nav-right']");

						if (pagination.getAttribute("class").equalsIgnoreCase("nav item end")) {
							break;
						} else {
							pagination.hover();
							pagination.click();
							isFirstPage = true;
						}
						Thread.sleep(2000);
					} catch (NoSuchElementException e) {
						e.printStackTrace();
						break;
					} catch (Exception e) {
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

		}

		return true;

	}

	private String isDownloadSectionAvailable(Page page, String vAgreementId, String invoiceNumber, String vendorId,
			double originalAmount) {

		page.waitForTimeout(5000);
		boolean isBackUpReportVisible = page.isVisible("//*[@id='backup-report-section']");

		if (originalAmount >= 0 && isBackUpReportVisible) {
			return "true";
		} else {
			if (originalAmount < 0) {
				logger.info("UPDATE CBAgreementInvoice set comment='Negative Original Amount : " + originalAmount
						+ "' where agreementId='" + vAgreementId + "'and invoiceNumber='" + invoiceNumber
						+ "'and vendorId='" + vendorId + "'");
				jdbcTemplate.execute("UPDATE CBAgreementInvoice set comment='Negative Original Amount : "
						+ originalAmount + "' where agreementId='" + vAgreementId + "'and invoiceNumber='"
						+ invoiceNumber + "'and vendorId='" + vendorId + "'");
				Locator closeButton = page.locator("//*[@id=\"a-popover-2\"]/div/header/button");
				closeButton.click();
				return "false";
			} else {
				return "true";
			}
		}

	}
}
