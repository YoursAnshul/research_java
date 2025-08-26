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
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.CBAgreementInvoiceDetails;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class CoopAgreementBackupDownload {

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

							try {
								CommonUtil.updateTimeStamp();
							} catch (IOException e) {
								e.printStackTrace();
							}

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
							sql2.append("Select count(*) from `CBAgreementInvoiceDetails` where agreementID='" + agreementId
									+ "' AND invoiceNumber ='" + invoiceNumber + "' AND vendorId ='" + vendorId + "'");
							
							logger.info("Select count(*) from `CBAgreementInvoiceDetails` where agreementID='" + agreementId
									+ "' AND invoiceNumber ='" + invoiceNumber + "' AND vendorId ='" + vendorId + "'");
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
											page.waitForTimeout(2000);
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
									if (startDtElement.count() == 0) {
										logger.info(
												"UPDATE CBAgreementInvoice set comment='No Records found' where agreementId='"
														+ vAgreementId + "'and invoiceNumber='" + invoiceNumber
														+ "'and vendorId='" + vendorId + "'");
										jdbcTemplate.execute(
												"UPDATE CBAgreementInvoice set comment='No Records found' where agreementId='"
														+ vAgreementId + "'and invoiceNumber='" + invoiceNumber
														+ "'and vendorId='" + vendorId + "'");
										
										page.locator("//div[@id=\"a-popover-2\"]//button[@data-action='a-popover-close']").click();
										continue;
									}
							
									String startDt = startDtElement.innerText();

									startDtElement = page.locator("//*[@id=\"backup-report-table\"]/tbody/tr[2]/td[2]");
									String endDt = startDtElement.innerText();

									String Uniquekey = vAgreementId + invoiceNumber;
									String currency = data.get(5).replaceAll("[\\d.]", "").replaceAll(",", "").trim();
									currency = commonUtilObj.getCurrency(currency);
									String amt = data.get(5);
									if (amt.contains("€") || amt.contains("KR") || amt.contains("ZŁ")) {
										amt = commonUtilObj.replaceCurrency(amt).trim();
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

									// String orginialAmt = amt.replaceAll("[^\\d.-]", "");

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
											// set override to 100 MB
											IOUtils.setByteArrayMaxOverride(100 * 1024 * 1024);
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

															List<CBAgreementInvoiceDetails> rowList = new ArrayList();
															try {
																logger.info("Agreement Rebate Currecncy Column :"
																		+ IsRebateAgreementCurrencyColumn);
																for (int rownum = 0; rownum < 100; rownum++) {
																	Row currentRow = rows1.next();
																	if (rowNumber == 0) {
																		rowNumber++;
																		continue;
																	}
																	rowNumber++;

																	logger.info("");
																	CBAgreementInvoiceDetails cbAgreementInvoiceDetails = new CBAgreementInvoiceDetails();
																	try {

																		if (currentRow
																				.getCell(firstRow.get("Receive Date"))
																				.getStringCellValue().equals("Total")) {
																			System.out.println("End of file");
																			break;

																		}

																		cbAgreementInvoiceDetails
																				.setReceiveDate(currentRow
																						.getCell(firstRow
																								.get("Receive Date"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setReceiveDate("");
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

																		cbAgreementInvoiceDetails.setQty(
																				(int) currentRow.getCell(firstRow.get(
																						"Revised Invoice Quantity"))
																						.getNumericCellValue());

																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setQty(0);
																	} catch (IllegalStateException e1) {
																		cbAgreementInvoiceDetails.setQty(0);
																	}

																	try {

																		cbAgreementInvoiceDetails
																				.setNetReceipts(currentRow.getCell(4)
																						.getNumericCellValue());

																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setNetReceipts(0.0);
																	}

																	try {

																		try {
																			cbAgreementInvoiceDetails.setRebate(
																					(double) currentRow.getCell(firstRow
																							.get("Revised Invoice Vendor Funding"))
																							.getNumericCellValue());
																		} catch (Exception ex) {
																			cbAgreementInvoiceDetails.setRebate(
																					(double) currentRow.getCell(firstRow
																							.get("Revised Invoice Rebate"))
																							.getNumericCellValue());
																		}

																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setRebate(0.0);
																	}

																	try {
																		try {

																			cbAgreementInvoiceDetails.setCurrency(
																					currentRow.getCell(firstRow.get(
																							"Revised Invoice Vendor Funding")
																							+ 1).getStringCellValue());

																		} catch (Exception e1) {
																			cbAgreementInvoiceDetails.setCurrency(
																					currentRow.getCell(firstRow.get(
																							"Revised Invoice Rebate")
																							+ 1).getStringCellValue());
																		}
																	} catch (Exception ex) {

																	}
																	
																	try {
																		try {

																			cbAgreementInvoiceDetails.setPoCurrency(
																					currentRow.getCell(firstRow.get(
																							"Revised Invoice Vendor Funding")
																							+ 1).getStringCellValue());

																		} catch (Exception e1) {
																			cbAgreementInvoiceDetails.setPoCurrency(
																					currentRow.getCell(firstRow.get(
																							"Revised Invoice Rebate")
																							+ 1).getStringCellValue());
																		}
																	} catch (Exception ex) {

																	}
																	
														
																	try {

																		String purchaseOrder = currentRow
																				.getCell(firstRow.get("Purchase Order"))
																				.getStringCellValue();
																		cbAgreementInvoiceDetails.setPO(purchaseOrder);
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setPO("");

																	}
																	try {
																		cbAgreementInvoiceDetails.setASIN(
																				currentRow.getCell(firstRow.get("Asin"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setASIN("");
																	}
																	try {
																		cbAgreementInvoiceDetails.setPOASIN(
																				cbAgreementInvoiceDetails.getPO()
																						+ cbAgreementInvoiceDetails
																								.getASIN());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setPOASIN(null);
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
																				.setRevisedInvoiceQty((double)currentRow
																						.getCell(firstRow
																								.get("Revised Invoice Quantity"))
																						.getNumericCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setRevisedInvoiceQty(0.0);
																	}
																	
																	try {
																		String uniqueKey = agreementId + invoiceNumber
																				+ cbAgreementInvoiceDetails.getPO()
																				+ cbAgreementInvoiceDetails.getASIN()
																				+ String.valueOf(
																						cbAgreementInvoiceDetails
																								.getQty())
																				+ cbAgreementInvoiceDetails
																						.getReceiveDate()
																				+ String.valueOf(rowNumber)
																				+ String.valueOf(numOfSheet)+cbAgreementInvoiceDetails.getRebate();

																		cbAgreementInvoiceDetails
																				.setUniqueKey(uniqueKey);

																	} catch (Exception e) {

																		e.printStackTrace();
																	}

																	if (cbAgreementInvoiceDetails.getASIN().equals("")
																			&& cbAgreementInvoiceDetails.getPO()
																					.equals("")
																			&& cbAgreementInvoiceDetails.getQty() == 0
																			&& cbAgreementInvoiceDetails
																					.getTransactionType().equals("")
																			&& cbAgreementInvoiceDetails
																					.getNetReceipts() == 0.0
																			&& cbAgreementInvoiceDetails
																					.getManufacturer().equals("")
																			&& cbAgreementInvoiceDetails
																					.getDistributor().equals("")) {
																		break;
																	}

																	rowList.add(cbAgreementInvoiceDetails);

																}
															} catch (Exception e1) {
																e1.printStackTrace();
															}
															try {
																CommonUtil.updateTimeStamp();
															} catch (IOException e) {
																e.printStackTrace();
															}

															logger.info("List Size " + rowList.size());
															if (rowList.size() == 0) {
																break;
															}
															boolean rowsAdded = false;
															StringBuilder sb1 = new StringBuilder(
																	"INSERT IGNORE INTO `CBAgreementInvoiceDetails`(`requestId`,`vendorId`, `agreementID`,`invoiceNumber`, `ASIN`,`PO`, `POASIN`, `Qty`, `transactionType`, `netReceipts`, `rebate`, `distributor`, `currency`, `createdDate`,`receiveDate`,`productGroup`,`category`,`subCategory`,`manufacturer`,`uniqueKey`,`poCurrency`,`invoiceType`,`newInvoiceQty`) VALUES");
															for (int row11 = 0; row11 < rowList.size(); row11++) {
																if (!rowList.get(row11).getTransactionType().trim()
																		.equalsIgnoreCase("Customer Shipment")) {
																	rowsAdded = true;
																	sb1.append("('" + requestId + "','" + vendorId
																			+ "','" + agreementId + "','"
																			+ invoiceNumber + "','"
																			+ rowList.get(row11).getASIN() + "','"
																			+ rowList.get(row11).getPO() + "','"
																			+ rowList.get(row11).getPOASIN() + "','"
																			+ rowList.get(row11).getQty() + "','"
																			+ rowList.get(row11).getTransactionType()
																			+ "','"
																			+ commonUtilObj.processAmount(
																					rowList.get(row11).getNetReceipts()
																							.toString())
																			+ "','"
																			+ commonUtilObj.processAmount(rowList
																					.get(row11).getRebate().toString())
																			+ "','"
																			+ rowList.get(row11).getDistributor()
																			+ "','" + rowList.get(row11).getCurrency()
																			+ "','" + java.time.LocalDate.now() + "', '"
																			+ rowList.get(row11).getReceiveDate()
																			+ "','"
																			+ rowList.get(row11).getProductGroup()
																			+ "',\"" + rowList.get(row11).getCategory()
																			+ "\",\""
																			+ rowList.get(row11).getSubCategory()
																			+ "\",'"
																			+ rowList.get(row11).getManufacturer()
																			+ "','" + rowList.get(row11).getUniqueKey()+"-"+row11
																			+ "','" + rowList.get(row11).getPoCurrency()
																			+ "','Revised'"
																			 +",'"+ rowList.get(row11).getRevisedInvoiceQty()
																			+ "')");

																	sb1.append(",");
																}

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
															List<CBAgreementInvoiceDetails> rowList = new ArrayList();
															try {
																logger.info("Agreement Rebate Currecncy Column :"
																		+ IsRebateAgreementCurrencyColumn);
																for (int rownum = 0; rownum < 100; rownum++) {
																	Row currentRow = rows1.next();
																	if (rowNumber == 0) {
																		rowNumber++;
																		continue;
																	}
																	rowNumber++;

																	logger.info("");
																	CBAgreementInvoiceDetails cbAgreementInvoiceDetails = new CBAgreementInvoiceDetails();
																	try {

																		if (currentRow
																				.getCell(firstRow.get("Receive Date"))
																				.getStringCellValue().equals("Total")) {
																			System.out.println("End of file");
																			break;

																		}

																		cbAgreementInvoiceDetails
																				.setReceiveDate(currentRow
																						.getCell(firstRow
																								.get("Receive Date"))
																						.getStringCellValue());
																	} catch (NullPointerException e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setReceiveDate("");
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
																		try
																		{
																			cbAgreementInvoiceDetails
																			.setQty((int) currentRow.getCell(firstRow.get(
																					"Quantity"))
																					.getNumericCellValue());
																		}
																		catch(Exception ex)
																		{
																			cbAgreementInvoiceDetails
																			.setQty((int) currentRow.getCell(4)
																					.getNumericCellValue());
																		}
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setQty(0);
																	} catch (IllegalStateException e1) {
																		cbAgreementInvoiceDetails.setQty(0);
																	}

																	try {
																		
																		if (IsRebateAgreementCurrencyColumn) {
																			try
																			{	cbAgreementInvoiceDetails.setNetReceipts(
																					currentRow.getCell(firstRow.get("Net Sales"))
																					.getNumericCellValue());
																			
																			
																			}
																			catch(Exception ex)
																			{
																				try
																				{
																				
																				cbAgreementInvoiceDetails.setNetReceipts(
																						currentRow.getCell(firstRow.get("Cost"))
																								.getNumericCellValue());
																				}
																				catch(Exception ex1)
																				{
																					cbAgreementInvoiceDetails.setNetReceipts(
																							currentRow.getCell(firstRow.get("Net Receipts"))
																									.getNumericCellValue());
																				}
																			
																			}
																		} else {
																			try
																			{
																			cbAgreementInvoiceDetails.setNetReceipts(
																					currentRow.getCell(firstRow.get("Net Receipts"))
																							.getNumericCellValue());
																			}
																			catch(Exception ex)
																			{
																				try
																				{
																					cbAgreementInvoiceDetails.setNetReceipts(
																					currentRow.getCell(firstRow.get("Cost"))
																							.getNumericCellValue());
																				}
																				catch(Exception ex1)
																				{
																					cbAgreementInvoiceDetails.setNetReceipts(
																							currentRow.getCell(firstRow.get("Net Sales"))
																							.getNumericCellValue());
																				}
																			}
																		}
									
																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setNetReceipts(0.0);
																	}

																	try {

																		if (IsRebateAgreementCurrencyColumn) {
																			cbAgreementInvoiceDetails.setRebate(
																					currentRow.getCell(firstRow.get(
																							"Vendor Funding In Agreement Currency"))
																							.getNumericCellValue());
																		} else {
																			try
																			{
																			cbAgreementInvoiceDetails.setRebate(
																					currentRow.getCell(firstRow.get(
																							"Rebate In Agreement Currency"))
																							.getNumericCellValue());
																			}
																			catch(Exception ex)
																			{
																			cbAgreementInvoiceDetails
																					.setRebate(currentRow.getCell(9)
																							.getNumericCellValue());
																			}
																		}

																	} catch (Exception e1) {
																		// e1.printStackTrace();
																		cbAgreementInvoiceDetails.setRebate(0.0);
																	}

																	try {

																		if (IsRebateAgreementCurrencyColumn) {
																			cbAgreementInvoiceDetails.setCurrency(
																					currentRow.getCell(firstRow
																							.get("Agreement Currency"))
																							.getStringCellValue());
																		} else {
																			try
																			{
																				cbAgreementInvoiceDetails.setCurrency(
																						currentRow.getCell(firstRow
																								.get("Agreement Currency"))
																								.getStringCellValue());
																			}
																			catch(Exception ex)
																			{
																				cbAgreementInvoiceDetails
																				.setCurrency(currentRow.getCell(10)
																						.getStringCellValue());
																			}
																			
																		}

																	} catch (Exception e1) {

																	}
																	try {

																		if (IsRebateAgreementCurrencyColumn) {
																			cbAgreementInvoiceDetails.setPoCurrency(
																					currentRow.getCell(firstRow.get(
																							"Purchase Order Currency"))
																							.getStringCellValue());
																		} else {
																			try
																			{
																				cbAgreementInvoiceDetails.setPoCurrency(
																						currentRow.getCell(firstRow.get(
																								"Purchase Order Currency"))
																								.getStringCellValue());
																			}
																			catch(Exception ex)
																			{
																				cbAgreementInvoiceDetails.setPoCurrency(
																						currentRow.getCell(12)
																								.getStringCellValue());
																			}

																		}

																	} catch (Exception e1) {

																	}
																	try {

																		String purchaseOrder = currentRow
																				.getCell(firstRow.get("Purchase Order"))
																				.getStringCellValue();
																		cbAgreementInvoiceDetails.setPO(purchaseOrder);
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setPO("");

																	}
																	try {
																		cbAgreementInvoiceDetails.setASIN(
																				currentRow.getCell(firstRow.get("Asin"))
																						.getStringCellValue());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setASIN("");
																	}
																	try {
																		cbAgreementInvoiceDetails.setPOASIN(
																				cbAgreementInvoiceDetails.getPO()
																						+ cbAgreementInvoiceDetails
																								.getASIN());
																	} catch (NullPointerException e) {
																		// e.printStackTrace();
																		cbAgreementInvoiceDetails.setPOASIN(null);
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
																		String uniqueKey = agreementId + invoiceNumber
																				+ cbAgreementInvoiceDetails.getPO()
																				+ cbAgreementInvoiceDetails.getASIN()
																				+ String.valueOf(
																						cbAgreementInvoiceDetails
																								.getQty())
																				+ cbAgreementInvoiceDetails
																						.getReceiveDate()
																				+ String.valueOf(rowNumber)
																				+ String.valueOf(numOfSheet);

																		cbAgreementInvoiceDetails
																				.setUniqueKey(uniqueKey);

																	} catch (Exception e) {

																		e.printStackTrace();
																	}

																	if (cbAgreementInvoiceDetails.getASIN().equals("")
																			&& cbAgreementInvoiceDetails.getPO()
																					.equals("")
																			&& cbAgreementInvoiceDetails.getQty() == 0
																			&& cbAgreementInvoiceDetails
																					.getTransactionType().equals("")
																			&& cbAgreementInvoiceDetails
																					.getNetReceipts() == 0.0
																			&& cbAgreementInvoiceDetails
																					.getManufacturer().equals("")
																			&& cbAgreementInvoiceDetails
																					.getDistributor().equals("")) {
																		break;
																	}

																	rowList.add(cbAgreementInvoiceDetails);

																}
															} catch (Exception e1) {
																e1.printStackTrace();
															}
															try {
																CommonUtil.updateTimeStamp();
															} catch (IOException e) {
																e.printStackTrace();
															}

															logger.info("List Size " + rowList.size());
															if (rowList.size() == 0) {
																break;
															}
															boolean rowsAdded = false;
															StringBuilder sb1 = new StringBuilder(
																	"INSERT IGNORE INTO `CBAgreementInvoiceDetails`(`requestId`,`vendorId`, `agreementID`,`invoiceNumber`, `ASIN`,`PO`, `POASIN`, `Qty`, `transactionType`, `netReceipts`, `rebate`, `distributor`, `currency`, `createdDate`,`receiveDate`,`productGroup`,`category`,`subCategory`,`manufacturer`,`uniqueKey`,`poCurrency`) VALUES");
															for (int row11 = 0; row11 < rowList.size(); row11++) {
																if (!rowList.get(row11).getTransactionType().trim()
																		.equalsIgnoreCase("Customer Shipment")) {
																	rowsAdded = true;
																	sb1.append("('" + requestId + "','" + vendorId
																			+ "','" + agreementId + "','"
																			+ invoiceNumber + "','"
																			+ rowList.get(row11).getASIN() + "','"
																			+ rowList.get(row11).getPO() + "','"
																			+ rowList.get(row11).getPOASIN() + "','"
																			+ rowList.get(row11).getQty() + "','"
																			+ rowList.get(row11).getTransactionType()
																			+ "','"
																			+ commonUtilObj.processAmount(
																					rowList.get(row11).getNetReceipts()
																							.toString())
																			+ "','"
																			+ commonUtilObj.processAmount(rowList
																					.get(row11).getRebate().toString())
																			+ "','"
																			+ rowList.get(row11).getDistributor()
																			+ "','" + rowList.get(row11).getCurrency()
																			+ "','" + java.time.LocalDate.now() + "', '"
																			+ rowList.get(row11).getReceiveDate()
																			+ "','"
																			+ rowList.get(row11).getProductGroup()
																			+ "',\"" + rowList.get(row11).getCategory()
																			+ "\",\""
																			+ rowList.get(row11).getSubCategory()
																			+ "\",'"
																			+ rowList.get(row11).getManufacturer()
																			+ "','" + rowList.get(row11).getUniqueKey()+"-"+row11
																			+ "','" + rowList.get(row11).getPoCurrency()
																			+ "')");

																	sb1.append(",");
																	logger.info(sb1.toString());
																}

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
										logger.info("Negative CBAgreementInvoice Amount " + orginialAmt);
										logger.info("UPDATE CBAgreementInvoice set comment='Negative Amount : " + orginialAmt
												+ "' where agreementId='" + vAgreementId + "'and invoiceNumber='"
												+ invoiceNumber + "'and vendorId='" + vendorId + "'");
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
						try {
							CommonUtil.updateTimeStamp();
						} catch (IOException e) {
							e.printStackTrace();
						}
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
				jdbcTemplate.execute("UPDATE CBAgreementInvoice set comment='Negative Original Amount : " + originalAmount
						+ "' where agreementId='" + vAgreementId + "'and invoiceNumber='" + invoiceNumber
						+ "'and vendorId='" + vendorId + "'");
				Locator closeButton = page.locator("//*[@id=\"a-popover-2\"]/div/header/button");
				closeButton.click();
				return "false";
			} else {
				return "true";
			}
		}

	}
}
