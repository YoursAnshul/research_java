package com.dimetyd.bot.process;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.CoopInvoice;
import com.dimetyd.bot.model.DisputeRquestData;
import com.dimetyd.bot.model.RequestDetailsData;
import com.dimetyd.bot.model.SummaryData;

@Component
public class DataUpdation {

	@Autowired
	JdbcTemplate jdbcTemplate;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public boolean UpdateData_QuantityMismatch(String in_vendorId, String in_requestId, String in_requestType,
			String jobType) {
		logger.info("----------------REQUEST TYPE : QTY MISMATCH----------------");

		String DeleteQtyMismatchDataQuery = "DELETE CBQuantityMismatch FROM CBQuantityMismatch INNER JOIN CBAgreementRequestDetails c1 "
				+ "ON (CBQuantityMismatch.agreementID = c1.agreementId ) INNER JOIN CBRequest c2 ON (c1.requestId = c2.id) "
				+ "WHERE c2.vendorId = '" + in_vendorId + "'  AND c1.status != 'DELETED' AND c2.id = '" + in_requestId
				+ "'";
		jdbcTemplate.execute(DeleteQtyMismatchDataQuery);
		logger.info("Deleted old Shipment QTY_MISMATCH Data....");

		String InsertQtyMismatchDataQuery = "INSERT IGNORE INTO `CBQuantityMismatch` (`vendorId`,`agreementID`,`PO`,ASIN,`POASIN`,`unitCost`,`amazonBilledQty`,`ShippedQty`,`excessUnitBilled`,`excessNetReceipt`,`overbilledAmount`,uniqueKey,currency) \r\n"
				+ "SELECT * FROM (SELECT i.vendorId, i.`agreementID` AS agreementID, i.PO, i.ASIN, i.POASIN, \r\n"
				+ "CASE WHEN i.unitCost IS NULL OR i.unitCost = 0 THEN p.unitCost ELSE i.unitCost END AS unitCost, i.Qty AS AmazonInvoiceQty, \r\n"
				+ "SUM(IFNULL(p.receivedQuantity, 0)) AS VendorInvoiceQty, ((i.Qty) - SUM(IFNULL(p.receivedQuantity, 0))) AS AmazonExcessQty, (((i.Qty) - SUM(IFNULL(p.receivedQuantity, 0))) * CASE WHEN i.unitCost IS NULL OR i.unitCost = 0 THEN p.unitCost ELSE i.unitCost END) AS ExcessPrice, \r\n"
				+ "(((i.Qty) - SUM(IFNULL(p.receivedQuantity, 0))) * (CASE WHEN i.unitCost IS NULL OR i.unitCost = 0 THEN p.unitCost ELSE i.unitCost END) * cr.`percentage` / 100) AS OverbilledRebate, \r\n"
				+ "CONCAT(i.`agreementID`, i.POASIN) AS uniqueKey, i.currency FROM \r\n"
				+ "(SELECT agreementID, vendorId, PO, ASIN, POASIN, SUM(Qty) AS Qty, unitCost, currency FROM \r\n"
				+ "(SELECT a.agreementID, vendorId, PO, ASIN, POASIN, Qty,\r\n"
				+ " CASE WHEN `invoiceType` = 'Revised' THEN `netReceipts` / `newInvoiceQty` ELSE `netReceipts` / `Qty` END AS unitCost, currency\r\n"
				+ "  FROM `CBAgreementInvoiceDetailsV2` a JOIN `CBAgreementRequestDetails` b ON (a.`agreementID` = b.`agreementId`)\r\n"
				+ "   WHERE CHAR_LENGTH(PO) = '8' AND vendorId IN ('" + in_vendorId + "') \r\n"
				+ "   AND `transactionType` IN ('Distributor Shipment','Amazon Payments','Amazon Payables','Expected Amazon Payables') \r\n"
				+ "   AND b.requestId = '" + in_requestId
				+ "' AND Qty != 0) AS z GROUP BY POASIN, agreementID) AS i \r\n"
				+ "   JOIN CBPODetailsFDReports p ON (i.POASIN = p.POASIN) \r\n"
				+ "   JOIN `CBAgreementRequestDetails` cr ON (i.agreementID = cr.agreementID)\r\n"
				+ "    JOIN CBRequest c1 ON (c1.id = cr.requestId AND c1.RequestType = 'QTY_MISMATCH')\r\n"
				+ "     WHERE CHAR_LENGTH(i.PO) = '8' AND c1.status != 'DELETED' AND cr.isActive = 'active' \r\n"
				+ "     AND p.`receivedQuantity` IS NOT NULL AND i.vendorId IN ('" + in_vendorId + "') \r\n"
				+ "GROUP BY i.POASIN, i.`agreementID`) AS z WHERE AmazonExcessQty != 0;";
		int insertedData = jdbcTemplate.update(InsertQtyMismatchDataQuery);
		logger.info("Inserted QTY_MISMATCH Data : " + insertedData);

		String CoopInvoiceCurrencyQuery = "SELECT DISTINCT vendorId,currency FROM CBAgreementInvoice WHERE vendorId = '"
				+ in_vendorId + "'";
		List<CoopInvoice> VendorCurrencyList = this.jdbcTemplate.query(CoopInvoiceCurrencyQuery.toString(),
				new RowMapper<CoopInvoice>() {
					@Override
					public CoopInvoice mapRow(ResultSet rs, int rowNum) throws SQLException {

						CoopInvoice ci = new CoopInvoice();

						ci.setVendorId(rs.getString("vendorId"));
						ci.setCurrency(rs.getString("currency"));
						return ci;
					}
				}, new Object[] {});

		for (CoopInvoice CurrencyList : VendorCurrencyList) {
			String VendorCurrency = CurrencyList.getCurrency();
			String SummaryTableQuery = "SELECT * FROM `CBSummary` WHERE vendorId = '" + in_vendorId
					+ "' AND `currency` = '" + VendorCurrency + "' AND `type` = '" + in_requestType + "'";
			List<SummaryData> SummaryDetailsList = this.jdbcTemplate.query(SummaryTableQuery.toString(),
					new RowMapper<SummaryData>() {
						@Override
						public SummaryData mapRow(ResultSet rs, int rowNum) throws SQLException {

							SummaryData summarydata = new SummaryData();

							summarydata.setId(rs.getString("id"));
							return summarydata;
						}
					}, new Object[] {});
			if (SummaryDetailsList.size() == 0) {
				String InsertSummaryNewRow = "INSERT  IGNORE INTO `CBSummary` (`vendorId`,`type`,`currency`,uniqueKey) VALUES ('"
						+ in_vendorId + "'," + "'" + in_requestType + "','" + VendorCurrency + "','" + in_vendorId
						+ in_requestType + VendorCurrency + "')";
				jdbcTemplate.execute(InsertSummaryNewRow);
				logger.info("Summary Table One New Row Inserted with Vendor Currency " + VendorCurrency);
			} else {
				logger.info("Already Inserted Summary Row Data For this Vendor Currency");
			}

			/*-----------UPDATE AGREEMENT SCANNED------------*/

			String UpdateAgreementScannedQuery = "UPDATE `CBSummary` SET `agreementScanned`=(SELECT IFNULL(COUNT(DISTINCT(cr.agreementId)),0) FROM CBAgreementRequestDetails cr "
					+ "JOIN `CBRequest` c ON (cr.requestId = c.id ) JOIN `CBAgreementInvoice` ci ON (cr.agreementId = ci.agreementId) WHERE   c.`RequestType` = '"
					+ in_requestType + "'" + " AND c.status != 'DELETED'  AND ci.currency = '" + VendorCurrency
					+ "' And c.vendorId In ('" + in_vendorId + "'))" + " WHERE `vendorId` = '" + in_vendorId
					+ "' AND TYPE =  '" + in_requestType + "' AND currency = '" + VendorCurrency + "'";
			jdbcTemplate.execute(UpdateAgreementScannedQuery);

			/*------------UPDATE INVOICE SCANNED-------------*/

			String UpdateInvoiceScannedQuery = "UPDATE `CBSummary` SET `invoiceScanned`= (SELECT IFNULL(COUNT(DISTINCT(ci.`invoiceNumber`)),0) FROM CBAgreementRequestDetails cr "
					+ "JOIN `CBRequest` c ON (cr.requestId = c.id ) JOIN `CBAgreementInvoice` ci ON (cr.agreementId = ci.agreementId)"
					+ " WHERE   c.`RequestType` = '" + in_requestType + "' "
					+ "AND c.status != 'DELETED'  AND c.vendorId IN ('" + in_vendorId + "') AND ci.currency = '"
					+ VendorCurrency + "') " + "WHERE `vendorId` = '" + in_vendorId + "' AND TYPE =  '" + in_requestType
					+ "' AND currency = '" + VendorCurrency + "'";
			jdbcTemplate.execute(UpdateInvoiceScannedQuery);

			/*----------UPDATE PO SCANNED SCANNED------------*/

			String UpdatePOScannedQuery = "UPDATE `CBSummary` SET `poScanned`=ifNULL(`oldPOScanned`,0)+ (SELECT IFNULL(COUNT(DISTINCT(PO)),0) "
					+ "FROM `CBAgreementInvoiceDetails` i"
					+ " LEFT JOIN CBAgreementRequestDetails cr ON (i.agreementId= cr.agreementId) JOIN `CBRequest` c ON (cr.requestId = c.id ) "
					+ "WHERE c.`RequestType` = '" + in_requestType + "'  AND c.status != 'DELETED' AND c.vendorId IN ('"
					+ in_vendorId + "') AND " + "i.currency = '" + VendorCurrency + "') WHERE `vendorId` = '"
					+ in_vendorId + "' AND TYPE =  '" + in_requestType + "' " + "AND currency = '" + VendorCurrency
					+ "'";
			jdbcTemplate.execute(UpdatePOScannedQuery);

			/*------------UPDATE PERIOD COVERED FROM----------*/

			String UpdatePerioedCoveredFromQuery = "UPDATE `CBSummary`  SET `periodCoveredFrom` = (SELECT MIN(`startDate`) FROM `CBAgreementInvoice` ci "
					+ "LEFT JOIN `CBAgreementRequestDetails` cd ON (ci.agreementId=cd.`agreementId`) LEFT JOIN `CBRequest` cr ON (cd.`requestId`=cr.id) "
					+ "WHERE cr.`RequestType` = '" + in_requestType
					+ "' AND startDate NOT IN ( '0001-01-01 00:00:00','0000-00-00 00:00:00')  "
					+ "AND cr.`vendorId` = '" + in_vendorId + "') Where `vendorId` = '" + in_vendorId
					+ "' AND TYPE =  '" + in_requestType + "'";
			jdbcTemplate.execute(UpdatePerioedCoveredFromQuery);

			/*-----------UPDATE PERIOD COVERED TO-------------*/

			String UpdatePeriodCoveredToQuery = "UPDATE `CBSummary`  SET `periodCoveredTo` = (SELECT MAX(`endDate`) FROM `CBAgreementInvoice` ci "
					+ "LEFT JOIN `CBAgreementRequestDetails` cd ON (ci.agreementId=cd.`agreementId`) LEFT JOIN `CBRequest` cr ON (cd.`requestId`=cr.id) "
					+ "WHERE cr.`RequestType` = '" + in_requestType
					+ "' AND startDate NOT IN( '0001-01-01 00:00:00','0000-00-00 00:00:00')  " + "AND cr.`vendorId` = '"
					+ in_vendorId + "') Where `vendorId` = '" + in_vendorId + "' AND TYPE =  '" + in_requestType + "'";
			jdbcTemplate.execute(UpdatePeriodCoveredToQuery);

			/*---------UPDATE OVER BILLED IDENTIFIED---------*/

			String UpdateOverbilledIdentifiedQuery = "UPDATE `CBSummary` SET overbillIdentified =(SELECT convertCurrency(Z.currency,'"
					+ VendorCurrency + "',"
					+ "SUM(overbilledAmount))  FROM (SELECT cr.agreementId AS Agreement, SUM(`billedAmount`) AS BilledAmount, i.currency  FROM `CBAgreementInvoice` i  "
					+ "RIGHT JOIN CBAgreementRequestDetails cr ON (i.agreementId= cr.agreementId) JOIN `CBRequest` c ON (cr.requestId = c.id ) "
					+ "WHERE c.`RequestType` = '" + in_requestType
					+ "' AND c.status != 'DELETED'    AND c.vendorId IN ( '" + in_vendorId + "' )  "
					+ "AND i. currency = '" + VendorCurrency
					+ "' GROUP BY cr.agreementID) AS Z LEFT JOIN `CBQuantityMismatch` qc "
					+ "ON (Z.Agreement= qc.agreementId)) WHERE `vendorId` = '" + in_vendorId + "' AND TYPE =  '"
					+ in_requestType + "' AND currency = '" + VendorCurrency + "'";
			jdbcTemplate.execute(UpdateOverbilledIdentifiedQuery);

			String currenyMissmacth = "INSERT IGNORE INTO CurrencyMissmatchDetails (`vendorId`,`agreementID`,`invoiceNumber`,`PO`,`ASIN`,`POASIN`,`Qty`,`transactionType`,`netReceipts`,`rebate`,`distributor`, AgreementCurrency,`productGroup`,`category`,`subCategory`,`manufacturer`,`poCurrency`,`uniqueKey`) "
					+ "SELECT `vendorId`,`agreementID`,`invoiceNumber`,`PO`,`ASIN`,`POASIN`,`Qty`,`transactionType`,`netReceipts`,`rebate`,`distributor`,"
					+ "`currency` AS AgreementCurrency,`productGroup`,`category`,`subCategory`,`manufacturer`,`poCurrency` , `uniqueKey`"
					+ " FROM `CBAgreementInvoiceDetailsV2` WHERE VendorId = '" + in_vendorId
					+ "' AND `currency` != `poCurrency` "
					+ "AND agreementId IN (SELECT DISTINCT agreementId FROM `CBAgreementRequestDetails`   WHERE requestId = '"
					+ in_requestId + "')";

			jdbcTemplate.execute(currenyMissmacth);

			String RequestDetailsQuery = "SELECT agreementId FROM `CBAgreementRequestDetails` WHERE  requestId = '"
					+ in_requestId + "'";

			List<RequestDetailsData> RequestDetailsAgreementList = this.jdbcTemplate
					.query(RequestDetailsQuery.toString(), new RowMapper<RequestDetailsData>() {
						@Override
						public RequestDetailsData mapRow(ResultSet rs, int rowNum) throws SQLException {

							RequestDetailsData RequestDetailsData = new RequestDetailsData();

							RequestDetailsData.setAgreementId(rs.getString("agreementId"));
							return RequestDetailsData;
						}
					}, new Object[] {});

			for (RequestDetailsData agreementsList : RequestDetailsAgreementList) {

				String AgreementId = agreementsList.getAgreementId();
				if (RequestDetailsAgreementList.size() != 0) {
					String DisputeRequestQuery = "SELECT * FROM `CBClientDispute` a JOIN `CBDisputeDetail` b ON (a.disputeId=b.disputeId)"
							+ " WHERE  b.`agreementNumber` = '" + AgreementId + "' AND a.vendorId = '" + in_vendorId
							+ "' AND a.status = 'ACTIVE'";

					List<DisputeRquestData> DisputeRequestList = this.jdbcTemplate.query(DisputeRequestQuery.toString(),
							new RowMapper<DisputeRquestData>() {
								@Override
								public DisputeRquestData mapRow(ResultSet rs, int rowNum) throws SQLException {

									DisputeRquestData disputeRequestsData = new DisputeRquestData();

									disputeRequestsData.setId(rs.getString("id"));
									return disputeRequestsData;
								}
							}, new Object[] {});
					if (DisputeRequestList.size() == 0) {
						jdbcTemplate.execute(
								"INSERT IGNORE INTO CBPreviousRefundDetails (`vendorId`,`agreementId`,`refundInvoice`,`remittanceInvoiceDate`,"
										+ "`originalInvoiceDate`,`invoiceNumber`,`amountPaid`,`billedAmount`,`Ukey`) SELECT z.vendorId,agreementId,z.invoiceNumber AS RefundInvoice,"
										+ "z.invoiceDate AS RemittanceInvoiceDate,a.startDate AS OriginalInvoieDate,a.invoiceNumber,z.amountPaid,a.billedAmount, CONCAT(z.vendorId,agreementId,z.invoiceNumber) "
										+ "FROM (SELECT vendorId,invoiceNumber,invoiceDate,amountPaid,termsDiscountTaken, REPLACE(invoiceNumber,SUBSTRING(invoiceNumber, LENGTH(invoiceNumber)-1,2),'') AS originalInvoice "
										+ "FROM CBShortageReconciliation WHERE vendorId = '" + in_vendorId
										+ "' AND invoiceType LIKE 'CoOp Refund') AS z  JOIN CBAgreementInvoice a ON (z.originalInvoice=a.invoiceNumber)"
										+ " WHERE a.agreementId = '" + AgreementId + "'");

						logger.info("Inserting Previous Refund Data in Table");

					}

					if (jobType.equalsIgnoreCase("New")) {

						String hideAgreement = "SELECT COUNT(Agreement) AS count FROM (SELECT Agreement, convertCurrency(Z1.currency, 'USD', BilledAmount) AS netReceipts, SUM(convertCurrency(Z1.currency, 'USD', excessNetReceipt)) AS rebate, "
								+ "SUM(convertCurrency(Z1.currency, 'USD', overbilledAmount)) AS overbilledRebateWithPercent, IFNULL(SUM(convertCurrency(currency, 'USD', UnderBilledAmount)),"
								+ " 0) AS UnderBilledAmount, IFNULL(SUM(convertCurrency(currency, 'USD', overbilledAmount)), 0) + IFNULL(SUM(convertCurrency(currency, 'USD', "
								+ "UnderBilledAmount)), 0) AS netOff, Z1.currency FROM (SELECT companyName, Agreement, BilledAmount, excessNetReceipt, "
								+ "CASE WHEN overbilledAmount > 0 THEN overbilledAmount ELSE 0 END AS overbilledAmount, Z.currency, "
								+ "CASE WHEN overbilledAmount < 0 THEN overbilledAmount ELSE 0 END AS UnderBilledAmount "
								+ "FROM (SELECT c1.companyName, cr.agreementId AS Agreement, SUM(billedAmount) AS BilledAmount, i.currency "
								+ "FROM CBAgreementInvoice i RIGHT JOIN CBAgreementRequestDetails cr ON i.agreementId = cr.agreementId "
								+ "JOIN CBRequest c ON cr.requestId = c.id JOIN `Client` c1 ON c.vendorId = c1.vendorId "
								+ "WHERE c.RequestType = 'QTY_MISMATCH' AND c.status != 'DELETED' "
								+ "AND c.vendorId IN ('" + in_vendorId + "') AND cr.agreementId = '" + AgreementId
								+ "' GROUP BY cr.agreementID) AS Z "
								+ "LEFT JOIN CBQuantityMismatch qc ON Z.Agreement = qc.agreementId) AS Z1 GROUP BY Agreement HAVING netOff < 0) AS z";
						
						logger.info(hideAgreement);
						List<Long> hideAgrremntlist = jdbcTemplate.query(hideAgreement.toString(), (rs, rowNum) -> rs.getLong("count"));
						Long agreementCount = hideAgrremntlist != null && hideAgrremntlist.size() > 0 ? hideAgrremntlist.get(0) : 0L;
						

						if (agreementCount > 0) {
							jdbcTemplate.execute("UPDATE `CBPreviousRefundDetails` SET `needToHide` = 'YES' "
									+ "WHERE vendorId = '" + in_vendorId + "' AND agreementId = '" + AgreementId + "'");
						}

					}

				}
			}

			try {
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT count(*) as totalCount FROM CBVendorLineScan WHERE vendorId = '" + in_vendorId + "'");
				jdbcTemplate.execute(sql.toString());
				logger.info(sql.toString());

				List<Long> lineScanned = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> rs.getLong("totalCount"));
				Long lineScannedCount = lineScanned != null && lineScanned.size() > 0 ? lineScanned.get(0) : 0L;

				if (lineScannedCount > 0) {
					sql = new StringBuilder();
					sql.append(
							"UPDATE CBVendorLineScan SET `lineScanned_live` = (SELECT COUNT(*) FROM CBAgreementInvoiceDetailsV2 WHERE vendorId = '"
									+ in_vendorId + "') " + "WHERE vendorId = '" + in_vendorId + "'");
					jdbcTemplate.execute(sql.toString());
					String message = String.format(
							" Update Line Scanned data successfully for vendor ID: %s on the page: Overbilling.",
							in_vendorId);
					logger.info(message);
				} else {
					sql = new StringBuilder();
					sql.append(
							"INSERT IGNORE INTO CBVendorLineScan  (`vendorId`,`lineScanned_live`) SELECT vendorId,COUNT(*) "
									+ "FROM CBAgreementInvoiceDetailsV2 WHERE vendorId = '" + in_vendorId + "'");
					jdbcTemplate.execute(sql.toString());
					String message = String.format(
							" Inserting Line Scanned data successfully for vendor ID: %s on the page: Overbilling.",
							in_vendorId);
					logger.info(message);
				}
				logger.info("Total line scanned saved successfully for vendorId: {}", in_vendorId);
			} catch (Exception e) {
				logger.error(e.toString());
			}

		}

