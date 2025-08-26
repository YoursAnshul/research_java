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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.SessionData;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.warrenstrange.googleauth.GoogleAuthenticator;

@Component

public class VCLogin {
	
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void processPage(Page page, BrowserContext context, String username,String password,String key)
	{
		
		
		logger.info("Login Page Opened");
		page.waitForTimeout(1000);
		boolean isSignInPage=page.isVisible("//input[@type='email']");
		
		if(isSignInPage==false)
		{
			try
			{
				logger.info("Page Already Signed in to Account trying to logout........");
				page.locator("//div[@class='utility-bar-settings']").click();
				page.locator("//div[@class=\"settings-list\"]/a[text()='Sign Out']").click();
				page.waitForTimeout(1000);
				page.reload();
				page.waitForTimeout(1000);
				//page.navigate("https://vendorcentral.amazon.com/ap/signin");
				//page.waitForTimeout(1000);
			}
			catch(Exception ex)
			{
				logger.info("Account Already Sign out");
				page.waitForTimeout(1000);
				page.reload();
				//page.navigate("https://vendorcentral.amazon.com/ap/signin");
			}
		}
		
		page.locator("//input[@id=\"ap_email\"]").fill(username);
		page.waitForTimeout(1000);
		if(page.isVisible("//input[@id=\"ap_password\"]"))
		{
			page.locator("//input[@id=\"ap_password\"]").fill(password);
		}
		else
		{
			page.locator("//span[@id=\"continue\"]").click();
			page.locator("//input[@id=\"ap_password\"]").fill(password);
		}
		
		//page.locator("//input[@id=\"signInSubmit\"]").click();
		page.evaluate("document.querySelector('#signInSubmit').click();");
		page.waitForTimeout(1000);
		//Two Step Verification login with OTP on Gauth Page
		
		boolean isLoggedin=false;
		do {
			
			GoogleAuthenticator gAuth = new GoogleAuthenticator();
			int otp = gAuth.getTotpPassword(key);
			String passCode = String.format("%06d", otp);

			page.locator("//input[@id=\"auth-mfa-otpcode\"]").fill(passCode);
			page.waitForTimeout(1000);
			page.locator("//*[@id=\"auth-signin-button\"]").click();
			
			page.waitForTimeout(1000);
			
			isLoggedin = page.isVisible("//*[@id=\"authportal-main-section\"]");
			if(isLoggedin==true)
			{
				
				page.waitForTimeout(7000);
				
				String jsonContext = null;
				String vendorName = "US";

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
				sql.append("SELECT sessionData FROM CBSessionStorage  WHERE  username = '" + username
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
									+ username + "','" + vendorName + "','" + safeJson + "')");

				} else {

					jdbcTemplate.execute("UPDATE CBSessionStorage SET `sessionData` = '" + safeJson
							+ "' , modifiedDateTime = now()" + " WHERE   username = '" + username
							+ "' AND `marketPlace` = '" + vendorName + "'");

					logger.info("login is successfull");
				}

				break;
			
				
				
				
				
			}
			logger.info("Page Logged in : " + isLoggedin);
			
			
			
			
		} while (isLoggedin == true);
		logger.info("---------------------Login Successfull---------------------");
	}
}

