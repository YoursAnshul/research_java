package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.CBShortageReconciliation;
import com.dimetyd.bot.model.ShortageInvoice;
import com.dimetyd.bot.model.ShortageJobData;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

@Component
public class GoGreen_RemmittanceData {

	@Autowired
	CommonUtil CommonUtil;

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");

	public boolean processPage(Page page, Long id, String vendorId, String vendorName, String startDate, String endDate,
			Path downloadPath) {
		try {

			Download download;
			File tempFile = null;
			String vName = vendorName.substring(0, 2);
			logger.info("VendorId : " + vendorId);

			logger.info("StrtDate " + startDate);
			logger.info("EndDate " + endDate);
			File Payments = new File(downloadPath + "Payments.xlsx");
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

			String startDateAsString = null;
			String endDateAsString = null;
			 Date startdateASDate = inputFormat.parse(startDate);
			    Date endDateAsDate = inputFormat.parse(endDate);
			if (vName.startsWith("CA") || vName.startsWith("US")) {
				startDateAsString = formatterForUS.format(startdateASDate);
				endDateAsString = formatterForUS.format(endDateAsDate);
			} else {
				startDateAsString = formatterForOthers.format(startdateASDate);
				endDateAsString = formatterForOthers.format(endDateAsDate);
			}

			Locator fromDateParent = page.locator("#from-date-wrap");
			Locator fromDate = fromDateParent.locator("#from-date");
			Locator toDateParent = page.locator("#to-date-wrap");
			Locator toDate = toDateParent.locator("#to-date");
			Locator serachButton = page.locator("#remittanceSearchForm-submit");
			try {
				fromDate.click();
			} catch (Exception e) {
				CommonUtil.closePopup(page);
				fromDate.click();
			}

			logger.info(startDateAsString);

			page.waitForTimeout(500);
			fromDate.fill(startDateAsString);
			Locator randomClick = null;
			try {
				randomClick = page.locator("#selected-remittance-count");
				randomClick.click();
			} catch (Exception e) {
				CommonUtil.closePopup(page);
				randomClick.click();
			}
			fromDate.fill(startDateAsString);
			Thread.sleep(1000);
			toDate.fill(endDateAsString);
			try {
				randomClick = page.locator("#selected-remittance-count");
				randomClick.click();
			} catch (Exception e) {
				CommonUtil.closePopup(page);
				randomClick.click();
			}

			try {
				serachButton.click();
			} catch (Exception e) {
				CommonUtil.closePopup(page);
				serachButton.click();
			}
			Thread.sleep(3000);
			try {
				Locator selectAll = page.locator("#remittance-home-select-all");
				selectAll.click();
			} catch (PlaywrightException e) {
				logger.info("No data between Start Date and End Date");
				return true;
			} catch (Exception e1) {
				CommonUtil.closePopup(page);
				page.click("#remittance-home-select-all");

			}
			try {
				Locator exportAll = page.locator("#remittance-home-export-link");
				download = page.waitForDownload(() -> {
					exportAll.click();
				});
			} catch (Exception e) {
				CommonUtil.closePopup(page);
				Locator exportAll = page.locator("#remittance-home-export-link");
				download = page.waitForDownload(() -> {
					exportAll.click();
				});
			}

			try {
				Path filePath = downloadPath.resolve(download.suggestedFilename());
				logger.info("Downloading file to: " + filePath);

				// Save the downloaded file to the specified path
				download.saveAs(filePath);
				tempFile = filePath.toFile();

				boolean fileExists = CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));

				if (fileExists) {
					logger.info("File exists!");
				} else {
					logger.info("File does not exist within the timeout period.");

				}
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
						String sheetName = workbook.getSheetName(i).trim();
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
						String sheetName = workbook.getSheetName(i).trim();

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
							logger.info("CellName : " + row.getCell(0).getStringCellValue());
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
					logger.info("CellValue : " + c.getStringCellValue());
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

				List<CBShortageReconciliation> cbShortageReconciliationList = new ArrayList<CBShortageReconciliation>();
				int count = 0;
				int limit = 100;
				for (int i = rowNumberForPaymentNumber + 2; i <= sheet.getLastRowNum(); i++) {
					CBShortageReconciliation objCBShortageReconciliation = new CBShortageReconciliation();
					XSSFRow currentRow = sheet.getRow(i);
					if (count < limit) {
						count++;

						// objCBShortageReconciliation.setRequestId(Integer.parseInt(requestId));
						try {
							objCBShortageReconciliation.setPaymentNumber(
									currentRow.getCell(firstRow.get("Payment Number")).getStringCellValue());
						} catch (NullPointerException e) {

						}
						objCBShortageReconciliation.setVendorId(vendorId);
						try {
							objCBShortageReconciliation.setInvoiceNumber(
									currentRow.getCell(firstRow.get("Invoice Number")).getStringCellValue());
						} catch (NullPointerException e) {

						}
						String invoiceDate = null;
						try {
							invoiceDate = currentRow.getCell(firstRow.get("Invoice Date")).getStringCellValue();

							objCBShortageReconciliation.setInvoiceDate(invoiceDate);
						} catch (NullPointerException e) {

						}
						try {
							objCBShortageReconciliation.setDescription(
									currentRow.getCell(firstRow.get("Description")).getStringCellValue());
						} catch (NullPointerException e) {

						}
						try {
							objCBShortageReconciliation.setInvoiceAmount(Double.parseDouble(
									currentRow.getCell(firstRow.get("Invoice Amount")).getStringCellValue()));
						} catch (NullPointerException e) {

						}
						try {
							objCBShortageReconciliation.setTermsDiscountTaken(Double.parseDouble(
									currentRow.getCell(firstRow.get("Terms Discount Taken")).getStringCellValue()));
						} catch (NullPointerException e) {

						}

						try {
							if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("MX")
									|| vName.startsWith("BR")) {
								objCBShortageReconciliation.setAmountPaid(Double.parseDouble(currentRow
										.getCell(firstRow.get("Amount Paid")).getStringCellValue().replace("$", "")
										.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
										.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()));
							} else {
								objCBShortageReconciliation.setAmountPaid(Double.parseDouble(currentRow
										.getCell(firstRow.get("Net Amount Paid")).getStringCellValue().replace("$", "")
										.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
										.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()));
							}
						} catch (NullPointerException e) {
							objCBShortageReconciliation.setAmountPaid(0.0);
						}
						try {
							objCBShortageReconciliation.setRemainingAmountAsOf(String.valueOf(Double.parseDouble(
									currentRow.getCell(firstRow.get("Remaining Amount")).getStringCellValue())));
						} catch (NullPointerException e) {

						}

						objCBShortageReconciliation.setCreatedDate(new Date());
						try {
							String paymentDate = paymentDateMap.get(objCBShortageReconciliation.getPaymentNumber());

							objCBShortageReconciliation.setPaymentDate((paymentDate));
						} catch (NullPointerException e) {

						}
						try {
							String currency = currentRow.getCell(firstRow.get("Invoice Currency")).getStringCellValue();
							// currency = SphinxUtil.getCurrency(currency);
							objCBShortageReconciliation.setCurrency(currency);
						} catch (NullPointerException e) {

						}

						String uniqueKey = objCBShortageReconciliation.getPaymentNumber().trim()
								+ objCBShortageReconciliation.getInvoiceNumber().trim()
								+ String.valueOf(objCBShortageReconciliation.getAmountPaid()).replace("$", "")
										.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
										.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()
								+ invoiceDate.trim() + objCBShortageReconciliation.getVendorId();
						objCBShortageReconciliation.setUniqueKey(uniqueKey);
						cbShortageReconciliationList.add(objCBShortageReconciliation);

					} else {
						count = 0;

						// objCBShortageReconciliation.setRequestId(Integer.parseInt(requestId));
						try {
							objCBShortageReconciliation.setPaymentNumber(
									currentRow.getCell(firstRow.get("Payment Number")).getStringCellValue());
						} catch (NullPointerException e) {

						}

						objCBShortageReconciliation.setVendorId(vendorId);
						try {
							objCBShortageReconciliation.setInvoiceNumber(
									currentRow.getCell(firstRow.get("Invoice Number")).getStringCellValue());
						} catch (NullPointerException e) {

						}
						String invoiceDate = null;
						try {
							invoiceDate = currentRow.getCell(firstRow.get("Invoice Date")).getStringCellValue();

							objCBShortageReconciliation.setInvoiceDate(invoiceDate);
						} catch (NullPointerException e) {

						}
						try {
							objCBShortageReconciliation.setDescription(
									currentRow.getCell(firstRow.get("Description")).getStringCellValue());
						} catch (NullPointerException e) {

						}
						try {
							objCBShortageReconciliation.setInvoiceAmount(Double.parseDouble(
									currentRow.getCell(firstRow.get("Invoice Amount")).getStringCellValue()));
						} catch (NullPointerException e) {

						}
						try {
							objCBShortageReconciliation.setTermsDiscountTaken(Double.parseDouble(
									currentRow.getCell(firstRow.get("Terms Discount Taken")).getStringCellValue()));
						} catch (NullPointerException e) {

						}

						try {
							if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("MX")
									|| vName.startsWith("BR")) {
								objCBShortageReconciliation.setAmountPaid(Double.parseDouble(currentRow
										.getCell(firstRow.get("Amount Paid")).getStringCellValue().replace("$", "")
										.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
										.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()));
							} else {
								objCBShortageReconciliation.setAmountPaid(Double.parseDouble(currentRow
										.getCell(firstRow.get("Net Amount Paid")).getStringCellValue().replace("$", "")
										.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
										.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()));
							}
						} catch (NullPointerException e) {
							objCBShortageReconciliation.setAmountPaid(0.0);
						}
						try {
							objCBShortageReconciliation.setRemainingAmountAsOf(String.valueOf(Double.parseDouble(
									currentRow.getCell(firstRow.get("Remaining Amount")).getStringCellValue())));
						} catch (NullPointerException e) {

						}
						objCBShortageReconciliation.setCreatedDate(new Date());
						try {
							String paymentDate = paymentDateMap.get(objCBShortageReconciliation.getPaymentNumber());

							objCBShortageReconciliation.setPaymentDate((paymentDate));
						} catch (NullPointerException e) {

						}
						try {
							String currency = currentRow.getCell(firstRow.get("Invoice Currency")).getStringCellValue();
							// currency = SphinxUtil.getCurrency(currency);
							objCBShortageReconciliation.setCurrency(currency);
						} catch (NullPointerException e) {

						}

						String uniqueKey = objCBShortageReconciliation.getPaymentNumber().trim()
								+ objCBShortageReconciliation.getInvoiceNumber().trim()
								+ String.valueOf(objCBShortageReconciliation.getAmountPaid()).replace("$", "")
										.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "")
										.replaceAll("[^\\n\\r\\t\\p{Print}]", "").replace(",", "").trim()
								+ invoiceDate.trim() + objCBShortageReconciliation.getVendorId();
						objCBShortageReconciliation.setUniqueKey(uniqueKey);
						cbShortageReconciliationList.add(objCBShortageReconciliation);
						StringBuilder sb1 = new StringBuilder(
								"Insert Ignore into CBShortageReconciliation_cron (requestId,paymentNumber, vendorId, invoiceNumber, invoiceDate, Description, invoiceAmount, termsDiscountTaken, amountPaid, remainingAmountAsOf, createdDate,paymentDate,currency,uniqueKey) Values");
						for (int row1 = 0; row1 < cbShortageReconciliationList.size(); row1++) {
							if (vName.startsWith("CA") || vName.startsWith("US")) {
								sb1.append("('" + cbShortageReconciliationList.get(row1).getRequestId() + "','"
										+ cbShortageReconciliationList.get(row1).getPaymentNumber() + "','"
										+ cbShortageReconciliationList.get(row1).getVendorId() + "',\""
										+ cbShortageReconciliationList.get(row1).getInvoiceNumber()
										+ "\", STR_TO_DATE('" + cbShortageReconciliationList.get(row1).getInvoiceDate()
										+ "','%m/%d/%Y') ,\"" + cbShortageReconciliationList.get(row1).getDescription()
										+ "\",'" + cbShortageReconciliationList.get(row1).getInvoiceAmount() + "','"
										+ cbShortageReconciliationList.get(row1).getTermsDiscountTaken() + "','"
										+ cbShortageReconciliationList.get(row1).getAmountPaid() + "','"
										+ cbShortageReconciliationList.get(row1).getRemainingAmountAsOf() + "','"
										+ java.time.LocalDate.now() + "', STR_TO_DATE('"
										+ cbShortageReconciliationList.get(row1).getPaymentDate() + "','%m/%d/%Y') ,'"
										+ cbShortageReconciliationList.get(row1).getCurrency() + "',\""
										+ cbShortageReconciliationList.get(row1).getUniqueKey() + "\")");

								sb1.append(",");
							} else {
								sb1.append("('" + cbShortageReconciliationList.get(row1).getRequestId() + "','"
										+ cbShortageReconciliationList.get(row1).getPaymentNumber() + "','"
										+ cbShortageReconciliationList.get(row1).getVendorId() + "',\""
										+ cbShortageReconciliationList.get(row1).getInvoiceNumber()
										+ "\", STR_TO_DATE('" + cbShortageReconciliationList.get(row1).getInvoiceDate()
										+ "','%d/%m/%Y') ,\"" + cbShortageReconciliationList.get(row1).getDescription()
										+ "\",'" + cbShortageReconciliationList.get(row1).getInvoiceAmount() + "','"
										+ cbShortageReconciliationList.get(row1).getTermsDiscountTaken() + "','"
										+ cbShortageReconciliationList.get(row1).getAmountPaid() + "','"
										+ cbShortageReconciliationList.get(row1).getRemainingAmountAsOf() + "','"
										+ java.time.LocalDate.now() + "', STR_TO_DATE('"
										+ cbShortageReconciliationList.get(row1).getPaymentDate() + "','%d/%m/%Y') ,'"
										+ cbShortageReconciliationList.get(row1).getCurrency() + "',\""
										+ cbShortageReconciliationList.get(row1).getUniqueKey() + "\")");

								sb1.append(",");
							}

						}
						String query = sb1.substring(0, sb1.length() - 1) + ";";

