package com.dimetyd.bot.process;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class Coop24Process {

	
	
	@Autowired
	JdbcTemplate jdbctemp;
	private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
	public void processPage(Page page,Path downloadPath,String vendorName,String startDate,String endDate,String vendorId, Integer requestId)
	{
		String downladPath1=null;
		String DateFormat=null;
		try
		{
			logger.info("Navigating to Coop Page");
			page.locator("//div[@class='nav-button']").click();
			page.waitForTimeout(1000);
			page.locator("//div[@class='side-nav-tab']/span[text()='Payments']").click();
			page.waitForTimeout(1000);
			try {
				
			page.locator("//div[@data-test-tag='flyout-item']/a/div[1]/span[text()='CoOp']").waitFor(new Locator.WaitForOptions().setTimeout(3000));
			page.locator("//div[@data-test-tag='flyout-item']/a/div[1]/span[text()='CoOp']").click();
			} catch (Exception e) {
				page.locator("//div[@data-test-tag=\"flyout-item\"]/a/div[1]/span[text()='Co-op']").click();
			}
			page.waitForTimeout(1000);
			boolean isDatePickedVisible=page.isVisible("//*[@id=\"from-date-id\"]");
			do
			{
				if(isDatePickedVisible)
				{
					logger.info("Coop date picker loaded.....");
					
					if(vendorName.contains("US -") || vendorName.contains("CA -"))
					{
						DateFormat="MM/dd/yyyy";
						startDate=convertDateString(startDate, "yyyy-MM-dd HH:mm:ss","MM/dd/yyyy");
						endDate=convertDateString(endDate, "yyyy-MM-dd HH:mm:ss","MM/dd/yyyy");
						logger.info("Vendor Name Contains US or CA");
						inputDates(page,startDate,endDate);
					}
					else
					{
						DateFormat="dd/MM/yyyy";
						startDate=convertDateString(startDate, "yyyy-MM-dd HH:mm:ss","dd/MM/yyyy");
						endDate=convertDateString(endDate, "yyyy-MM-dd HH:mm:ss","dd/MM/yyyy");
						logger.info("Vendor Name NOT Contains US or CA");
						inputDates(page,startDate,endDate);
					}
					isDatePickedVisible=false;
				}
				else
				{
					logger.info("Coop Date Picker not loaded yet");
				}
			}while(isDatePickedVisible);
			
		
			page.waitForTimeout(2000);
			boolean isDataFound=page.isVisible("//*[@id=\"kat-invoice-table\"]");
			if(isDataFound)
			{
				logger.trace("Data FOUND");
				page.locator("//a[@id=\"select-all\"]").click();
				Locator dropEx = page.locator("#cc-invoice-actions-dropdown");
				
				Download download = page.waitForDownload(() -> {
				dropEx.selectOption("exportToSpreadsheet");
				});// Wait until the download starts

				// Get the suggested filename and create a Path object for it
				Path filePath = downloadPath.resolve(download.suggestedFilename());
				logger.info("Downloading file to: " + filePath);

				// Save the downloaded file to the specified path
				download.saveAs(filePath);
				downladPath1=filePath.toString();
				logger.trace("File Downloaded Successfully..........");
			}
			else
			{
				logger.trace("DATA NOT FOUND...........");
			}
			
			
			boolean isDatainserted = readFileAndUpdateInDB(downladPath1,vendorId,jdbctemp, vendorName, DateFormat,requestId);
			if(isDatainserted)
			{
				System.out.println("Data is Inserted Properly....");
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
			jdbctemp.execute("update CBCoopContraCogsInvoiceRequest set status='ERROR' where id='2596'");
		
		}
	}
	 public static String convertDateString(String inputDate, String inputFormat,String outputFormat) throws Exception {
		 SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
	        Date date = inputDateFormat.parse(inputDate);
	        
	        // Step 2: Format the Date object into the desired output format
	        SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
	        String outputDate = outputDateFormat.format(date);
	        
	        return outputDate;
	    }
	 public static void inputDates(Page page,String startDate,String endDate)
	 {

			//page.locator("//*[@id=\"from-date-id\"]").click();
			page.locator("//*[@id=\"from-date-id\"]").fill(startDate);
			
			page.locator("//textarea[@id=\"search-input\"]").click();
			page.waitForTimeout(1000);
			
			//page.locator("//*[@id=\"to-date-id\"]").click();
			page.locator("//*[@id=\"to-date-id\"]").fill(endDate);

			page.waitForTimeout(1000);
			page.locator("//span[@id=\"search-button\"]").click();
	 }
	// Function to read the Excel file and return a list of RowData
	    public static boolean readFileAndUpdateInDB(String filePath,String vendorId,JdbcTemplate jdbctemp,String vendorName, String DateFormat,int requestId) throws IOException, ParseException {
	        List<RowData> rowDataList = new ArrayList<>();

			// Create a FileInputStream to read the Excel file
			FileInputStream fis = new FileInputStream(filePath);

			// Create a workbook instance for the Excel file
			HSSFWorkbook workbook = new HSSFWorkbook(fis);

			// Get the first sheet in the Excel file
			HSSFSheet sheet = workbook.getSheetAt(0);
			String insertQuery = "INSERT IGNORE INTO `CB_CoOpDeductionsPercentage`(`vendorId`,`invoiceId`,`invoiceDate`, `agreementId`,`agreementTitle`, `fundingType`,"
					+ "`originalBalance`, `ifAccrualPercentage`,`currency`, `uniqueKey`,`createdDate`,`requestId`) VALUES";
			// Loop through rows (starting from 1 to skip the header)
			String insertData;
			int count = 0;
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				HSSFRow row = sheet.getRow(i);
				if (row != null) {
					// Read data from each cell
					String invoiceId = row.getCell(0).getStringCellValue();
					String invoiceDate = row.getCell(1).getStringCellValue();
					Integer agreementId = (int) row.getCell(2).getNumericCellValue();
					String agreementTitle = row.getCell(3).getStringCellValue();
					String fundingType = row.getCell(4).getStringCellValue();
					String originalBalance = row.getCell(5).getStringCellValue();
					String VendorCurrency = null;
					String Percentage = null;

					
						try {
							String regex = "[\\p{Sc}]"; // Match any character that is a currency symbol
							String regexPercentage = "(\\d+\\.\\d+)";

							// Create a Pattern object
							Pattern pattern = Pattern.compile(regex);
							Matcher matcher = pattern.matcher(originalBalance);

							pattern = Pattern.compile(regexPercentage);
							Matcher PrecentageValues = pattern.matcher(agreementTitle);

							if (PrecentageValues.find()) {
								Percentage = PrecentageValues.group();
							} else {
								System.out.println("Percentage not Found...");
							}

							// Find and extract the first currency symbol
							if (matcher.find()) {
								VendorCurrency = matcher.group();
							} else {
								System.out.println("No Currency found...");
							}

							VendorCurrency = VendorCurrency.trim().replace(",", "") // Remove commas
									.replace("$", "USD") // Replace $ with USD (if needed)
									.replace("£", "GBP") // Replace £ with GBP
									.replace("US$", "USD") // Replace US$ with USD
									.replace("€", "EUR") // Replace € with EUR
									.replace("zł", "PLN") // Replace zł with PLN
									.replace("KR", "SEK") // Replace KR with SEK
									.trim();
						} catch (Exception ex) {
							System.out.println("Error found amount Currency matching..." + ex.toString());
						}

						String ukey = vendorId + invoiceId;

						SimpleDateFormat inputDateFormat = new SimpleDateFormat(DateFormat);
						Date date = inputDateFormat.parse(invoiceDate);

						// Step 2: Format the Date object into the desired output format
						SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						invoiceDate = outputDateFormat.format(date);

						// Add the data as an object in the list
						insertData = "('" + vendorId + "','" + invoiceId + "','" + invoiceDate + "','" + agreementId + "','"
								+ agreementTitle.replace("'", "''") + "','" + fundingType + "'," + "'"
								+ originalBalance.toString().replace(",", "").replace("USD", "").replace("GBP", "")
										.replace("USD", " ").replace("EUR", "").replace("PLN", "").replace("SEK", "").trim()
										.replace("$", "").replace("€", "").replace("$", "") // Replace $ with USD (if needed)
										.replace("£", "").replace("US$","").replace("€", "").replace("zł", "").replace("KR","").trim()
								+ "','"+Percentage+"','" + VendorCurrency + "','" + ukey + "',NOW(),'" + requestId +"' ),";
						insertQuery = insertQuery + insertData;
						if (count == 100) {
							try {
								insertQuery = insertQuery.substring(0, insertQuery.length() - 1);
								System.out.println(insertQuery);
								jdbctemp.execute(insertQuery);
								System.out.println("BATCH INSERTED");
								insertQuery = "INSERT IGNORE INTO `CB_CoOpDeductionsPercentage`(`vendorId`,`invoiceId`,`invoiceDate`, `agreementId`,`agreementTitle`, `fundingType`,"
										+ "`originalBalance`, `ifAccrualPercentage`,`currency`, `uniqueKey`,`createdDate`,`requestId`) VALUES";
								insertData = null;
								count = 0;
							} catch (Exception ex) {
								System.out.println("Data is not inserted properly " + ex.toString());
								return false;
							}
						}

				
					count++;
				}

				// if data is less than 100 it will insert in database
			}
			if (count != 0) {
				System.out.println("BATCH INSERTION COMPLETED REMAINING LESS THAN 100 PENDING DATA INSERTING...");
				insertQuery = insertQuery.substring(0, insertQuery.length() - 1);
				System.out.println(insertQuery);
				jdbctemp.execute(insertQuery);
			}

			// Close the workbook and the FileInputStream
			workbook.close();
			fis.close();
			return true;

	    }
	 // Inner class to hold the data from each row in the Excel sheet
	    public static class RowData {
	        private String invoiceId;
	        public String getInvoiceId() {
				return invoiceId;
			}
			public String getInvoiceDate() {
				return invoiceDate;
			}
			public String getAgreementId() {
				return agreementId;
			}
			public String getAgreementTitle() {
				return agreementTitle;
			}
			public String getFundingType() {
				return fundingType;
			}
			public String getOriginalBalance() {
				return originalBalance;
			}


			private String invoiceDate;
	        private String agreementId;
	        private String agreementTitle;
	        private String fundingType;
	        private String originalBalance;

	        public RowData(String invoiceId, String invoiceDate, String agreementId, String agreementTitle, String fundingType, String originalBalance) {
	            this.invoiceId = invoiceId;
	            this.invoiceDate = invoiceDate;
	            this.agreementId = agreementId;
	            this.agreementTitle = agreementTitle;
	            this.fundingType = fundingType;
	            this.originalBalance = originalBalance;
	        }

	        @Override
	        public String toString() {
	            return "RowData{" +
	                    "invoiceId='" + invoiceId + '\'' +
	                    ", invoiceDate='" + invoiceDate + '\'' +
	                    ", agreementId='" + agreementId + '\'' +
	                    ", agreementTitle='" + agreementTitle + '\'' +
	                    ", fundingType='" + fundingType + '\'' +
	                    ", originalBalance=" + originalBalance +
	                    '}';
	        }
	    }


	
	
}
