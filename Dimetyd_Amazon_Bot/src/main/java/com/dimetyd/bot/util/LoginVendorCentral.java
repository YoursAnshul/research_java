package com.dimetyd.bot.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.config.UploadScreenshotToS3;
import com.dimetyd.bot.model.SessionData;
import com.dimetyd.bot.model.VendorCrd;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class LoginVendorCentral {

	@Value("${region}")
	private String region; // Example region
	@Value("${bucketname}")
	private String bucketName;
	@Value("${spring.application.name}")
	private String s3Key;

	@Autowired
	UploadScreenshotToS3 uploadS3;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	GenrateMfaCode gMfaObj;

	@Autowired
	SwitchVCAccount swAccount;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public boolean Login(Page page, String username, String password, String url) {

		logger.info("LoginVendorCentral page Running, username: " + username);

		page.navigate(url);
		page.locator("//input[@type='email']").click();
		page.locator("//input[@type='email']").fill(username);
		logger.info("username inserted");

		try {

			page.waitForSelector("//input[@id='continue']", new Page.WaitForSelectorOptions().setTimeout(7000));
			page.locator("//input[@id='continue']").dblclick();
			logger.info("clicked continue");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		page.onDialog(dialog -> {
			logger.info("Dialog message: " + dialog.message());
			dialog.dismiss(); // Automatically dismiss the dialog
		});

		try {
			page.locator("//input[@type='password']").fill(password);
			page.evaluate("document.querySelector('#signInSubmit').click();");

			return true;

			// page.locator("//input[@id='signInSubmit']").click();
		} catch (Exception e) {

			LocalDateTime currentDateTime = LocalDateTime.now();
			Path tempFilePath = Paths.get("Dispute_update" + username + "_" + currentDateTime + "_screenshot.png");
			page.screenshot(new Page.ScreenshotOptions().setPath(tempFilePath));
			logger.info("Screenshot saved locally at: " + tempFilePath);
			File file = new File(tempFilePath.toString());
			uploadS3.uploadJobsFile(file, tempFilePath.toString());

			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		}

	}

	public Pair<Page, Boolean> LoadSessionData(Page page, Browser browser, BrowserContext context, String Url,
			String uername, String marketPlace) {

		logger.info("Url: " + Url);

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT sessionData FROM CBSessionStorage  WHERE  username = '" + uername + "' AND `marketPlace` = '"
				+ marketPlace + "' AND `modifiedDateTime`  > SUBDATE( NOW(), INTERVAL 24 HOUR)");

		List<SessionData> jobList = this.jdbcTemplate.query(sql.toString(), new RowMapper<SessionData>() {
			@Override
			public SessionData mapRow(ResultSet rs, int rowNum) throws SQLException {

				SessionData cd = new SessionData();
				cd.setSessionData(rs.getString("sessionData"));

				return cd;
			}

		}, new Object[] {});

		if (jobList.size() == 0) {
			logger.info("User session not found in db");

			return Pair.of(page, false);
		} else {

			String contextJson = jobList.get(0).getSessionData();

		

			Path tempFilePath = Paths.get("loaded-context.json");
			try {

				Files.writeString(tempFilePath, contextJson, java.nio.charset.StandardCharsets.UTF_8);
				// Files.writeString(tempFilePath, contextJson);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			logger.info("Context saved to file: " + tempFilePath.toAbsolutePath());

			boolean loginElement = false;

			try {
				// Load context from the JSON file
				context = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(tempFilePath));

				page = context.newPage();

				page.navigate(Url);

				page.waitForTimeout(2000);

				Locator element = page.locator("//img[@class='small utility-bar-icon']").nth(0);
				element.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
				page.locator("//*[@class='utility-bar-button-link']").nth(0)
						.waitFor(new Locator.WaitForOptions().setTimeout(3000));
				loginElement = page.locator("//*[@class='utility-bar-button-link']").nth(0).isVisible();
				logger.info("loginElement: " + loginElement);

			} catch (Exception e) {
				page.close();
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

			if (loginElement) {
				return Pair.of(page, true);
			} else {
				return Pair.of(page, false);
			}
		}

	}

	public void Logout(Page page)

	{

		boolean isLogin = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Sign in")).isVisible();

		if (isLogin) {

		} else

		{

			page.getByLabel("Settings").locator("img").click();
			page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign Out")).click();

		}

	}

	public Pair<Page, Boolean> loginProcess(String vendorId, Page page, String vendorName,
			HashMap<String, String> loginHashMap, int counter, Browser browser, BrowserContext context) {
		// TODO Auto-generated method stub

		logger.info("LoginVendorCentral Running");

		String markrtPlace = vendorName.substring(0, 2).trim();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT `userName`,`password`,`authenticationKeyAccount`,`authenticationKeySecret`,url "
				+ "FROM `VendorCredentials` WHERE vendorId = '" + vendorId + "'");

		List<VendorCrd> vcList = this.jdbcTemplate.query(sql.toString(), new RowMapper<VendorCrd>() {
			@Override
			public VendorCrd mapRow(ResultSet rs, int rowNum) throws SQLException {

				VendorCrd vc = new VendorCrd();

				vc.setUserName(rs.getString("userName"));
				vc.setPassword(rs.getString("password"));
				vc.setAuthenticationKeyAccount(rs.getString("authenticationKeyAccount"));
				vc.setAuthenticationKeySecret(rs.getString("authenticationKeySecret"));
				vc.setUrl(rs.getString("url"));
				return vc;
			}

		}, new Object[] {});

		if (vcList.size() == 0)

		{
			logger.info("Vendor Credentials not found in db");
			return Pair.of(page, false);

		} else {

			logger.info("Processing Transcation number :" + counter);

			if (counter == 1)

			{
				logger.info("Checking for the active session for: " + vcList.get(0).getUserName());

				Pair<Page, Boolean> isLoadSession = LoadSessionData(page, browser, context, vcList.get(0).getUrl(),
						vcList.get(0).getUserName(), markrtPlace);
				page = isLoadSession.getLeft();
				if (!isLoadSession.getRight()) {

					if (context == null) {
						context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 800)
								.setIgnoreHTTPSErrors(true).setJavaScriptEnabled(true).setLocale("en-US"));
						page = context.newPage();
					}

					boolean isLogin = Login(page, vcList.get(0).getUserName(), vcList.get(0).getPassword(),
							vcList.get(0).getUrl());
					isLogin =	gMfaObj.GenrateMfa(page, vcList.get(0).getUserName(), vcList.get(0).getAuthenticationKeySecret(),
							vendorName, context);

					if (isLogin) {

					} else {
						logger.info("Not able to login, Login flow has error");
						return Pair.of(page, false);

					}

				}

			}

			else {
				
				
				String username = loginHashMap.get("Username");
				String url = loginHashMap.get("Url");
				String vcUsername = vcList.get(0).getUserName();
				String vcUrl = vcList.get(0).getUrl();

				if (username != null && vcUsername != null && username.equalsIgnoreCase(vcUsername)
					    && url != null && vcUrl != null && url.equalsIgnoreCase(vcUrl))

				{

					logger.info("Username is same as old: " + loginHashMap.get("Username"));
					page.navigate(vcList.get(0).getUrl());

				}

				else

				{
					try {
						page.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					logger.info("Checking for the active session for: " + vcList.get(0).getUserName());

					Pair<Page, Boolean> isLoadSession = LoadSessionData(page, browser, context, vcList.get(0).getUrl(),
							vcList.get(0).getUserName(), markrtPlace);

					page = isLoadSession.getLeft();

					if (!isLoadSession.getRight()) {

						if (context == null) {
							context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 800)
									.setIgnoreHTTPSErrors(true).setJavaScriptEnabled(true).setLocale("en-US"));
							page = context.newPage();
						}

						boolean isLogin = Login(page, vcList.get(0).getUserName(), vcList.get(0).getPassword(),
								vcList.get(0).getUrl());
						isLogin = gMfaObj.GenrateMfa(page, vcList.get(0).getUserName(),
								vcList.get(0).getAuthenticationKeySecret(), vendorName, context);

						if (isLogin) {

						} else {
							logger.info("Not able to login, Login flow has error");
							return Pair.of(page, false);

						}

					}

				}
			}

			loginHashMap.put("Username", vcList.get(0).getUserName());
			loginHashMap.put("Url", vcList.get(0).getUrl());

			boolean switchSatus = swAccount.SwitchAccount(page, vendorName);

			if (switchSatus) {

				return Pair.of(page, true);
			} else {
				logger.info("Switch account has error");
				return Pair.of(page, false);

			}

		}

	}

}
