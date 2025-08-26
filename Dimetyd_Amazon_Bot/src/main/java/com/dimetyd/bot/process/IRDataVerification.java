package com.dimetyd.bot.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class IRDataVerification {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	EmailService Mailobj;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void checkJobStatus(String vendorId, String requestId, String jobStatus, String vendorName,
			String tableType) {

		logger.info("jobStatus: " + jobStatus);

		switch (jobStatus) {
		case "INPROGRESS": {
			String sql = "SELECT COUNT(*) FROM CbMissingInvoiceRequestDetails " + "WHERE CbMissingInvoiceRequestId ='"
					+ requestId + "' AND jobStatus != 'COMPLETED'";

			Long count = jdbcTemplate.queryForObject(sql, Long.class);

			logger.info("Inprogress or error count: " + count);

			if (count == 0) {
				sql = "INSERT IGNORE INTO CBMissingInvPORequest (PO, vendorId, requestId, createdDate, retry, uniqueKey, status) "
						+ "SELECT DISTINCT PO, vendorId, requestId, NOW(), 0, CONCAT(PO, vendorId, requestId), 'QUEUE' "
						+ "FROM CBMissingInvoiePODetails WHERE requestId = '" + requestId + "' AND vendorId = '"
						+ vendorId + "' " + "AND acceptedQuantity != 0 AND quantityReceived > 0";

				jdbcTemplate.update(sql.toString());

				sql = "UPDATE CbMissingInvoiceRequest SET jobStatus = 'QAPENDING' WHERE id = '" + requestId + "'";
				jdbcTemplate.update(sql);

			} else {
				String updateStatusQuery = "UPDATE CbMissingInvoiceRequestDetails SET `jobStatus`='PENDING' WHERE `jobStatus`\r\n"
						+ "IN('INPROGRESS','ERROR')AND  `CbMissingInvoiceRequestId` = " + requestId;
				jdbcTemplate.update(updateStatusQuery);
			}

			break;
		}

		case "QACOMPLETED": {
			String sql = "select COUNT(*) from CBMissingInvoiePODetails " + "WHERE vendorid = '" + vendorId
					+ "' AND requestId = '" + requestId + "' and `isCase` = 1";

			logger.info("sql: " + sql);

			Long caseCount = jdbcTemplate.queryForObject(sql, Long.class);

			logger.info("This vendor has case orders: " + caseCount);

			String countryCode = vendorName.substring(0, 2);

			if (countryCode.equalsIgnoreCase("US")) {
				if (caseCount > 0) {
					sql = "INSERT IGNORE INTO CBMissingInvoiceOutput "
							+ "(vendorId, requestId, PO, ASIN, POASIN, quantityReceived, quantityInvoiced, needToInvoicedQty, "
							+ "unitCost, currency, invoiceNumber, total, STATUS, vendorInvoicedQty, createdDate, ukey, poOrderDate) "
							+ "SELECT a.vendorId, requestId, a.PO, a.ASIN, CONCAT(a.PO, a.ASIN), "
							+ "IFNULL(quantityReceived * caseQty, 0), IFNULL(acceptedQuantity * caseQty, 0), "
							+ "IFNULL(quantityReceived * caseQty, 0) - IFNULL(invoicedQty, 0) AS needToInvoicedQty , "
							+ "unitCost / caseQty, currency, CONCAT(a.PO, '-', 'DTIR'), "
							+ "(IFNULL(quantityReceived * caseQty, 0) - IFNULL(invoicedQty, 0)) * (unitCost / caseQty), "
							+ "'QUEUE', IFNULL(invoicedQty, 0), NOW(), CONCAT(a.vendorId, a.PO, a.ASIN), po.Order_Date "
							+ "FROM CBMissingInvoiePODetails a "
							+ "LEFT JOIN CBMissingPOData po ON (a.PO = po.PO AND a.vendorId = po.vendorId) "
							+ " JOIN (SELECT vendorId, POASIN, PO, ASIN, SUM(Qty) AS invoicedQty FROM CBPODetailsFDReports"
							+ " WHERE  `POInvoice`  not like '%SCR' and FilePath NOT LIKE '%fixed.csv' GROUP BY POASIN) AS b ON (a.POASIN = b.POASIN) "
							+ "JOIN Vendor v ON (a.vendorId = v.id) " + "WHERE a.vendorId = '" + vendorId
							+ "' AND a.requestId = '" + requestId
							+ "' AND quantityReceived > 0 AND acceptedQuantity != 0 " + "HAVING needToInvoicedQty > 0";

					jdbcTemplate.update(sql);
				}

				else {
					sql = "INSERT IGNORE INTO CBMissingInvoiceOutput ( vendorId, requestId, PO, ASIN, POASIN, quantityReceived, quantityInvoiced, needToInvoicedQty, unitCost, currency, invoiceNumber, total, STATUS, `vendorInvoicedQty`, createdDate, ukey,`poOrderDate` ) "
							+ "SELECT a.vendorId, requestId, a.PO,  a.ASIN,  CONCAT(a.PO, a.ASIN) as POASIN, quantityReceived,  acceptedQuantity,  IFNULL(quantityReceived, 0) - IFNULL(invoicedQty, 0) AS needToInvoicedQty, unitCost, currency, CONCAT(a.PO, '-', 'IRDT') as invoiceNumber, ( IFNULL(quantityReceived, 0) - IFNULL(invoicedQty, 0) ) * unitCost as totalCost, 'QUEUE' as jonStatus,IFNULL(invoicedQty, 0) as vendorInvoicedQty, NOW(), CONCAT(a.vendorId, a.PO, a.ASIN) as UKey,po.`Order_Date` "
							+ "FROM CBMissingInvoiePODetails a "
							+ "left join `CBMissingPOData` po on (a.PO=po.PO and a.vendorId=po.vendorId) "
							+ " join (select vendorId ,POASIN,PO,ASIN,sum(`Qty`) as invoicedQty from `CBPODetailsFDReports`"
							+ " where `POInvoice`  not like '%SCR' and FilePath NOT LIKE '%fixed.csv' group by POASIN) as b on (a.POASIN= b.POASIN) "
							+ "JOIN Vendor v ON (a.vendorId = v.id) " + "WHERE a.vendorId = '" + vendorId
							+ "' AND a.requestId = '" + requestId
							+ "' and quantityReceived > 0 HAVING needToInvoicedQty > 0";

					jdbcTemplate.update(sql);

				}

			} else {
				if (caseCount > 0) {
					sql = "INSERT IGNORE INTO CBMissingInvoiceOutput "
							+ "(vendorId, requestId, PO, ASIN, POASIN, quantityReceived, quantityInvoiced, needToInvoicedQty, "
							+ "unitCost, currency, invoiceNumber, total, STATUS, vendorInvoicedQty, createdDate, ukey, poOrderDate) "
							+ "SELECT a.vendorId, requestId, a.PO, a.ASIN, CONCAT(a.PO, a.ASIN), "
							+ "IFNULL(quantityReceived * caseQty, 0), IFNULL(acceptedQuantity * caseQty, 0), "
							+ "IFNULL(quantityReceived * caseQty, 0) - IFNULL(invoicedQty, 0) AS needToInvoicedQty , "
							+ "unitCost / caseQty, currency, CONCAT(a.PO, '-', 'DTIR'), "
							+ "(IFNULL(quantityReceived * caseQty, 0) - IFNULL(invoicedQty, 0)) * (unitCost / caseQty), "
							+ "'QUEUE', IFNULL(invoicedQty, 0), NOW(), CONCAT(a.vendorId, a.PO, a.ASIN), po.Order_Date "
							+ "FROM CBMissingInvoiePODetails a "
							+ "LEFT JOIN CBMissingPOData po ON (a.PO = po.PO AND a.vendorId = po.vendorId) "
							+ " JOIN (SELECT vendorId, POASIN, PO, ASIN, SUM(Qty) AS invoicedQty FROM CBPODetailsFDReports"
							+ " WHERE  `POInvoice`  not like '%SCR' GROUP BY POASIN) AS b ON (a.POASIN = b.POASIN) "
							+ "JOIN Vendor v ON (a.vendorId = v.id) " + "WHERE a.vendorId = '" + vendorId
							+ "' AND a.requestId = '" + requestId
							+ "' AND quantityReceived > 0 AND acceptedQuantity != 0 " + "HAVING needToInvoicedQty > 0";

					jdbcTemplate.update(sql);
				}

				else {
					sql = "INSERT IGNORE INTO CBMissingInvoiceOutput ( vendorId, requestId, PO, ASIN, POASIN, quantityReceived, quantityInvoiced, needToInvoicedQty, unitCost, currency, invoiceNumber, total, STATUS, `vendorInvoicedQty`, createdDate, ukey,`poOrderDate` ) "
							+ "SELECT a.vendorId, requestId, a.PO,  a.ASIN,  CONCAT(a.PO, a.ASIN) as POASIN, quantityReceived,  acceptedQuantity,  IFNULL(quantityReceived, 0) - IFNULL(invoicedQty, 0) AS needToInvoicedQty, unitCost, currency, CONCAT(a.PO, '-', 'IRDT') as invoiceNumber, ( IFNULL(quantityReceived, 0) - IFNULL(invoicedQty, 0) ) * unitCost as totalCost, 'QUEUE' as jonStatus,IFNULL(invoicedQty, 0) as vendorInvoicedQty, NOW(), CONCAT(a.vendorId, a.PO, a.ASIN) as UKey,po.`Order_Date` "
							+ "FROM CBMissingInvoiePODetails a "
							+ "left join `CBMissingPOData` po on (a.PO=po.PO and a.vendorId=po.vendorId) "
							+ " join (select vendorId ,POASIN,PO,ASIN,sum(`Qty`) as invoicedQty from `CBPODetailsFDReports`"
							+ " where `POInvoice`  not like '%SCR' group by POASIN) as b on (a.POASIN= b.POASIN) "
							+ "JOIN Vendor v ON (a.vendorId = v.id) " + "WHERE a.vendorId = '" + vendorId
							+ "' AND a.requestId = '" + requestId
							+ "' and quantityReceived > 0 HAVING needToInvoicedQty > 0";

					jdbcTemplate.update(sql);

				}

			}

			sql = "UPDATE CbMissingInvoiceRequest SET jobStatus = 'CREATING_INVOICE_PENDING' WHERE id = '" + requestId
					+ "'";
			jdbcTemplate.update(sql);

			String newjobStatus = "CREATING_INVOICE_PENDING";

			//sendJobStatusEmail(vendorId, requestId, vendorName, newjobStatus);

			break;
		}

		case "CREATING_INVOICE_PENDING": {
			// function added out of this

			break;
		}

		case "CREATING_INVOICE": {

			if (tableType.equalsIgnoreCase("MainType")) {
				String sql = "SELECT COUNT(*) FROM CBMissingInvoiceOutput WHERE vendorId = '" + vendorId
						+ "' AND requestId ='" + requestId + "' " + "AND STATUS NOT IN ('COMPLETED', 'QUEUE')";

				Long rowCount = jdbcTemplate.queryForObject(sql, Long.class);

				logger.info("Current running invoic count: " + rowCount);

				if (rowCount == 0) {
					sql = "SELECT COUNT(*) FROM CBMissingInvoiceOutput WHERE vendorId = '" + vendorId
							+ "' AND requestId ='" + requestId + "'"
							+ "AND STATUS  IN ('COMPLETED') AND updateInvoiceStatus != 'COMPLETED'";

					Long invoiceUpdateCount = jdbcTemplate.queryForObject(sql, Long.class);

					logger.info("Update invoice data count: " + invoiceUpdateCount);

					if (invoiceUpdateCount == 0) {

						String invoiceSumbittedAmount = " UPDATE CbMissingInvoiceRequest SET `invoiceSumbittedAmount` =   "
								+ "(SELECT SUM(`total`) FROM `CBMissingInvoiceOutput` WHERE vendorId = '" + vendorId
								+ "' " + "AND STATUS = 'COMPLETED' AND requestId = '" + requestId + "') WHERE  id = '"
								+ requestId + "'";
						jdbcTemplate.update(invoiceSumbittedAmount);

						String countOfInvoice = "UPDATE CbMissingInvoiceRequest Set `countOfInvoice` =  (Select COUNT(Distinct(`invoiceNumber`)) "
								+ "From `CBMissingInvoiceOutput` Where vendorId = '" + vendorId
								+ "' AND STATUS = 'COMPLETED' AND requestId ='" + requestId + "') " + "WHERE id = '"
								+ requestId + "'";
						jdbcTemplate.update(countOfInvoice);

						String invoiceCreateDate = "  UPDATE CbMissingInvoiceRequest SET `invoiceCreateDate` =    (SELECT DATE(MIN(DISTINCT(`invoiceCreatedDate`))) FROM `CBMissingInvoiceOutput` WHERE vendorId = '"
								+ vendorId + "' AND STATUS = 'COMPLETED' " + "AND requestId ='" + requestId
								+ "') WHERE id = '" + requestId + "'";
						jdbcTemplate.update(invoiceCreateDate);

						String updatePaymentDueDate = " UPDATE CbMissingInvoiceRequest SET `expectedPaymentDueDate` =   (SELECT MAX(`paymentDueDate`) FROM `CBMissingInvoiceOutput` WHERE vendorId = '"
								+ vendorId + "' AND " + "STATUS = 'COMPLETED' AND requestId = '" + requestId
								+ "') WHERE  id = '" + requestId + "'";
						jdbcTemplate.update(updatePaymentDueDate);

						String updateCronjobTriggerDate = "  UPDATE `CbMissingInvoiceRequest` SET `cronTriggerDate` =  DATE_ADD(expectedPaymentDueDate, INTERVAL 15 DAY)  WHERE  id = '"
								+ requestId + "'";
						jdbcTemplate.update(updateCronjobTriggerDate);

						String updateInvoiceStatus = "UPDATE `CbMissingInvoiceRequest` SET `jobStatus` = 'QUEUED_PAYMENTS' , batchPaymentDueDate = now()  WHERE id = '"
								+ requestId + "'";
						jdbcTemplate.update(updateInvoiceStatus);

						// No processing needed
					} else {
						sql = "UPDATE CBMissingInvoiceOutput SET updateInvoiceStatus = 'PENDING' WHERE  vendorId  = '"
								+ vendorId + "' AND requestId = '" + requestId + "'"
								+ " AND `status`  = 'COMPLETED' and updateInvoiceStatus IN ('ERROR','INPROGRESS')";

						jdbcTemplate.update(sql);
					}

				} else {
					sql = "UPDATE CBMissingInvoiceOutput SET STATUS = 'PENDING' WHERE  vendorId  = '" + vendorId
							+ "' AND requestId = '" + requestId + "'" + " AND  STATUS IN ('ERROR','INPROGRESS') ";

					jdbcTemplate.update(sql);
				}
				break;
			}
			else
			{

				String sql = "SELECT COUNT(*) FROM CBMissingInvoiceOutput WHERE vendorId = '" + vendorId
						+ "' AND splitRequestId ='" + requestId + "' " + "AND STATUS NOT IN ('COMPLETED', 'QUEUE')";

				Long rowCount = jdbcTemplate.queryForObject(sql, Long.class);

				logger.info("Current running invoic count: " + rowCount);

				if (rowCount == 0) {
					sql = "SELECT COUNT(*) FROM CBMissingInvoiceOutput WHERE vendorId = '" + vendorId
							+ "' AND splitRequestId ='" + requestId + "'"
							+ "AND STATUS  IN ('COMPLETED') AND updateInvoiceStatus != 'COMPLETED'";

					Long invoiceUpdateCount = jdbcTemplate.queryForObject(sql, Long.class);

					logger.info("Update invoice data count: " + invoiceUpdateCount);

					if (invoiceUpdateCount == 0) {

						String invoiceSumbittedAmount = " UPDATE CbMissingInvoiceRequestSplit SET `invoiceSumbittedAmount` =   "
								+ "(SELECT SUM(`total`) FROM `CBMissingInvoiceOutput` WHERE vendorId = '" + vendorId
								+ "' " + "AND STATUS = 'COMPLETED' AND splitRequestId = '" + requestId + "') WHERE  id = '"
								+ requestId + "'";
						jdbcTemplate.update(invoiceSumbittedAmount);

						String countOfInvoice = "UPDATE CbMissingInvoiceRequestSplit Set `countOfInvoice` =  (Select COUNT(Distinct(`invoiceNumber`)) "
								+ "From `CBMissingInvoiceOutput` Where vendorId = '" + vendorId
								+ "' AND STATUS = 'COMPLETED' AND splitRequestId ='" + requestId + "') " + "WHERE id = '"
								+ requestId + "'";
						jdbcTemplate.update(countOfInvoice);

						String invoiceCreateDate = "  UPDATE CbMissingInvoiceRequestSplit SET `invoiceCreateDate` =    (SELECT DATE(MIN(DISTINCT(`invoiceCreatedDate`))) FROM `CBMissingInvoiceOutput` WHERE vendorId = '"
								+ vendorId + "' AND STATUS = 'COMPLETED' " + "AND splitRequestId ='" + requestId
								+ "') WHERE id = '" + requestId + "'";
						jdbcTemplate.update(invoiceCreateDate);

						String updatePaymentDueDate = " UPDATE CbMissingInvoiceRequestSplit SET `expectedPaymentDueDate` =   (SELECT MAX(`paymentDueDate`) FROM `CBMissingInvoiceOutput` WHERE vendorId = '"
								+ vendorId + "' AND " + "STATUS = 'COMPLETED' AND splitRequestId = '" + requestId
								+ "') WHERE  id = '" + requestId + "'";
						jdbcTemplate.update(updatePaymentDueDate);

						String updateCronjobTriggerDate = "  UPDATE `CbMissingInvoiceRequestSplit` SET `cronTriggerDate` =  DATE_ADD(expectedPaymentDueDate, INTERVAL 15 DAY)  WHERE  id = '"
								+ requestId + "'";
						jdbcTemplate.update(updateCronjobTriggerDate);

						String updateInvoiceStatus = "UPDATE `CbMissingInvoiceRequestSplit` SET `jobStatus` = 'QUEUED_PAYMENTS' , batchPaymentDueDate = now()  WHERE id = '"
								+ requestId + "'";
						jdbcTemplate.update(updateInvoiceStatus);

						// No processing needed
					} else {
						sql = "UPDATE CBMissingInvoiceOutput SET updateInvoiceStatus = 'PENDING' WHERE  vendorId  = '"
								+ vendorId + "' AND splitRequestId = '" + requestId + "'"
								+ " AND `status`  = 'COMPLETED' and updateInvoiceStatus IN ('ERROR','INPROGRESS')";

						jdbcTemplate.update(sql);
					}

				} else {
					sql = "UPDATE CBMissingInvoiceOutput SET STATUS = 'PENDING' WHERE  vendorId  = '" + vendorId
							+ "' AND requestId = '" + requestId + "'" + " AND  STATUS IN ('ERROR','INPROGRESS') ";

					jdbcTemplate.update(sql);
				}
				break;
			
				
			}

		}
		}

	}

	private void sendJobStatusEmail(String vendorId, String jobId, String vendorName, String jobStatus) {
		logger.info("Sending job email: " + vendorName);

		// Construct the email body
		StringBuilder emailBody = new StringBuilder();
		emailBody.append("Hello,\n\nBelow jobs are ready to review\n\n");
		emailBody.append("IR Job Status,\n\n");
		emailBody.append("vendorName\tvendorId\tJob Id\tStatus\n");

		emailBody.append(vendorName).append("\t").append(vendorId).append("\t").append(jobId).append("\t")
				.append(jobStatus).append("\n");

		emailBody.append("\nRegards,\nDimetyd Team.");

		// Email details
		String[] toRecipients = { "kchawla@threecolts.com" };
		String[] ccRecipients = { "sthaker@threecolts.com", "rbhattacharya@threecolts.com", "spandey@threecolts.com",
				"asingh@threecolts.com", "gbhandari@threecolts.com", "pshinde@threecolts.com" };
		String subject = "Invoice Reconciliation job to review";

		// Send email
		Mailobj.sendMailToClientWithAttchment(toRecipients, ccRecipients, subject, emailBody.toString(), null);
		logger.info("Job status email sent successfully for vendor: " + vendorId);
	}
}
