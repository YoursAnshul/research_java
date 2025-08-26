package com.dimetyd.bot.process;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.Vendor;
import com.dimetyd.bot.model.VendorCrd;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class CheckingVendors {
	@Autowired
	private JdbcTemplate jdbctemp;
	@Autowired
	private InsertNewVendorDetails getVendorData;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public void vendorCheck(Page page, int id, String username, String password, String key, String accountkey,
			BrowserContext context) {

		// Fetch vendor names from the web page
		StringBuilder sb = new StringBuilder();
		List<Locator> li = page.locator("//button[starts-with(@class, 'full-page-account-switcher-account-details')]/span").all();
		Set<String> webVendorNames = new HashSet<>(); // Set to avoid duplicates
		
		

		// Loop through the list of web vendor names and store them in a Set for fast
		// lookup
		for (Locator VCVendorNames : li) {
			webVendorNames.add(VCVendorNames.innerText());
		}

		logger.info("Fetching Vendors: " + li.size());

		// SQL query to fetch vendors from the database
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT id, vendorName FROM Vendor ");
		logger.info(sql.toString());
		// Execute the query and get the list of Vendors from the database
		List<Vendor> vcList = this.jdbctemp.query(sql.toString(), new RowMapper<Vendor>() {
			@Override
			public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
				Vendor vc = new Vendor();
				vc.setVendorId(rs.getString("id"));
				vc.setVendorName(rs.getString("vendorName"));
				return vc;
			}
		});

		logger.info("Checking For New Vendors-> START");

		// Create a list to store vendors not found on the web page
		List<Vendor> newVendors = new ArrayList<>();

		// Iterate over the web vendor names and compare them with the database vendor
		// names
		StringBuilder allVendorIds = new StringBuilder();

		for (String vendorNameFromWeb : webVendorNames) {
			// Check if the web vendor is in the database list
			boolean vendorFoundInDb = false;
			vendorNameFromWeb=vendorNameFromWeb.replace("(current)", "").trim();


			for (Vendor vendor : vcList) {
				String vendorNameFromDb = vendor.getVendorName().trim();


				if (vendorNameFromWeb.equals(vendorNameFromDb)) {
					vendorFoundInDb = true;
					// If the vendor is found, add its ID to the allVendorIds list
					if (allVendorIds.length() == 0) {
						allVendorIds.append("'").append(vendor.getVendorId()).append("'");
					} else {
						allVendorIds.append(", '").append(vendor.getVendorId()).append("'");
					}
					break; // Break the inner loop once we find a match
				}
			}

			if (!vendorFoundInDb) {
				// If the vendor was not found in the database, add to the newVendors list
				vendorNameFromWeb=vendorNameFromWeb.replace("'", "''");
				Vendor newVendor = new Vendor();
				newVendor.setVendorName(vendorNameFromWeb);
				newVendors.add(newVendor);
		
				String VendorCredsQuery = "SELECT *FROM `VendorCredentials` WHERE `amazonVCName` ='"
						+ vendorNameFromWeb + "'";
				logger.info("New Vendor Found: " + vendorNameFromWeb);

				List<VendorCrd> VendorCredentialsList = this.jdbctemp.query(VendorCredsQuery,
						new RowMapper<VendorCrd>() {
							@Override
							public VendorCrd mapRow(ResultSet rs, int rowNum) throws SQLException {
								VendorCrd MasterCreds = new VendorCrd();

								return MasterCreds;
							}
						}, new Object[] {});
				if (VendorCredentialsList.size() == 0) {

					LocalDateTime now = LocalDateTime.now();
					// Optionally, format the output
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
					String formattedNow = now.format(formatter);
					String newVendorId = "VN" + formattedNow;
					page.waitForTimeout(1000);
					logger.info(newVendorId);
					String InsertVendorQuery = "INSERT INTO Vendor (`id`,`vendorName`,`createdDate`,`active`,`isVendorMenuAccess`,`vcAccount`,`master_Crd_Id`,`isPaused`,vendorStatus) VALUES ('"
							+ newVendorId + "','" + vendorNameFromWeb + "',NOW(),'Y',1,'dimetyd'," + id
							+ ",'N','PENDING')";
					logger.info(InsertVendorQuery);
					jdbctemp.execute(InsertVendorQuery);

					logger.info("New Vendor Id Found : " + newVendorId);
					String CountryUrl = getCountryUrl(vendorNameFromWeb);

					String VendorCredentialsInsertQuery = "INSERT INTO `VendorCredentials`(`vendorId`, `organizationName`, `amazonVCName`, `userName`, "
							+ "`password`, `authenticationKeyAccount`, `authenticationKeySecret`, `url`, `createdDate`) "
							+ "VALUES ('" + newVendorId + "', '', '" + vendorNameFromWeb + "', '" + username + "', '"
							+ password + "', " + "'" + accountkey + "', '" + key + "', '" + CountryUrl + "', NOW())";

					logger.info(VendorCredentialsInsertQuery);
					jdbctemp.execute(VendorCredentialsInsertQuery);
				} else {
					logger.info("Vendor Credentials are already present in Database.........");

					String UpdateVendorCredentialsQuery = "UPDATE `VendorCredentials` Set `userName`='" + username
							+ "', `password`='" + password + "'," + "`authenticationKeyAccount`='" + accountkey
							+ "',`authenticationKeySecret`='" + key + "',"
							+ "`modifiedDate`=NOW() Where `amazonVCName`='" + vendorNameFromWeb.replace("'", "''")
							+ "'";
					logger.info(UpdateVendorCredentialsQuery);
					jdbctemp.execute(UpdateVendorCredentialsQuery);
					logger.info("Navigating Page to Vendor List");
					page.navigate("https://vendorcentral.amazon.com/account-switcher/regional/vendorGroup");
				}
			}

		}
		logger.info("Checking For New Vendors-> END");
		// Optionally, print the new vendors added to the list
		logger.info("New Vendors to be added:");

		if (allVendorIds.isEmpty() || allVendorIds.equals(null)) {

		} else {
			String upadateQuery = "UPDATE IGNORE Vendor SET `isVendorMenuAccess`= 1  WHERE id IN(" + allVendorIds + ")";
			logger.info(upadateQuery);
			jdbctemp.execute(upadateQuery);
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
