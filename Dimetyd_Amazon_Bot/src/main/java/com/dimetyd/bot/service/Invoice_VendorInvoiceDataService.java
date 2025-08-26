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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.ProcessStatus;
import com.dimetyd.bot.model.ShortageJobData;
import com.dimetyd.bot.process.InvoiceDataPage;
import com.dimetyd.bot.process.InvoiceDataPage_newUI;
import com.dimetyd.bot.process.InvoiceSummaryNewUi;
import com.dimetyd.bot.process.InvoiceSummaryPage;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class Invoice_VendorInvoiceDataService {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	InvoiceDataPage_newUI process_stage2;

	@Autowired
	InvoiceSummaryNewUi process_stage1;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("invoice_vendorInvioceData")) {

			logger.info("Invoice Input_Output Update Bot Started...");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));
			
			// **Step 1: Update old data from POInvoiceInput**
			jdbcTemplate.execute("UPDATE POInvoiceInput SET STATUS='PENDING' WHERE STATUS IN('ERROR','InProgress')");
			logger.info("Update Invoice Bot Input");


			while (true) {
				try {
					String sql = "SELECT cr.id AS id, cr.vendorId, v.vendorName, cr.startDt, cr.endDt, cr.createdBy "
							+ "FROM input_POInvoiceInput cr JOIN Vendor v ON (cr.vendorId = v.id) "
							+ "WHERE cr.status = 'PENDING' AND v.isPaused = 'N' ORDER BY v.master_Crd_Id";

					List<ShortageJobData> catlogList = jdbcTemplate.query(sql, (rs, rowNum) -> {
						ShortageJobData res = new ShortageJobData();
						res.setVendorId(rs.getString("vendorId"));
						res.setVendorName(rs.getString("vendorName"));
						res.setId(rs.getLong("id"));
						res.setStartDate(rs.getString("startDt"));
						res.setEndDate(rs.getString("endDt"));
						res.setCreatedBy(rs.getInt("createdBy"));
						return res;
					});

					Path downloadPath = Paths.get("C:\\java codes\\Forecast_DownloadFiles");
					if (catlogList.isEmpty()) {
						logger.info("No Data Found in Stage 1");

						sql = "SELECT i.id, i.vendorId,i.invoiceNumber,i.retry ,v.vendorName,v.jobPriority AS priority "
								+ "FROM POInvoiceInput i JOIN Vendor v ON (i.vendorId= v.id) "
								+ "WHERE i.Status = 'PENDING' AND v.isPaused = 'N' ORDER BY RAND() LIMIT 1";

						List<ShortageJobData> stage2 = jdbcTemplate.query(sql, (rs, rowNum) -> {
							ShortageJobData res = new ShortageJobData();
							res.setVendorId(rs.getString("vendorId"));
							res.setVendorName(rs.getString("vendorName"));
							res.setId(rs.getLong("id"));
							res.setPoInvoice(rs.getString("invoiceNumber"));
							res.setRetry(rs.getInt("retry"));
							return res;
						});

						if (stage2.isEmpty()) {
							logger.info("No Data Found in Stage 2");
							System.exit(0);
						} else {

							counter = counter + 1;
							jdbcTemplate.update("UPDATE POInvoiceInput SET Status='INPROGRESS' WHERE id=?",
									stage2.get(0).getId());

							Pair<Page, Boolean> loginStatus = loginObj.loginProcess(stage2.get(0).getVendorId(), page,
									stage2.get(0).getVendorName(), loginHashMap, counter, browser, context);
							page = loginStatus.getLeft();
							boolean status = false;
							if (loginStatus.getRight()) {
								status = process_stage2.processPage(page, stage2.get(0).getPoInvoice(),
										stage2.get(0).getVendorId(), stage2.get(0).getVendorName(), downloadPath,
										context);
								if (status) {
									jdbcTemplate.update("UPDATE POInvoiceInput SET Status='COMPLETED' WHERE id= '"
											+ stage2.get(0).getId() + "'");
								} else {
									jdbcTemplate.update("UPDATE POInvoiceInput SET Status='ERROR' WHERE id= '"
											+ stage2.get(0).getId() + "'");
								}

							} else {
								jdbcTemplate.update("UPDATE POInvoiceInput SET DataStatus='ERROR' WHERE id= ?",
										stage2.get(0).getId());
							}
						}
					} else {

						counter = counter + 1;

						jdbcTemplate.update("UPDATE input_POInvoiceInput SET STATUS='INPROGRESS' WHERE id=?",
								catlogList.get(0).getId());

						Pair<Page, Boolean> loginStatus = loginObj.loginProcess(catlogList.get(0).getVendorId(), page,
								catlogList.get(0).getVendorName(), loginHashMap, counter, browser, context);
						page = loginStatus.getLeft();

						if (loginStatus.getRight()) {
							boolean status = process_stage1.processPage(page, catlogList.get(0).getId(),
									catlogList.get(0).getVendorId(), catlogList.get(0).getVendorName(),
									catlogList.get(0).getStartDate(), catlogList.get(0).getEndDate(),
									catlogList.get(0).getCreatedBy(), downloadPath);

							jdbcTemplate.update("UPDATE input_POInvoiceInput SET STATUS=? WHERE id=?",
									status ? "COMPLETED" : "ERROR", catlogList.get(0).getId());
						}
					}
				} catch (Exception e) {
					logger.error("Error processing invoices", e);
				}
			}
		}
	}
}
