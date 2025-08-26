package com.dimetyd.bot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.SessionData;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.warrenstrange.googleauth.GoogleAuthenticator;

@Component
public class GenrateMfaCode {

	@Autowired
	JdbcTemplate jdbcTemplate;

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	boolean loginElement;

	public boolean GenrateMfa(Page page, String authenticationKeyAccount, String authenticationKeySecret,
			String vendorName, BrowserContext context) {

		try {
			GoogleAuthenticator gAuth = new GoogleAuthenticator();
			
			int mafCounter = 1;

			while (true) {
				
				int otp = gAuth.getTotpPassword(authenticationKeySecret);
				String otpString = String.format("%06d", otp);

				mafCounter = mafCounter + 1;

				if (mafCounter > 10)

				{

					break;
				}

				page.locator("//input[@id='auth-mfa-otpcode']").fill(otpString);
				page.locator("//input[@id='auth-signin-button']").click();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Locator errorWhileSignin = page.locator("#auth-error-message-box");
				if (errorWhileSignin.count() > 0) {

					logger.info("Waiting for 20 sec, for new mfa token.");

					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}

				int maxAttempts = 10;
				boolean pageLoadedProperly = false;

				for (int attempt = 0; attempt < maxAttempts; attempt++) {
					Locator accountsLoaded = page.locator(
							"//div[@class='full-page-account-switcher-accounts-error full-page-account-switcher-accounts-error-top-level']");
					Locator selectAccountPageLoaded = page.locator("//*[@class='utility-bar-button-link']").nth(0);
					// Check if the accounts error is visible
					boolean isAccountsErrorVisible = accountsLoaded.isVisible();

					if (!isAccountsErrorVisible) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						logger.info("No accounts error visible, checking for Help button...");

						// Check if the Help button is visible
						if (selectAccountPageLoaded.isVisible()) {
							logger.info("Page loaded properly. Help button is visible.");
							pageLoadedProperly = true;
							break; // Exit the loop if the page is loaded properly
						} else {
							logger.info("Help button is not visible. Reloading page...");
						}
					} else {
						logger.info("Accounts error is visible, indicating a problem with loading. Reloading page...");
					}

					// Reload the page and wait for it to load
					page.reload();
					// Optionally wait for a specific element to ensure the page is fully loaded
					page.waitForLoadState(LoadState.DOMCONTENTLOADED);
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

				
				try {

					Locator element = page.locator("//img[@class='small utility-bar-icon']").nth(0);
					element.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
					loginElement = page.locator("//*[@class='utility-bar-button-link']").nth(0).isVisible();
					logger.info("loginElement wihtout Error: " + loginElement);
				} catch (Exception e) {

					loginElement = false;
					// TODO Auto-generated catch block
					 e.printStackTrace();
				}

				logger.info("loginElement: " + loginElement);

				String jsonContext = null;
				if (loginElement)

				{
					vendorName = vendorName.substring(0, 2).trim();

					Path contextFilePath = Paths.get("context.json");
					context.storageState(new BrowserContext.StorageStateOptions().setPath(contextFilePath));

					// Read JSON content from the file to store in DB String jsonContext = null;
					try {
						jsonContext = Files.readString(contextFilePath);
					} catch (IOException e) {
						// TODO Auto-generated catch block e.printStackTrace();
					}

					logger.info("Context JSON: " + jsonContext);

					String safeJson = jsonContext.replace("\\", "\\\\").replace("\"", "\\\"");

					StringBuilder sql = new StringBuilder();
					sql.append("SELECT sessionData FROM CBSessionStorage  WHERE  username = '" + authenticationKeyAccount
							+ "' AND `marketPlace` = '" + vendorName + "'");

					List<SessionData> jobList = this.jdbcTemplate.query(sql.toString(), new RowMapper<SessionData>() {

						@Override
						public SessionData mapRow(ResultSet rs, int rowNum) throws SQLException {

							SessionData cd = new SessionData();
							cd.setSessionData(rs.getString("sessionData"));

							return cd;
						}

					}, new Object[] {});

					if (jobList.size() == 0) {
						jdbcTemplate
								.execute("INSERT INTO CBSessionStorage (`username`,`marketPlace`,`sessionData`) VALUES ('"
										+ authenticationKeyAccount + "','" + vendorName + "','" + safeJson + "')");

					} else {

						jdbcTemplate.execute("UPDATE CBSessionStorage SET `sessionData` = '" + safeJson
								+ "' , modifiedDateTime = now()" + " WHERE   username = '" + authenticationKeyAccount
								+ "' AND `marketPlace` = '" + vendorName + "'");

						logger.info("login is successfull");
					}

					break;
				}

				else {

					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			return loginElement;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			page.close();
			e.printStackTrace();
			return false;
		}

	}

}
