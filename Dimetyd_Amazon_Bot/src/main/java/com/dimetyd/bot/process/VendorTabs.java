package com.dimetyd.bot.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Page;

@Component
public class VendorTabs {
	private Logger logger = LoggerFactory.getLogger(getClass());

	public void checkAllTabs(Page page, JdbcTemplate jdbctemp, String newVendorId) {
		String MissingPermissions = "";
		page.locator("//div[@aria-label='Navigation menu']").click();
		logger.info("Clicked Navigation Menu");
		page.waitForTimeout(1000);

		boolean isVisible = page.isVisible("//div[@class='side-nav-tab']/span[text()='Orders']");
		int OrdersTab = 0;
		int purchaseOrderTab = 0;
		int ShipmentTab = 0;
		int vendorInitiateOrderTab = 0;
		if (isVisible == true) {
			logger.info("Orders Tab Found");
			OrdersTab = 1;
			page.locator("//div[@class='side-nav-tab']/span[text()='Orders']").click();
			page.waitForTimeout(1000);
			boolean isPurchaseOrderVisible = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Purchase Orders']");
			if (isPurchaseOrderVisible == true) {
				purchaseOrderTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Puchase Orders,";
			}
			boolean isShipmentVisible = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[contains(text(),'Shipments')]");
			if (isShipmentVisible == true) {
				ShipmentTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Shipments,";
			}
			boolean isVendoInitiatedVisible = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Vendor-initiated orders']");
			if (isVendoInitiatedVisible == true) {
				vendorInitiateOrderTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Vendor Initiated Orders,";
			}
		}
		logger.info(OrdersTab + " " + purchaseOrderTab + " " + ShipmentTab + " " + vendorInitiateOrderTab);

		boolean PayementbuttonVisible = page.isVisible("//div[@class='side-nav-tab']/span[text()='Payments']");
		int PaymentTab = 0;
		int FinancialDashboardTab = 0;
		int CoopTab = 0;
		int InvoiceTab = 0;
		int RemmittanceTab = 0;
		int DisputeManagementTab = 0;
		int financialScoreCardTab = 0;
		if (PayementbuttonVisible == true) {
			PaymentTab = 1;
			logger.info("Payment Tab Found");
			page.locator("//div[@class='side-nav-tab']/span[text()='Payments']").click();
			page.waitForTimeout(1000);
			boolean isFinancialDashboardTab = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Financial dashboard']");
			if (isFinancialDashboardTab) {
				FinancialDashboardTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Financial Dashboard,";
			}
			boolean isCoopTab = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='CoOp']");
			if (isCoopTab) {
				CoopTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"CoOp,";
			}
			boolean isInvoicesTab = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Invoices']");
			if (isInvoicesTab) {
				InvoiceTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Invoices,";
			}
			boolean isRemittanceTab = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Remittance']");
			if (isRemittanceTab) {
				RemmittanceTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Remmittance,";
			}
			boolean isDisputeManagementTab = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Dispute Management']");
			if (isDisputeManagementTab) {
				DisputeManagementTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Dispute Management,";
			}
			boolean isFinancialeScoreCardTab = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Financial Scorecard']");
			if (isFinancialeScoreCardTab) {
				financialScoreCardTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Financial Scorecard,";
			}
		}
		logger.info(FinancialDashboardTab + " " + CoopTab + " " + InvoiceTab + " " + RemmittanceTab + " "
				+ DisputeManagementTab + " " + financialScoreCardTab);

		boolean isMerchandisingVisible = page.isVisible("//div[@class='side-nav-tab']/span[text()='Merchandising']");
		int MerchandisingTab = 0;
		int CouponsTab = 0;
		int PromotionsTab = 0;
		int SubscribeSaveTab = 0;
		if (isMerchandisingVisible == true) {
			logger.info("Merchandising Tab Found");
			MerchandisingTab = 1;
			page.locator("//div[@class='side-nav-tab']/span[text()='Merchandising']").click();
			page.waitForTimeout(1000);
			boolean isCouponsVisible = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Coupons']");
			if (isCouponsVisible == true) {
				CouponsTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Coupons,";
			}
			boolean isPromotionVisible = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Promotions']");
			if (isPromotionVisible == true) {
				PromotionsTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Promotions,";
			}
			boolean isSubscribeVisible = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Subscribe & Save']");
			if (isSubscribeVisible == true) {
				SubscribeSaveTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Subscribe & Save,";
			}
		}
		logger.info(MerchandisingTab + " " + CouponsTab + " " + PromotionsTab + " " + SubscribeSaveTab);

		boolean isReportVisible = page.isVisible("//div[@class='side-nav-tab']/span[text()='Reports']");
		int ReportTab = 0;
		int OperationalPerformanceTab = 0;
		int RetailAnalyticsTab = 0;

		if (isReportVisible) {
			logger.info("Report Tab Found");
			ReportTab = 1;
			page.locator("//div[@class='side-nav-tab']/span[text()='Reports']").click();
			page.waitForTimeout(1000);
			boolean isOpertionalPerformanceVisible = page.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Operational Performance']");
			if (isOpertionalPerformanceVisible) {
				OperationalPerformanceTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Operational Performance,";
			}
			boolean isRetailAnalyticsVisible = page
					.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Retail Analytics']");
			if (isRetailAnalyticsVisible) {
				RetailAnalyticsTab = 1;
			}
			else
			{
				MissingPermissions=MissingPermissions+"Retail Analytics,";
			}

		}
		logger.info(ReportTab + " " + OperationalPerformanceTab + " " + RetailAnalyticsTab);

		try
		{
			MissingPermissions=MissingPermissions.substring(0, MissingPermissions.length() -1);
		}
		catch(Exception ex)
		{
			MissingPermissions="";
		}
       String UpdateVendorMenuAccess="UPDATE `vendorMenuAccess` SET `payments` = '"+PaymentTab+"' ,"
       		+ "`financialDashboard`='"+FinancialDashboardTab+"',`coop`='"+CoopTab+"',`invoice`='"+InvoiceTab+"',"
        		+ "`remittance`='"+RemmittanceTab+"',`disputeManagement`='"+DisputeManagementTab+"',"
        		+ "`financialScorecard`='"+financialScoreCardTab+"',`merchandising`='"+MerchandisingTab+"',"
        		+ "`coupons`='"+CouponsTab+"',`promotions`='"+PromotionsTab+"',`subscribeAndSave`='"+SubscribeSaveTab+"',"
        		+ "`Orders`='"+OrdersTab+"',`purchaseOrder`='"+purchaseOrderTab+"',`shipments`='"+ShipmentTab+"',`vendorInitiatedOrders`='"+vendorInitiateOrderTab+"',"
        		+ "`reports`='"+ReportTab+"',`operationalPerformance`='"+OperationalPerformanceTab+"',`retailAnalytics`='"+RetailAnalyticsTab+"',"
        		+ "`updatedOn`=NOW(),`missingpermissions` = '"+MissingPermissions+"' WHERE `vendorId` = '"+newVendorId+"'";
       logger.info(UpdateVendorMenuAccess);
       jdbctemp.execute(UpdateVendorMenuAccess);

	}

}
