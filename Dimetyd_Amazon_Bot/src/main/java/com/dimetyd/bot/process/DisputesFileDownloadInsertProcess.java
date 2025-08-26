package com.dimetyd.bot.process;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.VendorCrd;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

@Component
public class DisputesFileDownloadInsertProcess {

	@Autowired
	JdbcTemplate jdbcTemplate;
	private String resolvedDate;

	@Autowired
	open_shortage_Dispute_Update_ReadExcel readExcel;
	@Autowired
	CommonUtil commonobj;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void processPage(Page page, String vendorId, String vendorName, String Type, String DisputeTableName)

	{

		Path filePath;
		logger.info(vendorName);

		String URL = commonobj.getCountryUrl(vendorName);
		logger.info("Country URL : " + URL);

		// Navigate Page to

		page.navigate(URL + "/hz/vendor/members/disputes?ref_=vc_xx_subNav");

		// page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute
		// Management Remove")).click();

		page.waitForTimeout(4000);
		page.evaluate("() => {" + "const loader = document.querySelector('.melodic-loading-overlay');"
				+ "if (loader) loader.remove();" + "}");
		logger.info("POP UP END");

		while (true) {
			try {
				Locator disputeIdPage = page.locator("//option[@value='DISPUTE_ID']");
				if (disputeIdPage.count() > 0) {
					break;
				} else {
					page.reload();
					Thread.sleep(2000);
				}
			} catch (Exception e) {
				page.reload();
				page.waitForTimeout(1000);
			}
		}

		int index = 1;
		while (true) {
			
			
			Locator DisputeSearchCriteriaDropdownLocator=page.locator("//span[@id='search-criterion']//span[@data-action='a-dropdown-button']");
			Locator DisputeTypeDropdownLocator=page.locator("//span[@id='dispute-type']");
			if (index == 1) {
				logger.info("******Downloading Shortage invoice Re-Dispute File******");
				
				DisputeSearchCriteriaDropdownLocator.click();
				page.getByLabel("Dispute Type").getByText("Dispute Type").click();

				page.waitForTimeout(1000);
				
				DisputeTypeDropdownLocator.click();
				page.getByLabel("Shortage Invoice Re-Dispute").getByText("Shortage Invoice Re-Dispute").click();
				
			} else if (index == 2) {
				
				logger.info("******Downloading Shortage invoice File******");
				
				DisputeSearchCriteriaDropdownLocator.click();
				page.getByLabel("Dispute Type").getByText("Dispute Type").click();
				
				page.waitForTimeout(1000);
				
				DisputeTypeDropdownLocator.click();
				page.locator("//li/a[contains(text(),'Shortage invoice')]").click();
			}

			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();

			page.waitForTimeout(4000);
			page.evaluate("() => {" + "const loader = document.querySelector('.melodic-loading-overlay');"
					+ "if (loader) loader.remove();" + "}");
			logger.info("POP UP END");
			while (true) {
				page.waitForTimeout(1500);
				Locator selectAccountPageLoaded = page
						.locator("//*[@class='utility-bar-button-link']//span[text()='Help']");
				if (selectAccountPageLoaded.count() > 0) {
					break;
				} else {
					page.reload();
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			try {
				Download download = page.waitForDownload(
					    new Page.WaitForDownloadOptions().setTimeout(900_000), // 15 minutes
					    () -> {
					        // Trigger the download
					        Locator exportAsSpreadSheet = page.getByText("Download");
					        if (exportAsSpreadSheet.count() > 0) {
					            exportAsSpreadSheet.click();
					        } else {
					            page.reload();
					            try {
					                Thread.sleep(2500);
					            } catch (InterruptedException e) {
					                e.printStackTrace();
					            }
					        }
					    }
					);
				filePath = Paths.get("C:\\Playwright File\\", download.suggestedFilename());
				download.saveAs(filePath);

			}

			catch (Exception e) {

				Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(900_000),() -> {
					// Perform the action that initiates download

					Locator downloadBtn = page.getByText("Export as spreadsheet");
					if (downloadBtn.count() > 0) {
						downloadBtn.click();
					} else {
						page.reload();
						try {
							Thread.sleep(2500);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					// TODO Auto-generated catch block
					// e.printStackTrace();

				});
				filePath = Paths.get("C:\\Playwright File\\", download.suggestedFilename());
				download.saveAs(filePath);

				// TODO: handle exception
			}
			try {
				FileInputStream file = new FileInputStream(filePath.toString());
				XSSFWorkbook workbook = new XSSFWorkbook(file);

				XSSFSheet sheet = workbook.getSheetAt(0);

				for (Row row : sheet) {
					// First, build a map of header names to column indexes
					Row headerRow = sheet.getRow(0); // assuming header is in the first row
					Map<String, Integer> headerMap = new HashMap<>();

					for (Cell cell : headerRow) {
						headerMap.put(cell.getStringCellValue(), cell.getColumnIndex());
					}
					if (row.getRowNum() == 0) {
						// Skip header row
						continue;
					}

					String disputeId = getCellStringValue(row, headerMap, "Dispute ID");
					String disputeReason = getCellStringValue(row, headerMap, "Dispute reason");
					String disputeDate = getCellStringValue(row, headerMap, "Dispute date");
					String disputeStatus = getCellStringValue(row, headerMap, "Dispute Status");
					String totalDisputedAmount = commonobj
							.processAmount(getCellStringValue(row, headerMap, "Total disputed amount"));
					String approvedAmount = commonobj
							.processAmount(getCellStringValue(row, headerMap, "Approved Amount"));

					String checkSql = "SELECT * FROM " + DisputeTableName + " WHERE disputeId = '" + disputeId
							+ "' and vendorId='" + vendorId + "';";

					List<VendorCrd> disputeData = this.jdbcTemplate.query(checkSql, new RowMapper<VendorCrd>() {
						@Override
						public VendorCrd mapRow(ResultSet rs, int rowNum) throws SQLException {

							VendorCrd Cbdata = new VendorCrd();

							return Cbdata;
						}
					}, new Object[] {});

					System.out.println("Dispute Data Size : " + disputeData.size());
					if (disputeData.size() > 0) {
						System.out.println("Dispute Id data is already present");
						String updateSql = "UPDATE " + DisputeTableName + " SET disputeStatus = '" + disputeStatus
								+ "', totalDisputedAmount = '" + totalDisputedAmount + "', approvedAmount = '"
								+ approvedAmount + "',disputeDate= '" + disputeDate+"' WHERE disputeId = '" + disputeId + "' and vendorId='" + vendorId
								+ "' ;";
					
						jdbcTemplate.execute(updateSql);
					} else {
						System.out.println("Dispute Id data is not present");
						StringBuilder sql = new StringBuilder();
						sql.append("INSERT IGNORE INTO " + DisputeTableName
								+ " (`disputeId`,`vendorId`,`disputeType`,`disputeReason`,`disputeDate`,`disputeStatus`,`totalDisputedAmount`,`approvedAmount`)\r\n"
								+ "VALUES ('" + disputeId + "','" + vendorId + "','" + Type + "','" + disputeReason
								+ "',STR_TO_DATE('" + disputeDate + "','%m/%d/%Y'),'" + disputeStatus + "','"
								+ totalDisputedAmount + "','" + approvedAmount + "')");
						
						jdbcTemplate.execute(sql.toString());
					}
					logger.info("DisputeId : " + disputeId + " disputeStatus: " + disputeStatus + " Approved Amount : "
							+ approvedAmount);

				}

				workbook.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}

			index = index + 1;
		}

	}

	private String getCellStringValue(Row row, Map<String, Integer> headerMap, String columnName) {
		Integer colIndex = headerMap.get(columnName);
		if (colIndex == null)
			return ""; // column not found
		Cell cell = row.getCell(colIndex);
		if (cell == null)
			return "";

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
			}
			return String.valueOf(cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			return cell.getCellFormula();
		case BLANK:
		default:
			return "";
		}
	}
}
