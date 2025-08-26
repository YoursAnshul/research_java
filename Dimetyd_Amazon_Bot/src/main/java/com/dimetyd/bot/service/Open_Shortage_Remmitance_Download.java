package com.dimetyd.bot.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.dimetyd.bot.model.ShortageJobData;
import com.dimetyd.bot.process.OpenShortagePaymentDataUpdateProcess;
import com.dimetyd.bot.process.PaymentDataUpdationProcess;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.*;

import jakarta.annotation.PostConstruct;

@Service
public class Open_Shortage_Remmitance_Download {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private LoginVendorCentral loginObj;

	@Autowired
	private OpenShortagePaymentDataUpdateProcess paymentDataUpdationProcess;
	
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {
		if (GlobalSession.getGlobalSession().getName().equals("Open_Shortage_Remittance_download")) {

			Playwright playwright = Playwright.create();
			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

			BrowserContext context = null; // Create context without setting download path
			Page page = null;

		while (true) {
			try {
				StringBuilder sql = new StringBuilder();
				sql.append(
						"SELECT a.id,v.vendorName,a.`vendorId`,a.`startDate`,a.`endDate` "
								+ "FROM `CB_Open_Shortage_Dispute_Payment_Input` a "
								+ "JOIN Vendor v ON (a.vendorId=v.id) "
								+ "JOIN `Client` c ON (a.vendorId=c.vendorId) "
								+ "WHERE a.`status` = 'PENDING' AND v.`isPaused` = 'N' "
								+ " ORDER BY RAND()");

				List<ShortageJobData> jobList = this.jdbcTemplate.query(sql.toString(),
						new RowMapper<ShortageJobData>() {
							@Override
							public ShortageJobData mapRow(ResultSet rs, int rowNum) throws SQLException {
								ShortageJobData res = new ShortageJobData();
								res.setVendorId(rs.getString("vendorId"));
								res.setVendorName(rs.getString("vendorName"));
								res.setId(rs.getLong("id"));
								res.setStartDate(rs.getString("startDate"));
								res.setEndDate(rs.getString("endDate"));
								return res;
							}
						});

				if (jobList.isEmpty()) {
					logger.info("No Payment Update Jobs Found");
					Thread.sleep(2000);
					continue;
				}

				ShortageJobData job = jobList.get(0);
				jdbcTemplate.execute("UPDATE CB_Open_Shortage_Dispute_Payment_Input SET status='INPROGRESS' WHERE id='" + job.getId() + "'");

				Pair<Page, Boolean> loginStatus = loginObj.loginProcess(job.getVendorId(), page, job.getVendorName(), loginHashMap, counter, browser, context);
				page = loginStatus.getLeft();

				if (!loginStatus.getRight()) {
					logger.warn("Login failed for vendor: " + job.getVendorId());
					continue;
				}

				Path downloadPath = Paths.get("C:\\PalyWrightFile");

				boolean updated = paymentDataUpdationProcess.processPage(
						page,
						job.getId().intValue(),
						job.getVendorName(),
						job.getVendorId(),
						job.getStartDate(),
						job.getEndDate(),
						downloadPath
				);

				if (updated) {
					jdbcTemplate.execute("UPDATE CB_Open_Shortage_Dispute_Payment_Input SET status='COMPLETED' WHERE id='" + job.getId() + "'");
					logger.info("Payment data updated for vendor: " + job.getVendorId());
					
					String vendorId = job.getVendorId();

					String updateDisputeQuery = 
						"UPDATE CBClientDispute a " +
						"JOIN ( " +
						"  SELECT a.vendorId, a.newSubmittedDisputeId AS disputeId, b.disputeAmount, " +
						"         a.invoiceNumber AS submittedInvoiceNumber, DATE(b.disputeDate) AS disputeDate, " +
						"         GROUP_CONCAT(DISTINCT c.paymentNumber) AS paymentNumber, " +
						"         GROUP_CONCAT(DISTINCT DATE(c.paymentDate)) AS paymentDate, " +
						"         c.invoiceNumber AS remittanceInvoiceNumber, " +
						"         SUM(c.amountPaid), SUM(c.termsDiscountTaken), " +
						"         SUM(c.amountPaid + c.termsDiscountTaken) AS totalAmount " +
						"  FROM CB_Open_Shortage_Disputes_To_Submit a " +
						"  JOIN CBClientDispute b ON a.newSubmittedDisputeId = b.disputeId " +
						"  JOIN CBMissingRemmittnaceData c ON a.vendorId = c.vendorId " +
						"    AND c.invoiceNumber LIKE CONCAT('%', a.invoiceNumber, '%') " +
						"    AND c.paymentDate >= b.disputeDate " +
						"  WHERE b.requestStatus = 'Resolved' " +
						"    AND a.vendorId = '" + vendorId + "' " +
						"  GROUP BY a.invoiceNumber " +
						") AS z ON a.disputeId = z.disputeId AND a.vendorId = z.vendorId " +
						"SET a.paymentUniqueId = z.paymentNumber, " +
						"    a.paymentReceivedDate = z.paymentDate, " +
						"    a.recoveredAmount = z.totalAmount " +
						"WHERE a.vendorId = '" + vendorId + "' " +
						"  AND a.paymentUniqueId IS NULL";
					
					
			
					jdbcTemplate.execute(updateDisputeQuery);
					logger.info("CBClientDispute table updated for vendor: " + vendorId);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	}
}
