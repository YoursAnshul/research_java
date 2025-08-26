package com.dimetyd.bot.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.SessionData;
import com.dimetyd.bot.model.Vendor;
import com.dimetyd.bot.model.VendorCrd;
import com.dimetyd.bot.process.CheckingVendors;
import com.dimetyd.bot.process.InsertNewVendorDetails;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.dimetyd.bot.util.SwitchVCAccount;
import com.dimetyd.bot.util.VCLogin;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;

import jakarta.annotation.PostConstruct;

@Service
public class VendorOnBoardingService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	private InsertNewVendorDetails insertVendorDetails;
	@Autowired
	private CheckingVendors checkVendors;
	@Autowired
	SwitchVCAccount swAccount;
	@Autowired
	VCLogin VCLogin;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("VendorOnBoarding")) {

			logger.info("VendorOnBoarding bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			logger.info("Page Started...");

			while (true) {

				StringBuilder sb = new StringBuilder();

				sb.append(
						"select * from CBMasterLoginCredentials where status='PENDING' and needToRerunDaily='Yes' limit 1");

				 //jdbcTemplate.execute(sb.toString());

				List<VendorCrd> CredentialsList = this.jdbcTemplate.query(sb.toString(), new RowMapper<VendorCrd>() {
					@Override
					public VendorCrd mapRow(ResultSet rs, int rowNum) throws SQLException {

						VendorCrd MasterCreds = new VendorCrd();
						// Print the vendor name from the DB
						MasterCreds.setId(rs.getInt("id"));
						MasterCreds.setUserName(rs.getString("username"));
						MasterCreds.setPassword(rs.getString("Password"));
						MasterCreds.setAuthenticationKeyAccount(rs.getString("authenticationKeyAccount"));
						MasterCreds.setAuthenticationKeySecret(rs.getString("authenticationKeySecret"));
						MasterCreds.setStatus(rs.getString("status"));
						MasterCreds.setNeedToRerunDaily(rs.getString("NeedToRerunDaily"));

						return MasterCreds;
					}
				}, new Object[] {});

				if (CredentialsList.size() == 0) {
					
					while (true) {
						String VendorQuery = "SELECT id,vendorName FROM `Vendor` WHERE vendorStatus ='PENDING' AND master_Crd_Id='"+ CredentialsList.get(0).getId() + "'" + " limit 1";

						List<Vendor> NewVendorList = this.jdbcTemplate.query(VendorQuery, new RowMapper<Vendor>() {
							@Override
							public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
								Vendor vendors = new Vendor();
								vendors.setVendorId(rs.getString("id"));
								vendors.setVendorName(rs.getString("vendorName"));

								return vendors;
							}
						}, new Object[] {});
						if (NewVendorList.size() != 0) {
							// Vendor Details data insert update payee code History Input Data

							jdbcTemplate.execute(
									"update Vendor set vendorStatus='INPROGRESS' WHERE master_Crd_Id='"
											+ CredentialsList.get(0).getId() + "' " + "and id='"
											+ NewVendorList.get(0).getVendorId() + "'");
							swAccount.switchNow(page, NewVendorList.get(0).getVendorName());

							boolean jobstatus = false;

							jobstatus = insertVendorDetails.vendorDetails(page,
									NewVendorList.get(0).getVendorName(), NewVendorList.get(0).getVendorId(),
									CredentialsList.get(0).getId(), CredentialsList.get(0).getUserName(),
									CredentialsList.get(0).getPassword(),
									CredentialsList.get(0).getAuthenticationKeySecret(),
									CredentialsList.get(0).getAuthenticationKeyAccount(), context);

							if (jobstatus) {

								jdbcTemplate.execute(
										"update Vendor set vendorStatus='COMPLETED'  WHERE master_Crd_Id='"
												+ CredentialsList.get(0).getId() + "' and id='"
												+ NewVendorList.get(0).getVendorId() + "'");
							} else {
								jdbcTemplate.execute(
										"update Vendor set vendorStatus='ERROR'  WHERE master_Crd_Id='"
												+ CredentialsList.get(0).getId() + "' and id='"
												+ NewVendorList.get(0).getVendorId() + "'");
							}

						} else {
							logger.info("No New Vendor Found");
							page.close();
							break;
						}
					}
					logger.info("###NO TRANSACTIONS FOUND###");

					jdbcTemplate.execute(
							"update CBMasterLoginCredentials set status='PENDING' where NeedToRerunDaily='Yes'");

					System.exit(0);
				} else {
					logger.info("Transaction Found..............");
					jdbcTemplate.execute("update CBMasterLoginCredentials set status='INPROGRESS' where id='"
							+ CredentialsList.get(0).getId() + "'\r\n" + "");

					logger.info("Id INPORGRESS: " + CredentialsList.get(0).getId());

					try {
						Pair<Page, Boolean> isLoadSession = LoadSessionData(page, browser, context,
								"https://vendorcentral.amazon.com/", CredentialsList.get(0).getUserName(), "US");
						page = isLoadSession.getLeft();
						String vendorName = null;
						if (!isLoadSession.getRight()) {

							if (context == null) {
								context = browser.newContext(new Browser.NewContextOptions()
										.setViewportSize(1280, 800).setIgnoreHTTPSErrors(true)
										.setJavaScriptEnabled(true).setLocale("en-US"));
								page = context.newPage();
								page.navigate("https://vendorcentral.amazon.com/");
								VCLogin.processPage(page, context, CredentialsList.get(0).getUserName(),
										CredentialsList.get(0).getPassword(),
										CredentialsList.get(0).getAuthenticationKeySecret());
							} else {
								page = context.newPage();
								page.navigate("https://vendorcentral.amazon.com/");
								VCLogin.processPage(page, context, CredentialsList.get(0).getUserName(),
										CredentialsList.get(0).getPassword(),
										CredentialsList.get(0).getAuthenticationKeySecret());
							}

						}

						// Switching to vendor List Only to fetch new Vendors thats why passed vendor
						// Name as NULL

						swAccount.switchNow(page, vendorName);
							checkVendors.vendorCheck(page, CredentialsList.get(0).getId(),
									CredentialsList.get(0).getUserName(), CredentialsList.get(0).getPassword(),
									CredentialsList.get(0).getAuthenticationKeySecret(),
									CredentialsList.get(0).getAuthenticationKeyAccount(), context);
				
						logger.info("update CBMasterLoginCredentials set status='COMPLETED' where id='"
								+ CredentialsList.get(0).getId() + "'");
						jdbcTemplate.execute("update CBMasterLoginCredentials set status='COMPLETED' where id='"
								+ CredentialsList.get(0).getId() + "'");

					} catch (Exception ex) {
						logger.info("update CBMasterLoginCredentials set status='ERROR' where id='"
								+ CredentialsList.get(0).getId() + "'\r\n" + "");

						jdbcTemplate.execute("update CBMasterLoginCredentials set status='ERROR' where id='"
								+ CredentialsList.get(0).getId() + "'\r\n" + "");

						logger.error(ex.toString());
					}

					while (true) {
						String VendorQuery = "SELECT id,vendorName FROM `Vendor` WHERE vendorStatus ='PENDING'  and  master_Crd_Id='"
								+ CredentialsList.get(0).getId() + "'" + " limit 1";

						List<Vendor> NewVendorList = this.jdbcTemplate.query(VendorQuery, new RowMapper<Vendor>() {
							@Override
							public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
								Vendor vendors = new Vendor();
								vendors.setVendorId(rs.getString("id"));
								vendors.setVendorName(rs.getString("vendorName"));

								return vendors;
							}
						}, new Object[] {});
						if (NewVendorList.size() != 0) {
							// Vendor Details data insert update payee code History Input Data

							jdbcTemplate.execute(
									"update Vendor set vendorStatus='INPROGRESS' WHERE master_Crd_Id='"
											+ CredentialsList.get(0).getId() + "' " + "and id='"
											+ NewVendorList.get(0).getVendorId() + "'");
							swAccount.switchNow(page, NewVendorList.get(0).getVendorName());

							boolean jobstatus = false;

							jobstatus = insertVendorDetails.vendorDetails(page,
									NewVendorList.get(0).getVendorName(), NewVendorList.get(0).getVendorId(),
									CredentialsList.get(0).getId(), CredentialsList.get(0).getUserName(),
									CredentialsList.get(0).getPassword(),
									CredentialsList.get(0).getAuthenticationKeySecret(),
									CredentialsList.get(0).getAuthenticationKeyAccount(), context);

							if (jobstatus) {

								jdbcTemplate.execute(
										"update Vendor set vendorStatus='COMPLETED'  WHERE master_Crd_Id='"
												+ CredentialsList.get(0).getId() + "' and id='"
												+ NewVendorList.get(0).getVendorId() + "'");
							} else {
								jdbcTemplate.execute(
										"update Vendor set vendorStatus='ERROR'  WHERE master_Crd_Id='"
												+ CredentialsList.get(0).getId() + "' and id='"
												+ NewVendorList.get(0).getVendorId() + "'");
							}

						} else {
							logger.info("No New Vendor Found");
							page.close();
							break;
						}
					}

				}

			}

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

			logger.info("contextJson: " + contextJson);

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
}