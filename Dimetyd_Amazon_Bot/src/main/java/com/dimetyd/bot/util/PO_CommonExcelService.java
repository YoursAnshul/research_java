package com.dimetyd.bot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.CBPoHistoryData;
import com.dimetyd.bot.model.CBPOData;

@Component
public class PO_CommonExcelService {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
	final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");

	public void readPurchaseOrderFile(Path filePath, String vendorName, String vendorId, Long requestId) {
		try {

			String vName = vendorName.substring(0, 2);
			File tempFile = filePath.toFile();
			
			boolean fileExists = CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));

			if (fileExists) {
				System.out.println("File exists!");
			} else {
				System.out.println("File does not exist within the timeout period.");
				return;
			}
			try (InputStream fileStream = new FileInputStream(tempFile);
					
					XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
							 
					//Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(fileStream)
							) {
				for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
					Iterator<Row> rows = workbook.getSheetAt(sheetIndex).rowIterator();
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
								if (entry.getKey().equalsIgnoreCase("unit cost")) {
									try {
										cbPoHistoryData.setUnitCost(
												currentRow.getCell(firstRow.get("unit cost")).getNumericCellValue());
									} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
										cbPoHistoryData.setUnitCost(0.0);
									}
									try {
										cbPoHistoryData.setCurrency(
												currentRow.getCell(firstRow.get("unit cost") + 1).getStringCellValue());
									} catch (NullPointerException e) {

									}
									try {
										cbPoHistoryData.setCurrency1(
												currentRow.getCell(firstRow.get("unit cost") + 3).getStringCellValue());
									} catch (NullPointerException e) {

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
									try {
										cbPoHistoryData.setCurrency1(
												currentRow.getCell(firstRow.get("case cost") + 3).getStringCellValue());
									} catch (NullPointerException e) {

									}
									try {
										cbPoHistoryData.setCaseQty((int) currentRow.getCell(firstRow.get("case size"))
												.getNumericCellValue());
										cbPoHistoryData.setCase(true);
									} catch (NullPointerException | IllegalStateException | NumberFormatException e) {
										cbPoHistoryData.setCaseQty(0);
										cbPoHistoryData.setCase(false);
									}
								}
							}
							try {
								cbPoHistoryData.setPO(currentRow.getCell(firstRow.get("po")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							try {
								cbPoHistoryData
										.setVendor(currentRow.getCell(firstRow.get("vendor")).getStringCellValue());
							} catch (NullPointerException e) {

							}

							try {
								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX")) {
									cbPoHistoryData.setShiptolocation(
											currentRow.getCell(firstRow.get("ship to location")).getStringCellValue());
								} else {
									cbPoHistoryData.setShiptolocation(
											currentRow.getCell(firstRow.get("warehouse")).getStringCellValue());
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
								cbPoHistoryData.setTitle(currentRow.getCell(firstRow.get("title")).getStringCellValue()
										.replaceAll("'", ""));
							} catch (NullPointerException e) {

							}

							String newTitle = "\"" + cbPoHistoryData.getTitle() + "\"";
							try {
								cbPoHistoryData.setAvailability(
										currentRow.getCell(firstRow.get("availability")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							try {
								cbPoHistoryData.setWindowType(
										currentRow.getCell(firstRow.get("window type")).getStringCellValue());
							} catch (NullPointerException e) {

							}
							try {
								String windowStartDate = currentRow.getCell(firstRow.get("window start"))
										.getStringCellValue();

								cbPoHistoryData.setWindowStart(windowStartDate);

							} catch (NullPointerException | DateTimeParseException e) {
								// cbPoHistoryData.setWindowStart(null);
							}
							try {
								String windowEnd = currentRow.getCell(firstRow.get("window end")).getStringCellValue();
								cbPoHistoryData.setWindowEnd(windowEnd);
							} catch (NullPointerException | DateTimeParseException e) {
								// cbPoHistoryData.setWindowEnd(null);
							}
							try {
								String expectedDate = currentRow.getCell(firstRow.get("expected date"))
										.getStringCellValue();

								cbPoHistoryData.setExpectedDate(expectedDate);
							} catch (NullPointerException | DateTimeParseException e) {
								// cbPoHistoryData.setExpectedDate(null);
							}

							try {
								cbPoHistoryData.setQuantityRequested((int) currentRow
										.getCell(firstRow.get("quantity requested")).getNumericCellValue());
							} catch (IllegalStateException e) {
								cbPoHistoryData.setQuantityRequested(0);
							} catch (NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setQuantityRequested(0);
							}
							try {
								cbPoHistoryData.setAcceptedQuantity((int) currentRow
										.getCell(firstRow.get("accepted quantity")).getNumericCellValue());
							} catch (IllegalStateException e) {
								cbPoHistoryData.setAcceptedQuantity(0);
							} catch (NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setAcceptedQuantity(0);
							}
							try {
								if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("AU")
										|| vName.startsWith("MX")) {
									cbPoHistoryData.setQuantityReceived((int) currentRow
											.getCell(firstRow.get("quantity received")).getNumericCellValue());
								} else {
									cbPoHistoryData.setQuantityReceived((int) currentRow
											.getCell(firstRow.get("received quantity")).getNumericCellValue());
								}
							} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setQuantityReceived(0);
							}
							try {
								cbPoHistoryData.setQuantityOutstanding((int) currentRow
										.getCell(firstRow.get("quantity outstanding")).getNumericCellValue());
							} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setQuantityOutstanding(0);
							}

							try {
								cbPoHistoryData.setTotalCost(
										currentRow.getCell(firstRow.get("total cost")).getNumericCellValue());
							} catch (IllegalStateException | NullPointerException | NumberFormatException e) {
								cbPoHistoryData.setTotalCost(0.0);
							}
							try {
								cbPoHistoryData.setPOASIN(cbPoHistoryData.getPO() + cbPoHistoryData.getASIN());
							} catch (NullPointerException e) {

							}
							cbPoHistoryData.setUkey(vendorId + cbPoHistoryData.getPOASIN());
							rowList.add(cbPoHistoryData);
						}
						

						StringBuilder sb = new StringBuilder(
								"  INSERT IGNORE INTO CBPOHistoryData(requestId, vendorId,POASIN,acceptedQuantity,ASIN, availability,  externalID, externalIdType, modelNumber, PO, quantityOutstanding,quantityReceived,quantityRequested,shiptolocation, title,totalCost,currency,unitCost,currency1,vendor,windowStart,  windowType,windowEnd,expectedDate,createdDate,caseQty,isCase,Ukey) VALUES");
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
										+ rowList.get(row1).getVendor() + "',STR_TO_DATE('"
										+ rowList.get(row1).getWindowStart() + "','%m/%d/%Y'),'"
										+ rowList.get(row1).getWindowType() + "',STR_TO_DATE('"
										+ rowList.get(row1).getWindowEnd() + "','%m/%d/%Y'),STR_TO_DATE('"
										+ rowList.get(row1).getExpectedDate() + "','%m/%d/%Y'),'"
										+ Date.valueOf(LocalDate.now()) + "','" + rowList.get(row1).getCaseQty() + "',"
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
										+ rowList.get(row1).getVendor() + "',STR_TO_DATE('"
										+ rowList.get(row1).getWindowStart() + "','%d/%m/%Y'),'"
										+ rowList.get(row1).getWindowType() + "',STR_TO_DATE('"
										+ rowList.get(row1).getWindowEnd() + "','%d/%m/%Y'),STR_TO_DATE('"
										+ rowList.get(row1).getExpectedDate() + "','%d/%m/%Y'),'"
										+ Date.valueOf(LocalDate.now()) + "','" + rowList.get(row1).getCaseQty() + "',"
										+ rowList.get(row1).isCase() + ",'" + rowList.get(row1).getUkey() + "')");
								sb.append(",");
							}

						}
						String query = sb.substring(0, sb.length() - 1) + ";";
						try {
							logger.info("Query : " + query);
							jdbcTemplate.execute(query);
						} catch (Exception e) {

							e.printStackTrace();
						}
						rowList.clear();
					}
					fileStream.close();
					tempFile.delete();

				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void readOrderHistoryFile(Long id, String vendorId, String month, String yearString, Integer limit,
			String vName, Path filePath) {
		File tempFile = filePath.toFile();
		boolean fileExists = CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));

		if (fileExists) {
			System.out.println("File exists!");
		} else {
			System.out.println("File does not exist within the timeout period.");
			return;
		}
		FileInputStream file = null;
		try {
			file = new FileInputStream(tempFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
			Iterator<Row> rows = workbook.getSheetAt(sheetIndex).rowIterator();
			HashMap<String, Integer> firstRow = new HashMap<String, Integer>();
			if (rows.hasNext()) {
				Row r = rows.next(); // Get the first row
				for (int cn = 0; cn < r.getLastCellNum(); cn++) {
					Cell c = r.getCell(cn);
					if (c != null && c.getCellType() == CellType.STRING) {
						String text = c.getStringCellValue();
						firstRow.put(text, cn);
					}
				}
			}

			while (rows.hasNext()) {
				
				List<CBPOData> rowList = new ArrayList<CBPOData>();
				for (int rownum = 0; rownum < 100; rownum++) {
					Row currentRow = null;
					try {
						currentRow = rows.next();
					} catch (java.util.NoSuchElementException e) {
						break;
					}
					System.out.println("");
					CBPOData cbPoDataObj = new CBPOData();
					try {
						cbPoDataObj.setPO(currentRow.getCell(firstRow.get("PO")).getStringCellValue());
						cbPoDataObj.setWndowType(currentRow.getCell(firstRow.get("Window Type")).getStringCellValue());
					} catch (NullPointerException e) {

					}
					try {
						if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("MX")
								|| vName.startsWith("AU")) {
							java.util.Date date = null;
							try {
								date = formatterForUS
										.parse(currentRow.getCell(firstRow.get("Ordered On")).getStringCellValue());
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							cbPoDataObj.setOrder_Date(formatterForUS.format(date));

						} else {
							java.util.Date date = null;
							try {
								date = formatterForOthers
										.parse(currentRow.getCell(firstRow.get("Ordered On")).getStringCellValue());
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							cbPoDataObj.setOrder_Date(formatterForOthers.format(date));
						}

					} catch (NullPointerException e) {

					}
					rowList.add(cbPoDataObj);
				}
				
				StringBuilder sb;
				sb = new StringBuilder(
						"  INSERT IGNORE INTO CBPOData(Order_Date,PO,vendorId,createdDate,month,year,status,Ukey,windowType) VALUES");
				if (vName.startsWith("CA") || vName.startsWith("US") || vName.startsWith("MX")
						|| vName.startsWith("AU")) {
					if (limit > 1000) {
						for (int row1 = 0; row1 < rowList.size(); row1++) {
							sb.append("(STR_TO_DATE('" + rowList.get(row1).getOrder_Date() + "','%m/%d/%Y'),'"
									+ rowList.get(row1).getPO() + "','" + vendorId + "',NOW(),'" + month + "','"
									+ yearString + "','PENDING','" + rowList.get(row1).getPO() + vendorId + "','"+ rowList.get(row1).getWndowType() +"')");
							sb.append(",");
						}

					} else {
						for (int row1 = 0; row1 < rowList.size(); row1++) {
							sb.append("(STR_TO_DATE('" + rowList.get(row1).getOrder_Date() + "','%m/%d/%Y'),'"
									+ rowList.get(row1).getPO() + "','" + vendorId + "',NOW(),'" + month + "','"
									+ yearString + "','COMPLETED','" + rowList.get(row1).getPO() + vendorId +"','"+ rowList.get(row1).getWndowType()+ "')");
							sb.append(",");
						}

					}
				} else {
					if (limit > 1000) {
						for (int row1 = 0; row1 < rowList.size(); row1++) {
							sb.append("(STR_TO_DATE('" + rowList.get(row1).getOrder_Date() + "','%d/%m/%Y'),'"
									+ rowList.get(row1).getPO() + "','" + vendorId + "',NOW(),'" + month + "','"
									+ yearString + "','PENDING','" + rowList.get(row1).getPO() + vendorId +"','"+ rowList.get(row1).getWndowType()+ "')");
							sb.append(",");
						}

					} else {
						for (int row1 = 0; row1 < rowList.size(); row1++) {
							sb.append("(STR_TO_DATE('" + rowList.get(row1).getOrder_Date() + "','%d/%m/%Y'),'"
									+ rowList.get(row1).getPO() + "','" + vendorId + "',NOW(),'" + month + "','"
									+ yearString + "','COMPLETED','" + rowList.get(row1).getPO() + vendorId +"','"+ rowList.get(row1).getWndowType()+ "')");
							sb.append(",");
						}

					}
				}

				String query = sb.substring(0, sb.length() - 1) + ";";
				try {
					logger.info("Query : " + query);
					jdbcTemplate.execute(query);
				} catch (Exception e) {

					e.printStackTrace();
				}
				rowList.clear();
			}

		}

		logger.info("Update CBPOHistoryInput Set fileDownload='YES' where id='" + id + "'");
		jdbcTemplate.execute("Update CBPOHistoryInput Set fileDownload='YES' where id='" + id + "'");
		try {
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tempFile.delete();
	}
}
