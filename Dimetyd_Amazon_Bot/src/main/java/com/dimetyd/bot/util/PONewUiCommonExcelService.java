package com.dimetyd.bot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.CBPOData;
import com.dimetyd.bot.model.CBPoHistoryData;

@Component
public class PONewUiCommonExcelService {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private Logger logger = LoggerFactory.getLogger(getClass());
	DataFormatter formatter = new DataFormatter();
	SimpleDateFormat inputFormat = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
	SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
	final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
	final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");

	public void readPurchaseOrderFile(Path filePath, String vendorName, String vendorId, Long requestId) {
		try {

			String vName = vendorName.substring(0, 2);
			File tempFile = filePath.toFile();

			boolean fileExists = CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));

			if (fileExists) {
				System.out.println("File exists!!");
			} else {
				System.out.println("File does not exist within the timeout period..");
				return;
			}
			try (InputStream fileStream = new FileInputStream(tempFile);

					//HSSFWorkbook workbook = new HSSFWorkbook(fileStream);
			// XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
			 Workbook workbook = WorkbookFactory.create(fileStream);

			// Workbook workbook =
			// StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(fileStream)
			) {

					Iterator<Row> rows = workbook.getSheetAt(0).rowIterator();
					HashMap<String, Integer> firstRow = new HashMap<String, Integer>();
					if (rows.hasNext()) {
						Row r = rows.next(); // Get the first row
						for (int cn = 0; cn < r.getLastCellNum(); cn++) {
							Cell c = r.getCell(cn);
							if (c != null && c.getCellType() == CellType.STRING) {
								String text = c.getStringCellValue().toLowerCase();
								firstRow.put(text, cn);
							}
						}
					}
					while (rows.hasNext()) {
						// String sheetName = workbook.getSheetName(i).trim();
						List<CBPoHistoryData> rowList = new ArrayList<CBPoHistoryData>();
						for (int rownum = 0; rownum < 100; rownum++) {

							Row currentRow = null;
							try {
								currentRow = rows.next();
							} catch (NoSuchElementException e) {
								break;
							}
							CBPoHistoryData cbPoHistoryData = new CBPoHistoryData();
							for (Map.Entry<String, Integer> entry : firstRow.entrySet()) {
								// logger.info("first row");
								if (entry.getKey().equalsIgnoreCase("cost")) {
									try {
										cbPoHistoryData.setUnitCost(
												currentRow.getCell(firstRow.get("cost")).getNumericCellValue());
									} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
										cbPoHistoryData.setUnitCost(0.0);
									}
									try {
										cbPoHistoryData.setCurrency(
												currentRow.getCell(firstRow.get("cost") + 1).getStringCellValue());
									} catch (NullPointerException e) {

									}
									try {
										cbPoHistoryData.setCaseQty(
												(int) currentRow.getCell(firstRow.get("case size")).getNumericCellValue());
										cbPoHistoryData.setCase(true);
									} catch (NullPointerException | IllegalStateException | NumberFormatException e) {
										cbPoHistoryData.setCaseQty(0);
										cbPoHistoryData.setCase(false);
									}

								} else if (entry.getKey().equalsIgnoreCase("case cost")) {
									try {
										cbPoHistoryData.setUnitCost(
												currentRow.getCell(firstRow.get("case cost")).getNumericCellValue());
									} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
										cbPoHistoryData.setUnitCost(0.0);
									}
									try {
										cbPoHistoryData.setCurrency(
												currentRow.getCell(firstRow.get("case cost") + 1).getStringCellValue());
									} catch (NullPointerException e) {

									}

//										try {
//											cbPoHistoryData.setCaseQty((int) currentRow.getCell(firstRow.get("case size"))
//													.getNumericCellValue());
//											cbPoHistoryData.setCase(true);
//										} catch (NullPointerException | IllegalStateException | NumberFormatException e) {
//											cbPoHistoryData.setCaseQty(0);
//											cbPoHistoryData.setCase(false);
//										}
								} // below else block is for other countries
								else if (entry.getKey().equalsIgnoreCase("cost price")) {
									try {
										cbPoHistoryData.setUnitCost(
												currentRow.getCell(firstRow.get("cost price")).getNumericCellValue());
									} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
										cbPoHistoryData.setUnitCost(0.0);
									}
									try {
										cbPoHistoryData.setCurrency(
												currentRow.getCell(firstRow.get("cost price") + 1).getStringCellValue());
									} catch (NullPointerException e) {

									}
									try {
										cbPoHistoryData.setCaseQty(
												(int) currentRow.getCell(firstRow.get("case size")).getNumericCellValue());
										cbPoHistoryData.setCase(true);
									} catch (NullPointerException | IllegalStateException | NumberFormatException e) {
										cbPoHistoryData.setCaseQty(0);
										cbPoHistoryData.setCase(false);
									}
								}
							}

