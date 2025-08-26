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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.SessionData;
import com.dimetyd.bot.model.VendorCrd;
import com.dimetyd.bot.process.CheckingVendors_access;
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
public class VendorOnAccess {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    LoginVendorCentral loginObj;
    @Autowired
    private CheckingVendors_access checkVendors;
    @Autowired
    SwitchVCAccount swAccount;
    @Autowired
    VCLogin VCLogin;

    static Page page;
    BrowserContext context;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "0 0 * * * *") // runs every hour at 00:00
	@PostConstruct
    public void startService() {
        if (!GlobalSession.getGlobalSession().getName().equals("VendorOnAccess_Account")) {
            return;
        }

        logger.info("VendorOnBoarding bot started.");

        List<String> list = new ArrayList<>();
        list.add("--disable-webauthn");
        list.add("--disable-features=PasswordlessLogin");

        Playwright playwright = Playwright.create();

        Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));
  while(true)
  {
        String sql = "SELECT * FROM CBMasterLoginCredentials_07 WHERE status='PENDING' AND needToRerunDaily='Yes' LIMIT 1";

        List<VendorCrd> credentialsList = this.jdbcTemplate.query(sql, (rs, rowNum) -> {
            VendorCrd creds = new VendorCrd();
            creds.setId(rs.getInt("id"));
            creds.setUserName(rs.getString("username"));
            creds.setPassword(rs.getString("Password"));
            creds.setAuthenticationKeyAccount(rs.getString("authenticationKeyAccount"));
            creds.setAuthenticationKeySecret(rs.getString("authenticationKeySecret"));
            creds.setStatus(rs.getString("status"));
            creds.setNeedToRerunDaily(rs.getString("NeedToRerunDaily"));
            return creds;
        });

        if (credentialsList.isEmpty()) {
            logger.info("No pending credentials found.");
            return;
        }

        VendorCrd creds = credentialsList.get(0);
        int id = creds.getId();

        jdbcTemplate.execute("UPDATE CBMasterLoginCredentials_07 SET status='INPROGRESS' WHERE id='" + id + "'");
        logger.info("Processing ID: " + id);

        try {
            Pair<Page, Boolean> sessionResult = LoadSessionData(page, browser, context,
                    "https://vendorcentral.amazon.com/", creds.getUserName(), "US");

            page = sessionResult.getLeft();

            if (!sessionResult.getRight()) {
                context = browser.newContext(new Browser.NewContextOptions()
                        .setViewportSize(1280, 800).setIgnoreHTTPSErrors(true)
                        .setJavaScriptEnabled(true).setLocale("en-US"));
                page = context.newPage();
                page.navigate("https://vendorcentral.amazon.com/");
                VCLogin.processPage(page, context, creds.getUserName(), creds.getPassword(),
                        creds.getAuthenticationKeySecret());
            }

            swAccount.switchNow(page, null); // switch to vendor list
            checkVendors.vendorCheck(page, id, creds.getUserName(), creds.getPassword(),
                    creds.getAuthenticationKeySecret(), creds.getAuthenticationKeyAccount(), context);
            page.close();

            jdbcTemplate.execute("UPDATE CBMasterLoginCredentials_07 SET status='COMPLETED' WHERE id='" + id + "'");
            logger.info("ID COMPLETED: " + id);

        } catch (Exception ex) {
            jdbcTemplate.execute("UPDATE CBMasterLoginCredentials_07 SET status='ERROR' WHERE id='" + id + "'");
            logger.error("ID ERROR: " + id);
            logger.error("Exception: ", ex);
        }
    }
    }

    public Pair<Page, Boolean> LoadSessionData(Page page, Browser browser, BrowserContext context, String Url,
                                               String username, String marketPlace) {

        String sql = "SELECT sessionData FROM CBSessionStorage WHERE username = '" + username + "' AND marketPlace = '" + marketPlace + "' " +
                "AND modifiedDateTime > SUBDATE(NOW(), INTERVAL 24 HOUR)";

        List<SessionData> sessionList = this.jdbcTemplate.query(sql, (rs, rowNum) -> {
            SessionData sd = new SessionData();
            sd.setSessionData(rs.getString("sessionData"));
            return sd;
        });

        if (sessionList.isEmpty()) {
            logger.info("No session data found in DB for " + username);
            return Pair.of(page, false);
        }

        String contextJson = sessionList.get(0).getSessionData();
        Path tempFilePath = Paths.get("loaded-context.json");

        try {
            Files.writeString(tempFilePath, contextJson);
        } catch (IOException e) {
            logger.error("Error writing session data to file", e);
        }

        boolean loginSuccessful = false;
        try {
            context = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(tempFilePath));
            page = context.newPage();
            page.navigate(Url);
            page.waitForTimeout(2000);

            Locator element = page.locator("//img[@class='small utility-bar-icon']").nth(0);
            element.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            loginSuccessful = page.locator("//*[@class='utility-bar-button-link']").nth(0).isVisible();

        } catch (Exception e) {
            logger.warn("Session reuse failed, will proceed with manual login.");
            if (page != null) {
                page.close();
            }
        }

        return Pair.of(page, loginSuccessful);
    }
}
