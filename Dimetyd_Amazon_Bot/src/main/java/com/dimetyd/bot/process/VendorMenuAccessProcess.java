package com.dimetyd.bot.process;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Page;

@Component
public class VendorMenuAccessProcess {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void navigateAllPage(Page page, String vendorId) throws IOException {
		StringBuilder missingPermissions = new StringBuilder();
		StringBuilder foundPermissions = new StringBuilder();

		page.click("//div[@aria-label='Navigation menu']");
		page.waitForTimeout(3000);

		int payment = checkTab(page, "//span[text()='Payments']", "payment", missingPermissions, foundPermissions);
		int financialDashboard = checkTab(page,
				"//span[@class='flyout-menu-item-label' and contains(text(), 'Financial dashboard')]",
				"FinancialDashboard", missingPermissions, foundPermissions);
		int remittance = checkTab(page, "//span[@class='flyout-menu-item-label' and contains(text(), 'Remittance')]",
				"Remittance", missingPermissions, foundPermissions);
		int disputeManagement = checkTab(page,
				"//span[@class='flyout-menu-item-label' and contains(text(), 'Dispute Management')]",
				"DisputeManagement", missingPermissions, foundPermissions);
		int financialScorecard = checkTab(page,
				"//span[@class='flyout-menu-item-label' and contains(text(), 'Financial Scorecard')]",
				"FinancialScorecard", missingPermissions, foundPermissions);
		int merchandising = checkTab(page, "//span[@class='side-nav-tab-label' and contains(text(), 'Merchandising')]",
				"Merchandising", missingPermissions, foundPermissions);
		int promotions = checkTab(page, "//span[@class='flyout-menu-item-label' and contains(text(), 'Promotions')]",
				"Promotions", missingPermissions, foundPermissions);

		// ✅ Updated XPath to handle both "Subscribe and Save" and "Subscribe & Save"
		int subscribeAndSave = checkTab(page, "//span[contains(@class,'flyout-menu-item-label') and "
				+ "contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ&','abcdefghijklmnopqrstuvwxyzand'), 'subscribe') and "
				+ "contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ&','abcdefghijklmnopqrstuvwxyzand'), 'save')]",
				"SubscribeandSave", missingPermissions, foundPermissions);

		int shipments = checkTab(page, "//span[@class='flyout-menu-item-label' and contains(text(), 'Shipments')]",
				"Shipment", missingPermissions, foundPermissions);

		// ✅ Updated XPath to handle both "Vendor Initiated Orders" and
		// "Vendor-Initiated orders"
		int vendorInitiatedOrders = checkTab(page,
				"//span[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ-','abcdefghijklmnopqrstuvwxyz '), 'vendor initiated orders')]",
				"Vendorinitiatedorders", missingPermissions, foundPermissions);

		int reports = checkTab(page, "//span[text()='Reports']", "Reports", missingPermissions, foundPermissions);
		int operationalPerformance = checkTab(page, "//a[text()='Operational Performance']", "Operationalperformance",
				missingPermissions, foundPermissions);
		int retailAnalytics = checkTab(page,
				"//span[@class='flyout-menu-item-label' and contains(text(), 'Retail Analytics')]", "RetailAnalytics",
				missingPermissions, foundPermissions);

		if (missingPermissions.length() > 0) {
			missingPermissions.setLength(missingPermissions.length() - 1);
		}

		if (foundPermissions.length() > 0) {
			foundPermissions.setLength(foundPermissions.length() - 1);
		}

		String sqlQuery = "UPDATE `vendorMenuAccess` SET " + "`payments` = '" + payment + "', "
				+ "`financialDashboard` = '" + financialDashboard + "', " + "`remittance` = '" + remittance + "', "
				+ "`disputeManagement` = '" + disputeManagement + "', " + "`financialScorecard` = '"
				+ financialScorecard + "', " + "`merchandising` = '" + merchandising + "', " + "`promotions` = '"
				+ promotions + "', " + "`subscribeAndSave` = '" + subscribeAndSave + "', " + "`shipments` = '"
				+ shipments + "', " + "`vendorInitiatedOrders` = '" + vendorInitiatedOrders + "', " + "`reports` = '"
				+ reports + "', " + "`operationalPerformance` = '" + operationalPerformance + "', "
				+ "`retailAnalytics` = '" + retailAnalytics + "', " + "`updatedOn` = '"
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "', " + "`missingpermissions` = '"
				+ missingPermissions.toString() + "' " + "WHERE `vendorId` = '" + vendorId + "'";

		System.out.println("Executing query: " + sqlQuery);
		jdbcTemplate.update(sqlQuery);
		System.out.println("Database updated successfully for vendorId: " + vendorId);
		System.out.println("Tabs found: " + foundPermissions);
		System.out.println("Tabs missing: " + missingPermissions);
	}

	private int checkTab(Page page, String xpath, String tabName, StringBuilder missingPermissions,
			StringBuilder foundPermissions) {
		if (page.locator(xpath).count() > 0) {
			System.out.println(tabName + " tab is available.");
			foundPermissions.append(tabName).append(",");
			return 1;
		} else {
			System.out.println(tabName + " tab is NOT available.");
			missingPermissions.append(tabName).append(",");
			return 0;
		}
	}
}