							try {
								String poHeader = null;

								if (firstRow.containsKey("po")) {
									poHeader = "po";
								} else if (firstRow.containsKey("order/po number")) {
									poHeader = "order/po number";
								}

								if (poHeader != null) {
									cbPoHistoryData.setPO(currentRow.getCell(firstRow.get(poHeader)).getStringCellValue());
								} else {
									System.out.println("Neither 'po' nor 'Order/PO number' header found.");
								}

							} catch (NullPointerException e) {
								System.out.println("Null value encountered while setting PO.");
							}
							try {
								cbPoHistoryData
										.setVendor(currentRow.getCell(firstRow.get("vendor code")).getStringCellValue());
							} catch (NullPointerException e) {

							}

							try {
								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX") || vName.startsWith("ES") || vName.startsWith("GB")
										|| vName.startsWith("FR") || vName.startsWith("IT")) {
									cbPoHistoryData.setShiptolocation(
											currentRow.getCell(firstRow.get("ship-to location")).getStringCellValue());
								} else {
									cbPoHistoryData.setShiptolocation(
											currentRow.getCell(firstRow.get("fulfilment centre")).getStringCellValue());
								}
							} catch (NullPointerException e) {

							}
							try {
								cbPoHistoryData.setASIN(currentRow.getCell(firstRow.get("asin")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							try {
								cbPoHistoryData.setExternalID(
										currentRow.getCell(firstRow.get("external id")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							try {
								cbPoHistoryData.setExternalIdType(
										currentRow.getCell(firstRow.get("external id type")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							try {
								cbPoHistoryData.setModelNumber(currentRow.getCell(firstRow.get("model number"))
										.getStringCellValue().replace("'", ""));
							} catch (NullPointerException e) {

							}
							try {
								cbPoHistoryData.setTitle(currentRow.getCell(firstRow.get("product name"))
										.getStringCellValue().replaceAll("'", ""));
							} catch (NullPointerException e) {
								try {
									cbPoHistoryData.setTitle(currentRow.getCell(firstRow.get("title")).getStringCellValue()
											.replaceAll("'", ""));
								} catch (Exception ex) {

								}

							}

							String newTitle = "\"" + cbPoHistoryData.getTitle() + "\"";
							try {
								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX") || vName.startsWith("ES") || vName.startsWith("GB")
										|| vName.startsWith("FR") || vName.startsWith("IT")) {
									cbPoHistoryData.setAvailability(
											currentRow.getCell(firstRow.get("availability")).getStringCellValue());
								} else {
									cbPoHistoryData.setAvailability(
											currentRow.getCell(firstRow.get("availability status")).getStringCellValue());
								}
							} catch (NullPointerException e) {

							}
							try {
								cbPoHistoryData.setWindowType(
										currentRow.getCell(firstRow.get("freight terms")).getStringCellValue());
							} catch (NullPointerException e) {

							}

							/*
							 * try { int columnIndex = firstRow.get("window start"); // map of column names
							 * to indexes Cell cell = currentRow.getCell(columnIndex);
							 * 
							 * if (cell != null) { String windowStartDate = formatter.formatCellValue(cell);
							 * cbPoHistoryData.setWindowStart(windowStartDate); } else { // Handle blank
							 * cell cbPoHistoryData.setWindowStart(null); }
							 * 
							 * } catch (NullPointerException | DateTimeParseException e) { // Log or handle
							 * the error cbPoHistoryData.setWindowStart(null); } try { int columnIndex =
							 * firstRow.get("window end"); // map of column names to indexes Cell cell =
							 * currentRow.getCell(columnIndex);
							 * 
							 * if (cell != null) { String windowEndDate = formatter.formatCellValue(cell);
							 * cbPoHistoryData.setWindowEnd(windowEndDate); } else { // Handle blank cell
							 * cbPoHistoryData.setWindowEnd(null); }
							 * 
							 * } catch (NullPointerException | DateTimeParseException e) { // Log or handle
							 * the error cbPoHistoryData.setWindowEnd(null); } try { int columnIndex =
							 * firstRow.get("expected date"); // map of column names to indexes Cell cell =
							 * currentRow.getCell(columnIndex);
							 * 
							 * if (cell != null) { String expectedDate = formatter.formatCellValue(cell);
							 * cbPoHistoryData.setExpectedDate(expectedDate); } else { // Handle blank cell
							 * cbPoHistoryData.setExpectedDate(null); }
							 * 
							 * } catch (NullPointerException | DateTimeParseException e) { // Log or handle
							 * the error cbPoHistoryData.setExpectedDate(null); }
							 * 
							 * try { int columnIndex = firstRow.get("order date"); // map of column names to
							 * indexes Cell cell = currentRow.getCell(columnIndex);
							 * 
							 * if (cell != null) { String orderDate = formatter.formatCellValue(cell);
							 * cbPoHistoryData.setorderDate(orderDate); } else { // Handle blank cell
							 * cbPoHistoryData.setorderDate(null); }
							 * 
							 * } catch (NullPointerException | DateTimeParseException e) { // Log or handle
							 * the error cbPoHistoryData.setorderDate(null); }
							 */

							try {
								Date windowStartDate = null;

								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX") || vName.startsWith("ES") || vName.startsWith("GB")
										|| vName.startsWith("FR") || vName.startsWith("IT")) {
									windowStartDate = currentRow.getCell(firstRow.get("window start")).getDateCellValue();
								}
								/*
								 * else if(vName.startsWith("DE")) { windowStartDate =
								 * currentRow.getCell(firstRow.get("hand-off start")) .getDateCellValue();
								 * 
								 * }
								 */
								else {
									windowStartDate = currentRow.getCell(firstRow.get("hand-off start")).getDateCellValue();

								}
								// System.out.println("startdate" + windowStartDate);
								try {
									Date date = inputFormat.parse(windowStartDate.toString());
									String formattedDate = outputFormat.format(date);
									// System.out.println("Formatted Date: " + formattedDate);
									cbPoHistoryData.setWindowStart(formattedDate);

									// Use this formattedDate in your Playwright automation
								} catch (ParseException e) {
									e.printStackTrace();
								}
								// cbPoHistoryData.setWindowStart(windowStartDate);
							} catch (NullPointerException | DateTimeParseException e) {
								e.printStackTrace();
								cbPoHistoryData.setWindowStart("0000-00-00");
							}
							try {
								Date windowEndDate = null;

								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX") || vName.startsWith("ES") || vName.startsWith("GB")
										|| vName.startsWith("FR") || vName.startsWith("IT")) {
									windowEndDate = currentRow.getCell(firstRow.get("window end")).getDateCellValue();
								} else {
									windowEndDate = currentRow.getCell(firstRow.get("hand-off end")).getDateCellValue();
								}

								// System.out.println("widowned" + windowEndDate);
								try {

									Date date = inputFormat.parse(windowEndDate.toString());
									String formattedDate = outputFormat.format(date);
									System.out.println("Formatted Date: " + formattedDate);
									cbPoHistoryData.setWindowEnd(formattedDate);

									// Use this formattedDate in your Playwright automation
								} catch (ParseException e) {
									e.printStackTrace();
								}
								// cbPoHistoryData.setWindowStart(windowStartDate);
							} catch (NullPointerException | DateTimeParseException e) {
								cbPoHistoryData.setWindowEnd("0000-00-00");
							}

							try {
								Date orderdate = null;

								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX")) {
									orderdate = currentRow.getCell(firstRow.get("order date")).getDateCellValue();
								} else {
									orderdate = currentRow.getCell(firstRow.get("order date")).getDateCellValue();
								}
								System.out.println("orderdate" + orderdate);
								try {
									Date date = inputFormat.parse(orderdate.toString());
									String formattedDate = outputFormat.format(date);
									// System.out.println("Formatted Date: " + formattedDate);
									cbPoHistoryData.setorderDate(formattedDate);

									// Use this formattedDate in your Playwright automation
								} catch (ParseException e) {
									e.printStackTrace();
								}
								// cbPoHistoryData.setWindowStart(windowStartDate);
							} catch (NullPointerException | DateTimeParseException e) {
								cbPoHistoryData.setorderDate("0000-00-00");
							}

							try {
								Date expecteddate = null;

								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX") || vName.startsWith("ES") || vName.startsWith("GB")
										|| vName.startsWith("FR") || vName.startsWith("IT")|| vName.startsWith("SE")|| vName.startsWith("BE")|| vName.startsWith("NL")|| vName.startsWith("PL") || vName.startsWith("AE")) {
									expecteddate = currentRow.getCell(firstRow.get("expected date")).getDateCellValue();
								} else {
									expecteddate = currentRow.getCell(firstRow.get("expected hand-off date"))
											.getDateCellValue();
								}
								// System.out.println("expecteddate" + expecteddate);
								try {
									Date date = inputFormat.parse(expecteddate.toString());
									String formattedDate = outputFormat.format(date);
									// System.out.println("Formatted Date: " + formattedDate);
									cbPoHistoryData.setExpectedDate(formattedDate);

									// Use this formattedDate in your Playwright automation
								} catch (ParseException e) {
									e.printStackTrace();
								}
								// cbPoHistoryData.setWindowStart(windowStartDate);
							} catch (NullPointerException | DateTimeParseException e) {
								cbPoHistoryData.setExpectedDate("0000-00-00");
							}

							try {

								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX") || vName.startsWith("ES") || vName.startsWith("GB")
										|| vName.startsWith("FR") || vName.startsWith("IT")|| vName.startsWith("SE")|| vName.startsWith("BE")|| vName.startsWith("NL")|| vName.startsWith("PL") || vName.startsWith("AE")) {
									cbPoHistoryData.setQuantityRequested((int) currentRow
											.getCell(firstRow.get("requested quantity")).getNumericCellValue());
								} else {
									cbPoHistoryData.setQuantityRequested((int) currentRow
											.getCell(firstRow.get("requested quantity (units)")).getNumericCellValue());
								}

							} catch (IllegalStateException e) {
								cbPoHistoryData.setQuantityRequested(0);
							} catch (NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setQuantityRequested(0);
							}
							try {
								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX") || vName.startsWith("ES") || vName.startsWith("GB")
										|| vName.startsWith("FR") || vName.startsWith("IT")|| vName.startsWith("SE")|| vName.startsWith("BE")|| vName.startsWith("NL")|| vName.startsWith("PL") || vName.startsWith("AE")) {
									cbPoHistoryData.setAcceptedQuantity((int) currentRow
											.getCell(firstRow.get("accepted quantity")).getNumericCellValue());
								} else {
									cbPoHistoryData.setAcceptedQuantity((int) currentRow
											.getCell(firstRow.get("accepted quantity (units)")).getNumericCellValue());

								}
							} catch (IllegalStateException e) {
								cbPoHistoryData.setAcceptedQuantity(0);
							} catch (NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setAcceptedQuantity(0);
							}
							try {
								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX") || vName.startsWith("ES") || vName.startsWith("GB")
										|| vName.startsWith("FR") || vName.startsWith("IT")|| vName.startsWith("SE")|| vName.startsWith("BE")|| vName.startsWith("NL")|| vName.startsWith("PL") || vName.startsWith("AE")) {
									cbPoHistoryData.setQuantityReceived((int) currentRow
											.getCell(firstRow.get("received quantity")).getNumericCellValue());
								} else {
									cbPoHistoryData.setQuantityReceived((int) currentRow
											.getCell(firstRow.get("received quantity (units)")).getNumericCellValue());
								}
							} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setQuantityReceived(0);
							}
							try {
								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX") || vName.startsWith("ES") || vName.startsWith("GB")
										|| vName.startsWith("FR") || vName.startsWith("IT")|| vName.startsWith("SE")|| vName.startsWith("BE")|| vName.startsWith("NL")|| vName.startsWith("PL") || vName.startsWith("AE")) {
									cbPoHistoryData.setQuantityOutstanding((int) currentRow
											.getCell(firstRow.get("remaining quantity")).getNumericCellValue());
								} else {
									cbPoHistoryData.setQuantityOutstanding((int) currentRow
											.getCell(firstRow.get("remaining quantity (units)")).getNumericCellValue());
								}

							} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setQuantityOutstanding(0);
							}

							try {
								cbPoHistoryData.setTotalCost(
										currentRow.getCell(firstRow.get("total accepted cost")).getNumericCellValue());
							} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setTotalCost(0.0);
							}
							try {
								cbPoHistoryData.setPOASIN(cbPoHistoryData.getPO() + cbPoHistoryData.getASIN());
							} catch (NullPointerException e) {

							}
							cbPoHistoryData.setUkey(vendorId + cbPoHistoryData.getPO() + cbPoHistoryData.getASIN());
							logger.info("unique key" + vendorId + cbPoHistoryData.getPO() + cbPoHistoryData.getASIN());
							cbPoHistoryData.setUkey(cbPoHistoryData.getASIN() + "-" + cbPoHistoryData.getPO() + vendorId);
							if (cbPoHistoryData.getPO() != null && cbPoHistoryData.getASIN() != null) {
								rowList.add(cbPoHistoryData);
							} else {
								logger.info("NULL PO ASIN ROW");
							}
						
						}
						System.out.println("size of insert" + rowList.size());
						StringBuilder sb = new StringBuilder(
								"INSERT IGNORE INTO CBPOHistoryData(requestId, vendorId,POASIN,acceptedQuantity,ASIN, availability,  externalID, externalIdType, modelNumber, PO, quantityOutstanding,quantityReceived,quantityRequested,shiptolocation, title,totalCost,currency,unitCost,currency1,vendor,windowStart,  windowType,windowEnd,expectedDate,poOrderDate,createdDate,caseQty,isCase,Ukey) VALUES");
						for (int row1 = 0; row1 < rowList.size(); row1++) {

							if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
									|| vName.startsWith("MX")) {
								sb.append("('" + requestId + "','" + vendorId + "','" + rowList.get(row1).getPOASIN()
										+ "','" + rowList.get(row1).getAcceptedQuantity() + "','"
										+ rowList.get(row1).getASIN() + "','" + rowList.get(row1).getAvailability()
										+ "','" + rowList.get(row1).getExternalID() + "','"
										+ rowList.get(row1).getExternalIdType() + "','"
										+ rowList.get(row1).getModelNumber() + "','" + rowList.get(row1).getPO() + "','"
										+ rowList.get(row1).getQuantityOutstanding() + "','"
										+ rowList.get(row1).getQuantityReceived() + "','"
										+ rowList.get(row1).getQuantityRequested() + "','"
										+ rowList.get(row1).getShiptolocation() + "', '" + rowList.get(row1).getTitle()
										+ "','" + rowList.get(row1).getTotalCost() + "','"
										+ rowList.get(row1).getCurrency() + "','" + rowList.get(row1).getUnitCost()
										+ "','" + rowList.get(row1).getCurrency1() + "','"
										+ rowList.get(row1).getVendor() + "','"
										+ rowList.get(row1).getWindowStart()
										+ "','" + rowList.get(row1).getWindowType() + "','"
										+ rowList.get(row1).getWindowEnd()
										+ "','"
										+ rowList.get(row1).getExpectedDate()
										+ "','"
										+ rowList.get(row1).getorderDate()
										+ "',NOW(),'" + rowList.get(row1).getCaseQty() + "',"
										+ rowList.get(row1).isCase() + ",'" + rowList.get(row1).getUkey() + "')");
								sb.append(",");
							} else {
								sb.append("('" + requestId + "','" + vendorId + "','" + rowList.get(row1).getPOASIN()
										+ "','" + rowList.get(row1).getAcceptedQuantity() + "','"
										+ rowList.get(row1).getASIN() + "','" + rowList.get(row1).getAvailability()
										+ "','" + rowList.get(row1).getExternalID() + "','"
										+ rowList.get(row1).getExternalIdType() + "','"
										+ rowList.get(row1).getModelNumber() + "','" + rowList.get(row1).getPO() + "','"
										+ rowList.get(row1).getQuantityOutstanding() + "','"
										+ rowList.get(row1).getQuantityReceived() + "','"
										+ rowList.get(row1).getQuantityRequested() + "','"
										+ rowList.get(row1).getShiptolocation() + "', '" + rowList.get(row1).getTitle()
										+ "','" + rowList.get(row1).getTotalCost() + "','"
										+ rowList.get(row1).getCurrency() + "','" + rowList.get(row1).getUnitCost()
										+ "','" + rowList.get(row1).getCurrency1() + "','"
										+ rowList.get(row1).getVendor() + "','"
										+ rowList.get(row1).getWindowStart()
										+ "','" + rowList.get(row1).getWindowType() + "','"
										+ rowList.get(row1).getWindowEnd()
										+ "','"
										+ rowList.get(row1).getExpectedDate()
										+ "','"
										+rowList.get(row1).getorderDate()
										+ "',NOW(),'" + rowList.get(row1).getCaseQty() + "',"
										+ rowList.get(row1).isCase() + ",'" + rowList.get(row1).getUkey() + "')");
								sb.append(",");
							}

						}
						String query = sb.substring(0, sb.length() - 1) + ";";
						try {
							logger.info("Query: " + query);
							jdbcTemplate.execute(query);
						} catch (Exception e) {

							e.printStackTrace();
						}
						rowList.clear();
					}
					fileStream.close();
					tempFile.delete();

				

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
