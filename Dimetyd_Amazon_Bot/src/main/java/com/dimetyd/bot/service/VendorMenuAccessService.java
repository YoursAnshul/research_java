package com.dimetyd.bot.service;

import java.io.IOException;
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.ShipmentBotInput;
import com.dimetyd.bot.process.VendorMenuAccessProcess;
import com.dimetyd.bot.process.ShortagePage;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class VendorMenuAccessService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	VendorMenuAccessProcess coopprocess;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("VendorMenuAccess")) {

			logger.info("Vendor_Menu_Access bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			logger.info("Page Started...");

			while (true) {

				StringBuilder sb = new StringBuilder();

				sb.append("SELECT vendorId, vendorName FROM vendorMenuAccess a " + "JOIN Vendor v ON (a.vendorId=v.id) "
						+ "WHERE DATE_SUB(NOW(), INTERVAL 2 DAY) > updatedOn "
						+ "AND v.isVendorMenuAccess = '1' AND v.isPaused ='N'ORDER BY RAND() LIMIT 1");

				// jdbcTemplate.execute(sb.toString());

				List<ShipmentBotInput> InputDataValues = this.jdbcTemplate.query(sb.toString(),
						new RowMapper<ShipmentBotInput>() {
							@Override
							public ShipmentBotInput mapRow(ResultSet rs, int rowNum) throws SQLException {

								ShipmentBotInput inputdata = new ShipmentBotInput();
								// Set All Input data

								inputdata.setVendorId(rs.getString("vendorId"));
								inputdata.setVendorName(rs.getString("vendorName"));

								return inputdata;
							}
						}, new Object[] {});

				if (InputDataValues.size() == 0) {
					logger.info("###NO TRANSACTIONS FOUND###");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					counter=counter+1;
					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(InputDataValues.get(0).getVendorId(), page,
							InputDataValues.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();

					if (loginStatus.getRight()) {

						try {
							coopprocess.navigateAllPage(page, InputDataValues.get(0).getVendorId());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}

			}
		}

	}
}
