package com.dimetyd.bot.service;

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
import com.dimetyd.bot.model.T24CoopJobData;
import com.dimetyd.bot.process.Coop24Process;
import com.dimetyd.bot.process.ShortagePage;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class T24CoopJobService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	ShortagePage shortagePage;
	
	@Autowired
	Coop24Process coopprocess;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("T24CoopJob")) {

			logger.info("T24 Coop bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			logger.info("Page Started...");

			while (true) {

				StringBuilder sb = new StringBuilder();

				sb.append("SELECT a.id,a.vendorName,a.vendorId,a.startDate,a.endDate , c.`businessUnit` \r\n"
						+ "FROM `CBCoopContraCogsInvoiceRequest` a INNER JOIN Vendor v ON (a.vendorId=v.id)  INNER JOIN `Client` c ON (c.vendorId=v.id)WHERE a.STATUS='PENDING' AND v.`isPaused` = 'N' \r\n"
						+ "ORDER BY v.master_Crd_Id LIMIT 1");

				// jdbcTemplate.execute(sb.toString());

				List<T24CoopJobData> InputDataValues = this.jdbcTemplate.query(sb.toString(),
						new RowMapper<T24CoopJobData>() {
							@Override
							public T24CoopJobData mapRow(ResultSet rs, int rowNum) throws SQLException {

								T24CoopJobData inputdata = new T24CoopJobData();
								// Set All Input data
								inputdata.setId(rs.getInt("id"));
								inputdata.setVendorId(rs.getString("vendorid"));
								inputdata.setVendorName(rs.getString("vendorName"));
								inputdata.setStartDate(rs.getString("startDate"));
								inputdata.setEndDate(rs.getString("EndDate"));
								inputdata.setBusinessUnit(rs.getString("businessUnit"));

								return inputdata;
							}
						}, new Object[] {});

				if (InputDataValues.size() == 0) {
					logger.info("###NO TRANSACTIONS FOUND###");
					
					System.exit(0);
					
				
				} else {
					
					counter = counter + 1;
					logger.info("Transaction Found..............");
					jdbcTemplate.execute("update CBCoopContraCogsInvoiceRequest set status='INPROGRESS' where id='"
							+ InputDataValues.get(0).getId() + "'\r\n" + "");

					logger.info("Id INPORGRESS: " + InputDataValues.get(0).getId());

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(InputDataValues.get(0).getVendorId(), page,
							InputDataValues.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();
					
					if (loginStatus.getRight()) {
						
						
						Path downloadPath = Paths.get("C:\\PalyWrightFile");
						try {
							coopprocess.processPage(  page,downloadPath, InputDataValues.get(0).getVendorName(),InputDataValues.get(0).getStartDate(),InputDataValues.get(0).getEndDate(),InputDataValues.get(0).getVendorId(),InputDataValues.get(0).getId());
							jdbcTemplate.execute("update CBCoopContraCogsInvoiceRequest set status='COMPLETED' where id='"
									+ InputDataValues.get(0).getId() + "'\r\n" + "");
						} catch (DataAccessException e) {
							// TODO Auto-generated catch block
							
							logger.info("update CBCoopContraCogsInvoiceRequest set status='ERROR' where id='"+InputDataValues.get(0).getId() + "'\r\n"+"");

							jdbcTemplate.execute("update CBCoopContraCogsInvoiceRequest set status='ERROR' where id='"+InputDataValues.get(0).getId()+"'\r\n"+"");

							
							e.printStackTrace();
						}
					}
					else {
						

						logger.info("update CBCoopContraCogsInvoiceRequest set status='VC Access Not Found' where id='"+InputDataValues.get(0).getId() + "'\r\n"+"");

						jdbcTemplate.execute("update CBCoopContraCogsInvoiceRequest set status='VC Access Not Found' where id='"+InputDataValues.get(0).getId()+"'\r\n"+"");

						
					}

				}

			}
		}

	}
}
