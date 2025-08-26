package com.dimetyd.bot.service;

import java.io.IOException;
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
import com.dimetyd.bot.process.ForecastingProcess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class ForecastingBotService {
	
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	ForecastingProcess ForecastProcess;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Forecasting")) {
			
			
			logger.info("Forecasting Bot Started...");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));
			
			
			
			while (true) {
				

		        String sql = "SELECT c.`id`, c.`vendorId`, c.`vendorName`, c.`dataStatus`, c.`createdDate` " +
		                     "FROM CBForecastRequest c " +
		                     "JOIN Vendor v ON c.vendorId = v.id " +
		                     "WHERE c.`dataStatus` = 'Pending' AND v.`isVendorMenuAccess` = 1 " +
		                     "ORDER BY v.`master_Crd_Id` LIMIT 1";

		        List<Vendor> jobList = this.jdbcTemplate.query(sql, new RowMapper<Vendor>() {
		            @Override
		            public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
		            	Vendor job = new Vendor();
		                job.setId(rs.getString("id"));
		                job.setVendorId(rs.getString("vendorId"));
		                job.setVendorName(rs.getString("vendorName"));
		                job.setDataStatus(rs.getString("dataStatus"));
		                job.setCreatedDate(rs.getString("createdDate"));
		                return job;
		            }
		        });

		        if (jobList.isEmpty()) {
		            logger.info("No Data Found for job..");
		        	System.exit(0);
		        }
		        else
		        {
		        	counter = counter + 1;
					logger.info("  Vendor Name: " + jobList.get(0).getVendorName() + " vendorId: "
							+ jobList.get(0).getVendorId());

					 String updateQuery = "UPDATE CBForecastRequest SET dataStatus = ? WHERE id = ?";
			            jdbcTemplate.update(updateQuery, "InProgress", jobList.get(0).getId());

					logger.info("Process start..");

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(jobList.get(0).getVendorId(), page,
							jobList.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();
					logger.info("login status: " + loginStatus.getRight());
					if (loginStatus.getRight()) {
						
						
						try {
							ForecastProcess.navigateForecast(page, jobList.get(0).getVendorName(), jobList.get(0).getVendorId());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				      //  page.close();
				        jdbcTemplate.update(updateQuery, "Completed", jobList.get(0).getId());
						
						
					}
		        }
				
			}
			
		}
	}

}