						try {
							logger.info("Query " + query);
							jdbcTemplate.execute(query);

						} catch (Exception e1) {
							e1.printStackTrace();
						}
						cbShortageReconciliationList.clear();
					}

				}
				StringBuilder sb1 = new StringBuilder(
						"Insert Ignore into CBShortageReconciliation_cron (requestId,paymentNumber, vendorId, invoiceNumber, invoiceDate, Description, invoiceAmount, termsDiscountTaken, amountPaid, remainingAmountAsOf, createdDate,paymentDate,currency,uniqueKey) Values");
				for (int row1 = 0; row1 < cbShortageReconciliationList.size(); row1++) {
					if (vName.startsWith("CA") || vName.startsWith("US")) {
						sb1.append("('" + cbShortageReconciliationList.get(row1).getRequestId() + "','"
								+ cbShortageReconciliationList.get(row1).getPaymentNumber() + "','"
								+ cbShortageReconciliationList.get(row1).getVendorId() + "',\""
								+ cbShortageReconciliationList.get(row1).getInvoiceNumber() + "\", STR_TO_DATE('"
								+ cbShortageReconciliationList.get(row1).getInvoiceDate() + "','%m/%d/%Y') ,\""
								+ cbShortageReconciliationList.get(row1).getDescription() + "\",'"
								+ cbShortageReconciliationList.get(row1).getInvoiceAmount() + "','"
								+ cbShortageReconciliationList.get(row1).getTermsDiscountTaken() + "','"
								+ cbShortageReconciliationList.get(row1).getAmountPaid() + "','"
								+ cbShortageReconciliationList.get(row1).getRemainingAmountAsOf() + "','"
								+ java.time.LocalDate.now() + "', STR_TO_DATE('"
								+ cbShortageReconciliationList.get(row1).getPaymentDate() + "','%m/%d/%Y') ,'"
								+ cbShortageReconciliationList.get(row1).getCurrency() + "',\""
								+ cbShortageReconciliationList.get(row1).getUniqueKey() + "\")");

						sb1.append(",");
					} else {
						sb1.append("('" + cbShortageReconciliationList.get(row1).getRequestId() + "','"
								+ cbShortageReconciliationList.get(row1).getPaymentNumber() + "','"
								+ cbShortageReconciliationList.get(row1).getVendorId() + "',\""
								+ cbShortageReconciliationList.get(row1).getInvoiceNumber() + "\", STR_TO_DATE('"
								+ cbShortageReconciliationList.get(row1).getInvoiceDate() + "','%d/%m/%Y') ,\""
								+ cbShortageReconciliationList.get(row1).getDescription() + "\",'"
								+ cbShortageReconciliationList.get(row1).getInvoiceAmount() + "','"
								+ cbShortageReconciliationList.get(row1).getTermsDiscountTaken() + "','"
								+ cbShortageReconciliationList.get(row1).getAmountPaid() + "','"
								+ cbShortageReconciliationList.get(row1).getRemainingAmountAsOf() + "','"
								+ java.time.LocalDate.now() + "', STR_TO_DATE('"
								+ cbShortageReconciliationList.get(row1).getPaymentDate() + "','%d/%m/%Y') ,'"
								+ cbShortageReconciliationList.get(row1).getCurrency() + "',\""
								+ cbShortageReconciliationList.get(row1).getUniqueKey() + "\")");

						sb1.append(",");
					}
				}
				String query = sb1.substring(0, sb1.length() - 1) + ";";

				try {
					logger.info("Query " + query);
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
			e.printStackTrace();
			return false;
		}
	}

	private static void processBatch(List<String> batch, Locator searchInput) {
		searchInput.clear();
		for (String item : batch) {
			searchInput.focus();

			// Move the cursor to the end of the input field
			searchInput.press("End");

			// Type the item into the search input
			searchInput.evaluate("input => input.value += '" + item.replace("'", "\\'") + "'");

			// Optionally trigger an input event to update any listeners
			searchInput.evaluate("input => input.dispatchEvent(new Event('input'))");

			// Press Enter key
			searchInput.press("Enter");
		}
	}

	public void clickCoopPage(Page page) {
		try {
			List<ElementHandle> subMenuList = page.querySelectorAll(".flyout-menu-item-label");

			for (ElementHandle subMenu : subMenuList) {
				// Retrieve HTML content
				String htmlContent = subMenu.innerHTML();
				logger.info("htmlContent: " + htmlContent);
				if ("CoOp".equals(htmlContent) || "Co-op".equals(htmlContent)) {
					// Click the element
					subMenu.click();
					logger.info("Clicked element with Playwright");
					break; // Exit the loop after clicking
				}
			}

		} catch (Exception e) {
			page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'CoOp')]");
		}
	}

}
