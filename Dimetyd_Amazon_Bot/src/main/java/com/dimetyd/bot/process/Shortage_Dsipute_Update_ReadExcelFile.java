package com.dimetyd.bot.process;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.VendorCrd;


@Component
public class Shortage_Dsipute_Update_ReadExcelFile {


	@Autowired
	JdbcTemplate jdbcTemplate;

	public void readExcel(Path filepath, String disputeId, String vendorId) throws IOException {
		FileInputStream file = new FileInputStream(filepath.toString());
		XSSFWorkbook workbook = new XSSFWorkbook(file);

		XSSFSheet sheet = workbook.getSheetAt(0);

		for (Row row : sheet) {
			if (row.getRowNum() == 0) {
				// Skip header row
				continue;
			}

			// Read cell values (assuming first cell is name, second is age, etc.)
			String invoiceNumber = row.getCell(0).getStringCellValue();
	
			String PayeeCode = row.getCell(3).getStringCellValue();
			int invoiceDate = (int) row.getCell(4).getNumericCellValue();
			Calendar calendar = Calendar.getInstance();
			calendar.set(1900, Calendar.JANUARY, 1);
			calendar.add(Calendar.DAY_OF_YEAR, invoiceDate - 1);
			Date date = calendar.getTime();
			// Define the format
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
			// Format the date to a string
			String formattedInvoiceDate = dateFormat1.format(date);
			// Print the formatted date
			System.out.println("Formatted Invoice Date: " + formattedInvoiceDate);
			String ASIN = row.getCell(8).getStringCellValue();
			String externalId = row.getCell(7).getStringCellValue();
			double ShortageQuantity = row.getCell(14).getNumericCellValue();
			double shortageAmount = row.getCell(15).getNumericCellValue();
			double disputeQuantity = row.getCell(16).getNumericCellValue();
			double disputeAmount = row.getCell(17).getNumericCellValue();
			double unitPrice = row.getCell(11).getNumericCellValue();
			double ApprovedQuantity = row.getCell(19).getNumericCellValue();
			String disputeDecision = row.getCell(20).getStringCellValue();
			double dueDate = row.getCell(5).getNumericCellValue();
			calendar = Calendar.getInstance();
			calendar.set(1900, Calendar.JANUARY, 1);
			calendar.add(Calendar.DAY_OF_YEAR, (int) (dueDate - 2));
			date = calendar.getTime();
			// Define the format
			dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
			// Format the date to a string
			String formattedDueDate = dateFormat1.format(date);
			// Print the formatted date
			System.out.println("Formatted Due Date: " + formattedDueDate);
			double invoicedQuantity = row.getCell(12).getNumericCellValue();
			String PO = row.getCell(6).getStringCellValue();
			String ukey = PO + ASIN + invoiceNumber + disputeId;

			String checkSql = "SELECT * FROM CBItemizedShortages WHERE disputeId = '" + disputeId + "' and vendorId='"+vendorId+"' and po = '"+PO+"'  and `asin`='"+ASIN+"';";

			List<VendorCrd> cbitemeizedData = this.jdbcTemplate.query(checkSql, new RowMapper<VendorCrd>() {
				@Override
				public VendorCrd mapRow(ResultSet rs, int rowNum) throws SQLException {

					VendorCrd Cbdata = new VendorCrd();

					return Cbdata;
				}
			}, new Object[] {});
			System.out.println("CB itemized Data Size : "+cbitemeizedData.size());
			if (cbitemeizedData.size() > 0) {
				System.out.println("Dispute Id data is already present");
				String updateSql = "UPDATE CBItemizedShortages SET " + "disputeDecision = '" + disputeDecision
						+ "', vendorId = '" + vendorId + "', po = '" + PO + "', asin = '" + ASIN + "', shortageQty = '"
						+ ShortageQuantity + "', disputedQty = '" + disputeQuantity + "', " + "perUnit = '" + unitPrice
						+ "', shortageAmount = '" + shortageAmount + "', disputeAmount = '" + disputeAmount
						+ "', invoiceNumber = '" + invoiceNumber +  "', " + "approvedQuantity = '" + ApprovedQuantity + "', externalId = '"
						+ externalId + "', dueDate = '" + formattedDueDate + "',invoiceDate = '" + formattedInvoiceDate
						+ "', invoicedQuantity = '" + invoicedQuantity + "', payeeCode = '" + PayeeCode
						+ "' WHERE disputeId = '" + disputeId + "' and vendorId='"+vendorId+"' and po = '"+PO+"'  and `asin`='"+ASIN+"';";
				System.out.println(updateSql.toString());
				jdbcTemplate.execute(updateSql);
			} else {
				System.out.println("Dispute Id data is not present");
				StringBuilder sql = new StringBuilder();
				sql.append(
						"INSERT IGNORE INTO `CBItemizedShortages` (disputeDecision,`vendorId`,`disputeId`,`po`,`asin`,`shortageQty`,`disputedQty`,`perUnit`,`shortageAmount`,"
								+ "`disputeAmount`,`invoiceNumber`,`ukey`,`createdDate`,`approvedQuantity`,`externalId`,`dueDate`,`invoiceDate`,`invoicedQuantity`,`payeeCode`)"
								+ "VALUES('" + disputeDecision + "','" + vendorId + "','" + disputeId + "'," + "'"
								+ PO.trim() + "','" + ASIN.trim() + "','" + ShortageQuantity + "','" + disputeQuantity
								+ "'," + "'" + unitPrice + "','" + shortageAmount + "'," + "'" + disputeAmount + "','"
								+ invoiceNumber + "','" + ukey + "',NOW()," + "'" + 
								+ ApprovedQuantity + "','" + externalId + "'," + "'" + formattedDueDate + "','"
								+ formattedInvoiceDate + "','" + invoicedQuantity + "','" + PayeeCode + "')");
				System.out.println(sql.toString());
				jdbcTemplate.execute(sql.toString());
			}
			System.out.println("invoiceNumber: " + invoiceNumber + " disputeAmount: " + disputeAmount);

		}

		workbook.close();
	}



}
