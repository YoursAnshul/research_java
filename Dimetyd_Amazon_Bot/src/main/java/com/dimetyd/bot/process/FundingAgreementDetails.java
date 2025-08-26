package com.dimetyd.bot.process;

import java.util.List;

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

@Component
public class FundingAgreementDetails {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	CommonUtil commonobj;

	public boolean getFundingAgreementData(Page page, String vendorId, String vendorName, String promotionId) {

		String vName = vendorName.substring(0, 4);
		try
		{

		logger.info("vName: " + vName);

		logger.info("Navigating Promotion Page for agreement Funding");
		page.click("//div[@aria-label='Navigation menu']");
		try {
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Merchandising')]");
		} catch (Exception e) {
			page.reload();
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Merchandising')]");
		}

		try {
			List<ElementHandle> subMenuList = page.querySelectorAll(".flyout-menu-item-label");
			if (vendorName.contains("CA -")) {
				for (ElementHandle subMenu : subMenuList) {
					// Retrieve HTML content
					String htmlContent = subMenu.innerHTML();
					if ("Promotions".equals(htmlContent)) {
						// Click the element
						subMenu.click();
						System.out.println("Clicked element with Playwright");
						break; // Exit the loop after clicking
					}
				}
			} else {
				for (ElementHandle subMenu : subMenuList) {
					// Retrieve HTML content
					String htmlContent = subMenu.innerHTML();
					if ("Promotions".equals(htmlContent)) {
						// Click the element
						subMenu.click();
						System.out.println("Clicked element with Playwright");
						break; // Exit the loop after clicking
					}
				}
			}

		} catch (Exception e) {
			if (vendorName.contains("US -")) {
				logger.info("Vendor Name Contains - US");
				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Promotions')]");
			}
			if (vendorName.contains("CA -")) {
				logger.info("Vendor Name Contains - CA");

				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Promotions')]");
			} else {
				logger.info("Not get Vendor Country, is Using US Coop Click...");
				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Promotions')]");
			}
		}

		page.waitForLoadState(LoadState.NETWORKIDLE);

		Locator PromotionPageLocator = page.locator("//textarea[@id='promotion-list-search']");
		if (PromotionPageLocator.count() > 0) {
			PromotionPageLocator.fill(promotionId);
			page.waitForTimeout(1000);
		}
		page.locator("//input[@id='promotion-list-search-button-inner']").click();
		LoadingPage(page);

		page.locator("//*[@id='" + promotionId + "-promotionId']//a[contains(text(),'Promotion details')]").click();

		String FundingAgreement = page.locator("//div[@id='promotion-detail-page']//table//a[@class='a-link-normal']")
				.innerText().toString().trim();
		logger.info("**************FUNDING AGREEMENT : " + FundingAgreement + "****************");
		String UpdateFundingAgreementId = "UPDATE `CBPromotionCheck` SET `fundingAgreement`='" + FundingAgreement
				+ "' WHERE `vendorId`='" + vendorId + "' AND `promotionId`='" + promotionId + "'";
		logger.info(UpdateFundingAgreementId);
		jdbcTemplate.execute(UpdateFundingAgreementId);

		logger.info("Navigating Coop Page for Fetching Rebate");
		page.click("//div[@aria-label='Navigation menu']");
		try {
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
		} catch (Exception e) {
			page.reload();
			page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
		}

		try {
			List<ElementHandle> subMenuList = page.querySelectorAll(".flyout-menu-item-label");
			if (vendorName.contains("CA -")) {
				for (ElementHandle subMenu : subMenuList) {
					// Retrieve HTML content
					String htmlContent = subMenu.innerHTML();
					if ("Co-op".equals(htmlContent)) {
						// Click the element
						subMenu.click();
						System.out.println("Clicked element with Playwright");
						break; // Exit the loop after clicking
					}
				}
			} else {
				for (ElementHandle subMenu : subMenuList) {
					// Retrieve HTML content
					String htmlContent = subMenu.innerHTML();
					if ("CoOp".equals(htmlContent)) {
						// Click the element
						subMenu.click();
						System.out.println("Clicked element with Playwright");
						break; // Exit the loop after clicking
					}
				}
			}

		} catch (Exception e) {
			if (vendorName.contains("US -")) {
				logger.info("Vendor Name Contains - US");
				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'CoOp')]");
			}
			if (vendorName.contains("CA -")) {
				logger.info("Vendor Name Contains - CA");

				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Co-op')]");
			} else {
				logger.info("Not get Vendor Country, is Using US Coop Click...");
				page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'CoOp')]");
			}
		}

		page.waitForLoadState(LoadState.NETWORKIDLE);

		logger.info("Searching for Funding Agreement : " + FundingAgreement);
		Locator SearchLocator = page.locator("//textarea[@id='search-input']");
		if (SearchLocator.count() > 0) {
			SearchLocator.fill(FundingAgreement);
			page.waitForTimeout(1000);
		}
		page.locator("//button[@id='search-button-announce']").click();

		page.waitForTimeout(1000);
		boolean isNoDataFound = page.isVisible("//div[@id='contra-cogs-no-records-section']");
		if (isNoDataFound) {
			logger.info("No data found of this funding agreement");
			String UpdateNoResult = "UPDATE `CBPromotionCheck` SET `comment`='No Result were found' WHERE `vendorId`='"
					+ vendorId + "' AND `promotionId`='" + promotionId + "'";
			logger.info(UpdateNoResult);
			jdbcTemplate.execute(UpdateNoResult);
		} else {
			String RebateAmount = page.locator("//div[@id='total-deductions-summary-container']/div[2]/span")
					.innerText().trim();
			RebateAmount = commonobj.processAmount(RebateAmount);
			String UpdateRebateAmount = "UPDATE `CBPromotionCheck` SET `rebate`='" + RebateAmount
					+ "' WHERE `vendorId`='" + vendorId + "' AND `promotionId`='" + promotionId + "'";
			logger.info(UpdateRebateAmount);
			jdbcTemplate.execute(UpdateRebateAmount);

		}

		return true;
		}catch(Exception ex)
		{
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

}
