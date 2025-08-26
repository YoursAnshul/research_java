package com.dimetyd.bot.process;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class Stage_2_Price_Claim_Submit_Dispute {

	@Autowired
	JdbcTemplate jdbcTemplate;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public String SubmitDispute(Page page, String invoiceNumber, String VendorId, String vendorName,String qtyVarianceAmount,
			String invoiceAmount, String invoiceDate, String paymentDueDate, String payee) {

		// Navigate to Dispute page
		Locator navMenu = page.getByLabel("Navigation menu");
		navMenu.waitFor(new Locator.WaitForOptions().setTimeout(30000));
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
					page.waitForTimeout(1000);
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
			page.getByText("Payments").click(new Locator.ClickOptions().setTimeout(6000));
		} catch (Exception e) {
			page.locator("//div[@class='side-nav-tab']/span[normalize-space(text())='Payments']").click(new Locator.ClickOptions().setTimeout(6000));
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		try{
			page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute Management Remove")).click(new Locator.ClickOptions().setTimeout(6000));
		}catch (Exception e) {
			page.locator("//span[text()='Dispute Management']").click(new Locator.ClickOptions().setTimeout(6000));
		}

		Locator loader = page.locator(".a-popover-loading").first();
		loader.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));

		// Start process to creating dispute
		boolean isPopupFound = true;
		do {
			page.waitForTimeout(2000);
			isPopupFound = page.isVisible("//div[@class='melodic-loading-overlay' and @style='display: block;']");
		} while (isPopupFound);

		Locator button = page.locator("text=Create new dispute");
		// Wait for the button to be visible and enabled before clicking
		button.waitFor(new Locator.WaitForOptions().setTimeout(5000));
		// Ensure the button is visible (you can also use waitForSelector if needed)
		button.isVisible();
		// Click the button once it's visible
		button.click();
		page.waitForTimeout(1000);

		Locator dropdown = page.locator("//select[@name='itemType']");
		dropdown.selectOption(new SelectOption().setIndex(1));

		page.waitForTimeout(1000);
		page.getByPlaceholder("Please enter the invoice number").click();
		page.getByPlaceholder("Please enter the invoice number").fill(invoiceNumber);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("+")).click();
		// page.getByPlaceholder("If you want to dispute").click();

		page.waitForTimeout(2000);
		boolean isInvoiceError = page.getByText("These invoices are not").isVisible();

		if (isInvoiceError) {
			logger.info("Invoice number is not avaiable to create dispute..");
			page.locator("//button[@id='create-new-dispute-cancel-button-announce']").click();
			return "Not Disputed";
		}

		else {

			String disputedAmount = null;

			page.getByLabel("Next").click();
			page.waitForTimeout(1000);
			page.locator("a").filter(new Locator.FilterOptions().setHasText("Select all")).click();
			page.waitForTimeout(1000);

			Locator DisputeAgainstASINLocator = page.locator("//kat-alert[@id='additional-info-box']");
			if (DisputeAgainstASINLocator.count() > 0) {
				page.locator("kat-alert[id='additional-info-box']>>div[class='checkbox']").click();
				page.waitForTimeout(1000);
				page.locator("kat-alert[id='additional-info-box']>>kat-button[id='dpe-continue-button']").click();
			}
			

			disputedAmount = page.locator("//div[starts-with(@id, 'line-dispute-amount')]/div[2]").innerText();

			String[] AmountAndCurrency = extractCurrencyAndAmount(disputedAmount);

			String Currency = AmountAndCurrency[0];
			disputedAmount = AmountAndCurrency[1];

			logger.info(disputedAmount);

			int counter = 0;

			String getPO;
			String getASIN;
			String getQty;
			String getInvoiceCost;
			String getinItialResearchCost;
			String getDisputeAmount;
			while (true) {

				try {

					Locator po = page.locator("//td[@data-column='poNumber']").nth(counter);
					if (po.count() == 0) {
						break;
					}
					getPO = page.locator("//td[@data-column='poNumber']").nth(counter).innerText();
					getASIN = page.locator("//td[@data-column='asin']").nth(counter).innerText();
					getQty = page.locator("//td[@data-column='quantity']").nth(counter).innerText();
					getInvoiceCost = page.locator("//td[@data-column='invoiceCost']").nth(counter).innerText();
					getinItialResearchCost = page.locator("//td[@data-column='initialResearchCost']").nth(counter)
							.innerText();
					getDisputeAmount = page.locator("//td[@data-column='disputeAmount']").nth(counter).innerText();

					counter = counter + 1;

					logger.info("PO: " + getPO + " ASIN: " + getASIN + " getDisputeAmount: " + getDisputeAmount);

					StringBuilder sql = new StringBuilder();
					sql.append("INSERT IGNORE INTO DisputePriceDiscrepancy "
							+ "(`vendorId`,`disputeId`,`invoiceNumber`,`po`,`asin`,`quantity`,`invoiceCost`,`amountPaidCost`,`uniqueKey`,`createdDate`) VALUES('"
							+ VendorId + "','','" + invoiceNumber + "','" + getPO + "','" + getASIN + "','" + getQty
							+ "','" + getInvoiceCost.replace("$", "") + "','" + getDisputeAmount.replace("$", "")
							+ "','" + getPO + getASIN + invoiceNumber + VendorId + "',NOW())");

					logger.info(sql.toString());
					jdbcTemplate.execute(sql.toString());

				} catch (Exception e) {
					break;
				}

			}

			try {

				page.locator("kat-alert[id='additional-info-box']>>kat-button[id='dpe-continue-button']").click();
			} catch (Exception ex) {
				logger.info("Continue not clicked on pop up");
			}

			logger.info("Inserted Price Discrepancy Data-------> INSERTED");
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).click();
			page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Provide justification for the"))
					.click();
			page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Provide justification for the"))
					.fill("Disputing Price Claim Charges as the invoice was billed at the PO cost");
			page.locator("#root").click();
			logger.info("Provided Justification while creating dispute...");
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).click();
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).isVisible();
			page.waitForTimeout(1000);

			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();

			page.waitForTimeout(1000);
			page.locator("//kat-button[@id='submit-dispute-modal-submit-button']").click();
			logger.info("CREATED DISPUTE");

			page.waitForTimeout(2000);
			String SubmittedDisputeIdString = null;
			do {
				String isCompletedLabelFound = page.locator(
						"//*[@id='submit-dispute-submission-successful']/div/div[1]/div[2]/kat-label/span/label")
						.innerText();
				if (isCompletedLabelFound.equals("Submission completed")) {
					SubmittedDisputeIdString = page
							.locator("//*[@id='submit-dispute-submission-successful']/div/div[2]/kat-label")
							.innerText();
					break;
				}

			} while (true);

			if (SubmittedDisputeIdString != null) {
				SubmittedDisputeIdString = extractDisputeId(SubmittedDisputeIdString);
			}

			logger.info("----------------------------------------");
			logger.info("##### " + SubmittedDisputeIdString + " #####");
			logger.info("----------------------------------------");

			String InsertDisputeRequestQuery = "INSERT INTO CBClientDispute "
					+ " (`disputeId`,`vendorId`,`type`,`disputeDate`,`requestStatus`,`disputeAmount`,"
					+ "`createdDate`,`currency`,status) VALUES ('" + SubmittedDisputeIdString.trim() + "','" + VendorId
					+ "','Price Claim',NOW(),'PENDING'," + "'" + disputedAmount.replace("$", "").replace(",", "")
							.replace("€", "").replace("Dispute amount : ", "").trim()
					+ "',NOW(),'" + Currency + "','ACTIVE')";

			logger.info(InsertDisputeRequestQuery);

			jdbcTemplate.execute(InsertDisputeRequestQuery);

			logger.info("DISPUTE STATUS------->DISPUTED");

			page.locator("//kat-button[@id='submit-dispute-modal-ok-button']").click();

			return "Disputed";

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

}
