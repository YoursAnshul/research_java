package com.dimetyd.bot.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.ShipmentBotInput;
import com.dimetyd.bot.model.Vendor;
import com.dimetyd.bot.process.ShipmentDataDetailPage;
import com.dimetyd.bot.process.ShipmentDataPage;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class InvoiceShipmentDataService {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	LoginVendorCentral loginObj;

	@Autowired
	ShipmentDataDetailPage process_stage2;

	@Autowired
	ShipmentDataPage process_stage1;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<>();
	private int counter = 0;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {
		if (GlobalSession.getGlobalSession().getName().equals("shipment_data")) {
			
			logger.info("Shipment Data ASN Update Bot Started...");

			List<String> list = List.of("--disable-webauthn", "--disable-features=PasswordlessLogin");
			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));
			Path downloadPath = Paths.get("C:\\java codes\\Forecast_DownloadFiles");

			// **Step 1: Update old data from POInvoiceInput**
			jdbcTemplate.execute(
					"UPDATE  `ShippmentBotInput`  SET  DataStatus='Pending' WHERE DataStatus IN('INPROGRESS','ERROR')");
			logger.info("Update ShippmentBotInput Bot Input");

			while (true) {
				try {
					String sql = "SELECT s.id, s.vendorId, s.vendorName FROM ShipmentBotQueue s JOIN Vendor v ON(s.vendorId=v.id) AND v.isPaused='N' WHERE s.status = 'PENDING' ORDER BY RAND() LIMIT 1\r\n"
							+ "";
					List<Vendor> requestList = jdbcTemplate.query(sql, (rs, rowNum) -> {
						Vendor jobDataObj = new Vendor();
						jobDataObj.setId(rs.getString("id"));
						jobDataObj.setVendorId(rs.getString("vendorId"));
						jobDataObj.setVendorName(rs.getString("vendorName"));
						return jobDataObj;
					});

					if (requestList.isEmpty()) {
						logger.info("No Data Found in Stage 1");
						sql = "SELECT s.id, s.ASN,s.vendorId,s.vendorName FROM ShippmentBotInput s JOIN Vendor v ON(s.vendorId=v.id) AND v.isPaused='N' WHERE DataStatus = 'PENDING' AND TRIM(ASN) != '' ORDER BY priority, RAND() LIMIT 1\r\n"
								+ "";
						List<ShipmentBotInput> stage2List = jdbcTemplate.query(sql, (rs, rowNum) -> {
							ShipmentBotInput shipmentBotInputObj = new ShipmentBotInput();
							shipmentBotInputObj.setId(rs.getLong("id"));
							shipmentBotInputObj.setaSN(rs.getString("ASN"));
							shipmentBotInputObj.setVendorId(rs.getString("vendorId"));
							shipmentBotInputObj.setVendorName(rs.getString("vendorName"));
							return shipmentBotInputObj;
						});

						if (stage2List.isEmpty()) {
							logger.info("No Data Found in Stage 2");
							System.exit(0);
						} else {
							processStage2(stage2List.get(0), browser, downloadPath);
						}
					} else {
						processStage1(requestList.get(0), browser, downloadPath);
					}
				} catch (Exception e) {
					logger.error("Error processing shipment data", e);
				}
			}
		}

		
	}

	private void processStage1(Vendor vendor, Browser browser, Path downloadPath) {
		counter++;
		jdbcTemplate.update("UPDATE ShipmentBotQueue SET STATUS='INPROGRESS' WHERE id=?", vendor.getId());

		Pair<Page, Boolean> loginStatus = loginObj.loginProcess(vendor.getVendorId(), page, vendor.getVendorName(),
				loginHashMap, counter, browser, context);
		page = loginStatus.getLeft();

		if (loginStatus.getRight()) {
			boolean status = process_stage1.processPage(page, vendor.getVendorId(), vendor.getVendorName(),
					downloadPath);
			jdbcTemplate.update("UPDATE ShipmentBotQueue SET STATUS=? WHERE id=?", status ? "COMPLETED" : "ERROR",
					vendor.getId());
		}
	}

	private void processStage2(ShipmentBotInput shipment, Browser browser, Path downloadPath) {
		counter++;
		jdbcTemplate.update("UPDATE ShippmentBotInput SET DataStatus='INPROGRESS' WHERE id=?", shipment.getId());

		Pair<Page, Boolean> loginStatus = loginObj.loginProcess(shipment.getVendorId(), page, shipment.getVendorName(),
				loginHashMap, counter, browser, context);
		page = loginStatus.getLeft();

		if (loginStatus.getRight()) {
			jdbcTemplate.update("DELETE FROM ShippmentDataOutput WHERE ASN=? AND vendorId=?", shipment.getaSN(),
					shipment.getVendorId());
			boolean status = process_stage2.processPage(page, shipment.getaSN(), shipment.getVendorId(), downloadPath);
			jdbcTemplate.update("UPDATE ShippmentBotInput SET DataStatus=? WHERE id=?", status ? "COMPLETED" : "ERROR",
					shipment.getId());
		} else {
			jdbcTemplate.update("UPDATE ShippmentBotInput SET DataStatus='ERROR' WHERE id=?", shipment.getId());
		}
	}
}
