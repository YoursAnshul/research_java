package com.dimetyd.bot.process;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CheckingVendors_access {

	@Autowired
	private JdbcTemplate jdbctemp;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void vendorCheck(Page page, int masterCrdId, String username, String password, String key, String accountKey,
			BrowserContext context) {
		List<Locator> vendorElements = page
				.locator("//button[starts-with(@class, 'full-page-account-switcher-account-details')]/span").all();
		Set<String> vendorNames = new HashSet<>();

		for (Locator element : vendorElements) {
			String vendorName = element.innerText().replace("(current)", "").trim().replace("'", "''");
			vendorNames.add(vendorName);
		}

		logger.info("Total vendors found on site: " + vendorNames.size());

		for (String vendorName : vendorNames) {
			String checkQuery = "SELECT COUNT(*) FROM VendorCredentials_copy WHERE amazonVCName = '" + vendorName + "'";
			Integer count = jdbctemp.queryForObject(checkQuery, Integer.class);

			if (count != null && count > 0) {
				logger.info("Already exists in VendorCredentials_copy: " + vendorName);
				continue;
			}

			// Generate new VendorId
			String vendorId = "VN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
			String url = getCountryUrl(vendorName);

			String insertQuery = "INSERT INTO VendorCredentials_copy (`vendorId`, `organizationName`, `amazonVCName`, `userName`, "
					+ "`password`, `authenticationKeyAccount`, `authenticationKeySecret`, `url`, `createdDate`) "
					+ "VALUES ('" + vendorId + "', '', '" + vendorName + "', '" + username + "', '" + password + "', '"
					+ accountKey + "', '" + key + "', '" + url + "', NOW())";

			logger.info("Inserting new vendor into VendorCredentials_copy: " + vendorName);
			logger.info(insertQuery);
			jdbctemp.execute(insertQuery);

			page.waitForTimeout(200); // Small delay to prevent vendorId collision
		}
	}

	public static String getCountryUrl(String inVendorName) {
		String[] switchStrings = { "CA -", "ES -", "DE -", "GB -", "FR -", "AE -", "AU -", "IT -", "MX -", "BE -",
				"PL -", "SE -", "NL -" };
		String message = inVendorName.toUpperCase();
		for (String countryCode : switchStrings) {
			if (message.contains(countryCode)) {
				switch (countryCode) {
				case "GB -":
					return "https://vendorcentral.amazon.co.uk";
				case "CA -":
				case "MX -":
					return "https://vendorcentral.amazon.com";
				case "DE -":
					return "https://vendorcentral.amazon.de";
				case "FR -":
					return "https://vendorcentral.amazon.fr";
				case "ES -":
					return "https://vendorcentral.amazon.es";
				case "AU -":
					return "https://vendorcentral.amazon.com.au";
				case "IT -":
					return "https://vendorcentral.amazon.it";
				case "BE -":
					return "https://vendorcentral.amazon.com.be";
				case "PL -":
					return "https://vendorcentral.amazon.pl";
				case "NL -":
					return "https://vendorcentral.amazon.nl";
				case "SE -":
					return "https://vendorcentral.amazon.se";
				}
			}
		}
		return "https://vendorcentral.amazon.com";
	}
}
