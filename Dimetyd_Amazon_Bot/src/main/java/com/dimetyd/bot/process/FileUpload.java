package com.dimetyd.bot.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;


@Component
public class FileUpload {

	@Autowired
	JdbcTemplate jdbcTemplate;

	
	
	public List<String> insertData() {
        List<String> vendorIds = Arrays.asList(
                "VN20240503074255",
                "VN20230519055356",
                "VN21022024022800",
                "VN20240826020613",
                "VN20241210093431",
                "VN20221108020130",
                "VN20221108020133",
                "VN20221108020139",
                "VN20240327025923",
                "VN20220823020249",
                "VN20240606033025",
                "VN20240626093442",
                "VN20220713011117",
                "VN20221108020142",
                "VN20250529115106",
                "VN20230321055232",
                "VN20241009080909",
                "VN20220217024245",
                "VN20231130100247",
                "VN20230307040313",
                "VN20230821100253",
                "VN20230927080225",
                "VN20231108090818",
                "VN20230530110349",
                "VN20221029030150",
                "VN20240614032928",
                "VN20221124050153",
                "VN20022024022600",
                "VN20220228081541",
                "VN20220820030119",
                "VN20221220075755",
                "VN20250610102446",
                "VN20230320045357",
                "VN20230414061600",
                "VN20230215062837",
                "VN20220527112819",
                "VN20230404040209",
                "VN20230513020312",
                "VN20240403041909",
                "VN20230405105904",
                "VN20240712092459",
                "VN20230627120400",
                "VN20230706100305",
                "VN01172022122801",
                "VN20231013105935",
                "VN20231103011814",
                "VN20231116043631");

        List<String> list = new ArrayList<>();

        for (String vendorId : vendorIds) {
        	
        	System.out.println("Started vendorId: "+ vendorId);
            StringBuilder sql = new StringBuilder();
            sql.append(
                    "INSERT ignore INTO CB_Open_Shortage_Reconciliation_New_Output (paymentNumber,vendorId,invoiceNumber,parentInvoice,invoiceDate,Description,vendorCode,invoiceAmount,\n"
                            + //
                            "termsDiscountTaken,amountPaid,remainingAmountAsOf,invoiceType,createdDate,status,comments,paymentDate,currency,discount,invoiceTypeDetailed,paymentDueDate,payeeCode,\n"
                            + //
                            "disputeId,disputeType,disputeStatus,disputeDate,disputedAmount,disputeResolvedAmount,batchId\n"
                            + //
                            ")\n" + //
                            "SELECT distinct cb.paymentNumber ,cb.vendorId  ,cb.invoiceNumber,cb.parentInvoice,cb.invoiceDate,cb.Description,cb.vendorCode,cb.invoiceAmount,cb.termsDiscountTaken,\n"
                            + //
                            "cb.amountPaid,cb.remainingAmountAsOf,cb.invoiceType,cb.createdDate,cb.status,cb.comments,cb.paymentDate,cb.currency,cb.discount,cb.invoiceTypeDetailed,cb.paymentDueDate,\n"
                            + //
                            "cb.payeeCode , o.dispute_id as disputeId ,o.dispute_type as  disputeType ,o.dispute_status as disputeStatus , null AS disputeDate ,null  AS disputedAmount , null  AS DisputeResolvedAmount,\n"
                            + //
                            "CONCAT(cb.vendorId,'_',DATE_FORMAT(curdate(), '%m%d%y')) AS batchId\n" + //
                            " FROM CBShortageReconciliation_output cb LEFT JOIN opendisputeInput_copy o ON (cb.invoiceNumber = o.original_invoice_id)\n"
                            + //
                            "JOIN Vendor v ON (v.id = cb.vendorId) WHERE cb.vendorId = ?");
            int row = jdbcTemplate.update(sql.toString(), vendorId);
            list.add(vendorId + " - " + row + " rows inserted in CB_Open_Shortage_Reconciliation_New_Output");
            sql = new StringBuilder();
            sql.append(
                    "insert into CB_Open_Shortage_Disputes_To_Submit (`vendorId`,`payee`,`invoiceNumber`,`invoiceDate`,`invoiceAmount`,`submitType`,`disputeId`,\n"
                            + //
                            "`disputeStatus` ,`batchId`)SELECT `vendorId`,`payeeCode`,`invoiceNumber`,`invoiceDate`,`amountPaid`,'Shortage Re-dispute',`disputeId`,`disputeStatus`,`batchId`\n"
                            + //
                            "FROM CB_Open_Shortage_Reconciliation_New_Output where `disputeType` = 'Shortage invoice' and vendorId= ?\n"
                            + //
                            "Union SELECT `vendorId`,`payeeCode`,`invoiceNumber`,`invoiceDate`,`amountPaid`,'Shortage Dispute',disputeId,`disputeStatus`,`batchId` FROM CB_Open_Shortage_Reconciliation_New_Output \n"
                            + //
                            "where `disputeType` is NUll and vendorId=?");
            int row2 = jdbcTemplate.update(sql.toString(), vendorId, vendorId);
            list.add(vendorId + " - " + row2 + " rows inserted in CB_Open_Shortage_Disputes_To_Submit");
            sql = new StringBuilder();
            sql.append("INSERT INTO CB_Open_Shortage_Summary (\n" + //
                    "    id,\n" + //
                    "    currency,\n" + //
                    "    minPaymentDue,\n" + //
                    "    maxPaymentDue,\n" + //
                    "    periodStart,\n" + //
                    "    periodEnd,\n" + //
                    "    batchId,\n" + //
                    "    unsettledLineItems,\n" + //
                    "    unsettledShortages,\n" + //
                    "    findingsCompletedDate,\n" + //
                    "    vendorId\n" + //
                    ")\n" + //
                    "SELECT\n" + //
                    "    shortageSummaryId,\n" + //
                    "    currency,\n" + //
                    "    MIN(paymentDueDate) AS minPaymentDue,\n" + //
                    "    MAX(paymentDueDate) AS maxPaymentDue,\n" + //
                    "    MIN(invoiceDate) AS stdate,\n" + //
                    "    MAX(invoiceDate) AS enDate,\n" + //
                    "    batchId,\n" + //
                    "    COUNT(id) AS unsettledLineItems,\n" + //
                    "    SUM(amountPaid) + SUM(termsDiscountTaken) AS unsettledShortages,\n" + //
                    "    NOW() AS findingsCompletedDate,\n" + //
                    "    vendorId\n" + //
                    "FROM CB_Open_Shortage_Reconciliation_New_Output\n" + //
                    "WHERE vendorId = ?\n" + //
                    "GROUP BY shortageSummaryId, currency, batchId;\n" + //
                    "\n");

            int row3 = jdbcTemplate.update(sql.toString(), vendorId);
            list.add(vendorId + " - " + row3 + " rows inserted in CB_Open_Shortage_Summary");
        }
        return list;

    }
}
