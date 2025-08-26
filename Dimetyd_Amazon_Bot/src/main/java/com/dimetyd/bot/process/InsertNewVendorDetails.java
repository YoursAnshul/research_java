package com.dimetyd.bot.process;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.dimetyd.bot.util.VCLogin;

@Component
public class InsertNewVendorDetails {

	@Autowired
	private JdbcTemplate jdbctemp;
	@Autowired
	private VendorTabs vendortabs;
	@Autowired
	private VCLogin vclogin;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public boolean vendorDetails(Page page, String VendorName, String vendorId, int id, String username,
			String password, String key, String keyAccount, BrowserContext context) {

		String vName = VendorName.substring(0, 2);
		boolean isLoginPage;
		try {page.waitForTimeout(1000);
		isLoginPage = page.isVisible("//input[@type='email']");

		if (isLoginPage) {
			logger.info("Account Logged out due to other country vendor");
			vclogin.processPage(page, context, username, password, key);

		} else {
			logger.info("Inserting Vendor Details Data : " + VendorName);
			try {
				boolean isListPage = page
						.isVisible("//*[@id=\"sc-content-container\"]//h1[text()='Select an account']");
				if (isListPage) {
					logger.info("//*/button/span[normalize-space(text())=\"" + VendorName + "\"]");
					page.locator("//*/button/span[normalize-space(text())=\"" + VendorName + "\"]").click();
					page.locator("//button[text()=' Select account '][1]").click();
				} else {
					logger.info("List not Opened");
				}
			} catch (Exception ex) {
				logger.info(ex.toString());
				logger.info("No List Account Page Directly opened");
			}

		}
		String insertVendorMenuAccQuery = "INSERT Ignore INTO `vendorMenuAccess` (`vendorId`,`updatedOn`) VALUES ('"
				+ vendorId + "',NOW())";
		logger.info(insertVendorMenuAccQuery);
		jdbctemp.execute(insertVendorMenuAccQuery);

		try {
			try {

				page.waitForTimeout(2000);
				isLoginPage = page.isVisible("//input[@type='email']");
				if (isLoginPage) {
					logger.info("Account Logged out due to other country vendor");
					vclogin.processPage(page, context, username, password, key);

				}
				logger.info("Account switched : " + VendorName);
			} catch (Exception ex) {
				logger.info("Account is Already Switched.......");
			}
			try {

				page.waitForSelector("//div[@class='utility-bar-locale-icon-wrapper']",
						new Page.WaitForSelectorOptions().setTimeout(5000));

				String lang = page.locator("//div[@class='utility-bar-locale-icon-wrapper']").innerText().trim();

				if (!lang.equalsIgnoreCase("EN")) {
					page.locator(
							"//div[@class='utility-bar-locale-icon-wrapper']//img[@class='small utility-bar-icon']")
							.nth(0).click();
					page.locator("//a[@data-test-tag='locale-list-item-en_GB']").click();

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

			logger.info("Navigating to Remittance page for Payee Code");

			page.locator("//div[@aria-label='Navigation menu']").click();
			logger.info("Clicked Navigation Menu");
			String VendorPayeeCode = "";
			try {
				page.locator("//div[@class='side-nav-tab']/span[text()='Payments']").click();
				page.waitForTimeout(1000);
				logger.info("Clicking Payment>> Remmittance");

				boolean isRemmitanceTab = page
						.isVisible("//div[@data-test-tag='flyout-item']/a/div/span[text()='Remittance']");
				if (isRemmitanceTab) {
					page.locator("//div[@data-test-tag='flyout-item']/a/div/span[text()='Remittance']").click();
					page.waitForTimeout(2000);

					try {
						Thread.sleep(1500);
						Locator countriesDropDown = page.locator("//kat-dropdown[@id='countriesDropDown']");
						if (countriesDropDown.count() > 0) {
							countriesDropDown.click();
							List<Locator> listofCountries = page.locator("//kat-option[@role='option']").all();
							for (Locator country : listofCountries) {
								logger.info("CountryName : " + country.innerText());
								String countryName = country.innerText().replaceAll("[\\[\\](){}\\s]", "");
								String cntry = countryName.substring(countryName.length() - 2);
								logger.info("Country " + cntry);
								if (vName.equals(cntry)) {
									country.click();
									Thread.sleep(3000);
									break;
								}
							}
						}

					} catch (Exception e) {

					}

					VendorPayeeCode = page.locator("//*[@id='vendor-codes-info']").innerText();
					logger.info(VendorPayeeCode);
					VendorPayeeCode = VendorPayeeCode
							.replace("You are viewing information for the following vendor codes: ", "")
							.replace(".", "")
							.replace("If you want to add more vendor codes, please contact your account manager.",
									"")
							.replace(
									"You are viewing information for the following registered vendor codes with this account: ",
									"")
							.replace(
									"If you want to add more vendor codes, please contact your account manager.",
									"")
							.trim();
					logger.info(VendorPayeeCode);
				} else {
					logger.info("Remmittance tab is not found");

				}
				page.waitForTimeout(2000);

				logger.info("MESSAGE --->" + VendorPayeeCode);
			} catch (Exception ex) {
				ex.printStackTrace();
				// Output the matched vendor codes
			}

				// Output the matched vendor codes
				logger.info("Matched Vendor Codes: " + VendorPayeeCode);
				String PayeeCodeUpdateQuery = "UPDATE `Vendor` SET `payeeCode` = '" + VendorPayeeCode
						+ "' WHERE `id` = '" + vendorId + "'";
				logger.info(PayeeCodeUpdateQuery);
				jdbctemp.execute(PayeeCodeUpdateQuery);

				// Access Menu Check of vendors
				logger.info("---------------------Checking Menu Access START---------------------");

				vendortabs.checkAllTabs(page, jdbctemp, vendorId);

				logger.info("---------------------Checking Menu Access END---------------------");

				String POHistoryInputQuery = "INSERT INTO `CBPOHistoryInput` (`vendorID`,`vendorName`,`InputClick`,`Year`,`startDate`,`endDate`,"
						+ "`ReportingPeriod`,`status`) SELECT a.id AS vendorId, a.vendorName AS VendorName,MONTHNAME(z.startDate) AS `InputClick`,"
						+ "YEAR(z.startDate) AS `Year`, z.startDate  AS StartDate,z.enddate AS EndDate, 'Monthly' AS reportingPeriod , 'PENDING' AS `status` "
						+ "FROM Vendor a  JOIN (SELECT `startDate`,`endDate` FROM `MonthRange`   WHERE startDate > DATE_SUB(NOW() ,INTERVAL 1095 DAY )"
						+ "AND `endDate` < NOW() AND `reportingPeriod` = 'Monthly'  UNION SELECT startDate, endDate AS endDate FROM (SELECT `startDate`,`endDate`, "
						+ "DATE(NOW()-1)AS DATE ,DATE(NOW()-1) AS todaysDate  FROM `MonthRange` WHERE `reportingPeriod` = 'Monthly') AS z"
						+ " WHERE todaysDate  BETWEEN startDate AND endDate) AS z   WHERE a.id IN('" + vendorId + "')";
				logger.info(POHistoryInputQuery);
				jdbctemp.execute(POHistoryInputQuery);

				LocalDate now1 = LocalDate.now();
				// Define the formatter
				DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy");

				// Loop over the last three years (current, last year, and the year before last)
				for (int i = 0; i < 3; i++) {
					// Calculate the year (now, last year, or year before last)
					String year = now1.minusYears(i).format(formatter1);
					// Add Year wise in InventoryPODataStatus table
					String InventoryPoDataStausQuery = "INSERT INTO `CBInventoryPODataStatus`(`vendorId`,`year`,`createdDate`,`dataStatus`)VALUES ('"
							+ vendorId + "','" + year + "',NOW(), 'PENDING')";
					logger.info(InventoryPoDataStausQuery);
					jdbctemp.execute(InventoryPoDataStausQuery);
				}
				logger.info("*********New Vendor Added Successfully*********");
			} catch (Exception ex) {
				logger.error(ex.toString());
			}
			logger.info("---------------------Navigating Page to Vendor List---------------------");
			String URL = getCountryUrl(VendorName);
			URL = URL + "/account-switcher/regional/vendorGroup";
			logger.info(URL);
			page.navigate(URL);
			return true;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
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
