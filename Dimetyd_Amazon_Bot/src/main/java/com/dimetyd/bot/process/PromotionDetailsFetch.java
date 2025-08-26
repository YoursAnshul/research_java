package com.dimetyd.bot.process;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class PromotionDetailsFetch {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	CommonUtil commonobj;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public boolean getPromotionDetails(Page page, Long requestId, String startDate, String endDate, String vendorId,
			String vendorName, Browser browser) {
		String vName = vendorName.substring(0, 4);

		logger.info("vName: " + vName);
		try {
			logger.info("Navigating Promotion Page");
			page.click("//div[@aria-label='Navigation menu']");
			try {
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Merchandising')]",
						new Page.ClickOptions().setTimeout(3000));
			} catch (Exception e) {
				page.reload();
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Merchandising')]",
						new Page.ClickOptions().setTimeout(3000));
				return true;
			}

			try {
				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Promotions')]",
						new Page.ClickOptions().setTimeout(3000));
			} catch (Exception ex) {
				logger.info("Promotion tab missing..............................");
				return true;
			}

			page.locator("//span[@id='promotionStatusFilterDropdown']").click();
			page.waitForTimeout(1000);
			try {

				page.locator("//li[@class='a-dropdown-item']/a[contains(text(),'Canceled')]");

				page.waitForSelector("//li[@class='a-dropdown-item']/a[contains(text(),'Canceled')]",
						new Page.WaitForSelectorOptions().setTimeout(5000));

				page.locator("//li[@class='a-dropdown-item']/a[contains(text(),'Canceled')]").click();

			} catch (Exception e) {

				page.locator("//li[@class='a-dropdown-item']/a[contains(text(),'Cancelled')]").click();
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			page.waitForTimeout(1000);

			LoadingPage(page);

			if (vName.equals("US -")) {
				logger.info("Changing Date format to US");
				String us_formattedStartDate = convertDateFormat_US(startDate, vendorName);
				String us_formattedEndDate = convertDateFormat_US(endDate, vendorName);
				logger.info("Formatted start date: " + us_formattedStartDate); // Output: Date in dd/MM/yyyy
				logger.info("Formatted end date: " + us_formattedEndDate); // Output: Date in dd/MM/yyyy

				page.locator("//input[@id='startDateAfter']").fill(us_formattedStartDate);
				page.locator("//div[@id='sc-content-container']").click();
				LoadingPage(page);
				page.locator("//input[@id='startDateBefore']").fill(us_formattedEndDate);

				/*
				 * page.waitForTimeout(1000);
				 * page.locator("//input[@id='endDateAfter']").fill(us_formattedEndDate);
				 * page.locator("//input[@id='endDateBefore']").fill(us_formattedEndDate);
				 */

			} else {
				logger.info("Changing Date format for Other Countries");
				String other_formattedStartDate = convertDateFormat_Other(startDate);
				String other_formattedEndDate = convertDateFormat_Other(endDate);
				logger.info("Formatted start date: " + other_formattedStartDate); // Output: Date in MM/dd/yyyy
				logger.info("Formatted end date: " + other_formattedEndDate); // Output: Date in MM/dd/yyyy

				page.locator("//input[@id='startDateAfter']").fill(other_formattedStartDate);
				page.locator("//div[@id='sc-content-container']").click();
				LoadingPage(page);
				page.locator("//input[@id='startDateBefore']").fill(other_formattedEndDate);

				/*
				 * page.waitForTimeout(1000);
				 * page.locator("//input[@id='endDateAfter']").fill(other_formattedEndDate);
				 * page.locator("//input[@id='endDateBefore']").fill(other_formattedEndDate);
				 */

			}

			page.locator("//div[@id='sc-content-container']").click();
			LoadingPage(page);
			// page.waitForLoadState(LoadState.NETWORKIDLE);
			boolean isPromotionListFound = page
					.isVisible("//table[@class='a-bordered a-horizontal-stripes mt-table']/tbody/tr[2]");
			if (isPromotionListFound) {
				int RowCount = 1;
				logger.info("Promotion List Found");
				do {
					String PromotionId = null;
					RowCount = RowCount + 1;
					// PAGIGNATION FLOW
					if (RowCount == 12) {
						logger.info("Clicking on next page");
						try {
							page.click("//div[@id='promotion-list-pagination']//a[text()='next']");
							RowCount = 2;
							logger.info("Clicked on next Page");
						} catch (Exception ex) {
							logger.info("Promotion List ENDED......");
						}
					}

					// Data FETCHING

					// PROMOTION ID
					try {

						Locator PromotionData = page
								.locator("//table[@class='a-bordered a-horizontal-stripes mt-table']/tbody/tr["
										+ RowCount + "]");
						logger.info("//table[@class='a-bordered a-horizontal-stripes mt-table']/tbody/tr[" + RowCount
								+ "]");
						PromotionId = PromotionData.getAttribute("id");
						PromotionData.waitFor(new Locator.WaitForOptions().setTimeout(1000));
					} catch (Exception ex) {
						logger.info("Promotion List End");
						break;
					}
					PromotionId = PromotionId.trim();
					logger.info("Promotion Id :" + PromotionId);

					// PROMOTION STATUS
					String PromotionStatus = page.locator("//span[@id='" + PromotionId + "-status-text']").innerText();
					logger.info("Status : " + PromotionStatus);

					// REVENUE
					String Revenue = "0.0";
					String Currency = null;
					try {
						Revenue = page.locator("//span[@id='" + PromotionId + "-metrics-revenue-value']")
								.innerText(new Locator.InnerTextOptions().setTimeout(3000));
						Currency = commonobj.getCurrency(Revenue);
						Revenue = commonobj.processAmount(Revenue);

						logger.info("Revenue : " + Revenue);
					} catch (Exception ex) {
						logger.info("Revenue not found");
						logger.info("Revenue : " + Revenue);
					}

					// START DATE AND END DATE FETCH
					String PromoStartDate = null;
					try {
						PromoStartDate = page.locator("//span[@id='" + PromotionId + "-start-date-date']")
								.innerText(new Locator.InnerTextOptions().setTimeout(500));
					} catch (Exception ex) {
						logger.info("Start Date not found");
					}
					String PromoEndDate = null;
					try {
						PromoEndDate = page.locator("//span[@id='" + PromotionId + "-end-date-date']")
								.innerText(new Locator.InnerTextOptions().setTimeout(500));
					} catch (Exception ex) {
						logger.info("End Date not Found");
					}

					// STORES ONLY CONCELED DETAILS DATA

					if (PromotionStatus.equals("Canceled")) {
						String InsertPromotionData = "INSERT IGNORE INTO `CBPromotionCheck` (`vendorId`,`requestId`,`promotionId`,`revenueGenerated`,`promotionStatus`,`startDate`,`endDate`,`createddate`,`currency`,`Ukey`,`status`)\r\n"
								+ "VALUES ('" + vendorId + "','" + requestId + "','" + PromotionId + "','" + Revenue
								+ "','" + PromotionStatus + "','" + PromoStartDate + "','" + PromoEndDate + "',NOW(),'"
								+ Currency + "','" + vendorId + PromotionId + "','PENDING')";

						logger.info(InsertPromotionData);
						jdbcTemplate.execute(InsertPromotionData);
					}

				} while (true);

			} else {
				logger.info("Promotion List not Found........");
				return true;
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public void LoadingPage(Page page) {
		do {
			page.waitForTimeout(2000);
			boolean isLoader = page.isVisible("//div[@id='promotion-list']/div[@style='display: block;']");
			if (isLoader) {
				logger.info("Loader...");
			} else {
				logger.info("Page Loader END");
				break;

			}
		} while (true);
	}

	public String convertDateFormat_US(String CurrentDate, String vendorName) {

		if (vendorName.startsWith("DE -")) {
			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			// Parse the input string to a LocalDate
			LocalDate date = LocalDate.parse(CurrentDate, inputFormatter);

			DateTimeFormatter outputStartDate_US = DateTimeFormatter.ofPattern("dd.MM.yyyy");

			// Format the date to the desired output format
			return date.format(outputStartDate_US);
		} else {
			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			// Parse the input string to a LocalDate
			LocalDate date = LocalDate.parse(CurrentDate, inputFormatter);

			DateTimeFormatter outputStartDate_US = DateTimeFormatter.ofPattern("MM/dd/yyyy");

			// Format the date to the desired output format
			return date.format(outputStartDate_US);
		}

	}

	public String convertDateFormat_Other(String CurrentDate) {
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDate date = LocalDate.parse(CurrentDate, inputFormatter);
		DateTimeFormatter outputEndDate_Other = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		return date.format(outputEndDate_Other);
	}

}
