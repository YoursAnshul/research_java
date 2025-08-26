package com.dimetyd.bot.process;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class OperationalChargebackProcess {

	@Autowired
	OperationalChargeback_File_Download_Import ocfileDownlaod;
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	CommonUtil util;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public int OperationalChargebackProcessNavigate(Page page, String id, String vendorId, String vendorName,
			String inStartDate, String inEndDate) {
		
   

		int OCRows = 0;
		String vName = vendorName.substring(0, 4);
	
		logger.info("operational performance naviagtion started");
		

		page.click("//div[@aria-label='Navigation menu']");
		try {
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Reports')]");
		} catch (Exception e) {
			page.reload();
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Reports')]");
		}
		List<ElementHandle> subMenuList = page.querySelectorAll(".flyout-menu-item-label");

		boolean isOperationalTab = false;
		for (ElementHandle subMenu : subMenuList) {
			// Retrieve HTML content
			String htmlContent = subMenu.innerHTML();

			if ("Operational Performance".equals(htmlContent)) {
				// Click the element
				subMenu.click();
				logger.info("Clicked element with Playwright");
				isOperationalTab = true;

				break; // Exit the loop after clicking
			}
		}
		if (isOperationalTab != true) {
			logger.info("Operational Chargeback tab is not found :" + vendorName);
			logger.info(
					"UPDATE CBRequestOperationalChargeBack SET COMMENT='Operational Tab Missing' WHERE id='" + id + "'");
			jdbcTemplate.execute(
					"UPDATE CBRequestOperationalChargeBack SET COMMENT='Operational Tab Missing' WHERE id='" + id + "'");

			return OCRows;
		} else {

			boolean isVisible = page.isVisible("(//a[@id='drilldown-page'])[1]");
			if (isVisible) {
				logger.info("selector is visible now");

			} else {
				logger.info("Selector not visible Refresh Page...........");
				page.reload();
				page.waitForTimeout(1000);

			}

			page.click("(//a[@id='drilldown-page'])[1]");
			page.waitForTimeout(1000);
			
			logger.info("issue Type");
			logger.info("StartDate : " + inStartDate);
			logger.info("EndDate : " + inEndDate);
			DateTimeFormatter sourceFormatter;
			DateTimeFormatter targetFormatter;
			if (vName.contains("US -") || vName.contains("CA -")) {
				sourceFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				targetFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

			} else {
				sourceFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				targetFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			}

			// Convert and reformat dates
			LocalDate startDate = LocalDate.parse(inStartDate, sourceFormatter);
			LocalDate endDate = LocalDate.parse(inEndDate, sourceFormatter);

			String formattedStartDate = startDate.format(targetFormatter);
			String formattedEndDate = endDate.format(targetFormatter);

			page.waitForTimeout(1000);

			// Output formatted dates
			logger.info("Formatted Start Date: " + formattedStartDate);
			logger.info("Formatted End Date: " + formattedEndDate);
			Locator fromDate = page.locator("#fromDateId");
			fromDate.fill(formattedStartDate);
			util.reminemepopup(page);
			page.click("#refineBy");
			Locator toenddate = page.locator("#toDateId");
			toenddate.fill(formattedEndDate);
			util.reminemepopup(page);
			page.click("#refineBy");
			
			Locator isIssueType = page.locator("//b[text()='Issue types']");

			if (isIssueType.count() > 0) {
				page.click("//b[text()='Issue types']");
			
			} else {
				logger.info(
						"UPDATE CBRequestOperationalChargeBack SET COMMENT='Issue Types Missing' WHERE id='" + id + "'");
				jdbcTemplate.execute(
						"UPDATE CBRequestOperationalChargeBack SET COMMENT='Issue Types Missing' WHERE id='" + id + "'");
				return OCRows;
			}
			
			Locator checkboxes = page.locator(
					"//div[@class='a-expander-content a-expander-inline-content a-expander-inner a-expander-content-expanded']//i[@class='a-icon a-icon-checkbox']");
			int count = checkboxes.count();
			logger.info("Check Box Count is" + checkboxes.count());
			for (int i = 0; i < count; i++) {

				checkboxes.nth(i).waitFor();
				checkboxes.nth(i).check();
				logger.info("Clicked checkbox " + (i + 1));

			}
			

			
			page.waitForTimeout(1000);
			Locator feedbackPopup=page.locator("//h3[@id='vibes-modal-title']");
			if(feedbackPopup.count()>0)
			{
				page.click("//body//div[@class='a-section']");
			}
			String LineItemsString = null;
			while (true) {
				Locator isExportFileDownloadPage = page.locator("//h1[@id='chargebackListHeader']");
				if (isExportFileDownloadPage.count() > 0) {
					LineItemsString = ocfileDownlaod.File_Download_Import(page, id, vendorId, inStartDate, inEndDate,
							vendorName);
					break;
				} else {
					logger.info("Page Reloaded");
					page.waitForTimeout(2000);
				}
			}
			Pattern pattern = Pattern.compile("of (\\d+) total transactions");
			Matcher matcher = pattern.matcher(LineItemsString);

			if (matcher.find()) {
				OCRows = Integer.parseInt(matcher.group(1));
				logger.info("Extracted total (int): " + OCRows);
			} else {
				logger.info("No match found. " + OCRows);
			}
			return OCRows;
		}
	}


	
	
}

