package com.dimetyd.bot.process;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.dimetyd.bot.model.CoopDisputeTobeSubmittedDetailsData;

@Component
public class Stage_2_Submit_Dispute_Coop {

	@Autowired
	JdbcTemplate jdbcTemplate;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public String SubmitDispute(Page page, String invoiceNumber, String VendorId,String requestId, Path downloadPath) {

		// Navigate to Dispute page
		while (true) {
			try {
				Locator navigationMenu = page.getByLabel("Navigation menu");
				if (navigationMenu.count() > 0) {
					break;
				} else {
					page.reload();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			} catch (Exception e) {

			}
		}

		page.getByLabel("Navigation menu").click();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {
			page.waitForTimeout(1500);
			Locator paymentOption = page
					.locator("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
			if (paymentOption.count() > 0) {
				logger.info("payments Tab found");
				break;
			} else {
				logger.info("payments Tab not found");
				page.reload();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					page.getByLabel("Navigation menu").click();
					logger.info("click done again on navigation tab");
				} catch (Exception e) {
					logger.info("click not done again on navigation tab");
					page.reload();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			}
		}

		try {
			page.getByText("Payments").click();
		} catch (Exception e) {
			page.locator("//div[@class='side-nav-tab']/span[normalize-space(text())='Payments']").click();

			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute Management Remove")).click();

		Locator loader = page.locator(".a-popover-loading").first();
		loader.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));

		// Start process to creating dispute
		boolean isPopupFound = true;
		do {
			page.waitForTimeout(2000);
			isPopupFound = page.isVisible("//div[@class='melodic-loading-overlay' and @style='display: block;']");

		} while (isPopupFound);

		try {
			int count = 1;
			do {
				if (count <= 3) {
					page.waitForTimeout(1000);
					page.locator("//button[@id='create-new-dispute-button-announce']").click();

					page.waitForTimeout(1000);
					page.click("//span[@id='item-type']");
					page.waitForTimeout(1000);
					page.click("//*[@id='item-type-id_0' and contains(text(),'CoOp')]");

					page.waitForTimeout(1000);
					page.getByPlaceholder("If you want to dispute").click();

					page.getByPlaceholder("If you want to dispute").fill(invoiceNumber);
					page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("+")).click();
					// page.getByPlaceholder("If you want to dispute").click();
					page.waitForTimeout(2000);
					boolean isInvoiceError = page.getByText("These invoices are not").isVisible();

					if (isInvoiceError) {
						logger.info("Invoice number is not avaiable to create dispute..");
						page.locator("//button[@id='create-new-dispute-cancel-button-announce']").click();
						
						page.locator("//button[@id='create-new-dispute-button-announce']").click();
						
						page.waitForTimeout(1000);
						page.click("//span[@id='item-type']");
						page.waitForTimeout(1000);
						page.click("//*[@id='item-type-id_0' and contains(text(),'CoOp')]");
						String[] invoiceNumbers = invoiceNumber.split("\n");

						// Print the separated invoice numbers
						for (String invoices : invoiceNumbers) {
							page.getByPlaceholder("If you want to dispute").fill(invoices);
							page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("+")).click();
							isInvoiceError = page.getByText("These invoices are not").isVisible();
							if (isInvoiceError) {
								page.locator("//a[@id='clear-all-entries']").click();
								logger.info("Invoice Number NOT FOUND : " + invoices);
							}
						}

					}

					else {
						logger.info("-----------All Invoices are Available-----------");
					}
					page.locator("//span[@id='create-new-dispute-next-button']").click();
					page.waitForTimeout(2000);
					
					try
					{
					page.waitForSelector("kat-toggle[id='bulk-dispute-reason-toggle-button']>>div.indicator");
					}
					catch(Exception  ex)
					{
						ex.getStackTrace();
					}
					
					boolean PageCreateDisputePageFound = page.isVisible("kat-toggle[id='bulk-dispute-reason-toggle-button']>>div.indicator");
					if (PageCreateDisputePageFound) {
					
						String disputedAmount = page.locator("//div[@id='total-line-dispute-amount']").innerText();
						disputedAmount = disputedAmount.replaceAll("Dispute Amount :\\s*", "");
						
						String []AmountAndCurrency=extractCurrencyAndAmount(disputedAmount);
						
						String Currency=AmountAndCurrency[0];
						disputedAmount=AmountAndCurrency[1];
						
						logger.info(disputedAmount);
						
						String SumOverbilledAmountQuery = "SELECT SUM(overbilledRebate) as SumOverbilledRebate FROM CBCoopDisputeTobeSubmittedDetails WHERE requestId = '"
								+ requestId + "'";
						List<Long> TotalSumFound = jdbcTemplate.query(SumOverbilledAmountQuery.toString(),
								(rs, rowNum) -> rs.getLong("SumOverbilledRebate"));
						Long SumOverbilledAmount = 0L;
						if (!TotalSumFound.isEmpty()) {
							if (TotalSumFound.get(0) != null) {
								SumOverbilledAmount = TotalSumFound.get(0);
							}
						}

						page.locator("//kat-toggle[@id='bulk-dispute-reason-toggle-button']").click();
						page.waitForTimeout(1000);
						page.locator("//kat-dropdown[@id='bulk-dispute-reason-dropdown']").click();
						logger.info("Overbilled Amount : " + SumOverbilledAmount);
						if (SumOverbilledAmount > 1000) {
							page.locator(
									"kat-dropdown[id='bulk-dispute-reason-dropdown'] >> text='Mismatch in quantity billed vs quantity invoiced/shipped'")
									.click();

						} else {
							page.locator(
									"kat-dropdown[id='bulk-dispute-reason-dropdown'] >> text='Incorrect rebate amount'")
									.click();

						}

						page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).click();

						boolean IsFileAttach=false;
						do
						{
							if(count<=3)
							{
								try
								{
									String FileUploadPath=downloadPath.toString();
									FileUploadPath=FileUploadPath+"\\"+VendorId+"_"+requestId+".xlsx";
									IsFileAttach=FileAttach(page,requestId,FileUploadPath);
								count++;
								}
								catch(Exception ex)
								{
									logger.error(ex.toString());
									return "Not Disputed";
								}
							}
						}while(!IsFileAttach);
				
						count=1;
						String TitleQuery = "SELECT `title` FROM disputeitle GROUP BY  RAND() LIMIT 1";
						List<String> TitleData = jdbcTemplate.query(TitleQuery.toString(),
								(rs, rowNum) -> rs.getString("title"));
						String disputeTitle = TitleData != null && TitleData.size() > 0 ? TitleData.get(0) : null;

						String DisputeJustificationSummaryQuery = "SELECT `Title_Reasons_summary` FROM `disputeJustificationSummary` GROUP BY  RAND() LIMIT 1";

						List<String> JustificationData = jdbcTemplate.query(DisputeJustificationSummaryQuery.toString(),
								(rs, rowNum) -> rs.getString("Title_Reasons_summary"));
						String DisputeJustification = JustificationData != null && JustificationData.size() > 0
								? JustificationData.get(0)
								: null;

						logger.info("Dispute Title : " + disputeTitle);
						page.locator("kat-input[id='dispute-title-input'] input[type='text']").click();
						page.locator("kat-Input[id='dispute-title-input'] input[type='text']").clear();
						page.locator("kat-Input[id='dispute-title-input'] input[type='text']").fill(disputeTitle);

						logger.info("Dispute Justification Summary : " + DisputeJustification);

						page.getByRole(AriaRole.TEXTBOX,
								new Page.GetByRoleOptions().setName("Provide justification for the")).click();
						page.getByRole(AriaRole.TEXTBOX,
								new Page.GetByRoleOptions().setName("Provide justification for the")).clear();
						page.getByRole(AriaRole.TEXTBOX,
								new Page.GetByRoleOptions().setName("Provide justification for the"))
								.fill(DisputeJustification);
						page.locator("#root").click();
						logger.info("Provided Justification while creating dispute...");
						page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).click();
						
				
						page.getByRole(AriaRole.BUTTON, new
						Page.GetByRoleOptions().setName("Submit")).isVisible();
						page.waitForTimeout(1000);

						 page.getByRole(AriaRole.BUTTON, new
						Page.GetByRoleOptions().setName("Submit")).click();
							
						 logger.info("wait.......................................");

						page.waitForTimeout(100000);
						//page.locator("//kat-button[@id='submit-dispute-modal-submit-button']").click();
						logger.info("CREATED DISPUTE");

						page.waitForTimeout(2000);
						String SubmittedDisputeIdString = null;
						boolean isDisputeSubmitted = true;
						do {
							isDisputeSubmitted = page.isVisible("//div[@id='submit-dispute-submission-successful']");
							if (isDisputeSubmitted) {
								page.waitForTimeout(1000);
								SubmittedDisputeIdString = page
										.locator("//*[@id='submit-dispute-submission-successful']/div/div[2]/kat-label")
										.innerText();
								isDisputeSubmitted = false;
							}

						} while (isDisputeSubmitted);

						if (SubmittedDisputeIdString != null) {
							SubmittedDisputeIdString = extractDisputeId(SubmittedDisputeIdString);
						}

						logger.info("----------------------------------------");
						logger.info("##### " + SubmittedDisputeIdString + " #####");
						logger.info("----------------------------------------");

						
						  String InsertDisputeRequestQuery="INSERT INTO `red_T_Dispute_Request` (`disputeId`,`vendorId`,`disputeType`,`disputeDate`,`disputeStatus`,`totalDisputedAmount`,"
						  + "`createdDate`,`currency`,`disputeReason`) VALUES ('"
						  +SubmittedDisputeIdString.trim()+"','"+
						  VendorId+"','Coop',NOW(),'PENDING'," +
						  "'"+disputedAmount.replace("$","").replace(",","").replace("€",
						  "").replace("Dispute amount : ","").trim()+"',NOW(),'"+
						  Currency+"','Shipment Disparity')";
						 
						  logger.info(InsertDisputeRequestQuery);
						 jdbcTemplate.execute(InsertDisputeRequestQuery);
						 

						logger.info("DISPUTE STATUS------->DISPUTED");

						page.locator("//kat-button[@id='submit-dispute-modal-ok-button']").click();

						return "Disputed";
					}
				} else {
					page.locator("//*[@id='create-dispute-page-error']/div/kat-button").click();
					count++;
				}
			} while (true);
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "NOT Disputed";

		}

	}

	public static String extractDisputeId(String text) {
		String regex = "Dispute Id :\\s*(DSPT\\d+)";
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
		java.util.regex.Matcher matcher = pattern.matcher(text);

		if (matcher.find()) {
			return matcher.group(1); // Return the first captured group (Dispute ID)
		}
		return null; // Return null if no match found
	}

	public static String[] extractCurrencyAndAmount(String input) {
		// Map of currency symbols to 3-character currency codes
		Map<String, String> currencyMap = new HashMap<>();
		currencyMap.put("$", "USD");
		currencyMap.put("€", "EUR");
		currencyMap.put("£", "GBP");
		currencyMap.put("₹", "INR");
		currencyMap.put("¥", "JPY");

		// Regular expression to match currency and number
		String regex = "([\\$€£₹¥])?([0-9,]+(?:\\.\\d{1,2})?)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);

		// Check if the input matches the regex pattern
		if (matcher.matches()) {
			String currency = matcher.group(1); // Currency symbol
			String amount = matcher.group(2); // Numeric value

			// Replace the currency symbol with its 3-character code
			if (currency != null && currencyMap.containsKey(currency)) {
				currency = currencyMap.get(currency);
			} else {
				currency = "None"; // In case no recognized currency symbol is found
			}

			// Return the results as an array
			return new String[] { currency, amount };
		} else {
			// Return null if the format is invalid
			return null;
		}
	}
	public boolean FileAttach(Page page,String requestId, String filePath)
	{
		 try {
			// SQL Query
			String sql="SELECT `agreementId`,`ASIN`,`PO`,`amazonBilledQty`,`vendorInvQty`,`excessUnitsBilled`,`excessNetReceipts`,`overbilledRebate`,`agreementCurrency` FROM CBCoopDisputeTobeSubmittedDetails WHERE requestId = '"+requestId+"'";
			

			List<CoopDisputeTobeSubmittedDetailsData> jobList = this.jdbcTemplate.query(sql, new RowMapper<CoopDisputeTobeSubmittedDetailsData>() {
			    @Override
			    public CoopDisputeTobeSubmittedDetailsData mapRow(ResultSet rs, int rowNum) throws SQLException {
			        CoopDisputeTobeSubmittedDetailsData cbs = new CoopDisputeTobeSubmittedDetailsData();
			        
			        // Map the SQL result to the Java object
			        cbs.setAgreementId(rs.getString("agreementId"));
			        cbs.setASIN(rs.getString("ASIN"));
			        cbs.setPO(rs.getString("PO"));
			        cbs.setAmazonBilledQty(rs.getInt("amazonBilledQty"));
			        cbs.setVendorInvQty(rs.getInt("vendorInvQty"));
			        cbs.setExcessUnitsBilled(rs.getInt("excessUnitsBilled"));
			        cbs.setExcessNetReceipts(rs.getDouble("excessNetReceipts"));
			        cbs.setOverbilledRebate(rs.getDouble("overbilledRebate"));
			        cbs.setAgreementCurrency(rs.getString("agreementCurrency"));
			        
			        return cbs;
			    }
			});
			 // Create a new workbook and a sheet
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Dispute Data");

			// Create a header row
			Row headerRow = sheet.createRow(0);
			String[] headers = {"Agreement ID", "ASIN", "PO", "Amazon Billed Qty", "Vendor Inv Qty",
			        "Excess Units Billed", "Excess Net Receipts", "Overbilled Rebate", "Agreement Currency"};

			// Add headers to the first row
			for (int i = 0; i < headers.length; i++) {
			    Cell cell = headerRow.createCell(i);
			    cell.setCellValue(headers[i]);
			}

			// Populate data rows
			int rowNum = 1;
			for (CoopDisputeTobeSubmittedDetailsData data : jobList) {
			    Row row = sheet.createRow(rowNum++);

			    row.createCell(0).setCellValue(data.getAgreementId());
			    row.createCell(1).setCellValue(data.getASIN());
			    row.createCell(2).setCellValue(data.getPO());
			    row.createCell(3).setCellValue(data.getAmazonBilledQty());
			    row.createCell(4).setCellValue(data.getVendorInvQty());
			    row.createCell(5).setCellValue(data.getExcessUnitsBilled());
			    row.createCell(6).setCellValue(data.getExcessNetReceipts());
			    row.createCell(7).setCellValue(data.getOverbilledRebate());
			    row.createCell(8).setCellValue(data.getAgreementCurrency());
			}

			// Write to Excel file
			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
			    workbook.write(fileOut);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}


			System.out.println("Data has been written to " + filePath);
			Locator inputFile=page.locator("kat-file-upload[id='drag-and-drop-file-uploader'] input[id='kat-file-attachment']");

		//	Locator fileInput = page.locator("kat-file-upload[id='drag-and-drop-file-uploader'] kat-button[id='select-file'] >> text='Upload files'");
	
   
			Path FileUpload=Paths.get(filePath);
			inputFile.setInputFiles(FileUpload);

   
			
			return true;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

}
