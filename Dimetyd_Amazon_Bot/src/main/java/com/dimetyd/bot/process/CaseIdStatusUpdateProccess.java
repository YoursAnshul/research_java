package com.dimetyd.bot.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class CaseIdStatusUpdateProccess {

	@Autowired
	JdbcTemplate jdbcTemplate;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void updateStatusPage(Page page, String id, String vendorId, String vendorName, String caseId,
			String currentCaseStatus, String BotStage) {

		String UpdateCaseStatus = "UPDATE CaseIdUpdationLog SET `oldCaseStatus`=`currentCaseStatus` WHERE `caseId`='"
				+ caseId + "'";
		String URL = getCountryUrl(vendorName);
		logger.info("Country URL : " + URL);

		// Navigate Page to

		page.navigate(URL + "/cu/case-lobby?ref=xx_caselog_count_home");

		while (true) {
			try {
				boolean CaseIdSearch = page.isVisible("input[type='search']");
				if (CaseIdSearch) {
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

		page.waitForTimeout(1000);
//		Locator search = page.locator(".hill-case-lobby-tabs-container input[type='search']");
//		if (search.count() > 0) {
//			logger.info("search found");
//			page.locator(".hill-case-lobby-tabs-container input[type='search']").fill(caseId);
//			page.waitForTimeout(1000);
//			page.locator(".hill-search-all-cases-button > button.button").click();
//		}

		Locator search = page.locator("kat-input >> input[type='search']");
		if (search.count() > 0) {
		    logger.info("search found");
		    search.fill(caseId);
		    page.waitForTimeout(1000);
		    //page.locator("button:has-text('Search')").click();
		    page.locator("kat-button#search").click();

		}
		page.waitForTimeout(1500);

		String CaseIdDataFound = "";
		try {
			CaseIdDataFound = page.locator("//div[@class='pagination-display-summary']").innerText();
		} catch (Exception ex) {
			CaseIdDataFound = "Data Found";
		}

		logger.info("After Search : " + CaseIdDataFound);

		if (CaseIdDataFound.contains("Displaying 0 to 0 of 0")) {

			if (BotStage == "STAGE 2") {
				logger.info("After Searching in Audit Account Case id data not found");

				String updateQueueUpdationQuery = "UPDATE CaseIdUpdationLog SET jobStatus='ERROR', `lastUpdatedDateTime` = now(), comment='"
						+ CaseIdDataFound + "' WHERE `caseId` ='" + caseId + "'";

				logger.info(updateQueueUpdationQuery);

				jdbcTemplate.execute(updateQueueUpdationQuery);

				logger.info("Case Id updated in ERROR");
			} else {

				logger.info("After Searching Case id data not found");

				String updateQueueUpdationQuery = "UPDATE CaseIdUpdationLog SET jobStatus='QUEUE', `lastUpdatedDateTime` = now(), comment='"
						+ CaseIdDataFound + "' WHERE `caseId` ='" + caseId + "'";

				logger.info(updateQueueUpdationQuery);

				jdbcTemplate.execute(updateQueueUpdationQuery);

				logger.info("Case Id updated in Queue");
			}
		} else {
			logger.info("Updating Current status");

			logger.info(UpdateCaseStatus);

			jdbcTemplate.execute(UpdateCaseStatus);

//			try {
//
//				String portalCaseStatus = page.locator("//td[@class='hill-case-lobby-search-results-panel-status']")
//						.innerText();
//
//				logger.info("Case Status Now on portal : " + portalCaseStatus);
//
//				String UpdateNewStatus = "UPDATE CaseIdUpdationLog set currentCaseStatus='" + portalCaseStatus
//						+ "',lastUpdatedDateTime=NOW(),jobStatus='COMPLETED' where caseId='" + caseId + "'";
//
//				jdbcTemplate.execute(UpdateNewStatus);
//
//				logger.info("Updated portal status of case Id : " + caseId + "");
//			} catch (Exception ex) {
//				logger.info(ex.toString());
//			}
			try {
			    // Locate the row that exactly matches the caseId
			    Locator row = page.locator("//table//tr[td//a[text()='" + caseId + "']]");

			    if (row.count() > 0) {
			        // CaseId row exists → get the status
			        String portalCaseStatus = row.locator(".hill-case-lobby-search-results-panel-status").innerText();
			        logger.info("Case Status Now on portal : " + portalCaseStatus);

			        String UpdateNewStatus = "UPDATE CaseIdUpdationLog " +
			                                 "SET currentCaseStatus='" + portalCaseStatus + "', " +
			                                 "lastUpdatedDateTime=NOW(), jobStatus='COMPLETED' " +
			                                 "WHERE caseId='" + caseId + "'";

			        jdbcTemplate.execute(UpdateNewStatus);

			        logger.info("Updated portal status of case Id : " + caseId);
			    } else {
			        // No exact match found
			        logger.warn("No exact caseId found on portal for caseId: " + caseId);
			    }
			} catch (Exception ex) {
			    logger.error("Error while updating caseId: " + caseId + ", error: " + ex.toString());
			}

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

}
