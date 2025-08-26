package com.dimetyd.bot.process;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;


@Component
public class Stage_1_Shortage_Dispute_Submit {
	

	
	Path filePath = null;
	
	
	@Autowired
	CommonUtil util;
	
	@Autowired
	Shortage_Stage_1_Import_File importfile;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	

	public void downloadInvoice(Page page, String vendorId) {
		
		
		
		page.click("//div[@aria-label='Navigation menu']");
		try {
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
		} catch (Exception e) {
			page.reload();
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
		}
		List<ElementHandle> subMenuList = page.querySelectorAll(".flyout-menu-item-label");

		for (ElementHandle subMenu : subMenuList) {
			// Retrieve HTML content
			String htmlContent = subMenu.innerHTML();

			if ("Invoices".equals(htmlContent)) {
				// Click the element
				subMenu.click();
				logger.info("Clicked element with Playwright");
			
				break; // Exit the loop after clicking
			}
		}
		
		util.reFreshPage(page);
		
		
		boolean isnewUi = page.locator("//kat-badge[normalize-space(@label='Click here to return to the previous experience')]").isVisible();
		logger.info("isnewUi: "+isnewUi);
		
	
		
		if (isnewUi)
		{
			
			page.locator("//kat-badge[normalize-space(@label='Click here to return to the previous experience')]").click();
			logger.info("Clicked for old invoice");
		}
		
		
		
		
		while (true) {
			try {
				Locator invoicePage = page.locator("//span[@class='a-size-large a-text-bold'][text()='Search existing invoices']");
				if (invoicePage.count() > 0) {
					break;
				} else {
					page.reload();
					Thread.sleep(2000);
				}
			} catch (Exception e) {
				page.reload();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		logger.info("Navigated to invoice page");
		
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Review/dispute shortages")).click();
		
		
		
		
		while (true) {
			try {
				Locator invoicePage = page.locator("//a[@id='advancedSearchExportAll'][text()='Export All']");
				if (invoicePage.count() > 0) {
					break;
				} else {
					page.reload();
					Thread.sleep(2000);
				}
			} catch (Exception e) {
				page.reload();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		Download download ;
		boolean flag = true;
		Locator advancedSearch = page.getByText("Export All");
		Path downloadPath = Paths.get("C:\\Playwright File\\");
		File tempFile;
		Path filePath = null;
		
		while (flag) {
			try {

				try {
					download = page.waitForDownload(() -> {
						advancedSearch.click();
					});
				} catch (PlaywrightException e) {
					logger.info("Download timed out, retrying...");
					continue;
				} catch (Exception e) {
					util.closePopup(page);
					continue;
				}
				 filePath = downloadPath.resolve(download.suggestedFilename());
				logger.info("Downloading file to: " + filePath);

				// Save the downloaded file to the specified path
				download.saveAs(filePath);
				tempFile = filePath.toFile();

				boolean fileExists = util.waitForFile(tempFile, Duration.ofSeconds(25));

				if (fileExists) {
					logger.info("File exists!");
				} else {
					logger.info("File does not exist within the timeout period.");

				}
				if (fileExists) {
					flag = false;
				}

			} catch (Exception e) {
				e.printStackTrace();
				util.closePopup(page);
			}
		}
		
		importfile.importFile(filePath, vendorId);
		
	
	}


	

}
