package com.dimetyd.bot.process;

import java.io.File;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.dimetyd.bot.model.AgreementData;
import com.dimetyd.bot.util.CommonUtil;

@Component
public class Stage_1_FileDownloadAndGetInvoiceId {
	@Autowired
	JdbcTemplate jdbcTemp;
	@Autowired
	ShipmentFile_Import_File shipmentFileimport;
	private Logger logger = LoggerFactory.getLogger(getClass());
	public String getInvoiceid(Page page, String requestId,Path downloadPath,String vendorId, String vendorName)
	{
		String URL=page.url();
		URL=URL.replace("/home/vc","").replace("/hz/vendor/members/coop","").replace("/hz/vendor/members/disputes","")+"/hz/vendor/members/coop";
		logger.info("*********** URL :"+URL+" ***********");
		//page.navigate(URL);
		
		
		String AgreementsQuery="SELECT DISTINCT agreementId FROM CBCoopDisputeTobeSubmittedDetails WHERE requestId ='"+requestId+"'";
		
		List<AgreementData> AgreementList = this.jdbcTemp.query(AgreementsQuery,new RowMapper<AgreementData>() {
			@Override
			public AgreementData mapRow(ResultSet rs, int rowNum) throws SQLException {

				AgreementData cbs = new AgreementData();
				cbs.setAgreementId(rs.getString("agreementId"));

				return cbs;
			}

		}, new Object[] {});

		String AllAgreement="";
		int count=1;
		for(AgreementData agdata:AgreementList)
		{
			if(count==1)
			{
					AllAgreement="'"+agdata.getAgreementId();
					
			}
			else
			{
				AllAgreement=AllAgreement+"','"+agdata.getAgreementId()+"'";
			}
			count++;
		}
		AllAgreement=AllAgreement.replace("''","'");
		
		logger.info(AllAgreement);
	
		
		String InvoiceNumbersQuery="SELECT GROUP_CONCAT(DISTINCT invoiceNumber SEPARATOR '\\n') AS invoiceNumbers FROM Coop_Details WHERE agreementId IN ("+AllAgreement+");";
		 // Execute the query and get the result as a String
        List<String> result = jdbcTemp.queryForList(InvoiceNumbersQuery, String.class);

        // If result is empty, return empty string
        if (result.isEmpty()) {
            return null;
        }

        // Assuming the result contains only one row with the concatenated invoice numbers
        String InvoiceNumbers= result.get(0);
		logger.info(InvoiceNumbers);
		return InvoiceNumbers;
		
		
		
		/*
		boolean PageVisible=true;
		do
		{
			PageVisible=page.isVisible("//h1[@id='invoice-header' and contains(text(), 'CoOp deductions')]");
			if(PageVisible==false)
			{
				page.navigate(URL);
				page.waitForTimeout(3000);
				logger.info("Page Reloaded");
			}
		}while(PageVisible==false);
		try
		{
		logger.info(AllAgreement);
		page.locator("//textarea[@id='search-input']").fill(AllAgreement);
		page.waitForTimeout(1000);
		page.locator("//button[@id='search-button-announce']").click();
		page.waitForTimeout(2000);
		logger.info("Agreements Searched...");
		}
		catch(Exception ex)
		{
			logger.info("ERROR WHILE INPUT AND SERCHING FOR AGREEMENT DATA");
			logger.error(ex.toString());
		}
		
		
		try
		{
		page.locator("//a[@id='select-all']").click();
		page.locator("//span[@id='a-autoid-0-announce']").click();
		page.waitForTimeout(1000);
		}
		catch(Exception ex)
		{
			logger.info("Issue while Choosing Dropdown option....");
			logger.error(ex.toString());
		}
		
		
		
		Locator downloadBackupButton = page
				.locator("//*[@id='cc-invoice-actions-dropdown_2' and contains(text(), 'Export to a spreadsheet')]");
		Download download = page.waitForDownload(() -> {
			downloadBackupButton.click();
		});

		try {
			Path filePath = downloadPath.resolve(download.suggestedFilename());
			System.out.println("Downloading file to: " + filePath);

			// Save the downloaded file to the specified path
			download.saveAs(filePath);
			File tempFile = filePath.toFile();

			boolean fileExists = CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));

			if (fileExists) {
				System.out.println("File exists!");
			} else {
				System.out.println("File does not exist within the timeout period.");

			}
			logger.info("File Downloaded.........");
			String FilePathString=filePath.toString();
			
			return shipmentFileimport.getInvoiceIdsFromExcel(FilePathString);
		}
		catch(Exception ex)
		{
			logger.info("Issue found while Downloading file");
			logger.error(ex.toString());
		}
		return null;*/
	}
}