		return true;
	}

	public boolean UpdateDataDropShip(String in_vendorId, String in_requestId, String in_requestType) {
		logger.info("----------------REQUEST TYPE : DROPSHIP----------------");

		// DATA SHOULD BE DELETE OR NOT INCORRECT CODE BEFORE INSERT
		String DeleteIncorrectCodetCheckData = "DELETE CBIncorrectCode FROM CBIncorrectCode INNER JOIN CBAgreementRequestDetails c1 "
				+ "ON (CBIncorrectCode.agreementID = c1.agreementId ) INNER JOIN CBRequest c2 ON (c1.requestId = c2.id) "
				+ "WHERE c2.vendorId = '" + in_vendorId + "'  AND c1.status != 'DELETED' AND c2.id = '" + in_requestId
				+ "'";
		jdbcTemplate.execute(DeleteIncorrectCodetCheckData);
		logger.info("Deleted old Incorrect Code(DROPSHIP) Data....");

		String InsertIncorrectCodeDataQuery = "INSERT IGNORE INTO `CBIncorrectCode` (`vendorId`,`agreementId`,`invoiceNumber`,`POASIN`,`PO`,"
				+ "`ASIN`,`distributor`,`netReceipt`,`rebate`,`currency`,`createdDate`,`uniqueKey`) SELECT ci.`vendorId`,ci.`agreementId`,"
				+ "ci.`invoiceNumber`,ci.`POASIN`,ci.`PO`,ci.`ASIN`,ci.`distributor`,ci.`netReceipts`,ci.`rebate`,ci.`currency`,ci.`createdDate`,"
				+ "ci.`uniqueKey` FROM `CBAgreementInvoiceDetails` ci JOIN `CBAgreementRequestDetails` cr ON (ci.`agreementId` = cr.`agreementId`) "
				+ "JOIN `CBRequest` c ON (c.id=cr.requestID) WHERE (PO LIKE '%DROPSHIP%' OR CHAR_LENGTH(PO)= 9) and "
				+ "c.`RequestType` = '" + in_requestType + "' AND cr.requestID='" + in_requestId
				+ "' AND  ci.vendorId = '" + in_vendorId + "'";
		int insertedData = jdbcTemplate.update(InsertIncorrectCodeDataQuery);
		logger.info("Inserted Incorrect Code(DROPSHIP) Data : " + insertedData);

		String CoopInvoiceCurrencyQuery = "SELECT DISTINCT vendorId,currency FROM CBAgreementInvoice WHERE vendorId = '"
				+ in_vendorId + "'";
		List<CoopInvoice> VendorCurrencyList = this.jdbcTemplate.query(CoopInvoiceCurrencyQuery.toString(),
				new RowMapper<CoopInvoice>() {
					@Override
					public CoopInvoice mapRow(ResultSet rs, int rowNum) throws SQLException {

						CoopInvoice ci = new CoopInvoice();

						ci.setVendorId(rs.getString("vendorId"));
						ci.setCurrency(rs.getString("currency"));
						return ci;
					}
				}, new Object[] {});

		for (CoopInvoice CurrencyList : VendorCurrencyList) {
			String VendorCurrency = CurrencyList.getCurrency();
			String SummaryTableQuery = "SELECT * FROM `CBSummary` WHERE vendorId = '" + in_vendorId
					+ "' AND `currency` = '" + VendorCurrency + "' AND `type` = '" + in_requestType + "'";
			List<SummaryData> SummaryDetailsList = this.jdbcTemplate.query(SummaryTableQuery.toString(),
					new RowMapper<SummaryData>() {
						@Override
						public SummaryData mapRow(ResultSet rs, int rowNum) throws SQLException {

							SummaryData summarydata = new SummaryData();

							summarydata.setId(rs.getString("id"));
							return summarydata;
						}
					}, new Object[] {});
			if (SummaryDetailsList.size() == 0) {
				String InsertSummaryNewRow = "INSERT  IGNORE INTO `CBSummary` (`vendorId`,`type`,`currency`,uniqueKey,`createdDate`) VALUES ('"
						+ in_vendorId + "'," + "'" + in_requestType + "','" + VendorCurrency + "','" + in_vendorId
						+ in_requestType + VendorCurrency + "',NOW())";
				jdbcTemplate.execute(InsertSummaryNewRow);
				logger.info("Summary Table One New Row Inserted with Vendor Currency " + VendorCurrency);
			} else {
				logger.info("Already Inserted Summary Row Data For this Vendor Currency");
			}

			/* UPDATE AGREEMENT SCANNED */

			String UpdateAgreementScannedQuery = "UPDATE `CBSummary` SET `agreementScanned`=(SELECT IFNULL(COUNT(DISTINCT(cr.agreementId)),0) FROM CBAgreementRequestDetails cr "
					+ "JOIN `CBRequest` c ON (cr.requestId = c.id ) JOIN `CBAgreementInvoice` ci ON (cr.agreementId = ci.agreementId) WHERE   c.`RequestType` = '"
					+ in_requestType + "'" + " AND c.status != 'DELETED'  AND ci.currency = '" + VendorCurrency
					+ "' And c.vendorId In ('" + in_vendorId + "'))" + " WHERE `vendorId` = '" + in_vendorId
					+ "' AND TYPE =  '" + in_requestType + "' AND currency = '" + VendorCurrency + "'";
			jdbcTemplate.execute(UpdateAgreementScannedQuery);

			/* UPDATE INVOICE SCANNED */

			String UpdateInvoiceScannedQuery = "UPDATE `CBSummary` SET `invoiceScanned`= (SELECT IFNULL(COUNT(DISTINCT(ci.`invoiceNumber`)),0) FROM CBAgreementRequestDetails cr "
					+ "JOIN `CBRequest` c ON (cr.requestId = c.id ) JOIN `CBAgreementInvoice` ci ON (cr.agreementId = ci.agreementId) WHERE   c.`RequestType` = '"
					+ in_requestType + "' " + "AND c.status != 'DELETED' AND c.vendorId IN ('" + in_vendorId
					+ "') AND ci.currency = '" + VendorCurrency + "') " + "WHERE `vendorId` = '" + in_vendorId
					+ "' AND TYPE =  '" + in_requestType + "' AND currency = '" + VendorCurrency + "'";
			jdbcTemplate.execute(UpdateInvoiceScannedQuery);

			/* UPDATE PO SCANNED SCANNED */

			String UpdatePOScannedQuery = "UPDATE `CBSummary` SET `poScanned`=ifNULL(`oldPOScanned`,0)+ (SELECT IFNULL(COUNT(DISTINCT(PO)),0) FROM `CBAgreementInvoiceDetails` i"
					+ " LEFT JOIN CBAgreementRequestDetails cr ON (i.agreementId= cr.agreementId) JOIN `CBRequest` c ON (cr.requestId = c.id ) "
					+ "WHERE c.`RequestType` = '" + in_requestType + "'  AND c.status != 'DELETED' AND c.vendorId IN ('"
					+ in_vendorId + "') AND " + "i.currency = '" + VendorCurrency + "') WHERE `vendorId` = '"
					+ in_vendorId + "' AND TYPE =  '" + in_requestType + "' " + "AND currency = '" + VendorCurrency
					+ "'";
			jdbcTemplate.execute(UpdatePOScannedQuery);

			/* UPDATE PERIOD COVERED FROM */

			String UpdatePerioedCoveredFromQuery = "UPDATE `CBSummary`  SET `periodCoveredFrom` = (SELECT MIN(`startDate`) FROM `CBAgreementInvoice` ci "
					+ "LEFT JOIN `CBAgreementRequestDetails` cd ON (ci.agreementId=cd.`agreementId`) LEFT JOIN `CBRequest` cr ON (cd.`requestId`=cr.id) "
					+ "WHERE cr.`RequestType` = '" + in_requestType
					+ "' AND startDate NOT IN ( '0001-01-01 00:00:00','0000-00-00 00:00:00')  "
					+ "AND cr.`vendorId` = '" + in_vendorId + "') Where `vendorId` = '" + in_vendorId
					+ "' AND TYPE =  '" + in_requestType + "'";
			jdbcTemplate.execute(UpdatePerioedCoveredFromQuery);

			/* UPDATE PERIOD COVERED TO */

			String UpdatePeriodCoveredToQuery = "UPDATE `CBSummary`  SET `periodCoveredTo` = (SELECT MAX(`endDate`) FROM `CBAgreementInvoice` ci "
					+ "LEFT JOIN `CBAgreementRequestDetails` cd ON (ci.agreementId=cd.`agreementId`) LEFT JOIN `CBRequest` cr ON (cd.`requestId`=cr.id) "
					+ "WHERE cr.`RequestType` = '" + in_requestType
					+ "' AND startDate NOT IN( '0001-01-01 00:00:00','0000-00-00 00:00:00')  " + "AND cr.`vendorId` = '"
					+ in_vendorId + "') Where `vendorId` = '" + in_vendorId + "' AND TYPE =  '" + in_requestType + "'";
			jdbcTemplate.execute(UpdatePeriodCoveredToQuery);

			/* UPDATE OVER BILLED IDENTIFIED */

			String UpdateOverbilledIdentifiedQuery = "UPDATE `CBSummary` SET  overbillIdentified = (SELECT IFNULL(SUM(`rebate`),0) FROM `CBIncorrectCode`"
					+ " WHERE vendorId IN ('" + in_vendorId + "') AND  currency = '" + VendorCurrency + "') "
					+ "WHERE `vendorId` = '" + in_vendorId + "' AND TYPE =  '" + in_requestType + "' AND currency = '"
					+ in_vendorId + "'";
			jdbcTemplate.execute(UpdateOverbilledIdentifiedQuery);

			return true;
		}
		return false;
	}

	public boolean UpdateDataFreighthCheck(String in_vendorId, String in_requestId, String in_requestType) {
		logger.info("----------------REQUEST TYPE : FREIGHT CHECK----------------");

		// DATA SHOULD BE DELETE OR NOT INCORRECT FRIEGHT BEFORE INSERT
		String DeleteIncorrectFreightCheckData = "DELETE CBIncorrectFreight  FROM CBIncorrectFreight INNER JOIN CBAgreementRequestDetails c1 "
				+ "ON (CBIncorrectFreight.agreementID = c1.agreementId ) INNER JOIN CBRequest c2 ON (c1.requestId = c2.id) "
				+ "WHERE c2.vendorId = '" + in_vendorId + "'  AND c1.status != 'DELETED' AND c2.id = '" + in_requestId
				+ "'";
		jdbcTemplate.execute(DeleteIncorrectFreightCheckData);
		logger.info("Deleted old Incorrect Freight(FREIGHT CHECK) Data....");

		String InsertIncorrectFreightDataQuery = "INSERT IGNORE INTO `CBIncorrectFreight` (`vendorId`,`agreementId`,`invoiceNumber`,`POASIN`,`PO`,`ASIN`,`Qty`,`freigthTerm`,`netReceipt`,`rebate`,`currency`,`createdDate`,`uniqueKey`) \r\n"
				+ "SELECT ci.`vendorId`,ci.`agreementId`,ci.`invoiceNumber`,ci.`POASIN`,ci.`PO`,ci.`ASIN`,ci.`Qty`, p.`windowType`,ci.`netReceipts`,ci.`rebate`,ci.`currency`,ci.`createdDate`,ci.`uniqueKey` \r\n"
				+ "FROM `CBAgreementInvoiceDetails` ci JOIN CBAgreementRequestDetails cr ON (ci.`agreementId` = cr.`agreementId`) INNER JOIN  CBPOData p ON(ci.PO = p.PO) JOIN `CBRequest` c ON (c.id=cr.requestID) \r\n"
				+ "WHERE c.`RequestType` = '" + in_requestType + "' AND cr.requestID= '" + in_requestId
				+ "' AND p.`windowType` LIKE '%Prepaid%' AND ci.vendorId = '" + in_vendorId + "'";
		int insertedData = jdbcTemplate.update(InsertIncorrectFreightDataQuery);
		logger.info("Inserted Incorrect Freight(FREIGHT CHECK) Data : " + insertedData);

		String CoopInvoiceCurrencyQuery = "SELECT DISTINCT vendorId,currency FROM CBAgreementInvoice WHERE vendorId = '"
				+ in_vendorId + "'";
		List<CoopInvoice> VendorCurrencyList = this.jdbcTemplate.query(CoopInvoiceCurrencyQuery.toString(),
				new RowMapper<CoopInvoice>() {
					@Override
					public CoopInvoice mapRow(ResultSet rs, int rowNum) throws SQLException {

						CoopInvoice ci = new CoopInvoice();

						ci.setVendorId(rs.getString("vendorId"));
						ci.setCurrency(rs.getString("currency"));
						return ci;
					}
				}, new Object[] {});

		for (CoopInvoice CurrencyList : VendorCurrencyList) {
			String VendorCurrency = CurrencyList.getCurrency();
			String SummaryTableQuery = "SELECT * FROM `CBSummary` WHERE vendorId = '" + in_vendorId
					+ "' AND `currency` = '" + VendorCurrency + "' AND `type` = '" + in_requestType + "'";
			List<SummaryData> SummaryDetailsList = this.jdbcTemplate.query(SummaryTableQuery.toString(),
					new RowMapper<SummaryData>() {
						@Override
						public SummaryData mapRow(ResultSet rs, int rowNum) throws SQLException {

							SummaryData summarydata = new SummaryData();

							summarydata.setId(rs.getString("id"));
							return summarydata;
						}
					}, new Object[] {});
			if (SummaryDetailsList.size() == 0) {
				String InsertSummaryNewRow = "INSERT  IGNORE INTO `CBSummary` (`vendorId`,`type`,`currency`,uniqueKey) VALUES ('"
						+ in_vendorId + "'," + "'" + in_requestType + "','" + VendorCurrency + "','" + in_vendorId
						+ in_requestType + VendorCurrency + "')";
				jdbcTemplate.execute(InsertSummaryNewRow);
				logger.info("Summary Table One New Row Inserted with Vendor Currency " + VendorCurrency);
			} else {
				logger.info("Already Inserted Summary Row Data For this Vendor Currency");
			}

			/* UPDATE AGREEMENT SCANNED */

			String UpdateAgreementScannedQuery = "UPDATE `CBSummary` SET `agreementScanned`=(SELECT IFNULL(COUNT(DISTINCT(cr.agreementId)),0) FROM CBAgreementRequestDetails cr "
					+ "JOIN `CBRequest` c ON (cr.requestId = c.id ) JOIN `CBAgreementInvoice` ci ON (cr.agreementId = ci.agreementId) WHERE   c.`RequestType` = '"
					+ in_requestType + "'" + " AND c.status != 'DELETED'  AND ci.currency = '" + VendorCurrency
					+ "' And c.vendorId In ('" + in_vendorId + "'))" + " WHERE `vendorId` = '" + in_vendorId
					+ "' AND TYPE =  '" + in_requestType + "' AND currency = '" + VendorCurrency + "'";
			jdbcTemplate.execute(UpdateAgreementScannedQuery);

			/* UPDATE INVOICE SCANNED */

			String UpdateInvoiceScannedQuery = "UPDATE `CBSummary` SET `invoiceScanned`= (SELECT IFNULL(COUNT(DISTINCT(ci.`invoiceNumber`)),0) FROM CBAgreementRequestDetails cr "
					+ "JOIN `CBRequest` c ON (cr.requestId = c.id ) JOIN `CBAgreementInvoice` ci ON (cr.agreementId = ci.agreementId) WHERE   c.`RequestType` = '"
					+ in_requestType + "' " + "AND c.status != 'DELETED'   AND c.vendorId IN ('" + in_vendorId
					+ "') AND ci.currency = '" + VendorCurrency + "') " + "WHERE `vendorId` = '" + in_vendorId
					+ "' AND TYPE =  '" + in_requestType + "' AND currency = '" + VendorCurrency + "'";
			jdbcTemplate.execute(UpdateInvoiceScannedQuery);

			/* UPDATE PO SCANNED SCANNED */

			String UpdatePOScannedQuery = "UPDATE `CBSummary` SET `poScanned`=ifNULL(`oldPOScanned`,0)+ (SELECT IFNULL(COUNT(DISTINCT(PO)),0) FROM `CBAgreementInvoiceDetails` i"
					+ " LEFT JOIN CBAgreementRequestDetails cr ON (i.agreementId= cr.agreementId) JOIN `CBRequest` c ON (cr.requestId = c.id ) "
					+ "WHERE c.`RequestType` = '" + in_requestType + "'  AND c.status != 'DELETED' AND c.vendorId IN ('"
					+ in_vendorId + "') AND " + "i.currency = '" + VendorCurrency + "') WHERE `vendorId` = '"
					+ in_vendorId + "' AND TYPE =  '" + in_requestType + "' " + "AND currency = '" + VendorCurrency
					+ "'";
			jdbcTemplate.execute(UpdatePOScannedQuery);

			/* UPDATE PERIOD COVERED FROM */

			String UpdatePerioedCoveredFromQuery = "UPDATE `CBSummary`  SET `periodCoveredFrom` = (SELECT MIN(`startDate`) FROM `CBAgreementInvoice` ci "
					+ "LEFT JOIN `CBAgreementRequestDetails` cd ON (ci.agreementId=cd.`agreementId`) LEFT JOIN `CBRequest` cr ON (cd.`requestId`=cr.id) "
					+ "WHERE cr.`RequestType` = '" + in_requestType
					+ "' AND startDate NOT IN ( '0001-01-01 00:00:00','0000-00-00 00:00:00')  "
					+ "AND cr.`vendorId` = '" + in_vendorId + "') Where `vendorId` = '" + in_vendorId
					+ "' AND TYPE =  '" + in_requestType + "'";
			jdbcTemplate.execute(UpdatePerioedCoveredFromQuery);

			/* UPDATE PERIOD COVERED TO */

			String UpdatePeriodCoveredToQuery = "UPDATE `CBSummary`  SET `periodCoveredTo` = (SELECT MAX(`endDate`) FROM `CBAgreementInvoice` ci "
					+ "LEFT JOIN `CBAgreementRequestDetails` cd ON (ci.agreementId=cd.`agreementId`) LEFT JOIN `CBRequest` cr ON (cd.`requestId`=cr.id) "
					+ "WHERE cr.`RequestType` = '" + in_requestType
					+ "' AND startDate NOT IN( '0001-01-01 00:00:00','0000-00-00 00:00:00')  " + "AND cr.`vendorId` = '"
					+ in_vendorId + "') Where `vendorId` = '" + in_vendorId + "' AND TYPE =  '" + in_requestType + "'";
			jdbcTemplate.execute(UpdatePeriodCoveredToQuery);

			/* UPDATE OVER BILLED IDENTIFIED */

			String UpdateOverbilledIdentifiedQuery = "UPDATE `CBSummary` SET  overbillIdentified = (SELECT IFNULL(SUM(`rebate`),0) FROM `CBIncorrectFreight` "
					+ "WHERE vendorId IN ('" + in_vendorId + "') AND  currency = '" + VendorCurrency
					+ "') WHERE `vendorId` = '" + in_vendorId + "' " + "AND TYPE =  '" + in_requestType
					+ "' AND currency = '" + VendorCurrency + "'";
			jdbcTemplate.execute(UpdateOverbilledIdentifiedQuery);

			return true;
		}
		return false;
	}

	public boolean UpdateKilledPromational(String in_vendorId, String in_requestId, String in_requestType) {

		String CoopInvoiceCurrencyQuery = "SELECT DISTINCT vendorId,currency "
				+ "FROM CBPromotionCheck WHERE vendorId = '" + in_vendorId
				+ "' and `promotionStatus` IN ('Canceled',''Cancelled'')";
		List<CoopInvoice> VendorCurrencyList = this.jdbcTemplate.query(CoopInvoiceCurrencyQuery.toString(),
				new RowMapper<CoopInvoice>() {
					@Override
					public CoopInvoice mapRow(ResultSet rs, int rowNum) throws SQLException {

						CoopInvoice ci = new CoopInvoice();

						ci.setVendorId(rs.getString("vendorId"));
						ci.setCurrency(rs.getString("currency"));
						return ci;
					}
				}, new Object[] {});

		for (CoopInvoice CurrencyList : VendorCurrencyList) {
			String VendorCurrency = CurrencyList.getCurrency();
			String SummaryTableQuery = "SELECT * FROM `CBSummary` WHERE vendorId = '" + in_vendorId
					+ "' AND `currency` = '" + VendorCurrency + "' AND `type` = '" + in_requestType + "'";
			List<SummaryData> SummaryDetailsList = this.jdbcTemplate.query(SummaryTableQuery.toString(),
					new RowMapper<SummaryData>() {
						@Override
						public SummaryData mapRow(ResultSet rs, int rowNum) throws SQLException {

							SummaryData summarydata = new SummaryData();

							summarydata.setId(rs.getString("id"));
							return summarydata;
						}
					}, new Object[] {});
			if (SummaryDetailsList.size() == 0) {
				String InsertSummaryNewRow = "INSERT  IGNORE INTO `CBSummary` (`vendorId`,`type`,`currency`,uniqueKey) VALUES ('"
						+ in_vendorId + "'," + "'" + in_requestType + "','" + VendorCurrency + "','" + in_vendorId
						+ "NONMARKETPLACESALE" + VendorCurrency + "')";
				jdbcTemplate.execute(InsertSummaryNewRow);
				logger.info("Summary Table One New Row Inserted with Vendor Currency " + VendorCurrency);
			} else {
				logger.info("Already Inserted Summary Row Data For this Vendor Currency");
			}

			String UpdateOverbilledIdentifiedQuery = "UPDATE `CBSummary` SET  overbillIdentified = (Select IFNULL(SUM(`rebate`),0) From `CBPromotionCheck`"
					+ " Where `vendorId`= '" + in_vendorId
					+ "' AND  `promotionStatus` IN ('Canceled','Cancelled') AND `currency` = '" + VendorCurrency + "')"
					+ " WHERE `vendorId` = '" + in_vendorId + "' AND TYPE =  '" + in_requestType + "' AND currency = '"
					+ VendorCurrency + "'";
			jdbcTemplate.execute(UpdateOverbilledIdentifiedQuery);

		}
		return true;
	}

	public boolean UpdateDataNotFeatured(String in_vendorId, String in_requestId, String in_requestType) {
		String CoopInvoiceCurrencyQuery = "SELECT DISTINCT vendorId,currency "
				+ "FROM CBPromotionCheck WHERE vendorId = '" + in_vendorId
				+ "' and `promotionStatus` IN ('Needs your attention')";
		List<CoopInvoice> VendorCurrencyList = this.jdbcTemplate.query(CoopInvoiceCurrencyQuery.toString(),
				new RowMapper<CoopInvoice>() {
					@Override
					public CoopInvoice mapRow(ResultSet rs, int rowNum) throws SQLException {

						CoopInvoice ci = new CoopInvoice();

						ci.setVendorId(rs.getString("vendorId"));
						ci.setCurrency(rs.getString("currency"));
						return ci;
					}
				}, new Object[] {});

		for (CoopInvoice CurrencyList : VendorCurrencyList) {
			String VendorCurrency = CurrencyList.getCurrency();
			String SummaryTableQuery = "SELECT * FROM `CBSummary` WHERE vendorId = '" + in_vendorId
					+ "' AND `currency` = '" + VendorCurrency + "' AND `type` = '" + in_requestType + "'";
			List<SummaryData> SummaryDetailsList = this.jdbcTemplate.query(SummaryTableQuery.toString(),
					new RowMapper<SummaryData>() {
						@Override
						public SummaryData mapRow(ResultSet rs, int rowNum) throws SQLException {

							SummaryData summarydata = new SummaryData();

							summarydata.setId(rs.getString("id"));
							return summarydata;
						}
					}, new Object[] {});
			if (SummaryDetailsList.size() == 0) {
				String InsertSummaryNewRow = "INSERT  IGNORE INTO `CBSummary` (`vendorId`,`type`,`currency`,uniqueKey) VALUES ('"
						+ in_vendorId + "'," + "'" + in_requestType + "','" + VendorCurrency + "','" + in_vendorId
						+ "NONMARKETPLACESALE" + VendorCurrency + "')";
				jdbcTemplate.execute(InsertSummaryNewRow);
				logger.info("Summary Table One New Row Inserted with Vendor Currency " + VendorCurrency);
			} else {
				logger.info("Already Inserted Summary Row Data For this Vendor Currency");
			}

			String UpdateOverbilledIdentifiedQuery = "UPDATE `CBSummary` SET  overbillIdentified = (Select IFNULL(SUM(`rebate`),0) From `CBPromotionCheck`"
					+ " Where `vendorId`= '" + in_vendorId
					+ "' AND  `promotionStatus` IN ('Needs your attention') AND `currency` = '" + VendorCurrency + "')"
					+ " WHERE `vendorId` = '" + in_vendorId + "' AND TYPE =  '" + in_requestType + "' AND currency = '"
					+ VendorCurrency + "'";
			jdbcTemplate.execute(UpdateOverbilledIdentifiedQuery);
		}

		return true;
	}

	public boolean UpdateDataShortage() {
		return true;
	}

	public boolean UpdateDataPromationalAgreementCheck(String in_vendorId, String in_requestId, String in_requestType) {

		String insertPromotinalData = "INSERT IGNORE INTO `CBNonMktPlaceSale` (`vendorId`,`agreementId`,`invoiceNumber`,`transactionType`,`quantity`,`netSales`,`netSalesCurrency`,`rebateInAgreementCurrency`,`agreementCurrency`,`ASIN`,`Ukey`) SELECT `vendorId`,`agreementId`,`invoiceNumber`,`transactionType`,`quantity`,`netSales`,`netSalesCurrency`,`rebate`,`rebateCurrency`,`asin`,`uniqueKey` FROM `CBPromotionalAgreementDetail` WHERE `rebateCurrency` != 'USD' AND `rebate` != 0"
				+ " AND requestId = '" + in_requestId + "' and vendorId = '" + in_vendorId + "'";

		logger.info(insertPromotinalData);

		jdbcTemplate.execute(insertPromotinalData);

		String CoopInvoiceCurrencyQuery = "SELECT DISTINCT vendorId,agreementCurrency as currency FROM CBNonMktPlaceSale WHERE vendorId = '"
				+ in_vendorId + "'";
		List<CoopInvoice> VendorCurrencyList = this.jdbcTemplate.query(CoopInvoiceCurrencyQuery.toString(),
				new RowMapper<CoopInvoice>() {
					@Override
					public CoopInvoice mapRow(ResultSet rs, int rowNum) throws SQLException {

						CoopInvoice ci = new CoopInvoice();

						ci.setVendorId(rs.getString("vendorId"));
						ci.setCurrency(rs.getString("currency"));
						return ci;
					}
				}, new Object[] {});

		for (CoopInvoice CurrencyList : VendorCurrencyList) {
			String VendorCurrency = CurrencyList.getCurrency();
			String SummaryTableQuery = "SELECT * FROM `CBSummary` WHERE vendorId = '" + in_vendorId
					+ "' AND `currency` = '" + VendorCurrency + "' AND `type` = 'NONMARKETPLACESALE'";
			List<SummaryData> SummaryDetailsList = this.jdbcTemplate.query(SummaryTableQuery.toString(),
					new RowMapper<SummaryData>() {
						@Override
						public SummaryData mapRow(ResultSet rs, int rowNum) throws SQLException {

							SummaryData summarydata = new SummaryData();

							summarydata.setId(rs.getString("id"));
							return summarydata;
						}
					}, new Object[] {});
			if (SummaryDetailsList.size() == 0) {
				String InsertSummaryNewRow = "INSERT  IGNORE INTO `CBSummary` (`vendorId`,`type`,`currency`,uniqueKey) VALUES ('"
						+ in_vendorId + "'," + "'" + "NONMARKETPLACESALE" + "','" + VendorCurrency + "','" + in_vendorId
						+ "NONMARKETPLACESALE" + VendorCurrency + "')";
				jdbcTemplate.execute(InsertSummaryNewRow);
				logger.info("Summary Table One New Row Inserted with Vendor Currency " + VendorCurrency);
			} else {
				logger.info("Already Inserted Summary Row Data For this Vendor Currency");
			}

			String UpdatePerioedCoveredFromQuery = "UPDATE `CBSummary`  SET `periodCoveredFrom` = (SELECT MIN(`startDate`) FROM `CBAgreementInvoice` ci "
					+ "LEFT JOIN `CBAgreementRequestDetails` cd ON (ci.agreementId=cd.`agreementId`) LEFT JOIN `CBRequest` cr ON (cd.`requestId`=cr.id) "
					+ "WHERE cr.`RequestType` = '" + in_requestType
					+ "' AND startDate NOT IN ( '0001-01-01 00:00:00','0000-00-00 00:00:00')  "
					+ "AND cr.`vendorId` = '" + in_vendorId + "') Where `vendorId` = '" + in_vendorId
					+ "' AND TYPE =  'NONMARKETPLACESALE'";
			jdbcTemplate.execute(UpdatePerioedCoveredFromQuery);

			/* UPDATE PERIOD COVERED TO */

			String UpdatePeriodCoveredToQuery = "UPDATE `CBSummary`  SET `periodCoveredTo` = (SELECT MAX(`endDate`) FROM `CBAgreementInvoice` ci "
					+ "LEFT JOIN `CBAgreementRequestDetails` cd ON (ci.agreementId=cd.`agreementId`) LEFT JOIN `CBRequest` cr ON (cd.`requestId`=cr.id) "
					+ "WHERE cr.`RequestType` = '" + in_requestType
					+ "' AND startDate NOT IN( '0001-01-01 00:00:00','0000-00-00 00:00:00')  " + "AND cr.`vendorId` = '"
					+ in_vendorId + "') Where `vendorId` = '" + in_vendorId + "' AND TYPE =  'NONMARKETPLACESALE'";
			jdbcTemplate.execute(UpdatePeriodCoveredToQuery);

			/* UPDATE OVER BILLED IDENTIFIED */

			String UpdateOverbilledIdentifiedQuery = "UPDATE `CBSummary` SET  overbillIdentified = (SELECT IFNULL(SUM(`rebateInAgreementCurrency`),0) FROM `CBNonMktPlaceSale` "
					+ "WHERE vendorId = '" + in_vendorId + "' AND `agreementCurrency` = '" + VendorCurrency
					+ "') WHERE `vendorId` = '" + in_vendorId + "' " + "AND TYPE =  'NONMARKETPLACESALE'"
					+ " AND currency = '" + VendorCurrency + "'";
			jdbcTemplate.execute(UpdateOverbilledIdentifiedQuery);

		}

		return true;
	}

}
