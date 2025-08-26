package com.dimetyd.bot.process;

import java.io.FileInputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Coop_Dispute_Update_ReadExcelFile {

	private Logger logger = LoggerFactory.getLogger(getClass());

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
			String invoiceNumber = row.getCell(1).getStringCellValue();
			String agreemenrtNumber = row.getCell(2).getStringCellValue();
			String vendorCode = row.getCell(3).getStringCellValue();
			int invoiceDate = (int) row.getCell(4).getNumericCellValue();
			Calendar calendar = Calendar.getInstance();
			calendar.set(1900, Calendar.JANUARY, 1);
			calendar.add(Calendar.DAY_OF_YEAR, invoiceDate - 2);
			Date date = calendar.getTime();
			// Define the format
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			// Format the date to a string
			String formattedDate = dateFormat.format(date);
			// Print the formatted date

			double OriginalBalance = row.getCell(5).getNumericCellValue();
			String paymentType = row.getCell(6).getStringCellValue();
			String disputeReason = row.getCell(7).getStringCellValue();
			double disputeAmount = row.getCell(8).getNumericCellValue();

			StringBuilder sql = new StringBuilder();
			sql.append(
					"INSERT INTO `CBDisputeDetail` (`disputeId`,`vendorId`,`disputeAmount`,`invoiceNumber`,`agreementNumber`,`vendorCode`,`invoiceDate`,`originalBalance`,`paymentType`,`disputeReason`)\r\n"
							+ "VALUES ('" + disputeId + "','" + vendorId + "','" + disputeAmount + "','" + invoiceNumber
							+ "','" + agreemenrtNumber + "','" + vendorCode + "','" + formattedDate + "','"
							+ OriginalBalance + "','" + paymentType + "','" + disputeReason + "');");

			jdbcTemplate.execute(sql.toString());

		}

		workbook.close();
	}

}
