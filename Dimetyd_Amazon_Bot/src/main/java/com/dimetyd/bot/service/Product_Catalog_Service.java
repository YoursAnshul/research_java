package com.dimetyd.bot.service;

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
import com.dimetyd.bot.model.Vendor;
import com.dimetyd.bot.process.ProductCatalogProcess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.dimetyd.bot.util.SwitchVCAccount;
import com.dimetyd.bot.util.VCLogin;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class Product_Catalog_Service {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	ProductCatalogProcess productCatalogOrdersNaviagePageObj;

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

		if (GlobalSession.getGlobalSession().getName().equals("Product_Catalog")) {

			logger.info("Product Catalog bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));
			
			// **Step 1: Delete old data from financialReport_Input**
						jdbcTemplate.execute("DELETE FROM productCatalog_Input\r\n"
								+ "");
						logger.info("Old PRODUCT CATALOG data deleted.");

						// **Step 2: Insert new data into financialReport_Input**
						int InsertedRows = jdbcTemplate
								.update("INSERT INTO productCatalog_Input(`vendorName`,`vendorId`,`status`)\r\n"
										+ "SELECT v.vendorName AS VendorName,c.vendorId AS vendorId ,'PENDING' AS STATUS FROM Vendor v JOIN `Client` c ON (v.Id = c.vendorId)  WHERE  v.`isPaused` = 'N' AND v.`isVendorMenuAccess` = 1 AND DATE(v.`createdDate`) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)\r\n"
										+ "GROUP BY c.vendorId\r\n"
										+ "");
						logger.info("Inserted new PRODUCT CATALOG data : " + InsertedRows);


			while (true) {
				try {

					  String sql = "SELECT `vendorId`, `vendorName`, `status` FROM `productCatalog_Input` ORDER BY RAND() LIMIT 1";

				        List<Vendor> result = this.jdbcTemplate.query(sql, new RowMapper<Vendor>() {
				            @Override
				            public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
				            	Vendor job = new Vendor();
				                job.setVendorId(rs.getString("vendorId"));
				                job.setVendorName(rs.getString("vendorName"));
				                job.setDataStatus(rs.getString("status"));
				                return job;
								}
							}, new Object[] {});
					if (result.size() == 0) {
						logger.info("NO DATA FOUND");

						System.exit(0);

					}
					
					try {
						counter=counter+1;
						
						String vendorName = result.get(0).getVendorName().trim();
						String vendorId = result.get(0).getVendorId().trim();
					

						logger.info("UPDATE productCatalog_Input SET STATUS='INPROGRESS' WHERE vendorId='" + vendorId + "'");
						jdbcTemplate.execute("UPDATE productCatalog_Input SET STATUS='INPROGRESS' WHERE vendorId='" + vendorId + "'");

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(result.get(0).getVendorId(), page,
								result.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();

						if (loginStatus.getRight()) {

							
							boolean status = productCatalogOrdersNaviagePageObj.navigateForecast(page, vendorName,vendorId );
							if (status) {
								logger.info("UPDATE productCatalog_Input SET STATUS='COMPLETED' WHERE vendorId='" + vendorId + "'");
								jdbcTemplate
										.execute("UPDATE productCatalog_Input SET STATUS='COMPLETED' WHERE vendorId='" + vendorId + "'");
							} else {
								logger.info("UPDATE productCatalog_Input SET STATUS='ERROR' WHERE vendorId='" + vendorId + "'");
								jdbcTemplate.execute("UPDATE productCatalog_Input SET STATUS='ERROR' WHERE vendorId='" + vendorId + "'");
							}

						} else {
							logger.info("UPDATE productCatalog_Input SET STATUS='ERROR' WHERE vendorId='" + vendorId + "'");
							jdbcTemplate.execute("UPDATE productCatalog_Input SET STATUS='ERROR' WHERE vendorId='" + vendorId + "'");
						}

					} catch (IndexOutOfBoundsException e) {
						e.printStackTrace();
					}

				} catch (Exception e) {
					e.printStackTrace();
					// eventRecorder.navigate().to("https://vendorcentral.amazon.com/home/vc");

				}

			}

		}
	}

}
