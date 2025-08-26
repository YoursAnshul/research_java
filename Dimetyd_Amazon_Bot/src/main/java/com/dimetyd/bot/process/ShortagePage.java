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
public class ShortagePage {

	@Autowired
	CommonUtil CommonUtil;

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	 SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");

	public boolean processPage(Page page, Long id, String vendorId, String vendorName, String requestId, String startDate,
			String endDate, int createdBy, Path downloadPath) {
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
				CommonUtil.reminemepopup(page);
			} catch (Exception e) {
				CommonUtil.closePopup(page);
				randomClick.click();
				CommonUtil.reminemepopup(page);
			}
			fromDate.fill(startDateAsString);
			Thread.sleep(1000);
			toDate.fill(endDateAsString);

			try {
				randomClick = page.locator("#selected-remittance-count");
				randomClick.click();
				CommonUtil.reminemepopup(page);
			} catch (Exception e) {
				CommonUtil.closePopup(page);
				CommonUtil.reminemepopup(page);
				randomClick.click();
			}

			try {
				serachButton.click();
			} catch (Exception e) {
				CommonUtil.closePopup(page);
				CommonUtil.reminemepopup(page);
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
							if(paymentNumberRowFlag)
							{
								break;
							}
						}
					}
					if(paymentNumberRowFlag)
					{
						break;
					}
				}

				HashMap<String, String> paymentDateMap = new HashMap<>();
				int remittanceDataEndRow = 0;

				boolean firstSheetDone=false;
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
								logger.info("paymentNumber or PaymentDate issue.....");
								
									remittanceDataEndRow = r;
									firstSheetDone=true;
									break;
						
							}
						}

						break;

					}
					if(firstSheetDone)
					{
						break;
					}
				}

				//logger.info("paymentNumberEndRow : " + remittanceDataEndRow);
				//logger.info("HashMap : " + paymentDateMap);
				//logger.info("Last row : " + sheet.getLastRowNum());

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
					//logger.info("CellValue : " + c.getStringCellValue());
					if (c == null || c.getCellType() == CellType.BLANK) {
						// Can't be this cell - it's empty
						continue;
					}
					if (c.getCellType() == CellType.STRING) {
						String text = c.getStringCellValue();
						firstRow.put(text, cn);

					}
				}

				//logger.info("First row map : " + firstRow);

				List<CBShortageReconciliation> cbShortageReconciliationList = new ArrayList<CBShortageReconciliation>();
				int count = 0;
				int limit = 100;
				for (int i = rowNumberForPaymentNumber + 2; i <= sheet.getLastRowNum(); i++) {
					CBShortageReconciliation objCBShortageReconciliation = new CBShortageReconciliation();
					XSSFRow currentRow = sheet.getRow(i);
					if (count < limit) {
						count++;

						objCBShortageReconciliation.setRequestId(Integer.parseInt(requestId));
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
									currentRow.getCell(firstRow.get("Description")).getStringCellValue().replace("\"", "").replace("'", ""));
						} catch (NullPointerException e) {
							objCBShortageReconciliation.setDescription("");

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
										.replace("€", "").replace("£", "").replace("MXN", "").replace("AED", "").replace("INR", "")
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

						objCBShortageReconciliation.setRequestId(Integer.parseInt(requestId));
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
									currentRow.getCell(firstRow.get("Description")).getStringCellValue().replace("\"", "").replace("'", ""));
						} catch (NullPointerException e) {
							objCBShortageReconciliation.setDescription("");

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
								"Insert Ignore into CBShortageReconciliation (requestId,paymentNumber, vendorId, invoiceNumber, invoiceDate, Description, invoiceAmount, termsDiscountTaken, amountPaid, remainingAmountAsOf, createdDate,paymentDate,currency,uniqueKey) Values");
						for (int row1 = 0; row1 < cbShortageReconciliationList.size(); row1++) {
							if (vName.startsWith("CA") || vName.startsWith("US")) {
								sb1.append("('" + cbShortageReconciliationList.get(row1).getRequestId() + "','"
										+ cbShortageReconciliationList.get(row1).getPaymentNumber() + "','"
										+ cbShortageReconciliationList.get(row1).getVendorId() + "','"
										+ cbShortageReconciliationList.get(row1).getInvoiceNumber()
										+ "', STR_TO_DATE('" + cbShortageReconciliationList.get(row1).getInvoiceDate()
										+ "','%m/%d/%Y') ,'" + cbShortageReconciliationList.get(row1).getDescription()
										+ "','" + cbShortageReconciliationList.get(row1).getInvoiceAmount() + "','"
										+ cbShortageReconciliationList.get(row1).getTermsDiscountTaken() + "','"
										+ cbShortageReconciliationList.get(row1).getAmountPaid() + "','"
										+ cbShortageReconciliationList.get(row1).getRemainingAmountAsOf() + "','"
										+ java.time.LocalDate.now() + "', STR_TO_DATE('"
										+ cbShortageReconciliationList.get(row1).getPaymentDate() + "','%m/%d/%Y') ,'"
										+ cbShortageReconciliationList.get(row1).getCurrency() + "','"
										+ cbShortageReconciliationList.get(row1).getUniqueKey() + "')");

								sb1.append(",");
							} else {
								sb1.append("('" + cbShortageReconciliationList.get(row1).getRequestId() + "','"
										+ cbShortageReconciliationList.get(row1).getPaymentNumber() + "','"
										+ cbShortageReconciliationList.get(row1).getVendorId() + "','"
										+ cbShortageReconciliationList.get(row1).getInvoiceNumber()
										+ "', STR_TO_DATE('" + cbShortageReconciliationList.get(row1).getInvoiceDate()
										+ "','%d/%m/%Y') ,'" + cbShortageReconciliationList.get(row1).getDescription()
										+ "','" + cbShortageReconciliationList.get(row1).getInvoiceAmount() + "','"
										+ cbShortageReconciliationList.get(row1).getTermsDiscountTaken() + "','"
										+ cbShortageReconciliationList.get(row1).getAmountPaid() + "','"
										+ cbShortageReconciliationList.get(row1).getRemainingAmountAsOf() + "','"
										+ java.time.LocalDate.now() + "', STR_TO_DATE('"
										+ cbShortageReconciliationList.get(row1).getPaymentDate() + "','%d/%m/%Y') ,'"
										+ cbShortageReconciliationList.get(row1).getCurrency() + "','"
										+ cbShortageReconciliationList.get(row1).getUniqueKey() + "')");

								sb1.append(",");
							}

						}
						String query = sb1.substring(0, sb1.length() - 1) + ";";

						try {
							logger.info("Query " + query);
							jdbcTemplate.execute(query);

						} catch (Exception e1) {
							e1.printStackTrace();
							System.out.println(query);
						}
						cbShortageReconciliationList.clear();
					}

				}
				StringBuilder sb1 = new StringBuilder(
						"Insert Ignore into CBShortageReconciliation (requestId,paymentNumber, vendorId, invoiceNumber, invoiceDate, Description, invoiceAmount, termsDiscountTaken, amountPaid, remainingAmountAsOf, createdDate,paymentDate,currency,uniqueKey) Values");
				for (int row1 = 0; row1 < cbShortageReconciliationList.size(); row1++) {
					if (vName.startsWith("CA") || vName.startsWith("US")) {
						sb1.append("('" + cbShortageReconciliationList.get(row1).getRequestId() + "','"
								+ cbShortageReconciliationList.get(row1).getPaymentNumber() + "','"
								+ cbShortageReconciliationList.get(row1).getVendorId() + "','"
								+ cbShortageReconciliationList.get(row1).getInvoiceNumber() + "', STR_TO_DATE('"
								+ cbShortageReconciliationList.get(row1).getInvoiceDate() + "','%m/%d/%Y') ,'"
								+ cbShortageReconciliationList.get(row1).getDescription() + "','"
								+ cbShortageReconciliationList.get(row1).getInvoiceAmount() + "','"
								+ cbShortageReconciliationList.get(row1).getTermsDiscountTaken() + "','"
								+ cbShortageReconciliationList.get(row1).getAmountPaid() + "','"
								+ cbShortageReconciliationList.get(row1).getRemainingAmountAsOf() + "','"
								+ java.time.LocalDate.now() + "', STR_TO_DATE('"
								+ cbShortageReconciliationList.get(row1).getPaymentDate() + "','%m/%d/%Y') ,'"
								+ cbShortageReconciliationList.get(row1).getCurrency() + "','"
								+ cbShortageReconciliationList.get(row1).getUniqueKey() + "')");

						sb1.append(",");
					} else {
						sb1.append("('" + cbShortageReconciliationList.get(row1).getRequestId() + "','"
								+ cbShortageReconciliationList.get(row1).getPaymentNumber() + "','"
								+ cbShortageReconciliationList.get(row1).getVendorId() + "','"
								+ cbShortageReconciliationList.get(row1).getInvoiceNumber() + "', STR_TO_DATE('"
								+ cbShortageReconciliationList.get(row1).getInvoiceDate() + "','%d/%m/%Y') ,'"
								+ cbShortageReconciliationList.get(row1).getDescription() + "','"
								+ cbShortageReconciliationList.get(row1).getInvoiceAmount() + "','"
								+ cbShortageReconciliationList.get(row1).getTermsDiscountTaken() + "','"
								+ cbShortageReconciliationList.get(row1).getAmountPaid() + "','"
								+ cbShortageReconciliationList.get(row1).getRemainingAmountAsOf() + "','"
								+ java.time.LocalDate.now() + "', STR_TO_DATE('"
								+ cbShortageReconciliationList.get(row1).getPaymentDate() + "','%d/%m/%Y') ,'"
								+ cbShortageReconciliationList.get(row1).getCurrency() + "','"
								+ cbShortageReconciliationList.get(row1).getUniqueKey() + "')");

						sb1.append(",");
					}
				}
				String query = sb1.substring(0, sb1.length() - 1) + ";";

				try {
					//logger.info("Query " + query);
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

			page.click("//div[@aria-label='Navigation menu']");
			try {
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
			} catch (Exception e) {
				page.reload();
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
			}

			clickCoopPage(page);
			try {

				String anErrorOcurred = page.locator(
						"//div[@class='a-container page']//div[@class='a-section']//div[@class='a-box a-alert a-alert-error']//div[@class='a-box-inner a-alert-container']//div[@class='a-alert-content']")
						.innerText().trim();
				logger.info("anErrorOcurred : " + anErrorOcurred);
				if (anErrorOcurred.equals("A problem occurred, please try again.")) {
					page.click("//div[@aria-label='Navigation menu']");
					try {
						page.getByText("Payments").click(new Locator.ClickOptions().setTimeout(6000));
					} catch (Exception e) {
						page.reload();
						page.locator("//div[@class='side-nav-tab']/span[normalize-space(text())='Payments']")
						.click(new Locator.ClickOptions().setTimeout(6000));
						
						
					}

					clickCoopPage(page);
				}
			} catch (Exception e) {
				logger.info("No Error Ocurred");
			}
			SimpleDateFormat inputDateFormatForUs = new SimpleDateFormat("MM/dd/yyyy");
			SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd");
			SimpleDateFormat inputDateFormatForOthers = new SimpleDateFormat("dd/MM/yyyy");
			String outputStrStartDate = null;
			String outputStrEndDate = null;
			
			if (vName.startsWith("CA") || vName.startsWith("US")) {
				try {

					Date inputStartDate = inputDateFormatForUs.parse(startDateAsString);
					outputStrStartDate = outputDateFormat.format(inputStartDate);
					Date inputEndtDate = inputDateFormatForUs.parse(endDateAsString);
					outputStrEndDate = outputDateFormat.format(inputEndtDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				Date inputStartDate = inputDateFormatForOthers.parse(startDateAsString);
				outputStrStartDate = outputDateFormat.format(inputStartDate);
				Date inputEndtDate = inputDateFormatForOthers.parse(endDateAsString);
				outputStrEndDate = outputDateFormat.format(inputEndtDate);
			}
			logger.info("Query : SELECT `invoiceNumber` FROM CBShortageReconciliation WHERE vendorId = '" + vendorId
					+ "' AND `invoiceType` = 'CoOp deduction' AND `paymentDate` BETWEEN '" + outputStrStartDate
					+ "' AND '" + outputStrEndDate + "' and `invoiceTypeDetailed` IS NULL" );
			StringBuilder sql = new StringBuilder(
					"SELECT `invoiceNumber` FROM CBShortageReconciliation WHERE vendorId = '" + vendorId
							+ "' AND `invoiceType` = 'CoOp deduction' AND `paymentDate` BETWEEN '" + outputStrStartDate
							+ "' AND '" + outputStrEndDate + "' and `invoiceTypeDetailed` IS NULL ");
			List<ShortageJobData> jobDataList = this.jdbcTemplate.query(sql.toString(),
					new RowMapper<ShortageJobData>() {
						@Override
						public ShortageJobData mapRow(ResultSet rs, int rowNum) throws SQLException {
							ShortageJobData res = new ShortageJobData();
							res.setInvoiceNumber(rs.getString("invoiceNumber"));
							return res;
						}
					}, new Object[] {});
			int batchSize = 90;
			int currentIndex = 0;
			List<String> invoiceNumberList = new ArrayList<String>();
			for (ShortageJobData invoiceNumber : jobDataList) {
				invoiceNumberList.add(invoiceNumber.getInvoiceNumber());
			}
			//logger.info("invoiceNumberListSize : " + invoiceNumberList.size());
			while (currentIndex < invoiceNumberList.size()) {
				List<String> batch = invoiceNumberList.subList(currentIndex,
						Math.min(currentIndex + batchSize, invoiceNumberList.size()));
				Locator searchInput = null;
				while (true) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						searchInput = page.locator("#search-input");
						break;
					} catch (Exception e) {

					}
				}
				// Process the current batch
				processBatch(batch, searchInput);
				Locator searchButton = page.locator("#search-button-announce");
				searchButton.click();
				try {// div[@id='mons-error-page-template']//h4
					Locator unsupportedActionParent = page.locator("#mons-error-page-template");
					String unsupportedAction = "";
					try {
						unsupportedAction = unsupportedActionParent.locator("h4").innerText();
					} catch (Exception e) {

					}

					logger.info("unsupportedActionWord : " + unsupportedAction);
					Thread.sleep(2000);
					if (unsupportedAction.equals("Unsupported action")) {
						page.click("//div[@aria-label='Navigation menu']");
						try {
							page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
						} catch (Exception e) {
							page.reload();
							page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
						}

						clickCoopPage(page);
						while (true) {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							try {
								searchInput = page.locator("#search-input");
								break;
							} catch (Exception e) {

							}
						}
						processBatch(batch, searchInput);
						searchButton = page.locator("#search-button-announce");
						searchButton.click();

					}

				} catch (Exception e) {
					try {
						String anErrorOcurred = page.locator(
								"//div[@class='a-container page']//div[@class='a-section']//div[@class='a-box a-alert a-alert-error']//div[@class='a-box-inner a-alert-container']//div[@class='a-alert-content']")
								.innerText().trim();
						logger.info("anErrorOcurred : " + anErrorOcurred);
						if (anErrorOcurred.equals("A problem occurred, please try again.")) {
							try {
								page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
							} catch (Exception e1) {
								page.reload();
								page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
							}

							clickCoopPage(page);
						}
					} catch (Exception e1) {
						logger.info("No Error Ocurred");
					}
				}

				boolean isFirstPage = false;
				while (true) {
					ArrayList<String> data = null;
					try {
						Thread.sleep(3000);
						Locator table = page.locator("//table[@aria-labelledby='katal-id-0']//tbody");
						List<Locator> getRows = null;
						for (int i = 0; i < 3; i++) {
							Thread.sleep(1000);
							if (isFirstPage == false) {
								getRows = table.locator("*[style='display: table-row;']").all();
							} else {
								getRows = table.locator("*[style='display: table-row; opacity: 1;']").all();
							}

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
//							logger.info("row " + data);

							logger.info("UPDATE CBShortageReconciliation SET invoiceTypeDetailed = '" + data.get(4)
									+ "' WHERE vendorId = '" + vendorId + "' AND  invoiceNumber = '" + data.get(1)
									+ "'");
							jdbcTemplate.execute("UPDATE CBShortageReconciliation SET invoiceTypeDetailed = '"
									+ data.get(4) + "' WHERE vendorId = '" + vendorId + "' AND  invoiceNumber = '"
									+ data.get(1) + "'");
						}
						try {
							Thread.sleep(1500);
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
						} catch (Exception e) {
							e.printStackTrace();
							break;
						}

					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}

				currentIndex += batchSize;
				logger.info("CurrentIndex : " + currentIndex);
			}

			//logger.info("createdBy : " + createdBy);
			if (createdBy == 0) {
				return true;
			} else {
				try {

					page.click("//div[@aria-label='Navigation menu']");
					try {
						page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
					} catch (Exception e) {
						page.reload();
						page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
					}

					page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Invoices')]");

					Locator viewAllInvoices = page.locator("text=View all Invoices");
					if (viewAllInvoices.count() > 0) {
						viewAllInvoices.click();
					} else {
						try
						{
							Locator newUi = page.locator("//kat-badge[@class='link-hover']");
						newUi.click();
						}
						catch(Exception e)
						{
							
						}
						viewAllInvoices = page.locator("text=View all Invoices");
						if(viewAllInvoices.count() > 0)
						{
							viewAllInvoices.click();
						}
						else {
							return true;
							
						}
					
					}

					Locator startDateForInvoicesParent = page.locator("#start-date-calendar");
					Locator startDateForInvoices = startDateForInvoicesParent.locator("#start-date");
					Locator endDateForInvoicesParent = page.locator("#end-date-calendar");
					Locator endDateForInvoices = endDateForInvoicesParent.locator("#end-date");
					Locator serachButtonForInvoices = page.locator("#advancedSearchHarmonicForm-submit");
					
					page.locator("//span[@id='date-range-option']").click();
					page.locator("//ul[@class='a-nostyle a-list-link']/li/a[text()='Custom Date']").click();
					page.waitForTimeout(1000);
					//startDateForInvoices.click();
					startDateForInvoices.fill(startDateAsString);
					try {
						page.click("//h1[@class='a-size-extra-large a-spacing-top-micro a-text-bold']");// random click
					} catch (Exception e1) {
						CommonUtil.closePopup(page);
						page.click("//h1[@class='a-size-extra-large a-spacing-top-micro a-text-bold']");// random click
					}

					endDateForInvoices.fill(endDateAsString);
					try {
						page.click("//h1[@class='a-size-extra-large a-spacing-top-micro a-text-bold']");// random click
					} catch (Exception e1) {
						CommonUtil.closePopup(page);
						page.click("//h1[@class='a-size-extra-large a-spacing-top-micro a-text-bold']");// random click
					}

					try {
						serachButtonForInvoices.click();
					} catch (Exception e) {
						CommonUtil.closePopup(page);

						serachButtonForInvoices.click();
					}

					File dir = downloadPath.toFile(); // Convert Path to File

					if (dir.isDirectory()) {
						for (File f : dir.listFiles()) {
							if (f.isFile() && f.getName().endsWith(".csv")) {
								logger.info("File Name " + f.getName());
								if (f.delete()) {
									logger.info("Deleted file " + f.getName());
								} else {
									logger.info("Failed to delete file " + f.getName());
								}
							}
						}
					} else {
						logger.info("Provided path is not a directory.");
					}

					Locator advancedSearch = page.locator("#advancedSearchExportAll");

					try {
						boolean flag = true;
						while (flag) {
							try {
								download = page.waitForDownload(() -> {
									advancedSearch.click();
								});
							} catch (Exception e) {
								CommonUtil.closePopup(page);
								download = page.waitForDownload(() -> {
									advancedSearch.click();
								});
							}
//							page.waitForSelector(
//									"//div[@id='advancedSearchExportAllDiv']//span[@class='a-spinner a-spinner-small advancedSearchExportAllWaitingSpinner none']",
//									new Page.WaitForSelectorOptions().setTimeout(48000));
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
							if (fileExists) {
								flag = false;
							}
						}

					} catch (Exception e) {
						try {
							CommonUtil.closePopup(page);

							boolean flag = true;
							while (flag) {
								try {
									download = page.waitForDownload(() -> {
										advancedSearch.click();
									});
								} catch (Exception e1) {
									CommonUtil.closePopup(page);
									download = page.waitForDownload(() -> {
										advancedSearch.click();
									});
								}
//								page.waitForSelector(
//										"//div[@id='advancedSearchExportAllDiv']//span[@class='a-spinner a-spinner-small advancedSearchExportAllWaitingSpinner none']",
//										new Page.WaitForSelectorOptions().setTimeout(48000));
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
								if (fileExists) {
									flag = false;
								}
							}
						} catch (Exception e1) {

						}

					}

					FileReader reader = null;
					try {
						reader = new FileReader(tempFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
					CSVParser csvParser = null;
					try {
						csvParser = new CSVParser(reader,
								CSVFormat.DEFAULT.withHeader("Marketplace", "Invoice Date", "Payment Due date",
										"Status", "Actual Paid Amount", "Payee", "Creation Date", "Invoice number",
										"Price", "Any Deductions").withIgnoreHeaderCase().withTrim());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					SimpleDateFormat sdf1ForUS = new SimpleDateFormat("MM/dd/yyyy");
					SimpleDateFormat sdf1ForOthers = new SimpleDateFormat("dd/MM/yyyy");
					java.util.Date date = null;

					List<ShortageInvoice> invoiceList = new ArrayList<ShortageInvoice>();
					int count = 0;
					int limit = 100;
					for (CSVRecord csvRecord : csvParser) {
						ShortageInvoice invoice1 = new ShortageInvoice();

						if (csvRecord.getRecordNumber() == 1L) {
							logger.info("GetRecoredNumber " + csvRecord.getRecordNumber());
							continue;
						}

						if (count < limit) {
							count++;
							try {
								invoice1.setMarketplace(csvRecord.get("Marketplace"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								String invoiceDate = csvRecord.get("Invoice Date");
								try {
									if (vName.startsWith("CA") || vName.startsWith("US")) {
										date = sdf1ForUS.parse(invoiceDate);
									} else {
										date = sdf1ForOthers.parse(invoiceDate);
									}

								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								invoice1.setInvoiceDate(new java.sql.Date(date.getTime()));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								String paymentDueDate = csvRecord.get("Payment Due date");
								try {
									if (vName.startsWith("CA") || vName.startsWith("US")) {
										date = sdf1ForUS.parse(paymentDueDate);
									} else {
										date = sdf1ForOthers.parse(paymentDueDate);
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								invoice1.setPaymentDueDate(new java.sql.Date(date.getTime()));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								invoice1.setStatus(csvRecord.get("Status"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								String invoiceAmount = csvRecord.get("Price");
								if (invoiceAmount.contains("€")) {
									invoiceAmount = CommonUtil.replaceCurrency(invoiceAmount).trim();
									int amtCharatcerLength = invoiceAmount.length();
									if (amtCharatcerLength > 3) {
										char thirdLastChar = invoiceAmount.charAt(amtCharatcerLength - 3);
										if (thirdLastChar == ',') {
											invoiceAmount = invoiceAmount.substring(0, amtCharatcerLength - 3) + '.'
													+ invoiceAmount.substring(amtCharatcerLength - 2);
										}
										int lastDotIndex = invoiceAmount.lastIndexOf('.');

										if (lastDotIndex != -1) { // Check if a dot exists in the string
											invoiceAmount = invoiceAmount.replaceAll("\\.(?![^.]*$)", "");
											logger.info("Modified string: " + invoiceAmount);
										} else {
											logger.info("No dot found in the string.");
										}
									}
								}

								String orginialAmt = invoiceAmount.replaceAll("[^\\d.]", "")
										.replaceAll("[^\\n\\r\\t\\p{Print}]", "");
								logger.info("invoiceAmount : " + orginialAmt);
//				String amount = invoiceAmount.replace("$", "").replace("€", "").replace("£", "")
//						.replace("MXN", "").replace("AED", "").replaceAll("[^\\n\\r\\t\\p{Print}]", "")
//						.replace(",", "");
								Double actualAmout = Double.parseDouble(orginialAmt);
								invoice1.setInvoiceAmount(actualAmout);
//				logger.info("InvoiceAmount : "+actualAmout);
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								invoice1.setPayee(csvRecord.get("Payee"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}

							try {
								String creationDate = csvRecord.get("Creation Date");
								try {
									if (vName.startsWith("CA") || vName.startsWith("US")) {
										date = sdf1ForUS.parse(creationDate);
									} else {
										date = sdf1ForOthers.parse(creationDate);
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								invoice1.setInvoiceCreationDate(new java.sql.Date(date.getTime()));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								invoice1.setInvoiceNumber(csvRecord.get("Invoice number"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}

							try {
								invoice1.setAnyDeductions(csvRecord.get("Any Deductions"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							String uniqueKey = vendorId + invoice1.getInvoiceNumber();
							invoice1.setUniqueKey(uniqueKey);
							invoiceList.add(invoice1);

						} else {
							count = 0;
							try {
								invoice1.setMarketplace(csvRecord.get("Marketplace"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								String invoiceDate = csvRecord.get("Invoice Date");
								try {
									if (vName.startsWith("CA") || vName.startsWith("US")) {
										date = sdf1ForUS.parse(invoiceDate);
									} else {
										date = sdf1ForOthers.parse(invoiceDate);
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								invoice1.setInvoiceDate(new java.sql.Date(date.getTime()));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								String paymentDueDate = csvRecord.get("Payment Due date");
								try {
									if (vName.startsWith("CA") || vName.startsWith("US")) {
										date = sdf1ForUS.parse(paymentDueDate);
									} else {
										date = sdf1ForOthers.parse(paymentDueDate);
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								invoice1.setPaymentDueDate(new java.sql.Date(date.getTime()));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								invoice1.setStatus(csvRecord.get("Status"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								String invoiceAmount = csvRecord.get("Price");
								if (invoiceAmount.contains("€")) {
									invoiceAmount = CommonUtil.replaceCurrency(invoiceAmount).trim();
									int amtCharatcerLength = invoiceAmount.length();
									if (amtCharatcerLength > 3) {
										char thirdLastChar = invoiceAmount.charAt(amtCharatcerLength - 3);
										if (thirdLastChar == ',') {
											invoiceAmount = invoiceAmount.substring(0, amtCharatcerLength - 3) + '.'
													+ invoiceAmount.substring(amtCharatcerLength - 2);
										}
										int lastDotIndex = invoiceAmount.lastIndexOf('.');

										if (lastDotIndex != -1) { // Check if a dot exists in the string
											invoiceAmount = invoiceAmount.replaceAll("\\.(?![^.]*$)", "");
											logger.info("Modified string: " + invoiceAmount);
										} else {
											logger.info("No dot found in the string.");
										}
									}
								}

								String orginialAmt = invoiceAmount.replaceAll("[^\\d.]", "")
										.replaceAll("[^\\n\\r\\t\\p{Print}]", "");
								logger.info("invoiceAmount : " + orginialAmt);
//				String amount = invoiceAmount.replace("$", "").replace("€", "").replace("£", "")
//						.replace("MXN", "").replace("AED", "").replaceAll("[^\\n\\r\\t\\p{Print}]", "")
//						.replace(",", "");
								Double actualAmout = Double.parseDouble(orginialAmt);
								invoice1.setInvoiceAmount(actualAmout);
//				logger.info("InvoiceAmount : "+actualAmout);
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								invoice1.setPayee(csvRecord.get("Payee"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}

							try {
								String creationDate = csvRecord.get("Creation Date");
								try {
									if (vName.startsWith("CA") || vName.startsWith("US")) {
										date = sdf1ForUS.parse(creationDate);
									} else {
										date = sdf1ForOthers.parse(creationDate);
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								invoice1.setInvoiceCreationDate(new java.sql.Date(date.getTime()));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							try {
								invoice1.setInvoiceNumber(csvRecord.get("Invoice number"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}

							try {
								invoice1.setAnyDeductions(csvRecord.get("Any Deductions"));
							} catch (NullPointerException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							String uniqueKey = vendorId + invoice1.getInvoiceNumber();
							invoice1.setUniqueKey(uniqueKey);
							invoiceList.add(invoice1);
							StringBuilder sb1 = new StringBuilder(
									"INSERT IGNORE INTO `CBInvoiceSummery` (`vendorId`,`createdDate`,`payee`,`marketPlace`,`invoiceNumber`,`invoiceDate`,`invoiceCreationDate`,`invoiceAmount`,`paymentDueDate`,`invoiceStatus`,UKey) VALUES");
							for (int row1 = 0; row1 < invoiceList.size(); row1++) {

								sb1.append("('" + vendorId + "','" + java.time.LocalDate.now() + "','"
										+ invoiceList.get(row1).getPayee() + "','"
										+ invoiceList.get(row1).getMarketplace() + "','"
										+ invoiceList.get(row1).getInvoiceNumber() + "','"
										+ invoiceList.get(row1).getInvoiceDate() + "','"
										+ invoiceList.get(row1).getInvoiceCreationDate() + "','"
										+ invoiceList.get(row1).getInvoiceAmount() + "','"
										+ invoiceList.get(row1).getPaymentDueDate() + "','"
										+ invoiceList.get(row1).getStatus() + "','"
										+ invoiceList.get(row1).getUniqueKey() + "')");

								sb1.append(",");
							}
							String query = sb1.substring(0, sb1.length() - 1) + ";";

							try {
								logger.info("Query " + query);
								jdbcTemplate.execute(query);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
							invoiceList.clear();
						}

					}
					StringBuilder sb1 = new StringBuilder(
							"INSERT IGNORE INTO `CBInvoiceSummery` (`vendorId`,`createdDate`,`payee`,`marketPlace`,`invoiceNumber`,`invoiceDate`,`invoiceCreationDate`,`invoiceAmount`,`paymentDueDate`,`invoiceStatus`,UKey) VALUES");
					for (int row1 = 0; row1 < invoiceList.size(); row1++) {

						sb1.append("('" + vendorId + "','" + java.time.LocalDate.now() + "','"
								+ invoiceList.get(row1).getPayee() + "','" + invoiceList.get(row1).getMarketplace()
								+ "','" + invoiceList.get(row1).getInvoiceNumber() + "','"
								+ invoiceList.get(row1).getInvoiceDate() + "','"
								+ invoiceList.get(row1).getInvoiceCreationDate() + "','"
								+ invoiceList.get(row1).getInvoiceAmount() + "','"
								+ invoiceList.get(row1).getPaymentDueDate() + "','" + invoiceList.get(row1).getStatus()
								+ "','" + invoiceList.get(row1).getUniqueKey() + "')");

						sb1.append(",");
					}
					String query = sb1.substring(0, sb1.length() - 1) + ";";

					try {
						logger.info("Query " + query);
						jdbcTemplate.execute(query);
					} catch (Exception e1) {
						e1.printStackTrace();
					}

					reader.close();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}

			}
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
				logger.info("htmlContent: "+htmlContent);
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
