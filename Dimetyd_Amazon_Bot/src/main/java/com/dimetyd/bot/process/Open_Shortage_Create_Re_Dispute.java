package com.dimetyd.bot.process;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class Open_Shortage_Create_Re_Dispute {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	CommonUtil commonobj;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public String SubmitDispute(Page page, String disputeId, String VendorId, String invoiceAmount, String vendorName,
			String payee, String invoiceNumber, String submitType) {

		// Navigate to Help page

		String URL = getCountryUrl(vendorName);
		logger.info("Country URL : " + URL);

		// Navigate Page to

		page.navigate(URL + "/katalmonsapp/vendor/members/disputes/re/create");

		page.waitForTimeout(1000);

		page.locator("kat-textarea[id='create-re-dispute-validation-step-dispute-id-list']>>textarea").click();

		commonobj.clsoeFeedbackPopup(page);

		page.locator("kat-textarea[id='create-re-dispute-validation-step-dispute-id-list']>>textarea").fill(disputeId);
		page.waitForTimeout(500);

		page.locator("//kat-button[@id='create-re-dispute-validation-step-validate-button']").click();

		page.waitForTimeout(3000);

		boolean isDisputeIdAvailable = page.locator("//kat-statusindicator[@variant='error']").isVisible();

		if (isDisputeIdAvailable) {
			logger.info("Dispute ID is not available to create dispute...");

			// Try to get detailed error message
			String extractedComment = null;
			Locator errorBlock = page
					.locator("//div[@id='create-re-dispute-validation-step-validation-errors']//ul/li");

			if (errorBlock.count() > 0 && errorBlock.first().isVisible()) {
				extractedComment = errorBlock.first().innerText().trim();
				logger.info("Extracted Re-Dispute Error: " + extractedComment);
			}

			// Click Clear all
			page.locator("//kat-link[@id='create-re-dispute-validation-step-dispute-ids-clear-button']").click();

			if (extractedComment == null || extractedComment.isEmpty()) {
				extractedComment = "These invoices are not available for dispute:";
			}

			// Update DB
			String sql = "UPDATE CB_Open_Shortage_Disputes_To_Submit SET comments='" + extractedComment + "' "
					+ "WHERE disputeId='" + disputeId + "' AND vendorId='" + VendorId + "'";
			System.out.println("Executing SQL Query: " + sql);
			jdbcTemplate.execute(sql);

			return "Not Disputed";
		}

		else {

			String disputedAmount = null;

			try {
				page.getByText("Next").click();
			} catch (Exception ex) {
				logger.info("");
			}
			page.waitForTimeout(2000);
			/*
			 * Locator spanLocator = page.
			 * locator("//span[contains(text(),'Shortages on an invoice can be disputed only once')]"
			 * ); if (spanLocator.count() > 0) {
			 * logger.info("Dispute message found. Proceeding with chip cleanup.");
			 * 
			 * String expected = disputeId.trim() + " - " + invoiceNumber.trim();
			 * System.out.println("Expected: " + expected);
			 * 
			 * boolean matchedChipFound = false; // Declare it here
			 * 
			 * while (true) { Locator disputeChips =
			 * page.locator("kat-link[id*='select'] >> span"); Locator deleteIcons =
			 * page.locator("//kat-icon[contains(@id,'delete')]");
			 * 
			 * int chipCount = disputeChips.count(); boolean deleted = false; int
			 * deleteIconIndex = 0;
			 * 
			 * for (int i = 0; i < chipCount; i++) { String chipText =
			 * disputeChips.nth(i).innerText().trim(); logger.info("Found chip: " + chipText
			 * + " | Expected: " + expected);
			 * 
			 * if (chipText.toLowerCase().contains("select all")) { continue; }
			 * 
			 * if (!chipText.equals(expected)) { logger.info("Deleting unmatched chip: " +
			 * chipText); deleteIcons.nth(deleteIconIndex).click();
			 * page.waitForTimeout(3000); deleted = true; break; } else { matchedChipFound =
			 * true; // Set flag if match found deleteIconIndex++; } }
			 * 
			 * if (!deleted) { logger.info("Chip cleanup complete. Match found: " +
			 * matchedChipFound); break; } }
			 * 
			 * // Check if no matched chip was retained if (!matchedChipFound) { String
			 * updateComment = "Your invoice & VC invoice not matched hence not disputed";
			 * String updateSQL =
			 * "UPDATE CB_Open_Shortage_Disputes_To_Submit SET comments = ? WHERE invoiceNumber = ? AND vendorId = ?"
			 * ; logger.info("Executing SQL Query: " + updateSQL);
			 * jdbcTemplate.update(updateSQL, updateComment, invoiceNumber, VendorId);
			 * return "Not Disputed"; }
			 * 
			 * } else {
			 * logger.info("Dispute message not found. Skipping chip removal logic."); }
			 * 
			 */

			Locator spanLocator = page
					.locator("//span[contains(text(),'Shortages on an invoice can be disputed only once')]");
			if (spanLocator.count() > 0) {

				logger.info("need to click with all dispute invoice combinations");
				// Get all dispute invoice chips
				Locator disputeInvoices = page.locator("kat-link[id*='select'] >> span");
				page.waitForSelector("kat-link[id*='select'] >> span");

				int invoiceCount = disputeInvoices.count();
				System.out.println("Found " + invoiceCount + " dispute invoices.");

				// Loop through each dispute invoice
				for (int i = 0; i < invoiceCount; i++) {

					// Re-locate in case DOM changes after navigation
					disputeInvoices = page.locator("kat-link[id*='select'] >> span");

					System.out.println("Opening dispute invoice " + (i + 1) + " of " + invoiceCount);
					disputeInvoices.nth(i).click();
					page.waitForTimeout(1000); // let page load

					// Click "Select All" if present in this invoice
					Locator selectAllBtn = page.locator("a")
							.filter(new Locator.FilterOptions().setHasText("Select all"));
					if (selectAllBtn.count() > 0) {
						System.out.println("Clicking Select All for invoice " + (i + 1));
						selectAllBtn.click();
						page.waitForTimeout(2000); // let selection happen
					} else {
						System.out.println("No Select All button in invoice " + (i + 1));
					}

					// Optionally trigger dispute submission here
					// page.locator("button:has-text('Submit Dispute')").click();
					// page.waitForTimeout(1000);
				}

			} else {
				logger.info("Dispute message not found. Skipping chip removal logic.");
				page.locator("a").filter(new Locator.FilterOptions().setHasText("Select all")).click();
				page.waitForTimeout(3000);

			}

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

					String paymentDueDate = page
							.locator("//div[contains(@id,'line-due-date')]/div[@id='line-table-field-value']")
							.innerText();
					String invoiceDate = page
							.locator("//div[contains(@id,'line-invoice-date')]/div[@id='line-table-field-value']")
							.innerText();

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
					sql.append("INSERT IGNORE INTO CB_Open_Shortage_Dispute_Details (vendorId,PO,asin,shortageQuantity,"
							+ "disputeQuantity,unitPrice,shortageAmount,disputeAmount,invoiceNumber,createdDate,"
							+ "invoiceDate,dueDate,payeeCode,invoicedQuantity,invoicedAmount,externalId)VALUES('"
							+ VendorId + "','" + getPO + "','" + getASIN + "','" + getshortageQty + "','"
							+ getDisputeQty + "','" + getunitCost.replace("$", "").replace(",", "").trim() + "','"
							+ getShortageAmt.replace("$", "").replace(",", "").trim() + "','"
							+ getDisputeAmount.replace("$", "").replace(",", "").trim() + "','" + invoiceNumber
							+ "',now(),STR_TO_DATE('" + invoiceDate.toString().trim() + "','%m/%d/%Y') ,STR_TO_DATE('"
							+ paymentDueDate.toString().trim() + "','%m/%d/%Y') ,'" + payee + "','" + getInvoicedQty
							+ "','" + getInvoicedAmt.replace("$", "").replace(",", "").trim() + "','" + getExternalId
							+ "')");

					logger.info(sql.toString());
					jdbcTemplate.execute(sql.toString());

				} catch (Exception e) {
					break;
				}

			}
			page.waitForTimeout(2000);
			Locator confirmationText = page.locator("//div[@id='prepaid-invoice-warning-banner']");
			if (confirmationText.isVisible()) {
				System.out.println("Confirmation found. Updating database...");

				// Update the database
				/*
				 * System.out.println("Executing SQL Query: " +
				 * "UPDATE CB_Open_Shortage_Disputes_To_Submit SET comments='Your invoice has been fully paid through the following payment numbers' "
				 * + "WHERE invoiceNumber='" + invoiceNumber + "' AND vendorId='" + VendorId +
				 * "'"); jdbcTemplate.execute(
				 * "UPDATE CB_Open_Shortage_Disputes_To_Submit SET comments='Your invoice has been fully paid through the following payment numbers' "
				 * + "WHERE invoiceNumber='" + invoiceNumber + "' AND vendorId='" + VendorId +
				 * "'");
				 */

				System.out.println("Executing SQL Query: "
						+ "UPDATE CB_Open_Shortage_Disputes_To_Submit SET comments='Your invoice has been fully paid through the following payment numbers' "
						+ "WHERE disputeId='" + disputeId + "' AND vendorId='" + VendorId + "'");
				jdbcTemplate.execute(
						"UPDATE CB_Open_Shortage_Disputes_To_Submit SET comments='Your invoice has been fully paid through the following payment numbers' "
								+ "WHERE disputeId='" + disputeId + "' AND vendorId='" + VendorId + "'");
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

			page.waitForTimeout(3000);

			String InsertDisputeRequestQuery = null;
			List<Locator> spanlocator = page.locator("//div[@id='total-line-dispute-amount']").all();
			if (spanlocator.size() > 0) {
				disputedAmount = page.locator("//div[@id='total-line-dispute-amount']").textContent().trim();
				disputedAmount = commonobj.processAmount(disputedAmount);

			}
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();

			page.waitForTimeout(2000);
			page.locator("//kat-button[@id='submit-dispute-modal-submit-button']").click();
			logger.info("CREATED DISPUTE");
			page.waitForTimeout(5000);
			try {
				Locator errorText = page.locator("//div[@id='submit-dispute-modal']//h5");

				// Try accessing only if element exists (count > 0 avoids timeout of isVisible)
				if (errorText.count() > 0) {
					String errorMsg = errorText.first().textContent().trim(); // faster than innerText()

					if (errorMsg.contains("Could not submit the re-dispute") || errorMsg
							.contains("Could not submit the dispute as these items are ineligible for dispute")) {

						logger.info("Error message detected. Returning Not Disputed.");

						// Use extracted error message or fallback comment
						String comment = (errorMsg == null || errorMsg.isEmpty())
								? "Dispute already exists with ID or could not submit re-dispute & items are ineligible for re-dispute"
								: errorMsg;

						// Safe SQL update
						/*
						 * String sql = "UPDATE CB_Open_Shortage_Disputes_To_Submit SET comments = ? " +
						 * "WHERE invoiceNumber = ? AND vendorId = ?";
						 */

						String sql = "UPDATE CB_Open_Shortage_Disputes_To_Submit SET comments = ? "
								+ "WHERE disputeId = ? AND vendorId = ?";
						jdbcTemplate.update(sql, comment, disputeId, VendorId);

						return "Not Disputed";
					}
				}
			} catch (Exception ex) {
				logger.warn("Error while checking for error message: " + ex.getMessage());
				System.out.println("Dispute ID is available to create dispute.");
			}

			page.waitForTimeout(7000);
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

			InsertDisputeRequestQuery = "INSERT INTO CBClientDispute (disputeId,vendorId,type,disputeDate,requestStatus,disputeAmount,"
					+ "createdDate,currency,status,createdBy) VALUES ('" + SubmittedDisputeIdString.trim() + "','"
					+ VendorId + "','open_shortage',NOW(),'PENDING'," + "'" + disputedAmount.replace("$", "")
							.replace(",", "").replace("€", "").replace("Dispute amount : ", "").trim()
					+ "',NOW(),'" + Currency + "','ACTIVE','79')";

			logger.info(InsertDisputeRequestQuery);

			try {
				jdbcTemplate.execute(InsertDisputeRequestQuery);
			} catch (DataAccessException e) {
				e.printStackTrace();
				// TODO Auto-generated catch block
				InsertDisputeRequestQuery = InsertDisputeRequestQuery.replace("CBClientDispute",
						"CBClientDisputeMissingLiines");
				jdbcTemplate.execute(InsertDisputeRequestQuery);
			}

	

			System.out.println(
					"Executing SQL Query: " + "UPDATE CB_Open_Shortage_Disputes_To_Submit SET newSubmittedDisputeId = '"
							+ SubmittedDisputeIdString.trim() + "' " + "WHERE vendorId = '" + VendorId
							+ "' AND disputeId = '" + disputeId + "'");

			jdbcTemplate.execute("UPDATE CB_Open_Shortage_Disputes_To_Submit SET newSubmittedDisputeId = '"
					+ SubmittedDisputeIdString.trim() + "' " + "WHERE vendorId = '" + VendorId + "' AND disputeId = '"
					+ disputeId + "'");

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

			page.waitForTimeout(4000);

			commonobj.killLoader(page);

			return "Disputed";

		}

	}

	public static String getCountryUrl(String inVendorName) {
		String[] switchStrings = { "CA -", "ES -", "DE -", "GB -", "FR -", "AE -", "AU -", "IT -", "MX -", "BE -",
				"PL -", "SE -", "NL -" };
		String outCountry = "https://vendorcentral.amazon.com"; // Default value

		// Convert inVendorName to uppercase for case-insensitive comparison
		String message = inVendorName.toUpperCase();

		// Check if the message contains any of the country codes and set the
		// corresponding URL
		for (String countryCode : switchStrings) {
			if (message.contains(countryCode)) {
				switch (countryCode) {
				case "GB -":
					outCountry = "https://vendorcentral.amazon.co.uk";
					break;
				case "CA -":
					outCountry = "https://vendorcentral.amazon.com";
					break;
				case "DE -":
					outCountry = "https://vendorcentral.amazon.de";
					break;
				case "FR -":
					outCountry = "https://vendorcentral.amazon.fr";
					break;
				case "ES -":
					outCountry = "https://vendorcentral.amazon.es";
					break;
				case "AU -":
					outCountry = "https://vendorcentral.amazon.com.au";
					break;
				case "IT -":
					outCountry = "https://vendorcentral.amazon.it";
					break;
				case "MX -":
					outCountry = "https://vendorcentral.amazon.com";
					break;
				case "BE -":
					outCountry = "https://vendorcentral.amazon.com.be";
					break;
				case "PL -":
					outCountry = "https://vendorcentral.amazon.pl";
					break;
				case "NL -":
					outCountry = "https://vendorcentral.amazon.nl";
					break;
				case "SE -":
					outCountry = "https://vendorcentral.amazon.se";
					break;
				default:
					outCountry = "https://vendorcentral.amazon.com"; // Default case (already initialized)
					break;
				}
				break; // Exit the loop once a match is found
			}
		}
		return outCountry;
	}

	public static String extractDisputeId(String text, String currency) {
		// Case-insensitive pattern, handles optional space and colon
		// String regex = "(?i)Dispute\\s*Id\\s*:?\\s*(DSPT\\d+)";
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
