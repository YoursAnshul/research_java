package com.dimetyd.bot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

@Component

public class SwitchVCAccount {

	@Autowired
	CommonUtil commonobj;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public boolean SwitchAccount(Page page, String vendorName) {

		logger.info("Switch Account flow is started..");

		page.waitForTimeout(1500);

		Locator switchPage = page.locator(
				"//div[contains(@class, 'full-page-account-switcher') or contains(@class, 'switcher')]//h1[text()='Select an account']");

		if (switchPage.count() > 0) {
			try {
				page.click("//*[contains(text(),\"" + vendorName.trim() + "\")]");
				page.click("//kat-button[@class='full-page-account-switcher-button']");
				logger.info("Vendor Name Clicked.");
				commonobj.switchpagenewPopup(page);

				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

		}

		else {

			try {

				int maxAttempts = 100;
				boolean pageLoadedProperly = false;

				for (int attempt = 0; attempt < maxAttempts; attempt++) {
					Locator accountsLoaded = page.locator(
							"//div[@class='full-page-account-switcher-accounts-error full-page-account-switcher-accounts-error-top-level']");
					Locator selectAccountPageLoaded = page
							.locator("//*[@class='utility-bar-button-link']//span[text()='Help']");

					// Check if the accounts error is visible
					boolean isAccountsErrorVisible = accountsLoaded.isVisible();

					if (!isAccountsErrorVisible) {
						logger.info("No accounts error visible, checking for Help button...");

						// Check if the Help button is visible
						if (selectAccountPageLoaded.isVisible()) {
							logger.info("Page loaded properly. Help button is visible.");
							commonobj.switchpagenewPopup(page);
							pageLoadedProperly = true;
							break; // Exit the loop if the page is loaded properly
						} else {
							logger.info("Help button is not visible. Reloading page...");
						}
					} else {
						logger.info("Accounts error is visible, indicating a problem with loading. Reloading page...");
					}

					// Optionally wait for a specific element to ensure the page is fully loaded
					page.waitForLoadState(LoadState.DOMCONTENTLOADED);
				}
				try {
					page.waitForSelector("//div[@class= 'full-page-account-switcher account-switcher-root']//h1",
							new Page.WaitForSelectorOptions().setTimeout(5000));

				} catch (Exception e) {

					page.click("//span[@class='dropdown-account-switcher-header-label-regional']");
					page.waitForTimeout(1500);
					try {
						page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("See all")).click();
					} catch (Exception e1) {
						Locator parentLocator = page.locator("#hmd2f-trigger-tab");

						Locator cancelButtonLocator = parentLocator.locator(".vc-footer-hmd-feedback-link");

						cancelButtonLocator.click();
						page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("See all")).click();
					}
				}

				while (true) {
					page.waitForTimeout(1500);
					Locator selectAccountPageLoaded = page.locator("//h1[text()='Select an account']");
					if (selectAccountPageLoaded.count() > 0) {
						break;
					} else {
						page.reload();
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				page.click("//*[contains(text(),\"" + vendorName.trim() + "\")]");
				page.click("//kat-button[@class='full-page-account-switcher-button']");

				logger.info("Vendor Name Clicked after select account.");
				page.waitForTimeout(1500);
		
				commonobj.switchpagenewPopup(page);
				return true;

			} catch (Exception e) {
				// TODO: handle exception

				return false;
			}

		}

	}

	public void switchNow(Page page, String vendorName) {

		boolean isVendorList = false;

		page.waitForTimeout(3000);
		isVendorList = page.isVisible("//*[@class='full-page-account-switcher-accounts']");

		if (isVendorList) {
			if (vendorName == null) {
				logger.info("Vendor List Found, and Now Checking for New vendors..........");
			} else {
				try {
					logger.info("Vendor List Found Vendor Switching...");

					page.locator(
							"//button[starts-with(@class, 'full-page-account-switcher-account-details')]/span[text()='"
									+ vendorName + "']")
							.click();
					page.click("//kat-button[@class='full-page-account-switcher-button']");
				} catch (Exception ex) {
					logger.info("Account not found in List : " + vendorName);
					logger.info(ex.toString());
				}
			}

		} else {
			logger.info("Vendor List not found");
			page.navigate("https://vendorcentral.amazon.com/account-switcher/regional/vendorGroup");
			logger.info("Now Vendor List Found");
		}

	}

}
