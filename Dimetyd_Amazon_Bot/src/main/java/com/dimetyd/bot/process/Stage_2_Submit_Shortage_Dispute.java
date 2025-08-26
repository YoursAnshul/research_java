package com.dimetyd.bot.process;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class Stage_2_Submit_Shortage_Dispute {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	CommonUtil commonobj;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public String SubmitDispute(Page page, String invoiceNumber, String VendorId, String qtyVarianceAmount,
			String invoiceAmount, String invoiceDate, String paymentDueDate, String payee) {

		// Navigate to Dispute page
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
			page.locator("//div[@class='side-nav-tab']/span[normalize-space(text())='Payments']")
					.click(new Locator.ClickOptions().setTimeout(6000));

			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		try {
			page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute Management Remove"))
					.click(new Locator.ClickOptions().setTimeout(6000));
			;
		} catch (Exception e) {
			page.locator("//span[text()='Dispute Management']").click(new Locator.ClickOptions().setTimeout(6000));
			;
		}

//		Locator loader = page.locator(".a-popover-loading").first();
//		loader.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
//
//		// Start process to creating dispute
//		boolean isPopupFound = true;
//		do {
//			page.waitForTimeout(2000);
//			isPopupFound = page.isVisible("//div[@class='melodic-loading-overlay' and @style='display: block;']");
//
//		} while (isPopupFound);
		
		page.waitForTimeout(4000);
		page.evaluate("() => {" + "const loader = document.querySelector('.melodic-loading-overlay');"
				+ "if (loader) loader.remove();" + "}");
		logger.info("POP UP END");

		Locator button = page.locator("text=Create new dispute");

		// Wait for the button to be visible and enabled before clicking
		button.waitFor(new Locator.WaitForOptions().setTimeout(5000));
		// Ensure the button is visible (you can also use waitForSelector if needed)
		button.isVisible();
		// Click the button once it's visible
		button.click();
		// page.getByLabel("Create new dispute").locator("span").filter(new
		// Locator.FilterOptions().setHasText("Shortage invoice")).nth(3).click();
		// page.getByLabel("Shortage invoice").getByText("Shortage invoice").click();

		page.waitForTimeout(1000);
		page.getByPlaceholder("If you want to dispute").click();
		page.getByPlaceholder("If you want to dispute").fill(invoiceNumber);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("+")).click();
		// page.getByPlaceholder("If you want to dispute").click();

		try {
			Locator payeeCodeInsertLabel = page.locator("//input[@id='payee-code-input']");
			if (payeeCodeInsertLabel.count() > 1) {

				payeeCodeInsertLabel.fill(payee);
				page.waitForTimeout(500); // wait 0.5 seconds if needed
				payeeCodeInsertLabel.press("Enter");
				page.click("//span[@id='payee-code-submit']");
			}
		} catch (Exception ex) {

			logger.info("Payee Code Dialogue box not Found...");
			// logger.info(ex.toString());
		}
		page.waitForTimeout(2000);

		page.waitForTimeout(2000);
		boolean isInvoiceError = page.getByText("These invoices are not").isVisible()
				|| page.getByText("These invoices are not available for dispute:").isVisible();

		if (isInvoiceError) {
			logger.info("Invoice number is not avaiable to create dispute..");

			page.locator("//button[@id='create-new-dispute-cancel-button-announce']").click();
			return "Not Disputed";
		}

		else {

			String disputedAmount = null;

			page.getByLabel("Next").click();
			page.locator("a").filter(new Locator.FilterOptions().setHasText("Select all")).click();
			page.waitForTimeout(3000);
			disputedAmount = page.locator("//div[starts-with(@id, 'line-dispute-amount')]/div[2]").innerText();

			String[] AmountAndCurrency = extractCurrencyAndAmount(disputedAmount);

			System.out.println(Arrays.toString(AmountAndCurrency));

			String Currency = AmountAndCurrency[0];
			disputedAmount = AmountAndCurrency[1];

			logger.info(disputedAmount);

			int counter = 0;

			String getPO;
			String getASIN;
			String getExternalId;
			String getshortageQty;
			String getShortageAmt;
			String getDisputeQty;
			String getDisputeAmount;
			String getInvoicedQty;
			String getInvoicedAmt;
			String getunitCost;

			while (true) {

				try {

					Locator locator = page.locator("//td[@data-column='poNumber']//span//kat-link");
					locator.nth(counter).waitFor(new Locator.WaitForOptions().setTimeout(4000));

					getPO = page.locator("//td[@data-column='poNumber']//span//kat-link").nth(counter)
							.getAttribute("label");
					getASIN = page.locator("//td[@data-column='asin']").nth(counter).innerText();
					getExternalId = page.locator("//td[@data-column='externalId']").nth(counter).innerText();
					getshortageQty = page.locator("//td[@data-column='shortageQuantity']").nth(counter).innerText();
					getShortageAmt = page.locator("//td[@data-column='shortageAmount']").nth(counter).innerText();
					getDisputeQty = page.locator("//td[@data-column='disputeQuantity']//span//kat-input").nth(counter)
							.getAttribute("value");
					getDisputeAmount = page.locator("//td[@data-column='disputeAmount']//span//kat-input").nth(counter)
							.getAttribute("value");
					getInvoicedQty = page.locator("//td[@data-column='invoiceQuantity']").nth(counter).innerText();
					getInvoicedAmt = page.locator("//td[@data-column='invoiceAmount']").nth(counter).innerText();
					getunitCost = page.locator("//td[@data-column='unitPrice']").nth(counter).innerText();

					counter = counter + 1;

					logger.info("PO: " + getPO + " ASIN: " + getASIN + " Shortage Qty: " + getshortageQty
							+ "getDisputeQty: " + getDisputeQty + " getDisputeAmount: " + getDisputeAmount);

					StringBuilder sql = new StringBuilder();
					sql.append(
							"INSERT IGNORE INTO `CBItemizedShortages` (`vendorId`,`PO`,`asin`,`shortageQty`,`disputedQty`,`perUnit`,`shortageAmount`,`disputeAmount`,`invoiceNumber`,`createdDate`,`invoiceDate`,`dueDate`,`payeeCode`,`invoicedQuantity`,`invoicedAmount`,externalId,`ukey`)VALUES('"
									+ VendorId + "','" + getPO + "','" + getASIN + "','" + getshortageQty + "','"
									+ getDisputeQty + "','" + getunitCost.replace("$", "").replace(",", "").trim()
									+ "','" + getShortageAmt.replace("$", "").replace(",", "").trim() + "','"
									+ getDisputeAmount.replace("$", "").replace(",", "").trim() + "','" + invoiceNumber
									+ "',now(),STR_TO_DATE('" + invoiceDate + "','%m/%d/%Y'),STR_TO_DATE('"
									+ paymentDueDate + "','%m/%d/%Y')" + ",'" + payee + "','" + getInvoicedQty + "','"
									+ getInvoicedAmt.replace("$", "").replace(",", "").trim() + "','" + getExternalId
									+ "','" + getPO + getASIN + invoiceNumber + VendorId + "')");

					logger.info(sql.toString());
					jdbcTemplate.execute(sql.toString());

				} catch (Exception e) {
					break;
				}

			}

			Locator confirmationText = page.locator("//div[@id='prepaid-invoice-warning-banner']");
			if (confirmationText.isVisible()) {
				System.out.println("Confirmation found. Updating database...");

				// Step 3: Update the database
				jdbcTemplate.execute(
						"UPDATE `CBitemizedshortageInvoicesToBeCreated` SET `comments`='Your invoice has been fully paid through the following payment numbers' "
								+ "WHERE `invoiceNumber`='" + invoiceNumber + "' AND `vendorId`='" + VendorId + "'");
				return "Not Disputed";
			} else {
				System.out.println("Confirmation not found. No update done.");
			}

			logger.info("Inserted Itemized Shortage Data-------> INSERTED");
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).click();
			page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Provide justification for the"))
					.click();
			page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Provide justification for the"))
					.fill("this items are shipped");
			page.waitForTimeout(1000);
			page.locator("#root").click();
			logger.info("Provided Justification while creating dispute...");
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).click();
			// page.getByRole(AriaRole.BUTTON, new
			// Page.GetByRoleOptions().setName("Submit")).isVisible();
			page.waitForTimeout(1000);

			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();

			page.waitForTimeout(1000);
			page.locator("//kat-button[@id='submit-dispute-modal-submit-button']").click();
			logger.info("CREATED DISPUTE");

			page.waitForTimeout(2000);
			String SubmittedDisputeIdString = null;
			boolean isDisputeSubmitted = true;
			do {
				isDisputeSubmitted = page.isVisible("//div[@id='submit-dispute-submission-successful']");
				if (isDisputeSubmitted) {
					page.waitForTimeout(3000);
					SubmittedDisputeIdString = page
							.locator("//*[@id='submit-dispute-submission-successful']/div/div[2]/kat-label")
							.innerText();
					isDisputeSubmitted = false;
				}

			} while (isDisputeSubmitted);

			if (SubmittedDisputeIdString != null) {
				SubmittedDisputeIdString = extractDisputeId(SubmittedDisputeIdString, Currency);
			}

			logger.info("----------------------------------------");
			logger.info("##### " + SubmittedDisputeIdString + " #####");
			logger.info("----------------------------------------");

			disputedAmount = commonobj.processAmount(disputedAmount);
			String InsertDisputeRequestQuery = "INSERT INTO `CBClientDispute` (`disputeId`,`vendorId`,`type`,`disputeDate`,`requestStatus`,`disputeAmount`,"
					+ "`createdDate`,`currency`,`status`,`createdBy`) VALUES ('" + SubmittedDisputeIdString.trim()
					+ "','" + VendorId + "','Shortage',NOW(),'PENDING'," + "'" + disputedAmount.replace("$", "")
							.replace(",", "").replace("€", "").replace("Dispute amount : ", "").trim()
					+ "',NOW(),'" + Currency + "','ACTIVE','79')";

			logger.info(InsertDisputeRequestQuery);
			jdbcTemplate.execute(InsertDisputeRequestQuery);

			jdbcTemplate.execute("UPDATE `CBItemizedShortages` SET `disputeId` = '" + SubmittedDisputeIdString.trim()
					+ "' " + "WHERE `vendorId` = '" + VendorId + "' AND `invoiceNumber` = '" + invoiceNumber + "'");

			jdbcTemplate.execute(
					"INSERT Into `CBDisputeCreatedByBot` (`disputeId`,`vendorId`,`disputeType`,`createdDate`,`sentStatus`,`invoiceNumber`) VALUES"
							+ " ('" + SubmittedDisputeIdString.trim() + "','" + VendorId + "','Shortage',now(),'NO','"
							+ invoiceNumber + "')");

			logger.info("DISPUTE STATUS------->DISPUTED");

			try {
				page.waitForSelector("//kat-button[@id='submit-dispute-modal-ok-button']",
						new Page.WaitForSelectorOptions().setTimeout(7000));
				page.locator("//kat-button[@id='submit-dispute-modal-ok-button']").click();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			logger.info("Popup closed");

			return "Disputed";

		}

	}

	public static String extractDisputeId(String text, String currency) {
		// Case-insensitive pattern, handles optional space and colon
		//String regex = "(?i)Dispute\\s*Id\\s*:?\\s*(DSPT\\d+)";
		String regex = "(?i)(?:Re-)?Dispute\\s*Id\\s*:?\\s*(DSPT\\d+)";

		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
		java.util.regex.Matcher matcher = pattern.matcher(text);

		if (matcher.find()) {
			return matcher.group(1); // Return the first captured group (Dispute ID)
		}
		return null; // Return null if no match found
	}

	public static String[] extractCurrencyAndAmount(String input) {
		// Currency symbol to 3-character code mapping
		input = input.replace("\u00A0", " ").trim();
		Map<String, String> currencyMap = new HashMap<>();
		currencyMap.put("$", "USD");
		currencyMap.put("€", "EUR");
		currencyMap.put("£", "GBP");
		currencyMap.put("₹", "INR");
		currencyMap.put("¥", "JPY");

		// Updated regex to capture currency appearing before or after the amount
		String regex = "\\s*([€$£₹¥])?\\s*([0-9]{1,3}(?:[.,][0-9]{3})*|[0-9]+)([.,][0-9]{1,2})?\\s*([€$£₹¥])?";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input.trim());

		if (matcher.find()) {
			String currency1 = matcher.group(1); // Currency before amount
			String numberPart = matcher.group(2); // Integer or whole number part
			String decimalPart = matcher.group(3); // Decimal part (e.g., .40 or ,40)
			String currency2 = matcher.group(4); // Currency after amount

			// Determine correct currency symbol
			String symbol = (currency1 != null) ? currency1 : currency2;

			// Convert to ISO code
			String currency = (symbol != null && currencyMap.containsKey(symbol)) ? currencyMap.get(symbol) : "None";

			// Normalize number (remove thousands separator, fix decimal)
			if (decimalPart == null) {
				decimalPart = "";
			} else {
				decimalPart = decimalPart.replace(",", ".");
			}

			numberPart = numberPart.replaceAll("[.,]", ""); // Remove thousands separators
			String amount = decimalPart.isEmpty() ? numberPart : numberPart + decimalPart;

			return new String[] { currency, amount };
		} else {
			return null; // Invalid input format
		}
	}

}
