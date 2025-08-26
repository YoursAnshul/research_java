package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.CBRemittanceData;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

@Component
public class PaymentDataUpdationProcess {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private CommonUtil commonUtil;
	public static final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");


	public boolean processPage(Page page, Integer requestId, String vendorName, String vendorId,
			Date invoiceCreatedDate, Date cronTriggereddate,Path downloadPath) {
		try {

			String vName = vendorName.substring(0, 2);
			logger.info("VendorId : " + vendorId);

			logger.info("StrtDate " + invoiceCreatedDate);
			logger.info("EndDate " + cronTriggereddate);

			long diffInMilliseconds = Math.abs(cronTriggereddate.getTime() - invoiceCreatedDate.getTime());
			long daysDifference = TimeUnit.DAYS.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);

			logger.info("Difference between dates in days: " + daysDifference);

			File Payments = new File(CommonUtil.BasePath + "Payments.xlsx");
			if (Payments.exists()) {
				Payments.delete();
			}
			page.click("//div[@aria-label='Navigation menu']");
			try {
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
			} catch (Exception e) {
				page.reload();
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
			}

			page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Remittance')]");
			try {
				Thread.sleep(1500);
				Locator countriesDropDown = page.locator("//kat-dropdown[@id='countriesDropDown']");
				if (countriesDropDown.count() > 0) {
					countriesDropDown.click();
					List<Locator> listofCountries = page.locator("//kat-option[@role='option']").all();
					for (Locator country : listofCountries) {
						logger.info("CountryName : " + country.innerText());
						String countryName = country.innerText().replaceAll("[\\[\\](){}\\s]", "");
						String cntry = countryName.substring(countryName.length() - 2);
						logger.info("Country " + cntry);
						if (vName.equals(cntry)) {
							country.click();
							Thread.sleep(3000);
							break;
						}
					}
				}
			} catch (Exception e) {

			}

			LocalDate currentDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(invoiceCreatedDate));
			// LocalDate currentDate =
			// invoiceCreatedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(cronTriggereddate));
			String startDateAsString = null;
			String endDateAsString = null;
			while (ChronoUnit.DAYS.between(currentDate, endDate) >= 90) {
				// Calculate the next output date
				LocalDate outputDate = currentDate.plusDays(90);

				if (vName.startsWith("CA") || vName.startsWith("US")) {
					// currentDate = LocalDate.now();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
					startDateAsString = currentDate.format(formatter);
					// outputDate = LocalDate.now();
					endDateAsString = outputDate.format(formatter);
				} else {
					// currentDate = LocalDate.now();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					startDateAsString = currentDate.format(formatter);
					// outputDate = LocalDate.now();
					endDateAsString = outputDate.format(formatter);
				}
				logger.info("startDate : " + startDateAsString);
				logger.info("endDate : " + endDateAsString);
				processPageForFurther(page, requestId, vendorId, vName, startDateAsString, endDateAsString,downloadPath);
				currentDate = outputDate;
			}
			if (vName.startsWith("CA") || vName.startsWith("US")) {
				// currentDate = LocalDate.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
				startDateAsString = currentDate.format(formatter);
				// outputDate = LocalDate.now();
				endDateAsString = endDate.format(formatter);
			} else {
				// currentDate = LocalDate.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				startDateAsString = currentDate.format(formatter);
				// outputDate = LocalDate.now();
				endDateAsString = endDate.format(formatter);
			}
			processPageForFurther(page, requestId, vendorId, vName, startDateAsString, endDateAsString,downloadPath);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean processPageForFurther(Page page, Integer requestId, String vendorId, String vName,
			String startDateAsString, String endDateAsString, Path downloadPath) {
		try {
			Locator fromDate = page.locator("#from-date-wrap").locator("#from-date");
			Locator toDate = page.locator("#to-date-wrap").locator("#to-date");
			Locator serachButton = page.locator("#remittanceSearchForm-submit");
			fromDate.fill(startDateAsString);
			Locator randomClick = null;
			try {
				randomClick = page.locator("#selected-remittance-count");
				randomClick.click();
			} catch (Exception e) {
				commonUtil.closePopup(page);
				randomClick.click();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			toDate.fill(endDateAsString);
			try {
				randomClick = page.locator("#selected-remittance-count");
				randomClick.click();
			} catch (Exception e) {
				commonUtil.closePopup(page);
				randomClick.click();
			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				serachButton.click();
			} catch (Exception e) {
				commonUtil.closePopup(page);
				serachButton.click();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			Thread.sleep(2000);
			try {
				Locator selectAll = page.locator("#remittance-home-select-all");
				selectAll.click();
			} catch (PlaywrightException e) {
				logger.info("No data between Start Date and End Date");
				return true;
			} catch (Exception e1) {
				commonUtil.closePopup(page);
				page.click("#remittance-home-select-all");

			}
			Download download = null;
			for (int i = 0; i < 5; i++) {
				try {
					Locator exportAll = page.locator("#remittance-home-export-link");
					 download = page.waitForDownload(() -> {
						exportAll.click();
					});
					break;
				} catch (Exception e) {
					commonUtil.closePopup(page);
				}
			}

			try {
				Path filePath = downloadPath.resolve(download.suggestedFilename());
				System.out.println("Downloading file to: " + filePath);

				// Save the downloaded file to the specified path
				download.saveAs(filePath);
				File tempFile = filePath.toFile();
				FileInputStream file = new FileInputStream(tempFile);
				@SuppressWarnings("resource")
				XSSFWorkbook workbook = new XSSFWorkbook(file);
				XSSFSheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rows = sheet.iterator(); // iterating over excel file
				FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

				int rowNumberForPaymentNumber = 0;

				for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
					boolean paymentNumberRowFlag = false;
					while (rows.hasNext() && paymentNumberRowFlag == false) {
						Row currentRow = rows.next();
						Iterator<Cell> cellIterator = currentRow.cellIterator();

						while (cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							switch (cell.getCellType()) {
							case STRING: // field that represents string cell type
								if (cell.getStringCellValue().trim().equals("Payment Number")) {
									paymentNumberRowFlag = true;
									rowNumberForPaymentNumber = currentRow.getRowNum();
								}
								break;
							case NUMERIC: // field that represents number cell type
								System.out.println(cell.getNumericCellValue());
								break;
							case BLANK:
								continue;

							default:
								break;
							}
						}
					}
				}

				HashMap<String, String> paymentDateMap = new HashMap<>();
				int remittanceDataEndRow = 0;

				for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
					while (rows.hasNext()) {
						for (int r = rowNumberForPaymentNumber + 1; r <= sheet.getLastRowNum(); r++) {
							try {
								String paymentNumber = sheet.getRow(r).getCell(0).getStringCellValue();
								logger.info("paymentNumber : " + paymentNumber);

								String paymentDate = sheet.getRow(r).getCell(1).getStringCellValue();
								logger.info("paymentDate : " + paymentDate);
								paymentDateMap.put(paymentNumber, paymentDate);
							} catch (NullPointerException e) {
								remittanceDataEndRow = r;
								break;
							}
						}

						break;

					}
				}

				logger.info("paymentNumberEndRow : " + remittanceDataEndRow);
				logger.info("HashMap : " + paymentDateMap);
				logger.info("Last row : " + sheet.getLastRowNum());

				for (int r = remittanceDataEndRow; r <= sheet.getLastRowNum(); r++) {

					XSSFRow row = sheet.getRow(r);
					if (row != null) {
						if (formulaEvaluator.evaluateInCell(row.getCell(0)).getCellType() == CellType.STRING) {
							System.out.println("CellName : " + row.getCell(0).getStringCellValue());
							if (row.getCell(0).getStringCellValue().trim().equals("Invoices")) {
								rowNumberForPaymentNumber = r;
								break;
							}

						}
					}
				}
				HashMap<String, Integer> firstRow = new HashMap<String, Integer>();
				Row r = sheet.getRow(rowNumberForPaymentNumber + 1);
				for (int cn = 0; cn < r.getLastCellNum(); cn++) {
					Cell c = r.getCell(cn);
					System.out.println("CellValue : " + c.getStringCellValue());
					if (c == null || c.getCellType() == CellType.BLANK) {
						// Can't be this cell - it's empty
						continue;
					}
					if (c.getCellType() == CellType.STRING) {
						String text = c.getStringCellValue();
						firstRow.put(text, cn);

					}
				}

				logger.info("First row map : " + firstRow);

					List<CBRemittanceData> objCBRemittanceDataList = new ArrayList<CBRemittanceData>();
					int count = 0;
					int limit = 100;
					for (int i = rowNumberForPaymentNumber + 2; i <= sheet.getLastRowNum(); i++) {
						CBRemittanceData objCBRemittanceData = new CBRemittanceData();
						XSSFRow currentRow = sheet.getRow(i);
						if (count < limit) {
							count++;
							objCBRemittanceData.setRequestId(requestId);
							try {
								objCBRemittanceData.setPaymentNumber(
										currentRow.getCell(firstRow.get("Payment Number")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							objCBRemittanceData.setVendorId(vendorId);
							try {
								objCBRemittanceData.setInvoiceNumber(
										currentRow.getCell(firstRow.get("Invoice Number")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							String invoiceDate = null;
							try {
								invoiceDate = currentRow.getCell(firstRow.get("Invoice Date")).getStringCellValue();

								objCBRemittanceData.setInvoiceDate(invoiceDate);
							} catch (NullPointerException e) {

							}
							try {
								objCBRemittanceData.setDescription(
										currentRow.getCell(firstRow.get("Description")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							try {
								objCBRemittanceData.setInvoiceAmount(Double.parseDouble(
										currentRow.getCell(firstRow.get("Invoice Amount")).getStringCellValue()));
							} catch (NullPointerException e) {

							}
							try {
								objCBRemittanceData.setTermsDiscountTaken(Double.parseDouble(
										currentRow.getCell(firstRow.get("Terms Discount Taken")).getStringCellValue()));
							} catch (NullPointerException e) {

							}

							try {
								if (vName.startsWith("CA") || vName.startsWith("US")) {
									objCBRemittanceData.setAmountPaid(Double.parseDouble(currentRow
											.getCell(firstRow.get("Amount Paid")).getStringCellValue().replace("$", "")
											.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
											.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()));
								} else {
									objCBRemittanceData.setAmountPaid(
											Double.parseDouble(currentRow.getCell(firstRow.get("Net Amount Paid"))
													.getStringCellValue().replace("$", "").replace("€", "")
													.replace("£", "").replace("MXN", "").replace("AED", "")
													.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()));
								}
							} catch (NullPointerException e) {
								objCBRemittanceData.setAmountPaid(0.0);
							}
							try {
								objCBRemittanceData.setRemainingAmountAsOf(String.valueOf(Double.parseDouble(
										currentRow.getCell(firstRow.get("Remaining Amount")).getStringCellValue())));
							} catch (NullPointerException e) {

							}

							objCBRemittanceData.setCreatedDate(new Date());
							try {
								String paymentDate = paymentDateMap.get(objCBRemittanceData.getPaymentNumber());

								objCBRemittanceData.setPaymentDate((paymentDate));
							} catch (NullPointerException e) {

							}
							try {
								String currency = currentRow.getCell(firstRow.get("Invoice Currency"))
										.getStringCellValue();
								// currency = SphinxUtil.getCurrency(currency);
								objCBRemittanceData.setCurrency(currency);
							} catch (NullPointerException e) {

							}

							String uniqueKey = objCBRemittanceData.getPaymentNumber().trim()
									+ objCBRemittanceData.getInvoiceNumber().trim()
									+ String.valueOf(objCBRemittanceData.getAmountPaid()).replace("$", "")
											.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
											.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()
									+ invoiceDate.trim() + objCBRemittanceData.getVendorId();
							objCBRemittanceData.setUniqueKey(uniqueKey);
							objCBRemittanceDataList.add(objCBRemittanceData);

						} else {
							count = 0;
							objCBRemittanceData.setRequestId(requestId);
							try {
								objCBRemittanceData.setPaymentNumber(
										currentRow.getCell(firstRow.get("Payment Number")).getStringCellValue());
							} catch (NullPointerException e) {

							}

							objCBRemittanceData.setVendorId(vendorId);
							try {
								objCBRemittanceData.setInvoiceNumber(
										currentRow.getCell(firstRow.get("Invoice Number")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							String invoiceDate = null;
							try {
								invoiceDate = currentRow.getCell(firstRow.get("Invoice Date")).getStringCellValue();

								objCBRemittanceData.setInvoiceDate(invoiceDate);
							} catch (NullPointerException e) {

							}
							try {
								objCBRemittanceData.setDescription(
										currentRow.getCell(firstRow.get("Description")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							try {
								objCBRemittanceData.setInvoiceAmount(Double.parseDouble(
										currentRow.getCell(firstRow.get("Invoice Amount")).getStringCellValue()));
							} catch (NullPointerException e) {

							}
							try {
								objCBRemittanceData.setTermsDiscountTaken(Double.parseDouble(
										currentRow.getCell(firstRow.get("Terms Discount Taken")).getStringCellValue()));
							} catch (NullPointerException e) {

							}

							try {
								if (vName.startsWith("CA") || vName.startsWith("US")) {
									objCBRemittanceData.setAmountPaid(Double.parseDouble(currentRow
											.getCell(firstRow.get("Amount Paid")).getStringCellValue().replace("$", "")
											.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
											.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()));
								} else {
									objCBRemittanceData.setAmountPaid(
											Double.parseDouble(currentRow.getCell(firstRow.get("Net Amount Paid"))
													.getStringCellValue().replace("$", "").replace("€", "")
													.replace("£", "").replace("MXN", "").replace("AED", "")
													.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()));
								}
							} catch (NullPointerException e) {
								objCBRemittanceData.setAmountPaid(0.0);
							}
							try {
								objCBRemittanceData.setRemainingAmountAsOf(String.valueOf(Double.parseDouble(
										currentRow.getCell(firstRow.get("Remaining Amount")).getStringCellValue())));
							} catch (NullPointerException e) {

							}
							objCBRemittanceData.setCreatedDate(new Date());
							try {
								String paymentDate = paymentDateMap.get(objCBRemittanceData.getPaymentNumber());

								objCBRemittanceData.setPaymentDate((paymentDate));
							} catch (NullPointerException e) {

							}
							try {
								String currency = currentRow.getCell(firstRow.get("Invoice Currency"))
										.getStringCellValue();
								// currency = SphinxUtil.getCurrency(currency);
								objCBRemittanceData.setCurrency(currency);
							} catch (NullPointerException e) {

							}

							String uniqueKey = objCBRemittanceData.getPaymentNumber().trim()
									+ objCBRemittanceData.getInvoiceNumber().trim()
									+ String.valueOf(objCBRemittanceData.getAmountPaid()).replace("$", "")
											.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
											.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()
									+ invoiceDate.trim() + objCBRemittanceData.getVendorId();
							objCBRemittanceData.setUniqueKey(uniqueKey);
							objCBRemittanceDataList.add(objCBRemittanceData);
							StringBuilder sb1 = new StringBuilder(
									"Insert Ignore into CBMissingRemmittnaceData (requestId,paymentNumber, vendorId, invoiceNumber, invoiceDate, Description, invoiceAmount, termsDiscountTaken, amountPaid, remainingAmountAsOf, createdDate,paymentDate,currency,uniqueKey) Values");
							for (int row1 = 0; row1 < objCBRemittanceDataList.size(); row1++) {
								if (vName.startsWith("CA") || vName.startsWith("US")) {
									sb1.append("('" + objCBRemittanceDataList.get(row1).getRequestId() + "','"
											+ objCBRemittanceDataList.get(row1).getPaymentNumber() + "','"
											+ objCBRemittanceDataList.get(row1).getVendorId() + "',\""
											+ objCBRemittanceDataList.get(row1).getInvoiceNumber() + "\", STR_TO_DATE('"
											+ objCBRemittanceDataList.get(row1).getInvoiceDate() + "','%m/%d/%Y') ,\""
											+ objCBRemittanceDataList.get(row1).getDescription() + "\",'"
											+ objCBRemittanceDataList.get(row1).getInvoiceAmount() + "','"
											+ objCBRemittanceDataList.get(row1).getTermsDiscountTaken() + "','"
											+ objCBRemittanceDataList.get(row1).getAmountPaid() + "','"
											+ objCBRemittanceDataList.get(row1).getRemainingAmountAsOf() + "','"
											+ java.time.LocalDate.now() + "', STR_TO_DATE('"
											+ objCBRemittanceDataList.get(row1).getPaymentDate() + "','%m/%d/%Y') ,'"
											+ objCBRemittanceDataList.get(row1).getCurrency() + "',\""
											+ objCBRemittanceDataList.get(row1).getUniqueKey() + "\")");

									sb1.append(",");
								} else {
									sb1.append("('" + objCBRemittanceDataList.get(row1).getRequestId() + "','"
											+ objCBRemittanceDataList.get(row1).getPaymentNumber() + "','"
											+ objCBRemittanceDataList.get(row1).getVendorId() + "',\""
											+ objCBRemittanceDataList.get(row1).getInvoiceNumber() + "\", STR_TO_DATE('"
											+ objCBRemittanceDataList.get(row1).getInvoiceDate() + "','%d/%m/%Y') ,\""
											+ objCBRemittanceDataList.get(row1).getDescription() + "\",'"
											+ objCBRemittanceDataList.get(row1).getInvoiceAmount() + "','"
											+ objCBRemittanceDataList.get(row1).getTermsDiscountTaken() + "','"
											+ objCBRemittanceDataList.get(row1).getAmountPaid() + "','"
											+ objCBRemittanceDataList.get(row1).getRemainingAmountAsOf() + "','"
											+ java.time.LocalDate.now() + "', STR_TO_DATE('"
											+ objCBRemittanceDataList.get(row1).getPaymentDate() + "','%d/%m/%Y') ,'"
											+ objCBRemittanceDataList.get(row1).getCurrency() + "',\""
											+ objCBRemittanceDataList.get(row1).getUniqueKey() + "\")");

									sb1.append(",");
								}

							}
							String query = sb1.substring(0, sb1.length() - 1) + ";";

							try {
								logger.info("Query : " + query);
								jdbcTemplate.execute(query);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
							objCBRemittanceDataList.clear();
						}

					}
					StringBuilder sb1 = new StringBuilder(
							"Insert Ignore into CBMissingRemmittnaceData (requestId,paymentNumber, vendorId, invoiceNumber, invoiceDate, Description, invoiceAmount, termsDiscountTaken, amountPaid, remainingAmountAsOf, createdDate,paymentDate,currency,uniqueKey) Values");
					for (int row1 = 0; row1 < objCBRemittanceDataList.size(); row1++) {
						if (vName.startsWith("CA") || vName.startsWith("US")) {
							sb1.append("('" + objCBRemittanceDataList.get(row1).getRequestId() + "','"
									+ objCBRemittanceDataList.get(row1).getPaymentNumber() + "','"
									+ objCBRemittanceDataList.get(row1).getVendorId() + "',\""
									+ objCBRemittanceDataList.get(row1).getInvoiceNumber() + "\", STR_TO_DATE('"
									+ objCBRemittanceDataList.get(row1).getInvoiceDate() + "','%m/%d/%Y') ,\""
									+ objCBRemittanceDataList.get(row1).getDescription() + "\",'"
									+ objCBRemittanceDataList.get(row1).getInvoiceAmount() + "','"
									+ objCBRemittanceDataList.get(row1).getTermsDiscountTaken() + "','"
									+ objCBRemittanceDataList.get(row1).getAmountPaid() + "','"
									+ objCBRemittanceDataList.get(row1).getRemainingAmountAsOf() + "','"
									+ java.time.LocalDate.now() + "', STR_TO_DATE('"
									+ objCBRemittanceDataList.get(row1).getPaymentDate() + "','%m/%d/%Y') ,'"
									+ objCBRemittanceDataList.get(row1).getCurrency() + "',\""
									+ objCBRemittanceDataList.get(row1).getUniqueKey() + "\")");

							sb1.append(",");
						} else {
							sb1.append("('" + objCBRemittanceDataList.get(row1).getRequestId() + "','"
									+ objCBRemittanceDataList.get(row1).getPaymentNumber() + "','"
									+ objCBRemittanceDataList.get(row1).getVendorId() + "',\""
									+ objCBRemittanceDataList.get(row1).getInvoiceNumber() + "\", STR_TO_DATE('"
									+ objCBRemittanceDataList.get(row1).getInvoiceDate() + "','%d/%m/%Y') ,\""
									+ objCBRemittanceDataList.get(row1).getDescription() + "\",'"
									+ objCBRemittanceDataList.get(row1).getInvoiceAmount() + "','"
									+ objCBRemittanceDataList.get(row1).getTermsDiscountTaken() + "','"
									+ objCBRemittanceDataList.get(row1).getAmountPaid() + "','"
									+ objCBRemittanceDataList.get(row1).getRemainingAmountAsOf() + "','"
									+ java.time.LocalDate.now() + "', STR_TO_DATE('"
									+ objCBRemittanceDataList.get(row1).getPaymentDate() + "','%d/%m/%Y') ,'"
									+ objCBRemittanceDataList.get(row1).getCurrency() + "',\""
									+ objCBRemittanceDataList.get(row1).getUniqueKey() + "\")");

							sb1.append(",");
						}
					}
					String query = sb1.substring(0, sb1.length() - 1) + ";";

					try {
						System.out.println("Query : " + query);
						jdbcTemplate.execute(query);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					file.close();
					tempFile.delete();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}
}
